"use client"

import type { ReactNode } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Badge } from "@/components/ui/badge"
import { useToast } from "@/hooks/use-toast"
import { ApiError } from "@/lib/api"
import { deleteShow, type HostShowDetail } from "@/lib/show-manage-api"
import { getShowDisplayMeta } from "@/lib/show-display"
import { ArrowLeft, Heart, Calendar, MapPin } from "lucide-react"
import {
    FIXED_PLATFORM_STAKEHOLDER,
    PLATFORM_FEE_BPS,
    PLATFORM_TOTAL_BPS,
} from "./showFormUtils"

interface ShowDetailViewProps {
    showDetail: HostShowDetail
}

function formatDateTime(value: string) {
    return value.slice(0, 16).replace("T", " ")
}

export function ShowDetailView({ showDetail }: ShowDetailViewProps) {
    const router = useRouter()
    const { toast } = useToast()
    const displayMeta = getShowDisplayMeta(showDetail)

    const totalCapacity = showDetail.sessionInfo.reduce(
        (sum, session) => sum + session.capacity,
        0
    )
    const totalShareBps =
        PLATFORM_FEE_BPS +
        showDetail.stakeholders.reduce((sum, stakeholder) => sum + stakeholder.shareBps, 0)

    const handleEdit = () => {
        router.push(`/shows/${showDetail.showId}/edit`)
    }

    const handleDelete = async () => {
        if (!window.confirm("정말로 이 공연을 삭제하시겠습니까?")) {
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
                        : "공연 삭제 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                variant: "destructive",
            })
        }
    }

    return (
        <main className="mx-auto max-w-6xl px-6 py-10">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <Link
                        href="/mypage"
                        className="flex size-9 items-center justify-center rounded-sm bg-secondary text-secondary-foreground transition-colors hover:bg-secondary/80"
                aria-label="운영 홈으로 돌아가기"
                    >
                            <ArrowLeft className="size-4" />
                    </Link>
                    <div className="flex flex-wrap items-center gap-3">
                        <h1 className="text-2xl font-bold text-foreground">{showDetail.title}</h1>
                        <div className="flex flex-wrap gap-1.5">
                            {displayMeta.badges.map((badge) => (
                                <Badge key={`${badge.phase}-${badge.label}`} variant="outline">
                                    {badge.label}
                                </Badge>
                            ))}
                        </div>
                    </div>
                </div>
                <div className="flex items-center gap-1.5 text-muted-foreground">
                    <Heart className="size-4" />
                    <span className="text-sm font-medium">{showDetail.likeCount.toLocaleString()}</span>
                </div>
            </div>

            <div className="mt-8 flex flex-col gap-8 lg:flex-row">
                <div className="w-full shrink-0 lg:w-80">
                    <Card className="gap-0 overflow-hidden py-0">
                        <div className="aspect-[3/4] w-full bg-muted">
                            {/* Detail posters can come from external CDN URLs in the API spec. */}
                            <img
                                src={showDetail.posterUrl}
                                alt={`${showDetail.title} 포스터`}
                                className="h-full w-full object-cover"
                            />
                        </div>
                    </Card>
                </div>

                <div className="flex flex-1 flex-col gap-6">
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-base">기본 정보</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-4">
                            <InfoRow label="공연명" value={showDetail.title} />
                            <Separator />
                            <InfoRow
                                label="공연 기간"
                                value={`${showDetail.show.showStartDate} ~ ${showDetail.show.showEndDate}`}
                                icon={<Calendar className="size-3.5 text-muted-foreground" />}
                            />
                            <Separator />
                            <InfoRow
                                label="장소"
                                value={`${showDetail.venue.name} / ${showDetail.venue.address}`}
                                icon={<MapPin className="size-3.5 text-muted-foreground" />}
                            />
                            <Separator />
                            <InfoRow
                                label="예매 기간"
                                value={`${formatDateTime(showDetail.reservation.startDate)} ~ ${formatDateTime(showDetail.reservation.endDate)}`}
                            />
                            <Separator />
                            <div className="flex flex-col gap-1">
                                <span className="text-sm text-muted-foreground">공연 설명</span>
                                <p className="whitespace-pre-line text-sm leading-relaxed text-foreground">
                                    {showDetail.description}
                                </p>
                            </div>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle className="text-base">티켓 정보</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-4">
                            <InfoRow label="구매 제한" value={`1인당 ${showDetail.purchaseLimit}매`} />
                            <Separator />
                            <InfoRow label="총 좌석 수" value={`${totalCapacity.toLocaleString()}석`} />
                            <Separator />
                            <div className="flex flex-col gap-2">
                                <span className="text-sm text-muted-foreground">좌석별 가격</span>
                                <div className="flex flex-wrap gap-2">
                                    {showDetail.grade.map((seat, index) => (
                                        <Badge
                                            key={`${seat.sectionId}-${index}`}
                                            variant="secondary"
                                            className="text-xs"
                                            style={{ borderLeft: `3px solid ${seat.colorCode}` }}
                                        >
                                            {seat.gradeName}: {seat.price.toLocaleString()}CTK
                                        </Badge>
                                    ))}
                                </div>
                            </div>
                            <Separator />
                            <div className="flex flex-col gap-2">
                                <span className="text-sm text-muted-foreground">회차 정보</span>
                                <div className="flex flex-col gap-1">
                                    {showDetail.sessionInfo.map((session) => (
                                        <p key={session.sessionId} className="text-sm text-foreground">
                                            {session.sessionDate} {session.sessionStartDate} / 정원 {session.capacity.toLocaleString()}명
                                        </p>
                                    ))}
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader>
                            <CardTitle className="text-base">수익 분배 및 환불 정책</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-4">
                            <div className="flex flex-col gap-2">
                                <span className="text-sm text-muted-foreground">수익 분배</span>
                                <p className="text-xs text-muted-foreground">
                                    총 정산 비율은 {PLATFORM_TOTAL_BPS.toLocaleString()}bps이며, 플랫폼 수수료{" "}
                                    {PLATFORM_FEE_BPS.toLocaleString()}bps가 고정 적용됩니다.
                                </p>
                                <div className="flex flex-wrap gap-2">
                                    <Badge variant="secondary" className="text-xs">
                                        {FIXED_PLATFORM_STAKEHOLDER.name}(플랫폼 /{" "}
                                        {FIXED_PLATFORM_STAKEHOLDER.businessNo}){" "}
                                        {(PLATFORM_FEE_BPS / 100).toFixed(1)}%
                                    </Badge>
                                    {showDetail.stakeholders.map((stakeholder, index) => (
                                        <Badge key={index} variant="secondary" className="text-xs">
                                            {stakeholder.name ?? `회원 ${stakeholder.id}`}(
                                            {stakeholder.role === "ORGANIZER" ? "주최" : "아티스트"}){" "}
                                            {(stakeholder.shareBps / 100).toFixed(1)}%
                                        </Badge>
                                    ))}
                                </div>
                                <p className="text-xs text-muted-foreground">
                                    현재 표시 합계 {totalShareBps.toLocaleString()} /{" "}
                                    {PLATFORM_TOTAL_BPS.toLocaleString()}bps
                                </p>
                            </div>
                            <Separator />
                            <div className="flex flex-col gap-1">
                                <span className="text-sm text-muted-foreground">환불 정책</span>
                                <div className="mt-1 flex flex-col gap-1">
                                    {showDetail.refundPolicy.map((policy, index) => (
                                        <p key={index} className="text-sm text-foreground">
                                            - 공연 {policy.daysRemaining}일 전: {policy.refundRate}% 환불
                                        </p>
                                    ))}
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <div className="flex gap-3">
                        <button
                            type="button"
                            onClick={handleEdit}
                            className="flex-1 rounded-sm bg-primary py-3 text-sm font-semibold text-primary-foreground transition-colors hover:bg-primary/90"
                        >
                            수정하기
                        </button>
                        <button
                            type="button"
                            onClick={handleDelete}
                            className="flex-1 rounded-sm border border-destructive bg-card py-3 text-sm font-semibold text-destructive transition-colors hover:bg-destructive hover:text-primary-foreground"
                        >
                            삭제하기
                        </button>
                    </div>
                </div>
            </div>
        </main>
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
        <div className="flex items-start justify-between gap-4">
            <span className="shrink-0 text-sm text-muted-foreground">{label}</span>
            <div className="flex items-center gap-1.5">
                {icon}
                <span className="text-right text-sm font-medium text-foreground">{value}</span>
            </div>
        </div>
    )
}
