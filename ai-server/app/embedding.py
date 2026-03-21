from __future__ import annotations

import os

from openai import OpenAI


DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small"


def build_show_embedding_text(
    title: str,
    artist: str,
    venue: str | None,
    tags: list[tuple[str | None, str | None]],
) -> str:
    parts: list[str] = []

    if title:
        parts.append(f"제목: {title}")
    if artist:
        parts.append(f"아티스트: {artist}")
    if venue:
        parts.append(f"공연장: {venue}")

    tag_parts = [name for _, name in tags if name]
    if tag_parts:
        parts.append("태그: " + ", ".join(tag_parts))

    return " / ".join(parts)


def generate_embedding(text: str) -> list[float]:
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        raise RuntimeError("OPENAI_API_KEY is not configured.")

    client = OpenAI(api_key=api_key)
    response = client.embeddings.create(model=DEFAULT_EMBEDDING_MODEL, input=text)
    return response.data[0].embedding
