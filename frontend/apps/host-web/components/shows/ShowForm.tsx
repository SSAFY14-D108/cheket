"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import Image from "next/image"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { LoginInput } from "@/components/common/LoginInput"
import { LoginButton } from "@/components/common/LoginButton"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { ArrowLeft, Upload, Plus, Trash2 } from "lucide-react"
import Link from "next/link"
import type { Event } from "@/lib/mock-data"

interface SeatPrice {
    section: string
    price: string
}

interface RevenueShare {
    label: string
    percentage: string
}

interface ShowFormProps {
    mode: "create" | "edit"
    initialData?: Event
}

export function ShowForm({ mode, initialData }: ShowFormProps) {
    const router = useRouter()
    const [title, setTitle] = useState(initialData?.title ?? "")
    const [date, setDate] = useState(initialData?.date ?? "")
    const [bookingStart, setBookingStart] = useState(initialData?.bookingStartDate ?? "")
    const [bookingEnd, setBookingEnd] = useState(initialData?.bookingEndDate ?? "")
    const [location, setLocation] = useState(initialData?.location ?? "")
    const [description, setDescription] = useState(initialData?.description ?? "")
    const [refundPolicy, setRefundPolicy] = useState(initialData?.refundPolicy ?? "")
    const [maxPurchase, setMaxPurchase] = useState(initialData?.maxPurchase?.toString() ?? "")
    const [posterPreview, setPosterPreview] = useState<string | null>(
        initialData?.posterUrl ?? null
    )
    const [seatPrices, setSeatPrices] = useState<SeatPrice[]>(
        initialData?.seatPrices?.map((s) => ({
            section: s.section,
            price: s.price.toString(),
        })) ?? [{ section: "", price: "" }]
    )
    const [revenueShares, setRevenueShares] = useState<RevenueShare[]>(
        initialData?.revenueDistribution?.map((r) => ({
            label: r.label,
            percentage: r.percentage.toString(),
        })) ?? [
            { label: "소속사", percentage: "50" },
            { label: "가수", percentage: "30" },
            { label: "기획자", percentage: "20" },
        ]
    )

    const isEdit = mode === "edit"
    const headerTitle = isEdit ? "공연 수정" : "공연 등록"
    const submitLabel = isEdit ? "수정하기" : "등록하기"

    const handlePosterChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0]
        if (file) {
            const reader = new FileReader()
            reader.onload = () => setPosterPreview(reader.result as string)
            reader.readAsDataURL(file)
        }
    }

    const addSeatPrice = () => {
        setSeatPrices((prev) => [...prev, { section: "", price: "" }])
    }

    const removeSeatPrice = (index: number) => {
        setSeatPrices((prev) => prev.filter((_, i) => i !== index))
    }

    const updateSeatPrice = (index: number, field: keyof SeatPrice, value: string) => {
        setSeatPrices((prev) =>
            prev.map((item, i) => (i === index ? { ...item, [field]: value } : item))
        )
    }

    const updateRevenueShare = (index: number, field: keyof RevenueShare, value: string) => {
        setRevenueShares((prev) =>
            prev.map((item, i) => (i === index ? { ...item, [field]: value } : item))
        )
    }

    const handleSubmit = async () => {
        if (!title.trim() || !date.trim() || !location.trim()) {
            alert("공연명, 일시, 장소는 필수 입력 항목입니다.")
            return
        }

        const payload = {
            title,
            date,
            bookingStartDate: bookingStart,
            bookingEndDate: bookingEnd,
            location,
            description,
            refundPolicy,
            maxPurchase: Number(maxPurchase),
            seatPrices: seatPrices.map((s) => ({
                section: s.section,
                price: Number(s.price),
            })),
            revenueDistribution: revenueShares.map((r) => ({
                label: r.label,
                percentage: Number(r.percentage),
            })),
        }

        if (isEdit && initialData) {
            // TODO: PUT api/v1/host/shows/{showId}
            console.log(`PUT /api/v1/host/shows/${initialData.id}`, payload)
            alert("공연이 수정되었습니다.")
        } else {
            // TODO: POST api/v1/host/shows
            console.log("POST /api/v1/host/shows", payload)
            alert("공연이 등록되었습니다.")
        }

        router.push("/mypage")
    }

    return (
        <main className="mx-auto max-w-5xl px-6 py-10">
            {/* 헤더 */}
            <div className="flex items-center gap-3">
                <Link
                    href={isEdit && initialData ? `/shows/${initialData.id}` : "/mypage"}
                    className="flex size-9 items-center justify-center rounded-sm bg-secondary text-secondary-foreground transition-colors hover:bg-secondary/80"
                    aria-label="뒤로가기"
                >
                    <ArrowLeft className="size-4" />
                </Link>
                <h1 className="text-2xl font-bold text-foreground">{headerTitle}</h1>
            </div>

            <div className="mt-8 flex flex-col gap-8 lg:flex-row">
                {/* 좌측: 포스터 업로드 */}
                <div className="w-full lg:w-80 shrink-0">
                    <Card className="overflow-hidden">
                        <CardContent className="p-0">
                            <label
                                htmlFor="poster-upload"
                                className="group flex cursor-pointer flex-col items-center justify-center"
                            >
                                {posterPreview ? (
                                    <div className="relative aspect-[3/4] w-full">
                                        <Image
                                            src={posterPreview}
                                            alt="업로드된 포스터 미리보기"
                                            fill
                                            className="object-cover"
                                        />
                                    </div>
                                ) : (
                                    <div className="flex aspect-[3/4] w-full flex-col items-center justify-center gap-3 bg-secondary transition-colors group-hover:bg-secondary/80">
                                        <Upload className="size-10 text-muted-foreground" />
                                        <span className="text-sm text-muted-foreground">포스터 이미지 업로드</span>
                                    </div>
                                )}
                            </label>
                            <input
                                id="poster-upload"
                                type="file"
                                accept="image/*"
                                className="sr-only"
                                onChange={handlePosterChange}
                            />
                        </CardContent>
                    </Card>
                </div>

                {/* 우측: 폼 필드 */}
                <div className="flex flex-1 flex-col gap-6">
                    {/* 기본 정보 */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-base">기본 정보</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-4">
                            <div className="flex flex-col gap-1.5">
                                <Label htmlFor="event-title">공연명</Label>
                                <LoginInput
                                    id="event-title"
                                    placeholder="공연명을 입력하세요"
                                    value={title}
                                    onChange={(e) => setTitle(e.target.value)}
                                />
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <Label htmlFor="event-date">공연 일시</Label>
                                <LoginInput
                                    id="event-date"
                                    type="date"
                                    value={date}
                                    onChange={(e) => setDate(e.target.value)}
                                />
                            </div>
                            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                                <div className="flex flex-col gap-1.5">
                                    <Label htmlFor="booking-start">예매 시작일</Label>
                                    <LoginInput
                                        id="booking-start"
                                        type="date"
                                        value={bookingStart}
                                        onChange={(e) => setBookingStart(e.target.value)}
                                    />
                                </div>
                                <div className="flex flex-col gap-1.5">
                                    <Label htmlFor="booking-end">예매 마감일</Label>
                                    <LoginInput
                                        id="booking-end"
                                        type="date"
                                        value={bookingEnd}
                                        onChange={(e) => setBookingEnd(e.target.value)}
                                    />
                                </div>
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <Label htmlFor="event-location">장소</Label>
                                <LoginInput
                                    id="event-location"
                                    placeholder="공연 장소를 입력하세요"
                                    value={location}
                                    onChange={(e) => setLocation(e.target.value)}
                                />
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <Label htmlFor="event-description">공연 설명</Label>
                                <Textarea
                                    id="event-description"
                                    placeholder="공연에 대한 설명을 작성하세요"
                                    value={description}
                                    onChange={(e) => setDescription(e.target.value)}
                                    rows={4}
                                />
                            </div>
                        </CardContent>
                    </Card>

                    {/* 티켓 설정 */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-base">티켓 설정</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-4">
                            <div className="flex flex-col gap-1.5">
                                <Label htmlFor="max-purchase">구매 제한 개수</Label>
                                <LoginInput
                                    id="max-purchase"
                                    type="number"
                                    placeholder="1인당 최대 구매 수량"
                                    value={maxPurchase}
                                    onChange={(e) => setMaxPurchase(e.target.value)}
                                />
                            </div>

                            <Separator />

                            <div className="flex flex-col gap-3">
                                <div className="flex items-center justify-between">
                                    <Label>좌석별 가격</Label>
                                    <button
                                        type="button"
                                        onClick={addSeatPrice}
                                        className="flex items-center gap-1 text-xs text-muted-foreground transition-colors hover:text-foreground"
                                    >
                                        <Plus className="size-3.5" />
                                        추가
                                    </button>
                                </div>
                                {seatPrices.map((seat, index) => (
                                    <div key={index} className="flex items-center gap-2">
                                        <LoginInput
                                            placeholder="좌석 구역 (예: VIP)"
                                            value={seat.section}
                                            onChange={(e) => updateSeatPrice(index, "section", e.target.value)}
                                            className="flex-1"
                                        />
                                        <LoginInput
                                            type="number"
                                            placeholder="가격 (원)"
                                            value={seat.price}
                                            onChange={(e) => updateSeatPrice(index, "price", e.target.value)}
                                            className="flex-1"
                                        />
                                        {seatPrices.length > 1 && (
                                            <button
                                                type="button"
                                                onClick={() => removeSeatPrice(index)}
                                                className="flex size-9 shrink-0 items-center justify-center rounded-sm text-muted-foreground transition-colors hover:bg-secondary hover:text-destructive"
                                                aria-label="좌석 가격 삭제"
                                            >
                                                <Trash2 className="size-4" />
                                            </button>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </CardContent>
                    </Card>

                    {/* 수익 분배 & 환불 정책 */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-base">수익 분배 및 환불 정책</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-4">
                            <div className="flex flex-col gap-3">
                                <Label>수익 분배 비율 (%)</Label>
                                {revenueShares.map((share, index) => (
                                    <div key={index} className="flex items-center gap-2">
                                        <LoginInput
                                            placeholder="대상"
                                            value={share.label}
                                            onChange={(e) => updateRevenueShare(index, "label", e.target.value)}
                                            className="flex-1"
                                        />
                                        <LoginInput
                                            type="number"
                                            placeholder="%"
                                            value={share.percentage}
                                            onChange={(e) => updateRevenueShare(index, "percentage", e.target.value)}
                                            className="w-24"
                                        />
                                    </div>
                                ))}
                            </div>

                            <Separator />

                            <div className="flex flex-col gap-1.5">
                                <Label htmlFor="refund-policy">환불 정책</Label>
                                <Textarea
                                    id="refund-policy"
                                    placeholder="환불 정책을 입력하세요"
                                    value={refundPolicy}
                                    onChange={(e) => setRefundPolicy(e.target.value)}
                                    rows={3}
                                />
                            </div>
                        </CardContent>
                    </Card>

                    {/* 제출 버튼 */}
                    <LoginButton type="button" variant="primary" onClick={handleSubmit}>
                        {submitLabel}
                    </LoginButton>
                </div>
            </div>
        </main>
    )
}
