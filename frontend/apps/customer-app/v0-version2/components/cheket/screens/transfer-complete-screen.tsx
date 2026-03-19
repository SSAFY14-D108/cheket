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
        <button className="neutral-icon-button rounded-full" aria-label="공유">
          <Share2 className="h-5 w-5" />
        </button>
      }
    >
      <div className="flex flex-col gap-5 p-4">
        <div className="flex flex-col items-center gap-3 py-4">
          <div className="relative h-24 w-24">
            <div className="absolute left-2 top-0 h-2 w-2 rounded-full bg-[#d9ccff]" />
            <div className="absolute right-1 top-3 h-1.5 w-1.5 rounded-full bg-[#c5e2ff]" />
            <div className="absolute bottom-2 left-0 h-1.5 w-1.5 rounded-full bg-[#c4f7e0]" />
            <div className="absolute bottom-0 right-3 h-2.5 w-2.5 rounded-full bg-[#dfe7ff]" />
            <div className="gradient-border-icon-button flex h-24 w-24 items-center justify-center rounded-full">
              <div className="gradient-border-icon-button flex h-16 w-16 items-center justify-center rounded-full bg-white">
                <CheckCircle2 className="h-8 w-8 text-[#333333]" />
              </div>
            </div>
          </div>
          <div className="text-center">
            <h2 className="text-xl font-bold text-[#111111]">티켓 양도가 완료됐어요</h2>
            <p className="mt-1 text-sm text-muted-foreground">블록체인 기록과 함께 소유권 이전이 정상적으로 처리됐습니다.</p>
          </div>
        </div>

        <div className="elevated-surface overflow-hidden rounded-2xl">
          <div className="flex items-center gap-3 border-b border-[#edf1f4] p-4">
            <div className="relative h-12 w-12 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={ticket.poster} alt={ticket.eventName} fill className="object-cover" sizes="48px" />
            </div>
            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground">Concert</p>
              <p className="text-sm font-bold leading-tight text-[#111111]">{ticket.eventName}</p>
            </div>
          </div>

          <div className="divide-y divide-[#edf1f4]">
            <div className="flex items-center justify-between px-4 py-3">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Armchair className="h-4 w-4 text-[#6b7280]" />
                <span>좌석 정보</span>
              </div>
              <span className="text-sm font-semibold text-[#111111]">
                {ticket.grade} {ticket.seatLabel}
              </span>
            </div>
            <div className="flex items-center justify-between px-4 py-3">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <User className="h-4 w-4 text-[#6b7280]" />
                <span>받는 사람</span>
              </div>
              <div className="flex items-center gap-1.5">
                <div className="gradient-border-icon-button flex h-5 w-5 items-center justify-center rounded-full">
                  <User className="h-3 w-3 text-[#333333]" />
                </div>
                <span className="text-sm font-semibold text-[#111111]">{recipientName}</span>
              </div>
            </div>
            <div className="flex items-center justify-between px-4 py-3">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Banknote className="h-4 w-4 text-[#6b7280]" />
                <span>양도 방식</span>
              </div>
              <span className="text-sm font-semibold text-[#111111]">무료 양도</span>
            </div>
          </div>
        </div>

        <div className="elevated-surface flex flex-col gap-3 rounded-2xl p-4">
          <div>
            <p className="mb-1.5 text-[10px] font-bold uppercase tracking-widest text-muted-foreground">Transaction Hash</p>
            <div className="flex items-center justify-between gap-3">
              <p className="min-w-0 truncate text-xs font-mono text-[#111111]">{txHash}</p>
              <button className="text-xs font-medium text-[#333333]">복사</button>
            </div>
          </div>
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-1.5">
              <Hash className="h-3.5 w-3.5 text-[#6b7280]" />
              <div>
                <p className="text-[10px] text-muted-foreground">NFT 상태</p>
                <p className="text-xs font-semibold text-[#111111]">소유권 이전 완료</p>
              </div>
            </div>
            <div className="flex items-center gap-1.5">
              <Clock className="h-3.5 w-3.5 text-[#6b7280]" />
              <div>
                <p className="text-[10px] text-muted-foreground">처리 시간</p>
                <p className="text-xs font-semibold text-[#111111]">1.8초</p>
              </div>
            </div>
          </div>
        </div>

        <p className="px-2 text-center text-xs text-muted-foreground">{recipientName} 님의 계정으로 티켓이 안전하게 전달되었습니다.</p>

        <div className="mt-auto flex flex-col gap-3">
          <button
            onClick={() => navigate('my-tickets')}
            className="gradient-border-button flex w-full items-center justify-center gap-2 rounded-xl py-4 text-sm font-semibold text-[#111111] transition-all active:scale-[0.98]"
          >
            내 티켓 보기
          </button>
          <button
            onClick={() => navigateTab('home')}
            className="elevated-surface w-full rounded-xl py-3 text-sm font-semibold text-[#333333] transition-all active:scale-[0.98]"
          >
            홈으로 돌아가기
          </button>
        </div>
      </div>
    </AppShell>
  )
}
