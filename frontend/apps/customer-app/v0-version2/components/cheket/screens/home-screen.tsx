'use client'

import { useState, useEffect, useRef } from 'react'
import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import {
  BANNER_SLIDES,
  CATEGORY_ICONS,
  RANKING_ITEMS,
  OPEN_SCHEDULE,
  MOCK_EVENTS,
} from '@/lib/mock-data'
import { AppShell } from '../app-shell'
import { ChevronRight, Clock, Heart, Tag } from 'lucide-react'

// ── Hero banner carousel ──────────────────────────────────────────────────
function HeroBanner({ onEventClick }: { onEventClick: (id: string) => void }) {
  const [current, setCurrent] = useState(0)
  const total = BANNER_SLIDES.length
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const startTimer = () => {
    timerRef.current = setInterval(() => {
      setCurrent((c) => (c + 1) % total)
    }, 3500)
  }

  useEffect(() => {
    startTimer()
    return () => { if (timerRef.current) clearInterval(timerRef.current) }
  }, [])

  const slide = BANNER_SLIDES[current]

  return (
    <div className="relative w-full aspect-[4/3] overflow-hidden">
      <button
        className="relative w-full h-full block"
        onClick={() => onEventClick(slide.eventId)}
        aria-label={slide.title}
      >
        <Image
          src={slide.image}
          alt={slide.title}
          fill
          className="object-cover transition-opacity duration-500"
          priority
        />
        {/* gradient overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
        {/* text */}
        <div className="absolute bottom-0 left-0 p-4 text-left">
          <p className="text-white font-black text-xl leading-tight text-balance">{slide.title}</p>
          <p className="text-white/90 font-bold text-sm mt-0.5">{slide.subtitle}</p>
          <p className="text-white/70 text-xs mt-1">{slide.venue}</p>
          <p className="text-white/70 text-xs">{slide.dates}</p>
        </div>
      </button>
      {/* counter pill */}
      <div className="absolute bottom-3 right-3 bg-black/50 text-white text-xs font-medium px-2.5 py-1 rounded-full backdrop-blur-sm">
        {current + 1} / {total}
      </div>
      {/* dot indicators */}
      <div className="absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-1.5">
        {BANNER_SLIDES.map((_, i) => (
          <button
            key={i}
            onClick={() => setCurrent(i)}
            className={`rounded-full transition-all ${i === current ? 'w-4 h-1.5 bg-white' : 'w-1.5 h-1.5 bg-white/40'}`}
            aria-label={`슬라이드 ${i + 1}`}
          />
        ))}
      </div>
    </div>
  )
}

// ── Category icon grid ────────────────────────────────────────────────────
function CategoryGrid({ onSelect }: { onSelect: (id: string) => void }) {
  return (
    <div className="px-4 py-4 grid grid-cols-5 gap-y-4">
      {CATEGORY_ICONS.map((cat) => (
        <button
          key={cat.id}
          onClick={() => onSelect(cat.id)}
          className="flex flex-col items-center gap-1.5 group"
        >
          <div className="w-12 h-12 rounded-2xl bg-secondary flex items-center justify-center text-2xl group-hover:bg-primary/10 transition-colors">
            {cat.icon}
          </div>
          <span className="text-[10px] text-foreground font-medium leading-tight text-center">{cat.label}</span>
        </button>
      ))}
    </div>
  )
}

// ── Section header ────────────────────────────────────────────────────────
function SectionHeader({ title, onMore }: { title: string; onMore?: () => void }) {
  return (
    <div className="flex items-center justify-between px-4 mb-3">
      <h2 className="font-bold text-base text-foreground">{title}</h2>
      {onMore && (
        <button onClick={onMore} className="flex items-center gap-0.5 text-xs text-muted-foreground hover:text-primary transition-colors">
          전체보기 <ChevronRight className="w-3.5 h-3.5" />
        </button>
      )}
    </div>
  )
}

