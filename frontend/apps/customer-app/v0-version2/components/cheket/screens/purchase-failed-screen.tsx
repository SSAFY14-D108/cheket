'use client'

import { useApp } from '@/lib/app-context'
import { PurchaseFailureReason } from '@/lib/types'
import { AlertCircle, WifiOff, Ban, Lock, Coins, ArrowLeft, Home } from 'lucide-react'
import { cn } from '@/lib/utils'

const FAILURE_CONFIGS: Record<
  PurchaseFailureReason,
  { icon: typeof AlertCircle; title: string; description: string; color: string }
> = {
  SOLD_OUT: {
    icon: Ban,
    title: '매진된 좌석입니다',
    description: '선택하신 좌석이 다른 사용자에 의해 이미 구매되었습니다. 다른 좌석을 선택해주세요.',
    color: 'text-red-400',
  },
  LOCK_FAILED: {
    icon: Lock,
    title: '좌석 잠금 실패',
    description: '요청량이 많아 좌석 선점에 실패했습니다. 잠시 후 다시 시도해주세요.',
    color: 'text-orange-400',
  },
  LIMIT_EXCEEDED: {
    icon: AlertCircle,
    title: '구매 한도 초과',
    description: '1인 최대 구매 한도를 초과했습니다. 이미 보유한 티켓을 확인해주세요.',
    color: 'text-yellow-400',
  },
  INSUFFICIENT_BALANCE: {
    icon: Coins,
    title: 'CTK 잔액 부족',
    description: '보유 CTK가 부족합니다. 지갑에서 CTK를 충전한 후 다시 시도해주세요.',
    color: 'text-primary',
  },
  NETWORK: {
    icon: WifiOff,
    title: '네트워크 오류',
    description: '결제 처리 중 네트워크 오류가 발생했습니다. 인터넷 연결을 확인하고 다시 시도해주세요.',
    color: 'text-muted-foreground',
  },
}

export function PurchaseFailedScreen() {
  const { navParams, navigate, navigateTab } = useApp()
  const reason: PurchaseFailureReason = navParams.failureReason ?? 'NETWORK'
  const config = FAILURE_CONFIGS[reason]
  const FailIcon = config.icon

  return (
    <div className="min-h-full flex flex-col items-center justify-center bg-background p-6 gap-8">
      {/* Icon */}
      <div className={cn('w-24 h-24 rounded-full flex items-center justify-center bg-secondary border-2 border-border', config.color)}>
        <FailIcon className="w-12 h-12" />
      </div>

      {/* Title + description */}
      <div className="text-center flex flex-col gap-3">
        <h2 className="font-bold text-2xl text-foreground">구매 실패</h2>
        <p className="font-semibold text-base text-foreground">{config.title}</p>
        <p className="text-sm text-muted-foreground leading-relaxed max-w-xs">
          {config.description}
        </p>
      </div>

      {/* Failure reason badge */}
      <div className="px-4 py-2 rounded-full bg-secondary border border-border text-xs text-muted-foreground font-mono">
        오류 코드: {reason}
      </div>

      {/* CTK top-up hint */}
      {reason === 'INSUFFICIENT_BALANCE' && (
        <div className="w-full bg-primary/5 border border-primary/20 rounded-xl p-4 text-sm text-center">
          <p className="text-primary font-medium mb-1">CTK 충전이 필요합니다</p>
          <p className="text-muted-foreground text-xs">마이페이지 → 지갑에서 충전할 수 있습니다.</p>
        </div>
      )}

      {/* Action buttons */}
      <div className="w-full flex flex-col gap-3">
        {navParams.eventId && (
          <button
            onClick={() => navigate('seat-selection', { eventId: navParams.eventId })}
            className="w-full flex items-center justify-center gap-2 bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all"
          >
            <ArrowLeft className="w-4 h-4" />
            좌석 재선택
          </button>
        )}
        <button
          onClick={() => navigateTab('home')}
          className="w-full flex items-center justify-center gap-2 bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm hover:border-primary/50 active:scale-[0.98] transition-all"
        >
          <Home className="w-4 h-4" />
          홈으로 가기
        </button>
      </div>
    </div>
  )
}
