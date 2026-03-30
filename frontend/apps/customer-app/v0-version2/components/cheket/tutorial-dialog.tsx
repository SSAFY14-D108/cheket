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
      <div className="overflow-hidden rounded-[1.5rem] bg-[linear-gradient(135deg,rgba(244,246,250,0.98),rgba(255,255,255,0.94)_58%,rgba(241,245,249,0.96))]">
        <div className="space-y-2 px-5 py-5">
          <div className="inline-flex items-center gap-2 rounded-full border border-[#edf1f4] bg-white px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.18em] text-[#333333] shadow-[0_6px_18px_rgba(15,23,42,0.04)]">
            <Sparkles className="h-3 w-3" />
            {content.category}
          </div>
          <DialogHeader className="gap-2 text-left">
            <DialogTitle className="text-xl font-bold text-[#1f2a2a]">{content.title}</DialogTitle>
            <DialogDescription className="text-sm leading-6 text-[#718080]">
              {content.summary}
            </DialogDescription>
          </DialogHeader>
        </div>
      </div>

      <div className="space-y-2.5">
        {content.points.map((point, index) => (
          <div
            key={`${content.id}-${index}`}
            className="flex items-start gap-3 rounded-xl bg-[#f3f4f6] px-4 py-3.5"
          >
            <div className="mt-0.5 flex h-6 w-6 flex-shrink-0 items-center justify-center rounded-full border border-[#edf1f4] bg-white text-xs font-bold text-[#333333] shadow-[0_4px_10px_rgba(15,23,42,0.04)]">
              {index + 1}
            </div>
            <p className="text-sm leading-6 text-[#1f2a2a]">{point}</p>
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
          <button className="gradient-border-button w-full rounded-[1.4rem] px-4 py-3.5 text-sm">
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
          className={cn('gradient-border-icon-button h-8 w-8 flex-shrink-0 text-foreground', className)}
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
        <button className="w-full rounded-lg border border-[#edf1f4] bg-white px-4 py-3 text-left shadow-[0_10px_30px_rgba(15,23,42,0.045)] transition-all active:scale-[0.98]">
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-2">
              <BookOpen className="h-4 w-4 text-[#7b8794]" />
              <div>
                <p className="text-sm font-medium text-foreground">튜토리얼 다시 보기</p>
                <p className="mt-1 text-xs text-muted-foreground">CHEKET 차별 기능 안내를 다시 확인해요.</p>
              </div>
            </div>
            <ChevronRight className="h-4 w-4 text-[#7b8794]" />
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
                      ? 'border border-[#dbe5f0] bg-white text-[#111111] shadow-[0_6px_18px_rgba(15,23,42,0.04)]'
                      : 'border border-[#e7ebf0] bg-white text-[#7b8794] hover:text-[#1f2a2a]'
                  )}
                >
                  {item.category}
                </button>
              )
            })}
          </div>

          <div className="rounded-2xl border border-[#edf1f4] bg-[#f8fafc] p-3">
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-[#333333]">{selected.category}</p>
            <p className="mt-1 text-sm font-semibold text-[#1f2a2a]">{selected.title}</p>
          </div>

          <TutorialPanel tutorialId={selectedId} />
        </div>

        <DialogFooter className="mt-1">
          <DialogClose asChild>
            <button className="gradient-border-button w-full rounded-[1.4rem] px-4 py-3.5 text-sm">
              확인했어요
            </button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
