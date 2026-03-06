'use client'

import { useMemo, useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { MOCK_EVENTS } from '@/lib/mock-data'
import { EventDate } from '@/lib/types'
import { cn } from '@/lib/utils'
import { Calendar, ChevronLeft, ChevronRight } from 'lucide-react'

const KR_WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'] as const

function parseDateFromLabel(label: string) {
  const match = label.match(/(\d{4})\.(\d{2})\.(\d{2})/)
  if (!match) return null
  return { year: Number(match[1]), month: Number(match[2]), day: Number(match[3]) }
}

export function EventDateSelectionScreen() {
  const { navParams, navigate, goBack } = useApp()
  const event = MOCK_EVENTS.find((e) => e.id === navParams.eventId)

  const [calendarDayShows, setCalendarDayShows] = useState<EventDate[]>([])

  if (!event) return null

  const eventDates = useMemo<EventDate[]>(() => {
    if (event.dates && event.dates.length > 0) return event.dates
    return [{ id: `${event.id}_d1`, label: event.date, day: 'DAY 1' }]
  }, [event])

  const firstParsed = parseDateFromLabel(eventDates[0].label) ?? { year: 2026, month: 1, day: 1 }
  const calYear = firstParsed.year
  const calMonth = firstParsed.month
  const firstDayOfMonth = new Date(calYear, calMonth - 1, 1).getDay()
  const daysInMonth = new Date(calYear, calMonth, 0).getDate()
  const cells: (number | null)[] = [
    ...Array(firstDayOfMonth).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ]
  while (cells.length % 7 !== 0) cells.push(null)

  const availableMap = new Map<string, EventDate[]>()
  eventDates.forEach((d) => {
    const parsed = parseDateFromLabel(d.label)
    if (!parsed) return
    const key = `${parsed.year}-${String(parsed.month).padStart(2, '0')}-${String(parsed.day).padStart(2, '0')}`
    if (!availableMap.has(key)) availableMap.set(key, [])
    availableMap.get(key)!.push(d)
  })

  return (
    <AppShell showBack onBack={goBack} title="회차 선택" showBottomNav={false}>
      <div className="flex flex-col gap-4 p-4">
        <p className="text-xs text-muted-foreground">
          날짜/회차를 먼저 선택하면 대기열에 입장합니다.
        </p>

        <div className="bg-card border border-border rounded-2xl overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-border">
            <ChevronLeft className="w-5 h-5 text-muted-foreground/30" />
            <span className="text-sm font-bold text-foreground">{calYear}.{String(calMonth).padStart(2, '0')}</span>
            <ChevronRight className="w-5 h-5 text-muted-foreground/30" />
          </div>

          <div className="grid grid-cols-7 px-3 pt-3 pb-1">
            {KR_WEEKDAYS.map((wd, i) => (
              <div
                key={wd}
                className={cn(
                  'text-center text-xs font-semibold py-1',
                  i === 0 ? 'text-red-400' : i === 6 ? 'text-blue-400' : 'text-muted-foreground'
                )}
              >
                {wd}
              </div>
            ))}
          </div>

          <div className="grid grid-cols-7 px-3 pb-4 gap-y-1">
            {cells.map((day, idx) => {
              if (day === null) return <div key={`e-${idx}`} />

              const key = `${calYear}-${String(calMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`
              const datesOnDay = availableMap.get(key) ?? []
              const hasShow = datesOnDay.length > 0
              const dow = (firstDayOfMonth + day - 1) % 7

              return (
                <button
                  key={day}
                  disabled={!hasShow}
                  onClick={() => setCalendarDayShows(datesOnDay)}
                  className={cn(
                    'relative flex flex-col items-center justify-center aspect-square rounded-xl transition-all',
                    hasShow ? 'hover:scale-105 active:scale-95' : 'cursor-default'
                  )}
                >
                  <span
                    className={cn(
                      'w-8 h-8 flex items-center justify-center rounded-full text-sm font-semibold transition-colors',
                      hasShow
                        ? 'bg-primary text-primary-foreground shadow-md shadow-primary/30'
                        : dow === 0
                        ? 'text-red-400/30'
                        : dow === 6
                        ? 'text-blue-400/30'
                        : 'text-muted-foreground/30'
                    )}
                  >
                    {day}
                  </span>
                  {hasShow && <span className="absolute bottom-0.5 w-1 h-1 rounded-full bg-primary-foreground/70" />}
                </button>
              )
            })}
          </div>
        </div>

        {calendarDayShows.length > 0 && (
          <div className="flex flex-col gap-3">
            <p className="text-xs font-semibold text-foreground px-1">선택 가능한 회차</p>
            {calendarDayShows.map((d) => (
              <button
                key={d.id}
                onClick={() => navigate('waiting-queue', { eventId: event.id, eventDateId: d.id })}
                className="flex flex-col p-4 rounded-2xl border border-border bg-card hover:border-primary hover:bg-primary/5 transition-all active:scale-[0.98] text-left gap-3"
              >
                <div className="flex items-center justify-between w-full">
                  <div>
                    <p className="text-xs font-bold text-primary uppercase tracking-wide">{d.day}</p>
                    <p className="text-sm font-semibold text-foreground mt-0.5">{d.label}</p>
                  </div>
                  <Calendar className="w-4 h-4 text-muted-foreground shrink-0" />
                </div>

                <div className="flex flex-wrap gap-2">
                  {event.grades.map((g) => (
                    <div
                      key={g.name}
                      className="flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium"
                      style={{ backgroundColor: `${g.color}22`, color: g.color, border: `1px solid ${g.color}55` }}
                    >
                      <span className="font-bold">{g.name}</span>
                      {g.remaining === 0 ? (
                        <span className="text-red-400 font-semibold">매진</span>
                      ) : (
                        <span>{g.remaining.toLocaleString()}석</span>
                      )}
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
