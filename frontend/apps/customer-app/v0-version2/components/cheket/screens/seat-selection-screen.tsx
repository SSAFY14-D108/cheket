'use client'

import { useEffect, useMemo, useRef, useState } from 'react'
import {
  AlertCircle,
  Calendar,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  RotateCcw,
  ZoomIn,
  ZoomOut,
} from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { fetchKopisEventDetail } from '@/lib/kopis-client'
import { generateVenueSeats } from '@/lib/mock-data'
import type { Event, EventDate, Seat } from '@/lib/types'
import { cn } from '@/lib/utils'
import { AppShell } from '../app-shell'

function parseEventDate(label: string): { year: number; month: number; day: number } {
  const parts = label.split(' ')[0].split('.')
  return { year: Number(parts[0]), month: Number(parts[1]), day: Number(parts[2]) }
}

const KR_WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'] as const

type Step = 'date' | 'seats'

export function SeatSelectionScreen() {
  const { navParams, navigate, goBack, events, updateEvent } = useApp()
  const baseEvent = events.find((e) => e.id === navParams.eventId)
  const [resolvedEvent, setResolvedEvent] = useState<Event | null>(baseEvent ?? null)
  const [detailLoading, setDetailLoading] = useState(false)
  const fetchedEventIdsRef = useRef<Set<string>>(new Set())

  useEffect(() => {
    let cancelled = false

    async function enrichEvent() {
      if (!baseEvent) {
        setResolvedEvent(null)
        return
      }

      setResolvedEvent(baseEvent)

      const needsDetail =
        baseEvent.id.startsWith('kopis_') &&
        (!baseEvent.priceInfo ||
          !baseEvent.runtime ||
          !baseEvent.dates ||
          baseEvent.dates.length === 0 ||
          baseEvent.grades.every((grade) => grade.price === 0))
      if (!needsDetail) return
      if (fetchedEventIdsRef.current.has(baseEvent.id)) return

      setDetailLoading(true)
      const detail = await fetchKopisEventDetail(baseEvent.id)

      if (!cancelled && detail) {
        fetchedEventIdsRef.current.add(baseEvent.id)
        setResolvedEvent(detail)
        updateEvent(baseEvent.id, detail)
      }

      if (!cancelled) {
        setDetailLoading(false)
      }
    }

    void enrichEvent()
    return () => {
      cancelled = true
    }
  }, [baseEvent?.id, baseEvent?.priceInfo, baseEvent?.grades, updateEvent])

  const event = resolvedEvent

  const hasMultipleDates = Boolean(event?.dates && event.dates.length > 1)
  const presetDate = event?.dates?.find((d) => d.id === navParams.eventDateId) ?? null
  const needsDateStep = hasMultipleDates && !presetDate

  const [step, setStep] = useState<Step>(needsDateStep ? 'date' : 'seats')
  const [selectedDate, setSelectedDate] = useState<EventDate | null>(presetDate)
  const [calendarDayShows, setCalendarDayShows] = useState<EventDate[]>([])
  const [showTimePicker, setShowTimePicker] = useState(false)
  const [selectedSeats, setSelectedSeats] = useState<Seat[]>([])
  const [zoom, setZoom] = useState(1)
  const mapRef = useRef<HTMLDivElement>(null)

  const seats = useMemo(() => {
    if (!event) return []
    return generateVenueSeats({
      id: event.id,
      name: event.name,
      venue: event.venue,
      grades: event.grades,
    })
  }, [event])

  if (!event) return null

  const maxSeats = event.maxPerUser

  const gradeColorMap = useMemo(() => {
    const map: Record<string, string> = {}
    event.grades.forEach((g) => {
      map[g.name] = g.color ?? '#6366f1'
    })
    return map
  }, [event.grades])

  const seatsByGrade = useMemo(() => {
    const map = new Map<string, Seat[]>()
    seats.forEach((s) => {
      if (!map.has(s.grade)) map.set(s.grade, [])
      map.get(s.grade)?.push(s)
    })
    return map
  }, [seats])

  const availableByGrade = useMemo(() => {
    const map: Record<string, number> = {}
    seats.forEach((s) => {
      if (!map[s.grade]) map[s.grade] = 0
      if (s.status === 'AVAILABLE') map[s.grade]++
    })
    return map
  }, [seats])

  const steps: { key: Step; label: string }[] = [
    ...(needsDateStep ? [{ key: 'date' as Step, label: '날짜' }] : []),
    { key: 'seats', label: '좌석' },
  ]
  const currentStepIndex = steps.findIndex((s) => s.key === step)

  const handleBack = () => {
    if (showTimePicker) {
      setShowTimePicker(false)
      setCalendarDayShows([])
      return
    }

    if (step === 'seats') {
      setSelectedSeats([])
      if (needsDateStep) {
        setSelectedDate(null)
        setStep('date')
      } else {
        goBack()
      }
    } else {
      goBack()
    }
  }

  const toggleSeat = (seat: Seat) => {
    if (seat.status !== 'AVAILABLE') return

    const isSelected = selectedSeats.some((s) => s.id === seat.id)
    if (isSelected) {
      setSelectedSeats((prev) => prev.filter((s) => s.id !== seat.id))
    } else {
      if (selectedSeats.length >= maxSeats) return
      setSelectedSeats((prev) => [...prev, seat])
    }
  }

  const totalPrice = selectedSeats.reduce((sum, s) => sum + s.price, 0)

  const renderDateStep = () => {
    if (!event.dates || event.dates.length === 0) return null

    const availableMap = new Map<string, EventDate[]>()
    for (const d of event.dates) {
      const { year, month, day } = parseEventDate(d.label)
      const key = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      if (!availableMap.has(key)) availableMap.set(key, [])
      availableMap.get(key)?.push(d)
    }

    const firstParsed = parseEventDate(event.dates[0].label)
    const calYear = firstParsed.year
    const calMonth = firstParsed.month
    const firstDayOfMonth = new Date(calYear, calMonth - 1, 1).getDay()
    const daysInMonth = new Date(calYear, calMonth, 0).getDate()
    const cells: (number | null)[] = [...Array(firstDayOfMonth).fill(null), ...Array.from({ length: daysInMonth }, (_, i) => i + 1)]
    while (cells.length % 7 !== 0) cells.push(null)

    return (
      <div className="flex flex-col gap-4 p-4">
        <p className="text-xs text-muted-foreground">공연 날짜를 선택해 주세요.</p>

        <div className="overflow-hidden rounded-2xl border border-border bg-card">
          <div className="flex items-center justify-between border-b border-border px-5 py-4">
            <ChevronLeft className="h-5 w-5 text-muted-foreground/30" />
            <span className="text-sm font-bold text-foreground">
              {calYear}.{String(calMonth).padStart(2, '0')}
            </span>
            <ChevronRight className="h-5 w-5 text-muted-foreground/30" />
          </div>

          <div className="grid grid-cols-7 px-3 pb-1 pt-3">
            {KR_WEEKDAYS.map((wd, i) => (
              <div
                key={wd}
                className={cn('py-1 text-center text-xs font-semibold', i === 0 ? 'text-red-400' : i === 6 ? 'text-blue-400' : 'text-muted-foreground')}
              >
                {wd}
              </div>
            ))}
          </div>

          <div className="grid grid-cols-7 gap-y-1 px-3 pb-4">
            {cells.map((day, idx) => {
              if (day === null) return <div key={`e-${idx}`} />
              const key = `${calYear}-${String(calMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`
              const datesOnDay = availableMap.get(key)
              const hasShow = Boolean(datesOnDay?.length)
              const dow = (firstDayOfMonth + day - 1) % 7

              return (
                <button
                  key={day}
                  disabled={!hasShow}
                  onClick={() => {
                    if (!datesOnDay) return
                    setCalendarDayShows(datesOnDay)
                    setShowTimePicker(true)
                  }}
                  className={cn(
                    'relative flex aspect-square flex-col items-center justify-center rounded-xl transition-all',
                    hasShow ? 'hover:scale-105 active:scale-95' : 'cursor-default'
                  )}
                >
                  <span
                    className={cn(
                      'flex h-8 w-8 items-center justify-center rounded-full text-sm font-semibold transition-colors',
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
                  {hasShow && <span className="absolute bottom-0.5 h-1 w-1 rounded-full bg-primary-foreground/70" />}
                </button>
              )
            })}
          </div>
        </div>

        <div className="flex items-center gap-3 px-1 text-xs text-muted-foreground">
          <div className="flex items-center gap-1.5">
            <div className="h-5 w-5 rounded-full bg-primary" />
            <span>공연 있음</span>
          </div>
          <div className="flex items-center gap-1.5">
            <div className="h-5 w-5 rounded-full bg-muted" />
            <span>공연 없음</span>
          </div>
        </div>

        {showTimePicker && calendarDayShows.length > 0 && (
          <div className="mt-1 flex flex-col gap-3">
            <p className="px-1 text-xs font-semibold text-foreground">회차를 선택해 주세요.</p>
            {calendarDayShows.map((d) => (
              <button
                key={d.id}
                onClick={() => {
                  setSelectedDate(d)
                  setShowTimePicker(false)
                  setCalendarDayShows([])
                  setStep('seats')
                }}
                className="flex flex-col gap-3 rounded-2xl border border-border bg-card p-4 text-left transition-all hover:border-primary hover:bg-primary/5 active:scale-[0.98]"
              >
                <div className="flex w-full items-center justify-between">
                  <div>
                    <p className="text-xs font-bold uppercase tracking-wide text-primary">{d.day}</p>
                    <p className="mt-0.5 text-sm font-semibold text-foreground">{d.label}</p>
                  </div>
                  <ChevronRight className="h-4 w-4 shrink-0 text-muted-foreground" />
                </div>

                <div className="flex flex-wrap gap-2">
                  {event.grades.map((g) => (
                    <div
                      key={g.name}
                      className="flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium"
                      style={{
                        backgroundColor: `${g.color ?? '#6b7280'}22`,
                        color: g.color ?? '#6b7280',
                        border: `1px solid ${(g.color ?? '#6b7280')}55`,
                      }}
                    >
                      <span className="font-bold">{g.name}</span>
                      {g.remaining === 0 ? <span className="font-semibold text-red-400">매진</span> : <span>{g.remaining.toLocaleString()}석</span>}
                    </div>
                  ))}
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    )
  }

  const renderSeatsStep = () => (
    <div className="flex h-full flex-col">
      <div className="flex flex-1 flex-col overflow-hidden">
        <div className="flex flex-wrap items-center gap-2 px-4 pb-2 pt-3">
          {selectedDate && (
            <span className="flex items-center gap-1 rounded-full bg-primary/10 px-2.5 py-1 text-xs font-medium text-primary">
              <Calendar className="h-3 w-3" />
              {selectedDate.day} · {selectedDate.label.split(' ').slice(-1)[0]}
            </span>
          )}
          <span className="ml-auto text-xs text-muted-foreground">{maxSeats}매 이하 선택</span>
        </div>

        <div className="flex items-center justify-end gap-2 px-4 pb-2">
          <button
            onClick={() => setZoom((z) => Math.max(0.6, z - 0.2))}
            className="flex h-7 w-7 items-center justify-center rounded-full border border-border bg-secondary transition-colors hover:border-primary"
          >
            <ZoomOut className="h-3.5 w-3.5 text-muted-foreground" />
          </button>
          <button
            onClick={() => setZoom(1)}
            className="flex h-7 w-7 items-center justify-center rounded-full border border-border bg-secondary transition-colors hover:border-primary"
          >
            <RotateCcw className="h-3.5 w-3.5 text-muted-foreground" />
          </button>
          <button
            onClick={() => setZoom((z) => Math.min(2, z + 0.2))}
            className="flex h-7 w-7 items-center justify-center rounded-full border border-border bg-secondary transition-colors hover:border-primary"
          >
            <ZoomIn className="h-3.5 w-3.5 text-muted-foreground" />
          </button>
        </div>

        <div className="relative flex-1 overflow-auto bg-muted/30">
          <div ref={mapRef} className="mx-auto min-w-max origin-top p-4 transition-transform" style={{ transform: `scale(${zoom})`, transformOrigin: 'top center' }}>
            <div className="mb-6 text-center">
              <div className="inline-block rounded-xl border border-foreground/20 bg-foreground/10 px-16 py-2.5 text-xs font-bold uppercase tracking-widest text-muted-foreground">
                STAGE
              </div>
            </div>

            <div className="flex flex-wrap justify-center gap-6">
              {event.grades.map((grade) => {
                const gradeSeats = seatsByGrade.get(grade.name) ?? []
                const rows = Array.from(new Set(gradeSeats.map((s) => s.row)))
                const color = grade.color ?? '#6366f1'

                return (
                  <div key={grade.name} className="flex flex-col items-center gap-1">
                    <span
                      className="mb-1 rounded-full px-3 py-1 text-[10px] font-bold"
                      style={{ backgroundColor: `${color}22`, color, border: `1px solid ${color}55` }}
                    >
                      {grade.name}
                    </span>

                    {rows.map((row) => {
                      const rowSeats = gradeSeats.filter((s) => s.row === row)
                      return (
                        <div key={row} className="flex items-center gap-1">
                          {rowSeats.map((seat) => {
                            const isSelected = selectedSeats.some((s) => s.id === seat.id)
                            const isAvailable = seat.status === 'AVAILABLE'

                            let bgColor = '#e5e7eb'
                            let border = 'transparent'
                            if (isSelected) {
                              bgColor = color
                              border = color
                            } else if (isAvailable) {
                              bgColor = `${color}33`
                              border = `${color}88`
                            }

                            return (
                              <button
                                key={seat.id}
                                onClick={() => toggleSeat(seat)}
                                disabled={!isAvailable && !isSelected}
                                title={`${seat.grade} · ${row.split('-')[1]}열 ${seat.number}번 · ${seat.price.toLocaleString()} CTK`}
                                aria-label={`${seat.grade} ${seat.number}번`}
                                style={{ backgroundColor: bgColor, borderColor: border, borderWidth: 1 }}
                                className={cn(
                                  'h-6 w-6 rounded-sm text-[9px] font-bold transition-all',
                                  isSelected
                                    ? 'scale-110 text-white shadow-md'
                                    : isAvailable
                                      ? 'cursor-pointer hover:scale-110'
                                      : 'cursor-not-allowed opacity-40'
                                )}
                              >
                                {isSelected ? '선' : ''}
                              </button>
                            )
                          })}
                        </div>
                      )
                    })}

                    <span className="mt-1 text-[10px] text-muted-foreground">
                      {grade.remaining === 0 ? <span className="font-semibold text-red-400">매진</span> : `잔여 ${availableByGrade[grade.name] ?? 0}석`}
                    </span>
                  </div>
                )
              })}
            </div>
          </div>
        </div>

        <div className="bg-surface border-t border-border px-4 py-2">
          {detailLoading && (
            <div className="mb-2 rounded-lg border border-border bg-card px-3 py-2 text-xs text-muted-foreground">
              KOPIS 가격 정보를 불러오는 중입니다.
            </div>
          )}
          <div className="flex flex-wrap items-center gap-3">
            {event.grades.map((g) => (
              <div key={g.name} className="flex items-center gap-1.5 text-xs">
                <div className="h-3 w-3 rounded-sm" style={{ backgroundColor: g.color ?? '#6366f1' }} />
                <span className="font-medium text-muted-foreground">{g.name}</span>
                <span className="text-muted-foreground/60">{g.price.toLocaleString()} CTK</span>
              </div>
            ))}
            <div className="ml-auto flex items-center gap-1.5 text-xs">
              <div className="h-3 w-3 rounded-sm border border-border bg-muted" />
              <span className="text-muted-foreground">판매 불가</span>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-surface border-t border-border p-4">
        <div className="flex flex-col gap-3">
          {selectedSeats.length >= maxSeats && (
            <div className="flex items-center gap-2 rounded-xl border border-orange-500/30 bg-orange-500/10 p-3">
              <AlertCircle className="h-4 w-4 shrink-0 text-orange-400" />
              <p className="text-xs text-orange-400">최대 {maxSeats}매까지 선택할 수 있습니다.</p>
            </div>
          )}

          {selectedSeats.length > 0 && (
            <div className="flex flex-col gap-1">
              {selectedSeats.map((s) => (
                <div key={s.id} className="flex items-center justify-between text-xs">
                  <div className="flex items-center gap-1.5">
                    <div className="h-2.5 w-2.5 rounded-sm" style={{ backgroundColor: gradeColorMap[s.grade] }} />
                    <span className="text-muted-foreground">
                      {s.grade} · {s.row.split('-')[1]}열 {s.number}번
                    </span>
                  </div>
                  <span className="font-medium text-foreground">{s.price.toLocaleString()} CTK</span>
                </div>
              ))}
              <div className="mt-1 flex items-center justify-between border-t border-border pt-1 text-sm font-semibold">
                <span className="text-foreground">총 금액</span>
                <span className="text-primary">{totalPrice.toLocaleString()} CTK</span>
              </div>
            </div>
          )}

          <button
            onClick={() => navigate('payment', { eventId: event.id, seats: selectedSeats, totalPrice })}
            disabled={selectedSeats.length === 0 || detailLoading}
            className="w-full rounded-xl bg-primary py-3.5 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-40"
          >
            {selectedSeats.length > 0 ? `결제하기 (${selectedSeats.length}매)` : '좌석을 선택해 주세요'}
          </button>
        </div>
      </div>
    </div>
  )

  return (
    <AppShell showBack onBack={handleBack} title="좌석 선택" showBottomNav={false}>
      <div className="flex h-full flex-col">
        <div className="border-b border-border px-4 pb-2 pt-3">
          <div className="flex items-center gap-1">
            {steps.map((s, idx) => (
              <div key={s.key} className="flex flex-1 items-center gap-1">
                <div
                  className={cn(
                    'flex items-center gap-1.5 text-xs font-medium transition-colors',
                    idx < currentStepIndex ? 'text-primary' : idx === currentStepIndex ? 'text-foreground' : 'text-muted-foreground/50'
                  )}
                >
                  {idx < currentStepIndex ? (
                    <CheckCircle2 className="h-4 w-4 shrink-0 text-primary" />
                  ) : (
                    <span
                      className={cn(
                        'flex h-4 w-4 shrink-0 items-center justify-center rounded-full text-[10px] font-bold',
                        idx === currentStepIndex ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'
                      )}
                    >
                      {idx + 1}
                    </span>
                  )}
                  <span>{s.label}</span>
                </div>
                {idx < steps.length - 1 && <div className={cn('mx-1 h-0.5 flex-1 rounded-full', idx < currentStepIndex ? 'bg-primary' : 'bg-border')} />}
              </div>
            ))}
          </div>
        </div>

        <div className="flex flex-1 flex-col overflow-y-auto">
          {step === 'date' && renderDateStep()}
          {step === 'seats' && renderSeatsStep()}
        </div>
      </div>
    </AppShell>
  )
}
