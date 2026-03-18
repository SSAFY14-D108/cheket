'use client'

import { AlertCircle, Armchair, Music2, Phone, User, XCircle } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'

const FAILURE_MESSAGES: Record<string, string> = {
  LIMIT_EXCEEDED: '받는 사람의 보유 한도를 초과해서 양도할 수 없어요.',
  USER_NOT_FOUND: 'CHEKET에 가입되지 않은 사용자라 양도를 진행할 수 없어요.',
  NETWORK: '네트워크 오류로 양도 처리가 완료되지 않았어요. 잠시 후 다시 시도해 주세요.',
}

export function TransferFailedScreen() {
  const { navParams, navigate, goBack, tickets } = useApp()
  const ticket = tickets.find((t) => t.id === navParams.ticketId)
  const recipientName = navParams.recipientName ?? '받는 사람'
  const recipientPhone = navParams.recipientPhone ?? '-'
  const reason = navParams.transferFailureReason ?? 'NETWORK'
  const failureMessage = FAILURE_MESSAGES[reason] ?? FAILURE_MESSAGES.NETWORK

  if (!ticket) return null

  return (
    <AppShell showBack onBack={goBack} title="" showBottomNav={false}>
      <div className="flex flex-col gap-5 p-4">
        <div className="flex flex-col items-center gap-3 pt-4">
          <div className="flex h-20 w-20 items-center justify-center rounded-full border border-destructive/20 bg-destructive/10">
            <div className="flex h-14 w-14 items-center justify-center rounded-full bg-destructive">
              <XCircle className="h-7 w-7 text-white" />
            </div>
          </div>
          <div className="text-center">
            <h2 className="text-xl font-bold text-[#111111]">양도에 실패했어요</h2>
            <p className="mt-1 text-sm text-muted-foreground">입력한 정보는 그대로 유지되니 다시 시도할 수 있어요.</p>
          </div>
        </div>

        <div className="rounded-2xl border border-destructive/20 bg-destructive/10 p-4">
          <div className="flex items-start gap-3">
            <AlertCircle className="mt-0.5 h-4 w-4 flex-shrink-0 text-destructive" />
            <div>
              <p className="mb-1 text-xs font-bold text-destructive">실패 사유</p>
              <p className="text-sm text-[#111111]">{failureMessage}</p>
            </div>
          </div>
        </div>

        <div className="elevated-surface rounded-2xl divide-y divide-border">
          <div className="flex items-center justify-between px-4 py-3">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Music2 className="h-4 w-4" />
              <span>공연명</span>
            </div>
            <span className="max-w-[55%] text-right text-sm font-semibold leading-tight text-[#111111]">{ticket.eventName}</span>
          </div>
          <div className="flex items-center justify-between px-4 py-3">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Armchair className="h-4 w-4" />
              <span>좌석</span>
            </div>
            <span className="text-sm font-semibold text-[#111111]">
              {ticket.grade} {ticket.seatLabel}
            </span>
          </div>
          <div className="flex items-center justify-between px-4 py-3">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <User className="h-4 w-4" />
              <span>받는 사람</span>
            </div>
            <span className="text-sm font-semibold text-[#111111]">{recipientName}</span>
          </div>
          <div className="flex items-center justify-between px-4 py-3">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Phone className="h-4 w-4" />
              <span>전화번호</span>
            </div>
            <span className="text-sm font-mono text-[#111111]">{recipientPhone}</span>
          </div>
        </div>

        <div className="flex items-start gap-2 px-1">
          <AlertCircle className="mt-0.5 h-3.5 w-3.5 flex-shrink-0 text-muted-foreground" />
          <p className="text-xs leading-relaxed text-muted-foreground">
            티켓은 아직 내 계정에 그대로 남아 있어요.
            <br />
            정보를 다시 확인한 뒤 같은 화면에서 다시 양도를 진행해 주세요.
          </p>
        </div>

        <div className="mt-auto flex flex-col gap-3">
          <button
            onClick={() => navigate('transfer', { ticketId: ticket.id })}
            className="gradient-border-button w-full rounded-xl py-4 text-sm font-semibold text-[#111111]"
          >
            다시 시도하기
          </button>
          <button
            onClick={() => navigate('my-tickets')}
            className="elevated-surface w-full rounded-xl py-3 text-sm font-semibold text-[#333333]"
          >
            내 티켓으로 돌아가기
          </button>
        </div>
      </div>
    </AppShell>
  )
}
