"use client"

import { useEffect, useState, type ReactNode } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import {
  ArrowLeft,
  Calendar,
  Heart,
  MapPin,
  Ticket,
  Users,
  Wallet,
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
import { useToast } from "@/hooks/use-toast"
import { ApiError } from "@/lib/api"
import {
  approveShowContract,
  confirmShowContracts,
  deleteShow,
  type HostShowContractApproval,
  type HostShowDetail,
  rejectShowContract,
} from "@/lib/show-manage-api"
import { getShowDisplayMeta } from "@/lib/show-display"
import { ShowTxProgressModal } from "./ShowTxProgressModal"
import {
  FIXED_PLATFORM_STAKEHOLDER,
  PLATFORM_FEE_BPS,
  PLATFORM_TOTAL_BPS,
} from "./showFormUtils"

interface ShowDetailViewProps {
  showDetail: HostShowDetail
  contractApprovals: HostShowContractApproval[]
}

function formatDateTime(value: string) {
  return value.slice(0, 16).replace("T", " ")
}

function formatPercentFromBps(value: number) {
  return `${(value / 100).toFixed(1)}%`
}

function getContractStatusMeta(status: HostShowContractApproval["approvalStatus"]) {
  switch (status) {
    case "APPROVED":
      return {
        label: "승인",
        badgeClassName: "border-emerald-200 bg-emerald-50 text-emerald-700",
        dotClassName: "bg-emerald-500",
      }
    case "REJECTED":
      return {
        label: "거절",
        badgeClassName: "border-rose-200 bg-rose-50 text-rose-700",
        dotClassName: "bg-rose-500",
      }
    default:
      return {
        label: "대기중",
        badgeClassName: "border-amber-200 bg-amber-50 text-amber-700",
        dotClassName: "bg-amber-500",
      }
  }
}

export function ShowDetailView({
  showDetail,
  contractApprovals,
}: ShowDetailViewProps) {
  const router = useRouter()
  const { toast } = useToast()
  const [localContractApprovals, setLocalContractApprovals] =
    useState<HostShowContractApproval[]>(contractApprovals)
  const [isSubmittingFinalRegistration, setIsSubmittingFinalRegistration] =
    useState(false)
  const [contractDecisionAction, setContractDecisionAction] = useState<
    "approve" | "reject" | null
  >(null)
  const [activeTxId, setActiveTxId] = useState<number | null>(null)
  const [isTxModalOpen, setIsTxModalOpen] = useState(false)
  const [selectedDescriptionImage, setSelectedDescriptionImage] = useState<
    string | null
  >(null)

  useEffect(() => {
    setLocalContractApprovals(contractApprovals)
  }, [contractApprovals])

  const displayMeta = getShowDisplayMeta(showDetail)
  const visibleStakeholders = showDetail.stakeholders.filter((stakeholder) => {
    const isPlatformStakeholder =
      stakeholder.name === FIXED_PLATFORM_STAKEHOLDER.name &&
      stakeholder.number === FIXED_PLATFORM_STAKEHOLDER.businessNo
    const hasIdentity = Boolean(
      stakeholder.name?.trim() || stakeholder.number?.trim() || stakeholder.id,
    )

    return !isPlatformStakeholder && hasIdentity && stakeholder.shareBps > 0
  })

  const totalCapacity = showDetail.sessionInfo.reduce(
    (sum, session) => sum + session.capacity,
    0,
  )
  const totalShareBps =
    PLATFORM_FEE_BPS +
    visibleStakeholders.reduce((sum, stakeholder) => sum + stakeholder.shareBps, 0)

  const hasContracts = localContractApprovals.length > 0
  const approvedCount = localContractApprovals.filter(
    (approval) => approval.approvalStatus === "APPROVED",
  ).length
  const pendingCount = localContractApprovals.filter(
    (approval) => approval.approvalStatus === "PENDING",
  ).length
  const rejectedCount = localContractApprovals.filter(
    (approval) => approval.approvalStatus === "REJECTED",
  ).length
  const hasRejectedContract = rejectedCount > 0
  const isAllApproved =
    hasContracts && approvedCount === localContractApprovals.length
  const isPendingContract = showDetail.status === "PENDING_CONTRACT"
  const hasHostPendingApproval = localContractApprovals.some(
    (approval) =>
      approval.userType === "HOST" && approval.approvalStatus === "PENDING",
  )
  const canEditShow = showDetail.status !== "CANCELLED"
  const canConfirmShow = isPendingContract
  const isSubmittingContractDecision = contractDecisionAction !== null
  const confirmButtonDisabled =
    !hasRejectedContract &&
    (!isAllApproved || isSubmittingFinalRegistration || isSubmittingContractDecision)

  const actionHint = hasRejectedContract
    ? "거절된 이해관계자가 있어 공연 수정 후 다시 승인 절차를 진행해야 합니다."
    : !isPendingContract
      ? "최종 등록 이후에는 공연명, 상세 설명, 포스터와 설명 이미지만 수정할 수 있습니다."
      : isAllApproved
        ? "모든 이해관계자 승인이 완료되어 최종등록을 진행할 수 있습니다."
        : "아직 승인 대기 중인 이해관계자가 있어 최종등록 버튼이 비활성화됩니다."

  const summaryItems = [
    {
      label: "좋아요",
      value: `${showDetail.likeCount.toLocaleString()}`,
      hint: "관심도",
      icon: Heart,
    },
    {
      label: "총 좌석",
      value: `${totalCapacity.toLocaleString()}석`,
      hint: `${showDetail.sessionInfo.length}회차`,
      icon: Ticket,
    },
    {
      label: "승인 현황",
      value: `${approvedCount}/${localContractApprovals.length || 0}`,
      hint: hasRejectedContract ? "거절 포함" : "완료 수",
      icon: Users,
    },
    {
      label: "플랫폼 수수료",
      value: formatPercentFromBps(PLATFORM_FEE_BPS),
      hint: "고정 비율",
      icon: Wallet,
    },
  ]

  const handleEdit = () => {
    router.push(`/shows/${showDetail.showId}/edit`)
  }

  const handleDelete = async () => {
    if (!window.confirm("공연을 정말 삭제하시겠습니까?")) {
      return
    }

    try {
      const response = await deleteShow(showDetail.showId)
      window.alert(response.responseMessage || "공연이 삭제되었습니다.")
      router.push("/mypage")
    } catch (error) {
      toast({
        title: "공연 삭제 실패",
        description:
          error instanceof ApiError
            ? error.message
            : "공연 삭제 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
        variant: "destructive",
      })
    }
  }

  const handleFinalRegistration = async () => {
    try {
      setIsSubmittingFinalRegistration(true)
      const response = await confirmShowContracts(showDetail.showId)
      const txId = response.data?.txId

      if (typeof txId === "number" && txId > 0) {
        setActiveTxId(txId)
        setIsTxModalOpen(true)
      } else {
        window.alert(response.responseMessage || "최종등록이 완료되었습니다.")
        router.refresh()
      }
    } catch (error) {
      toast({
        title: "최종등록 실패",
        description:
          error instanceof ApiError
            ? error.message
            : "최종등록 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
        variant: "destructive",
      })
    } finally {
      setIsSubmittingFinalRegistration(false)
    }
  }

  const handleApproveContract = async () => {
    try {
      setContractDecisionAction("approve")
      const response = await approveShowContract(showDetail.showId)
      setLocalContractApprovals((currentApprovals) =>
        currentApprovals.map((approval) =>
          approval.userType === "HOST" && approval.approvalStatus === "PENDING"
            ? {
                ...approval,
                approvalStatus: "APPROVED",
                determinedAt: new Date().toISOString(),
              }
            : approval,
        ),
      )
      toast({
        title: "계약 승인 완료",
        description: response.responseMessage || "주최측 계약 승인이 완료되었습니다.",
      })
      router.refresh()
    } catch (error) {
      toast({
        title: "계약 승인 실패",
        description:
          error instanceof ApiError
            ? error.message
            : "계약 승인 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
        variant: "destructive",
      })
    } finally {
      setContractDecisionAction(null)
    }
  }

  const handleRejectContract = async () => {
    if (!window.confirm("이 계약을 거절하시겠습니까? 거절 후에는 공연 수정이 필요할 수 있습니다.")) {
      return
    }

    try {
      setContractDecisionAction("reject")
      const response = await rejectShowContract(showDetail.showId)
      setLocalContractApprovals((currentApprovals) =>
        currentApprovals.map((approval) =>
          approval.userType === "HOST" && approval.approvalStatus === "PENDING"
            ? {
                ...approval,
                approvalStatus: "REJECTED",
                determinedAt: new Date().toISOString(),
              }
            : approval,
        ),
      )
      toast({
        title: "계약 거절 완료",
        description: response.responseMessage || "주최측 계약이 거절 처리되었습니다.",
      })
      router.refresh()
    } catch (error) {
      toast({
        title: "계약 거절 실패",
        description:
          error instanceof ApiError
            ? error.message
            : "계약 거절 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
        variant: "destructive",
      })
    } finally {
      setContractDecisionAction(null)
    }
  }

  return (
    <main className="mx-auto max-w-7xl px-6 py-8 lg:px-10 lg:py-10">
      <section className="overflow-hidden rounded-[2rem] border border-black/8 bg-white">
        <div className="border-b border-black/6 px-6 py-5 lg:px-8">
          <div className="flex items-start gap-4">
            <Link
              href="/mypage"
              className="mt-1 flex size-11 shrink-0 items-center justify-center rounded-full border border-black/10 bg-white text-black transition-colors hover:bg-black/[0.03]"
              aria-label="목록으로 돌아가기"
            >
              <ArrowLeft className="size-4" />
            </Link>

            <div className="min-w-0 flex-1 space-y-4">
              <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div className="space-y-2">
                  <p className="text-[0.72rem] font-medium uppercase tracking-[0.28em] text-black/38">
                    Show detail
                  </p>
                  <div className="space-y-3">
                    <h1 className="text-[clamp(2rem,4vw,3.2rem)] font-semibold tracking-[-0.06em] text-black">
                      {showDetail.title}
                    </h1>
                    <div className="flex flex-wrap gap-2">
                      {displayMeta.badges.map((badge) => (
                        <Badge
                          key={`${badge.phase}-${badge.label}`}
                          variant="outline"
                          className="rounded-full border-black/10 bg-black/[0.03] px-3 py-1 text-black/70"
                        >
                          {badge.label}
                        </Badge>
                      ))}
                    </div>
                  </div>
                </div>

                <div className="flex flex-wrap gap-2 text-sm text-black/55">
                  <span className="rounded-full bg-black/[0.04] px-3 py-1.5">
                    {showDetail.venue.name}
                  </span>
                  <span className="rounded-full bg-black/[0.04] px-3 py-1.5">
                    {showDetail.show.showStartDate} - {showDetail.show.showEndDate}
                  </span>
                </div>
              </div>

              <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                {summaryItems.map((item) => {
                  const Icon = item.icon

                  return (
                    <div
                      key={item.label}
                      className="rounded-[1.25rem] border border-black/8 bg-[#f7f7f8] px-4 py-4 transition-colors hover:bg-black/[0.035]"
                    >
                      <div className="flex items-center justify-between gap-3">
                        <p className="text-sm text-black/48">{item.label}</p>
                        <Icon className="size-4 text-black/36" />
                      </div>
                      <p className="mt-3 text-2xl font-semibold tracking-[-0.05em] text-black">
                        {item.value}
                      </p>
                      <p className="mt-1 text-xs text-black/42">{item.hint}</p>
                    </div>
                  )
                })}
              </div>
            </div>
          </div>
        </div>

        <div className="grid gap-8 px-6 py-6 lg:grid-cols-[320px_minmax(0,1fr)] lg:px-8 lg:py-8">
          <aside className="space-y-6 lg:sticky lg:top-8 lg:self-start">
            <div className="overflow-hidden rounded-[1.8rem] border border-black/8 bg-[#f4f4f5]">
              <div className="aspect-[4/5] w-full bg-black/[0.04]">
                <img
                  src={showDetail.posterUrl}
                  alt={`${showDetail.title} 포스터`}
                  className="h-full w-full object-cover"
                />
              </div>
            </div>

            <div className="rounded-[1.6rem] border border-black/8 bg-white p-5">
              <div className="space-y-1">
                <p className="text-[0.72rem] font-medium uppercase tracking-[0.24em] text-black/36">
                  Action status
                </p>
                <h2 className="text-xl font-semibold tracking-[-0.04em] text-black">
                  등록 진행 상태
                </h2>
              </div>

              <div className="mt-5 rounded-[1.25rem] bg-[#f7f7f8] p-4">
                <div className="flex items-center justify-between gap-3">
                  <span className="text-sm text-black/50">현재 상태</span>
                  <span className="text-sm font-semibold text-black">
                    {hasRejectedContract
                      ? "수정 필요"
                      : isAllApproved
                        ? "최종등록 가능"
                        : "승인 대기"}
                  </span>
                </div>
                <p className="mt-3 text-sm leading-6 text-black/56">
                  {actionHint}
                </p>
              </div>

              <div className="mt-5">
                <div className="space-y-3">
                  {isPendingContract && hasHostPendingApproval ? (
                    <>
                      <Button
                        type="button"
                        onClick={handleApproveContract}
                        disabled={isSubmittingContractDecision}
                        className="h-12 w-full rounded-full text-sm font-semibold"
                      >
                        {contractDecisionAction === "approve" ? "승인 처리중..." : "계약 승인"}
                      </Button>
                      <Button
                        type="button"
                        onClick={handleRejectContract}
                        disabled={isSubmittingContractDecision}
                        variant="outline"
                        className="h-12 w-full rounded-full border-amber-300 text-sm font-semibold text-amber-700 hover:bg-amber-50 hover:text-amber-800"
                      >
                        {contractDecisionAction === "reject" ? "거절 처리중..." : "계약 거절"}
                      </Button>
                    </>
                  ) : null}
                  {canConfirmShow ? (
                    <Button
                      type="button"
                      onClick={handleFinalRegistration}
                      disabled={confirmButtonDisabled}
                      variant="outline"
                      className="h-12 w-full rounded-full text-sm font-semibold"
                    >
                      {isSubmittingFinalRegistration ? "처리중..." : "최종등록"}
                    </Button>
                  ) : null}
                </div>
              </div>
            </div>

            <div className="rounded-[1.6rem] border border-black/8 bg-white p-5">
              <div className="space-y-1">
                <p className="text-[0.72rem] font-medium uppercase tracking-[0.24em] text-black/36">
                  Manage show
                </p>
                <h2 className="text-xl font-semibold tracking-[-0.04em] text-black">
                  공연 관리
                </h2>
              </div>

              <div className="mt-5 space-y-3">
                {canEditShow ? (
                  <Button
                    type="button"
                    onClick={handleEdit}
                    className="h-12 w-full rounded-full text-sm font-semibold"
                  >
                    공연 수정
                  </Button>
                ) : null}
                <Button
                  type="button"
                  onClick={handleDelete}
                  variant="outline"
                  className="h-12 w-full rounded-full border-destructive text-sm font-semibold text-destructive hover:bg-destructive hover:text-primary-foreground"
                >
                  삭제하기
                </Button>
              </div>
            </div>
          </aside>

          <div className="space-y-6">
            <DetailSection
              eyebrow="Overview"
              title="공연 기본 정보"
              contentClassName="space-y-4"
            >
              <InfoRow label="공연명" value={showDetail.title} />
              <InfoRow
                label="공연 기간"
                value={`${showDetail.show.showStartDate} ~ ${showDetail.show.showEndDate}`}
                icon={<Calendar className="size-4 text-black/40" />}
              />
              <InfoRow
                label="공연장"
                value={`${showDetail.venue.name} / ${showDetail.venue.address}`}
                icon={<MapPin className="size-4 text-black/40" />}
              />
              <InfoRow
                label="예매 기간"
                value={`${formatDateTime(showDetail.reservation.startDate)} ~ ${formatDateTime(showDetail.reservation.endDate)}`}
              />
              <div className="rounded-[1.25rem] border border-black/8 bg-white px-5 py-5">
                <p className="text-sm text-black/45">공연 소개</p>
                <p className="mt-3 whitespace-pre-line text-[15px] leading-7 text-black/82">
                  {showDetail.description}
                </p>
              </div>

              {showDetail.descriptionImages?.length ? (
                <div className="space-y-3">
                  <SectionLabel>상세 소개 이미지</SectionLabel>
                  <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
                    {showDetail.descriptionImages.map((imageUrl, index) => (
                      <button
                        type="button"
                        key={`${imageUrl}-${index}`}
                        onClick={() => setSelectedDescriptionImage(imageUrl)}
                        className="overflow-hidden rounded-[1.1rem] border border-black/8 bg-[#f7f7f8] text-left transition-transform hover:scale-[1.01]"
                      >
                        <div className="aspect-[5/4] w-full bg-black/[0.04]">
                          <img
                            src={imageUrl}
                            alt={`${showDetail.title} 상세 이미지 ${index + 1}`}
                            className="h-full w-full object-cover transition-transform duration-300 hover:scale-[1.02]"
                          />
                        </div>
                      </button>
                    ))}
                  </div>
                </div>
              ) : null}
            </DetailSection>

            <DetailSection
              eyebrow="Ticketing"
              title="예매 및 좌석 정보"
              contentClassName="space-y-5"
            >
              <div className="grid gap-3 md:grid-cols-2">
                <MetricPanel
                  label="구매 제한"
                  value={`1인당 ${showDetail.purchaseLimit}매`}
                />
                <MetricPanel
                  label="총 좌석 수"
                  value={`${totalCapacity.toLocaleString()}석`}
                />
              </div>

              <div className="space-y-3">
                <SectionLabel>좌석 등급</SectionLabel>
                <div className="flex flex-wrap gap-2.5">
                  {showDetail.grade.map((seat, index) => (
                    <div
                      key={`${seat.sectionId}-${index}`}
                      className="rounded-full border border-black/8 bg-[#f7f7f8] px-4 py-2 text-sm text-black/78"
                    >
                      <span
                        className="mr-2 inline-block size-2.5 rounded-full align-middle"
                        style={{ backgroundColor: seat.colorCode }}
                      />
                      <span className="font-medium">{seat.gradeName}</span>
                      <span className="ml-2 text-black/52">
                        {seat.price.toLocaleString()}SSF
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="space-y-3">
                <SectionLabel>회차 정보</SectionLabel>
                <div className="space-y-3">
                  {showDetail.sessionInfo.map((session, index) => (
                    <div
                      key={session.sessionId}
                      className="flex flex-col gap-3 rounded-[1.25rem] border border-black/8 bg-[#f7f7f8] px-5 py-4 md:flex-row md:items-center md:justify-between"
                    >
                      <div>
                        <p className="text-[0.72rem] font-medium uppercase tracking-[0.24em] text-black/34">
                          Session {index + 1}
                        </p>
                        <p className="mt-2 text-lg font-semibold tracking-[-0.03em] text-black">
                          {session.sessionDate} {session.sessionStartDate}
                        </p>
                      </div>
                      <div className="rounded-full bg-white px-4 py-2 text-sm font-medium text-black/72">
                        좌석 {session.capacity.toLocaleString()}석
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </DetailSection>

            <DetailSection
              eyebrow="Settlement"
              title="수익 분배 및 환불 정책"
              contentClassName="space-y-5"
            >
              <div className="space-y-3">
                <SectionLabel>수익 분배</SectionLabel>
                <p className="text-sm leading-6 text-black/50">
                  전체 분배 비율은 {PLATFORM_TOTAL_BPS.toLocaleString()}bps 기준이며,
                  플랫폼 수수료 {PLATFORM_FEE_BPS.toLocaleString()}bps가 포함됩니다.
                </p>
                <div className="space-y-2.5">
                  <SplitRow
                    label="플랫폼"
                    description={`${PLATFORM_FEE_BPS.toLocaleString()}bps 고정`}
                    value={formatPercentFromBps(PLATFORM_FEE_BPS)}
                  />
                  {visibleStakeholders.map((stakeholder, index) => (
                    <SplitRow
                      key={`split-${stakeholder.id ?? index}`}
                      label={stakeholder.name?.trim() || `이해관계자 ${stakeholder.id}`}
                      description="배분 비율"
                      value={formatPercentFromBps(stakeholder.shareBps)}
                    />
                  ))}
                </div>
                <p className="text-sm text-black/48">
                  총 배분 비율 {totalShareBps.toLocaleString()} /{" "}
                  {PLATFORM_TOTAL_BPS.toLocaleString()}bps
                </p>
              </div>

              <div className="space-y-3 border-t border-black/8 pt-5">
                <SectionLabel>환불 정책</SectionLabel>
                <div className="space-y-2.5">
                  {showDetail.refundPolicy.map((policy, index) => (
                    <div
                      key={index}
                      className="rounded-[1.1rem] bg-[#f7f7f8] px-4 py-3 text-sm text-black/78"
                    >
                      공연 {policy.daysRemaining}일 전까지 {policy.refundRate}% 환불
                    </div>
                  ))}
                </div>
              </div>
            </DetailSection>

            {hasContracts ? (
              <DetailSection
                eyebrow="Approval flow"
                title="이해관계자 승인 현황"
                contentClassName="space-y-5"
              >
                <div className="grid gap-3 md:grid-cols-3">
                  <MetricPanel label="승인 완료" value={`${approvedCount}명`} />
                  <MetricPanel label="대기중" value={`${pendingCount}명`} />
                  <MetricPanel label="거절" value={`${rejectedCount}명`} />
                </div>

                <div className="space-y-3">
                  {localContractApprovals.map((approval) => {
                    const statusMeta = getContractStatusMeta(approval.approvalStatus)

                    return (
                      <div
                        key={`${approval.userType}-${approval.userId}`}
                        className="flex flex-col gap-3 rounded-[1.25rem] border border-black/8 bg-white px-5 py-4 transition-colors hover:bg-black/[0.02] md:flex-row md:items-center md:justify-between"
                      >
                        <div className="min-w-0">
                          <div className="flex items-center gap-2">
                            <span
                              className={`size-2 rounded-full ${statusMeta.dotClassName}`}
                            />
                            <p className="text-base font-semibold text-black">
                              {approval.username || "이해관계자"}
                            </p>
                          </div>
                          <p className="mt-2 text-sm text-black/48">
                            {approval.userType === "HOST" ? "주최자" : "아티스트"}
                            {approval.determinedAt
                              ? ` · ${formatDateTime(approval.determinedAt)}`
                              : ""}
                          </p>
                        </div>
                        <Badge
                          variant="outline"
                          className={`w-fit rounded-full px-3 py-1 ${statusMeta.badgeClassName}`}
                        >
                          {statusMeta.label}
                        </Badge>
                      </div>
                    )
                  })}
                </div>

                <div className="rounded-[1.25rem] bg-[#f7f7f8] px-5 py-4 text-sm leading-6 text-black/58">
                  {actionHint}
                </div>
              </DetailSection>
            ) : null}
          </div>
        </div>
      </section>

      <Dialog
        open={Boolean(selectedDescriptionImage)}
        onOpenChange={(open) => {
          if (!open) {
            setSelectedDescriptionImage(null)
          }
        }}
      >
        <DialogContent
          showCloseButton
          className="max-h-[92vh] max-w-5xl overflow-hidden border-black/10 bg-white p-3 sm:max-w-5xl"
        >
          <DialogHeader className="sr-only">
            <DialogTitle>상세 소개 이미지 원본 보기</DialogTitle>
            <DialogDescription>
              공연 상세 소개 이미지를 원본 비율에 가깝게 크게 봅니다.
            </DialogDescription>
          </DialogHeader>

          {selectedDescriptionImage ? (
            <div className="flex max-h-[86vh] items-center justify-center overflow-auto rounded-[1.25rem] bg-[#f4f4f5] p-2">
              <img
                src={selectedDescriptionImage}
                alt={`${showDetail.title} 상세 소개 원본 이미지`}
                className="h-auto max-h-[82vh] w-auto max-w-full rounded-[1rem] object-contain"
              />
            </div>
          ) : null}
        </DialogContent>
      </Dialog>

      <ShowTxProgressModal
        txId={activeTxId}
        open={isTxModalOpen}
        onClose={() => {
          setIsTxModalOpen(false)
          setActiveTxId(null)
        }}
        onConfirmed={() => {
          setIsTxModalOpen(false)
          setActiveTxId(null)
          router.refresh()
        }}
      />
    </main>
  )
}

function DetailSection({
  eyebrow,
  title,
  children,
  contentClassName,
}: {
  eyebrow: string
  title: string
  children: ReactNode
  contentClassName?: string
}) {
  return (
    <section className="rounded-[1.6rem] border border-black/8 bg-white">
      <div className="border-b border-black/6 px-5 py-5 lg:px-6">
        <p className="text-[0.72rem] font-medium uppercase tracking-[0.24em] text-black/36">
          {eyebrow}
        </p>
        <h2 className="mt-2 text-xl font-semibold tracking-[-0.04em] text-black">
          {title}
        </h2>
      </div>
      <div className={`px-5 py-5 lg:px-6 lg:py-6 ${contentClassName ?? ""}`}>
        {children}
      </div>
    </section>
  )
}

function SectionLabel({ children }: { children: ReactNode }) {
  return <p className="text-sm font-medium text-black/52">{children}</p>
}

function MetricPanel({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-[1.15rem] bg-[#f7f7f8] px-4 py-4">
      <p className="text-sm text-black/46">{label}</p>
      <p className="mt-2 text-lg font-semibold tracking-[-0.03em] text-black">
        {value}
      </p>
    </div>
  )
}

function SplitRow({
  label,
  description,
  value,
}: {
  label: string
  description: string
  value: string
}) {
  return (
    <div className="flex items-center justify-between gap-4 rounded-[1.15rem] bg-[#f7f7f8] px-4 py-4">
      <div className="min-w-0">
        <p className="text-sm font-medium text-black">{label}</p>
        <p className="mt-1 text-xs text-black/46">{description}</p>
      </div>
      <p className="shrink-0 text-xl font-semibold tracking-[-0.04em] text-black">
        {value}
      </p>
    </div>
  )
}

function InfoRow({
  label,
  value,
  icon,
}: {
  label: string
  value: string
  icon?: ReactNode
}) {
  return (
    <div className="flex flex-col gap-3 rounded-[1.25rem] bg-[#f7f7f8] px-5 py-4 md:flex-row md:items-center md:justify-between">
      <span className="text-sm text-black/46">{label}</span>
      <div className="flex items-center gap-2 text-sm font-medium text-black md:text-right">
        {icon}
        <span>{value}</span>
      </div>
    </div>
  )
}
