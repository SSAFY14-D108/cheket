'use client'

import { useState, useEffect } from 'react'
import { useApp } from '@/lib/app-context'
import { MOCK_EVENTS } from '@/lib/mock-data'
import { AppShell } from '../app-shell'
import { Clock, Users } from 'lucide-react'

const TOTAL_WAIT_SECONDS = 5
const MOCK_QUEUE_POSITION = 47
const MOCK_ESTIMATED_WAIT = 3 // minutes

export function WaitingQueueScreen() {
  const { navParams, navigate, goBack } = useApp()
  const event = MOCK_EVENTS.find((e) => e.id === navParams.eventId)

  const [position, setPosition] = useState(MOCK_QUEUE_POSITION)
  const [elapsed, setElapsed] = useState(0)

  // Simulate queue progression: count down position then enter seat selection immediately
  useEffect(() => {
    const interval = setInterval(() => {
      setElapsed((prev) => {
        const next = prev + 1
        if (next >= TOTAL_WAIT_SECONDS) {
          clearInterval(interval)
        }
        return next
      })
      setPosition((prev) => Math.max(1, prev - Math.floor(Math.random() * 4 + 1)))
    }, 1000)

    return () => clearInterval(interval)
  }, [])

  useEffect(() => {
    if (elapsed < TOTAL_WAIT_SECONDS) return
    navigate('seat-selection', { eventId: navParams.eventId })
  }, [elapsed, navigate, navParams.eventId])

  if (!event) return null

  const progress = Math.min(elapsed / TOTAL_WAIT_SECONDS, 1)
  const estimatedWait = Math.max(0, Math.round(MOCK_ESTIMATED_WAIT * (1 - progress)))

  return (
    <AppShell showBack onBack={goBack} title="대기열" showBottomNav={false}>
      <div className="flex flex-col items-center justify-center min-h-full p-6 gap-8">
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
          대기열이 끝나면 좌석 선택 화면으로 자동 이동합니다.<br />
          화면을 닫지 마세요.
        </p>
      </div>
    </AppShell>
  )
}
