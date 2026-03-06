'use client'

import { useEffect, useMemo, useRef, useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { EmptyState } from '../empty-state'
import { ShoppingBag, Calendar, MapPin } from 'lucide-react'

const PAGE_SIZE = 4

export function ResaleListScreen() {
  const { resaleItems, navigate, events } = useApp()
  const loadMoreRef = useRef<HTMLDivElement | null>(null)
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)

  const eventMap = useMemo(() => {
    const map = new Map<string, typeof resaleItems>()
    resaleItems.forEach((item) => {
      if (!map.has(item.eventId)) map.set(item.eventId, [])
      map.get(item.eventId)!.push(item)
    })
    return map
  }, [resaleItems])

  const eventsWithResale = useMemo(() => {
    return Array.from(eventMap.entries())
      .map(([eventId, items]) => {
        const event = events.find((value) => value.id === eventId)
        return { event, itemCount: items.length }
      })
      .filter(({ event }) => event !== undefined)
      .sort((a, b) => b.itemCount - a.itemCount)
  }, [eventMap, events])

  const visibleEvents = eventsWithResale.slice(0, visibleCount)
  const hasMore = visibleCount < eventsWithResale.length

  useEffect(() => {
    setVisibleCount(PAGE_SIZE)
  }, [eventsWithResale.length])

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
        {eventsWithResale.length === 0 ? (
          <EmptyState
            title="등록된 2차 거래 공연이 없습니다"
            description="현재 등록된 재판매 티켓이 없습니다."
          />
        ) : (
          <>
            <div className="grid grid-cols-2 gap-3">
              {visibleEvents.map(({ event, itemCount }) => (
                <button
                  key={event!.id}
                  onClick={() => navigate('resale-tickets', { eventId: event!.id })}
                  className="bg-card border border-border rounded-2xl overflow-hidden hover:border-primary/40 active:scale-[0.98] transition-all text-left"
                >
                  <div className="w-full h-24 bg-secondary relative overflow-hidden">
                    <img
                      src={event!.poster}
                      alt={event!.name}
                      className="w-full h-full object-cover"
                    />
                  </div>

                  <div className="p-2.5 flex flex-col gap-1.5">
                    <h3 className="text-[11px] font-semibold text-foreground line-clamp-2 min-h-[2rem]">
                      {event!.name}
                    </h3>

                    <div className="flex flex-col gap-1 text-muted-foreground">
                      <div className="flex items-center gap-1">
                        <Calendar className="w-3 h-3 flex-shrink-0" />
                        <span className="text-[10px] line-clamp-1">{event!.date}</span>
                      </div>
                      <div className="flex items-center gap-1">
                        <MapPin className="w-3 h-3 flex-shrink-0" />
                        <span className="text-[10px] line-clamp-1">{event!.venue}</span>
                      </div>
                    </div>

                    <div className="flex items-center gap-1 text-muted-foreground pt-0.5">
                      <ShoppingBag className="w-3 h-3" />
                      <span className="text-[10px] font-medium">{itemCount}건</span>
                    </div>
                  </div>
                </button>
              ))}
            </div>

            <div ref={loadMoreRef} className="h-14 flex items-center justify-center">
              {hasMore ? (
                <span className="text-xs text-muted-foreground">공연을 더 불러오는 중...</span>
              ) : (
                <span className="text-xs text-muted-foreground">모든 2차 거래 공연을 확인했습니다.</span>
              )}
            </div>
          </>
        )}
      </div>
    </AppShell>
  )
}
