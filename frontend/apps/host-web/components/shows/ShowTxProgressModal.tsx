"use client"

import { useEffect, useRef, useState } from "react"
import {
  AlertTriangle,
  CheckCircle2,
  Circle,
  FileText,
  Loader2,
  MapPin,
  ScrollText,
  ShieldCheck,
  Ticket,
  Users,
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
import {
  fetchTxStatus,
  type HostShowContractApproval,
  type HostShowDetail,
  type TxStatus,
} from "@/lib/show-manage-api"
import {
  FIXED_PLATFORM_STAKEHOLDER,
  PLATFORM_FEE_BPS,
} from "./showFormUtils"

interface ShowTxProgressModalProps {
  txId: number | null
  open: boolean
  showDetail: HostShowDetail
  contractApprovals: HostShowContractApproval[]
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

function getTxStatusMeta(status: TxStatus) {
  switch (status) {
    case "CONFIRMED":
      return {
        badgeLabel: "CONFIRMED",
        badgeClassName: "rounded-full border-emerald-200 bg-white/80 text-emerald-800",
        title: "공연 등록이 블록체인에 확정되었습니다.",
        description: "계약 요약과 트랜잭션 정보가 온체인 기준으로 잠기고 있습니다.",
        progressClassName: "bg-emerald-500",
        progressValue: "100%",
      }
    case "SUBMITTED":
      return {
        badgeLabel: "SUBMITTED",
        badgeClassName: "rounded-full border-emerald-200 bg-white/80 text-emerald-800",
        title: "블록에 포함되어 최종 확정을 기다리고 있습니다.",
        description: "트랜잭션이 네트워크에 제출되었고 현재 처리 순서를 밟고 있습니다.",
        progressClassName: "bg-emerald-500",
        progressValue: "68%",
      }
    case "FAILED":
      return {
        badgeLabel: "FAILED",
        badgeClassName: "rounded-full border-amber-200 bg-amber-50 text-amber-700",
        title: "등록 처리 중 문제가 발생했습니다.",
        description: "네트워크 응답을 다시 확인하거나 잠시 후 재시도해 주세요.",
        progressClassName: "bg-amber-500",
        progressValue: "100%",
      }
    default:
      return {
        badgeLabel: "PENDING",
        badgeClassName: "rounded-full border-emerald-200 bg-white/80 text-emerald-800",
        title: "트랜잭션을 제출하고 블록체인 등록을 시작합니다.",
        description: "공연 계약 정보를 묶어 스마트 컨트랙트에 반영할 준비를 하고 있습니다.",
        progressClassName: "bg-emerald-500",
        progressValue: "34%",
      }
  }
}

function formatHash(hash?: string) {
  if (!hash) {
    return "-"
  }

  return hash.length > 18 ? `${hash.slice(0, 10)}...${hash.slice(-8)}` : hash
}

function formatPercentFromBps(value: number) {
  return `${(value / 100).toFixed(1)}%`
}

export function ShowTxProgressModal({
  txId,
  open,
  showDetail,
  contractApprovals,
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
  const totalCapacity = showDetail.sessionInfo.reduce(
    (sum, session) => sum + session.capacity,
    0,
  )
  const visibleStakeholders = showDetail.stakeholders.filter((stakeholder) => {
    const isPlatformStakeholder =
      stakeholder.name === FIXED_PLATFORM_STAKEHOLDER.name &&
      stakeholder.number === FIXED_PLATFORM_STAKEHOLDER.businessNo
    const hasIdentity = Boolean(
      stakeholder.name?.trim() || stakeholder.number?.trim() || stakeholder.id,
    )

    return !isPlatformStakeholder && hasIdentity && stakeholder.shareBps > 0
  })
  const approvedCount = contractApprovals.filter(
    (approval) => approval.approvalStatus === "APPROVED",
  ).length
  const refundSummary = showDetail.refundPolicy
    .map((policy) => `D-${policy.daysRemaining} ${policy.refundRate}%`)
    .join(" / ")
  const statusMeta = getTxStatusMeta(currentStatus)

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
        className="max-w-5xl gap-0 overflow-hidden rounded-3xl border-black/10 bg-white p-0 sm:min-h-[720px] sm:max-w-5xl"
      >
        <DialogHeader className="border-b border-emerald-100 bg-[radial-gradient(circle_at_top_left,_rgba(16,185,129,0.12),_transparent_40%),linear-gradient(135deg,_rgba(236,253,245,0.96),_rgba(239,246,255,0.95))] px-6 py-6">
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-2">
              <ShieldCheck className="size-5 text-emerald-600" />
              <DialogTitle className="text-lg font-semibold text-slate-950">
                블록체인 등록 진행 중
              </DialogTitle>
            </div>
            <Badge variant="outline" className={statusMeta.badgeClassName}>
              {statusMeta.badgeLabel}
            </Badge>
          </div>
          <DialogDescription className="mt-2 text-sm leading-relaxed text-slate-600">
            공연 정보를 스마트 컨트랙트에 기록하고 있습니다. 이 과정은 보통 수 초에서 수십 초가 소요됩니다.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-0 lg:min-h-[580px] lg:grid-cols-[minmax(0,1.25fr)_360px]">
          <div className="flex min-h-[420px] flex-col">
            <div className="flex-1 bg-[linear-gradient(180deg,rgba(255,255,255,0.96),rgba(247,250,252,0.98))] px-6 py-8">
              <div className="flex h-full flex-col">
                <div className="rounded-[1.75rem] border border-emerald-100 bg-[radial-gradient(circle_at_top_left,_rgba(16,185,129,0.12),_transparent_40%),linear-gradient(180deg,_rgba(255,255,255,0.98),_rgba(248,250,252,0.98))] px-5 py-5 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.35)]">
                  <div className="flex items-start gap-4">
                    <div
                      className={`flex size-12 shrink-0 items-center justify-center rounded-2xl ${
                        isFailed ? "bg-amber-100 text-amber-700" : "bg-emerald-100 text-emerald-700"
                      }`}
                    >
                      {isTerminal && !isFailed ? (
                        <CheckCircle2 className="size-6" />
                      ) : isFailed ? (
                        <AlertTriangle className="size-6" />
                      ) : (
                        <Loader2 className="size-6 animate-spin" />
                      )}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="text-base font-semibold text-slate-950">
                        {statusMeta.title}
                      </p>
                      <p className="mt-2 text-sm leading-6 text-slate-600">
                        {statusMeta.description}
                      </p>
                    </div>
                  </div>
                  <div className="mt-5">
                    <div className="mb-2 flex items-center justify-between text-[11px] font-medium uppercase tracking-[0.18em] text-slate-400">
                      <span>Progress</span>
                      <span>{statusMeta.progressValue}</span>
                    </div>
                    <div className="h-2 overflow-hidden rounded-full bg-emerald-50">
                      <div
                        className={`h-full rounded-full transition-all duration-500 ${statusMeta.progressClassName}`}
                        style={{ width: statusMeta.progressValue }}
                      />
                    </div>
                  </div>
                </div>

                <div className="mt-5 flex-1 space-y-4">
                  {TX_STEPS.map((step, index) => {
                    const isComplete =
                      currentStepIndex > index ||
                      (currentStatus === "CONFIRMED" && step.status === "CONFIRMED")
                    const isCurrent =
                      currentStatus === step.status && currentStatus !== "CONFIRMED" && !isFailed
                    const isPending = !isComplete && !isCurrent

                    return (
                      <div key={step.status} className="relative pl-18">
                        {index < TX_STEPS.length - 1 ? (
                          <div
                            className={`absolute left-[1.45rem] top-12 h-[calc(100%+0.75rem)] w-0.5 ${
                              isComplete ? "bg-emerald-300" : "bg-slate-200"
                            }`}
                          />
                        ) : null}

                        <div
                          className={`absolute left-0 top-1 flex size-12 items-center justify-center rounded-2xl border ${
                            isComplete
                              ? "border-emerald-200 bg-emerald-50 text-emerald-600"
                              : isCurrent
                                ? "border-emerald-200 bg-white text-emerald-600 shadow-[0_12px_30px_-22px_rgba(16,185,129,0.9)]"
                                : "border-slate-200 bg-white text-slate-300"
                          }`}
                        >
                          {isComplete ? (
                            <CheckCircle2 className="size-6" />
                          ) : isCurrent ? (
                            <Loader2 className="size-6 animate-spin" />
                          ) : (
                            <Circle className="size-6" />
                          )}
                        </div>

                        <div
                          className={`rounded-[1.6rem] border px-5 py-4 transition-all ${
                            isComplete
                              ? "border-emerald-100 bg-emerald-50/70"
                              : isCurrent
                                ? "border-emerald-200 bg-white shadow-[0_20px_45px_-30px_rgba(16,185,129,0.45)]"
                                : "border-slate-200 bg-white/75"
                          }`}
                        >
                          <div className="flex items-center justify-between gap-3">
                            <p
                              className={`text-sm font-semibold ${
                                isPending ? "text-slate-400" : "text-slate-950"
                              }`}
                            >
                              {step.label}
                            </p>
                            <span
                              className={`text-[11px] font-medium uppercase tracking-[0.14em] ${
                                isComplete
                                  ? "text-emerald-600"
                                  : isCurrent
                                    ? "text-emerald-500"
                                    : "text-slate-300"
                              }`}
                            >
                              {isComplete ? "Done" : isCurrent ? "In progress" : "Waiting"}
                            </span>
                          </div>
                          <p
                            className={`mt-2 text-sm leading-6 ${
                              isPending ? "text-slate-400" : "text-slate-500"
                            }`}
                          >
                            {step.description}
                          </p>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            </div>

            <div className="border-t border-slate-100 bg-[linear-gradient(180deg,rgba(248,250,252,0.96),rgba(241,245,249,0.98))] px-6 py-5">
              <div className="grid gap-3 sm:grid-cols-2">
                <div className="rounded-2xl border border-slate-200/80 bg-white/90 px-4 py-3 shadow-[0_12px_30px_-24px_rgba(15,23,42,0.35)]">
                  <p className="text-[11px] uppercase tracking-[0.18em] text-slate-400">TX ID</p>
                  <p className="mt-2 font-mono text-sm text-slate-900">{txId ?? "-"}</p>
                </div>
                <div className="rounded-2xl border border-emerald-100 bg-emerald-50/70 px-4 py-3 shadow-[0_12px_30px_-24px_rgba(16,185,129,0.25)]">
                  <p className="text-[11px] uppercase tracking-[0.18em] text-emerald-500">TX Hash</p>
                  <p className="mt-2 font-mono text-sm text-emerald-800">
                    {formatHash(txHash)}
                  </p>
                </div>
              </div>
              {errorMessage ? (
                <p className="mt-3 text-xs text-amber-600">{errorMessage}</p>
              ) : null}
            </div>
          </div>

          <aside className="border-t border-slate-100 bg-[#f7f7f8] px-6 py-6 lg:border-t-0 lg:border-l lg:border-slate-100">
            <div className="flex items-center gap-2">
              <ScrollText className="size-4 text-emerald-600" />
              <h3 className="text-sm font-semibold text-slate-950">계약 등록 요약</h3>
            </div>

            <div className="mt-4 space-y-4">
              <section className="rounded-2xl border border-black/6 bg-white px-4 py-4">
                <div className="flex items-center gap-2 text-xs uppercase tracking-[0.18em] text-slate-400">
                  <FileText className="size-3.5" />
                  Show
                </div>
                <p className="mt-3 text-base font-semibold text-slate-950">
                  {showDetail.title}
                </p>
                <div className="mt-3 space-y-2 text-sm text-slate-600">
                  <div className="flex items-start justify-between gap-3">
                    <span>공연 기간</span>
                    <span className="text-right text-slate-950">
                      {showDetail.show.showStartDate} - {showDetail.show.showEndDate}
                    </span>
                  </div>
                  <div className="flex items-start justify-between gap-3">
                    <span className="flex items-center gap-1.5">
                      <MapPin className="size-3.5 text-slate-400" />
                      공연장
                    </span>
                    <span className="text-right text-slate-950">{showDetail.venue.name}</span>
                  </div>
                </div>
              </section>

              <section className="rounded-2xl border border-black/6 bg-white px-4 py-4">
                <div className="flex items-center gap-2 text-xs uppercase tracking-[0.18em] text-slate-400">
                  <Users className="size-3.5" />
                  Stakeholders
                </div>
                <div className="mt-3 flex items-center justify-between text-sm">
                  <span className="text-slate-500">승인 현황</span>
                  <span className="font-semibold text-slate-950">
                    {approvedCount}/{contractApprovals.length}
                  </span>
                </div>
                <div className="mt-3 space-y-2">
                  {visibleStakeholders.slice(0, 4).map((stakeholder, index) => (
                    <div
                      key={`${stakeholder.id ?? stakeholder.name ?? index}`}
                      className="flex items-center justify-between gap-3 rounded-xl bg-[#f7f7f8] px-3 py-2"
                    >
                      <span className="truncate text-sm text-slate-700">
                        {stakeholder.name?.trim() || `이해관계자 ${index + 1}`}
                      </span>
                      <span className="shrink-0 text-sm font-semibold text-slate-950">
                        {formatPercentFromBps(stakeholder.shareBps)}
                      </span>
                    </div>
                  ))}
                  <div className="flex items-center justify-between gap-3 rounded-xl bg-emerald-50 px-3 py-2">
                    <span className="text-sm text-emerald-800">플랫폼 수수료</span>
                    <span className="text-sm font-semibold text-emerald-900">
                      {formatPercentFromBps(PLATFORM_FEE_BPS)}
                    </span>
                  </div>
                </div>
              </section>

              <section className="rounded-2xl border border-black/6 bg-white px-4 py-4">
                <div className="flex items-center gap-2 text-xs uppercase tracking-[0.18em] text-slate-400">
                  <Ticket className="size-3.5" />
                  Rules
                </div>
                <div className="mt-3 space-y-2 text-sm">
                  <div className="flex items-center justify-between gap-3">
                    <span className="text-slate-500">총 좌석</span>
                    <span className="font-semibold text-slate-950">
                      {totalCapacity.toLocaleString()}석
                    </span>
                  </div>
                  <div className="flex items-center justify-between gap-3">
                    <span className="text-slate-500">구매 제한</span>
                    <span className="font-semibold text-slate-950">
                      1인당 {showDetail.purchaseLimit}매
                    </span>
                  </div>
                  <div className="border-t border-slate-100 pt-2">
                    <p className="text-slate-500">환불 정책</p>
                    <p className="mt-1 text-sm leading-6 text-slate-950">
                      {refundSummary || "환불 정책 정보 없음"}
                    </p>
                  </div>
                </div>
              </section>
            </div>
          </aside>
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
