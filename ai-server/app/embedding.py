from __future__ import annotations

import os

from openai import OpenAI


DEFAULT_EMBEDDING_MODEL = "text-embedding-3-large"
DEFAULT_BASE_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1"
GENERIC_ARTIST_VALUES = {
    "기타",
    "various artists",
    "various artist",
    "various",
    "unknown artist",
    "unknown",
}


def _create_client() -> tuple[OpenAI, str]:
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        raise RuntimeError("OPENAI_API_KEY is not configured.")

    base_url = os.getenv("OPENAI_BASE_URL", DEFAULT_BASE_URL)
    model = os.getenv("EMBEDDING_MODEL", DEFAULT_EMBEDDING_MODEL)
    return OpenAI(api_key=api_key, base_url=base_url), model


def build_show_embedding_text(
    title: str,
    artist: str,
    venue: str | None,
    tags: list[tuple[str | None, str | None]],
) -> str:
    parts: list[str] = []

    if title:
        parts.append(f"제목: {title}")
    normalized_artist = " ".join(artist.strip().lower().split()) if artist else ""
    if artist and normalized_artist not in GENERIC_ARTIST_VALUES:
        parts.append(f"아티스트: {artist}")
    if venue:
        parts.append(f"공연장: {venue}")

    tag_parts = [name for _, name in tags if name]
    if tag_parts:
        parts.append("태그: " + ", ".join(tag_parts))

    return " / ".join(parts)


def generate_embedding(text: str) -> list[float]:
    client, model = _create_client()
    response = client.embeddings.create(model=model, input=text)
    return response.data[0].embedding


def generate_embeddings(texts: list[str]) -> list[list[float]]:
    if not texts:
        return []

    client, model = _create_client()
    response = client.embeddings.create(model=model, input=texts)
    return [item.embedding for item in response.data]
