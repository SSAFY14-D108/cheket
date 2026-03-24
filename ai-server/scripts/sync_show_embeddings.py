from __future__ import annotations

import argparse
import json
import os
import sys
from collections import OrderedDict
from pathlib import Path
from typing import Any

import pymysql

ROOT_DIR = Path(__file__).resolve().parents[3]
AI_SERVER_DIR = Path(__file__).resolve().parents[1]
BACKEND_DIR = ROOT_DIR / "be-dev" / "backend"
if str(AI_SERVER_DIR) not in sys.path:
    sys.path.insert(0, str(AI_SERVER_DIR))

from app.embedding import build_show_embedding_text, generate_embeddings

DEFAULT_BATCH_SIZE = 16
CREATE_TABLE_SQL = """
create table if not exists show_embeddings (
    show_id bigint not null primary key,
    embedding_json longtext not null,
    source_text text not null,
    embedding_model varchar(100) not null,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    created_at datetime not null default current_timestamp
)
"""
SELECT_SHOWS_SQL = """
select
    s.id as show_id,
    s.title,
    s.artist,
    v.name as venue_name,
    t.category as tag_category,
    t.name as tag_name
from shows s
join venues v on v.id = s.venue_id
left join show_tags st on st.show_id = s.id
left join tags t on t.id = st.tag_id
where (:only_open = 0 or s.status = 'MINTED')
order by s.id asc, st.weight desc, t.id asc
"""
UPSERT_SQL = """
insert into show_embeddings (show_id, embedding_json, source_text, embedding_model)
values (%s, %s, %s, %s)
on duplicate key update
    embedding_json = values(embedding_json),
    source_text = values(source_text),
    embedding_model = values(embedding_model),
    updated_at = current_timestamp
"""


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate and upsert show embeddings into local MySQL.")
    parser.add_argument("--show-id", type=int, action="append", default=[], help="Only sync the given show id.")
    parser.add_argument("--limit", type=int, default=0, help="Limit the number of shows to sync.")
    parser.add_argument("--batch-size", type=int, default=DEFAULT_BATCH_SIZE, help="Embedding batch size.")
    parser.add_argument(
        "--include-non-minted",
        action="store_true",
        help="Include shows outside the MINTED status.",
    )
    return parser.parse_args()


def load_env_file(path: Path) -> None:
    if not path.exists():
        return

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue

        key, value = line.split("=", 1)
        os.environ.setdefault(key.strip(), value.strip())


def load_local_env() -> None:
    load_env_file(AI_SERVER_DIR / ".env.local")
    load_env_file(BACKEND_DIR / ".env.local")


def create_connection() -> pymysql.Connection:
    return pymysql.connect(
        host=os.getenv("MYSQL_HOST", "127.0.0.1"),
        port=int(os.getenv("MYSQL_PORT", "3306")),
        user=os.getenv("MYSQL_USER", "user"),
        password=os.getenv("MYSQL_PASSWORD", ""),
        database=os.getenv("MYSQL_DATABASE", "cheket"),
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
        autocommit=False,
    )


def fetch_show_rows(connection: pymysql.Connection, include_non_minted: bool) -> list[dict[str, Any]]:
    sql = SELECT_SHOWS_SQL.replace(":only_open", "1" if not include_non_minted else "0")
    with connection.cursor() as cursor:
        cursor.execute(sql)
        return list(cursor.fetchall())


def build_documents(rows: list[dict[str, Any]], *, show_ids: set[int], limit: int) -> list[dict[str, Any]]:
    grouped: OrderedDict[int, dict[str, Any]] = OrderedDict()

    for row in rows:
        show_id = int(row["show_id"])
        if show_ids and show_id not in show_ids:
            continue

        show = grouped.setdefault(
            show_id,
            {
                "show_id": show_id,
                "title": row["title"] or "",
                "artist": row["artist"] or "",
                "venue": row["venue_name"] or "",
                "tags": [],
            },
        )

        if row["tag_name"]:
            show["tags"].append((row["tag_category"], row["tag_name"]))

        if limit and len(grouped) >= limit:
            break

    documents: list[dict[str, Any]] = []
    for show in grouped.values():
        documents.append(
            {
                "show_id": show["show_id"],
                "source_text": build_show_embedding_text(
                    title=show["title"],
                    artist=show["artist"],
                    venue=show["venue"],
                    tags=show["tags"],
                ),
            }
        )

    return documents


def batched(items: list[dict[str, Any]], batch_size: int) -> list[list[dict[str, Any]]]:
    return [items[index:index + batch_size] for index in range(0, len(items), batch_size)]


def upsert_embeddings(
    connection: pymysql.Connection,
    documents: list[dict[str, Any]],
    model_name: str,
    batch_size: int,
) -> int:
    synced_count = 0
    for batch in batched(documents, max(batch_size, 1)):
        source_texts = [document["source_text"] for document in batch]
        embeddings = generate_embeddings(source_texts)
        rows = [
            (
                document["show_id"],
                json.dumps(embedding),
                document["source_text"],
                model_name,
            )
            for document, embedding in zip(batch, embeddings, strict=True)
        ]

        with connection.cursor() as cursor:
            cursor.executemany(UPSERT_SQL, rows)
        connection.commit()
        synced_count += len(rows)

    return synced_count


def ensure_table(connection: pymysql.Connection) -> None:
    with connection.cursor() as cursor:
        cursor.execute(CREATE_TABLE_SQL)
    connection.commit()


def main() -> None:
    args = parse_args()
    load_local_env()

    model_name = os.getenv("EMBEDDING_MODEL", "text-embedding-3-large")
    connection = create_connection()

    try:
        ensure_table(connection)
        rows = fetch_show_rows(connection, args.include_non_minted)
        documents = build_documents(rows, show_ids=set(args.show_id), limit=max(args.limit, 0))

        if not documents:
            print("No shows matched the sync criteria.")
            return

        synced_count = upsert_embeddings(connection, documents, model_name, args.batch_size)
        print(f"Synced {synced_count} show embeddings using model={model_name}.")
    finally:
        connection.close()


if __name__ == "__main__":
    main()
