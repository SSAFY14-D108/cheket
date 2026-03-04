"use client"

import Image from "next/image"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Heart, Calendar, MapPin } from "lucide-react"
import type { Event } from "@/lib/mock-data"

interface EventDetailViewProps {
    event: Event
}

export function EventDetailView({ event }: EventDetailViewProps) {
    const router = useRouter()

    const handleEdit = () => {
        // TODO: 수정 페이지 이동 또는 수정 모드 전환
        alert("수정하기 기능은 추후 구현됩니다.")
    }

    const handleDelete = () => {
        if (confirm("정말로 이 공연을 삭제하시겠습니까?")) {
            // TODO: 삭제 API 연동
            alert("공연이 삭제되었습니다.")
            router.push("/mypage")
        }
    }

    return (
        <main className="mx-auto max-w-5xl px-6 py-10">
            {/* 헤더 */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <Link
                        href="/mypage"
                        className="flex size-9 items-center justify-center rounded-sm bg-secondary text-secondary-foreground transition-colors hover:bg-secondary/80"
                        aria-label="마이페이지로 돌아가기"
                    >
                        <ArrowLeft className="size-4" />
                    </Link>
                    <h1 className="text-2xl font-bold text-foreground">{event.title}</h1>
                </div>
                <div className="flex items-center gap-1.5 text-muted-foreground">
                    <Heart className="size-4" />
                    <span className="text-sm font-medium">{event.likes.toLocaleString()}</span>
                </div>
            </div>

            <div className="mt-8 flex flex-col gap-8 lg:flex-row">
                {/* 좌측: 포스터 */}
                <div className="w-full lg:w-80 shrink-0">
                    <Card className="overflow-hidden gap-0 py-0">
                        <div className="relative aspect-[3/4] w-full">
                            <Image
                                src={event.posterUrl}
                                alt={`${event.title} 포스터`}
                                fill
                                className="object-cover"
                                sizes="(max-width: 768px) 100vw, 320px"
                            />
                        </div>
                    </Card>
                </div>

                {/* 우측: 상세 정보 (읽기 전용) */}
                <div className="flex flex-1 flex-col gap-6">
                    {/* 기본 정보 */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-base">기본 정보</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-4">
                            <InfoRow label="공연명" value={event.title} />
                            <Separator />
                            <InfoRow
                                label="공연 일시"
                                value={event.date}
                                icon={<Calendar className="size-3.5 text-muted-foreground" />}
                            />
                            <Separator />
                            <InfoRow
                                label="장소"
                                value={event.location}
                                icon={<MapPin className="size-3.5 text-muted-foreground" />}
                            />
                            <Separator />
                            <InfoRow label="예매 기간" value={`${event.bookingStartDate} ~ ${event.bookingEndDate}`} />
                            <Separator />
                            <div className="flex flex-col gap-1">
                                <span className="text-sm text-muted-foreground">공연 설명</span>
                                <p className="text-sm leading-relaxed text-foreground">{event.description}</p>
                            </div>
                        </CardContent>
                    </Card>

                    {/* 티켓 정보 */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-base">티켓 정보</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-4">
                            <InfoRow label="구매 제한" value={`1인당 ${event.maxPurchase}매`} />
                            <Separator />
                            <div className="flex flex-col gap-2">
                                <span className="text-sm text-muted-foreground">좌석별 가격</span>
                                <div className="flex flex-wrap gap-2">
                                    {event.seatPrices.map((seat) => (
                                        <Badge key={seat.section} variant="secondary" className="text-xs">
                                            {seat.section}: {seat.price.toLocaleString()}원
                                        </Badge>
                                    ))}
                                </div>
                            </div>
                            <Separator />
                            <InfoRow
                                label="판매 현황"
                                value={`${event.soldSeats.toLocaleString()} / ${event.totalSeats.toLocaleString()}석 (${((event.soldSeats / event.totalSeats) * 100).toFixed(1)}%)`}
                            />
                        </CardContent>
                    </Card>

                    {/* 수익 분배 & 환불 정책 */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-base">수익 분배 및 환불 정책</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-4">
                            <div className="flex flex-col gap-2">
                                <span className="text-sm text-muted-foreground">수익 분배</span>
                                <div className="flex flex-wrap gap-2">
                                    {event.revenueDistribution.map((r) => (
                                        <Badge key={r.label} variant="secondary" className="text-xs">
                                            {r.label} {r.percentage}%
                                        </Badge>
                                    ))}
                                </div>
                            </div>
                            <Separator />
                            <div className="flex flex-col gap-1">
                                <span className="text-sm text-muted-foreground">환불 정책</span>
                                <p className="text-sm leading-relaxed text-foreground">{event.refundPolicy}</p>
                            </div>
                        </CardContent>
                    </Card>

                    {/* 수정 / 삭제 버튼 */}
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
    icon?: React.ReactNode
}) {
    return (
        <div className="flex items-start justify-between gap-4">
            <span className="text-sm text-muted-foreground shrink-0">{label}</span>
            <div className="flex items-center gap-1.5">
                {icon}
                <span className="text-sm font-medium text-foreground text-right">{value}</span>
            </div>
        </div>
    )
}
