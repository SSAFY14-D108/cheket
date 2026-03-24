from __future__ import annotations

from datetime import date
from math import sqrt

from app.schemas import ArtistPreference, CandidateShow, TagWeight

EMBEDDING_SCORE_WEIGHT = 0.30
TAG_SCORE_WEIGHT = 0.30
ARTIST_SCORE_WEIGHT = 0.20
SEARCH_BONUS_MAX = 0.10
FRESHNESS_BONUS_MAX = 0.10
TIMING_BONUS_MAX = 0.08
DIVERSITY_PENALTY_STEP = 0.06
DIVERSITY_PENALTY_CAP = 0.18
GENERIC_ARTIST_VALUES = {
    "기타",
    "various artists",
    "various artist",
    "various",
    "unknown artist",
    "unknown",
}
SEGMENT_REASON_LABELS = {
    "MALE_IDOL": "남자 아이돌 계열과 유사함",
    "FEMALE_IDOL": "여자 아이돌 계열과 유사함",
    "BAND": "선호 밴드 계열과 유사함",
}


def normalize_text(value: str | None) -> str:
    if not value:
        return ""
    return " ".join(value.strip().lower().split())


def is_meaningful_artist(value: str | None) -> bool:
    normalized = normalize_text(value)
    return bool(normalized) and normalized not in GENERIC_ARTIST_VALUES


def to_weight_map(items: list[TagWeight]) -> dict[int, float]:
    return {item.tag_id: item.weight for item in items}


def collect_matching_tag_names(
    user_profile: list[TagWeight],
    candidate_tags: list[TagWeight],
    category: str,
) -> list[str]:
    normalized_category = normalize_text(category)
    user_tag_names = {
        normalize_text(tag.name): tag.name
        for tag in user_profile
        if normalize_text(tag.category) == normalized_category and tag.name
    }
    candidate_tag_names = {
        normalize_text(tag.name): tag.name
        for tag in candidate_tags
        if normalize_text(tag.category) == normalized_category and tag.name
    }

    shared_names = set(user_tag_names.keys()) & set(candidate_tag_names.keys())
    return sorted(candidate_tag_names[name] for name in shared_names)


def cosine_similarity(left: dict[int, float], right: dict[int, float]) -> float:
    if not left or not right:
        return 0.0

    shared_ids = set(left.keys()) & set(right.keys())
    dot_product = sum(left[tag_id] * right[tag_id] for tag_id in shared_ids)
    left_norm = sqrt(sum(weight * weight for weight in left.values()))
    right_norm = sqrt(sum(weight * weight for weight in right.values()))

    if left_norm == 0.0 or right_norm == 0.0:
        return 0.0

    return dot_product / (left_norm * right_norm)


def cosine_similarity_dense(left: list[float], right: list[float]) -> float:
    if not left or not right or len(left) != len(right):
        return 0.0

    dot_product = sum(l * r for l, r in zip(left, right, strict=False))
    left_norm = sqrt(sum(value * value for value in left))
    right_norm = sqrt(sum(value * value for value in right))

    if left_norm == 0.0 or right_norm == 0.0:
        return 0.0

    return dot_product / (left_norm * right_norm)


def compute_embedding_similarity(user_embedding: list[float], candidate_embedding: list[float]) -> float:
    return cosine_similarity_dense(user_embedding, candidate_embedding)


def compute_tag_similarity(user_profile: list[TagWeight], candidate_tags: list[TagWeight]) -> float:
    return cosine_similarity(to_weight_map(user_profile), to_weight_map(candidate_tags))


def compute_artist_bonus(candidate_artist: str, artist_preferences: list[ArtistPreference]) -> float:
    normalized_candidate = normalize_text(candidate_artist)
    if not is_meaningful_artist(normalized_candidate):
        return 0.0

    for preference in artist_preferences:
        if not is_meaningful_artist(preference.artist):
            continue

        if normalize_text(preference.artist) == normalized_candidate:
            normalized_weight = min(preference.weight / 2.0, 1.0)
            return ARTIST_SCORE_WEIGHT * normalized_weight
    return 0.0


def compute_search_bonus(candidate: CandidateShow, recent_keywords: list[str]) -> float:
    if not recent_keywords:
        return 0.0

    artist = normalize_text(candidate.artist)
    title = normalize_text(candidate.title)
    venue = normalize_text(candidate.venue)
    tag_names = [normalize_text(tag.name) for tag in candidate.tags if tag.name]

    best_bonus = 0.0
    for keyword in recent_keywords:
        normalized_keyword = normalize_text(keyword)
        if not normalized_keyword:
            continue

        if normalized_keyword in artist or normalized_keyword in title:
            best_bonus = max(best_bonus, SEARCH_BONUS_MAX)
            continue

        if venue and normalized_keyword in venue:
            best_bonus = max(best_bonus, 0.07)

        if any(normalized_keyword == tag_name or normalized_keyword in tag_name for tag_name in tag_names):
            best_bonus = max(best_bonus, 0.05)

    return min(best_bonus, SEARCH_BONUS_MAX)


