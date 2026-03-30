from __future__ import annotations

import os
import sys
from collections import defaultdict
from pathlib import Path

import pymysql


ROOT_DIR = Path(__file__).resolve().parents[3]
AI_SERVER_DIR = Path(__file__).resolve().parents[1]
BACKEND_DIR = ROOT_DIR / "be-dev" / "backend"
if str(AI_SERVER_DIR) not in sys.path:
    sys.path.insert(0, str(AI_SERVER_DIR))

LOCAL_SOURCE = "LOCAL_AI_BOOTSTRAP"
GENERIC_ARTISTS = {"기타", "various artists", "various artist", "various", "unknown artist", "unknown"}
AGENCY_RULES = {
    "HYBE": [
        "방탄소년단",
        "bts",
        "세븐틴",
        "seventeen",
        "투모로우바이투게더",
        "txt",
        "tomorrow x together",
        "엔하이픈",
        "enhypen",
        "르세라핌",
        "le sserafim",
        "뉴진스",
        "newjeans",
        "아일릿",
        "illit",
        "보이넥스트도어",
        "boynextdoor",
        "투어스",
        "tws",
    ],
    "SM": [
        "에스파",
        "aespa",
        "nct",
        "라이즈",
        "riize",
        "샤이니",
        "shinee",
        "동방신기",
        "tvxq",
        "레드벨벳",
        "red velvet",
        "엑소",
        "exo",
    ],
    "JYP": [
        "스트레이 키즈",
        "stray kids",
        "있지",
        "itzy",
        "엔믹스",
        "nmixx",
        "twice",
        "트와이스",
        "데이식스",
        "day6",
    ],
    "YG": [
        "블랙핑크",
        "blackpink",
        "베이비몬스터",
        "babymonster",
        "treasure",
        "트레저",
        "위너",
        "winner",
        "악뮤",
        "akmu",
    ],
}
SHOW_TYPE_RULES = {
    "FAN_CONCERT": ["fan concert", "팬 콘서트"],
    "SOLO_CONCERT": ["단독 콘서트", "solo concert", "archive live"],
    "FESTIVAL": ["festival", "페스티벌", "뮤직 페스티벌"],
    "GALA": ["gala", "갈라"],
    "MUSICAL": ["musical", "뮤지컬"],
    "BAND_CONCERT": ["band", "밴드", "rock and roll", "rolling"],
}
GENRE_RULES = {
    "ROCK": ["rock", "락", "록", "rocking", "rockstar", "rockation"],
    "INDIE": ["indie", "인디", "rolling", "club ff", "먼데이프로젝트"],
    "JAZZ": ["jazz", "재즈"],
    "BALLAD": ["ballad", "발라드"],
    "HIPHOP": ["hiphop", "hip-hop", "힙합", "rap", "래퍼"],
    "IDOL": [
        "idol",
        "아이돌",
        "fan concert",
        "팬 콘서트",
        "world tour",
        "1st world tour",
        "comeback",
    ],
}
MOOD_RULES = {
    "ENERGETIC": ["rock", "festival", "party", "hommcoming", "rockation", "world tour"],
    "EMOTIONAL": ["archive", "serenade", "night", "감성", "발라드"],
    "TRENDY": ["fan concert", "world tour", "comeback", "signal", "project"],
}
SEGMENT_RULES = {
    "MALE_IDOL": [
        "방탄소년단",
        "bts",
        "세븐틴",
        "seventeen",
        "투모로우바이투게더",
        "txt",
        "엔하이픈",
        "enhypen",
        "보이넥스트도어",
        "boynextdoor",
        "투어스",
        "tws",
        "nct",
        "라이즈",
        "riize",
        "샤이니",
        "shinee",
        "엑소",
        "exo",
        "stray kids",
        "스트레이 키즈",
        "treasure",
        "트레저",
    ],
    "FEMALE_IDOL": [
        "에스파",
        "aespa",
        "블랙핑크",
        "blackpink",
        "르세라핌",
        "le sserafim",
        "뉴진스",
        "newjeans",
        "있지",
        "itzy",
        "엔믹스",
        "nmixx",
        "트와이스",
        "twice",
        "레드벨벳",
        "red velvet",
        "아일릿",
        "illit",
        "qwer",
    ],
}
SEGMENT_WEIGHTS = {
    "AGENCY": 0.55,
    "ARTIST_SEGMENT": 0.6,
    "SHOW_TYPE": 0.75,
    "GENRE": 0.9,
    "MOOD": 0.65,
}


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


