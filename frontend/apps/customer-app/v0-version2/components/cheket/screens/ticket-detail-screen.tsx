'use client'

import { useState } from 'react'
import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { TicketStatusBadge } from '../status-badge'
import { QrCode, ArrowRightLeft, ShoppingBag, X, Calendar, MapPin, Receipt } from 'lucide-react'
import { RefundRule } from '@/lib/types'

function parseEventDate(value: string) {
  const match = value.match(/(\d{4})\.(\d{2})\.(\d{2})/)
  if (!match) return null
  const [, year, month, day] = match
  return new Date(Number(year), Number(month) - 1, Number(day), 0, 0, 0, 0)
}

function getDefaultRefundRules(): RefundRule[] {
  return [
    { id: 'default_r1', daysBefore: 7, feeRate: 0, label: '공연 7일 전까지' },
    { id: 'default_r2', daysBefore: 3, feeRate: 0.1, label: '공연 3일 전까지' },
    { id: 'default_r3', daysBefore: 1, feeRate: 0.2, label: '공연 1일 전까지' },
    { id: 'default_r4', daysBefore: 0, feeRate: 1, label: '공연 당일 이후' },
  ]
}

function getRefundPolicy(originalPrice: number, eventDateLabel: string, status: string, refundRules: RefundRule[]) {
  const eventDate = parseEventDate(eventDateLabel)
  if (!eventDate) {
    return { refundable: false, daysLeft: -1, feeRate: 1, feeAmount: originalPrice, refundAmount: 0 }
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const daysLeft = Math.floor((eventDate.getTime() - today.getTime()) / 86400000)

  const appliedRule =
    [...refundRules].sort((a, b) => b.daysBefore - a.daysBefore).find((rule) => daysLeft >= rule.daysBefore) ??
    refundRules[refundRules.length - 1]
  const feeRate = appliedRule?.feeRate ?? 1

  const refundable = status === 'SOLD' && daysLeft >= 1
  const feeAmount = refundable ? Math.floor(originalPrice * feeRate) : originalPrice
  const refundAmount = refundable ? originalPrice - feeAmount : 0

  return { refundable, daysLeft, feeRate, feeAmount, refundAmount }
}

export function TicketDetailScreen() {
  const { navParams, navigate, goBack, tickets, refundTicket, events } = useApp()
  const [showRefundConfirm, setShowRefundConfirm] = useState(false)
  const ticket = tickets.find((t) => t.id === navParams.ticketId)
  const event = events.find((item) => item.id === ticket?.eventId)

  if (!ticket) return null

  const refundRules = event?.refundRules ?? getDefaultRefundRules()
  const refundPolicy = getRefundPolicy(ticket.originalPrice, ticket.eventDate, ticket.status, refundRules)

  const handleRefundConfirm = () => {
    const result = refundTicket(ticket.id)
    if (result.success) {
      setShowRefundConfirm(false)
      goBack()
    }
  }

  return (
    <AppShell showBack onBack={goBack} title="티켓 상세" showBottomNav={false}>
      <div className="relative flex flex-col gap-4 p-4">
        <div className="relative w-full aspect-video rounded-xl overflow-hidden bg-secondary">
          <Image src={ticket.poster} alt={ticket.eventName} fill className="object-cover" sizes="390px" />
          <div className="absolute inset-0 bg-gradient-to-t from-background/90 via-transparent to-transparent" />
          <div className="absolute bottom-3 left-3">
            <TicketStatusBadge status={ticket.status} />
          </div>
        </div>

        <div className="bg-card rounded-xl border border-border p-4 flex flex-col gap-3">
          <h2 className="font-bold text-base text-foreground">{ticket.eventName}</h2>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Calendar className="w-4 h-4 text-primary" />
            <span>{ticket.eventDate}</span>
          </div>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <MapPin className="w-4 h-4 text-primary" />
            <span>{ticket.venue}</span>
          </div>
          <div className="h-px bg-border" />
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">좌석</span>
            <span className="text-sm font-semibold text-foreground">{ticket.seatLabel}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">등급</span>
            <span className="text-sm font-semibold text-primary">{ticket.grade}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">결제 금액</span>
            <span className="text-sm font-semibold text-foreground">{ticket.originalPrice.toLocaleString()} CTK</span>
          </div>
          {ticket.resalePrice && (
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">재판매 금액</span>
              <span className="text-sm font-semibold text-orange-500">{ticket.resalePrice.toLocaleString()} CTK</span>
            </div>
          )}
        </div>

        <div className="bg-secondary rounded-xl p-4 text-xs text-muted-foreground">
          <p className="font-semibold text-foreground mb-1.5">NFT 정보</p>
          <p>Token ID: #{ticket.id.split('_').pop()?.toUpperCase()}</p>
          <p className="mt-1">Owner: 0x3a9F...dE42</p>
        </div>

        <div className="flex flex-col gap-3 mt-2">
          {ticket.status === 'SOLD' && (
            <>
              <button
                onClick={() => navigate('qr-checkin', { ticketId: ticket.id })}
                className="w-full flex items-center justify-center gap-2 bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all"
              >
                <QrCode className="w-4 h-4" /> QR 체크인
              </button>
              <div className="grid grid-cols-2 gap-3">
                <button
                  onClick={() => navigate('transfer', { ticketId: ticket.id })}
                  className="flex items-center justify-center gap-2 bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm hover:border-primary/50 active:scale-[0.98] transition-all"
                >
                  <ArrowRightLeft className="w-4 h-4" /> 양도하기
                </button>
                <button
                  onClick={() => navigate('resale-create', { ticketId: ticket.id })}
                  className="flex items-center justify-center gap-2 bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm hover:border-primary/50 active:scale-[0.98] transition-all"
                >
                  <ShoppingBag className="w-4 h-4" /> 판매하기
                </button>
              </div>
              <button
                onClick={() => setShowRefundConfirm(true)}
                disabled={!refundPolicy.refundable}
                className="w-full flex items-center justify-center gap-2 bg-amber-500/15 border border-amber-500/30 text-amber-700 font-semibold py-3.5 rounded-xl text-sm hover:bg-amber-500/20 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <Receipt className="w-4 h-4" /> 환불하기
              </button>
            </>
          )}
          {ticket.status === 'LISTED' && (
            <CancelListingButton ticketId={ticket.id} />
          )}
          {ticket.status === 'USED' && (
            <button
              disabled
              className="w-full flex items-center justify-center gap-2 bg-secondary text-muted-foreground font-semibold py-3.5 rounded-xl text-sm cursor-not-allowed opacity-60"
            >
              <QrCode className="w-4 h-4" /> 사용 완료
            </button>
          )}
        </div>

        {showRefundConfirm && (
          <div className="fixed inset-0 z-50 bg-black/45 backdrop-blur-[2px] flex items-end sm:items-center justify-center p-4">
            <div className="w-full max-w-sm rounded-2xl bg-card border border-border shadow-2xl p-5 flex flex-col gap-4">
              <div>
                <h3 className="text-lg font-bold text-foreground">정말 환불하시겠습니까?</h3>
                <p className="text-sm text-muted-foreground mt-1">
                  환불 처리 후 티켓은 내 티켓 목록에서 사라집니다.
                </p>
              </div>

              <div className="rounded-xl border border-border p-4">
                <div className="flex items-center gap-2 mb-3">
                  <Receipt className="w-4 h-4 text-primary" />
                  <h4 className="font-semibold text-sm text-foreground">환불 규정</h4>
                </div>
                <div className="grid grid-cols-1 gap-2 text-sm">
                  {refundRules.map((rule) => (
                    <div key={rule.id} className="flex items-center justify-between text-muted-foreground">
                      <span>{rule.label}</span>
                      <span>{rule.feeRate >= 1 ? '환불 불가' : `수수료 ${Math.round(rule.feeRate * 100)}%`}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="rounded-xl bg-secondary p-4 space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted-foreground">결제 금액</span>
                  <span className="font-medium text-foreground">{ticket.originalPrice.toLocaleString()} CTK</span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted-foreground">수수료 ({Math.round(refundPolicy.feeRate * 100)}%)</span>
                  <span className="font-medium text-foreground">-{refundPolicy.feeAmount.toLocaleString()} CTK</span>
                </div>
                <div className="h-px bg-border" />
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-foreground">최종 환불 금액</span>
                  <span className="font-bold text-primary">{refundPolicy.refundAmount.toLocaleString()} CTK</span>
                </div>
              </div>

              <p className="text-xs text-muted-foreground">
                공연일까지 {refundPolicy.daysLeft}일 남아 있어 현재 규정이 적용됩니다.
              </p>

              <div className="grid grid-cols-2 gap-3">
                <button
                  onClick={() => setShowRefundConfirm(false)}
                  className="w-full py-3 rounded-xl border border-border bg-secondary text-foreground font-semibold text-sm hover:border-primary/40 transition-all"
                >
                  취소
                </button>
                <button
                  onClick={handleRefundConfirm}
                  className="w-full py-3 rounded-xl bg-amber-500 text-white font-semibold text-sm hover:opacity-90 transition-all"
                >
                  환불 확정
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </AppShell>
  )
}

function CancelListingButton({ ticketId }: { ticketId: string }) {
  const { updateTicketStatus, removeResaleItem, resaleItems, goBack } = useApp()

  const handleCancel = () => {
    const resaleItem = resaleItems.find((r) => r.ticketId === ticketId)
    if (resaleItem) removeResaleItem(resaleItem.id)
    updateTicketStatus(ticketId, { status: 'SOLD', resalePrice: undefined })
    goBack()
  }

  return (
    <button
      onClick={handleCancel}
      className="w-full flex items-center justify-center gap-2 bg-red-600/20 border border-red-600/30 text-red-400 font-semibold py-3.5 rounded-xl text-sm hover:bg-red-600/30 active:scale-[0.98] transition-all"
    >
      <X className="w-4 h-4" /> 판매 등록 취소
    </button>
  )
}
