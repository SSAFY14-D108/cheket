from app.scoring import (
    EMBEDDING_SCORE_WEIGHT,
    TAG_SCORE_WEIGHT,
    build_reason,
    compute_artist_bonus,
    compute_embedding_similarity,
    compute_freshness_bonus,
    compute_search_bonus,
    compute_tag_similarity,
)
from app.schemas import RecommendationItem, RecommendationRequest, RecommendationResponse


def get_recommendations(payload: RecommendationRequest) -> RecommendationResponse:
    recommendations: list[RecommendationItem] = []

    for candidate in payload.candidates:
        embedding_similarity = compute_embedding_similarity(payload.user_embedding, candidate.embedding)
        tag_similarity = compute_tag_similarity(payload.user_profile, candidate.tags)
        artist_bonus = compute_artist_bonus(candidate.artist, payload.artist_preferences)
        search_bonus = compute_search_bonus(candidate, payload.recent_keywords)
        freshness_bonus = compute_freshness_bonus(candidate.ticketing_state, candidate.show_state)

        final_score = min(
            (embedding_similarity * EMBEDDING_SCORE_WEIGHT)
            + (tag_similarity * TAG_SCORE_WEIGHT)
            + artist_bonus
            + search_bonus
            + freshness_bonus,
            1.0,
        )
        reason = build_reason(
            embedding_similarity=embedding_similarity,
            tag_similarity=tag_similarity,
            artist_bonus=artist_bonus,
            search_bonus=search_bonus,
            freshness_bonus=freshness_bonus,
        )

        recommendations.append(
            RecommendationItem(showId=candidate.show_id, score=round(final_score, 4), reason=reason)
        )

    recommendations.sort(key=lambda item: item.score, reverse=True)
    return RecommendationResponse(recommendations=recommendations)
