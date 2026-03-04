'use client'

import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { CheckCircle, Calendar, MapPin, Ticket, ArrowRight, Wallet } from 'lucide-react'

export function ResalePurchaseCompleteScreen() {
  const { navParams, navigate, tickets, user } = useApp()

  const ticket = tickets.find((t) => t.id === navParams.purchasedTicketId)

  if (!ticket || !user) return null

  const discount = ticket.resalePrice
    ? Math.round(((ticket.originalPrice - ticket.resalePrice) / ticket.originalPrice) * 100)
    : 0
  const paidPrice = ticket.resalePrice ?? ticket.originalPrice
  const txHash = `0x${ticket.id.replace(/[^a-zA-Z0-9]/g, '').slice(0, 12).toLowerCase()}`

  return (
    <div className="flex flex-col h-full bg-background overflow-y-auto">

      {/* Top success banner */}
      <div className="flex flex-col items-center pt-10 pb-6 px-6 bg-white">
        <div className="relative">
          <div className="w-20 h-20 rounded-full bg-primary/10 flex items-center justify-center">
            <CheckCircle className="w-10 h-10 text-primary" strokeWidth={2} />
          </div>
          {/* pulse ring */}
          <span className="absolute inset-0 rounded-full border-2 border-primary/30 animate-ping" />
        </div>
        <h1 className="font-bold text-xl text-foreground mt-5 text-balance text-center">
          구매가 완료되었습니다
        </h1>
        <p className="text-sm text-muted-foreground mt-1 text-center">
          NFT 티켓 소유권이 내 지갑으로 이전되었습니다
        </p>
      </div>

      <div className="flex flex-col gap-4 px-4 pb-8 pt-2">

        {/* Ticket card */}
        <div className="bg-card border border-border rounded-2xl overflow-hidden shadow-sm">
          {/* Poster strip */}
          <div className="relative w-full h-36">
            <Image
              src={ticket.poster}
              alt={ticket.eventName}
              fill
              className="object-cover"
              sizes="390px"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
            <div className="absolute bottom-3 left-4 right-4">
              <p className="text-white font-bold text-base leading-snug line-clamp-1">{ticket.eventName}</p>
            </div>
          </div>

          {/* Dashed divider with NFT badge */}
          <div className="relative flex items-center px-4 py-0">
            <div className="flex-1 border-t border-dashed border-border" />
            <span className="mx-3 text-[10px] font-bold text-primary bg-primary/10 px-2 py-0.5 rounded-full">
              NFT
            </span>
            <div className="flex-1 border-t border-dashed border-border" />
          </div>

          {/* Ticket details */}
          <div className="px-4 py-4 flex flex-col gap-3">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Calendar className="w-4 h-4 text-primary flex-shrink-0" />
              <span>{ticket.eventDate}</span>
            </div>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <MapPin className="w-4 h-4 text-primary flex-shrink-0" />
              <span>{ticket.venue}</span>
            </div>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Ticket className="w-4 h-4 text-primary flex-shrink-0" />
              <span>{ticket.seatLabel} · {ticket.grade}</span>
            </div>
          </div>
        </div>

        {/* Payment summary */}
        <div className="bg-card border border-border rounded-2xl px-4 py-4 flex flex-col gap-2.5">
          <h3 className="font-semibold text-sm text-foreground mb-0.5">결제 내역</h3>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">원가</span>
            <span className="text-muted-foreground line-through">{ticket.originalPrice.toLocaleString()} CTK</span>
          </div>
          {discount > 0 && (
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">할인</span>
              <span className="text-primary font-medium">-{discount}%</span>
            </div>
          )}
          <div className="h-px bg-border" />
          <div className="flex justify-between">
            <span className="font-semibold text-sm text-foreground">실제 결제</span>
            <span className="font-bold text-primary text-base">{paidPrice.toLocaleString()} CTK</span>
          </div>
          <div className="flex items-center gap-2 mt-1 pt-2 border-t border-border">
            <Wallet className="w-3.5 h-3.5 text-muted-foreground" />
            <span className="text-xs text-muted-foreground">잔액</span>
            <span className="ml-auto text-xs font-semibold text-foreground">{user.ctkBalance.toLocaleString()} CTK</span>
          </div>
        </div>

        {/* Blockchain info */}
        <div className="bg-secondary rounded-2xl px-4 py-4">
          <p className="text-xs font-semibold text-foreground mb-2">블록체인 처리 정보</p>
          <div className="flex flex-col gap-1.5">
            <div className="flex justify-between gap-4">
              <span className="text-xs text-muted-foreground flex-shrink-0">에스크로 상태</span>
              <span className="text-xs font-medium text-green-600">완료 (Confirmed)</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-xs text-muted-foreground flex-shrink-0">TX Hash</span>
              <span className="text-xs font-mono text-foreground truncate">{txHash}…</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-xs text-muted-foreground flex-shrink-0">네트워크</span>
              <span className="text-xs font-medium text-foreground">Cheket Chain</span>
            </div>
          </div>
        </div>

        {/* CTA buttons */}
        <button
          onClick={() => navigate('ticket-detail', { ticketId: ticket.id })}
          className="w-full flex items-center justify-center gap-2 bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all"
        >
          티켓 확인하기
          <ArrowRight className="w-4 h-4" />
        </button>
        <button
          onClick={() => navigate('my-tickets')}
          className="w-full bg-secondary text-foreground font-medium py-3.5 rounded-xl text-sm hover:bg-border transition-colors"
        >
          내 티켓 목록
        </button>
      </div>
    </div>
  )
}
