from pydantic import BaseModel, ConfigDict, Field


class CamelModel(BaseModel):
    model_config = ConfigDict(populate_by_name=True)


class ArtistPreference(CamelModel):
    artist: str
    weight: float


class CandidateShow(CamelModel):
    show_id: int = Field(..., alias="showId")
    artist: str
    title: str
    venue: str | None = None
    embedding_text: str | None = Field(default=None, alias="embeddingText")
    ticketing_state: str | None = Field(default=None, alias="ticketingState")
    show_state: str | None = Field(default=None, alias="showState")
    show_start_date: str | None = Field(default=None, alias="showStartDate")


class RecommendationRequest(CamelModel):
    user_id: int = Field(..., alias="userId")
    user_profile_text: str | None = Field(default=None, alias="userProfileText")
    artist_preferences: list[ArtistPreference] = Field(default_factory=list, alias="artistPreferences")
    recent_keywords: list[str] = Field(default_factory=list, alias="recentKeywords")
    candidates: list[CandidateShow]


class RecommendationItem(CamelModel):
    show_id: int = Field(..., alias="showId")
    score: float
    reason: str


class RecommendationResponse(CamelModel):
    recommendations: list[RecommendationItem]


class HealthResponse(CamelModel):
    status: str