// ── Concert Ranking (no genre filter) ─────────────────────────────────────
function ConcertRanking({ onEventClick }: { onEventClick: (id: string) => void }) {
  const items = RANKING_ITEMS.slice(0, 5)

  // Get rank badge styles based on position
  const getRankStyle = (rank: number) => {
    if (rank === 1) return 'bg-yellow-500 text-white'
    if (rank === 2) return 'bg-gray-400 text-white'
    if (rank === 3) return 'bg-amber-700 text-white'
    return 'bg-foreground text-background'
  }

  return (
    <section className="pt-5 pb-4">
      <SectionHeader title="콘서트 랭킹" onMore={() => {}} />
      {/* Ranking cards — horizontal scroll */}
      <div className="flex gap-3 overflow-x-auto px-4 pb-1 scrollbar-hide">
        {items.map((item) => (
          <button
            key={item.rank}
            onClick={() => onEventClick(item.eventId)}
            className="flex-shrink-0 w-32 flex flex-col gap-1.5 text-left"
          >
            <div className="relative w-32 h-44 rounded-xl overflow-hidden bg-secondary">
              <Image src={item.poster} alt={item.name} fill className="object-cover" />
              <div className={`absolute top-2 left-2 w-6 h-6 rounded-full flex items-center justify-center ${getRankStyle(item.rank)}`}>
                <span className="text-xs font-bold">{item.rank}</span>
              </div>
            </div>
            <p className="text-xs font-semibold text-foreground leading-snug line-clamp-2">{item.name}</p>
            <p className="text-[10px] text-muted-foreground">{item.venue}</p>
          </button>
        ))}
      </div>
    </section>
  )
}

