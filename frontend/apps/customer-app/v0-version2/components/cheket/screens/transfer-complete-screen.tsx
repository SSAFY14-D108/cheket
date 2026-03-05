'use client'

import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { CheckCircle2, Share2, User, Armchair, Banknote, Hash, Clock } from 'lucide-react'

export function TransferCompleteScreen() {
  const { navParams, navigate, navigateTab, tickets } = useApp()
  const ticket = tickets.find((t) => t.id === navParams.ticketId)
  const recipientName = navParams.recipientName ?? '—'
  const txHash = `0x${Math.random().toString(16).slice(2, 9)}...${Math.random().toString(16).slice(2, 7)}`

  if (!ticket) return null

  return (
    <AppShell
      title="양도 완료"
      showBottomNav={false}
      rightElement={
        <button className="p-1 text-muted-foreground hover:text-foreground transition-colors">
          <Share2 className="w-5 h-5" />
        </button>
      }
    >
      <div className="flex flex-col gap-5 p-4">

        {/* Success icon + title */}
        <div className="flex flex-col items-center gap-3 py-4">
          {/* Scattered dots decoration */}
          <div className="relative w-24 h-24">
            <div className="absolute top-0 left-2 w-2 h-2 rounded-full bg-primary/40" />
            <div className="absolute top-3 right-1 w-1.5 h-1.5 rounded-full bg-primary/25" />
            <div className="absolute bottom-2 left-0 w-1.5 h-1.5 rounded-full bg-primary/30" />
            <div className="absolute bottom-0 right-3 w-2.5 h-2.5 rounded-full bg-primary/20" />
            <div className="w-24 h-24 rounded-full bg-primary/10 border-2 border-primary/40 flex items-center justify-center">
              <div className="w-16 h-16 rounded-full bg-primary flex items-center justify-center shadow-lg shadow-primary/30">
                <CheckCircle2 className="w-8 h-8 text-primary-foreground" />
              </div>
            </div>
          </div>
          <div className="text-center">
            <h2 className="font-bold text-xl text-foreground">양도 완료되었습니다!</h2>
            <p className="text-sm text-muted-foreground mt-1">티켓이 성공적으로 이전되었습니다</p>
          </div>
        </div>

        {/* Ticket info card */}
        <div className="bg-card border border-border rounded-2xl overflow-hidden">
          {/* Poster strip */}
          <div className="flex items-center gap-3 p-4 border-b border-border">
            <div className="relative w-12 h-12 rounded-xl overflow-hidden flex-shrink-0 bg-secondary">
              <Image src={ticket.poster} alt={ticket.eventName} fill className="object-cover" sizes="48px" />
            </div>
            <div>
              <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest">Concert</p>
              <p className="font-bold text-sm text-foreground leading-tight">{ticket.eventName}</p>
            </div>
          </div>

          {/* Detail rows */}
          <div className="divide-y divide-border">
            <div className="flex items-center justify-between px-4 py-3">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Armchair className="w-4 h-4" />
                <span>착석</span>
              </div>
              <span className="text-sm font-semibold text-foreground">{ticket.grade} {ticket.seatLabel}</span>
            </div>
            <div className="flex items-center justify-between px-4 py-3">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <User className="w-4 h-4" />
                <span>받는 사람</span>
              </div>
              <div className="flex items-center gap-1.5">
                <div className="w-5 h-5 rounded-full bg-primary/20 flex items-center justify-center">
                  <User className="w-3 h-3 text-primary" />
                </div>
                <span className="text-sm font-semibold text-foreground">{recipientName} 님</span>
              </div>
            </div>
            <div className="flex items-center justify-between px-4 py-3">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Banknote className="w-4 h-4" />
                <span>양도 금액</span>
              </div>
              <span className="text-sm font-semibold text-primary">무료 (Free)</span>
            </div>
          </div>
        </div>

        {/* Transaction info */}
        <div className="bg-secondary rounded-2xl p-4 flex flex-col gap-3">
          <div>
            <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-1.5">Transaction Hash</p>
            <div className="flex items-center justify-between">
              <p className="text-xs font-mono text-foreground">{txHash}</p>
              <button className="text-xs text-primary font-medium ml-2">복사</button>
            </div>
          </div>
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-1.5">
              <Hash className="w-3.5 h-3.5 text-primary" />
              <div>
                <p className="text-[10px] text-muted-foreground">NFT 소유권</p>
                <p className="text-xs font-semibold text-primary">이전 완료</p>
              </div>
            </div>
            <div className="flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5 text-muted-foreground" />
              <div>
                <p className="text-[10px] text-muted-foreground">처리 시간</p>
                <p className="text-xs font-semibold text-foreground">1.8초</p>
              </div>
            </div>
          </div>
        </div>

        {/* Notice */}
        <p className="text-xs text-center text-muted-foreground px-2">
          {recipientName} 님에게 양도 알림이 전송되었습니다.
        </p>

        {/* CTA */}
        <div className="flex flex-col gap-3 mt-auto">
          <button
            onClick={() => navigate('my-tickets')}
            className="w-full bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all flex items-center justify-center gap-2"
          >
            내 티켓 확인하기
          </button>
          <button
            onClick={() => navigateTab('home')}
            className="w-full bg-transparent text-muted-foreground font-semibold py-3 rounded-xl text-sm hover:text-foreground active:scale-[0.98] transition-all"
          >
            홈으로 돌아가기
          </button>
        </div>
      </div>
    </AppShell>
  )
}
