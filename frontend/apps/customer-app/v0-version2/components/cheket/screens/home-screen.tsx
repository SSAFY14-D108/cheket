'use client'

import { useEffect, useMemo, useRef, useState } from 'react'
import Image from 'next/image'
import { ChevronRight, Heart, Tag } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import type { Event } from '@/lib/types'
import { AppShell } from '../app-shell'

const CATEGORY_ITEMS = [
  { id: 'concert', label: '콘서트', icon: '🎤' },
  { id: 'musical', label: '뮤지컬', icon: '🎭' },
  { id: 'play', label: '연극', icon: '🎬' },
  { id: 'classic', label: '클래식', icon: '🎹' },
  { id: 'festival', label: '페스티벌', icon: '🎪' },
]

function getLowestPrice(event: Event) {
  const prices = event.grades.map((grade) => grade.price).filter((price) => price > 0)
  return prices.length > 0 ? Math.min(...prices) : 0
}

function getVenueLabel(venue: string) {
  return venue.split(',')[0]?.trim() || venue
}

function getBannerSubtitle(event: Event) {
  return event.artistName || event.region || 'KOPIS'
}

function getOpenLabel(event: Event) {
  return event.date || '일정 확인'
}

function HeroBanner({
  events,
  onEventClick,
}: {
  events: Event[]
  onEventClick: (id: string) => void
}) {
  const [current, setCurrent] = useState(0)
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const total = events.length

  useEffect(() => {
    if (total <= 1) return

    timerRef.current = setInterval(() => {
      setCurrent((prev) => (prev + 1) % total)
    }, 3500)

    return () => {
      if (timerRef.current) clearInterval(timerRef.current)
    }
  }, [total])

  if (events.length === 0) return null

  const slide = events[current]

  return (
    <div className="relative w-full aspect-[4/3] overflow-hidden">
      <button className="relative block h-full w-full" onClick={() => onEventClick(slide.id)} aria-label={slide.name}>
        <Image src={slide.poster} alt={slide.name} fill className="object-cover" priority sizes="390px" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
        <div className="absolute bottom-0 left-0 p-4 text-left">
          <p className="text-balance text-xl font-black leading-tight text-white">{slide.name}</p>
          <p className="mt-0.5 text-sm font-bold text-white/90">{getBannerSubtitle(slide)}</p>
          <p className="mt-1 text-xs text-white/70">{getVenueLabel(slide.venue)}</p>
          <p className="text-xs text-white/70">{slide.date}</p>
        </div>
      </button>

      <div className="absolute bottom-3 right-3 rounded-full bg-black/50 px-2.5 py-1 text-xs font-medium text-white backdrop-blur-sm">
        {current + 1} / {total}
      </div>

      <div className="absolute bottom-3 left-1/2 flex -translate-x-1/2 gap-1.5">
        {events.map((event, index) => (
          <button
            key={event.id}
            onClick={() => setCurrent(index)}
            className={`rounded-full transition-all ${index === current ? 'h-1.5 w-4 bg-white' : 'h-1.5 w-1.5 bg-white/40'}`}
            aria-label={`배너 ${index + 1}`}
          />
        ))}
      </div>
    </div>
  )
}

function CategoryGrid() {
  return (
    <div className="grid grid-cols-5 gap-y-4 px-4 py-4">
      {CATEGORY_ITEMS.map((item) => (
        <div key={item.id} className="flex flex-col items-center gap-1.5">
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-secondary text-2xl">
            {item.icon}
          </div>
          <span className="text-center text-[10px] font-medium leading-tight text-foreground">{item.label}</span>
        </div>
      ))}
    </div>
  )
}

function SectionHeader({ title, onMore }: { title: string; onMore?: () => void }) {
  return (
    <div className="mb-3 flex items-center justify-between px-4">
      <h2 className="text-base font-bold text-foreground">{title}</h2>
      {onMore && (
        <button onClick={onMore} className="flex items-center gap-0.5 text-xs text-muted-foreground transition-colors hover:text-primary">
          전체보기 <ChevronRight className="h-3.5 w-3.5" />
        </button>
      )}
    </div>
  )
}

