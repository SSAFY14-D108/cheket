'use client'

import { useEffect, useMemo, useRef, useState } from 'react'
import { Calendar, ChevronLeft, ChevronRight } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { fetchKopisEventDetail } from '@/lib/kopis-client'
import type { Event, EventDate } from '@/lib/types'
import { cn } from '@/lib/utils'
import { AppShell } from '../app-shell'

const KR_WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'] as const

function parseDateFromLabel(label: string) {
  const match = label.match(/(\d{4})\.(\d{2})\.(\d{2})/)
  if (!match) return null
  return { year: Number(match[1]), month: Number(match[2]), day: Number(match[3]) }
}

function needsKopisDetail(event: Event) {
  return event.id.startsWith('kopis_') && (!event.priceInfo || !event.runtime || !event.dates || event.dates.length === 0 || event.grades.every((grade) => grade.price === 0))
}

export function EventDateSelectionScreen() {
  const { navParams, navigate, goBack, events, updateEvent } = useApp()
  const baseEvent = events.find((item) => item.id === navParams.eventId)
  const [resolvedEvent, setResolvedEvent] = useState<Event | null>(baseEvent ?? null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [calendarDayShows, setCalendarDayShows] = useState<EventDate[]>([])
  const fetchedEventIdsRef = useRef<Set<string>>(new Set())

  useEffect(() => {
    let cancelled = false

    async function enrichEvent() {
      if (!baseEvent) {
        setResolvedEvent(null)
        return
      }

      setResolvedEvent(baseEvent)
      if (!needsKopisDetail(baseEvent)) return
      if (fetchedEventIdsRef.current.has(baseEvent.id)) return

      setDetailLoading(true)
      const detail = await fetchKopisEventDetail(baseEvent.id)

      if (!cancelled && detail) {
        fetchedEventIdsRef.current.add(baseEvent.id)
        setResolvedEvent(detail)
        updateEvent(baseEvent.id, detail)
      }

      if (!cancelled) setDetailLoading(false)
    }

    void enrichEvent()
    return () => {
      cancelled = true
    }
  }, [baseEvent?.id, baseEvent?.priceInfo, baseEvent?.grades, updateEvent])

  if (!resolvedEvent) return null

  const eventDates = useMemo<EventDate[]>(() => {
    if (resolvedEvent.dates && resolvedEvent.dates.length > 0) return resolvedEvent.dates
    return [{ id: `${resolvedEvent.id}_d1`, label: resolvedEvent.date, day: 'DAY 1' }]
  }, [resolvedEvent])

  const firstParsed = parseDateFromLabel(eventDates[0].label) ?? { year: 2026, month: 1, day: 1 }
  const calYear = firstParsed.year
  const calMonth = firstParsed.month
  const firstDayOfMonth = new Date(calYear, calMonth - 1, 1).getDay()
  const daysInMonth = new Date(calYear, calMonth, 0).getDate()
  const cells: (number | null)[] = [...Array(firstDayOfMonth).fill(null), ...Array.from({ length: daysInMonth }, (_, i) => i + 1)]
  while (cells.length % 7 !== 0) cells.push(null)

  const availableMap = new Map<string, EventDate[]>()
  eventDates.forEach((eventDate) => {
    const parsed = parseDateFromLabel(eventDate.label)
    if (!parsed) return
    const key = `${parsed.year}-${String(parsed.month).padStart(2, '0')}-${String(parsed.day).padStart(2, '0')}`
    if (!availableMap.has(key)) availableMap.set(key, [])
    availableMap.get(key)?.push(eventDate)
  })

  return (
    <AppShell showBack onBack={goBack} title="공연 날짜 선택" showBottomNav={false}>
      <div className="flex flex-col gap-4 bg-gray-50 p-4">
        <p className="text-xs text-muted-foreground">원하는 날짜와 회차를 선택하면 예매 단계로 이어집니다.</p>

        {detailLoading && <div className="elevated-surface rounded-xl px-4 py-3 text-xs text-muted-foreground">KOPIS 상세 정보를 불러오는 중입니다.</div>}

        <div className="elevated-surface overflow-hidden rounded-2xl">
          <div className="flex items-center justify-between border-b border-border px-5 py-4">
            <ChevronLeft className="h-5 w-5 text-muted-foreground/30" />
            <span className="text-sm font-bold text-[#111111]">
              {calYear}.{String(calMonth).padStart(2, '0')}
            </span>
            <ChevronRight className="h-5 w-5 text-muted-foreground/30" />
          </div>

          <div className="grid grid-cols-7 px-3 pb-1 pt-3">
            {KR_WEEKDAYS.map((weekday, index) => (
              <div
                key={weekday}
                className={cn('py-1 text-center text-xs font-semibold', index === 0 ? 'text-red-400' : index === 6 ? 'text-blue-400' : 'text-muted-foreground')}
              >
                {weekday}
              </div>
            ))}
          </div>

          <div className="grid grid-cols-7 gap-y-1 px-3 pb-4">
            {cells.map((day, index) => {
              if (day === null) return <div key={`empty-${index}`} />

              const key = `${calYear}-${String(calMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`
              const datesOnDay = availableMap.get(key) ?? []
              const hasShow = datesOnDay.length > 0
              const dayOfWeek = (firstDayOfMonth + day - 1) % 7

              return (
                <button
                  key={day}
                  disabled={!hasShow}
                  onClick={() => setCalendarDayShows(datesOnDay)}
                  className={cn('relative flex aspect-square flex-col items-center justify-center rounded-xl transition-all', hasShow ? 'hover:scale-105 active:scale-95' : 'cursor-default')}
                >
                  <span
                    className={cn(
                      'flex h-8 w-8 items-center justify-center rounded-full text-sm font-semibold transition-colors',
                      hasShow
                        ? 'bg-[#eef2f1] text-[#111111]'
                        : dayOfWeek === 0
                          ? 'text-red-400/30'
                          : dayOfWeek === 6
                            ? 'text-blue-400/30'
                            : 'text-muted-foreground/30'
                    )}
                  >
                    {day}
                  </span>
                  {hasShow && <span className="absolute bottom-0.5 h-1 w-1 rounded-full bg-[#9aa4b2]" />}
                </button>
              )
            })}
          </div>
        </div>

        {calendarDayShows.length > 0 && (
          <div className="flex flex-col gap-3">
            <p className="px-1 text-xs font-semibold text-[#111111]">선택 가능한 회차</p>
            {calendarDayShows.map((eventDate) => (
              <button
                key={eventDate.id}
                onClick={() => navigate('waiting-queue', { eventId: resolvedEvent.id, eventDateId: eventDate.id })}
                disabled={detailLoading}
                className="elevated-surface flex flex-col gap-3 rounded-2xl p-4 text-left transition-all active:scale-[0.98] disabled:opacity-60"
              >
                <div className="flex w-full items-center justify-between">
                  <div>
                    <p className="text-xs font-bold uppercase tracking-wide text-[#6b7280]">{eventDate.day}</p>
                    <p className="mt-0.5 text-sm font-semibold text-[#111111]">{eventDate.label}</p>
                  </div>
                  <Calendar className="h-4 w-4 shrink-0 text-muted-foreground" />
                </div>

                <div className="flex flex-wrap gap-2">
                  {resolvedEvent.grades.map((grade, index) => (
                    <div
                      key={`${grade.name}-${grade.price}-${index}`}
                      className="flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium"
                      style={{
                        backgroundColor: `${grade.color ?? '#9aa4b2'}18`,
                        color: grade.color ?? '#6b7280',
                      }}
                    >
                      <span className="font-bold">{grade.name}</span>
                      <span>{grade.price > 0 ? `${grade.price.toLocaleString()} CTK` : '가격 정보 확인'}</span>
                    </div>
                  ))}
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
