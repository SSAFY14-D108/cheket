import unittest
from datetime import date, timedelta

from app.scoring import compute_artist_bonus, compute_timing_bonus
from app.schemas import ArtistPreference, CandidateShow, RecommendationItem, RecommendationRequest, TagWeight
from app.service import get_recommendations, rerank_with_artist_diversity


class RecommendationServiceTest(unittest.TestCase):
    def test_compute_timing_bonus_prefers_nearby_upcoming_show(self) -> None:
        bonus = compute_timing_bonus(
            show_start_date=str(date(2026, 3, 30)),
            ticketing_state="IN_PROGRESS",
            show_state="UPCOMING",
            today=date(2026, 3, 24),
        )

        self.assertGreaterEqual(bonus, 0.07)

    def test_rerank_with_artist_diversity_moves_duplicate_artist_down(self) -> None:
        candidates = {
            1: CandidateShow(showId=1, artist="IU", title="A", tags=[]),
            2: CandidateShow(showId=2, artist="IU", title="B", tags=[]),
            3: CandidateShow(showId=3, artist="AKMU", title="C", tags=[]),
        }
        recommendations = [
            RecommendationItem(showId=1, score=0.41, reason="same artist"),
            RecommendationItem(showId=2, score=0.39, reason="same artist"),
            RecommendationItem(showId=3, score=0.36, reason="different artist"),
        ]

        reranked = rerank_with_artist_diversity(recommendations, candidates)

        self.assertEqual([item.show_id for item in reranked[:3]], [1, 3, 2])

    def test_get_recommendations_handles_cold_start_with_timing_signal(self) -> None:
        today = date.today()
        payload = RecommendationRequest(
            userId=1,
            userEmbedding=[],
            userProfile=[],
            artistPreferences=[],
            recentKeywords=[],
            candidates=[
                CandidateShow(
                    showId=10,
                    artist="Artist A",
                    title="Open Now",
                    tags=[],
                    showStartDate=str(today + timedelta(days=3)),
                    ticketingState="IN_PROGRESS",
                    showState="UPCOMING",
                ),
                CandidateShow(
                    showId=20,
                    artist="Artist B",
                    title="Later Show",
                    tags=[],
                    showStartDate=str(today + timedelta(days=40)),
                    ticketingState="BEFORE_OPEN",
                    showState="UPCOMING",
                ),
            ],
        )

        response = get_recommendations(payload)

        self.assertEqual(response.recommendations[0].show_id, 10)
        self.assertIn("예매", response.recommendations[0].reason)

    def test_get_recommendations_uses_tag_similarity(self) -> None:
        payload = RecommendationRequest(
            userId=1,
            userEmbedding=[],
            userProfile=[TagWeight(tagId=1, weight=1.0, name="록")],
            artistPreferences=[],
            recentKeywords=[],
            candidates=[
                CandidateShow(
                    showId=100,
                    artist="Band A",
                    title="Rock Show",
                    tags=[TagWeight(tagId=1, weight=1.0, name="록")],
                ),
                CandidateShow(
                    showId=200,
                    artist="Band B",
                    title="Ballad Show",
                    tags=[TagWeight(tagId=2, weight=1.0, name="발라드")],
                ),
            ],
        )

        response = get_recommendations(payload)

        self.assertEqual(response.recommendations[0].show_id, 100)
        self.assertIn("태그", response.recommendations[0].reason)

    def test_compute_artist_bonus_ignores_generic_artist_label(self) -> None:
        bonus = compute_artist_bonus(
            candidate_artist="기타",
            artist_preferences=[ArtistPreference(artist="기타", weight=2.0)],
        )

        self.assertEqual(bonus, 0.0)

    def test_get_recommendations_explains_agency_and_segment_match(self) -> None:
        payload = RecommendationRequest(
            userId=1,
            userEmbedding=[],
            userProfile=[
                TagWeight(tagId=11, weight=1.0, category="AGENCY", name="HYBE"),
                TagWeight(tagId=12, weight=1.0, category="ARTIST_SEGMENT", name="MALE_IDOL"),
            ],
            artistPreferences=[],
            recentKeywords=[],
            candidates=[
                CandidateShow(
                    showId=300,
                    artist="BTS",
                    title="BTS Special Stage",
                    tags=[
                        TagWeight(tagId=11, weight=1.0, category="AGENCY", name="HYBE"),
                        TagWeight(tagId=12, weight=1.0, category="ARTIST_SEGMENT", name="MALE_IDOL"),
                    ],
                    ticketingState="IN_PROGRESS",
                    showState="UPCOMING",
                    showStartDate=str(date.today() + timedelta(days=5)),
                )
            ],
        )

        response = get_recommendations(payload)

        self.assertIn("같은 소속사", response.recommendations[0].reason)
        self.assertIn("남자 아이돌", response.recommendations[0].reason)


if __name__ == "__main__":
    unittest.main()
