"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { ChevronRight, Clock, Heart } from "lucide-react";

import {
  bannerSlides,
  discountItems,
  openSchedule,
  rankingItems,
} from "@/lib/mock-data";

function SectionHeader({ title }: { title: string }) {
  return (
    <div className="mb-3 flex items-center justify-between px-1">
      <h2 className="text-base font-bold text-foreground">{title}</h2>
      <button className="flex items-center gap-0.5 text-xs text-muted-foreground">
        전체보기 <ChevronRight className="h-3.5 w-3.5" />
      </button>
    </div>
  );
}

function HeroBanner() {
  const [current, setCurrent] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrent((prev) => (prev + 1) % bannerSlides.length);
    }, 3500);
    return () => clearInterval(timer);
  }, []);

  const slide = bannerSlides[current];

  return (
    <section className="relative overflow-hidden">
      <div className="relative aspect-[4/3] w-full overflow-hidden rounded-none">
        <Image src={slide.image} alt={slide.title} fill className="object-cover" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
        <div className="absolute right-3 bottom-3 rounded-full bg-black/45 px-2.5 py-1 text-xs text-white">
          {current + 1} / {bannerSlides.length}
        </div>
        <div className="absolute bottom-0 left-0 p-4 text-white">
          <p className="text-sm font-bold">{slide.subtitle}</p>
          <h1 className="mt-1 text-xl font-black leading-tight text-balance">
            {slide.title}
          </h1>
          <p className="mt-1 text-xs text-white/90">{slide.venue}</p>
          <p className="text-xs text-white/80">{slide.dates}</p>
        </div>
      </div>

      <div className="absolute bottom-3 left-1/2 flex -translate-x-1/2 gap-1.5">
        {bannerSlides.map((_, i) => (
          <button
            key={`dot-${i}`}
            onClick={() => setCurrent(i)}
            className={`rounded-full transition-all ${
              i === current ? "h-1.5 w-4 bg-white" : "h-1.5 w-1.5 bg-white/50"
            }`}
            aria-label={`배너 ${i + 1}`}
          />
        ))}
      </div>
    </section>
  );
}

function RankingSection() {
  const getRankClass = (rank: number) => {
    if (rank === 1) return "bg-yellow-500";
    if (rank === 2) return "bg-zinc-400";
    if (rank === 3) return "bg-amber-700";
    return "bg-black/65";
  };

  return (
    <section className="px-4 py-5">
      <SectionHeader title="콘서트 랭킹" />
      <div className="flex gap-3 overflow-x-auto pb-1">
        {rankingItems.map((item) => (
          <article key={item.rank} className="w-32 shrink-0">
            <div className="relative h-44 w-32 overflow-hidden rounded-xl bg-secondary">
              <Image src={item.poster} alt={item.name} fill className="object-cover" />
              <span
                className={`absolute top-2 left-2 inline-flex h-6 w-6 items-center justify-center rounded-full text-xs font-bold text-white ${getRankClass(item.rank)}`}
              >
                {item.rank}
              </span>
            </div>
            <p className="mt-2 line-clamp-2 text-xs font-semibold text-card-foreground">
              {item.name}
            </p>
            <p className="mt-1 text-[10px] text-muted-foreground">{item.venue}</p>
          </article>
        ))}
      </div>
    </section>
  );
}

function OpenScheduleSection() {
  return (
    <section className="bg-secondary/40 px-4 py-5">
      <SectionHeader title="오픈 예정" />
      <div className="flex gap-3 overflow-x-auto pb-1">
        {openSchedule.map((item) => (
          <article
            key={item.id}
            className="flex w-[80vw] max-w-[300px] shrink-0 gap-3 rounded-2xl border border-border bg-card p-3"
          >
            <div className="relative h-24 w-20 shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={item.poster} alt={item.name} fill className="object-cover" />
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-xs font-semibold text-primary">{item.openLabel}</p>
              <p className="mt-1 line-clamp-2 text-sm font-semibold">{item.name}</p>
              <p className="text-xs text-muted-foreground">{item.openType}</p>
              <div className="mt-2 flex flex-wrap gap-1.5">
                {item.tags.map((tag) => (
                  <span
                    key={`${item.id}-${tag}`}
                    className={`rounded-md px-2 py-0.5 text-[10px] font-bold ${
                      tag === "HOT"
                        ? "border border-red-200 bg-red-100 text-red-600"
                        : "border border-primary/20 bg-primary/10 text-primary"
                    }`}
                  >
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

function RecommendationSection() {
  return (
    <section className="px-4 py-5">
      <SectionHeader title="리세일 추천" />
      <div className="flex gap-3">
        <article className="flex h-36 w-28 shrink-0 flex-col items-center justify-center rounded-2xl border border-primary/30 bg-gradient-to-br from-primary/20 via-primary/10 to-secondary">
          <div className="flex h-11 w-11 items-center justify-center rounded-full bg-primary/20">
            <Heart className="h-5 w-5 text-primary" />
          </div>
          <p className="mt-2 text-center text-[11px] font-semibold text-card-foreground">
            찜한 공연
          </p>
          <p className="text-xl font-bold text-primary">3</p>
        </article>

        <article className="flex flex-1 gap-3 rounded-2xl border border-border bg-card p-3">
          <div className="relative h-28 w-20 shrink-0 overflow-hidden rounded-xl bg-secondary">
            <Image src="/posters/concert1.jpg" alt="추천 공연" fill className="object-cover" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="line-clamp-2 text-sm font-semibold text-card-foreground">
              AESPA WORLD TOUR 2026
            </p>
            <p className="mt-1 text-xs text-muted-foreground">2026.03.20 - 03.21</p>
            <p className="text-xs text-muted-foreground">올림픽체조경기장</p>
            <p className="mt-2 text-xs font-medium text-primary">95,000 CTK~</p>
          </div>
        </article>
      </div>
    </section>
  );
}

function DiscountSection() {
  return (
    <section className="px-4 pt-5 pb-6">
      <SectionHeader title="지금 특가" />
      <div className="space-y-2">
        {discountItems.map((item) => (
          <article key={item.id} className="rounded-2xl border border-border bg-card p-3">
            <div className="flex gap-3">
              <div className="relative h-28 w-20 shrink-0 overflow-hidden rounded-xl bg-secondary">
                <Image src={item.poster} alt={item.name} fill className="object-cover" />
              </div>
              <div className="min-w-0 flex-1">
                <div className="mb-2 inline-flex items-center gap-1 rounded-md bg-red-500 px-2 py-0.5 text-[10px] font-bold text-white">
                  <Clock className="h-3 w-3" />
                  타임특가
                </div>
                <p className="line-clamp-2 text-sm font-semibold text-card-foreground">
                  {item.name}
                </p>
                <p className="mt-1 text-xs text-muted-foreground">{item.venue}</p>
                <p className="text-xs text-muted-foreground">{item.dates}</p>
                <div className="mt-1 flex items-baseline gap-1.5">
                  <span className="text-sm font-bold text-red-500">{item.discountPct}%</span>
                  <span className="text-sm font-bold text-card-foreground">
                    {item.finalPrice.toLocaleString()}원
                  </span>
                </div>
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

export default function HomePage() {
  return (
    <>
      <HeroBanner />
      <div className="h-px bg-border" />
      <RankingSection />
      <div className="h-2 bg-secondary/60" />
      <OpenScheduleSection />
      <div className="h-2 bg-secondary/60" />
      <RecommendationSection />
      <div className="h-2 bg-secondary/60" />
      <DiscountSection />
    </>
  );
}
