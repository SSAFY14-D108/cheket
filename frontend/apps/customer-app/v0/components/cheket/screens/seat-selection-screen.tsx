'use client'

import { useState, useMemo } from 'react'
import { useApp } from '@/lib/app-context'
import { MOCK_EVENTS, generateSeats } from '@/lib/mock-data'
import { Seat, Grade, EventDate } from '@/lib/types'
import { AppShell } from '../app-shell'
import { cn } from '@/lib/utils'
import { AlertCircle, Calendar, CheckCircle2, ChevronLeft, ChevronRight } from 'lucide-react'

// Parse "2026.04.12 (토) 18:00" → { year, month, day }
function parseEventDate(label: string): { year: number; month: number; day: number } {
  const parts = label.split(' ')[0].split('.')
  return { year: Number(parts[0]), month: Number(parts[1]), day: Number(parts[2]) }
}

const KR_WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'] as const

type Step = 'date' | 'grade' | 'seats'

const STATUS_COLORS: Record<Seat['status'], string> = {
  AVAILABLE: 'bg-primary/20 border border-primary/50 text-primary hover:bg-primary/30',
  LOCKED: 'bg-yellow-500/20 border border-yellow-500/40 text-yellow-400 cursor-not-allowed',
  SOLD: 'bg-secondary border border-border text-muted-foreground/30 cursor-not-allowed',
}

const SELECTED_COLOR = 'bg-primary border-primary text-primary-foreground'

