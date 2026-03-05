'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { XCircle, AlertCircle, Music2, Armchair, User, Phone } from 'lucide-react'

const FAILURE_MESSAGES: Record<string, string> = {
  LIMIT_EXCEEDED: '받는 사람의 구매 수량 제한(4매)을 초과합니다',
  USER_NOT_FOUND: 'CHEKET 회원이 아니거나 등록되지 않은 번호입니다',
  NETWORK: '네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요',
}

export function TransferFailedScreen() {
  const { navParams, navigate, goBack, tickets } = useApp()
  const ticket = tickets.find((t) => t.id === navParams.ticketId)
  const recipientName = navParams.recipientName ?? '—'
  const recipientPhone = navParams.recipientPhone ?? '—'
  const reason = navParams.transferFailureReason ?? 'NETWORK'
  const failureMessage = FAILURE_MESSAGES[reason]

  if (!ticket) return null

  return (
    <AppShell showBack onBack={goBack} title="" showBottomNav={false}>
      <div className="flex flex-col gap-5 p-4">

        {/* Fail icon + title */}
        <div className="flex flex-col items-center gap-3 pt-4">
          <div className="w-20 h-20 rounded-full bg-destructive/10 border-2 border-destructive/30 flex items-center justify-center">
            <div className="w-14 h-14 rounded-full bg-destructive flex items-center justify-center shadow-lg shadow-destructive/30">
              <XCircle className="w-7 h-7 text-white" />
            </div>
          </div>
          <div className="text-center">
            <h2 className="font-bold text-xl text-foreground">양도에 실패했습니다</h2>
            <p className="text-sm text-muted-foreground mt-1">다시 시도해주세요</p>
          </div>
        </div>

        {/* Failure reason */}
        <div className="bg-destructive/10 border border-destructive/20 rounded-2xl p-4 flex items-start gap-3">
          <AlertCircle className="w-4 h-4 text-destructive flex-shrink-0 mt-0.5" />
          <div>
            <p className="text-xs font-bold text-destructive mb-1">실패 사유</p>
            <p className="text-sm text-foreground">{failureMessage}</p>
          </div>
        </div>

        {/* Transfer detail card */}
        <div className="bg-card border border-border rounded-2xl divide-y divide-border">
          <div className="flex items-center justify-between px-4 py-3">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Music2 className="w-4 h-4" />
              <span>공연</span>
            </div>
            <span className="text-sm font-semibold text-foreground text-right max-w-[55%] leading-tight">{ticket.eventName}</span>
          </div>
          <div className="flex items-center justify-between px-4 py-3">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Armchair className="w-4 h-4" />
              <span>자신</span>
            </div>
            <span className="text-sm font-semibold text-foreground">{ticket.grade} {ticket.seatLabel}</span>
          </div>
          <div className="flex items-center justify-between px-4 py-3">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <User className="w-4 h-4" />
              <span>받는 사람</span>
            </div>
            <span className="text-sm font-semibold text-foreground">{recipientName} 님</span>
          </div>
          <div className="flex items-center justify-between px-4 py-3">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Phone className="w-4 h-4" />
              <span>전화번호</span>
            </div>
            <span className="text-sm font-mono text-foreground">{recipientPhone}</span>
          </div>
        </div>

        {/* Info notice */}
        <div className="flex items-start gap-2 px-1">
          <AlertCircle className="w-3.5 h-3.5 text-muted-foreground flex-shrink-0 mt-0.5" />
          <p className="text-xs text-muted-foreground leading-relaxed">
            티켓은 여전히 내 보관함에 안전하게 있습니다.<br />
            문제가 지속되면 고객센터에 문의해주세요.
          </p>
        </div>

        {/* CTA */}
        <div className="flex flex-col gap-3 mt-auto">
          <button
            onClick={() => navigate('transfer', { ticketId: ticket.id })}
            className="w-full bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all"
          >
            다시 시도하기
          </button>
          <button
            onClick={() => navigate('my-tickets')}
            className="w-full bg-transparent text-muted-foreground font-semibold py-3 rounded-xl text-sm hover:text-foreground active:scale-[0.98] transition-all"
          >
            내 티켓으로 돌아가기
          </button>
        </div>
      </div>
    </AppShell>
  )
}
