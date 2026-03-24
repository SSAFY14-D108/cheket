from fastapi import FastAPI

from app.schemas import HealthResponse, RecommendationRequest, RecommendationResponse
from app.service import get_recommendations


app = FastAPI(title="CHEKET AI Recommendation Server", version="0.1.0")


@app.get("/health", response_model=HealthResponse)
def health_check() -> HealthResponse:
    return HealthResponse(status="ok")


@app.post("/api/v1/recommendations", response_model=RecommendationResponse)
def recommend(payload: RecommendationRequest) -> RecommendationResponse:
    return get_recommendations(payload)
