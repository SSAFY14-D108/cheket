'use client'

import type { ComponentProps, ComponentType } from 'react'
import {
  HomeIcon as HomeOutlineIcon,
  MusicalNoteIcon as MusicalNoteOutlineIcon,
  TagIcon as TagOutlineIcon,
  TicketIcon as TicketOutlineIcon,
  UserCircleIcon as UserCircleOutlineIcon,
} from '@heroicons/react/24/outline'
import {
  HomeIcon as HomeSolidIcon,
  MusicalNoteIcon as MusicalNoteSolidIcon,
  TagIcon as TagSolidIcon,
  TicketIcon as TicketSolidIcon,
  UserCircleIcon as UserCircleSolidIcon,
} from '@heroicons/react/24/solid'
import { useApp } from '@/lib/app-context'
import { Tab } from '@/lib/types'
import { cn } from '@/lib/utils'

type TabIconPair = {
  id: Tab
  label: string
  outline: ComponentType<ComponentProps<'svg'>>
  solid: ComponentType<ComponentProps<'svg'>>
}

const TABS: TabIconPair[] = [
  { id: 'home', label: '메인', outline: HomeOutlineIcon, solid: HomeSolidIcon },
  { id: 'concerts', label: '공연', outline: MusicalNoteOutlineIcon, solid: MusicalNoteSolidIcon },
  { id: 'resale', label: '2차거래소', outline: TagOutlineIcon, solid: TagSolidIcon },
  { id: 'my-tickets', label: '내 티켓', outline: TicketOutlineIcon, solid: TicketSolidIcon },
  { id: 'my-page', label: '마이', outline: UserCircleOutlineIcon, solid: UserCircleSolidIcon },
]

export function BottomNav() {
  const { activeTab, navigateTab } = useApp()

  return (
    <nav className="fixed bottom-0 left-1/2 z-50 w-full max-w-[390px] -translate-x-1/2 border-t border-border bg-card">
      <div className="flex items-center">
        {TABS.map(({ id, label, outline: OutlineIcon, solid: SolidIcon }) => {
          const isActive = activeTab === id
          const Icon = isActive ? SolidIcon : OutlineIcon

          return (
            <button
              key={id}
              onClick={() => navigateTab(id)}
              className={cn(
                'flex flex-1 flex-col items-center gap-1 py-3 transition-colors',
                isActive ? 'text-[#374151]' : 'text-[#c4cad4]'
              )}
              aria-label={label}
            >
              <Icon className="h-5 w-5" />
              <span className={cn('text-[11px] font-medium', isActive ? 'text-[#374151]' : 'text-[#9ca3af]')}>{label}</span>
            </button>
          )
        })}
      </div>
      <div className="h-safe-bottom" />
    </nav>
  )
}
