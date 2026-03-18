'use client'

import { useState } from 'react'
import { AlertCircle, CheckCircle, FlaskConical, Wallet } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { PurchaseFailureReason, Ticket } from '@/lib/types'
import { AppShell } from '../app-shell'

const FAILURE_REASONS: PurchaseFailureReason[] = ['SOLD_OUT', 'LOCK_FAILED', 'LIMIT_EXCEEDED', 'INSUFFICIENT_BALANCE', 'NETWORK']

export function PaymentScreen() {
  const { navParams, navigate, goBack, user, addTicket, addTx, events } = useApp()
  const event = events.find((item) => item.id === navParams.eventId)
  const selectedEventDate = event?.dates?.find((item) => item.id === navParams.eventDateId) ?? null
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
      navigate('purchase-failed', {
        failureReason: selectedReason,
        eventId: event.id,
      })
      return
    }

    seats.forEach((seat) => {
      const ticket: Ticket = {
        id: `tkt_${Date.now()}_${seat.id}`,
        eventId: event.id,
        eventName: event.name,
        eventDate: selectedEventDate?.label ?? event.date,
        venue: event.venue,
        poster: event.poster,
        seatId: seat.id,
        seatLabel: `${seat.row}-${seat.number}번`,
        grade: seat.grade,
        originalPrice: seat.price,
        status: 'SOLD',
      }
      addTicket(ticket)
    })

    addTx('PURCHASE', `${event.name} 예매`, totalPrice)
    setSuccess(true)
  }

  if (success) {
    return (
      <div className="h-full overflow-y-auto bg-[#f7f8fa]">
        <div className="flex min-h-full flex-col items-center gap-6 p-6 pb-32 pt-10 text-center">
          <div className="gradient-border-icon-button flex h-20 w-20 items-center justify-center rounded-full">
            <CheckCircle className="h-10 w-10 text-[#333333]" />
          </div>

          <div>
            <h2 className="mb-2 text-xl font-bold text-[#111111]">예매가 완료되었어요</h2>
            <p className="text-sm text-muted-foreground">구매한 티켓은 내 티켓 탭에서 바로 확인할 수 있어요.</p>
          </div>

          <div className="elevated-surface w-full overflow-hidden rounded-2xl text-left">
            <div className="relative aspect-[16/9] w-full">
              <img src={event.poster} alt={event.name} className="h-full w-full object-cover" />
              <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
              <div className="absolute bottom-3 left-4 right-4">
                <h3 className="text-lg font-bold text-white drop-shadow-lg">{event.name}</h3>
              </div>
            </div>

            <div className="flex flex-col gap-3 p-4">
              <div className="flex items-center gap-3 text-sm">
                <div className="min-w-0 flex-1">
                  <p className="mb-0.5 text-xs text-muted-foreground">공연 일시</p>
                  <p className="font-medium text-[#111111]">{selectedEventDate?.label ?? event.date}</p>
                </div>
                <div className="h-8 w-px bg-[#e5e7eb]" />
                <div className="min-w-0 flex-1">
                  <p className="mb-0.5 text-xs text-muted-foreground">공연 장소</p>
                  <p className="break-words text-sm leading-5 text-[#111111]">{event.venue.split(',')[0]}</p>
                </div>
              </div>

              <div className="h-px bg-[#e5e7eb]" />

              <div>
                <p className="mb-2 text-xs text-muted-foreground">선택 좌석</p>
                <div className="flex flex-col gap-1.5">
                  {seats.map((seat) => (
                    <div key={seat.id} className="elevated-surface-soft flex items-center justify-between rounded-lg px-3 py-2">
                      <span className="text-sm font-medium text-[#111111]">
                        {seat.row}-{seat.number}번
                      </span>
                      <span className="rounded-full bg-[#f3f4f6] px-2 py-0.5 text-xs font-semibold text-[#333333]">{seat.grade}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex items-center justify-between border-t border-[#e5e7eb] pt-2">
                <span className="text-sm text-muted-foreground">총 결제 금액</span>
                <span className="font-bold text-[#111111]">{totalPrice.toLocaleString()} CTK</span>
              </div>
            </div>
          </div>
        </div>

        <div className="sticky bottom-0 left-0 right-0 border-t border-border/60 bg-background/95 px-6 py-4 backdrop-blur supports-[backdrop-filter]:bg-background/80">
          <button
            onClick={() => navigate('my-tickets')}
            className="gradient-border-button w-full rounded-xl py-3.5 text-sm font-semibold text-[#111111] transition-all active:scale-[0.98]"
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
        <div className="elevated-surface rounded-xl p-4">
          <h3 className="mb-3 text-sm font-semibold text-[#111111]">{event.name}</h3>
          <div className="flex flex-col gap-2">
            {seats.map((seat) => (
              <div key={seat.id} className="flex items-center justify-between">
                <div>
                  <span className="text-sm text-[#111111]">
                    {seat.row}-{seat.number}번
                  </span>
                  <span className="ml-2 text-xs text-muted-foreground">({seat.grade})</span>
                </div>
                <span className="text-sm font-medium text-[#111111]">{seat.price.toLocaleString()} CTK</span>
              </div>
            ))}
            <div className="my-1 h-px bg-[#e5e7eb]" />
            <div className="flex items-center justify-between">
              <span className="text-sm font-semibold text-[#111111]">총 결제 금액</span>
              <span className="text-base font-bold text-[#111111]">{totalPrice.toLocaleString()} CTK</span>
            </div>
          </div>
        </div>

        <div className={`rounded-xl p-[2px] ${hasSufficientBalance ? '' : 'bg-[linear-gradient(#fff,#fff),linear-gradient(135deg,rgba(248,113,113,0.3),rgba(252,165,165,0.2))] bg-origin-border bg-clip-[padding-box,border-box]'}`}>
          <div className={`rounded-[10px] p-4 ${hasSufficientBalance ? 'elevated-surface' : 'border border-red-200 bg-red-50'}`}>
            <div className="mb-3 flex items-center gap-2">
              <Wallet className={`h-4 w-4 ${hasSufficientBalance ? 'text-[#6b7280]' : 'text-red-500'}`} />
              <span className="text-sm font-semibold text-[#111111]">보유 CTK 잔액</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-2xl font-bold text-[#111111]">{user.ctkBalance.toLocaleString()}</span>
              <span className="text-sm font-medium text-muted-foreground">CTK</span>
            </div>
            {!hasSufficientBalance && (
              <div className="mt-3 flex items-center gap-2">
                <AlertCircle className="h-4 w-4 text-red-500" />
                <p className="text-xs text-red-500">
                  잔액이 부족해요. {(totalPrice - user.ctkBalance).toLocaleString()} CTK 이상 충전한 뒤 다시 시도해 주세요.
                </p>
              </div>
            )}
          </div>
        </div>

        <div className="elevated-surface rounded-xl p-4">
          <p className="mb-3 text-xs text-muted-foreground">결제를 진행하면 선택한 좌석이 확정되고 티켓이 즉시 발급돼요.</p>
          <button
            onClick={() => setApproved(true)}
            disabled={!hasSufficientBalance || approved}
            className={`w-full rounded-xl py-3 text-sm font-semibold transition-all disabled:cursor-not-allowed disabled:opacity-40 ${
              approved
                ? 'gradient-border-button text-[#111111]'
                : 'elevated-surface text-[#111111] hover:opacity-90'
            }`}
          >
            {approved ? (
              <span className="flex items-center justify-center gap-2">
                <CheckCircle className="h-4 w-4" />
                결제 승인 완료
              </span>
            ) : (
              '결제 승인'
            )}
          </button>
        </div>

        <div className="elevated-surface-soft flex flex-col gap-3 rounded-xl p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <FlaskConical className="h-4 w-4 text-muted-foreground" />
              <span className="text-xs font-semibold text-muted-foreground">테스트용 실패 시뮬레이션</span>
            </div>
            <button
              onClick={() => setSimulateFailure((value) => !value)}
              className={`relative h-5 w-10 rounded-full transition-colors ${simulateFailure ? 'bg-[#9aa4b2]' : 'bg-muted-foreground/30'}`}
              role="switch"
              aria-checked={simulateFailure}
            >
              <span
                className={`absolute top-0.5 h-4 w-4 rounded-full bg-white shadow transition-transform ${simulateFailure ? 'translate-x-5' : 'translate-x-0.5'}`}
              />
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
                        ? 'border-[#cfd6df] bg-white text-[#111111]'
                        : 'border-[#e5e7eb] bg-transparent text-muted-foreground hover:text-foreground'
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
          className="gradient-border-button w-full rounded-xl py-4 text-sm font-semibold text-[#111111] transition-all active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-40"
        >
          {simulateFailure ? '실패 케이스로 진행' : '결제 진행하기'}
        </button>

        <p className="text-center text-xs text-muted-foreground">결제가 완료되면 선택한 좌석 정보가 NFT 티켓으로 발급되고, 내 티켓에서 바로 확인할 수 있어요.</p>
      </div>
    </AppShell>
  )
}
