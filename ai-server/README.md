# AI Server

## Run

```bash
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
```

## Endpoints

- `GET /health`
- `POST /api/v1/recommendations`

## Scoring

```text
final_score =
0.30 * embedding_similarity
+ 0.30 * tag_similarity
+ 0.20 * artist_match_score
+ 0.10 * search_bonus
+ 0.10 * freshness_bonus
```

Notes:

- `embedding_similarity`: precomputed embedding vector similarity
- `tag_similarity`: user tag profile vs show tags cosine similarity
- `artist_match_score`: exact artist match bonus
- `search_bonus`: recent keyword match bonus
- `freshness_bonus`: ticketing/open timing bonus

## Example Request

```json
{
  "userId": 7,
  "userEmbedding": [0.12, -0.04, 0.28, 0.31],
  "userProfile": [
    { "tagId": 1, "weight": 1.0, "category": "GENRE", "name": "발라드" },
    { "tagId": 7, "weight": 1.0, "category": "MOOD", "name": "감성" },
    { "tagId": 10, "weight": 2.0, "category": "SHOW_TYPE", "name": "페스티벌" }
  ],
  "artistPreferences": [
    { "artist": "아이유", "weight": 2.0 }
  ],
  "recentKeywords": ["아이유", "페스티벌"],
  "candidates": [
    {
      "showId": 12,
      "title": "2026 서울 스프링 콘서트",
      "artist": "아이유",
      "venue": "올림픽공원 체조경기장",
      "embedding": [0.14, -0.01, 0.24, 0.27],
      "ticketingState": "IN_PROGRESS",
      "showState": "UPCOMING",
      "tags": [
        { "tagId": 1, "weight": 0.9, "category": "GENRE", "name": "발라드" },
        { "tagId": 7, "weight": 0.8, "category": "MOOD", "name": "감성" }
      ]
    }
  ]
}
```

## Embedding

`app.embedding.generate_embedding()` can be used later to generate show embeddings from text fields.

Example text:

```text
제목: 2026 아이유 앵콜 콘서트 / 아티스트: 아이유 / 공연장: 올림픽공원 체조경기장 / 태그: 발라드, 감성, 단독콘서트
```
