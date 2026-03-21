# AI Server

## Run

```bash
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
```

## Endpoints

- `GET /health`
- `POST /api/v1/recommendations`
