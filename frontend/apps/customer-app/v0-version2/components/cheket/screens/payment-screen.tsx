'use client'

import { useState } from 'react'
import { useApp } from '@/lib/app-context'
import { PurchaseFailureReason, Ticket } from '@/lib/types'
import { AppShell } from '../app-shell'
import { Wallet, CheckCircle, AlertCircle, FlaskConical } from 'lucide-react'

const FAILURE_REASONS: PurchaseFailureReason[] = [
  'SOLD_OUT',
  'LOCK_FAILED',
  'LIMIT_EXCEEDED',
  'INSUFFICIENT_BALANCE',
  'NETWORK',
]

export function PaymentScreen() {
  const { navParams, navigate, goBack, user, addTicket, addTx, events } = useApp()
  const event = events.find((item) => item.id === navParams.eventId)
  const seats = navParams.seats ?? []
  const totalPrice = navParams.totalPrice ?? 0
  const [approved, setApproved] = useState(false)
  const [success, setSuccess] = useState(false)
  const [simulateFailure, setSimulateFailure] = useState(false)
  const [selectedReason, setSelectedReason] = useState<PurchaseFailureReason>('SOLD_OUT')

  if (!event || !user) return null

  const hasSufficientBalance = user.ctkBalance >= totalPrice

  const handleConfirm = () => {
    if (!approved || !hasSufficientBalance) return

    if (simulateFailure) {
      navigate('purchase-failed', { failureReason: selectedReason, eventId: event.id })
      return
    }

    seats.forEach((seat) => {
      const ticket: Ticket = {
        id: `tkt_${Date.now()}_${seat.id}`,
        eventId: event.id,
        eventName: event.name,
        eventDate: event.date,
        venue: event.venue,
        poster: event.poster,
        seatId: seat.id,
        seatLabel: `${seat.row} ${seat.number}번`,
        grade: seat.grade,
        originalPrice: seat.price,
        status: 'SOLD',
      }
      addTicket(ticket)
    })

    addTx('PURCHASE', `${event.name} 티켓 구매`, totalPrice)
    setSuccess(true)
  }

  if (success) {
    return (
      <div className="flex h-full flex-col bg-background relative">
        <div className="flex flex-col items-center flex-1 overflow-y-auto px-6 pt-10 pb-24 text-center">
          <div className="flex h-20 w-20 flex-shrink-0 items-center justify-center rounded-full border-2 border-primary bg-primary/10">
            <CheckCircle className="h-10 w-10 text-primary" />
          </div>
          <div>
          <h2 className="mb-2 text-xl font-bold text-foreground">예매가 완료되었습니다</h2>
          <p className="text-sm text-muted-foreground">
            구매한 티켓은 내 티켓에서 바로 확인할 수 있습니다.
          </p>
        </div>

        <div className="w-full overflow-hidden rounded-2xl border border-border bg-card text-left">
          <div className="relative aspect-[16/9] w-full">
            <img src={event.poster} alt={event.name} className="h-full w-full object-cover" />
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
            <div className="absolute right-4 bottom-3 left-4">
              <h3 className="text-lg font-bold text-white drop-shadow-lg">{event.name}</h3>
            </div>
          </div>

          <div className="flex flex-col gap-3 p-4">
            <div className="flex items-center gap-3 text-sm">
              <div className="flex-1">
                <p className="mb-0.5 text-xs text-muted-foreground">공연 일시</p>
                <p className="font-medium text-foreground">{event.date}</p>
              </div>
              <div className="h-8 w-px bg-border" />
              <div className="flex-1">
                <p className="mb-0.5 text-xs text-muted-foreground">공연장</p>
                <p className="truncate font-medium text-foreground">{event.venue.split(',')[0]}</p>
              </div>
            </div>

            <div className="h-px bg-border" />

            <div>
              <p className="mb-2 text-xs text-muted-foreground">선택 좌석</p>
              <div className="flex flex-col gap-1.5">
                {seats.map((seat) => (
                  <div key={seat.id} className="flex items-center justify-between rounded-lg bg-secondary/50 px-3 py-2">
                    <span className="text-sm font-medium text-foreground">{seat.row} {seat.number}번</span>
                    <span className="rounded-full bg-primary/10 px-2 py-0.5 text-xs font-semibold text-primary">
                      {seat.grade}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex items-center justify-between border-t border-border pt-2">
              <span className="text-sm text-muted-foreground">총 결제 금액</span>
              <span className="font-bold text-primary">{totalPrice.toLocaleString()} CTK</span>
            </div>
          </div>
        </div>

        <button
          onClick={() => navigate('my-tickets')}
          className="w-full rounded-xl bg-primary py-3.5 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98]"
        >
          내 티켓 보기
        </button>
        </div>
      </div>
    )
  }

  return (
    <AppShell showBack onBack={goBack} title="결제 확인" showBottomNav={false}>
      <div className="flex flex-col gap-4 p-4">
        <div className="rounded-xl border border-border bg-card p-4">
          <h3 className="mb-3 text-sm font-semibold text-foreground">{event.name}</h3>
          <div className="flex flex-col gap-2">
            {seats.map((seat) => (
              <div key={seat.id} className="flex items-center justify-between">
                <div>
                  <span className="text-sm text-foreground">{seat.row} {seat.number}번</span>
                  <span className="ml-2 text-xs text-muted-foreground">({seat.grade})</span>
                </div>
                <span className="text-sm font-medium text-foreground">{seat.price.toLocaleString()} CTK</span>
              </div>
            ))}
            <div className="my-1 h-px bg-border" />
            <div className="flex items-center justify-between">
              <span className="text-sm font-semibold text-foreground">총 결제 금액</span>
              <span className="text-base font-bold text-primary">{totalPrice.toLocaleString()} CTK</span>
            </div>
          </div>
        </div>

        <div className={`rounded-xl border p-4 ${hasSufficientBalance ? 'border-border bg-card' : 'border-red-500/30 bg-red-500/10'}`}>
          <div className="mb-3 flex items-center gap-2">
            <Wallet className="h-4 w-4 text-primary" />
            <span className="text-sm font-semibold text-foreground">보유 CTK 잔액</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-2xl font-bold text-foreground">{user.ctkBalance.toLocaleString()}</span>
            <span className="text-sm font-medium text-muted-foreground">CTK</span>
          </div>
          {!hasSufficientBalance && (
            <div className="mt-3 flex items-center gap-2">
              <AlertCircle className="h-4 w-4 text-red-400" />
              <p className="text-xs text-red-400">
                잔액이 부족합니다. {(totalPrice - user.ctkBalance).toLocaleString()} CTK를 더 충전해 주세요.
              </p>
            </div>
          )}
        </div>

        <div className="rounded-xl border border-border bg-card p-4">
          <p className="mb-3 text-xs text-muted-foreground">
            결제 전 승인 단계를 거치면 구매 버튼이 활성화됩니다.
          </p>
          <button
            onClick={() => setApproved(true)}
            disabled={!hasSufficientBalance || approved}
            className={`w-full rounded-xl py-3 text-sm font-semibold transition-all ${
              approved
                ? 'border border-primary/30 bg-primary/10 text-primary'
                : 'border border-border bg-secondary text-foreground hover:border-primary/50'
            } disabled:cursor-not-allowed disabled:opacity-40`}
          >
            {approved ? (
              <span className="flex items-center justify-center gap-2">
                <CheckCircle className="h-4 w-4" />
                Approve 완료
              </span>
            ) : (
              'Approve'
            )}
          </button>
        </div>

        <div className="flex flex-col gap-3 rounded-xl border border-border bg-secondary p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <FlaskConical className="h-4 w-4 text-muted-foreground" />
              <span className="text-xs font-semibold text-muted-foreground">실패 시뮬레이션</span>
            </div>
            <button
              onClick={() => setSimulateFailure((value) => !value)}
              className={`relative h-5 w-10 rounded-full transition-colors ${simulateFailure ? 'bg-primary' : 'bg-muted-foreground/30'}`}
              role="switch"
              aria-checked={simulateFailure}
            >
              <span className={`absolute top-0.5 h-4 w-4 rounded-full bg-white shadow transition-transform ${simulateFailure ? 'translate-x-5' : 'translate-x-0.5'}`} />
            </button>
          </div>

          {simulateFailure && (
            <div className="flex flex-col gap-2">
              <p className="text-xs text-muted-foreground">실패 사유 선택</p>
              <div className="flex flex-wrap gap-1.5">
                {FAILURE_REASONS.map((reason) => (
                  <button
                    key={reason}
                    onClick={() => setSelectedReason(reason)}
                    className={`rounded-full border px-2.5 py-1 text-xs font-medium transition-colors ${
                      selectedReason === reason
                        ? 'border-primary bg-primary text-primary-foreground'
                        : 'border-border bg-card text-muted-foreground hover:text-foreground'
                    }`}
                  >
                    {reason}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        <button
          onClick={handleConfirm}
          disabled={!approved || !hasSufficientBalance}
          className="w-full rounded-xl bg-primary py-4 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-40"
        >
          {simulateFailure ? '구매 실패 테스트' : '구매 확정'}
        </button>

        <p className="text-center text-xs text-muted-foreground">
          구매가 완료되면 NFT 티켓이 즉시 발급되고 내 티켓에서 확인할 수 있습니다.
        </p>
      </div>
    </AppShell>
  )
}
