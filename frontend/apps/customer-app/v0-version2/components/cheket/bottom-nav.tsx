'use client'

import { useApp } from '@/lib/app-context'
import { Tab } from '@/lib/types'
import { cn } from '@/lib/utils'
import { Home, Music2, Tag, Ticket, UserCircle } from 'lucide-react'

const TABS: { id: Tab; label: string; icon: typeof Home }[] = [
  { id: 'home', label: '메인', icon: Home },
  { id: 'concerts', label: '공연', icon: Music2 },
  { id: 'resale', label: '2차거래소', icon: Tag },
  { id: 'my-tickets', label: '내 티켓', icon: Ticket },
  { id: 'my-page', label: '마이', icon: UserCircle },
]

export function BottomNav() {
  const { activeTab, navigateTab } = useApp()

  return (
    <nav className="fixed bottom-0 left-1/2 z-50 w-full max-w-[390px] -translate-x-1/2 border-t border-border bg-card">
      <div className="flex items-center">
        {TABS.map(({ id, label, icon: Icon }) => {
          const isActive = activeTab === id

          return (
            <button
              key={id}
              onClick={() => navigateTab(id)}
              className={cn(
                'flex flex-1 flex-col items-center gap-0.5 py-3 transition-colors',
                isActive ? 'text-primary' : 'text-muted-foreground'
              )}
              aria-label={label}
            >
              <Icon className="h-5 w-5" strokeWidth={isActive ? 2.5 : 1.8} />
              <span className={cn('text-[10px] font-medium', isActive ? 'text-primary' : 'text-muted-foreground')}>
                {label}
              </span>
            </button>
          )
        })}
      </div>
      <div className="h-safe-bottom" />
    </nav>
  )
}
