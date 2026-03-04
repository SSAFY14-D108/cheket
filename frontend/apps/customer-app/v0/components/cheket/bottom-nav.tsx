'use client'

import { Tab } from '@/lib/types'
import { useApp } from '@/lib/app-context'
import { Home, Music2, Tag, Ticket, Star } from 'lucide-react'
import { cn } from '@/lib/utils'

const TABS: { id: Tab; label: string; icon: typeof Home }[] = [
  { id: 'home', label: '메인', icon: Home },
  { id: 'concerts', label: '공연', icon: Music2 },
  { id: 'resale', label: '티켓 줍줍', icon: Tag },
  { id: 'my-tickets', label: '내 티켓', icon: Ticket },
  { id: 'collection', label: '컬렉션', icon: Star },
]

export function BottomNav() {
  const { activeTab, navigateTab } = useApp()

  return (
    <nav className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-[390px] bg-card border-t border-border z-50">
      <div className="flex items-center">
        {TABS.map(({ id, label, icon: Icon }) => {
          const isActive = activeTab === id
          return (
            <button
              key={id}
              onClick={() => navigateTab(id)}
              className={cn(
                'flex-1 flex flex-col items-center gap-0.5 py-3 transition-colors',
                isActive ? 'text-primary' : 'text-muted-foreground'
              )}
              aria-label={label}
            >
              <Icon className="w-5 h-5" strokeWidth={isActive ? 2.5 : 1.8} />
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
