'use client'

import { AlertCircle, ArrowLeft, Ban, Coins, Home, Lock, WifiOff } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { PurchaseFailureReason } from '@/lib/types'
import { cn } from '@/lib/utils'

const FAILURE_CONFIGS: Record<
  PurchaseFailureReason,
  { icon: typeof AlertCircle; title: string; description: string; color: string }
> = {
  SOLD_OUT: {
    icon: Ban,
    title: '선택한 좌석이 이미 판매되었어요',
    description: '다른 사용자가 먼저 결제를 완료한 경우예요. 다른 좌석을 다시 선택해 주세요.',
    color: 'text-red-400',
  },
  LOCK_FAILED: {
    icon: Lock,
    title: '좌석 점유에 실패했어요',
    description: '결제 직전 좌석 확보 과정에서 오류가 발생했어요. 잠시 후 다시 시도해 주세요.',
    color: 'text-orange-400',
  },
  LIMIT_EXCEEDED: {
    icon: AlertCircle,
    title: '구매 가능 수량을 초과했어요',
    description: '1인당 구매 가능 수량을 넘겨서 결제를 진행할 수 없어요.',
    color: 'text-yellow-500',
  },
  INSUFFICIENT_BALANCE: {
    icon: Coins,
    title: 'CTK 잔액이 부족해요',
    description: '보유한 CTK가 부족해서 결제를 완료할 수 없어요. 충전 후 다시 시도해 주세요.',
    color: 'text-[#6b7280]',
  },
  NETWORK: {
    icon: WifiOff,
    title: '네트워크 오류가 발생했어요',
    description: '결제 처리 중 연결이 불안정했어요. 잠시 후 다시 시도해 주세요.',
    color: 'text-muted-foreground',
  },
}

export function PurchaseFailedScreen() {
  const { navParams, navigate, navigateTab } = useApp()
  const reason: PurchaseFailureReason = navParams.failureReason ?? 'NETWORK'
  const config = FAILURE_CONFIGS[reason]
  const FailIcon = config.icon

  return (
    <div className="flex min-h-full flex-col items-center justify-center gap-8 bg-background p-6">
      <div className={cn('gradient-outline-surface flex h-24 w-24 items-center justify-center rounded-full', config.color)}>
        <FailIcon className="h-12 w-12" />
      </div>

      <div className="flex flex-col gap-3 text-center">
        <h2 className="text-2xl font-bold text-[#111111]">예매에 실패했어요</h2>
        <p className="text-base font-semibold text-[#111111]">{config.title}</p>
        <p className="max-w-xs text-sm leading-relaxed text-muted-foreground">{config.description}</p>
      </div>

      <div className="gradient-outline-surface rounded-full px-4 py-2 text-xs font-mono text-muted-foreground">오류 코드: {reason}</div>

      {reason === 'INSUFFICIENT_BALANCE' && (
        <div className="gradient-outline-surface w-full rounded-xl p-4 text-center text-sm">
          <p className="mb-1 font-medium text-[#111111]">CTK 충전이 필요해요</p>
          <p className="text-xs text-muted-foreground">마이페이지 지갑 화면에서 CTK를 충전한 뒤 다시 예매를 진행해 주세요.</p>
        </div>
      )}

      <div className="flex w-full flex-col gap-3">
        {navParams.eventId && (
          <button
            onClick={() => navigate('seat-selection', { eventId: navParams.eventId })}
            className="gradient-outline-button flex w-full items-center justify-center gap-2 rounded-xl py-4 text-sm font-semibold text-[#111111]"
          >
            <ArrowLeft className="h-4 w-4" />
            좌석 다시 선택
          </button>
        )}
        <button
          onClick={() => navigateTab('home')}
          className="gradient-outline-surface flex w-full items-center justify-center gap-2 rounded-xl py-3.5 text-sm font-semibold text-[#111111]"
        >
          <Home className="h-4 w-4" />
          홈으로 이동
        </button>
      </div>
    </div>
  )
}