def normalize_text(value: str | None) -> str:
    if not value:
        return ""
    return " ".join(value.strip().lower().split())


def contains_any(text: str, needles: list[str]) -> bool:
    return any(needle in text for needle in needles)


def fetch_shows(connection: pymysql.Connection) -> list[dict]:
    sql = """
        select
            s.id,
            s.title,
            s.artist,
            s.description,
            v.name as venue_name,
            r.name as region_name
        from shows s
        join venues v on v.id = s.venue_id
        join regions r on r.id = v.region_id
        where s.status = 'MINTED'
        order by s.id asc
    """
    with connection.cursor() as cursor:
        cursor.execute(sql)
        return list(cursor.fetchall())


def derive_tags(show: dict) -> list[tuple[str, str, float]]:
    title = normalize_text(show["title"])
    artist = normalize_text(show["artist"])
    description = normalize_text(show["description"])
    venue = normalize_text(show["venue_name"])
    region = normalize_text(show["region_name"])
    haystack = " ".join(filter(None, [title, artist, description, venue, region]))
    derived: list[tuple[str, str, float]] = []

    for agency, keywords in AGENCY_RULES.items():
        if contains_any(haystack, keywords):
            derived.append(("AGENCY", agency, SEGMENT_WEIGHTS["AGENCY"]))

    for segment, keywords in SEGMENT_RULES.items():
        if contains_any(haystack, keywords):
            derived.append(("ARTIST_SEGMENT", segment, SEGMENT_WEIGHTS["ARTIST_SEGMENT"]))

    for show_type, keywords in SHOW_TYPE_RULES.items():
        if contains_any(haystack, keywords):
            derived.append(("SHOW_TYPE", show_type, SEGMENT_WEIGHTS["SHOW_TYPE"]))

    for genre, keywords in GENRE_RULES.items():
        if contains_any(haystack, keywords):
            derived.append(("GENRE", genre, SEGMENT_WEIGHTS["GENRE"]))

    for mood, keywords in MOOD_RULES.items():
        if contains_any(haystack, keywords):
            derived.append(("MOOD", mood, SEGMENT_WEIGHTS["MOOD"]))

    if artist and artist not in GENERIC_ARTISTS:
        if "band" in haystack or "밴드" in haystack:
            derived.append(("ARTIST_SEGMENT", "BAND", 0.5))
        if "solo" in haystack or "단독" in haystack:
            derived.append(("SHOW_TYPE", "SOLO_CONCERT", 0.6))

    deduped: dict[tuple[str, str], float] = {}
    for category, name, weight in derived:
        key = (category, name)
        deduped[key] = max(deduped.get(key, 0.0), weight)

    return [(category, name, weight) for (category, name), weight in deduped.items()]


def ensure_base_tables(connection: pymysql.Connection) -> None:
    with connection.cursor() as cursor:
        cursor.execute(
            """
            create table if not exists tags (
                id bigint not null auto_increment primary key,
                category varchar(30) not null,
                name varchar(100) not null,
                unique key uk_tags_category_name (category, name)
            )
            """
        )
        cursor.execute(
            """
            create table if not exists show_tags (
                show_id bigint not null,
                tag_id bigint not null,
                weight decimal(6,2) not null default 1.00,
                source varchar(20) not null default 'BOOTSTRAP',
                confidence decimal(4,2) not null default 1.00,
                primary key (show_id, tag_id)
            )
            """
        )
        cursor.execute(
            """
            create table if not exists user_preference_profiles (
                user_id bigint not null,
                tag_id bigint not null,
                weight decimal(6,2) not null,
                updated_at datetime not null,
                primary key (user_id, tag_id)
            )
            """
        )
    connection.commit()


