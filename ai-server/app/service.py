from app.scoring import TAG_SCORE_WEIGHT, build_reason, compute_artist_bonus, compute_search_bonus, compute_tag_similarity
from app.schemas import RecommendationItem, RecommendationRequest, RecommendationResponse


def get_recommendations(payload: RecommendationRequest) -> RecommendationResponse:
    recommendations: list[RecommendationItem] = []

    for candidate in payload.candidates:
        tag_similarity = compute_tag_similarity(payload.user_profile, candidate.tags)
        artist_bonus = compute_artist_bonus(candidate.artist, payload.artist_preferences)
        search_bonus = compute_search_bonus(candidate, payload.recent_keywords)

        final_score = min((tag_similarity * TAG_SCORE_WEIGHT) + artist_bonus + search_bonus, 1.0)
        reason = build_reason(tag_similarity, artist_bonus, search_bonus)

        recommendations.append(
            RecommendationItem(showId=candidate.show_id, score=round(final_score, 4), reason=reason)
        )

    recommendations.sort(key=lambda item: item.score, reverse=True)
    return RecommendationResponse(recommendations=recommendations)
