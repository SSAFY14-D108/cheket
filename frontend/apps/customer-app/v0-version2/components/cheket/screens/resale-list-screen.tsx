'use client'

import { useMemo } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { EmptyState } from '../empty-state'
import { ShoppingBag } from 'lucide-react'

export function ResaleListScreen() {
  const { resaleItems, navigate, events } = useApp()

  // Group resale items by eventId
  const eventMap = useMemo(() => {
    const map = new Map<string, typeof resaleItems>()
    resaleItems.forEach((item) => {
      if (!map.has(item.eventId)) map.set(item.eventId, [])
      map.get(item.eventId)!.push(item)
    })
    return map
  }, [resaleItems])

  // Get events that have resale items, sorted with most items first
  const eventsWithResale = useMemo(() => {
    return Array.from(eventMap.entries())
      .map(([eventId, items]) => {
        const event = events.find((e) => e.id === eventId)
        return { event, itemCount: items.length }
      })
      .filter(({ event }) => event !== undefined)
      .sort((a, b) => b.itemCount - a.itemCount)
  }, [eventMap, events])

  return (
    <AppShell title="2차 거래소">
      <div className="flex flex-col gap-4 p-4">
        {/* Events grid */}
        {eventsWithResale.length === 0 ? (
          <EmptyState
            title="등록된 리세일 티켓이 없습니다"
            description="아직 리세일 등록된 티켓이 없습니다."
          />
        ) : (
          <div className="grid grid-cols-2 gap-3">
            {eventsWithResale.map(({ event, itemCount }) => (
              <button
                key={event!.id}
                onClick={() => navigate('resale-tickets', { eventId: event!.id })}
                className="bg-card border border-border rounded-lg overflow-hidden hover:border-primary/40 active:scale-[0.98] transition-all"
              >
                {/* Poster */}
                <div className="w-full h-32 bg-secondary relative overflow-hidden">
                  <img
                    src={event!.poster}
                    alt={event!.name}
                    className="w-full h-full object-cover"
                  />
                </div>
                {/* Info */}
                <div className="p-3">
                  <h3 className="text-xs font-semibold text-foreground line-clamp-2">{event!.name}</h3>
                  <div className="flex items-center gap-1.5 mt-2 text-muted-foreground">
                    <ShoppingBag className="w-3 h-3" />
                    <span className="text-xs font-medium">{itemCount}개</span>
                  </div>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
