"use client";

import { Search } from "lucide-react";
import { useMemo, useState } from "react";

type Show = {
  id: string;
  title: string;
  venue: string;
  date: string;
  genre: string;
};

const mockShows: Show[] = [
  {
    id: "show-001",
    title: "CHEKET FEST 2026",
    venue: "Jamsil Arena",
    date: "2026-03-12",
    genre: "Festival",
  },
  {
    id: "show-002",
    title: "SUMMER LIVE NIGHT",
    venue: "Olympic Hall",
    date: "2026-03-19",
    genre: "K-POP",
  },
  {
    id: "show-003",
    title: "INDIE MOON STAGE",
    venue: "Blue Square",
    date: "2026-03-23",
    genre: "Indie",
  },
  {
    id: "show-004",
    title: "RETRO CITY SOUND",
    venue: "Seoul Forest Theater",
    date: "2026-04-04",
    genre: "Band",
  },
];

function formatDate(dateString: string) {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    weekday: "short",
  }).format(date);
}

export default function Home() {
  const [query, setQuery] = useState("");

  const filteredShows = useMemo(() => {
    const keyword = query.trim().toLowerCase();
    if (!keyword) return mockShows;

    return mockShows.filter((show) => {
      return (
        show.title.toLowerCase().includes(keyword) ||
        show.venue.toLowerCase().includes(keyword) ||
        show.genre.toLowerCase().includes(keyword)
      );
    });
  }, [query]);

  return (
    <section className="space-y-4">
      <div className="rounded-xl bg-secondary p-4">
        <p className="text-xs text-secondary-foreground/80">Home</p>
        <h1 className="mt-1 text-xl font-semibold text-secondary-foreground">
          Find your next show
        </h1>
      </div>

      <div className="relative">
        <Search
          size={16}
          className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-muted-foreground"
        />
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search by title, venue, genre"
          className="h-11 w-full rounded-lg border border-border bg-card pl-10 pr-3 text-sm outline-none placeholder:text-muted-foreground focus:ring-2 focus:ring-ring/40"
        />
      </div>

      <div className="flex items-center justify-between">
        <h2 className="text-sm font-medium text-muted-foreground">Shows</h2>
        <p className="text-xs text-muted-foreground">
          {filteredShows.length} result{filteredShows.length === 1 ? "" : "s"}
        </p>
      </div>

      {filteredShows.length === 0 ? (
        <div className="rounded-xl border border-dashed border-border bg-card p-6 text-center text-sm text-muted-foreground">
          No matching shows found.
        </div>
      ) : (
        <ul className="space-y-2">
          {filteredShows.map((show) => (
            <li
              key={show.id}
              className="rounded-xl border border-border bg-card p-4 text-sm"
            >
              <div className="flex items-start justify-between gap-3">
                <p className="font-medium text-card-foreground">{show.title}</p>
                <span className="rounded-md bg-accent px-2 py-0.5 text-[11px] text-accent-foreground">
                  {show.genre}
                </span>
              </div>
              <p className="mt-1 text-xs text-muted-foreground">{show.venue}</p>
              <p className="mt-0.5 text-xs text-muted-foreground">
                {formatDate(show.date)}
              </p>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