export function SeatSelectionScreen() {
  const { navParams, navigate, goBack } = useApp()
  const event = MOCK_EVENTS.find((e) => e.id === navParams.eventId)

  const hasMultipleDates = Boolean(event?.dates && event.dates.length > 1)

  // 3-step state
  const [step, setStep] = useState<Step>(hasMultipleDates ? 'date' : 'grade')
  const [selectedDate, setSelectedDate] = useState<EventDate | null>(null)
  const [calendarDayShows, setCalendarDayShows] = useState<EventDate[]>([])
  const [showTimePicker, setShowTimePicker] = useState(false)
  const [selectedGrade, setSelectedGrade] = useState<Grade | null>(null)
  const [selectedSeats, setSelectedSeats] = useState<Seat[]>([])

  const seats = useMemo(
    () => generateSeats(navParams.eventId ?? '', selectedGrade?.name),
    [navParams.eventId, selectedGrade]
  )

  if (!event) return null

  const maxSeats = event.maxPerUser
  const rows = Array.from(new Set(seats.map((s) => s.row)))

  // ---- Step breadcrumb ----
  const steps: { key: Step; label: string }[] = [
    ...(hasMultipleDates ? [{ key: 'date' as Step, label: '날짜' }] : []),
    { key: 'grade', label: '구역' },
    { key: 'seats', label: '좌석' },
  ]
  const currentStepIndex = steps.findIndex((s) => s.key === step)

  const handleBack = () => {
    if (showTimePicker) {
      setShowTimePicker(false)
      setCalendarDayShows([])
      return
    }
    if (step === 'grade' && hasMultipleDates) {
      setSelectedGrade(null)
      setSelectedDate(null)
      setStep('date')
    } else if (step === 'seats') {
      setSelectedSeats([])
      setStep('grade')
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

  // ---- Calendar date step ----
  const renderDateStep = () => {
    if (!event.dates || event.dates.length === 0) return null

    // Build a map of available dates: "YYYY-MM-DD" -> EventDate[]
    const availableMap = new Map<string, EventDate[]>()
    for (const d of event.dates) {
      const { year, month, day } = parseEventDate(d.label)
      const key = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      if (!availableMap.has(key)) availableMap.set(key, [])
      availableMap.get(key)!.push(d)
    }

    // Derive calendar month from first date
    const firstParsed = parseEventDate(event.dates[0].label)
    const calYear = firstParsed.year
    const calMonth = firstParsed.month

    const firstDayOfMonth = new Date(calYear, calMonth - 1, 1).getDay()
    const daysInMonth = new Date(calYear, calMonth, 0).getDate()

    // Build grid cells (empty + day numbers)
    const cells: (number | null)[] = [
      ...Array(firstDayOfMonth).fill(null),
      ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
    ]
    // Pad to complete last row
    while (cells.length % 7 !== 0) cells.push(null)

    const monthLabel = `${calYear}년 ${calMonth}월`

    return (
      <div className="flex flex-col gap-4 p-4">
        <p className="text-xs text-muted-foreground">공연 날짜를 선택해주세요</p>

        {/* Calendar card */}
        <div className="bg-card border border-border rounded-2xl overflow-hidden">
          {/* Month header */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-border">
            <ChevronLeft className="w-5 h-5 text-muted-foreground/30" />
            <span className="text-sm font-bold text-foreground">{monthLabel}</span>
            <ChevronRight className="w-5 h-5 text-muted-foreground/30" />
          </div>

          {/* Weekday headers */}
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

          {/* Day cells */}
          <div className="grid grid-cols-7 px-3 pb-4 gap-y-1">
            {cells.map((day, idx) => {
              if (day === null) return <div key={`empty-${idx}`} />

              const key = `${calYear}-${String(calMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`
              const datesOnDay = availableMap.get(key)
              const hasShow = Boolean(datesOnDay && datesOnDay.length > 0)
              const dayOfWeek = (firstDayOfMonth + day - 1) % 7

              return (
                <button
                  key={day}
                  disabled={!hasShow}
                  onClick={() => {
                    if (!datesOnDay) return
                    if (datesOnDay.length === 1) {
                      setSelectedDate(datesOnDay[0])
                      setStep('grade')
                    } else {
                      // Multiple shows on same day — show time picker below
                      setSelectedDate(null)
                      setCalendarDayShows(datesOnDay)
                      setShowTimePicker(true)
                    }
                  }}
                  className={cn(
                    'relative flex flex-col items-center justify-center aspect-square rounded-xl transition-all',
                    hasShow
                      ? 'hover:scale-105 active:scale-95'
                      : 'cursor-default'
                  )}
                >
                  <span
                    className={cn(
                      'w-8 h-8 flex items-center justify-center rounded-full text-sm font-semibold transition-colors',
                      hasShow
                        ? 'bg-primary text-primary-foreground shadow-md shadow-primary/30'
                        : dayOfWeek === 0
                        ? 'text-red-400/30'
                        : dayOfWeek === 6
                        ? 'text-blue-400/30'
                        : 'text-muted-foreground/30'
                    )}
                  >
                    {day}
                  </span>
                  {/* Dot indicator for shows */}
                  {hasShow && (
                    <span className="absolute bottom-0.5 w-1 h-1 rounded-full bg-primary-foreground/70" />
                  )}
                </button>
              )
            })}
          </div>
        </div>

        {/* Legend */}
        <div className="flex items-center gap-3 text-xs text-muted-foreground px-1">
          <div className="flex items-center gap-1.5">
            <div className="w-5 h-5 rounded-full bg-primary" />
            <span>공연 있음</span>
          </div>
          <div className="flex items-center gap-1.5">
            <div className="w-5 h-5 rounded-full bg-muted" />
            <span>공연 없음</span>
          </div>
        </div>

        {/* Time picker sheet (same-day multiple shows) */}
        {showTimePicker && calendarDayShows.length > 0 && (
          <div className="flex flex-col gap-2 mt-1">
            <p className="text-xs font-semibold text-foreground px-1">회차를 선택해주세요</p>
            {calendarDayShows.map((d) => (
              <button
                key={d.id}
                onClick={() => {
                  setSelectedDate(d)
                  setShowTimePicker(false)
                  setCalendarDayShows([])
                  setStep('grade')
                }}
                className="flex items-center justify-between p-4 rounded-2xl border border-border bg-card hover:border-primary hover:bg-primary/5 transition-all active:scale-[0.98]"
              >
                <div className="text-left">
                  <p className="text-xs font-bold text-primary uppercase tracking-wide">{d.day}</p>
                  <p className="text-sm font-semibold text-foreground mt-0.5">{d.label}</p>
                </div>
                <ChevronRight className="w-4 h-4 text-muted-foreground" />
              </button>
            ))}
          </div>
        )}
      </div>
    )
  }

  // ---- Grade step ----
  const renderGradeStep = () => (
    <div className="flex flex-col gap-3 p-4">
      {selectedDate && (
        <div className="flex items-center gap-2 mb-1 px-1">
          <Calendar className="w-3.5 h-3.5 text-muted-foreground" />
          <span className="text-xs text-muted-foreground">{selectedDate.label}</span>
        </div>
      )}
      <p className="text-xs text-muted-foreground mb-1">좌석 구역을 선택해주세요</p>
      {event.grades.map((g) => {
        const isSoldOut = g.remaining === 0
        return (
          <button
            key={g.name}
            onClick={() => {
              if (isSoldOut) return
              setSelectedGrade(g)
              setStep('seats')
            }}
            disabled={isSoldOut}
            className={cn(
              'flex items-center justify-between p-4 rounded-2xl border transition-all active:scale-[0.98]',
              isSoldOut
                ? 'border-border bg-secondary opacity-50 cursor-not-allowed'
                : 'border-border bg-card hover:border-primary hover:bg-primary/5'
            )}
          >
            <div className="flex items-center gap-3">
              <div
                className="w-10 h-10 rounded-xl flex items-center justify-center text-white text-xs font-bold shrink-0"
                style={{ backgroundColor: g.color ?? '#6366f1' }}
              >
                {g.name.replace(/[^A-Za-z가-힣]/g, '').slice(0, 2)}
              </div>
              <div className="text-left">
                <p className="text-sm font-bold text-foreground">{g.name}</p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {g.price.toLocaleString()} CTK
                  {isSoldOut ? (
                    <span className="ml-2 text-red-400 font-semibold">· 매진</span>
                  ) : (
                    <span className="ml-2 text-green-400">· 잔여 {g.remaining}석</span>
                  )}
                </p>
              </div>
            </div>
            {!isSoldOut && <ChevronRight className="w-4 h-4 text-muted-foreground" />}
          </button>
        )
      })}
    </div>
  )

  // ---- Seats step ----
  const renderSeatsStep = () => (
    <div className="flex flex-col h-full">
      <div className="flex-1 overflow-y-auto p-4">
        {/* Selected context chips */}
        <div className="flex items-center gap-2 flex-wrap mb-4">
          {selectedDate && (
            <span className="flex items-center gap-1 text-xs bg-primary/10 text-primary px-2.5 py-1 rounded-full font-medium">
              <Calendar className="w-3 h-3" />
              {selectedDate.day} · {selectedDate.label.split(' ').slice(-1)[0]}
            </span>
          )}
          {selectedGrade && (
            <span
              className="text-xs px-2.5 py-1 rounded-full font-bold text-white"
              style={{ backgroundColor: selectedGrade.color ?? '#6366f1' }}
            >
              {selectedGrade.name}
            </span>
          )}
        </div>

        {/* Legend */}
        <div className="flex items-center gap-4 mb-4 text-xs text-muted-foreground flex-wrap">
          <div className="flex items-center gap-1.5">
            <div className="w-4 h-4 rounded bg-primary/20 border border-primary/50" />
            <span>선택가능</span>
          </div>
          <div className="flex items-center gap-1.5">
            <div className="w-4 h-4 rounded bg-primary" />
            <span>내 선택</span>
          </div>
          <div className="flex items-center gap-1.5">
            <div className="w-4 h-4 rounded bg-yellow-500/20 border border-yellow-500/40" />
            <span>선택중</span>
          </div>
          <div className="flex items-center gap-1.5">
            <div className="w-4 h-4 rounded bg-secondary border border-border" />
            <span>판매됨</span>
          </div>
        </div>

        {/* Stage */}
        <div className="text-center mb-5">
          <div className="inline-block bg-secondary border border-border rounded-lg px-8 py-2 text-xs text-muted-foreground font-medium tracking-widest">
            STAGE
          </div>
        </div>

        {/* Seat grid */}
        <div className="overflow-x-auto pb-2">
          <div className="flex flex-col gap-1.5 min-w-max mx-auto">
            {rows.map((row) => (
              <div key={row} className="flex items-center gap-1.5">
                <span className="w-5 text-xs text-muted-foreground font-medium text-right shrink-0">{row}</span>
                {seats
                  .filter((s) => s.row === row)
                  .map((seat) => {
                    const isSelected = selectedSeats.some((s) => s.id === seat.id)
                    return (
                      <button
                        key={seat.id}
                        onClick={() => toggleSeat(seat)}
                        disabled={seat.status !== 'AVAILABLE' && !isSelected}
                        className={cn(
                          'w-8 h-8 rounded-lg text-[10px] font-semibold transition-all',
                          isSelected ? SELECTED_COLOR : STATUS_COLORS[seat.status]
                        )}
                        title={`${row}열 ${seat.number}번 (${seat.grade}) - ${seat.price.toLocaleString()}CTK`}
                        aria-label={`${row}열 ${seat.number}번`}
                      >
                        {seat.number}
                      </button>
                    )
                  })}
              </div>
            ))}
          </div>
        </div>

        {selectedSeats.length >= maxSeats && (
          <div className="flex items-center gap-2 mt-4 p-3 bg-orange-500/10 border border-orange-500/30 rounded-xl">
            <AlertCircle className="w-4 h-4 text-orange-400 shrink-0" />
            <p className="text-xs text-orange-400">최대 구매 가능 매수({maxSeats}매)를 초과할 수 없습니다.</p>
          </div>
        )}
      </div>

      {/* Bottom panel */}
      <div className="p-4 border-t border-border bg-surface flex flex-col gap-3">
        {selectedSeats.length > 0 && (
          <div className="flex flex-col gap-1.5">
            {selectedSeats.map((s) => (
              <div key={s.id} className="flex items-center justify-between text-xs">
                <span className="text-muted-foreground">{s.row}열 {s.number}번 ({s.grade})</span>
                <span className="text-foreground font-medium">{s.price.toLocaleString()} CTK</span>
              </div>
            ))}
            <div className="flex items-center justify-between text-sm font-semibold pt-1 border-t border-border">
              <span className="text-foreground">총 금액</span>
              <span className="text-primary">{totalPrice.toLocaleString()} CTK</span>
            </div>
            <p className="text-xs text-muted-foreground">
              잔여 구매 가능 매수: <span className="text-foreground">{maxSeats - selectedSeats.length}매</span>
            </p>
          </div>
        )}
        <button
          onClick={() =>
            navigate('payment', {
              eventId: event.id,
              seats: selectedSeats,
              totalPrice,
            })
          }
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
        {/* Step progress bar */}
        <div className="px-4 pt-3 pb-2 border-b border-border">
          <div className="flex items-center gap-1">
            {steps.map((s, idx) => (
              <div key={s.key} className="flex items-center gap-1 flex-1">
                <div
                  className={cn(
                    'flex items-center gap-1.5 text-xs font-medium transition-colors',
                    idx < currentStepIndex
                      ? 'text-primary'
                      : idx === currentStepIndex
                      ? 'text-foreground'
                      : 'text-muted-foreground/50'
                  )}
                >
                  {idx < currentStepIndex ? (
                    <CheckCircle2 className="w-4 h-4 text-primary shrink-0" />
                  ) : (
                    <span
                      className={cn(
                        'w-4 h-4 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0',
                        idx === currentStepIndex
                          ? 'bg-primary text-primary-foreground'
                          : 'bg-muted text-muted-foreground'
                      )}
                    >
                      {idx + 1}
                    </span>
                  )}
                  <span>{s.label}</span>
                </div>
                {idx < steps.length - 1 && (
                  <div
                    className={cn(
                      'flex-1 h-0.5 rounded-full mx-1',
                      idx < currentStepIndex ? 'bg-primary' : 'bg-border'
                    )}
                  />
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Step content */}
        <div className="flex-1 overflow-y-auto">
          {step === 'date' && renderDateStep()}
          {step === 'grade' && renderGradeStep()}
          {step === 'seats' && renderSeatsStep()}
        </div>
      </div>
    </AppShell>
  )
}
