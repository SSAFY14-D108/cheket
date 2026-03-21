# AI Server

## Run

```bash
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
```

## Endpoints

- `GET /health`
- `POST /api/v1/recommendations`

## Example Request

```json
{
  "userId": 7,
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
      "tags": [
        { "tagId": 1, "weight": 0.9, "category": "GENRE", "name": "발라드" },
        { "tagId": 7, "weight": 0.8, "category": "MOOD", "name": "감성" }
      ]
    }
  ]
}
```
