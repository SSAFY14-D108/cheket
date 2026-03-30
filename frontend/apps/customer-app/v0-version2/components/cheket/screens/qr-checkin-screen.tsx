'use client'

import { useCallback, useEffect, useState } from 'react'
import { RefreshCw } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'

function MockQR({ value }: { value: string }) {
  const size = 200
  const cells = 21
  const cellSize = size / cells

  const pattern = Array.from({ length: cells }, (_, row) =>
    Array.from({ length: cells }, (_, col) => {
      const hash = (row * 31 + col * 17 + value.charCodeAt(row % value.length)) % 3
      if (row < 8 && col < 8) return row === 0 || row === 7 || col === 0 || col === 7 || (row >= 2 && row <= 4 && col >= 2 && col <= 4)
      if (row < 8 && col > cells - 9) return row === 0 || row === 7 || col === cells - 1 || col === cells - 8 || (row >= 2 && row <= 4 && col >= cells - 6 && col <= cells - 4)
      if (row > cells - 9 && col < 8) return row === cells - 1 || row === cells - 8 || col === 0 || col === 7 || (row >= cells - 5 && row <= cells - 3 && col >= 2 && col <= 4)
      return hash === 0
    })
  )

  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="rounded-xl" aria-label="QR 코드">
      <rect width={size} height={size} fill="white" />
      {pattern.map((row, r) =>
        row.map((filled, c) =>
          filled ? <rect key={`${r}-${c}`} x={c * cellSize} y={r * cellSize} width={cellSize} height={cellSize} fill="black" /> : null
        )
      )}
    </svg>
  )
}

export function QrCheckinScreen() {
  const { navParams, goBack, tickets, updateTicketStatus } = useApp()
  const ticket = tickets.find((t) => t.id === navParams.ticketId)
  const [timer, setTimer] = useState(30)
  const [otpValue, setOtpValue] = useState(`${Date.now()}`)
  const [checkedIn, setCheckedIn] = useState(false)

  const refresh = useCallback(() => {
    setOtpValue(`${Date.now()}`)
    setTimer(30)
  }, [])

  useEffect(() => {
    if (checkedIn) return
    const interval = setInterval(() => {
      setTimer((prev) => {
        if (prev <= 1) {
          refresh()
          return 30
        }
        return prev - 1
      })
    }, 1000)
    return () => clearInterval(interval)
  }, [checkedIn, refresh])

  const handleCheckIn = () => {
    if (!ticket) return
    updateTicketStatus(ticket.id, { status: 'USED', attendedDate: new Date().toISOString().split('T')[0] })
    setCheckedIn(true)
  }

  if (!ticket) return null

  const circumference = 2 * Math.PI * 22
  const dashOffset = circumference - (timer / 30) * circumference

  return (
    <AppShell showBack onBack={goBack} title="QR 체크인" showBottomNav={false}>
      <div className="flex min-h-full flex-col items-center justify-between bg-background p-6 py-8">
        <div className="text-center">
          <h2 className="mb-1 text-base font-bold text-[#111111]">{ticket.eventName}</h2>
          <p className="text-sm text-muted-foreground">
            {ticket.seatLabel} · {ticket.grade}
          </p>
        </div>

        {checkedIn ? (
          <div className="flex flex-col items-center gap-4 text-center">
            <div className="gradient-border-icon-button flex h-24 w-24 items-center justify-center rounded-full">
              <span className="text-4xl text-[#333333]">✓</span>
            </div>
            <p className="text-xl font-bold text-[#111111]">체크인이 완료됐어요</p>
            <p className="text-sm text-muted-foreground">즐거운 공연 관람 되세요.</p>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-6">
            <div className="relative">
              <div className="elevated-surface p-3 rounded-2xl shadow-lg">
                <MockQR value={otpValue} />
              </div>
              <div className="absolute -right-3 -top-3">
                <svg width="48" height="48" viewBox="0 0 48 48">
                  <circle cx="24" cy="24" r="22" fill="none" stroke="#e5e7eb" strokeWidth="3" />
                  <circle
                    cx="24"
                    cy="24"
                    r="22"
                    fill="none"
                    stroke="#9aa4b2"
                    strokeWidth="3"
                    strokeDasharray={circumference}
                    strokeDashoffset={dashOffset}
                    strokeLinecap="round"
                    transform="rotate(-90 24 24)"
                    className="transition-all duration-1000"
                  />
                  <text x="24" y="28" textAnchor="middle" fontSize="13" fontWeight="bold" fill="#111111">
                    {timer}
                  </text>
                </svg>
              </div>
            </div>

            <p className="text-center text-xs text-muted-foreground">{timer}초 후 QR 코드가 자동으로 갱신됩니다.</p>

            <button onClick={refresh} className="flex items-center gap-2 text-sm text-muted-foreground transition-colors hover:text-foreground">
              <RefreshCw className="h-4 w-4" />
              QR 새로고침
            </button>
          </div>
        )}

        <div className="flex w-full flex-col gap-3">
          {!checkedIn && (
            <button onClick={handleCheckIn} className="gradient-border-button w-full rounded-xl py-4 text-sm font-semibold text-[#111111]">
              체크인 처리 (Mock)
            </button>
          )}
          {checkedIn && (
            <button onClick={goBack} className="elevated-surface w-full rounded-xl py-3.5 text-sm font-semibold text-[#111111]">
              돌아가기
            </button>
          )}
        </div>
      </div>
    </AppShell>
  )
}
