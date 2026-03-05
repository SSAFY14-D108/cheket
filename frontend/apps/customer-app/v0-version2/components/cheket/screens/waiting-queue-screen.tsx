'use client'

import { useState, useEffect, useCallback } from 'react'
import { useApp } from '@/lib/app-context'
import { MOCK_EVENTS } from '@/lib/mock-data'
import { WaitingQueueState } from '@/lib/types'
import { AppShell } from '../app-shell'
import { cn } from '@/lib/utils'
import { Clock, CheckCircle2, AlertCircle, Users } from 'lucide-react'

const TOTAL_WAIT_SECONDS = 5
const MOCK_QUEUE_POSITION = 47
const MOCK_ESTIMATED_WAIT = 3 // minutes

export function WaitingQueueScreen() {
  const { navParams, navigate, goBack } = useApp()
  const event = MOCK_EVENTS.find((e) => e.id === navParams.eventId)

  const [queueState, setQueueState] = useState<WaitingQueueState>('WAITING')
  const [position, setPosition] = useState(MOCK_QUEUE_POSITION)
  const [elapsed, setElapsed] = useState(0)

  // Simulate queue progression: count down position then go READY
  useEffect(() => {
    if (queueState !== 'WAITING') return

    const interval = setInterval(() => {
      setElapsed((prev) => {
        const next = prev + 1
        if (next >= TOTAL_WAIT_SECONDS) {
          setQueueState('READY_TO_ENTER')
          clearInterval(interval)
        }
        return next
      })
      setPosition((prev) => Math.max(1, prev - Math.floor(Math.random() * 4 + 1)))
    }, 1000)

    return () => clearInterval(interval)
  }, [queueState])

  // Auto-enter on READY_TO_ENTER after a brief moment
  useEffect(() => {
    if (queueState !== 'READY_TO_ENTER') return
    const timeout = setTimeout(() => {
      navigate('seat-selection', { eventId: navParams.eventId })
    }, 1500)
    return () => clearTimeout(timeout)
  }, [queueState, navigate, navParams.eventId])

  // Expire after 60 seconds of being READY (kept as fallback)
  const [readyCountdown, setReadyCountdown] = useState(60)
  useEffect(() => {
    if (queueState !== 'READY_TO_ENTER') return
    const interval = setInterval(() => {
      setReadyCountdown((prev) => {
        if (prev <= 1) {
          setQueueState('EXPIRED')
          clearInterval(interval)
          return 0
        }
        return prev - 1
      })
    }, 1000)
    return () => clearInterval(interval)
  }, [queueState])

  const handleEnter = useCallback(() => {
    navigate('seat-selection', { eventId: navParams.eventId })
  }, [navigate, navParams.eventId])

  if (!event) return null

  const progress = Math.min(elapsed / TOTAL_WAIT_SECONDS, 1)
  const estimatedWait = Math.max(0, Math.round(MOCK_ESTIMATED_WAIT * (1 - progress)))

  return (
    <AppShell showBack onBack={goBack} title="대기열" showBottomNav={false}>
      <div className="flex flex-col items-center justify-center min-h-full p-6 gap-8">
        {/* State: WAITING */}
        {queueState === 'WAITING' && (
          <>
            <div className="flex flex-col items-center gap-4 text-center">
              <div className="relative w-28 h-28">
                <svg className="w-28 h-28 -rotate-90" viewBox="0 0 112 112">
                  <circle cx="56" cy="56" r="48" strokeWidth="6" className="stroke-secondary fill-none" />
                  <circle
                    cx="56" cy="56" r="48"
                    strokeWidth="6"
                    strokeDasharray={`${2 * Math.PI * 48}`}
                    strokeDashoffset={`${2 * Math.PI * 48 * (1 - progress)}`}
                    strokeLinecap="round"
                    className="stroke-primary fill-none transition-all duration-1000"
                  />
                </svg>
                <div className="absolute inset-0 flex flex-col items-center justify-center">
                  <Clock className="w-5 h-5 text-primary mb-0.5" />
                  <span className="font-bold text-foreground text-lg">{Math.ceil((TOTAL_WAIT_SECONDS - elapsed) / 60 * 60)}</span>
                  <span className="text-xs text-muted-foreground">초 남음</span>
                </div>
              </div>

              <div>
                <p className="text-muted-foreground text-sm mb-1">현재 대기 순번</p>
                <p className="font-bold text-5xl text-foreground">{position}</p>
                <p className="text-muted-foreground text-sm mt-1">번</p>
              </div>
            </div>

            <div className="w-full bg-card border border-border rounded-xl p-4 flex flex-col gap-3">
              <div className="flex items-center justify-between text-sm">
                <span className="flex items-center gap-2 text-muted-foreground">
                  <Users className="w-4 h-4" /> 예상 대기 시간
                </span>
                <span className="font-semibold text-foreground">
                  {estimatedWait === 0 ? '잠시 후 입장' : `약 ${estimatedWait}분`}
                </span>
              </div>
              <div className="h-2 bg-secondary rounded-full overflow-hidden">
                <div
                  className="h-full bg-primary rounded-full transition-all duration-1000"
                  style={{ width: `${progress * 100}%` }}
                />
              </div>
              <p className="text-xs text-muted-foreground text-center">
                {event.name}
              </p>
            </div>

            <p className="text-xs text-muted-foreground text-center leading-relaxed">
              대기열 이탈 시 순번이 초기화됩니다.<br />
              화면을 닫지 마세요.
            </p>
          </>
        )}

        {/* State: READY_TO_ENTER */}
        {queueState === 'READY_TO_ENTER' && (
          <>
            <div className="flex flex-col items-center gap-4 text-center">
              <div className="w-24 h-24 rounded-full bg-primary/10 border-2 border-primary flex items-center justify-center">
                <CheckCircle2 className="w-12 h-12 text-primary" />
              </div>
              <div>
                <h2 className="font-bold text-2xl text-foreground mb-2">입장 가능!</h2>
                <p className="text-muted-foreground text-sm">
                  좌석 선택 화면으로 이동하세요.<br />
                  <span className="text-primary font-semibold">{readyCountdown}초</span> 내에 입장해주세요.
                </p>
              </div>
            </div>

            <div className="w-full bg-primary/5 border border-primary/20 rounded-xl p-4 text-center">
              <p className="text-sm text-foreground font-medium">{event.name}</p>
              <p className="text-xs text-muted-foreground mt-1">{event.date}</p>
            </div>

            <div className="w-full flex flex-col gap-3">
              <button
                onClick={handleEnter}
                className="w-full bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all"
              >
                좌석 선택하러 가기
              </button>
            </div>
          </>
        )}

        {/* State: EXPIRED */}
        {queueState === 'EXPIRED' && (
          <>
            <div className="flex flex-col items-center gap-4 text-center">
              <div className="w-24 h-24 rounded-full bg-red-500/10 border-2 border-red-500/40 flex items-center justify-center">
                <AlertCircle className="w-12 h-12 text-red-400" />
              </div>
              <div>
                <h2 className="font-bold text-2xl text-foreground mb-2">입장 시간 만료</h2>
                <p className="text-muted-foreground text-sm">
                  입장 가능 시간이 초과되었습니다.<br />
                  다시 대기열에 참여해주세요.
                </p>
              </div>
            </div>

            <div className="w-full flex flex-col gap-3">
              <button
                onClick={() => {
                  setQueueState('WAITING')
                  setElapsed(0)
                  setPosition(MOCK_QUEUE_POSITION)
                  setReadyCountdown(60)
                }}
                className="w-full bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all"
              >
                다시 대기열 참여
              </button>
              <button
                onClick={goBack}
                className={cn(
                  'w-full bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm',
                  'hover:border-primary/50 active:scale-[0.98] transition-all'
                )}
              >
                공연 목록으로
              </button>
            </div>
          </>
        )}
      </div>
    </AppShell>
  )
}
