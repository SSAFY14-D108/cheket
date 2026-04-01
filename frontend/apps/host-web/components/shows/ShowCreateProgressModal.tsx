"use client"

import { useEffect, useRef, useState } from "react"
import {
  AlertTriangle,
  CheckCircle2,
  CircleDashed,
  FileCheck,
  ImageUp,
  Loader2,
  ServerCog,
  ShieldCheck,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"

export type CreateShowPhase =
  | "idle"
  | "validating"
  | "uploading"
  | "processing"
  | "done"
  | "error"

interface ShowCreateProgressModalProps {
  open: boolean
  phase: CreateShowPhase
  startedAt: string | null
  errorMessage?: string | null
  onDismiss: () => void
}

const ESTIMATED_SECONDS = 10

const STEPS = [
  {
    phase: "validating" as const,
    label: "입력 정보 검증",
    description: "공연 정보와 이해관계자 데이터를 확인하고 있습니다.",
    icon: FileCheck,
  },
  {
    phase: "uploading" as const,
    label: "이미지 업로드",
    description: "포스터 및 상세 이미지를 서버에 전송하고 있습니다.",
    icon: ImageUp,
  },
  {
    phase: "processing" as const,
    label: "서버 등록 처리",
    description: "이해관계자 계약 생성 및 블록체인 등록을 준비하고 있습니다.",
    icon: ServerCog,
  },
  {
    phase: "done" as const,
    label: "등록 완료",
    description: "공연 등록이 정상적으로 완료되었습니다.",
    icon: CheckCircle2,
  },
] as const

const PHASE_ORDER: Record<CreateShowPhase, number> = {
  idle: -1,
  validating: 0,
  uploading: 1,
  processing: 2,
  done: 3,
  error: -1,
}

function formatElapsed(seconds: number) {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60

  return `${String(minutes).padStart(2, "0")}:${String(remainingSeconds).padStart(2, "0")}`
}

function getTimerMessage(elapsedSeconds: number) {
  if (elapsedSeconds < 3) {
    return "공연 정보를 등록하고 있습니다..."
  }

  if (elapsedSeconds < 6) {
    return "이미지와 계약 정보를 처리하고 있습니다..."
  }

  if (elapsedSeconds < 9) {
    return "조금만 더 기다려주세요..."
  }

  return "이해관계자가 많을 경우 시간이 더 소요될 수 있습니다."
}

function computeProgress(phase: CreateShowPhase, elapsedSeconds: number) {
  if (phase === "done") {
    return 100
  }

  if (phase === "error" || phase === "idle") {
    return 0
  }

  const maxProgress = 95
  const progress = Math.min(
    maxProgress,
    (elapsedSeconds / ESTIMATED_SECONDS) * maxProgress,
  )

  return Math.round(progress)
}

export function ShowCreateProgressModal({
  open,
  phase,
  startedAt,
  errorMessage,
  onDismiss,
}: ShowCreateProgressModalProps) {
  const [elapsedSeconds, setElapsedSeconds] = useState(0)
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const isError = phase === "error"
  const isDone = phase === "done"
  const currentPhaseIndex = PHASE_ORDER[phase] ?? -1
  const progress = computeProgress(phase, elapsedSeconds)

  useEffect(() => {
    if (!open || !startedAt) {
      setElapsedSeconds(0)
      return
    }

    const startTime = new Date(startedAt).getTime()

    function tick() {
      const now = Date.now()
      setElapsedSeconds(Math.floor((now - startTime) / 1000))
    }

    tick()
    intervalRef.current = setInterval(tick, 1000)

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current)
        intervalRef.current = null
      }
    }
  }, [open, startedAt])

  useEffect(() => {
    if (isDone || isError) {
      if (intervalRef.current) {
        clearInterval(intervalRef.current)
        intervalRef.current = null
      }
    }
  }, [isDone, isError])

  return (
    <Dialog
      open={open}
      onOpenChange={(value) => {
        if (!value && isError) {
          onDismiss()
        }
      }}
    >
      <DialogContent
        showCloseButton={false}
        className="max-w-lg gap-0 overflow-hidden rounded-3xl border-black/10 bg-white p-0"
        onEscapeKeyDown={(event) => {
          if (!isError) {
            event.preventDefault()
          }
        }}
        onPointerDownOutside={(event) => {
          if (!isError) {
            event.preventDefault()
          }
        }}
      >
        <DialogHeader className="border-b border-slate-200 bg-[radial-gradient(circle_at_top_left,_rgba(15,23,42,0.06),_transparent_40%),linear-gradient(135deg,_rgba(250,250,250,0.98),_rgba(241,245,249,0.96))] px-6 py-6">
          <div className="flex items-center gap-2.5">
            {isDone ? (
              <div className="flex size-10 items-center justify-center rounded-2xl bg-emerald-100 text-emerald-700">
                <CheckCircle2 className="size-5" />
              </div>
            ) : isError ? (
              <div className="flex size-10 items-center justify-center rounded-2xl bg-amber-100 text-amber-700">
                <AlertTriangle className="size-5" />
              </div>
            ) : (
              <div className="flex size-10 items-center justify-center rounded-2xl bg-slate-900 text-white">
                <ShieldCheck className="size-5" />
              </div>
            )}
            <div className="min-w-0 flex-1">
              <DialogTitle className="text-lg font-semibold text-slate-950">
                {isDone
                  ? "공연 등록 완료"
                  : isError
                    ? "등록 중 오류 발생"
                    : "공연을 등록하고 있습니다"}
              </DialogTitle>
              <DialogDescription className="mt-1 text-sm text-slate-500">
                {isDone
                  ? "잠시 후 마이페이지로 이동합니다."
                  : isError
                    ? "문제가 발생했습니다. 다시 시도해주세요."
                    : `예상 소요 시간: 약 ${ESTIMATED_SECONDS}초`}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <div className="px-6 py-6">
          {/* Timer */}
          <div className="flex items-center justify-between rounded-2xl bg-slate-50 px-5 py-4">
            <div className="flex items-center gap-3">
              <div className="flex size-10 items-center justify-center rounded-xl bg-white text-slate-700 shadow-[0_2px_8px_-4px_rgba(15,23,42,0.15)]">
                {isDone ? (
                  <CheckCircle2 className="size-5 text-emerald-600" />
                ) : (
                  <Loader2 className="size-5 animate-spin" />
                )}
              </div>
              <div>
                <p className="font-mono text-2xl font-bold tracking-tight text-slate-900">
                  {formatElapsed(elapsedSeconds)}
                </p>
                <p className="mt-0.5 text-xs text-slate-500">
                  {isDone ? "완료" : "경과 시간"}
                </p>
              </div>
            </div>
            {!isDone && !isError ? (
              <p className="max-w-[160px] text-right text-sm leading-5 text-slate-500">
                {getTimerMessage(elapsedSeconds)}
              </p>
            ) : null}
          </div>

          {/* Progress bar */}
          {!isError ? (
            <div className="mt-5">
              <div className="mb-1.5 flex items-center justify-between text-[11px] font-medium uppercase tracking-[0.16em] text-slate-400">
                <span>Progress</span>
                <span>{progress}%</span>
              </div>
              <div className="h-2.5 overflow-hidden rounded-full bg-slate-100">
                <div
                  className={`h-full rounded-full transition-all duration-700 ease-out ${
                    isDone
                      ? "bg-emerald-500"
                      : "bg-slate-800 progress-shimmer"
                  }`}
                  style={{ width: `${progress}%` }}
                />
              </div>
            </div>
          ) : null}

          {/* Steps */}
          <div className="mt-6 space-y-2.5">
            {STEPS.map((step, index) => {
              const StepIcon = step.icon
              const isComplete = currentPhaseIndex > index || (isDone && step.phase === "done")
              const isCurrent = phase === step.phase && !isDone
              const isPending = !isComplete && !isCurrent

              return (
                <div
                  key={step.phase}
                  className={`flex items-center gap-3.5 rounded-2xl border px-4 py-3.5 transition-all duration-300 ${
                    isComplete
                      ? "border-slate-200/80 bg-slate-50/80"
                      : isCurrent
                        ? "border-slate-300 bg-white shadow-[0_8px_24px_-16px_rgba(15,23,42,0.2)]"
                        : "border-slate-100 bg-white/60"
                  }`}
                >
                  <div
                    className={`flex size-9 shrink-0 items-center justify-center rounded-xl transition-all duration-300 ${
                      isComplete
                        ? "bg-slate-900 text-white"
                        : isCurrent
                          ? "bg-slate-100 text-slate-800"
                          : "bg-slate-50 text-slate-300"
                    }`}
                  >
                    {isComplete ? (
                      <CheckCircle2 className="size-4.5" />
                    ) : isCurrent ? (
                      <Loader2 className="size-4.5 animate-spin" />
                    ) : (
                      <CircleDashed className="size-4.5" />
                    )}
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between gap-2">
                      <p
                        className={`text-sm font-semibold ${
                          isPending ? "text-slate-300" : "text-slate-900"
                        }`}
                      >
                        {step.label}
                      </p>
                      <span
                        className={`text-[10px] font-medium uppercase tracking-[0.12em] ${
                          isComplete
                            ? "text-slate-600"
                            : isCurrent
                              ? "text-slate-500"
                              : "text-slate-300"
                        }`}
                      >
                        {isComplete ? "Done" : isCurrent ? "In progress" : "Waiting"}
                      </span>
                    </div>
                    <p
                      className={`mt-0.5 text-xs leading-5 ${
                        isPending ? "text-slate-300" : "text-slate-500"
                      }`}
                    >
                      {step.description}
                    </p>
                  </div>
                </div>
              )
            })}
          </div>

          {/* Warning / Error */}
          {isError ? (
            <div className="mt-5 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-4">
              <div className="flex items-center gap-2">
                <AlertTriangle className="size-4 text-amber-600" />
                <p className="text-sm font-medium text-amber-900">
                  등록 처리 중 문제가 발생했습니다.
                </p>
              </div>
              {errorMessage ? (
                <p className="mt-2 text-xs leading-5 text-amber-700">
                  {errorMessage}
                </p>
              ) : null}
              <Button
                type="button"
                onClick={onDismiss}
                variant="outline"
                className="mt-4 h-10 w-full rounded-full border-amber-300 text-sm font-semibold text-amber-800 hover:bg-amber-100"
              >
                닫기
              </Button>
            </div>
          ) : !isDone ? (
            <div className="mt-5 rounded-2xl bg-slate-50 px-4 py-3">
              <p className="text-center text-xs leading-5 text-slate-500">
                ⚠️ 브라우저를 닫지 말고 잠시만 기다려주세요.
              </p>
            </div>
          ) : null}
        </div>
      </DialogContent>
    </Dialog>
  )
}
