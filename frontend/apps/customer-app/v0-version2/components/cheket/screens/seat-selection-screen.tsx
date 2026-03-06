'use client'

import { useState, useMemo, useRef } from 'react'
import { useApp } from '@/lib/app-context'
import { MOCK_EVENTS, generateSeats } from '@/lib/mock-data'
import { Seat, EventDate } from '@/lib/types'
import { AppShell } from '../app-shell'
import { cn } from '@/lib/utils'
import { AlertCircle, Calendar, CheckCircle2, ChevronLeft, ChevronRight, ZoomIn, ZoomOut, RotateCcw } from 'lucide-react'

function parseEventDate(label: string): { year: number; month: number; day: number } {
  const parts = label.split(' ')[0].split('.')
  return { year: Number(parts[0]), month: Number(parts[1]), day: Number(parts[2]) }
}

const KR_WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'] as const

type Step = 'date' | 'seats'

export function SeatSelectionScreen() {
  const { navParams, navigate, goBack } = useApp()
  const event = MOCK_EVENTS.find((e) => e.id === navParams.eventId)

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

  const seats = useMemo(() => generateSeats(navParams.eventId ?? ''), [navParams.eventId])

  if (!event) return null

  const maxSeats = event.maxPerUser

  // Grade color map from event.grades
  const gradeColorMap = useMemo(() => {
    const map: Record<string, string> = {}
    event.grades.forEach((g) => { map[g.name] = g.color ?? '#6366f1' })
    return map
  }, [event.grades])

  // Group seats by grade for section rendering
  const seatsByGrade = useMemo(() => {
    const map = new Map<string, Seat[]>()
    seats.forEach((s) => {
      if (!map.has(s.grade)) map.set(s.grade, [])
      map.get(s.grade)!.push(s)
    })
    return map
  }, [seats])

  // Available count per grade
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
    { key: 'seats' as Step, label: '좌석' },
  ]
  const currentStepIndex = steps.findIndex((s) => s.key === step)

  const handleBack = () => {
    if (showTimePicker) { setShowTimePicker(false); setCalendarDayShows([]); return }
    if (step === 'seats') {
      setSelectedSeats([])
      if (needsDateStep) { setSelectedDate(null); setStep('date') }
      else goBack()
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

  // ---- Date step ----
  const renderDateStep = () => {
    if (!event.dates || event.dates.length === 0) return null

    const availableMap = new Map<string, EventDate[]>()
    for (const d of event.dates) {
      const { year, month, day } = parseEventDate(d.label)
      const key = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      if (!availableMap.has(key)) availableMap.set(key, [])
      availableMap.get(key)!.push(d)
    }

    const firstParsed = parseEventDate(event.dates[0].label)
    const calYear = firstParsed.year
    const calMonth = firstParsed.month
    const firstDayOfMonth = new Date(calYear, calMonth - 1, 1).getDay()
    const daysInMonth = new Date(calYear, calMonth, 0).getDate()
    const cells: (number | null)[] = [
      ...Array(firstDayOfMonth).fill(null),
      ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
    ]
    while (cells.length % 7 !== 0) cells.push(null)

    return (
      <div className="flex flex-col gap-4 p-4">
        <p className="text-xs text-muted-foreground">공연 날짜를 선택해주세요</p>

        {/* Calendar */}
        <div className="bg-card border border-border rounded-2xl overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-border">
            <ChevronLeft className="w-5 h-5 text-muted-foreground/30" />
            <span className="text-sm font-bold text-foreground">{calYear}년 {calMonth}월</span>
            <ChevronRight className="w-5 h-5 text-muted-foreground/30" />
          </div>
          <div className="grid grid-cols-7 px-3 pt-3 pb-1">
            {KR_WEEKDAYS.map((wd, i) => (
              <div key={wd} className={cn('text-center text-xs font-semibold py-1', i === 0 ? 'text-red-400' : i === 6 ? 'text-blue-400' : 'text-muted-foreground')}>
                {wd}
              </div>
            ))}
          </div>
          <div className="grid grid-cols-7 px-3 pb-4 gap-y-1">
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
                  className={cn('relative flex flex-col items-center justify-center aspect-square rounded-xl transition-all', hasShow ? 'hover:scale-105 active:scale-95' : 'cursor-default')}
                >
                  <span className={cn('w-8 h-8 flex items-center justify-center rounded-full text-sm font-semibold transition-colors',
                    hasShow ? 'bg-primary text-primary-foreground shadow-md shadow-primary/30'
                      : dow === 0 ? 'text-red-400/30' : dow === 6 ? 'text-blue-400/30' : 'text-muted-foreground/30'
                  )}>{day}</span>
                  {hasShow && <span className="absolute bottom-0.5 w-1 h-1 rounded-full bg-primary-foreground/70" />}
                </button>
              )
            })}
          </div>
        </div>

        {/* Legend */}
        <div className="flex items-center gap-3 text-xs text-muted-foreground px-1">
          <div className="flex items-center gap-1.5"><div className="w-5 h-5 rounded-full bg-primary" /><span>공연 있음</span></div>
          <div className="flex items-center gap-1.5"><div className="w-5 h-5 rounded-full bg-muted" /><span>공연 없음</span></div>
        </div>

        {/* Time picker with grade remaining */}
        {showTimePicker && calendarDayShows.length > 0 && (
          <div className="flex flex-col gap-3 mt-1">
            <p className="text-xs font-semibold text-foreground px-1">회차를 선택해주세요</p>
            {calendarDayShows.map((d) => (
              <button
                key={d.id}
                onClick={() => {
                  setSelectedDate(d)
                  setShowTimePicker(false)
                  setCalendarDayShows([])
                  setStep('seats')
                }}
                className="flex flex-col p-4 rounded-2xl border border-border bg-card hover:border-primary hover:bg-primary/5 transition-all active:scale-[0.98] text-left gap-3"
              >
                {/* Header row */}
                <div className="flex items-center justify-between w-full">
                  <div>
                    <p className="text-xs font-bold text-primary uppercase tracking-wide">{d.day}</p>
                    <p className="text-sm font-semibold text-foreground mt-0.5">{d.label}</p>
                  </div>
                  <ChevronRight className="w-4 h-4 text-muted-foreground shrink-0" />
                </div>
                {/* Grade remaining badges */}
                <div className="flex flex-wrap gap-2">
                  {event.grades.map((g) => (
                    <div
                      key={g.name}
                      className="flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium"
                      style={{ backgroundColor: `${g.color}22`, color: g.color, border: `1px solid ${g.color}55` }}
                    >
                      <span className="font-bold">{g.name}</span>
                      {g.remaining === 0
                        ? <span className="text-red-400 font-semibold">매진</span>
                        : <span>{g.remaining.toLocaleString()}석</span>
                      }
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

  // ---- Seats step (full venue map) ----
  const renderSeatsStep = () => (
    <div className="flex flex-col h-full">
      <div className="flex-1 overflow-hidden flex flex-col">

        {/* Context strip */}
        <div className="flex items-center gap-2 px-4 pt-3 pb-2 flex-wrap">
          {selectedDate && (
            <span className="flex items-center gap-1 text-xs bg-primary/10 text-primary px-2.5 py-1 rounded-full font-medium">
              <Calendar className="w-3 h-3" />
              {selectedDate.day} · {selectedDate.label.split(' ').slice(-1)[0]}
            </span>
          )}
          <span className="text-xs text-muted-foreground ml-auto">{maxSeats}매 이하 선택</span>
        </div>

        {/* Zoom controls */}
        <div className="flex items-center justify-end gap-2 px-4 pb-2">
          <button onClick={() => setZoom((z) => Math.max(0.6, z - 0.2))} className="w-7 h-7 rounded-full bg-secondary border border-border flex items-center justify-center hover:border-primary transition-colors">
            <ZoomOut className="w-3.5 h-3.5 text-muted-foreground" />
          </button>
          <button onClick={() => setZoom(1)} className="w-7 h-7 rounded-full bg-secondary border border-border flex items-center justify-center hover:border-primary transition-colors">
            <RotateCcw className="w-3.5 h-3.5 text-muted-foreground" />
          </button>
          <button onClick={() => setZoom((z) => Math.min(2, z + 0.2))} className="w-7 h-7 rounded-full bg-secondary border border-border flex items-center justify-center hover:border-primary transition-colors">
            <ZoomIn className="w-3.5 h-3.5 text-muted-foreground" />
          </button>
        </div>

        {/* Venue map */}
        <div className="flex-1 overflow-auto bg-muted/30 relative">
          <div
            ref={mapRef}
            className="min-w-max mx-auto p-4 transition-transform origin-top"
            style={{ transform: `scale(${zoom})`, transformOrigin: 'top center' }}
          >
            {/* Stage */}
            <div className="text-center mb-6">
              <div className="inline-block bg-foreground/10 border border-foreground/20 rounded-xl px-16 py-2.5 text-xs text-muted-foreground font-bold tracking-widest uppercase">
                STAGE
              </div>
            </div>

            {/* All grade sections side by side */}
            <div className="flex gap-6 justify-center flex-wrap">
              {event.grades.map((grade) => {
                const gradeSeats = seatsByGrade.get(grade.name) ?? []
                const rows = Array.from(new Set(gradeSeats.map((s) => s.row)))
                const color = grade.color ?? '#6366f1'

                return (
                  <div key={grade.name} className="flex flex-col items-center gap-1">
                    {/* Section label */}
                    <span
                      className="text-[10px] font-bold px-3 py-1 rounded-full mb-1"
                      style={{ backgroundColor: `${color}22`, color, border: `1px solid ${color}55` }}
                    >
                      {grade.name}
                    </span>

                    {/* Seat grid per row */}
                    {rows.map((row) => {
                      const rowSeats = gradeSeats.filter((s) => s.row === row)
                      return (
                        <div key={row} className="flex items-center gap-1">
                          {rowSeats.map((seat) => {
                            const isSelected = selectedSeats.some((s) => s.id === seat.id)
                            const isAvailable = seat.status === 'AVAILABLE'

                            let bgColor = '#e5e7eb' // SOLD / LOCKED default
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
                                title={`${seat.grade} · ${row.split('-')[1]}열 ${seat.number}번 · ${seat.price.toLocaleString()}CTK`}
                                aria-label={`${seat.grade} ${seat.number}번`}
                                style={{ backgroundColor: bgColor, borderColor: border, borderWidth: 1 }}
                                className={cn(
                                  'w-6 h-6 rounded-sm transition-all text-[9px] font-bold',
                                  isSelected ? 'text-white scale-110 shadow-md' : isAvailable ? 'hover:scale-110 cursor-pointer' : 'opacity-40 cursor-not-allowed'
                                )}
                              >
                                {isSelected ? '✓' : ''}
                              </button>
                            )
                          })}
                        </div>
                      )
                    })}

                    {/* Available count */}
                    <span className="text-[10px] text-muted-foreground mt-1">
                      {grade.remaining === 0
                        ? <span className="text-red-400 font-semibold">매진</span>
                        : `잔여 ${availableByGrade[grade.name] ?? 0}석`}
                    </span>
                  </div>
                )
              })}
            </div>
          </div>
        </div>

        {/* Color legend */}
        <div className="px-4 py-2 border-t border-border bg-surface">
          <div className="flex items-center gap-3 flex-wrap">
            {event.grades.map((g) => (
              <div key={g.name} className="flex items-center gap-1.5 text-xs">
                <div className="w-3 h-3 rounded-sm" style={{ backgroundColor: g.color ?? '#6366f1' }} />
                <span className="text-muted-foreground font-medium">{g.name}</span>
                <span className="text-muted-foreground/60">{g.price.toLocaleString()}CTK</span>
              </div>
            ))}
            <div className="flex items-center gap-1.5 text-xs ml-auto">
              <div className="w-3 h-3 rounded-sm bg-muted border border-border" />
              <span className="text-muted-foreground">판매됨</span>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom panel */}
      <div className="p-4 border-t border-border bg-surface flex flex-col gap-3">
        {selectedSeats.length >= maxSeats && (
          <div className="flex items-center gap-2 p-3 bg-orange-500/10 border border-orange-500/30 rounded-xl">
            <AlertCircle className="w-4 h-4 text-orange-400 shrink-0" />
            <p className="text-xs text-orange-400">최대 {maxSeats}매까지 선택할 수 있습니다.</p>
          </div>
        )}
        {selectedSeats.length > 0 && (
          <div className="flex flex-col gap-1">
            {selectedSeats.map((s) => (
              <div key={s.id} className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-1.5">
                  <div className="w-2.5 h-2.5 rounded-sm" style={{ backgroundColor: gradeColorMap[s.grade] }} />
                  <span className="text-muted-foreground">{s.grade} · {s.row.split('-')[1]}열 {s.number}번</span>
                </div>
                <span className="text-foreground font-medium">{s.price.toLocaleString()} CTK</span>
              </div>
            ))}
            <div className="flex items-center justify-between text-sm font-semibold pt-1 border-t border-border mt-1">
              <span className="text-foreground">총 금액</span>
              <span className="text-primary">{totalPrice.toLocaleString()} CTK</span>
            </div>
          </div>
        )}
        <button
          onClick={() => navigate('payment', { eventId: event.id, seats: selectedSeats, totalPrice })}
          disabled={selectedSeats.length === 0}
          className="w-full bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed"
        >
          {selectedSeats.length > 0 ? `결제하기 (${selectedSeats.length}매)` : '좌석을 선택해주세요'}
        </button>
      </div>
    </div>
  )

  return (
    <AppShell showBack onBack={handleBack} title="좌석 선택" showBottomNav={false}>
      <div className="flex flex-col h-full">
        {/* Step progress */}
        <div className="px-4 pt-3 pb-2 border-b border-border">
          <div className="flex items-center gap-1">
            {steps.map((s, idx) => (
              <div key={s.key} className="flex items-center gap-1 flex-1">
                <div className={cn('flex items-center gap-1.5 text-xs font-medium transition-colors',
                  idx < currentStepIndex ? 'text-primary' : idx === currentStepIndex ? 'text-foreground' : 'text-muted-foreground/50'
                )}>
                  {idx < currentStepIndex
                    ? <CheckCircle2 className="w-4 h-4 text-primary shrink-0" />
                    : <span className={cn('w-4 h-4 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0',
                        idx === currentStepIndex ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'
                      )}>{idx + 1}</span>
                  }
                  <span>{s.label}</span>
                </div>
                {idx < steps.length - 1 && (
                  <div className={cn('flex-1 h-0.5 rounded-full mx-1', idx < currentStepIndex ? 'bg-primary' : 'bg-border')} />
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto flex flex-col">
          {step === 'date' && renderDateStep()}
          {step === 'seats' && renderSeatsStep()}
        </div>
      </div>
    </AppShell>
  )
}
