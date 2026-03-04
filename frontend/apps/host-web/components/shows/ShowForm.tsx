"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import Image from "next/image"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { ArrowLeft, Upload, Plus, Trash2 } from "lucide-react"
import Link from "next/link"
import { DescriptionEditor } from "./DescriptionEditor"
import { mockEvents, mockVenues } from "@/lib/mock-data"

interface ShowFormProps {
    mode: "create" | "edit"
    // Using any for initialData here temporarily since mockEvent type doesn't match the new API yet
    initialData?: any
}

interface Grade {
    seatGrade: string
    price: string
}

interface Stakeholder {
    role: "organizer" | "artist"
    name: string
    businessNo?: string
    phone?: string
    shareBps: string
}

interface RefundItem {
    daysRemaining: string
    refundRate: string
    feeDescription: string
}

interface SessionItem {
    sessionNo: number
    startAt: string
    endAt: string
    capacity: string
}

export function ShowForm({ mode, initialData }: ShowFormProps) {
    const router = useRouter()

    // Header & Info
    const [title, setTitle] = useState(initialData?.title ?? "")
    const [posterPreview, setPosterPreview] = useState<string | null>(initialData?.posterUrl ?? null)

    // Main Content
    const [description, setDescription] = useState(initialData?.description ?? "")

    // Settings Panel
    const [venueId, setVenueId] = useState(initialData?.venueId?.toString() ?? "")
    const [showStartAt, setShowStartAt] = useState(initialData?.show?.startAt?.substring(0, 16) ?? "")
    const [showEndAt, setShowEndAt] = useState(initialData?.show?.endAt?.substring(0, 16) ?? "")

    const [openAt, setOpenAt] = useState(initialData?.reservation?.openAt?.substring(0, 16) ?? "")
    const [closeAt, setCloseAt] = useState(initialData?.reservation?.closeAt?.substring(0, 16) ?? "")

    const [purchaseLimit, setPurchaseLimit] = useState(initialData?.purchaseLimit?.toString() ?? "")

    // Arrays
    const [grades, setGrades] = useState<Grade[]>(
        initialData?.grade?.map((g: any) => ({ seatGrade: g.seatGrade, price: g.price.toString() }))
        ?? [{ seatGrade: "", price: "" }]
    )

    const [stakeholders, setStakeholders] = useState<Stakeholder[]>(
        initialData?.stakeholders?.map((s: any) => ({
            role: s.role, name: s.name, businessNo: s.businessNo, phone: s.phone, shareBps: s.shareBps.toString()
        }))
        ?? [
            { role: "organizer", name: "", businessNo: "", shareBps: "" },
            { role: "artist", name: "", phone: "", shareBps: "" }
        ]
    )

    const [refundPolicy, setRefundPolicy] = useState<RefundItem[]>(
        initialData?.refundPolicy?.map((r: any) => ({
            daysRemaining: r.daysRemaining.toString(),
            refundRate: r.refundRate.toString(),
            feeDescription: r.feeDescription
        }))
        ?? [
            { daysRemaining: "14", refundRate: "1.0", feeDescription: "전액 환불" },
            { daysRemaining: "7", refundRate: "0.7", feeDescription: "30% 수수료" }
        ]
    )

    const [sessionInfo, setSessionInfo] = useState<SessionItem[]>(
        initialData?.sessionInfo?.map((s: any) => ({
            sessionNo: s.sessionNo,
            startAt: s.startAt.substring(0, 16),
            endAt: s.endAt.substring(0, 16),
            capacity: s.capacity.toString()
        }))
        ?? [
            { sessionNo: 1, startAt: "", endAt: "", capacity: "" }
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

    const handleSubmit = async () => {
        if (!title.trim() || !venueId) {
            alert("공연명, 장소아이디는 필수입니다.")
            return
        }

        const payload = {
            title,
            posterUrl: posterPreview ?? "https://cdn.example.com/default.jpg", // TODO: 실제 업로드 로직
            venueId: Number(venueId),
            show: {
                startAt: showStartAt ? new Date(showStartAt).toISOString() : null,
                endAt: showEndAt ? new Date(showEndAt).toISOString() : null,
            },
            reservation: {
                openAt: openAt ? new Date(openAt).toISOString() : null,
                closeAt: closeAt ? new Date(closeAt).toISOString() : null,
            },
            description,
            purchaseLimit: Number(purchaseLimit),
            grade: grades.map(g => ({
                seatGrade: g.seatGrade,
                price: Number(g.price)
            })),
            stakeholders: stakeholders.map(s => ({
                role: s.role,
                name: s.name,
                ...(s.role === 'organizer' ? { businessNo: s.businessNo } : {}),
                ...(s.role === 'artist' ? { phone: s.phone } : {}),
                shareBps: Number(s.shareBps)
            })),
            refundPolicy: refundPolicy.map(r => ({
                daysRemaining: Number(r.daysRemaining),
                refundRate: Number(r.refundRate),
                feeDescription: r.feeDescription
            })),
            sessionInfo: sessionInfo.map(s => ({
                sessionNo: s.sessionNo,
                startAt: s.startAt ? new Date(s.startAt).toISOString() : null,
                endAt: s.endAt ? new Date(s.endAt).toISOString() : null,
                capacity: Number(s.capacity)
            }))
        }

        if (isEdit && initialData?.id) {
            console.log(`PUT /api/v1/host/shows/${initialData.id}`, payload)
            alert("공연이 수정되었습니다.")
        } else {
            console.log("POST /api/v1/host/shows", payload)
            alert("공연이 등록되었습니다.")
        }

        router.push("/mypage")
    }

    // Array manipulation helpers
    const addGrade = () => setGrades(prev => [...prev, { seatGrade: "", price: "" }])
    const removeGrade = (idx: number) => setGrades(prev => prev.filter((_, i) => i !== idx))
    const updateGrade = (idx: number, field: keyof Grade, val: string) =>
        setGrades(prev => prev.map((item, i) => i === idx ? { ...item, [field]: val } : item))

    const addStakeholder = () => setStakeholders(prev => [...prev, { role: "artist", name: "", shareBps: "" }])
    const removeStakeholder = (idx: number) => setStakeholders(prev => prev.filter((_, i) => i !== idx))
    const updateStakeholder = (idx: number, field: keyof Stakeholder, val: string) =>
        setStakeholders(prev => prev.map((item, i) => i === idx ? { ...item, [field]: val } : item))

    const addRefund = () => setRefundPolicy(prev => [...prev, { daysRemaining: "", refundRate: "", feeDescription: "" }])
    const removeRefund = (idx: number) => setRefundPolicy(prev => prev.filter((_, i) => i !== idx))
    const updateRefund = (idx: number, field: keyof RefundItem, val: string) =>
        setRefundPolicy(prev => prev.map((item, i) => i === idx ? { ...item, [field]: val } : item))

    const addSession = () => setSessionInfo(prev => [...prev, { sessionNo: prev.length + 1, startAt: "", endAt: "", capacity: "" }])
    const removeSession = (idx: number) => setSessionInfo(prev => prev.filter((_, i) => i !== idx).map((s, i) => ({ ...s, sessionNo: i + 1 })))
    const updateSession = (idx: number, field: keyof SessionItem, val: string | number) =>
        setSessionInfo(prev => prev.map((item, i) => i === idx ? { ...item, [field]: val } : item))

    return (
        <main className="mx-auto max-w-screen-xl px-4 py-8 md:px-6">
            {/* Header & Title Area */}
            <div className="mb-6 flex flex-col gap-4 border-b pb-6">
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <Link
                            href={isEdit && initialData?.id ? `/shows/${initialData.id}` : "/mypage"}
                            className="flex size-9 items-center justify-center rounded-md border bg-background hover:bg-accent hover:text-accent-foreground"
                            aria-label="뒤로가기"
                        >
                            <ArrowLeft className="size-4" />
                        </Link>
                        <span className="text-sm font-medium text-muted-foreground">{headerTitle}</span>
                    </div>
                </div>

                <div>
                    <input
                        type="text"
                        placeholder="멋진 공연 제목을 입력하세요"
                        className="w-full bg-transparent text-3xl md:text-5xl font-bold font-heading placeholder:text-muted-foreground border-none focus:outline-none focus:ring-0 px-0"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                    />
                </div>
            </div>

            {/* Split Layout */}
            <div className="grid grid-cols-1 items-start gap-8 lg:grid-cols-12">

                {/* ----------------- 좌측 영역 (Main Content) ----------------- */}
                <div className="flex flex-col gap-8 lg:col-span-8">
                    {/* 포스터 업로드 */}
                    <div className="rounded-lg border bg-card p-6 shadow-sm">
                        <Label className="text-base font-semibold mb-4 block">대표 포스터</Label>
                        <label
                            htmlFor="poster-upload"
                            className="group flex cursor-pointer flex-col items-center justify-center rounded-md border-2 border-dashed bg-muted/30 transition-colors hover:bg-muted/60"
                        >
                            {posterPreview ? (
                                <div className="relative aspect-[16/9] w-full max-w-2xl overflow-hidden rounded-md">
                                    <Image
                                        src={posterPreview}
                                        alt="업로드된 포스터 미리보기"
                                        fill
                                        className="object-contain"
                                    />
                                </div>
                            ) : (
                                <div className="flex h-64 w-full flex-col items-center justify-center gap-4 text-muted-foreground">
                                    <div className="rounded-full bg-secondary p-4 group-hover:bg-background">
                                        <Upload className="size-8" />
                                    </div>
                                    <div className="text-center">
                                        <p className="font-medium text-foreground">클릭하여 이미지 업로드</p>
                                        <p className="text-sm">PNG, JPG, JPEG (권장 비율 16:9 또는 3:4)</p>
                                    </div>
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
                    </div>

                    {/* 웹 에디터 (공연 설명) */}
                    <DescriptionEditor
                        value={description}
                        onChange={setDescription}
                    />
                </div>

                {/* ----------------- 우측 영역 (Settings Panel) ----------------- */}
                <div className="flex flex-col gap-6 lg:col-span-4 select-none">

                    {/* 기본 설정 */}
                    <Card>
                        <CardHeader className="py-4">
                            <CardTitle className="text-lg">기본 정보</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-4 pb-4">
                            <div className="flex flex-col gap-1.5">
                                <Label className="text-xs">장소 선택</Label>
                                <select
                                    className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
                                    value={venueId}
                                    onChange={(e) => setVenueId(e.target.value)}
                                >
                                    <option value="" disabled>장소를 선택하세요</option>
                                    {mockVenues.map((venue) => (
                                        <option key={venue.id} value={venue.id}>
                                            {venue.name} (최대 {venue.capacity}명)
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <Separator />

                            <Label className="text-sm font-medium">공연 진행 전체 기간</Label>
                            <div className="grid grid-cols-2 gap-2">
                                <div className="flex flex-col gap-1">
                                    <Label className="text-xs text-muted-foreground">시작</Label>
                                    <Input type="datetime-local" className="h-9 px-2 text-xs" value={showStartAt} onChange={e => setShowStartAt(e.target.value)} />
                                </div>
                                <div className="flex flex-col gap-1">
                                    <Label className="text-xs text-muted-foreground">종료</Label>
                                    <Input type="datetime-local" className="h-9 px-2 text-xs" value={showEndAt} onChange={e => setShowEndAt(e.target.value)} />
                                </div>
                            </div>

                            <Separator />

                            <Label className="text-sm font-medium">예매 가능 기간</Label>
                            <div className="grid grid-cols-2 gap-2">
                                <div className="flex flex-col gap-1">
                                    <Label className="text-xs text-muted-foreground">오픈</Label>
                                    <Input type="datetime-local" className="h-9 px-2 text-xs" value={openAt} onChange={e => setOpenAt(e.target.value)} />
                                </div>
                                <div className="flex flex-col gap-1">
                                    <Label className="text-xs text-muted-foreground">마감</Label>
                                    <Input type="datetime-local" className="h-9 px-2 text-xs" value={closeAt} onChange={e => setCloseAt(e.target.value)} />
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    {/* 좌석 및 회차 */}
                    <Card>
                        <CardHeader className="py-4">
                            <CardTitle className="text-lg">티켓 설정</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-5 pb-4">
                            <div className="flex flex-col gap-1.5">
                                <Label className="text-xs">1인당 구매 제한</Label>
                                <Input type="number" placeholder="예: 2" value={purchaseLimit} onChange={e => setPurchaseLimit(e.target.value)} className="h-9" />
                            </div>

                            <div className="flex flex-col gap-2">
                                <div className="flex items-center justify-between">
                                    <Label className="text-sm">좌석 등급 및 가격 (Grade)</Label>
                                    <Button variant="ghost" size="icon" className="h-6 w-6" onClick={addGrade}><Plus className="size-3" /></Button>
                                </div>
                                {grades.map((grade, idx) => (
                                    <div key={'grade' + idx} className="flex gap-2">
                                        <Input placeholder="등급(VIP)" value={grade.seatGrade} onChange={e => updateGrade(idx, 'seatGrade', e.target.value)} className="h-8 text-xs" />
                                        <Input type="number" placeholder="가격" value={grade.price} onChange={e => updateGrade(idx, 'price', e.target.value)} className="h-8 text-xs" />
                                        <Button variant="ghost" size="icon" className="h-8 w-8 text-muted-foreground" onClick={() => removeGrade(idx)}><Trash2 className="size-3" /></Button>
                                    </div>
                                ))}
                            </div>

                            <Separator />

                            <div className="flex flex-col gap-2">
                                <div className="flex items-center justify-between">
                                    <Label className="text-sm">회차 정보 (Session)</Label>
                                    <Button variant="ghost" size="icon" className="h-6 w-6" onClick={addSession}><Plus className="size-3" /></Button>
                                </div>
                                {sessionInfo.map((sess, idx) => (
                                    <div key={'sess' + idx} className="flex flex-col gap-2 bg-muted/40 p-2 rounded border text-xs">
                                        <div className="flex justify-between items-center font-medium">
                                            <span>Session {sess.sessionNo}</span>
                                            <Button variant="ghost" size="icon" className="h-5 w-5 h-auto" onClick={() => removeSession(idx)}><Trash2 className="size-3" /></Button>
                                        </div>
                                        <div className="grid grid-cols-2 gap-1">
                                            <Input type="datetime-local" value={sess.startAt} onChange={e => updateSession(idx, 'startAt', e.target.value)} className="h-7 px-1 text-[10px]" />
                                            <Input type="datetime-local" value={sess.endAt} onChange={e => updateSession(idx, 'endAt', e.target.value)} className="h-7 px-1 text-[10px]" />
                                            <Input type="number" placeholder="수용인원" value={sess.capacity} onChange={e => updateSession(idx, 'capacity', e.target.value)} className="h-7 px-2 text-xs col-span-2" />
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </CardContent>
                    </Card>

                    {/* 정산 및 환불 */}
                    <Card>
                        <CardHeader className="py-4">
                            <CardTitle className="text-lg">정산 및 정책</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col gap-5 pb-4">
                            <div className="flex flex-col gap-2">
                                <div className="flex items-center justify-between">
                                    <Label className="text-sm">수익 분배 (BPS)</Label>
                                    <Button variant="ghost" size="icon" className="h-6 w-6" onClick={addStakeholder}><Plus className="size-3" /></Button>
                                </div>
                                <div className="text-[10px] text-muted-foreground leading-tight">플랫폼 8% 선공제 후 나머지 금액 분배. 총합 10000bps=100%</div>

                                {stakeholders.map((sh, idx) => (
                                    <div key={'sh' + idx} className="flex flex-col gap-1 border-b pb-2 mb-1 last:border-0">
                                        <div className="flex gap-1">
                                            <select
                                                className="h-8 rounded-md border border-input bg-background px-2 py-1 text-xs"
                                                value={sh.role}
                                                onChange={e => updateStakeholder(idx, 'role', e.target.value as any)}
                                            >
                                                <option value="organizer">주최사</option>
                                                <option value="artist">아티스트</option>
                                            </select>
                                            <Input placeholder="이름/법인명" value={sh.name} onChange={e => updateStakeholder(idx, 'name', e.target.value)} className="h-8 text-xs flex-1" />
                                            <Button variant="ghost" size="icon" className="h-8 w-8 text-muted-foreground shrink-0" onClick={() => removeStakeholder(idx)}><Trash2 className="size-3" /></Button>
                                        </div>
                                        <div className="flex gap-1">
                                            {sh.role === 'organizer' ? (
                                                <Input placeholder="사업자번호" value={sh.businessNo} onChange={e => updateStakeholder(idx, 'businessNo', e.target.value)} className="h-8 text-xs" />
                                            ) : (
                                                <Input placeholder="연락처" value={sh.phone} onChange={e => updateStakeholder(idx, 'phone', e.target.value)} className="h-8 text-xs" />
                                            )}
                                            <Input type="number" placeholder="BPS(예:7000)" value={sh.shareBps} onChange={e => updateStakeholder(idx, 'shareBps', e.target.value)} className="h-8 text-xs w-28" />
                                        </div>
                                    </div>
                                ))}
                            </div>

                            <Separator />

                            <div className="flex flex-col gap-2">
                                <div className="flex items-center justify-between">
                                    <Label className="text-sm">환불 정책</Label>
                                    <Button variant="ghost" size="icon" className="h-6 w-6" onClick={addRefund}><Plus className="size-3" /></Button>
                                </div>
                                {refundPolicy.map((ref, idx) => (
                                    <div key={'ref' + idx} className="flex flex-col gap-1 text-xs border bg-muted/20 rounded p-2">
                                        <div className="flex justify-between">
                                            <span>정책 #{idx + 1}</span>
                                            <Button variant="ghost" size="icon" className="h-4 w-4 text-muted-foreground" onClick={() => removeRefund(idx)}><Trash2 className="size-2.5" /></Button>
                                        </div>
                                        <div className="flex gap-1">
                                            <Input type="number" placeholder="N일 전" value={ref.daysRemaining} onChange={e => updateRefund(idx, 'daysRemaining', e.target.value)} className="h-7 px-1 text-[10px] w-1/3" />
                                            <Input type="number" step="0.1" placeholder="환불률(0~1)" value={ref.refundRate} onChange={e => updateRefund(idx, 'refundRate', e.target.value)} className="h-7 px-1 text-[10px] w-1/3" />
                                            <Input placeholder="설명(부분환불)" value={ref.feeDescription} onChange={e => updateRefund(idx, 'feeDescription', e.target.value)} className="h-7 px-1 text-[10px] flex-1" />
                                        </div>
                                    </div>
                                ))}
                            </div>

                        </CardContent>
                    </Card>

                    {/* 제출 버튼 영역 - Floating Effect */}
                    <div className="sticky bottom-6 mt-4">
                        <Button
                            className="w-full h-14 text-lg font-bold shadow-lg hover:shadow-xl transition-all"
                            size="lg"
                            onClick={handleSubmit}
                        >
                            {submitLabel}
                        </Button>
                    </div>

                </div>
            </div>
        </main>
    )
}
