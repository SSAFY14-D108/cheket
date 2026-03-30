'use client'

import { ChevronRight, Lock } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { Switch } from '@/components/ui/switch'
import { AppShell } from '../app-shell'

export function SettingsScreen() {
  const { navigate, goBack, allowNotifications, setAllowNotifications } = useApp()

  return (
    <AppShell title="설정" showBack onBack={goBack} showBottomNav={false}>
      <div className="flex-1 space-y-4 overflow-y-auto px-4 py-4">
        <section className="elevated-surface rounded-xl p-4">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="text-sm font-semibold text-[#111111]">결제 및 혜택 알림</p>
              <p className="mt-1 text-xs text-muted-foreground">예매 오픈, 거래 상태, 혜택 정보 같은 주요 소식을 알림으로 받아볼 수 있어요.</p>
            </div>
            <Switch checked={allowNotifications} onCheckedChange={setAllowNotifications} aria-label="알림 설정" />
          </div>
        </section>

        <section className="space-y-2">
          <button
            onClick={() => navigate('password-change')}
            className="elevated-surface flex w-full items-center justify-between rounded-lg px-4 py-3 transition-all active:scale-[0.98]"
          >
            <div className="flex items-center gap-2">
              <Lock className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium text-[#111111]">비밀번호 변경</span>
            </div>
            <ChevronRight className="h-4 w-4 text-muted-foreground" />
          </button>
        </section>
      </div>
    </AppShell>
  )
}
