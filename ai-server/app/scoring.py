from __future__ import annotations

from math import sqrt

from app.schemas import ArtistPreference, CandidateShow, TagWeight

TAG_SCORE_WEIGHT = 0.75
ARTIST_BONUS_MAX = 0.15
SEARCH_BONUS_MAX = 0.10


def normalize_text(value: str | None) -> str:
    if not value:
        return ""
    return " ".join(value.strip().lower().split())


def to_weight_map(items: list[TagWeight]) -> dict[int, float]:
    return {item.tag_id: item.weight for item in items}


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


def compute_tag_similarity(user_profile: list[TagWeight], candidate_tags: list[TagWeight]) -> float:
    return cosine_similarity(to_weight_map(user_profile), to_weight_map(candidate_tags))


def compute_artist_bonus(candidate_artist: str, artist_preferences: list[ArtistPreference]) -> float:
    normalized_candidate = normalize_text(candidate_artist)
    if not normalized_candidate:
        return 0.0

    for preference in artist_preferences:
        if normalize_text(preference.artist) == normalized_candidate:
            normalized_weight = min(preference.weight / 2.0, 1.0)
            return ARTIST_BONUS_MAX * normalized_weight
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


def build_reason(tag_similarity: float, artist_bonus: float, search_bonus: float) -> str:
    reasons: list[str] = []

    if tag_similarity >= 0.55:
        reasons.append("선호 태그가 유사함")
    elif tag_similarity > 0.0:
        reasons.append("관심 태그와 일부 일치함")

    if artist_bonus > 0.0:
        reasons.append("동일 아티스트 공연")

    if search_bonus > 0.0:
        reasons.append("최근 검색어와 관련됨")

    if not reasons:
        return "기본 추천 로직으로 선정된 공연"

    return " + ".join(reasons)
