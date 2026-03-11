'use client'

import Image from 'next/image'
import { useEffect, useMemo, useRef, useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { EmptyState } from '../empty-state'
import { Search, Calendar, MapPin, Tag, X } from 'lucide-react'
import { cn } from '@/lib/utils'

const PAGE_SIZE = 4
const REGIONS = ['전체', '서울', '경기', '인천', '부산', '대구', '광주', '경남', '전북']

export function ResaleListScreen() {
  const { resaleItems, navigate, events } = useApp()
  const loadMoreRef = useRef<HTMLDivElement | null>(null)
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)
  const [query, setQuery] = useState('')
  const [selectedRegion, setSelectedRegion] = useState('전체')

  const groupedByEvent = useMemo(() => {
    const map = new Map<string, typeof resaleItems>()

    resaleItems.forEach((item) => {
      if (!map.has(item.eventId)) {
        map.set(item.eventId, [])
      }
      map.get(item.eventId)!.push(item)
    })

    return map
  }, [resaleItems])

  const eventsWithResale = useMemo(() => {
    const keyword = query.trim().toLowerCase()

    return Array.from(groupedByEvent.entries())
      .map(([eventId, items]) => {
        const event = events.find((value) => value.id === eventId)

        // Build display info from event or fallback to resaleItem data
        const firstItem = items[0]
        const displayEvent = event ?? {
          id: eventId,
          name: firstItem.eventName,
          date: firstItem.eventDate,
          venue: firstItem.venue,
          poster: firstItem.poster,
          region: '',
          status: 'ON_SALE' as const,
          maxPerUser: 4,
          grades: [],
          artistName: '',
        }

        const minPrice = Math.min(...items.map((item) => item.resalePrice))
        return {
          event: displayEvent,
          itemCount: items.length,
          minPrice,
        }
      })
      .filter(({ event }) => {
        const matchQuery =
          keyword.length === 0 ||
          event.name.toLowerCase().includes(keyword) ||
          event.venue.toLowerCase().includes(keyword) ||
          (event.artistName ?? '').toLowerCase().includes(keyword)

        const matchRegion = selectedRegion === '전체' || event.region === selectedRegion
        return matchQuery && matchRegion
      })
      .sort((a, b) => a.minPrice - b.minPrice)
  }, [events, groupedByEvent, query, selectedRegion])

  const visibleEvents = eventsWithResale.slice(0, visibleCount)
  const hasMore = visibleCount < eventsWithResale.length
  const activeFilterCount = selectedRegion === '전체' ? 0 : 1

  useEffect(() => {
    setVisibleCount(PAGE_SIZE)
  }, [eventsWithResale.length, query, selectedRegion])

  useEffect(() => {
    const node = loadMoreRef.current
    if (!node || !hasMore) return

    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries
        if (entry?.isIntersecting) {
          setVisibleCount((prev) => Math.min(prev + PAGE_SIZE, eventsWithResale.length))
        }
      },
      { rootMargin: '160px 0px' }
    )

    observer.observe(node)
    return () => observer.disconnect()
  }, [eventsWithResale.length, hasMore])

  return (
    <AppShell title="2차 거래소">
      <div className="flex flex-col gap-4 p-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <input
            className="w-full rounded-xl border border-border bg-secondary py-3 pl-10 pr-4 text-sm text-foreground placeholder:text-muted-foreground focus:border-primary focus:outline-none transition-colors"
            placeholder="공연명, 아티스트, 장소 검색"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>

        <div className="flex gap-2 overflow-x-auto pb-1">
          {REGIONS.map((region) => (
            <button
              key={region}
              onClick={() => setSelectedRegion((prev) => {
                if (region === '전체') return '전체'
                return prev === region ? '전체' : region
              })}
              className={cn(
                'whitespace-nowrap rounded-full px-3 py-1.5 text-xs font-semibold transition-colors',
                selectedRegion === region
                  ? 'bg-primary text-primary-foreground'
                  : 'bg-secondary text-muted-foreground hover:text-foreground'
              )}
            >
              {region}
            </button>
          ))}
        </div>

        {activeFilterCount > 0 && (
          <div className="flex flex-wrap gap-2">
            <span className="inline-flex items-center gap-1 rounded-full bg-primary/10 px-2.5 py-1 text-xs font-medium text-primary">
              {selectedRegion}
              <button onClick={() => setSelectedRegion('전체')}>
                <X className="h-3 w-3" />
              </button>
            </span>
          </div>
        )}

        {eventsWithResale.length === 0 ? (
          <EmptyState
            title="조건에 맞는 2차 거래 티켓이 없어요"
            description="검색어나 필터를 바꿔서 다시 확인해 보세요."
          />
        ) : (
          <>
            <div className="grid grid-cols-2 gap-3">
              {visibleEvents.map(({ event, minPrice }, index) => (
                <button
                  key={`${event.id}-${event.date}-${event.venue}-${index}`}
                  onClick={() =>
                    navigate('resale-tickets', {
                      eventId: event.id,
                      resaleEntrySource: 'marketplace',
                    })
                  }
                  className="overflow-hidden rounded-2xl border border-border bg-card text-left transition-all hover:border-primary/40 active:scale-[0.98]"
                >
                  <div className="relative h-28 w-full overflow-hidden bg-secondary">
                    <Image
                      src={event.poster}
                      alt={event.name}
                      fill
                      className="object-cover"
                      sizes="(max-width: 390px) 50vw, 195px"
                    />
                  </div>

                  <div className="flex flex-col gap-1.5 p-3">
                    <h3 className="min-h-[2.2rem] text-xs font-semibold leading-[1.35] text-foreground line-clamp-2">
                      {event.name}
                    </h3>

                    <div className="flex flex-col gap-1 text-muted-foreground">
                      <div className="flex items-center gap-1">
                        <Calendar className="h-3 w-3 flex-shrink-0" />
                        <span className="line-clamp-1 text-[10px]">{event.date}</span>
                      </div>
                      <div className="flex items-center gap-1">
                        <MapPin className="h-3 w-3 flex-shrink-0" />
                        <span className="line-clamp-1 text-[10px]">{event.venue}</span>
                      </div>
                    </div>

                    <div className="flex items-center gap-1 pt-0.5 text-primary">
                      <Tag className="h-3 w-3" />
                      <span className="text-[10px] font-semibold">
                        {minPrice.toLocaleString()} ~
                      </span>
                    </div>
                  </div>
                </button>
              ))}
            </div>

            <div
              ref={loadMoreRef}
              className={cn('flex items-center justify-center', hasMore ? 'h-10' : 'h-2')}
            >
              {hasMore ? (
                <span className="text-xs text-muted-foreground">거래 티켓을 더 불러오는 중...</span>
              ) : null}
            </div>
          </>
        )}
      </div>
    </AppShell>
  )
}
