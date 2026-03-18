'use client'

import Image from 'next/image'
import { ReactNode } from 'react'
import { Bell, ChevronLeft } from 'lucide-react'
import { BottomNav } from './bottom-nav'
import { useApp } from '@/lib/app-context'
import { TutorialHelpButton } from './tutorial-dialog'
import type { Screen } from '@/lib/types'

interface AppShellProps {
  children: ReactNode
  showBottomNav?: boolean
  title?: string
  showBack?: boolean
  onBack?: () => void
  showNotification?: boolean
  rightElement?: ReactNode
  footer?: ReactNode
  hideProfileIcon?: boolean
}

const TUTORIAL_SCREEN_MAP: Partial<Record<Screen, ReactNode>> = {
  'resale-list': <TutorialHelpButton tutorialId="resale-list" />,
  'resale-tickets': <TutorialHelpButton tutorialId="resale-detail" />,
  'resale-detail': <TutorialHelpButton tutorialId="resale-detail" />,
  'resale-create': <TutorialHelpButton tutorialId="resale-create" />,
  wallet: <TutorialHelpButton tutorialId="wallet" />,
  transfer: <TutorialHelpButton tutorialId="transfer" />,
  collection: <TutorialHelpButton tutorialId="collection" />,
  'collectible-ticket-detail': <TutorialHelpButton tutorialId="collectible-ticket-detail" />,
  'qr-checkin': <TutorialHelpButton tutorialId="qr-checkin" />,
}

export function AppShell({
  children,
  showBottomNav = true,
  title,
  showBack = false,
  onBack,
  showNotification = true,
  rightElement,
  footer,
  hideProfileIcon: _hideProfileIcon = false,
}: AppShellProps) {
  const { screen, goBack } = useApp()
  const resolvedRightElement = rightElement ?? TUTORIAL_SCREEN_MAP[screen]
  const resolvedShowBack = showBack || screen === 'collection'
  const resolvedOnBack = onBack ?? (screen === 'collection' ? goBack : undefined)

  return (
    <div className="flex h-full flex-col">
      <header className="z-40 flex flex-shrink-0 items-center justify-between border-b border-border bg-surface px-4 py-3">
        <div className="flex min-w-0 flex-1 items-center gap-2">
          {resolvedShowBack ? (
            <button
              onClick={resolvedOnBack}
              className="-ml-1 gradient-border-icon-button h-8 w-8"
              aria-label="뒤로가기"
            >
              <ChevronLeft className="h-5 w-5 text-foreground" />
            </button>
          ) : null}

          {title ? (
            <>
              <h1 className="truncate text-base font-semibold text-foreground">{title}</h1>
              {resolvedRightElement}
            </>
          ) : (
            !resolvedShowBack && (
              <Image
                src="/logo2.webp"
                alt="cheket"
                width={164}
                height={38}
                className="h-auto w-[92px] object-contain"
                style={{ filter: 'saturate(0.38) hue-rotate(32deg) brightness(1.02) contrast(1.02)' }}
                priority
              />
            )
          )}
        </div>

        <div className="flex items-center gap-1">
          {showNotification ? (
            <button
              className="neutral-icon-button h-8 w-8"
              aria-label="알림"
            >
              <Bell className="h-5 w-5 text-foreground" />
            </button>
          ) : null}
        </div>
      </header>

      <main className={`flex-1 overflow-y-auto ${showBottomNav ? 'pb-14' : ''}`}>{children}</main>

      {footer ? (
        <div className={`flex-shrink-0 px-4 py-2 ${showBottomNav ? 'mb-14' : ''}`}>
          {footer}
        </div>
      ) : null}

      {showBottomNav ? <BottomNav /> : null}
    </div>
  )
}
