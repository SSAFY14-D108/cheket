'use client'

import { useState } from 'react'
import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { MOCK_EVENTS } from '@/lib/mock-data'
import { Ticket, PurchaseFailureReason } from '@/lib/types'
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
  const { navParams, navigate, goBack, user, addTicket } = useApp()
  const event = MOCK_EVENTS.find((e) => e.id === navParams.eventId)
  const seats = navParams.seats ?? []
  const totalPrice = navParams.totalPrice ?? 0
  const [approved, setApproved] = useState(false)
  const [success, setSuccess] = useState(false)
  const [simulateFailure, setSimulateFailure] = useState(false)
  const [selectedReason, setSelectedReason] = useState<PurchaseFailureReason>('SOLD_OUT')

  if (!event || !user) return null

  const hasSufficientBalance = user.ctkBalance >= totalPrice

  const handleApprove = () => setApproved(true)

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
        seatLabel: `${seat.row}열 ${seat.number}번`,
        grade: seat.grade,
        originalPrice: seat.price,
        status: 'SOLD',
      }
      addTicket(ticket)
    })
    setSuccess(true)
  }

  if (success) {
    return (
      <div className="min-h-full flex flex-col items-center justify-center bg-background p-6 gap-6 text-center">
        <div className="w-20 h-20 rounded-full bg-primary/10 border-2 border-primary flex items-center justify-center">
          <CheckCircle className="w-10 h-10 text-primary" />
        </div>
        <div>
          <h2 className="font-bold text-xl text-foreground mb-2">구매 완료!</h2>
          <p className="text-muted-foreground text-sm">NFT 티켓이 발급되었습니다.<br />내 티켓에서 확인하세요.</p>
        </div>
        
        {/* Concert info card with image */}
        <div className="w-full bg-card border border-border rounded-2xl overflow-hidden text-left">
          {/* Concert poster */}
          <div className="relative w-full aspect-[16/9]">
            <Image
              src={event.poster} 
              alt={event.name}
              fill
              sizes="390px"
              className="object-cover"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
            <div className="absolute bottom-3 left-4 right-4">
              <h3 className="font-bold text-white text-lg drop-shadow-lg">{event.name}</h3>
            </div>
          </div>
          
          {/* Event details */}
          <div className="p-4 flex flex-col gap-3">
            <div className="flex items-center gap-3 text-sm">
              <div className="flex-1">
                <p className="text-xs text-muted-foreground mb-0.5">일시</p>
                <p className="font-medium text-foreground">{event.date}</p>
              </div>
              <div className="w-px h-8 bg-border" />
              <div className="flex-1">
                <p className="text-xs text-muted-foreground mb-0.5">장소</p>
                <p className="font-medium text-foreground truncate">{event.venue.split(',')[0]}</p>
              </div>
            </div>
            
            <div className="h-px bg-border" />
            
            {/* Seat info */}
            <div>
              <p className="text-xs text-muted-foreground mb-2">예매 좌석</p>
              <div className="flex flex-col gap-1.5">
                {seats.map((s) => (
                  <div key={s.id} className="flex items-center justify-between bg-secondary/50 rounded-lg px-3 py-2">
                    <span className="text-sm font-medium text-foreground">{s.row}열 {s.number}번</span>
                    <span className="text-xs text-primary font-semibold px-2 py-0.5 bg-primary/10 rounded-full">{s.grade}</span>
                  </div>
                ))}
              </div>
            </div>
            
            {/* Total */}
            <div className="flex items-center justify-between pt-2 border-t border-border">
              <span className="text-sm text-muted-foreground">결제 금액</span>
              <span className="font-bold text-primary">{totalPrice.toLocaleString()} CTK</span>
            </div>
          </div>
        </div>
        
        <button
          onClick={() => navigate('my-tickets')}
          className="w-full bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all"
        >
          내 티켓 보기
        </button>
      </div>
    )
  }

  return (
    <AppShell showBack onBack={goBack} title="결제" showBottomNav={false}>
      <div className="p-4 flex flex-col gap-4">
        {/* Event summary */}
        <div className="bg-card rounded-xl border border-border p-4">
          <h3 className="font-semibold text-sm text-foreground mb-3">{event.name}</h3>
          <div className="flex flex-col gap-2">
            {seats.map((seat) => (
              <div key={seat.id} className="flex items-center justify-between">
                <div>
                  <span className="text-sm text-foreground">{seat.row}열 {seat.number}번</span>
                  <span className="text-xs text-muted-foreground ml-2">({seat.grade})</span>
                </div>
                <span className="text-sm font-medium text-foreground">{seat.price.toLocaleString()} CTK</span>
              </div>
            ))}
            <div className="h-px bg-border my-1" />
            <div className="flex items-center justify-between">
              <span className="font-semibold text-sm text-foreground">총 결제 금액</span>
              <span className="font-bold text-primary text-base">{totalPrice.toLocaleString()} CTK</span>
            </div>
          </div>
        </div>

        {/* Wallet balance */}
        <div className={`rounded-xl border p-4 ${hasSufficientBalance ? 'bg-card border-border' : 'bg-red-500/10 border-red-500/30'}`}>
          <div className="flex items-center gap-2 mb-3">
            <Wallet className="w-4 h-4 text-primary" />
            <span className="font-semibold text-sm text-foreground">내 CTK 잔액</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-2xl font-bold text-foreground">{user.ctkBalance.toLocaleString()}</span>
            <span className="text-sm font-medium text-muted-foreground">CTK</span>
          </div>
          {!hasSufficientBalance && (
            <div className="flex items-center gap-2 mt-3">
              <AlertCircle className="w-4 h-4 text-red-400" />
              <p className="text-xs text-red-400">
                잔액이 부족합니다. {(totalPrice - user.ctkBalance).toLocaleString()} CTK 더 필요합니다.
              </p>
            </div>
          )}
        </div>

        {/* Approve step */}
        <div className="bg-card rounded-xl border border-border p-4">
          <p className="text-xs text-muted-foreground mb-3">
            CTK 사용을 승인하면 스마트 컨트랙트가 지갑에서 결제 금액을 처리합니다.
          </p>
          <button
            onClick={handleApprove}
            disabled={!hasSufficientBalance || approved}
            className={`w-full py-3 rounded-xl text-sm font-semibold transition-all ${
              approved
                ? 'bg-primary/10 text-primary border border-primary/30'
                : 'bg-secondary border border-border text-foreground hover:border-primary/50 disabled:opacity-40 disabled:cursor-not-allowed'
            }`}
          >
            {approved ? (
              <span className="flex items-center justify-center gap-2">
                <CheckCircle className="w-4 h-4" /> Approve 완료
              </span>
            ) : 'Approve'}
          </button>
        </div>

        {/* Failure simulation panel (dev/demo) */}
        <div className="bg-secondary border border-border rounded-xl p-4 flex flex-col gap-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <FlaskConical className="w-4 h-4 text-muted-foreground" />
              <span className="text-xs font-semibold text-muted-foreground">실패 시뮬레이션 (데모용)</span>
            </div>
            <button
              onClick={() => setSimulateFailure((v) => !v)}
              className={`relative w-10 h-5 rounded-full transition-colors ${simulateFailure ? 'bg-primary' : 'bg-muted-foreground/30'}`}
              role="switch"
              aria-checked={simulateFailure}
            >
              <span className={`absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-transform ${simulateFailure ? 'translate-x-5' : 'translate-x-0.5'}`} />
            </button>
          </div>
          {simulateFailure && (
            <div className="flex flex-col gap-2">
              <p className="text-xs text-muted-foreground">실패 유형 선택:</p>
              <div className="flex flex-wrap gap-1.5">
                {FAILURE_REASONS.map((r) => (
                  <button
                    key={r}
                    onClick={() => setSelectedReason(r)}
                    className={`text-xs px-2.5 py-1 rounded-full border font-medium transition-colors ${
                      selectedReason === r
                        ? 'bg-primary text-primary-foreground border-primary'
                        : 'bg-card border-border text-muted-foreground hover:text-foreground'
                    }`}
                  >
                    {r}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        <button
          onClick={handleConfirm}
          disabled={!approved || !hasSufficientBalance}
          className="w-full bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed"
        >
          {simulateFailure ? '구매 확정 (실패 시뮬레이션)' : '구매 확정'}
        </button>

        <p className="text-xs text-muted-foreground text-center">
          구매 확정 후 NFT 티켓이 즉시 발급됩니다. 환불은 불가합니다.
        </p>
      </div>
    </AppShell>
  )
}