def upsert_tags(connection: pymysql.Connection, shows: list[dict]) -> dict[tuple[str, str], int]:
    categories_and_names: set[tuple[str, str]] = set()
    for show in shows:
        categories_and_names.update((category, name) for category, name, _ in derive_tags(show))

    if not categories_and_names:
        return {}

    with connection.cursor() as cursor:
        cursor.executemany(
            "insert into tags (category, name) values (%s, %s) on duplicate key update name = values(name)",
            list(categories_and_names),
        )
        placeholders = ", ".join(["(%s, %s)"] * len(categories_and_names))
        params: list[str] = []
        for category, name in categories_and_names:
            params.extend([category, name])
        cursor.execute(
            f"select id, category, name from tags where (category, name) in ({placeholders})",
            params,
        )
        rows = cursor.fetchall()
    connection.commit()

    return {(row["category"], row["name"]): int(row["id"]) for row in rows}


def rebuild_show_tags(connection: pymysql.Connection, shows: list[dict], tag_ids: dict[tuple[str, str], int]) -> int:
    with connection.cursor() as cursor:
        cursor.execute("delete from show_tags where source = %s", (LOCAL_SOURCE,))
        rows_to_insert: list[tuple[int, int, float, str, float]] = []
        for show in shows:
            for category, name, weight in derive_tags(show):
                tag_id = tag_ids.get((category, name))
                if tag_id is None:
                    continue
                rows_to_insert.append((int(show["id"]), tag_id, weight, LOCAL_SOURCE, min(weight + 0.1, 0.99)))

        if rows_to_insert:
            cursor.executemany(
                """
                insert into show_tags (show_id, tag_id, weight, source, confidence)
                values (%s, %s, %s, %s, %s)
                on duplicate key update
                    weight = values(weight),
                    source = values(source),
                    confidence = values(confidence)
                """,
                rows_to_insert,
            )
    connection.commit()
    return len(rows_to_insert)


def rebuild_user_profiles(connection: pymysql.Connection) -> int:
    with connection.cursor() as cursor:
        cursor.execute("delete from user_preference_profiles")
        cursor.execute(
            """
            insert into user_preference_profiles (user_id, tag_id, weight, updated_at)
            select aggregated.user_id, aggregated.tag_id, round(sum(aggregated.weight), 2) as weight, now()
            from (
                select l.user_id, st.tag_id, st.weight * 1.0 as weight
                from likes l
                join show_tags st on st.show_id = l.show_id

                union all

                select tk.user_id, st.tag_id, st.weight * 2.0 as weight
                from tickets tk
                join session_seats ss on ss.id = tk.session_seat_id
                join sessions se on se.id = ss.session_id
                join show_tags st on st.show_id = se.show_id
            ) aggregated
            group by aggregated.user_id, aggregated.tag_id
            """
        )
        affected = cursor.rowcount
    connection.commit()
    return affected


def print_examples(connection: pymysql.Connection) -> None:
    sql = """
        select
            s.id,
            s.title,
            s.artist,
            group_concat(concat(t.category, ':', t.name) order by st.weight desc separator ', ') as tags
        from shows s
        join show_tags st on st.show_id = s.id
        join tags t on t.id = st.tag_id
        where st.source = %s
        group by s.id, s.title, s.artist
        order by s.id asc
        limit 10
    """
    with connection.cursor() as cursor:
        cursor.execute(sql, (LOCAL_SOURCE,))
        for row in cursor.fetchall():
            print(f"{row['id']}\t{row['artist']}\t{row['title']}\t{row['tags']}")


def main() -> None:
    load_local_env()
    connection = create_connection()

    try:
        ensure_base_tables(connection)
        shows = fetch_shows(connection)
        tag_ids = upsert_tags(connection, shows)
        inserted_count = rebuild_show_tags(connection, shows, tag_ids)
        profile_count = rebuild_user_profiles(connection)

        category_counts: dict[str, int] = defaultdict(int)
        for show in shows:
            for category, _, _ in derive_tags(show):
                category_counts[category] += 1

        print(f"Seeded/updated {inserted_count} local AI signal rows.")
        print(f"Rebuilt {profile_count} user preference profile rows.")
        print("Derived signal counts by category:", dict(sorted(category_counts.items())))
        print_examples(connection)
    finally:
        connection.close()


if __name__ == "__main__":
    main()