def compute_freshness_bonus(ticketing_state: str | None, show_state: str | None) -> float:
    normalized_ticketing_state = normalize_text(ticketing_state)
    normalized_show_state = normalize_text(show_state)

    if normalized_show_state == "upcoming" and normalized_ticketing_state == "in_progress":
        return FRESHNESS_BONUS_MAX
    if normalized_show_state == "upcoming" and normalized_ticketing_state == "before_open":
        return 0.07
    if normalized_show_state == "ongoing":
        return 0.06
    return 0.0


def parse_show_start_date(show_start_date: str | None) -> date | None:
    if not show_start_date:
        return None

    try:
        return date.fromisoformat(show_start_date)
    except ValueError:
        return None


def compute_timing_bonus(
    show_start_date: str | None,
    ticketing_state: str | None,
    show_state: str | None,
    *,
    today: date | None = None,
) -> float:
    normalized_show_state = normalize_text(show_state)
    normalized_ticketing_state = normalize_text(ticketing_state)
    parsed_show_start_date = parse_show_start_date(show_start_date)

    if normalized_show_state != "upcoming" or parsed_show_start_date is None:
        return 0.0

    reference_day = today or date.today()
    days_until_show = (parsed_show_start_date - reference_day).days
    if days_until_show < 0:
        return 0.0

    state_bonus = 0.0
    if normalized_ticketing_state == "in_progress":
        state_bonus = 0.04
    elif normalized_ticketing_state == "before_open":
        state_bonus = 0.025

    proximity_bonus = 0.0
    if days_until_show <= 7:
        proximity_bonus = 0.04
    elif days_until_show <= 14:
        proximity_bonus = 0.03
    elif days_until_show <= 30:
        proximity_bonus = 0.02
    elif days_until_show <= 60:
        proximity_bonus = 0.01

    return min(state_bonus + proximity_bonus, TIMING_BONUS_MAX)


def has_personalization_signal(
    user_embedding: list[float],
    user_profile: list[TagWeight],
    artist_preferences: list[ArtistPreference],
    recent_keywords: list[str],
) -> bool:
    return bool(user_embedding or user_profile or artist_preferences or recent_keywords)


def compute_artist_diversity_penalty(candidate_artist: str, seen_artists: dict[str, int]) -> float:
    normalized_artist = normalize_text(candidate_artist)
    if not is_meaningful_artist(normalized_artist):
        return 0.0

    duplicate_count = seen_artists.get(normalized_artist, 0)
    if duplicate_count <= 0:
        return 0.0

    return min(duplicate_count * DIVERSITY_PENALTY_STEP, DIVERSITY_PENALTY_CAP)


def build_reason(
    embedding_similarity: float,
    tag_similarity: float,
    artist_bonus: float,
    search_bonus: float,
    freshness_bonus: float,
    timing_bonus: float = 0.0,
    matching_agencies: list[str] | None = None,
    matching_segments: list[str] | None = None,
    personalized: bool = True,
) -> str:
    reasons: list[str] = []
    normalized_segments = [normalize_text(segment).upper() for segment in (matching_segments or []) if segment]

    if embedding_similarity >= 0.45:
        reasons.append("임베딩 유사도가 높음")

    if matching_agencies:
        reasons.append("같은 소속사 계열 공연")

    segment_reason = next(
        (SEGMENT_REASON_LABELS[segment] for segment in normalized_segments if segment in SEGMENT_REASON_LABELS),
        None,
    )
    if segment_reason:
        reasons.append(segment_reason)

    if tag_similarity >= 0.55:
        reasons.append("선호 태그가 유사함")
    elif tag_similarity > 0.0:
        reasons.append("관심 태그와 일부 일치함")

    if artist_bonus > 0.0:
        reasons.append("동일 아티스트 공연")

    if search_bonus > 0.0:
        reasons.append("최근 검색어와 관련됨")

    if freshness_bonus >= 0.07:
        reasons.append("현재 예매 시점과도 잘 맞음")
    elif timing_bonus >= 0.03:
        reasons.append("공연 일정이 가까움")

    if not personalized and not reasons:
        return "입문자용 기본 추천 공연"

    if not reasons:
        return "기본 추천 로직으로 선정된 공연"

    return " + ".join(reasons)
