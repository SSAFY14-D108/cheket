from app.schemas import RecommendationItem, RecommendationRequest, RecommendationResponse


def get_recommendations(payload: RecommendationRequest) -> RecommendationResponse:
    # Placeholder ranking until the actual scoring logic is added.
    recommendations = [
        RecommendationItem(showId=candidate.show_id, score=0.0, reason="stub")
        for candidate in payload.candidates
    ]
    return RecommendationResponse(recommendations=recommendations)
