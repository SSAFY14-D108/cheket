'use client'

import Image from 'next/image'
import { Armchair, Banknote, CheckCircle2, Clock, Hash, Share2, User } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'

export function TransferCompleteScreen() {
  const { navParams, navigate, navigateTab, tickets } = useApp()
  const ticket = tickets.find((t) => t.id === navParams.ticketId)
  const recipientName = (navParams.recipientName as string | undefined) ?? '받는 사람'
  const txHash = `0x${Math.random().toString(16).slice(2, 9)}...${Math.random().toString(16).slice(2, 7)}`

  if (!ticket) return null

  return (
    <AppShell
      title="양도 완료"
      showBottomNav={false}
      rightElement={
        <button className="p-1 text-muted-foreground transition-colors hover:text-foreground" aria-label="공유">
          <Share2 className="h-5 w-5" />
        </button>
      }
    >
      <div className="flex flex-col gap-5 p-4">
        <div className="flex flex-col items-center gap-3 py-4">
          <div className="relative h-24 w-24">
            <div className="absolute left-2 top-0 h-2 w-2 rounded-full bg-primary/40" />
            <div className="absolute right-1 top-3 h-1.5 w-1.5 rounded-full bg-primary/25" />
            <div className="absolute bottom-2 left-0 h-1.5 w-1.5 rounded-full bg-primary/30" />
            <div className="absolute bottom-0 right-3 h-2.5 w-2.5 rounded-full bg-primary/20" />
            <div className="flex h-24 w-24 items-center justify-center rounded-full border-2 border-primary/40 bg-primary/10">
              <div className="flex h-16 w-16 items-center justify-center rounded-full bg-primary shadow-lg shadow-primary/30">
                <CheckCircle2 className="h-8 w-8 text-primary-foreground" />
              </div>
            </div>
          </div>
          <div className="text-center">
            <h2 className="text-xl font-bold text-foreground">양도가 완료되었어요</h2>
            <p className="mt-1 text-sm text-muted-foreground">티켓 소유권이 수신자에게 정상적으로 전달되었어요.</p>
          </div>
        </div>

        <div className="overflow-hidden rounded-2xl border border-border bg-card">
          <div className="flex items-center gap-3 border-b border-border p-4">
            <div className="relative h-12 w-12 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={ticket.poster} alt={ticket.eventName} fill className="object-cover" sizes="48px" />
            </div>
            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground">Concert</p>
              <p className="text-sm font-bold leading-tight text-foreground">{ticket.eventName}</p>
            </div>
          </div>

          <div className="divide-y divide-border">
            <div className="flex items-center justify-between px-4 py-3">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Armchair className="h-4 w-4" />
                <span>좌석</span>
              </div>
              <span className="text-sm font-semibold text-foreground">
                {ticket.grade} {ticket.seatLabel}
              </span>
            </div>
            <div className="flex items-center justify-between px-4 py-3">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <User className="h-4 w-4" />
                <span>받는 사람</span>
              </div>
              <div className="flex items-center gap-1.5">
                <div className="flex h-5 w-5 items-center justify-center rounded-full bg-primary/20">
                  <User className="h-3 w-3 text-primary" />
                </div>
                <span className="text-sm font-semibold text-foreground">{recipientName} 님</span>
              </div>
            </div>
            <div className="flex items-center justify-between px-4 py-3">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Banknote className="h-4 w-4" />
                <span>양도 비용</span>
              </div>
              <span className="text-sm font-semibold text-primary">무료</span>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-3 rounded-2xl bg-secondary p-4">
          <div>
            <p className="mb-1.5 text-[10px] font-bold uppercase tracking-widest text-muted-foreground">Transaction Hash</p>
            <div className="flex items-center justify-between">
              <p className="text-xs font-mono text-foreground">{txHash}</p>
              <button className="ml-2 text-xs font-medium text-primary">복사</button>
            </div>
          </div>
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-1.5">
              <Hash className="h-3.5 w-3.5 text-primary" />
              <div>
                <p className="text-[10px] text-muted-foreground">NFT 상태</p>
                <p className="text-xs font-semibold text-primary">전송 완료</p>
              </div>
            </div>
            <div className="flex items-center gap-1.5">
              <Clock className="h-3.5 w-3.5 text-muted-foreground" />
              <div>
                <p className="text-[10px] text-muted-foreground">처리 시간</p>
                <p className="text-xs font-semibold text-foreground">1.8초</p>
              </div>
            </div>
          </div>
        </div>

        <p className="px-2 text-center text-xs text-muted-foreground">{recipientName} 님에게 양도 알림이 전송되었어요.</p>
        <div className="mt-auto flex flex-col gap-3">
          <button
            onClick={() => navigate('my-tickets')}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-primary py-4 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98]"
          >
            내 티켓 보기
          </button>
          <button
            onClick={() => navigateTab('home')}
            className="w-full rounded-xl bg-transparent py-3 text-sm font-semibold text-muted-foreground transition-all hover:text-foreground active:scale-[0.98]"
          >
            홈으로 돌아가기
          </button>
        </div>
      </div>
    </AppShell>
  )
}