function ConcertRanking({
  events,
  onEventClick,
}: {
  events: Event[]
  onEventClick: (id: string) => void
}) {
  const getRankStyle = (rank: number) => {
    if (rank === 1) return 'bg-yellow-500 text-white'
    if (rank === 2) return 'bg-gray-400 text-white'
    if (rank === 3) return 'bg-amber-700 text-white'
    return 'bg-foreground text-background'
  }

  if (events.length === 0) return null

  return (
    <section className="pb-4 pt-5">
      <SectionHeader title="콘서트 랭킹" />
      <div className="scrollbar-hide flex gap-3 overflow-x-auto px-4 pb-1">
        {events.map((event, index) => (
          <button key={event.id} onClick={() => onEventClick(event.id)} className="flex w-32 flex-shrink-0 flex-col gap-1.5 text-left">
            <div className="relative h-44 w-32 overflow-hidden rounded-xl bg-secondary">
              <Image src={event.poster} alt={event.name} fill className="object-cover" sizes="128px" />
              <div className={`absolute left-2 top-2 flex h-6 w-6 items-center justify-center rounded-full ${getRankStyle(index + 1)}`}>
                <span className="text-xs font-bold">{index + 1}</span>
              </div>
            </div>
            <p className="line-clamp-2 text-xs font-semibold leading-snug text-foreground">{event.name}</p>
            <p className="text-[10px] text-muted-foreground">{getVenueLabel(event.venue)}</p>
          </button>
        ))}
      </div>
    </section>
  )
}

function OpenSchedule({
  events,
  onEventClick,
}: {
  events: Event[]
  onEventClick: (id: string) => void
}) {
  if (events.length === 0) return null

  return (
    <section className="bg-secondary/40 py-5">
      <SectionHeader title="오픈 예정" onMore={() => {}} />
      <div className="scrollbar-hide flex gap-3 overflow-x-auto px-4 pb-1">
        {events.map((event, index) => (
          <button
            key={event.id}
            onClick={() => onEventClick(event.id)}
            className="flex w-[80vw] max-w-[300px] flex-shrink-0 gap-3 rounded-2xl border border-border bg-card p-3 text-left transition-all hover:border-primary/40 active:scale-[0.98]"
          >
            <div className="relative h-24 w-20 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={event.poster} alt={event.name} fill className="object-cover" sizes="80px" />
            </div>
            <div className="min-w-0 flex-1 py-0.5">
              <p className={`text-sm font-bold ${index === 0 ? 'text-primary' : 'text-blue-600'}`}>{getOpenLabel(event)}</p>
              <p className="mt-1 line-clamp-2 text-sm font-semibold leading-snug text-foreground">{event.name}</p>
              <p className="text-xs text-muted-foreground">{getVenueLabel(event.venue)}</p>
              <div className="mt-2 flex flex-wrap gap-1.5">
                <span className="rounded-md border border-primary/20 bg-primary/10 px-2 py-0.5 text-[10px] font-bold text-primary">
                  {event.region}
                </span>
                <span className="rounded-md border border-border bg-secondary px-2 py-0.5 text-[10px] font-bold text-muted-foreground">
                  {event.status === 'ON_SALE' ? '판매중' : '매진'}
                </span>
              </div>
            </div>
          </button>
        ))}
      </div>
    </section>
  )
}

function DiscountSection() {
  const { resaleItems, navigate } = useApp()

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
      <SectionHeader title="즉시 할인 중" onMore={() => navigate('resale-list')} />
      <div className="divide-y divide-border px-4">
        {discounted.map((item) => (
          <button
            key={item.id}
            onClick={() =>
              navigate('resale-tickets', {
                eventId: item.eventId,
                resaleItemId: item.id,
                resaleEntrySource: 'home',
              })
            }
            className="-mx-4 flex gap-3 px-4 py-4 text-left transition-all hover:bg-secondary/50 active:scale-[0.99]"
          >
            <div className="relative h-32 w-24 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={item.poster} alt={item.eventName} fill className="object-cover" sizes="96px" />
              <div className="absolute left-1.5 top-1.5 rounded-md bg-red-500 px-1.5 py-0.5 text-[10px] font-bold text-white">
                -{item.discountPct}%
              </div>
            </div>

            <div className="min-w-0 flex-1 py-0.5">
              <div className="mb-1.5 inline-flex items-center gap-1 rounded-md bg-secondary px-2 py-0.5 text-[10px] font-medium text-muted-foreground">
                <Tag className="h-2.5 w-2.5" />
                2차 거래
              </div>
              <p className="line-clamp-2 text-sm font-semibold leading-snug text-foreground">{item.eventName}</p>
              <p className="mt-0.5 text-xs text-muted-foreground">{getVenueLabel(item.venue)}</p>
              <p className="text-xs text-muted-foreground">
                {item.seatLabel} · {item.grade}
              </p>
              <div className="mt-1.5 flex items-baseline gap-1.5">
                <span className="text-xs text-muted-foreground line-through">{item.originalPrice.toLocaleString()} CTK</span>
                <span className="text-sm font-bold text-red-500">{item.resalePrice.toLocaleString()} CTK</span>
              </div>
            </div>
          </button>
        ))}
      </div>
    </section>
  )
}

