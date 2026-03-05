"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import Image from "next/image"
import Link from "next/link"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { ArrowLeft, Upload, ImagePlus } from "lucide-react"
import { DescriptionEditor } from "./DescriptionEditor"
import { SettingsCardBasic } from "./SettingsCardBasic"
import { SettingsCardTickets } from "./SettingsCardTickets"
import { SettingsCardPolicies } from "./SettingsCardPolicies"
import { mockVenues } from "@/lib/mock-data"
import type { Grade, Stakeholder, RefundItem } from "./showFormTypes"

interface ShowFormProps {
    mode: "create" | "edit"
    initialData?: any
}

export function ShowForm({ mode, initialData }: ShowFormProps) {
    const router = useRouter()

    // ─── State ───────────────────────────────────────────────
    const [title, setTitle] = useState(initialData?.title ?? "")
    const [posterPreview, setPosterPreview] = useState<string | null>(initialData?.posterUrl ?? null)
    const [description, setDescription] = useState(initialData?.description ?? "")

    const [venueId, setVenueId] = useState(initialData?.venue?.venueId?.toString() ?? "")
    const [showStartAt, setShowStartAt] = useState(initialData?.show?.startAt?.substring(0, 16) ?? "")
    const [showEndAt, setShowEndAt] = useState(initialData?.show?.endAt?.substring(0, 16) ?? "")
    const [openAt, setOpenAt] = useState(initialData?.reservation?.openAt?.substring(0, 16) ?? "")
    const [closeAt, setCloseAt] = useState(initialData?.reservation?.closeAt?.substring(0, 16) ?? "")
    const [purchaseLimit, setPurchaseLimit] = useState(initialData?.purchaseLimit?.toString() ?? "")
    const [ticketEffectId, setTicketEffectId] = useState<number | undefined>(initialData?.ticketEffectId)

    const [grades, setGrades] = useState<Grade[]>(
        initialData?.grade?.map((g: any) => ({
            ...g,
            price: g.price.toString(),
            sectionId: Array.isArray(g.sectionId) ? g.sectionId.join(', ') : (g.sectionId ? String(g.sectionId) : '')
        }))
        ?? [{ gradeName: "VIP", price: "150000", colorCode: "#7C6EF0", sectionId: "" }]
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
            refundRate: r.refundRate.toString()
        }))
        ?? [
            { daysRemaining: "14", refundRate: "100" },
            { daysRemaining: "7", refundRate: "70" }
        ]
    )

    // ─── Computed ─────────────────────────────────────────────
    const isEdit = mode === "edit"
    const headerTitle = isEdit ? "공연 수정" : "공연 등록"
    const submitLabel = isEdit ? "수정하기" : "등록하기"

    // ─── Handlers ─────────────────────────────────────────────
    const handlePosterChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0]
        if (file) {
            const reader = new FileReader()
            reader.onload = () => setPosterPreview(reader.result as string)
            reader.readAsDataURL(file)
        }
    }

    const handleVenueChange = (id: string) => {
        setVenueId(id)
    }

    const addGrade = () => setGrades(prev => [...prev, { sectionId: "", gradeName: "", price: "", colorCode: "#aaaaaa" }])
    const removeGrade = (idx: number) => setGrades(prev => prev.filter((_, i) => i !== idx))
    const updateGrade = (idx: number, field: keyof Grade, val: string) =>
        setGrades(prev => prev.map((item, i) => i === idx ? { ...item, [field]: val } : item))

    // Stakeholder helpers
    const addStakeholder = () => setStakeholders(prev => [...prev, { role: "artist", name: "", shareBps: "" }])
    const removeStakeholder = (idx: number) => setStakeholders(prev => prev.filter((_, i) => i !== idx))
    const updateStakeholder = (idx: number, field: keyof Stakeholder, val: string) =>
        setStakeholders(prev => prev.map((item, i) => i === idx ? { ...item, [field]: val } : item))

    // RefundPolicy helpers
    const addRefund = () => setRefundPolicy(prev => [...prev, { daysRemaining: "", refundRate: "" }])
    const removeRefund = (idx: number) => setRefundPolicy(prev => prev.filter((_, i) => i !== idx))
    const updateRefund = (idx: number, field: keyof RefundItem, val: string) =>
        setRefundPolicy(prev => prev.map((item, i) => i === idx ? { ...item, [field]: val } : item))

    const handleSubmit = async () => {
        if (!title.trim() || !venueId) {
            alert("공연명과 장소는 필수입니다.")
            return
        }

        const payload = {
            title,
            posterUrl: posterPreview ?? "https://cdn.example.com/default.jpg",
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
            ticketEffectId,
            grade: grades.map(g => ({
                gradeName: g.gradeName,
                price: Number(g.price),
                colorCode: g.colorCode,
                sectionId: g.sectionId ? g.sectionId.split(',').map(s => Number(s.trim())).filter(n => !isNaN(n)) : []
            })),
            stakeholders: stakeholders.map(s => ({
                role: s.role, name: s.name,
                ...(s.role === "organizer" ? { businessNo: s.businessNo } : {}),
                ...(s.role === "artist" ? { phone: s.phone } : {}),
                shareBps: Number(s.shareBps)
            })),
            refundPolicy: refundPolicy.map(r => ({
                daysRemaining: Number(r.daysRemaining),
                refundRate: Number(r.refundRate)
            }))
        }

        if (isEdit && initialData?.showId) {
            console.log(`PUT /api/v1/host/shows/${initialData.showId}`, payload)
            alert("공연이 수정되었습니다.")
        } else {
            console.log("POST /api/v1/host/shows", payload)
            alert("공연이 등록되었습니다.")
        }

        router.push("/mypage")
    }

    // ─── Render ───────────────────────────────────────────────
    return (
        <main className="mx-auto max-w-screen-xl px-4 py-8 md:px-6">
            {/* Header & Title */}
            <div className="mb-6 flex flex-col gap-4 border-b pb-6">
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
                <input
                    type="text"
                    placeholder="멋진 공연 제목을 입력하세요"
                    className="w-full bg-transparent text-3xl md:text-5xl font-bold placeholder:text-muted-foreground border-none focus:outline-none focus:ring-0 px-0"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                />
            </div>

            {/* Split Layout */}
            <div className="grid grid-cols-1 items-start gap-8 lg:grid-cols-12">

                {/* Left (Main Content) */}
                <div className="flex flex-col gap-8 lg:col-span-8">
                    {/* 포스터 업로드 */}
                    <div className="rounded-lg border bg-card p-6 shadow-sm">
                        <Label className="text-base font-semibold mb-4 block">대표 포스터</Label>
                        <label
                            htmlFor="poster-upload"
                            className="group flex cursor-pointer flex-col items-center justify-center rounded-md border-2 border-dashed bg-muted/30 transition-colors hover:bg-muted/60"
                        >
                            {posterPreview ? (
                                <div className="flex flex-col items-center gap-3 w-full p-4">
                                    <div className="relative aspect-[3/4] w-full max-w-sm mx-auto overflow-hidden rounded-md border bg-muted/20">
                                        <Image src={posterPreview} alt="포스터 미리보기" fill className="object-contain" />
                                    </div>
                                    <Button type="button" variant="outline" size="sm" className="gap-2" onClick={(e) => { e.preventDefault(); document.getElementById('poster-upload')?.click(); }}>
                                        <ImagePlus className="size-4" />
                                        포스터 변경
                                    </Button>
                                </div>
                            ) : (
                                <div className="flex aspect-[3/4] w-full max-w-sm mx-auto flex-col items-center justify-center gap-4 text-muted-foreground p-6">
                                    <div className="rounded-full bg-secondary p-4 group-hover:bg-background">
                                        <Upload className="size-8" />
                                    </div>
                                    <div className="text-center">
                                        <p className="font-medium text-foreground">클릭하여 포스터 업로드</p>
                                        <p className="text-sm mt-1">세로형 이미지 (권장 비율 3:4)</p>
                                    </div>
                                </div>
                            )}
                        </label>
                        <input id="poster-upload" type="file" accept="image/*" className="sr-only" onChange={handlePosterChange} />
                    </div>

                    {/* 공연 설명 에디터 */}
                    <DescriptionEditor value={description} onChange={setDescription} />
                </div>

                {/* Right (Settings Panel) */}
                <div className="flex flex-col gap-6 lg:col-span-4 select-none">
                    <SettingsCardBasic
                        venueId={venueId}
                        showStartAt={showStartAt}
                        showEndAt={showEndAt}
                        openAt={openAt}
                        closeAt={closeAt}
                        onChangeVenueId={handleVenueChange}
                        onChangeShowRange={(s, e) => { setShowStartAt(s); setShowEndAt(e) }}
                        onChangeReservationRange={(s, e) => { setOpenAt(s); setCloseAt(e) }}
                    />

                    <SettingsCardTickets
                        venueId={venueId}
                        purchaseLimit={purchaseLimit}
                        grades={grades}
                        ticketEffectId={ticketEffectId ? Number(ticketEffectId) : undefined}
                        onChangeTicketEffectId={setTicketEffectId}
                        onChangePurchaseLimit={setPurchaseLimit}
                        onAddGrade={addGrade}
                        onRemoveGrade={removeGrade}
                        onUpdateGrade={updateGrade}
                    />

                    <SettingsCardPolicies
                        stakeholders={stakeholders}
                        refundPolicy={refundPolicy}
                        onAddStakeholder={addStakeholder}
                        onRemoveStakeholder={removeStakeholder}
                        onUpdateStakeholder={updateStakeholder}
                        onAddRefund={addRefund}
                        onRemoveRefund={removeRefund}
                        onUpdateRefund={updateRefund}
                    />

                    {/* Sticky Submit */}
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