// ── Open schedule ─────────────────────────────────────────────────────────
function OpenSchedule({ onEventClick }: { onEventClick: (id: string) => void }) {
  return (
    <section className="py-5 bg-secondary/40">
      <SectionHeader title="오픈 예정" onMore={() => {}} />
      <div className="flex gap-3 overflow-x-auto px-4 pb-1 scrollbar-hide">
        {OPEN_SCHEDULE.map((item) => (
          <button
            key={item.id}
            onClick={() => onEventClick(item.eventId)}
            className="flex-shrink-0 w-[80vw] max-w-[300px] flex gap-3 bg-card rounded-2xl border border-border p-3 text-left hover:border-primary/40 active:scale-[0.98] transition-all"
          >
            <div className="relative w-20 h-24 rounded-xl overflow-hidden bg-secondary flex-shrink-0">
              <Image src={item.poster} alt={item.name} fill className="object-cover" />
            </div>
            <div className="flex flex-col justify-between py-0.5 min-w-0">
              <div>
                <p className={`text-sm font-bold ${item.isToday ? 'text-primary' : 'text-blue-600'}`}>
                  {item.openLabel}
                </p>
                <p className="text-sm font-semibold text-foreground mt-1 leading-snug line-clamp-2">{item.name}</p>
                <p className="text-xs text-muted-foreground mt-0.5">{item.openType}</p>
              </div>
              <div className="flex flex-wrap gap-1.5 mt-2">
                {item.tags.map((tag) => (
                  <span
                    key={tag}
                    className={`text-[10px] font-bold px-2 py-0.5 rounded-md ${
                      tag === 'HOT'
                        ? 'bg-red-100 text-red-600 border border-red-200'
                        : 'bg-primary/10 text-primary border border-primary/20'
                    }`}
                  >
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          </button>
        ))}
      </div>
    </section>
  )
}

// ── Discount section (2차 거래소 리세일 티켓, 할인율 높은 순) ─────────────
function DiscountSection() {
  const { resaleItems, navigate } = useApp()

  // 할인율 = (originalPrice - resalePrice) / originalPrice * 100
  // 할인이 있는 것만 필터 후 내림차순 상위 5개
  const discounted = [...resaleItems]
    .map((item) => ({
      ...item,
      discountPct: Math.round(((item.originalPrice - item.resalePrice) / item.originalPrice) * 100),
    }))
    .filter((item) => item.discountPct > 0)
    .sort((a, b) => b.discountPct - a.discountPct)
    .slice(0, 5)

  if (discounted.length === 0) return null

  return (
    <section className="py-5">
      <SectionHeader
        title="지금 할인중!"
        onMore={() => navigate('resale-list')}
      />
      <div className="flex flex-col gap-0 divide-y divide-border px-4">
        {discounted.map((item) => (
          <button
            key={item.id}
            onClick={() => navigate('resale-detail', { resaleItemId: item.id })}
            className="flex gap-3 py-4 text-left hover:bg-secondary/50 active:scale-[0.99] transition-all -mx-4 px-4"
          >
            {/* Poster */}
            <div className="relative w-24 h-32 rounded-xl overflow-hidden bg-secondary flex-shrink-0">
              <Image src={item.poster} alt={item.eventName} fill className="object-cover" />
              {/* Discount badge */}
              <div className="absolute top-1.5 left-1.5 bg-red-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-md">
                -{item.discountPct}%
              </div>
            </div>

            {/* Info */}
            <div className="flex flex-col justify-between py-0.5 min-w-0 flex-1">
              <div>
                <div className="inline-flex items-center gap-1 bg-secondary text-muted-foreground text-[10px] font-medium px-2 py-0.5 rounded-md mb-1.5">
                  <Tag className="w-2.5 h-2.5" />
                  2차 거래소
                </div>
                <p className="text-sm font-semibold text-foreground leading-snug line-clamp-2">{item.eventName}</p>
                <p className="text-xs text-muted-foreground mt-0.5">{item.venue.split(',')[0]}</p>
                <p className="text-xs text-muted-foreground">{item.seatLabel} · {item.grade}</p>
              </div>
              <div className="flex items-baseline gap-1.5 mt-1.5">
                <span className="text-xs text-muted-foreground line-through">
                  {item.originalPrice.toLocaleString()} CTK
                </span>
                <span className="text-red-500 font-bold text-sm">
                  {item.resalePrice.toLocaleString()} CTK
                </span>
              </div>
            </div>
          </button>
        ))}
      </div>
    </section>
  )
}

// ── Recommendation Section with Wishlist ──────────────────────────────────
function RecommendationSection({ onEventClick }: { onEventClick: (id: string) => void }) {
  const { wishlist, navigate } = useApp()
  
  // Get one wishlisted event for display
  const wishlistedEvents = MOCK_EVENTS.filter((e) => wishlist.includes(e.id))
  const recommendedEvent = MOCK_EVENTS.find((e) => !wishlist.includes(e.id)) || MOCK_EVENTS[0]

  return (
    <section className="py-5">
      <SectionHeader title="사용자 추천 콘서트" />
      <div className="px-4 flex gap-3">
        {/* Wishlist Card */}
        <button
          onClick={() => navigate('wishlist')}
          className="flex-shrink-0 w-28 h-36 rounded-2xl bg-gradient-to-br from-primary/20 via-primary/10 to-secondary border border-primary/30 flex flex-col items-center justify-center gap-2 hover:border-primary/50 active:scale-[0.98] transition-all"
        >
          <div className="w-12 h-12 rounded-full bg-primary/20 flex items-center justify-center">
            <Heart className="w-6 h-6 text-primary" />
          </div>
          <div className="text-center">
            <p className="text-xs font-bold text-foreground">찜한 콘서트</p>
            <p className="text-lg font-bold text-primary">{wishlist.length}</p>
          </div>
        </button>

        {/* Recommended Event Card */}
        {recommendedEvent && (
          <button
            onClick={() => onEventClick(recommendedEvent.id)}
            className="flex-1 flex gap-3 bg-card rounded-2xl border border-border p-3 text-left hover:border-primary/40 active:scale-[0.98] transition-all"
          >
            <div className="relative w-20 h-28 rounded-xl overflow-hidden bg-secondary flex-shrink-0">
              <Image src={recommendedEvent.poster} alt={recommendedEvent.name} fill className="object-cover" />
            </div>
            <div className="flex flex-col justify-between py-0.5 min-w-0 flex-1">
              <div>
                <p className="text-sm font-semibold text-foreground leading-snug line-clamp-2">{recommendedEvent.name}</p>
                <p className="text-xs text-muted-foreground mt-1">{recommendedEvent.date}</p>
                <p className="text-xs text-muted-foreground">{recommendedEvent.venue.split(',')[0]}</p>
              </div>
              <div className="flex items-center gap-1 mt-1">
                <span className="text-xs text-primary font-medium">
                  {recommendedEvent.grades[0]?.price.toLocaleString()} CTK~
                </span>
              </div>
            </div>
          </button>
        )}
      </div>
    </section>
  )
}

// ── Main HomeScreen ───────────────────────────────────────────────────────
export function HomeScreen() {
  const { navigate } = useApp()

  const goToEvent = (eventId: string) => navigate('event-detail', { eventId })

  return (
    <AppShell>
      <HeroBanner onEventClick={goToEvent} />

      {/* Thin divider */}
      <div className="h-px bg-border" />

      <ConcertRanking onEventClick={goToEvent} />

      <div className="h-2 bg-secondary/60" />

      <OpenSchedule onEventClick={goToEvent} />

      <div className="h-2 bg-secondary/60" />

      <RecommendationSection onEventClick={goToEvent} />

      <div className="h-2 bg-secondary/60" />

      <DiscountSection />
    </AppShell>
  )
}
