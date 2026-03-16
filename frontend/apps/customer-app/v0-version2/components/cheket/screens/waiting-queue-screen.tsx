'use client'

import { useCallback, useEffect, useState } from 'react'
import { AlertCircle, CheckCircle2, Clock, Users } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { WaitingQueueState } from '@/lib/types'
import { AppShell } from '../app-shell'
import { cn } from '@/lib/utils'

const TOTAL_WAIT_SECONDS = 5
const MOCK_QUEUE_POSITION = 47
const MOCK_ESTIMATED_WAIT = 3

export function WaitingQueueScreen() {
  const { navParams, navigate, goBack, events } = useApp()
  const event = events.find((item) => item.id === navParams.eventId)

  const [queueState, setQueueState] = useState<WaitingQueueState>('WAITING')
  const [position, setPosition] = useState(MOCK_QUEUE_POSITION)
  const [elapsed, setElapsed] = useState(0)
  const [readyCountdown, setReadyCountdown] = useState(60)

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

  useEffect(() => {
    if (queueState !== 'READY_TO_ENTER') return
    const timeout = setTimeout(() => {
      navigate('seat-selection', { eventId: navParams.eventId, eventDateId: navParams.eventDateId })
    }, 1500)
    return () => clearTimeout(timeout)
  }, [queueState, navigate, navParams.eventDateId, navParams.eventId])

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
    navigate('seat-selection', { eventId: navParams.eventId, eventDateId: navParams.eventDateId })
  }, [navigate, navParams.eventDateId, navParams.eventId])

  if (!event) return null

  const progress = Math.min(elapsed / TOTAL_WAIT_SECONDS, 1)
  const estimatedWait = Math.max(0, Math.round(MOCK_ESTIMATED_WAIT * (1 - progress)))

  return (
    <AppShell showBack onBack={goBack} title="대기열" showBottomNav={false}>
      <div className="flex min-h-full flex-col items-center justify-center gap-8 p-6">
        {queueState === 'WAITING' && (
          <>
            <div className="flex flex-col items-center gap-4 text-center">
              <div className="relative h-28 w-28">
                <svg className="h-28 w-28 -rotate-90" viewBox="0 0 112 112">
                  <circle cx="56" cy="56" r="48" strokeWidth="6" className="fill-none stroke-[#e5e7eb]" />
                  <circle
                    cx="56"
                    cy="56"
                    r="48"
                    strokeWidth="6"
                    strokeDasharray={`${2 * Math.PI * 48}`}
                    strokeDashoffset={`${2 * Math.PI * 48 * (1 - progress)}`}
                    strokeLinecap="round"
                    className="fill-none stroke-[#9aa4b2] transition-all duration-1000"
                  />
                </svg>
                <div className="absolute inset-0 flex flex-col items-center justify-center">
                  <Clock className="mb-0.5 h-5 w-5 text-[#6b7280]" />
                  <span className="text-lg font-bold text-[#111111]">{Math.ceil(TOTAL_WAIT_SECONDS - elapsed)}</span>
                  <span className="text-xs text-muted-foreground">초 남음</span>
                </div>
              </div>

              <div>
                <p className="mb-1 text-sm text-muted-foreground">현재 대기 순번</p>
                <p className="text-5xl font-bold text-[#111111]">{position}</p>
                <p className="mt-1 text-sm text-muted-foreground">번째</p>
              </div>
            </div>

            <div className="gradient-outline-surface flex w-full flex-col gap-3 rounded-xl p-4">
              <div className="flex items-center justify-between text-sm">
                <span className="flex items-center gap-2 text-muted-foreground">
                  <Users className="h-4 w-4" />
                  예상 대기 시간
                </span>
                <span className="font-semibold text-[#111111]">{estimatedWait === 0 ? '곧 입장 가능' : `약 ${estimatedWait}분`}</span>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-[#eef1f4]">
                <div className="h-full rounded-full bg-[#9aa4b2] transition-all duration-1000" style={{ width: `${progress * 100}%` }} />
              </div>
              <p className="text-center text-xs text-muted-foreground">{event.name}</p>
            </div>

            <p className="text-center text-xs leading-relaxed text-muted-foreground">
              순서가 되면 자동으로 좌석 선택 화면으로 이동해요.
              <br />
              화면을 유지한 상태로 잠시만 기다려 주세요.
            </p>
          </>
        )}

        {queueState === 'READY_TO_ENTER' && (
          <>
            <div className="flex flex-col items-center gap-4 text-center">
              <div className="gradient-outline-icon-button flex h-24 w-24 items-center justify-center rounded-full">
                <CheckCircle2 className="h-12 w-12 text-[#333333]" />
              </div>
              <div>
                <h2 className="mb-2 text-2xl font-bold text-[#111111]">입장할 수 있어요</h2>
                <p className="text-sm text-muted-foreground">
                  좌석 선택 화면으로 바로 이동할 수 있어요.
                  <br />
                  <span className="font-semibold text-[#111111]">{readyCountdown}초</span> 안에 입장해 주세요.
                </p>
              </div>
            </div>

            <div className="gradient-outline-surface w-full rounded-xl p-4 text-center">
              <p className="text-sm font-medium text-[#111111]">{event.name}</p>
              <p className="mt-1 text-xs text-muted-foreground">{event.date}</p>
            </div>

            <button onClick={handleEnter} className="gradient-outline-button w-full rounded-xl py-4 text-sm font-semibold text-[#111111]">
              바로 입장하기
            </button>
          </>
        )}

        {queueState === 'EXPIRED' && (
          <>
            <div className="flex flex-col items-center gap-4 text-center">
              <div className="flex h-24 w-24 items-center justify-center rounded-full border border-red-200 bg-red-50">
                <AlertCircle className="h-12 w-12 text-red-400" />
              </div>
              <div>
                <h2 className="mb-2 text-2xl font-bold text-[#111111]">입장 시간이 만료됐어요</h2>
                <p className="text-sm text-muted-foreground">다시 대기열에 등록한 뒤 순서를 기다려야 해요.</p>
              </div>
            </div>

            <div className="flex w-full flex-col gap-3">
              <button
                onClick={() => {
                  setQueueState('WAITING')
                  setElapsed(0)
                  setPosition(MOCK_QUEUE_POSITION)
                  setReadyCountdown(60)
                }}
                className="gradient-outline-button w-full rounded-xl py-4 text-sm font-semibold text-[#111111]"
              >
                다시 대기하기
              </button>
              <button
                onClick={goBack}
                className={cn('gradient-outline-surface w-full rounded-xl py-3.5 text-sm font-semibold text-[#111111]', 'transition-all active:scale-[0.98]')}
              >
                이전 화면으로
              </button>
            </div>
          </>
        )}
      </div>
    </AppShell>
  )
}
