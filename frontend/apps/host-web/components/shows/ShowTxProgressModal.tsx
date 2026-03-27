"use client"

import { useEffect, useRef, useState } from "react"
import {
  AlertTriangle,
  CheckCircle2,
  Circle,
  Loader2,
  ShieldCheck,
} from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { fetchTxStatus, type TxStatus } from "@/lib/show-manage-api"

interface ShowTxProgressModalProps {
  txId: number | null
  open: boolean
  onClose: () => void
  onConfirmed: () => void
}

const TX_STEPS = [
  {
    status: "PENDING" as const,
    label: "트랜잭션 제출",
    description: "블록체인 네트워크에 트랜잭션을 전송하고 있습니다.",
  },
  {
    status: "SUBMITTED" as const,
    label: "블록 처리 중",
    description: "트랜잭션이 블록에 포함되어 처리되고 있습니다.",
  },
  {
    status: "CONFIRMED" as const,
    label: "등록 완료",
    description: "스마트 컨트랙트에 공연 정보가 확정되었습니다.",
  },
] as const

const STATUS_ORDER: Record<TxStatus, number> = {
  PENDING: 0,
  SUBMITTED: 1,
  CONFIRMED: 2,
  FAILED: -1,
}

function formatHash(hash?: string) {
  if (!hash) {
    return "-"
  }

  return hash.length > 18 ? `${hash.slice(0, 10)}...${hash.slice(-8)}` : hash
}

export function ShowTxProgressModal({
  txId,
  open,
  onClose,
  onConfirmed,
}: ShowTxProgressModalProps) {
  const [currentStatus, setCurrentStatus] = useState<TxStatus>("PENDING")
  const [txHash, setTxHash] = useState<string | undefined>()
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const isPollingRef = useRef(false)

  const isFailed = currentStatus === "FAILED"
  const isConfirmed = currentStatus === "CONFIRMED"
  const isTerminal = isFailed || isConfirmed
  const currentStepIndex = STATUS_ORDER[currentStatus] ?? -1

  useEffect(() => {
    if (!open || !txId) {
      return
    }

    const targetTxId = txId

    function clearPolling() {
      if (intervalRef.current) {
        clearInterval(intervalRef.current)
        intervalRef.current = null
      }
    }

    setCurrentStatus("PENDING")
    setTxHash(undefined)
    setErrorMessage(null)
    isPollingRef.current = false

    async function poll() {
      if (isPollingRef.current) {
        return
      }

      isPollingRef.current = true

      try {
        const result = await fetchTxStatus(targetTxId)

        setCurrentStatus(result.status)
        setErrorMessage(null)

        if (result.txHash) {
          setTxHash(result.txHash)
        }

        if (result.status === "CONFIRMED" || result.status === "FAILED") {
          clearPolling()
        }
      } catch {
        setErrorMessage("트랜잭션 상태를 조회하지 못했습니다. 잠시 후 다시 시도합니다.")
      } finally {
        isPollingRef.current = false
      }
    }

    void poll()
    intervalRef.current = setInterval(() => {
      void poll()
    }, 1000)

    return () => {
      clearPolling()
      isPollingRef.current = false
    }
  }, [open, txId])

  return (
    <Dialog
      open={open}
      onOpenChange={(value) => {
        if (!value && isTerminal) {
          onClose()
        }
      }}
    >
      <DialogContent
        showCloseButton={false}
        className="max-w-lg gap-0 overflow-hidden rounded-3xl border-black/10 bg-white p-0 sm:max-w-lg"
      >
        <DialogHeader className="border-b border-emerald-100 bg-[radial-gradient(circle_at_top_left,_rgba(16,185,129,0.12),_transparent_40%),linear-gradient(135deg,_rgba(236,253,245,0.96),_rgba(239,246,255,0.95))] px-6 py-6">
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-2">
              <ShieldCheck className="size-5 text-emerald-600" />
              <DialogTitle className="text-lg font-semibold text-slate-950">
                블록체인 등록 진행 중
              </DialogTitle>
            </div>
            <Badge
              variant="outline"
              className={
                isFailed
                  ? "rounded-full border-amber-200 bg-amber-50 text-amber-700"
                  : "rounded-full border-emerald-200 bg-white/70 text-emerald-800"
              }
            >
              {isFailed ? "FAILED" : currentStatus}
            </Badge>
          </div>
          <DialogDescription className="mt-2 text-sm leading-relaxed text-slate-600">
            공연 정보를 스마트 컨트랙트에 기록하고 있습니다. 이 과정은 보통 수 초에서 수십 초가 소요됩니다.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-0 px-6 py-6">
          {TX_STEPS.map((step, index) => {
            const isComplete = currentStepIndex > index
            const isCurrent = currentStepIndex === index && !isFailed

            return (
              <div key={step.status} className="flex gap-4">
                <div className="flex flex-col items-center">
                  <div className="flex size-8 shrink-0 items-center justify-center">
                    {isComplete ? (
                      <CheckCircle2 className="size-6 text-emerald-500" />
                    ) : isCurrent ? (
                      <Loader2 className="size-6 animate-spin text-emerald-500" />
                    ) : (
                      <Circle className="size-6 text-slate-200" />
                    )}
                  </div>
                  {index < TX_STEPS.length - 1 ? (
                    <div
                      className={`my-1 h-8 w-0.5 ${
                        isComplete ? "bg-emerald-400" : "bg-slate-200"
                      }`}
                    />
                  ) : null}
                </div>

                <div className="pb-6">
                  <p
                    className={`text-sm font-semibold ${
                      isComplete || isCurrent ? "text-slate-950" : "text-slate-400"
                    }`}
                  >
                    {step.label}
                  </p>
                  <p
                    className={`mt-1 text-xs leading-relaxed ${
                      isCurrent ? "text-slate-500" : "text-slate-400"
                    }`}
                  >
                    {step.description}
                  </p>
                </div>
              </div>
            )
          })}
        </div>

        <div className="border-t border-slate-100 bg-slate-950 px-6 py-4">
          <div className="flex items-center justify-between gap-3">
            <span className="text-xs text-slate-400">TX ID</span>
            <span className="font-mono text-xs text-slate-200">{txId ?? "-"}</span>
          </div>
          {txHash ? (
            <div className="mt-2 flex items-center justify-between gap-3">
              <span className="text-xs text-slate-400">TX Hash</span>
              <span className="font-mono text-xs text-emerald-300">
                {formatHash(txHash)}
              </span>
            </div>
          ) : null}
          {errorMessage ? (
            <p className="mt-3 text-xs text-amber-300">{errorMessage}</p>
          ) : null}
        </div>

        {isFailed ? (
          <div className="border-t border-amber-200 bg-amber-50 px-6 py-4">
            <div className="flex items-center gap-2">
              <AlertTriangle className="size-4 text-amber-600" />
              <p className="text-sm font-medium text-amber-900">
                트랜잭션 처리에 실패했습니다.
              </p>
            </div>
          </div>
        ) : null}

        {isTerminal ? (
          <div className="border-t border-slate-100 px-6 py-4">
            {isConfirmed ? (
              <Button
                type="button"
                onClick={onConfirmed}
                className="h-12 w-full rounded-full bg-emerald-600 text-sm font-semibold text-white hover:bg-emerald-700"
              >
                확인
              </Button>
            ) : (
              <Button
                type="button"
                onClick={onClose}
                variant="outline"
                className="h-12 w-full rounded-full text-sm font-semibold"
              >
                닫기
              </Button>
            )}
          </div>
        ) : null}
      </DialogContent>
    </Dialog>
  )
}
