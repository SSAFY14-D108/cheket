from pydantic import BaseModel, Field


class TagWeight(BaseModel):
    tag_id: int = Field(..., alias="tagId")
    weight: float


class CandidateShow(BaseModel):
    show_id: int = Field(..., alias="showId")
    artist: str
    title: str
    venue: str | None = None
    tags: list[TagWeight]


class RecommendationRequest(BaseModel):
    user_id: int = Field(..., alias="userId")
    user_profile: list[TagWeight] = Field(..., alias="userProfile")
    recent_keywords: list[str] = Field(default_factory=list, alias="recentKeywords")
    candidates: list[CandidateShow]


class RecommendationItem(BaseModel):
    show_id: int = Field(..., alias="showId")
    score: float
    reason: str


class RecommendationResponse(BaseModel):
    recommendations: list[RecommendationItem]


class HealthResponse(BaseModel):
    status: str
