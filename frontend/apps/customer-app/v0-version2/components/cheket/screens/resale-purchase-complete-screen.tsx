'use client'

import { useRef } from 'react'
import Image from 'next/image'
import { ArrowRight, Calendar, CheckCircle, MapPin, Ticket, Wallet } from 'lucide-react'
import { useApp } from '@/lib/app-context'

function mockTxHash() {
  return `0x${Array.from({ length: 12 }, () => Math.floor(Math.random() * 16).toString(16)).join('')}`
}

export function ResalePurchaseCompleteScreen() {
  const { navParams, navigate, tickets, user } = useApp()
  const txHash = useRef(mockTxHash())
  const ticket = tickets.find((t) => t.id === navParams.purchasedTicketId)

  if (!ticket || !user) return null

  const discount = ticket.resalePrice ? Math.round(((ticket.originalPrice - ticket.resalePrice) / ticket.originalPrice) * 100) : 0
  const paidPrice = ticket.resalePrice ?? ticket.originalPrice

  return (
    <div className="flex h-full flex-col overflow-y-auto bg-background">
      <div className="bg-white px-6 pb-6 pt-10">
        <div className="flex flex-col items-center">
          <div className="relative">
            <div className="gradient-border-icon-button flex h-20 w-20 items-center justify-center rounded-full">
              <CheckCircle className="h-10 w-10 text-[#333333]" strokeWidth={2} />
            </div>
            <span className="absolute inset-0 animate-ping rounded-full border-2 border-[#d6dde9]" />
          </div>
          <h1 className="mt-5 text-center text-xl font-bold text-[#111111]">재판매 구매가 완료되었어요</h1>
          <p className="mt-1 text-center text-sm text-muted-foreground">NFT 티켓 소유권 이전까지 정상적으로 처리되었습니다.</p>
        </div>
      </div>

      <div className="flex flex-col gap-4 px-4 pb-8 pt-2">
        <div className="elevated-surface-soft overflow-hidden rounded-2xl shadow-sm">
          <div className="relative h-36 w-full">
            <Image src={ticket.poster} alt={ticket.eventName} fill className="object-cover" sizes="390px" />
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
            <div className="absolute bottom-3 left-4 right-4">
              <p className="line-clamp-1 text-base font-bold leading-snug text-white">{ticket.eventName}</p>
            </div>
          </div>

          <div className="relative flex items-center px-4 py-0">
            <div className="flex-1 border-t border-dashed border-border" />
            <span className="elevated-surface mx-3 rounded-full px-2 py-0.5 text-[10px] font-bold text-[#333333]">NFT</span>
            <div className="flex-1 border-t border-dashed border-border" />
          </div>

          <div className="flex flex-col gap-3 px-4 py-4">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Calendar className="h-4 w-4 flex-shrink-0 text-[#333333]" />
              <span>{ticket.eventDate}</span>
            </div>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <MapPin className="h-4 w-4 flex-shrink-0 text-[#333333]" />
              <span>{ticket.venue}</span>
            </div>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Ticket className="h-4 w-4 flex-shrink-0 text-[#333333]" />
              <span>
                {ticket.seatLabel} · {ticket.grade}
              </span>
            </div>
          </div>
        </div>

        <div className="elevated-surface-soft flex flex-col gap-2.5 rounded-2xl px-4 py-4">
          <h3 className="mb-0.5 text-sm font-semibold text-[#111111]">결제 요약</h3>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">정가</span>
            <span className="text-muted-foreground line-through">{ticket.originalPrice.toLocaleString()} CTK</span>
          </div>
          {discount > 0 && (
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">할인</span>
              <span className="font-medium text-[#333333]">-{discount}%</span>
            </div>
          )}
          <div className="h-px bg-border" />
          <div className="flex justify-between">
            <span className="text-sm font-semibold text-[#111111]">최종 결제 금액</span>
            <span className="text-base font-bold text-[#111111]">{paidPrice.toLocaleString()} CTK</span>
          </div>
          <div className="mt-1 flex items-center gap-2 border-t border-border pt-2">
            <Wallet className="h-3.5 w-3.5 text-muted-foreground" />
            <span className="text-xs text-muted-foreground">잔액</span>
            <span className="ml-auto text-xs font-semibold text-[#111111]">{user.ctkBalance.toLocaleString()} CTK</span>
          </div>
        </div>

        <div className="elevated-surface-soft rounded-2xl px-4 py-4">
          <p className="mb-2 text-xs font-semibold text-[#111111]">블록체인 처리 정보</p>
          <div className="flex flex-col gap-1.5">
            <div className="flex justify-between gap-4">
              <span className="flex-shrink-0 text-xs text-muted-foreground">트랜잭션 상태</span>
              <span className="text-xs font-medium text-[#333333]">완료 (Confirmed)</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="flex-shrink-0 text-xs text-muted-foreground">TX Hash</span>
              <span className="truncate text-xs font-mono text-[#111111]">{txHash.current}</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="flex-shrink-0 text-xs text-muted-foreground">네트워크</span>
              <span className="text-xs font-medium text-[#111111]">Cheket Chain</span>
            </div>
          </div>
        </div>

        <button
          onClick={() => navigate('ticket-detail', { ticketId: ticket.id })}
          className="gradient-border-button flex w-full items-center justify-center gap-2 rounded-xl py-4 text-sm"
        >
          티켓 상세 보기
          <ArrowRight className="h-4 w-4" />
        </button>
        <button
          onClick={() => navigate('my-tickets')}
          className="elevated-surface w-full rounded-xl py-3.5 text-sm font-medium text-foreground transition-colors"
        >
          내 티켓으로 이동
        </button>
      </div>
    </div>
  )
}
