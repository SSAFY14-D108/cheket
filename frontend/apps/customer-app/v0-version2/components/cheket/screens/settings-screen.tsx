'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { ChevronRight, Lock } from 'lucide-react'
import { Switch } from '@/components/ui/switch'

export function SettingsScreen() {
  const { navigate, goBack, allowNotifications, setAllowNotifications } = useApp()

  return (
    <AppShell title="설정" showBack onBack={goBack} showBottomNav={false}>
      <div className="flex-1 space-y-4 overflow-y-auto px-4 py-4">
        <section className="rounded-xl border border-border bg-card p-4">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="text-sm font-semibold text-foreground">예매 알림 받기</p>
              <p className="mt-1 text-xs text-muted-foreground">
                마감 임박 공연이나 주요 예매 일정을 알림으로 받아볼 수 있어요.
              </p>
            </div>
            <Switch
              checked={allowNotifications}
              onCheckedChange={setAllowNotifications}
              aria-label="예매 알림 설정"
            />
          </div>
        </section>

        <section className="space-y-2">
          <button
            onClick={() => navigate('password-change')}
            className="flex w-full items-center justify-between rounded-lg border border-border bg-card px-4 py-3 transition-all hover:border-primary/40 active:scale-[0.98]"
          >
            <div className="flex items-center gap-2">
              <Lock className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium text-foreground">비밀번호 변경</span>
            </div>
            <ChevronRight className="h-4 w-4 text-muted-foreground" />
          </button>
        </section>
      </div>
    </AppShell>
  )
}
