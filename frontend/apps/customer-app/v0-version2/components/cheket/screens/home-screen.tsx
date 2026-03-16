'use client'

import { useEffect, useMemo, useRef, useState } from 'react'
import Image from 'next/image'
import { ChevronRight, Heart, Tag } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import type { Event } from '@/lib/types'
import { AppShell } from '../app-shell'

function getLowestPrice(event: Event) {
  const prices = event.grades.map((grade) => grade.price).filter((price) => price > 0)
  return prices.length > 0 ? Math.min(...prices) : 0
}

function getVenueLabel(venue: string) {
  return venue.split(',')[0]?.trim() || venue
}

function getBannerSubtitle(event: Event) {
  return event.artistName || event.region || ''
}

function getOpenLabel(event: Event) {
  return event.date || '오픈 예정'
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
    <div className="relative w-full overflow-hidden aspect-[4/3]">
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

function SectionHeader({ title, onMore }: { title: string; onMore?: () => void }) {
  return (
    <div className="mb-3 flex items-center justify-between px-4">
      <h2 className="text-base font-bold text-foreground">{title}</h2>
      {onMore ? (
        <button onClick={onMore} className="flex items-center gap-0.5 text-xs text-muted-foreground transition-colors hover:text-foreground">
          더보기 <ChevronRight className="h-3.5 w-3.5" />
        </button>
      ) : null}
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
  if (events.length === 0) return null

  const getRankBadgeClass = (rank: number) => {
    if (rank === 1) return 'bg-yellow-500'
    if (rank === 2) return 'bg-gray-400'
    if (rank === 3) return 'bg-amber-700'
    return 'bg-gray-700'
  }

  return (
    <section className="pb-4 pt-5">
      <SectionHeader title="랭킹" />
      <div className="scrollbar-hide flex gap-3 overflow-x-auto px-4 pb-1">
        {events.map((event, index) => (
          <button
            key={event.id}
            onClick={() => onEventClick(event.id)}
            className="gradient-outline-surface-soft flex w-32 flex-shrink-0 flex-col gap-1.5 rounded-2xl p-2 text-left shadow-[0_6px_18px_rgba(15,23,42,0.04)] transition-all hover:shadow-[0_10px_24px_rgba(15,23,42,0.08)]"
          >
            <div className="relative h-44 w-full overflow-hidden rounded-xl bg-secondary">
              <Image src={event.poster} alt={event.name} fill className="object-cover" sizes="128px" />
              <div className={`absolute left-2 top-2 flex h-6 w-6 items-center justify-center rounded-full ${getRankBadgeClass(index + 1)}`}>
                <span className="text-xs font-bold text-white">{index + 1}</span>
              </div>
            </div>
            <p className="line-clamp-2 text-xs font-semibold leading-snug text-gray-900">{event.name}</p>
            <p className="text-[10px] text-gray-500">{getVenueLabel(event.venue)}</p>
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
    <section className="py-5">
      <SectionHeader title="오픈 예정" onMore={() => {}} />
      <div className="scrollbar-hide flex gap-3 overflow-x-auto px-4 pb-1">
        {events.map((event, index) => (
          <button
            key={event.id}
            onClick={() => onEventClick(event.id)}
            className="gradient-outline-surface-soft flex w-[80vw] max-w-[300px] flex-shrink-0 gap-3 rounded-2xl p-3 text-left shadow-[0_6px_18px_rgba(15,23,42,0.04)] transition-all hover:shadow-[0_10px_24px_rgba(15,23,42,0.08)] active:scale-[0.98]"
          >
            <div className="relative h-24 w-20 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={event.poster} alt={event.name} fill className="object-cover" sizes="80px" />
            </div>
            <div className="min-w-0 flex-1 py-0.5">
              <p className={`text-sm font-bold ${index === 0 ? 'text-[#111111]' : 'text-[#4b5563]'}`}>{getOpenLabel(event)}</p>
              <p className="mt-1 line-clamp-2 text-sm font-semibold leading-snug text-gray-900">{event.name}</p>
              <p className="text-xs text-gray-500">{getVenueLabel(event.venue)}</p>
              <div className="mt-2 flex flex-wrap gap-1.5">
                <span className="rounded-md bg-[#e8f6ef] px-2 py-0.5 text-[10px] font-bold text-[#333333] shadow-[0_4px_12px_rgba(196,247,224,0.24)]">{event.region}</span>
                <span className="rounded-md bg-gray-100 px-2 py-0.5 text-[10px] font-bold text-muted-foreground">
                  {event.status === 'ON_SALE' ? '예매중' : '매진'}
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
      <SectionHeader title="타임 세일" onMore={() => navigate('resale-list')} />
      <div className="flex flex-col gap-3 px-4">
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
            className="gradient-outline-surface-soft flex gap-3 rounded-2xl px-4 py-4 text-left shadow-[0_6px_18px_rgba(15,23,42,0.04)] transition-all hover:shadow-[0_10px_24px_rgba(15,23,42,0.08)] active:scale-[0.99]"
          >
            <div className="relative h-32 w-24 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={item.poster} alt={item.eventName} fill className="object-cover" sizes="96px" />
              <div className="absolute left-1.5 top-1.5 rounded-md bg-red-500 px-1.5 py-0.5 text-[10px] font-bold text-white">
                -{item.discountPct}%
              </div>
            </div>

            <div className="min-w-0 flex-1 py-0.5">
              <div className="mb-1.5 inline-flex items-center gap-1 rounded-md bg-[#e8f6ef] px-2 py-0.5 text-[10px] font-medium text-muted-foreground shadow-[0_4px_12px_rgba(196,247,224,0.22)]">
                <Tag className="h-2.5 w-2.5" />
                2차 거래
              </div>
              <p className="line-clamp-2 text-sm font-semibold leading-snug text-gray-900">{item.eventName}</p>
              <p className="mt-0.5 text-xs text-gray-500">{getVenueLabel(item.venue)}</p>
              <p className="text-xs text-gray-500">
                {item.seatLabel} · {item.grade}
              </p>
              <div className="mt-1.5 flex items-baseline gap-1.5">
                <span className="text-xs text-gray-400 line-through">{item.originalPrice.toLocaleString()} CTK</span>
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
      <SectionHeader title="취향 저격 추천" />
      <div className="flex gap-3 px-4">
        <button
          onClick={() => navigate('wishlist')}
          className="gradient-outline-surface-soft flex h-36 w-28 flex-shrink-0 flex-col items-center justify-center gap-2 rounded-2xl shadow-[0_6px_18px_rgba(15,23,42,0.04)] transition-all hover:shadow-[0_10px_24px_rgba(15,23,42,0.08)] active:scale-[0.98]"
        >
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-100">
            <Heart className="h-6 w-6 text-[#333333]" />
          </div>
          <div className="text-center">
            <p className="text-xs font-bold text-gray-900">찜한 공연</p>
            <p className="text-lg font-bold text-[#111111]">{wishlist.length}</p>
          </div>
        </button>

        {recommendedEvent ? (
          <button
            onClick={() => onEventClick(recommendedEvent.id)}
            className="gradient-outline-surface-soft flex flex-1 gap-3 rounded-2xl p-3 text-left shadow-[0_6px_18px_rgba(15,23,42,0.04)] transition-all hover:shadow-[0_10px_24px_rgba(15,23,42,0.08)] active:scale-[0.98]"
          >
            <div className="relative h-28 w-20 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={recommendedEvent.poster} alt={recommendedEvent.name} fill className="object-cover" sizes="80px" />
            </div>
            <div className="min-w-0 flex-1 py-0.5">
              <p className="line-clamp-2 text-sm font-semibold leading-snug text-gray-900">{recommendedEvent.name}</p>
              <p className="mt-1 text-xs text-gray-500">{recommendedEvent.date}</p>
              <p className="text-xs text-gray-500">{getVenueLabel(recommendedEvent.venue)}</p>
              <div className="mt-1">
                <span className="text-xs font-medium text-[#333333]">
                  {getLowestPrice(recommendedEvent) > 0
                    ? `${getLowestPrice(recommendedEvent).toLocaleString()} CTK~`
                    : '가격 정보 확인'}
                </span>
              </div>
            </div>
          </button>
        ) : null}
      </div>
    </section>
  )
}

export function HomeScreen() {
  const { navigate, allEvents } = useApp()

  const visibleEvents = useMemo(() => allEvents.filter((event) => event.poster), [allEvents])
  const heroEvents = useMemo(() => visibleEvents.slice(0, 3), [visibleEvents])
  const rankingEvents = useMemo(() => visibleEvents.slice(0, 5), [visibleEvents])
  const openEvents = useMemo(() => visibleEvents.slice(5, 10), [visibleEvents])

  const goToEvent = (eventId: string) => navigate('event-detail', { eventId })

  return (
    <AppShell>
      <div className="min-h-full bg-gray-50">
        <HeroBanner events={heroEvents} onEventClick={goToEvent} />
        <ConcertRanking events={rankingEvents} onEventClick={goToEvent} />
        <div className="h-2 bg-gray-100" />
        <OpenSchedule events={openEvents} onEventClick={goToEvent} />
        <div className="h-2 bg-gray-100" />
        <RecommendationSection onEventClick={goToEvent} />
        <div className="h-2 bg-gray-100" />
        <DiscountSection />
      </div>
    </AppShell>
  )
}