function RecommendationSection({
  onEventClick,
}: {
  onEventClick: (id: string) => void
}) {
  const { wishlist, navigate, events } = useApp()
  const recommendedEvent = events.find((event) => !wishlist.includes(event.id)) || events[0]

  return (
    <section className="py-5">
      <SectionHeader title="취향 추천 공연" />
      <div className="flex gap-3 px-4">
        <button
          onClick={() => navigate('wishlist')}
          className="flex h-36 w-28 flex-shrink-0 flex-col items-center justify-center gap-2 rounded-2xl border border-primary/30 bg-gradient-to-br from-primary/20 via-primary/10 to-secondary transition-all hover:border-primary/50 active:scale-[0.98]"
        >
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/20">
            <Heart className="h-6 w-6 text-primary" />
          </div>
          <div className="text-center">
            <p className="text-xs font-bold text-foreground">찜한 공연</p>
            <p className="text-lg font-bold text-primary">{wishlist.length}</p>
          </div>
        </button>

        {recommendedEvent && (
          <button
            onClick={() => onEventClick(recommendedEvent.id)}
            className="flex flex-1 gap-3 rounded-2xl border border-border bg-card p-3 text-left transition-all hover:border-primary/40 active:scale-[0.98]"
          >
            <div className="relative h-28 w-20 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={recommendedEvent.poster} alt={recommendedEvent.name} fill className="object-cover" sizes="80px" />
            </div>
            <div className="min-w-0 flex-1 py-0.5">
              <p className="line-clamp-2 text-sm font-semibold leading-snug text-foreground">{recommendedEvent.name}</p>
              <p className="mt-1 text-xs text-muted-foreground">{recommendedEvent.date}</p>
              <p className="text-xs text-muted-foreground">{getVenueLabel(recommendedEvent.venue)}</p>
              <div className="mt-1">
                <span className="text-xs font-medium text-primary">
                  {getLowestPrice(recommendedEvent) > 0 ? `${getLowestPrice(recommendedEvent).toLocaleString()} CTK~` : '가격 확인 예정'}
                </span>
              </div>
            </div>
          </button>
        )}
      </div>
    </section>
  )
}

export function HomeScreen() {
  const { navigate, events } = useApp()

  const visibleEvents = useMemo(() => events.filter((event) => event.poster), [events])
  const heroEvents = useMemo(() => visibleEvents.slice(0, 3), [visibleEvents])
  const rankingEvents = useMemo(() => visibleEvents.slice(0, 5), [visibleEvents])
  const openEvents = useMemo(() => visibleEvents.slice(5, 10), [visibleEvents])

  const goToEvent = (eventId: string) => navigate('event-detail', { eventId })

  return (
    <AppShell>
      <HeroBanner events={heroEvents} onEventClick={goToEvent} />
      <div className="h-px bg-border" />
      <CategoryGrid />
      <ConcertRanking events={rankingEvents} onEventClick={goToEvent} />
      <div className="h-2 bg-secondary/60" />
      <OpenSchedule events={openEvents} onEventClick={goToEvent} />
      <div className="h-2 bg-secondary/60" />
      <RecommendationSection onEventClick={goToEvent} />
      <div className="h-2 bg-secondary/60" />
      <DiscountSection />
    </AppShell>
  )
}
