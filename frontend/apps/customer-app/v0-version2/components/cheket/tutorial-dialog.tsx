'use client'

import { useMemo, useState } from 'react'
import { BookOpen, CircleHelp, ChevronRight, ShieldAlert, Sparkles } from 'lucide-react'
import { cn } from '@/lib/utils'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { TUTORIAL_CONTENT, TUTORIAL_ORDER, type TutorialId } from './tutorial-content'

function TutorialPanel({ tutorialId }: { tutorialId: TutorialId }) {
  const content = TUTORIAL_CONTENT[tutorialId]

  return (
    <div className="space-y-4">
      <div className="overflow-hidden rounded-[1.5rem] bg-gradient-to-br from-primary/12 via-background to-secondary/80">
        <div className="space-y-2 px-5 py-5">
          <div className="inline-flex items-center gap-2 rounded-full bg-primary/12 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.18em] text-primary">
            <Sparkles className="h-3 w-3" />
            {content.category}
          </div>
          <DialogHeader className="gap-2 text-left">
            <DialogTitle className="text-xl font-bold text-foreground">{content.title}</DialogTitle>
            <DialogDescription className="text-sm leading-6 text-muted-foreground">
              {content.summary}
            </DialogDescription>
          </DialogHeader>
        </div>
      </div>

      <div className="space-y-2.5">
        {content.points.map((point, index) => (
          <div
            key={`${content.id}-${index}`}
            className="flex items-start gap-3 rounded-xl bg-gray-100 px-4 py-3.5"
          >
            <div className="mt-0.5 flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full bg-primary/12 text-xs font-bold text-primary">
              {index + 1}
            </div>
            <p className="text-sm leading-6 text-foreground">{point}</p>
          </div>
        ))}
      </div>

      {content.caution ? (
        <div className="flex items-start gap-3 rounded-xl bg-amber-100 px-4 py-4">
          <div className="mt-0.5 flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full bg-amber-200/80 text-amber-700">
            <ShieldAlert className="h-3.5 w-3.5" />
          </div>
          <p className="text-sm leading-6 text-amber-950">{content.caution}</p>
        </div>
      ) : null}
    </div>
  )
}

function TutorialDialogBody({ tutorialId }: { tutorialId: TutorialId }) {
  return (
    <DialogContent className="w-[calc(100vw-2rem)] max-w-[360px] rounded-[1.75rem] border-0 px-4 py-4 shadow-[0_24px_80px_rgba(15,23,42,0.18)] sm:max-w-[360px]">
      <TutorialPanel tutorialId={tutorialId} />
      <DialogFooter className="mt-1">
        <DialogClose asChild>
          <button className="w-full rounded-xl bg-primary px-4 py-3.5 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98]">
            확인했어요
          </button>
        </DialogClose>
      </DialogFooter>
    </DialogContent>
  )
}

export function TutorialHelpButton({
  tutorialId,
  className,
}: {
  tutorialId: TutorialId
  className?: string
}) {
  return (
    <Dialog>
      <DialogTrigger asChild>
        <button
          className={cn(
            'flex h-8 w-8 items-center justify-center rounded-full text-foreground transition-all hover:bg-secondary',
            className
          )}
          aria-label="도움말 보기"
        >
          <CircleHelp className="h-5 w-5 stroke-[1.9]" />
        </button>
      </DialogTrigger>
      <TutorialDialogBody tutorialId={tutorialId} />
    </Dialog>
  )
}

export function TutorialLibraryButton() {
  const [selectedId, setSelectedId] = useState<TutorialId>(TUTORIAL_ORDER[0])
  const selected = useMemo(() => TUTORIAL_CONTENT[selectedId], [selectedId])

  return (
    <Dialog>
      <DialogTrigger asChild>
        <button className="w-full rounded-lg border border-border bg-card px-4 py-3 text-left transition-all hover:border-primary/40 active:scale-[0.98]">
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-2">
              <BookOpen className="h-4 w-4 text-muted-foreground" />
              <div>
                <p className="text-sm font-medium text-foreground">튜토리얼 다시 보기</p>
                <p className="mt-1 text-xs text-muted-foreground">CHEKET 차별 기능 안내를 다시 확인해요.</p>
              </div>
            </div>
            <ChevronRight className="h-4 w-4 text-muted-foreground" />
          </div>
        </button>
      </DialogTrigger>
      <DialogContent className="w-[calc(100vw-2rem)] max-w-[360px] rounded-[1.75rem] border-0 px-4 py-4 shadow-[0_24px_80px_rgba(15,23,42,0.18)] sm:max-w-[360px]">
        <div className="space-y-4 overflow-hidden">
          <DialogHeader className="text-left">
            <DialogTitle className="text-lg font-bold">튜토리얼 다시 보기</DialogTitle>
            <DialogDescription>서비스 차별 기능만 모아서 다시 볼 수 있어요.</DialogDescription>
          </DialogHeader>

          <div className="flex flex-wrap gap-2">
            {TUTORIAL_ORDER.map((tutorialId) => {
              const item = TUTORIAL_CONTENT[tutorialId]
              const active = tutorialId === selectedId

              return (
                <button
                  key={tutorialId}
                  onClick={() => setSelectedId(tutorialId)}
                  className={cn(
                    'rounded-full px-3 py-1.5 text-xs font-semibold transition-colors',
                    active
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-secondary text-muted-foreground hover:text-foreground'
                  )}
                >
                  {item.category}
                </button>
              )
            })}
          </div>

          <div className="rounded-2xl bg-secondary/45 p-3">
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-primary">{selected.category}</p>
            <p className="mt-1 text-sm font-semibold text-foreground">{selected.title}</p>
          </div>

          <TutorialPanel tutorialId={selectedId} />
        </div>

        <DialogFooter className="mt-1">
          <DialogClose asChild>
            <button className="w-full rounded-xl bg-primary px-4 py-3.5 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98]">
              확인했어요
            </button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
