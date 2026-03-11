'use client'

import Image from 'next/image'
import { useEffect, useMemo, useState } from 'react'
import { CalendarDays, ChevronDown, MapPin, Ticket, X } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { EmptyState } from '../empty-state'
import { ResaleCard } from '../resale-card'
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { ResaleDetailContent } from './resale-detail-screen'

type SortMode = 'latest' | 'price'
type SessionFilter = 'all' | string

const LABELS = {
  marketplace: '2차 거래소',
  all: '전체',
  session: '회차',
  sessionSelect: '회차 선택',
  sessionGuide: '원하는 공연 회차를 선택하세요.',
  latest: '최신순',
  price: '가격순',
  sellingCountPrefix: '판매 중 ',
  sellingCountSuffix: '건',
  eventNotFound: '공연 정보를 찾을 수 없습니다.',
  emptyTitle: '등록된 재판매 티켓이 없습니다.',
  emptyDescriptionSuffix: ' 재판매 티켓이 등록되면 여기에서 확인할 수 있습니다.',
} as const

export function ResaleTicketsScreen() {
  const { resaleItems, goBack, events, navParams } = useApp()
  const [sort, setSort] = useState<SortMode>('latest')
  const [sessionFilter, setSessionFilter] = useState<SessionFilter>('all')
  const [isSessionSheetOpen, setIsSessionSheetOpen] = useState(false)
  const [selectedResaleItemId, setSelectedResaleItemId] = useState<string | null>(
    (navParams.resaleItemId as string | undefined) ?? null
  )

  const eventId = navParams.eventId as string | undefined
  const foundEvent = events.find((item) => item.id === eventId)

  const eventItems = useMemo(
    () => resaleItems.filter((item) => item.eventId === eventId),
    [eventId, resaleItems]
  )

  // Build fallback event from resaleItem data when event is not in current events list
  const firstItem = eventItems[0]
  const event = foundEvent ?? (firstItem ? {
    id: eventId ?? '',
    name: firstItem.eventName,
    date: firstItem.eventDate,
    venue: firstItem.venue,
    poster: firstItem.poster,
    region: '',
    status: 'ON_SALE' as const,
    maxPerUser: 4,
    grades: [],
  } : null)

  const sessionOptions = useMemo(() => {
    const uniqueDates = Array.from(new Set(eventItems.map((item) => item.eventDate)))
    return [LABELS.all, ...uniqueDates]
  }, [eventItems])

  const actualSessionCount = sessionOptions.length - 1
  const useSessionSheet = actualSessionCount >= 4
  const selectedSessionLabel = sessionFilter === 'all' ? LABELS.all : sessionFilter
  const entrySource = navParams.resaleEntrySource ?? 'marketplace'

  const filtered = useMemo(() => {
    const items =
      sessionFilter === 'all'
        ? eventItems
        : eventItems.filter((item) => item.eventDate === sessionFilter)

    if (sort === 'price') {
      return [...items].sort((a, b) => a.resalePrice - b.resalePrice)
    }

    return [...items].reverse()
  }, [eventItems, sessionFilter, sort])

  useEffect(() => {
    const targetId = navParams.resaleItemId as string | undefined
    setSelectedResaleItemId(targetId ?? null)
  }, [navParams.resaleItemId])

  if (!event) {
    return (
      <AppShell title={LABELS.marketplace} showBack onBack={goBack}>
        <div className="flex h-screen items-center justify-center">
          <EmptyState title={LABELS.eventNotFound} />
        </div>
      </AppShell>
    )
  }

  return (
    <AppShell title={LABELS.marketplace} showBack onBack={goBack}>
      <div className="relative flex h-full flex-col">
        <div className="border-b border-border px-4 py-4">
          <div className="flex gap-3 rounded-2xl border border-border bg-card p-3">
            <div className="relative h-20 w-20 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={event.poster} alt={event.name} fill className="object-cover" sizes="80px" />
            </div>

            <div className="min-w-0 flex-1">
              <h3 className="line-clamp-2 text-lg font-bold leading-tight text-foreground">{event.name}</h3>
              <div className="mt-1.5 flex items-center gap-1.5 text-xs text-muted-foreground">
                <CalendarDays className="h-3.5 w-3.5 flex-shrink-0 text-primary" />
                <span className="truncate leading-5">{event.date}</span>
              </div>
              <div className="mt-1 flex items-center gap-1.5 text-xs text-muted-foreground">
                <MapPin className="h-3.5 w-3.5 flex-shrink-0 text-primary" />
                <span className="truncate leading-5">{event.venue}</span>
              </div>
              <div className="mt-2 inline-flex items-center gap-1.5 rounded-full bg-primary/10 px-2.5 py-1 text-[11px] font-semibold text-primary">
                <Ticket className="h-3 w-3" />
                <span>
                  {LABELS.sellingCountPrefix}
                  {filtered.length}
                  {LABELS.sellingCountSuffix}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-4">
          {actualSessionCount >= 2 && !useSessionSheet && (
            <div className="mb-3">
              <p className="mb-2 text-[11px] font-semibold text-muted-foreground">{LABELS.session}</p>
              <div className="flex gap-2 overflow-x-auto pb-1">
                {sessionOptions.map((option) => {
                  const value = option === LABELS.all ? 'all' : option
                  const isActive = sessionFilter === value

                  return (
                    <button
                      key={value}
                      onClick={() => setSessionFilter(value)}
                      className={`whitespace-nowrap rounded-full px-3 py-1.5 text-xs font-semibold transition-colors ${
                        isActive
                          ? 'bg-primary text-primary-foreground'
                          : 'bg-secondary text-muted-foreground hover:text-foreground'
                      }`}
                    >
                      {option}
                    </button>
                  )
                })}
              </div>
            </div>
          )}

          {useSessionSheet && (
            <div className="mb-3">
              <p className="mb-2 text-[11px] font-semibold text-muted-foreground">{LABELS.session}</p>
              <button
                onClick={() => setIsSessionSheetOpen(true)}
                className="flex w-full items-center justify-between rounded-xl border border-border bg-card px-3 py-2 text-left transition-colors hover:border-primary/40"
              >
                <div className="min-w-0">
                  <p className="text-xs font-semibold text-foreground">{LABELS.sessionSelect}</p>
                  <p className="truncate text-xs text-muted-foreground">{selectedSessionLabel}</p>
                </div>
                <ChevronDown className="h-4 w-4 flex-shrink-0 text-muted-foreground" />
              </button>
            </div>
          )}

          <div className="mb-4 flex gap-2">
            {([
              ['latest', LABELS.latest],
              ['price', LABELS.price],
            ] as [SortMode, string][]).map(([value, label]) => (
              <button
                key={value}
                onClick={() => setSort(value)}
                className={`rounded-full px-3 py-1.5 text-xs font-semibold transition-colors ${
                  sort === value
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
              title={LABELS.emptyTitle}
              description={`${event.name}${LABELS.emptyDescriptionSuffix}`}
            />
          ) : (
            <div className="flex flex-col gap-2.5">
              {filtered.map((item) => (
                <ResaleCard
                  key={item.id}
                  item={item}
                  onClick={() => setSelectedResaleItemId(item.id)}
                />
              ))}
            </div>
          )}
        </div>

        <Sheet open={isSessionSheetOpen} onOpenChange={setIsSessionSheetOpen}>
          <SheetContent side="bottom" className="rounded-t-3xl px-0 pb-6">
            <div className="mx-auto mt-3 h-1.5 w-12 rounded-full bg-border" />
            <SheetHeader className="px-4 pb-2">
              <SheetTitle>{LABELS.sessionSelect}</SheetTitle>
              <SheetDescription>{LABELS.sessionGuide}</SheetDescription>
            </SheetHeader>
            <div className="max-h-[50vh] overflow-y-auto px-4">
              <div className="flex flex-col gap-2">
                {sessionOptions.map((option) => {
                  const value = option === LABELS.all ? 'all' : option
                  const isActive = sessionFilter === value

                  return (
                    <button
                      key={value}
                      onClick={() => {
                        setSessionFilter(value)
                        setIsSessionSheetOpen(false)
                      }}
                      className={`rounded-xl border px-3 py-3 text-left text-sm transition-colors ${
                        isActive
                          ? 'border-primary bg-primary/10 text-primary'
                          : 'border-border bg-card text-foreground hover:border-primary/40'
                      }`}
                    >
                      {option}
                    </button>
                  )
                })}
              </div>
            </div>
          </SheetContent>
        </Sheet>

        {selectedResaleItemId && (
          <div className="absolute inset-0 z-50 bg-black/45">
            <div className="absolute inset-0 flex flex-col bg-background">
              <div className="flex items-center justify-between border-b border-border px-4 py-3">
                <div className="w-8" />
                <h2 className="text-base font-semibold text-foreground">재판매 티켓</h2>
                <button
                  onClick={() => {
                    setSelectedResaleItemId(null)
                    if (entrySource === 'home') {
                      goBack()
                    }
                  }}
                  className="flex h-8 w-8 items-center justify-center rounded-full transition-colors hover:bg-secondary"
                  aria-label="닫기"
                >
                  <X className="h-5 w-5 text-foreground" />
                </button>
              </div>
              <div className="flex-1 overflow-y-auto">
                <ResaleDetailContent resaleItemId={selectedResaleItemId} embedded />
              </div>
            </div>
          </div>
        )}
      </div>
    </AppShell>
  )
}
