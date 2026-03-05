'use client'

import { useState, useMemo } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { ResaleCard } from '../resale-card'
import { EmptyState } from '../empty-state'
import { ChevronLeft } from 'lucide-react'

type SortMode = 'latest' | 'price'

export function ResaleTicketsScreen() {
  const { resaleItems, navigate, goBack, events, navParams } = useApp()
  const [sort, setSort] = useState<SortMode>('latest')

  const eventId = navParams.eventId as string | undefined
  const event = events.find((e) => e.id === eventId)

  // Filter resale items by eventId
  const filtered = useMemo(() => {
    const items = resaleItems.filter((item) => item.eventId === eventId)
    if (sort === 'price') return items.sort((a, b) => a.resalePrice - b.resalePrice)
    return items
  }, [resaleItems, eventId, sort])

  if (!event) {
    return (
      <AppShell showNotification={false}>
        <div className="flex items-center justify-center h-screen">
          <EmptyState title="공연을 찾을 수 없습니다" />
        </div>
      </AppShell>
    )
  }

  return (
    <AppShell showNotification={false}>
      <div className="flex flex-col h-full">
        {/* Header */}
        <div className="p-4 border-b border-border flex items-center gap-3">
          <button
            onClick={goBack}
            className="p-2 hover:bg-secondary rounded-lg transition-colors"
          >
            <ChevronLeft className="w-5 h-5 text-foreground" />
          </button>
          <div className="flex-1 min-w-0">
            <h2 className="text-sm font-bold text-foreground truncate">{event.name}</h2>
            <p className="text-xs text-muted-foreground">{event.date}</p>
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto flex flex-col gap-4 p-4">
          {/* Sort options */}
          <div className="flex gap-2">
            {([['latest', '최신순'], ['price', '가격순']] as [SortMode, string][]).map(([key, label]) => (
              <button
                key={key}
                onClick={() => setSort(key)}
                className={`px-3 py-1.5 rounded-full text-xs font-medium transition-colors ${
                  sort === key
                    ? 'bg-primary text-primary-foreground'
                    : 'bg-secondary text-muted-foreground hover:text-foreground'
                }`}
              >
                {label}
              </button>
            ))}
          </div>

          {filtered.length === 0 ? (
            <EmptyState
              title="등록된 리세일 티켓이 없습니다"
              description={`${event.name}의 리세일 티켓이 없습니다.`}
            />
          ) : (
            <div className="flex flex-col gap-3">
              {filtered.map((item) => (
                <ResaleCard
                  key={item.id}
                  item={item}
                  onClick={() => navigate('resale-detail', { resaleItemId: item.id })}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </AppShell>
  )
}
