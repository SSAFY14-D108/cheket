'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { ChevronRight, Lock } from 'lucide-react'

export function SettingsScreen() {
  const { navigate, goBack } = useApp()

  const settingsMenu = [
    { label: '비밀번호 변경', icon: Lock, screen: 'password-change' as const },
  ]

  return (
    <AppShell>
      <div className="flex flex-col h-full">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-border sticky top-0 bg-background/95 backdrop-blur-sm">
          <button onClick={goBack} className="text-foreground">←</button>
          <h1 className="font-bold text-lg text-foreground">설정</h1>
          <div className="w-6" />
        </div>

        {/* Menu */}
        <div className="flex-1 overflow-y-auto px-4 py-4">
          <div className="space-y-2">
            {settingsMenu.map((item) => (
              <button
                key={item.screen}
                onClick={() => navigate(item.screen)}
                className="w-full flex items-center justify-between px-4 py-3 bg-card border border-border rounded-lg hover:border-primary/40 active:scale-[0.98] transition-all"
              >
                <span className="text-sm font-medium text-foreground">{item.label}</span>
                <ChevronRight className="w-4 h-4 text-muted-foreground" />
              </button>
            ))}
          </div>
        </div>
      </div>
    </AppShell>
  )
}
