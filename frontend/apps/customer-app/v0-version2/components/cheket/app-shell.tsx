'use client'

import { ReactNode } from 'react'
import { Bell, ChevronLeft, UserCircle } from 'lucide-react'
import { BottomNav } from './bottom-nav'
import { useApp } from '@/lib/app-context'

interface AppShellProps {
  children: ReactNode
  showBottomNav?: boolean
  title?: string
  showBack?: boolean
  onBack?: () => void
  showNotification?: boolean
  rightElement?: ReactNode
  /** Hide the profile icon — use on screens that are already "my page" */
  hideProfileIcon?: boolean
}

export function AppShell({
  children,
  showBottomNav = true,
  title,
  showBack = false,
  onBack,
  showNotification = false,
  rightElement,
  hideProfileIcon = false,
}: AppShellProps) {
  const { navigate, user } = useApp()

  return (
    <div className="flex flex-col h-full">
      {/* AppBar */}
      <header className="flex items-center justify-between px-4 py-3 bg-surface border-b border-border flex-shrink-0 z-40">
        <div className="flex items-center gap-2">
          {showBack ? (
            <button
              onClick={onBack}
              className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-secondary transition-colors -ml-1"
              aria-label="뒤로가기"
            >
              <ChevronLeft className="w-5 h-5 text-foreground" />
            </button>
          ) : null}
          {title ? (
            <h1 className="font-semibold text-base text-foreground">{title}</h1>
          ) : (
            !showBack && (
              <span className="font-bold text-xl tracking-tight text-primary">cheket</span>
            )
          )}
        </div>
        <div className="flex items-center gap-1">
          {rightElement}
          {showNotification && (
            <button
              className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-secondary transition-colors"
              aria-label="알림"
            >
              <Bell className="w-5 h-5 text-foreground" />
            </button>
          )}
          {!hideProfileIcon && user && (
            <button
              onClick={() => navigate('my-page')}
              className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-secondary transition-colors"
              aria-label="마이 페이지"
            >
              <UserCircle className="w-5 h-5 text-foreground" />
            </button>
          )}
        </div>
      </header>

      {/* Scrollable content */}
      <main className={`flex-1 overflow-y-auto ${showBottomNav ? 'pb-20' : ''}`}>
        {children}
      </main>

      {showBottomNav && <BottomNav />}
    </div>
  )
}
