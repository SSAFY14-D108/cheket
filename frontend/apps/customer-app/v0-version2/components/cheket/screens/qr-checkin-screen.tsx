'use client'

import { useState, useEffect, useCallback } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { RefreshCw } from 'lucide-react'

// Generate a simple SVG-based QR-like code for visual mock
function MockQR({ value }: { value: string }) {
  const size = 200
  const cells = 21
  const cellSize = size / cells

  // Deterministic pattern based on value
  const pattern = Array.from({ length: cells }, (_, row) =>
    Array.from({ length: cells }, (_, col) => {
      const hash = (row * 31 + col * 17 + value.charCodeAt(row % value.length)) % 3
      // Finder patterns (corners)
      if (row < 8 && col < 8) return row === 0 || row === 7 || col === 0 || col === 7 || (row >= 2 && row <= 4 && col >= 2 && col <= 4)
      if (row < 8 && col > cells - 9) return row === 0 || row === 7 || col === cells - 1 || col === cells - 8 || (row >= 2 && row <= 4 && col >= cells - 6 && col <= cells - 4)
      if (row > cells - 9 && col < 8) return row === cells - 1 || row === cells - 8 || col === 0 || col === 7 || (row >= cells - 5 && row <= cells - 3 && col >= 2 && col <= 4)
      return hash === 0
    })
  )

  return (
    <svg
      width={size}
      height={size}
      viewBox={`0 0 ${size} ${size}`}
      className="rounded-xl"
      aria-label="QR 코드"
    >
      <rect width={size} height={size} fill="white" />
      {pattern.map((row, r) =>
        row.map((filled, c) =>
          filled ? (
            <rect
              key={`${r}-${c}`}
              x={c * cellSize}
              y={r * cellSize}
              width={cellSize}
              height={cellSize}
              fill="black"
            />
          ) : null
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
      <div className="flex flex-col items-center justify-between min-h-full p-6 py-8 bg-background">
        <div className="text-center">
          <h2 className="font-bold text-base text-foreground mb-1">{ticket.eventName}</h2>
          <p className="text-sm text-muted-foreground">{ticket.seatLabel} · {ticket.grade}</p>
        </div>

        {checkedIn ? (
          <div className="flex flex-col items-center gap-4 text-center">
            <div className="w-24 h-24 rounded-full bg-primary/10 border-2 border-primary flex items-center justify-center">
              <span className="text-4xl text-primary">✓</span>
            </div>
            <p className="font-bold text-xl text-foreground">체크인 완료!</p>
            <p className="text-muted-foreground text-sm">즐거운 관람 되세요!</p>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-6">
            {/* QR code with timer ring */}
            <div className="relative">
              <div className="p-3 bg-white rounded-2xl shadow-lg">
                <MockQR value={otpValue} />
              </div>
              {/* Timer ring */}
              <div className="absolute -top-3 -right-3">
                <svg width="48" height="48" viewBox="0 0 48 48">
                  <circle cx="24" cy="24" r="22" fill="none" stroke="hsl(var(--border))" strokeWidth="3" />
                  <circle
                    cx="24" cy="24" r="22"
                    fill="none"
                    stroke="hsl(var(--primary))"
                    strokeWidth="3"
                    strokeDasharray={circumference}
                    strokeDashoffset={dashOffset}
                    strokeLinecap="round"
                    transform="rotate(-90 24 24)"
                    className="transition-all duration-1000"
                  />
                  <text x="24" y="28" textAnchor="middle" fontSize="13" fontWeight="bold" fill="hsl(var(--foreground))">
                    {timer}
                  </text>
                </svg>
              </div>
            </div>

            <p className="text-xs text-muted-foreground text-center">
              {timer}초 후 QR 코드가 자동 갱신됩니다
            </p>

            <button
              onClick={refresh}
              className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
            >
              <RefreshCw className="w-4 h-4" /> QR 재발급
            </button>
          </div>
        )}

        <div className="flex flex-col gap-3 w-full">
          {!checkedIn && (
            <button
              onClick={handleCheckIn}
              className="w-full bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all"
            >
              체크인 (Mock)
            </button>
          )}
          {checkedIn && (
            <button
              onClick={goBack}
              className="w-full bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm hover:border-primary/50 transition-all"
            >
              닫기
            </button>
          )}
        </div>
      </div>
    </AppShell>
  )
}
