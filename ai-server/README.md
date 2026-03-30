# AI Server

## Run

```bash
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
```

Local env example:

```env
OPENAI_API_KEY=your-real-gms-key
OPENAI_BASE_URL=https://gms.ssafy.io/gmsapi/api.openai.com/v1
EMBEDDING_MODEL=text-embedding-3-large
```

Notes:

- Put this in `ai-dev/ai-server/.env.local` and load it into your shell before starting uvicorn.
- `OPENAI_API_KEY=GMS_KEY` does not usually expand to another variable in a plain `.env` file. Put the real key value directly into `OPENAI_API_KEY`.

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

Local sync workflow:

```bash
python scripts/sync_show_embeddings.py
python scripts/bootstrap_local_ai_signal_tags.py
```

Notes:

- This script reads local `ai-dev/ai-server/.env.local` and `be-dev/backend/.env.local`.
- It creates and upserts a local-only `show_embeddings` table.
- `bootstrap_local_ai_signal_tags.py` enriches `tags`, `show_tags`, and `user_preference_profiles` with local-only AI signals such as `GENRE`, `MOOD`, `SHOW_TYPE`, `AGENCY`, and `ARTIST_SEGMENT`.
- Current user embedding is not stored separately. Backend builds it at request time by averaging liked and purchased show embeddings.

Default embedding configuration:

- model: `text-embedding-3-large`
- base URL: `https://gms.ssafy.io/gmsapi/api.openai.com/v1`
- key env: `OPENAI_API_KEY`

Example text:

```text
제목: 2026 아이유 앵콜 콘서트 / 아티스트: 아이유 / 공연장: 올림픽공원 체조경기장 / 태그: 발라드, 감성, 단독콘서트
```
