import unittest
from datetime import date, timedelta
from unittest.mock import patch

from app.scoring import compute_artist_bonus, compute_timing_bonus
from app.schemas import ArtistPreference, CandidateShow, RecommendationItem, RecommendationRequest
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
            1: CandidateShow(showId=1, artist="IU", title="A"),
            2: CandidateShow(showId=2, artist="IU", title="B"),
            3: CandidateShow(showId=3, artist="AKMU", title="C"),
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
            artistPreferences=[],
            recentKeywords=[],
            candidates=[
                CandidateShow(
                    showId=10,
                    artist="Artist A",
                    title="Open Now",
                    showStartDate=str(today + timedelta(days=3)),
                    ticketingState="IN_PROGRESS",
                    showState="UPCOMING",
                ),
                CandidateShow(
                    showId=20,
                    artist="Artist B",
                    title="Later Show",
                    showStartDate=str(today + timedelta(days=40)),
                    ticketingState="BEFORE_OPEN",
                    showState="UPCOMING",
                ),
            ],
        )

        response = get_recommendations(payload)

        self.assertEqual(response.recommendations[0].show_id, 10)
        self.assertIn("예매", response.recommendations[0].reason)

    def test_compute_artist_bonus_ignores_generic_artist_label(self) -> None:
        bonus = compute_artist_bonus(
            candidate_artist="기타",
            artist_preferences=[ArtistPreference(artist="기타", weight=2.0)],
        )

        self.assertEqual(bonus, 0.0)

    @patch("app.service.generate_embedding")
    @patch("app.service.generate_embeddings")
    def test_get_recommendations_generates_embeddings_from_text(
        self,
        mock_generate_embeddings,
        mock_generate_embedding,
    ) -> None:
        mock_generate_embedding.return_value = [1.0, 0.0]
        mock_generate_embeddings.return_value = [[1.0, 0.0], [0.0, 1.0]]

        payload = RecommendationRequest(
            userId=1,
            userProfileText="BTS 콘서트와 HYBE 선호",
            artistPreferences=[],
            recentKeywords=[],
            candidates=[
                CandidateShow(showId=1, artist="BTS", title="BTS Live", embeddingText="BTS HYBE 콘서트"),
                CandidateShow(showId=2, artist="Jazz Band", title="Jazz Night", embeddingText="재즈 공연"),
            ],
        )

        response = get_recommendations(payload)

        self.assertEqual(response.recommendations[0].show_id, 1)
        mock_generate_embedding.assert_called_once()
        mock_generate_embeddings.assert_called_once()

    @patch("app.service.generate_embedding")
    @patch("app.service.generate_embeddings")
    def test_get_recommendations_prefers_same_artist_signal(
        self,
        mock_generate_embeddings,
        mock_generate_embedding,
    ) -> None:
        mock_generate_embedding.return_value = [1.0, 0.0]
        mock_generate_embeddings.return_value = [[1.0, 0.0], [0.0, 1.0]]

        payload = RecommendationRequest(
            userId=1,
            userProfileText="BTS 월드투어 콘서트",
            artistPreferences=[ArtistPreference(artist="BTS", weight=2.0)],
            recentKeywords=["월드투어"],
            candidates=[
                CandidateShow(
                    showId=100,
                    artist="BTS",
                    title="BTS Special Stage",
                    embeddingText="BTS 월드투어 콘서트",
                    ticketingState="IN_PROGRESS",
                    showState="UPCOMING",
                    showStartDate=str(date.today() + timedelta(days=5)),
                ),
                CandidateShow(
                    showId=200,
                    artist="Jazz Band",
                    title="Jazz Night",
                    embeddingText="재즈 공연",
                    ticketingState="IN_PROGRESS",
                    showState="UPCOMING",
                    showStartDate=str(date.today() + timedelta(days=5)),
                ),
            ],
        )

        response = get_recommendations(payload)

        self.assertEqual(response.recommendations[0].show_id, 100)
        self.assertIn("동일 아티스트", response.recommendations[0].reason)

    @patch("app.service.generate_embedding")
    @patch("app.service.generate_embeddings")
    def test_get_recommendations_uses_precomputed_embeddings_when_present(
        self,
        mock_generate_embeddings,
        mock_generate_embedding,
    ) -> None:
        payload = RecommendationRequest(
            userId=1,
            userEmbedding=[1.0, 0.0],
            userProfileText="이 텍스트는 사용되지 않아야 함",
            artistPreferences=[],
            recentKeywords=[],
            candidates=[
                CandidateShow(showId=1, artist="BTS", title="BTS Live", embedding=[1.0, 0.0]),
                CandidateShow(showId=2, artist="Jazz Band", title="Jazz Night", embedding=[0.0, 1.0]),
            ],
        )

        response = get_recommendations(payload)

        self.assertEqual(response.recommendations[0].show_id, 1)
        mock_generate_embedding.assert_not_called()
        mock_generate_embeddings.assert_not_called()


if __name__ == "__main__":
    unittest.main()
