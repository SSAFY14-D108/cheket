from app.scoring import (
    EMBEDDING_SCORE_WEIGHT,
    TAG_SCORE_WEIGHT,
    build_reason,
    collect_matching_tag_names,
    compute_artist_bonus,
    compute_artist_diversity_penalty,
    compute_embedding_similarity,
    compute_freshness_bonus,
    compute_search_bonus,
    compute_tag_similarity,
    compute_timing_bonus,
    has_personalization_signal,
    normalize_text,
)
from app.schemas import CandidateShow, RecommendationItem, RecommendationRequest, RecommendationResponse


def get_recommendations(payload: RecommendationRequest) -> RecommendationResponse:
    recommendations: list[RecommendationItem] = []
    candidate_by_id = {candidate.show_id: candidate for candidate in payload.candidates}
    personalized = has_personalization_signal(
        payload.user_embedding,
        payload.user_profile,
        payload.artist_preferences,
        payload.recent_keywords,
    )

    for candidate in payload.candidates:
        embedding_similarity = compute_embedding_similarity(payload.user_embedding, candidate.embedding)
        tag_similarity = compute_tag_similarity(payload.user_profile, candidate.tags)
        artist_bonus = compute_artist_bonus(candidate.artist, payload.artist_preferences)
        search_bonus = compute_search_bonus(candidate, payload.recent_keywords)
        freshness_bonus = compute_freshness_bonus(candidate.ticketing_state, candidate.show_state)
        timing_bonus = compute_timing_bonus(candidate.show_start_date, candidate.ticketing_state, candidate.show_state)
        matching_agencies = collect_matching_tag_names(payload.user_profile, candidate.tags, "AGENCY")
        matching_segments = collect_matching_tag_names(payload.user_profile, candidate.tags, "ARTIST_SEGMENT")

        final_score = min(
            (embedding_similarity * EMBEDDING_SCORE_WEIGHT)
            + (tag_similarity * TAG_SCORE_WEIGHT)
            + artist_bonus
            + search_bonus
            + freshness_bonus
            + timing_bonus,
            1.0,
        )

        # cold-start 유저는 개인화 신호가 약하므로 일정/예매 상태 기반 점수를 그대로 살린다.
        if not personalized and final_score == 0.0:
            final_score = max(freshness_bonus, timing_bonus)

        reason = build_reason(
            embedding_similarity=embedding_similarity,
            tag_similarity=tag_similarity,
            artist_bonus=artist_bonus,
            search_bonus=search_bonus,
            freshness_bonus=freshness_bonus,
            timing_bonus=timing_bonus,
            matching_agencies=matching_agencies,
            matching_segments=matching_segments,
            personalized=personalized,
        )

        recommendations.append(
            RecommendationItem(showId=candidate.show_id, score=round(final_score, 4), reason=reason)
        )

    recommendations.sort(key=lambda item: item.score, reverse=True)
    reranked = rerank_with_artist_diversity(recommendations, candidate_by_id)
    return RecommendationResponse(recommendations=reranked)


def rerank_with_artist_diversity(
    recommendations: list[RecommendationItem],
    candidate_by_id: dict[int, CandidateShow],
) -> list[RecommendationItem]:
    selected: list[RecommendationItem] = []
    remaining = list(recommendations)
    seen_artists: dict[str, int] = {}

    while remaining:
        best_index = 0
        best_score = -1.0

        for index, item in enumerate(remaining):
            candidate = candidate_by_id.get(item.show_id)
            artist_name = candidate.artist if candidate else ""
            diversity_penalty = compute_artist_diversity_penalty(artist_name, seen_artists)
            adjusted_score = max(item.score - diversity_penalty, 0.0)

            if adjusted_score > best_score:
                best_score = adjusted_score
                best_index = index

        chosen = remaining.pop(best_index)
        chosen_candidate = candidate_by_id.get(chosen.show_id)
        chosen_artist = normalize_text(chosen_candidate.artist if chosen_candidate else "")

        selected.append(
            RecommendationItem(
                showId=chosen.show_id,
                score=round(best_score, 4),
                reason=chosen.reason,
            )
        )

        if chosen_artist:
            seen_artists[chosen_artist] = seen_artists.get(chosen_artist, 0) + 1

    return selected
