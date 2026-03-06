'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { ChevronRight, Lock } from 'lucide-react'
import { Switch } from '@/components/ui/switch'

export function SettingsScreen() {
  const { navigate, goBack, allowNotifications, setAllowNotifications } = useApp()

  return (
    <AppShell title="설정" showBack onBack={goBack} showBottomNav={false}>
      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-4">
        <section className="rounded-xl border border-border bg-card p-4">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="text-sm font-semibold text-foreground">푸시 알림 받기</p>
              <p className="text-xs text-muted-foreground mt-1">
                마케팅/이벤트 등 푸시 알림 수신 여부를 설정합니다.
              </p>
            </div>
            <Switch
              checked={allowNotifications}
              onCheckedChange={setAllowNotifications}
              aria-label="푸시 알림 받기 설정"
            />
          </div>
        </section>

        <section className="space-y-2">
          <button
            onClick={() => navigate('password-change')}
            className="w-full flex items-center justify-between px-4 py-3 bg-card border border-border rounded-lg hover:border-primary/40 active:scale-[0.98] transition-all"
          >
            <div className="flex items-center gap-2">
              <Lock className="w-4 h-4 text-muted-foreground" />
              <span className="text-sm font-medium text-foreground">비밀번호 변경</span>
            </div>
            <ChevronRight className="w-4 h-4 text-muted-foreground" />
          </button>
        </section>
      </div>
    </AppShell>
  )
}
