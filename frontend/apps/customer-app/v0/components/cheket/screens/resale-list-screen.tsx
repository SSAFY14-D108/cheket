'use client'

import { useMemo } from 'react'
import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { ResaleCard } from '../resale-card'
import { EmptyState } from '../empty-state'
import { ChevronRight } from 'lucide-react'

export function ResaleListScreen() {
  const { resaleItems, navParams, navigate, goBack } = useApp()
  const selectedEventName = navParams.resaleEventName

  const eventSummaries = useMemo(() => {
    const map = new Map<
      string,
      {
        eventName: string
        eventDate: string
        venue: string
        poster: string
        count: number
        minPrice: number
      }
    >()

    resaleItems.forEach((item) => {
      const prev = map.get(item.eventName)
      if (!prev) {
        map.set(item.eventName, {
          eventName: item.eventName,
          eventDate: item.eventDate,
          venue: item.venue,
          poster: item.poster,
          count: 1,
          minPrice: item.resalePrice,
        })
        return
      }
      prev.count += 1
      prev.minPrice = Math.min(prev.minPrice, item.resalePrice)
    })

    return Array.from(map.values()).sort((a, b) => a.minPrice - b.minPrice)
  }, [resaleItems])

  const sortedTickets = useMemo(() => {
    if (!selectedEventName) return []
    return resaleItems
      .filter((item) => item.eventName === selectedEventName)
      .sort((a, b) => a.resalePrice - b.resalePrice)
  }, [resaleItems, selectedEventName])

  if (selectedEventName) {
    return (
      <AppShell showBack onBack={goBack} title={selectedEventName} showNotification={false}>
        <div className="flex flex-col gap-3 p-4">
          <p className="text-xs text-muted-foreground">가격 낮은 순</p>
          {sortedTickets.length === 0 ? (
            <EmptyState
              title="판매중인 티켓이 없습니다"
              description="이 공연의 판매 티켓이 아직 없습니다."
            />
          ) : (
            sortedTickets.map((item) => (
              <ResaleCard
                key={item.id}
                item={item}
                onClick={() => navigate('resale-detail', { resaleItemId: item.id })}
              />
            ))
          )}
        </div>
      </AppShell>
    )
  }

  return (
    <AppShell showNotification={false} title="티켓 줍줍">
      <div className="flex flex-col gap-4 p-4">
        <p className="text-xs text-muted-foreground">공연을 선택하면 판매중인 티켓을 볼 수 있어요.</p>

        {eventSummaries.length === 0 ? (
          <EmptyState
            title="등록된 리세일 티켓이 없습니다"
            description="아직 양도 등록된 티켓이 없습니다."
          />
        ) : (
          <div className="flex flex-col gap-3">
            {eventSummaries.map((event) => (
              <button
                key={event.eventName}
                onClick={() => navigate('resale-list', { resaleEventName: event.eventName })}
                className="w-full flex gap-3 p-3 bg-card rounded-xl border border-border hover:border-primary/40 active:scale-[0.98] transition-all text-left"
              >
                <div className="relative w-20 h-20 rounded-lg overflow-hidden flex-shrink-0 bg-secondary">
                  <Image src={event.poster} alt={event.eventName} fill className="object-cover" sizes="80px" />
                </div>
                <div className="flex-1 min-w-0 py-0.5">
                  <h3 className="font-semibold text-sm text-foreground leading-tight line-clamp-1 mb-1">
                    {event.eventName}
                  </h3>
                  <p className="text-xs text-muted-foreground line-clamp-1">{event.venue}</p>
                  <p className="text-xs text-muted-foreground mb-2">{event.eventDate}</p>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-muted-foreground">{event.count}건 판매중</span>
                      <span className="font-bold text-sm text-foreground">
                        {event.minPrice.toLocaleString()} CTK~
                      </span>
                    </div>
                    <ChevronRight className="w-4 h-4 text-muted-foreground" />
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
