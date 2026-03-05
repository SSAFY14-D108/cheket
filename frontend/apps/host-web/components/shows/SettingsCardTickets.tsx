"use client"

import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Plus, Trash2, Ticket, X, ZoomIn } from "lucide-react"
import { useState, useEffect } from "react"
import type { Grade } from "./showFormTypes"

interface SettingsCardTicketsProps {
    venueId: string
    purchaseLimit: string
    grades: Grade[]
    ticketEffectId?: number
    onChangeTicketEffectId: (val: number) => void
    onChangePurchaseLimit: (val: string) => void
    onAddGrade: () => void
    onRemoveGrade: (idx: number) => void
    onUpdateGrade: (idx: number, field: keyof Grade, val: string) => void
}

export function SettingsCardTickets({
    venueId,
    purchaseLimit,
    grades,
    ticketEffectId,
    onChangeTicketEffectId,
    onChangePurchaseLimit,
    onAddGrade,
    onRemoveGrade,
    onUpdateGrade,
}: SettingsCardTicketsProps) {
    const [isImageModalOpen, setIsImageModalOpen] = useState(false)

    // 장소 ID에 따른 도면 이미지 매핑
    const getVenueMapImg = (id: string) => {
        switch (id) {
            case '1': return '/venue_map/sectionId1.png'
            case '2': return '/venue_map/sectionid2.jpg'
            case '3': return '/venue_map/sectionid3.png'
            case '4': return '/venue_map/sectionid4.jpg'
            default: return '/sectionId.png' // fallback
        }
    }

    const mapImageSrc = getVenueMapImg(venueId)

    // ESC 키로 모달 닫기
    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === 'Escape') setIsImageModalOpen(false)
        }
        if (isImageModalOpen) window.addEventListener('keydown', handleKeyDown)
        return () => window.removeEventListener('keydown', handleKeyDown)
    }, [isImageModalOpen])

    return (
        <Card>
            <CardHeader className="py-4">
                <CardTitle className="text-lg flex items-center gap-2">
                    <Ticket className="size-5 text-primary" />
                    티켓 설정
                </CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-5 pb-4">
                <div className="flex flex-col gap-1.5">
                    <Label className="text-xs">1인당 구매 제한</Label>
                    <Input
                        type="number"
                        placeholder="예: 2"
                        value={purchaseLimit}
                        onChange={e => onChangePurchaseLimit(e.target.value)}
                        className="h-9"
                    />
                </div>

                <div className="flex flex-col gap-2">
                    <div className="flex items-center justify-between">
                        <Label className="text-sm">좌석 등급 및 가격 (Grade)</Label>
                        <Button variant="ghost" size="icon" className="h-6 w-6" onClick={onAddGrade}>
                            <Plus className="size-3" />
                        </Button>
                    </div>

                    {/* 구역 안내 이미지 */}
                    <div className="mb-2 p-3 bg-muted/20 border rounded-lg flex flex-col gap-2 items-center relative group">
                        <Label className="text-xs text-muted-foreground w-full text-left">구역 번호 참조 (sectionId)</Label>
                        <div
                            className="relative cursor-pointer overflow-hidden rounded border border-border/50 bg-background/50 hover:border-primary/50 transition-colors w-full flex justify-center"
                            onClick={() => setIsImageModalOpen(true)}
                        >
                            <img
                                src={mapImageSrc}
                                alt="구역 안내도"
                                className="max-h-48 object-contain transition-transform duration-300 group-hover:scale-[1.02]"
                                onError={(e) => {
                                    (e.target as HTMLImageElement).src = '/sectionId.png';
                                }}
                            />
                            {/* Overlay icon on hover */}
                            <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity duration-300">
                                <span className="text-white text-sm font-medium flex items-center gap-2 bg-black/50 px-3 py-1.5 rounded-full backdrop-blur-sm">
                                    <ZoomIn className="size-4" /> 클릭하여 크게 보기
                                </span>
                            </div>
                        </div>
                    </div>
                    {grades.map((grade, idx) => (
                        <div key={'grade' + idx} className="flex items-center gap-3 p-3 bg-muted/10 border rounded-lg overflow-hidden relative group transition-colors hover:border-primary/40 shadow-sm">
                            {/* 좌측 테마 색상 바 */}
                            <div className="absolute left-0 top-0 bottom-0 w-1.5" style={{ backgroundColor: grade.colorCode || '#ccc' }} />

                            <div className="flex-1 flex items-start gap-2 pl-1">
                                <div className="flex flex-col gap-1.5 w-[25%]">
                                    <Label className="text-[10px] text-muted-foreground uppercase tracking-wider truncate">등급명</Label>
                                    <Input placeholder="예: VIP" value={grade.gradeName} onChange={e => onUpdateGrade(idx, 'gradeName', e.target.value)} className="h-8 text-xs font-medium px-2" />
                                </div>
                                <div className="flex flex-col gap-1.5 w-[35%] relative">
                                    <Label className="text-[10px] text-muted-foreground uppercase tracking-wider truncate">가격(원)</Label>
                                    <Input type="number" placeholder="150000" value={grade.price} onChange={e => onUpdateGrade(idx, 'price', e.target.value)} className="h-8 text-xs pl-5 pr-1" />
                                    <span className="absolute left-1.5 top-[26px] text-[10px] text-muted-foreground font-medium">₩</span>
                                </div>
                                <div className="flex flex-col gap-1.5 flex-1 min-w-0">
                                    <Label className="text-[10px] text-muted-foreground uppercase tracking-wider truncate" title="구역(콤마구분)">구역(콤마)</Label>
                                    <Input placeholder="1, 3" value={grade.sectionId || ''} onChange={e => onUpdateGrade(idx, 'sectionId', e.target.value)} className="h-8 text-xs px-2" />
                                </div>
                            </div>

                            <div className="flex flex-col items-center gap-1.5 shrink-0 ml-1">
                                <Label className="text-[10px] text-muted-foreground uppercase tracking-wider">색상</Label>
                                <div
                                    className="relative flex items-center justify-center size-8 rounded-full border-2 shadow-sm cursor-pointer transition-transform hover:scale-105"
                                    style={{ backgroundColor: grade.colorCode || '#000000', borderColor: 'rgba(255,255,255,0.2)' }}
                                >
                                    <Input
                                        type="color"
                                        value={grade.colorCode || '#000000'}
                                        onChange={e => onUpdateGrade(idx, 'colorCode', e.target.value)}
                                        className="absolute inset-0 opacity-0 w-full h-full cursor-pointer"
                                        title="좌석 색상 선택"
                                    />
                                </div>
                            </div>

                            <Button
                                variant="ghost"
                                size="icon"
                                className="h-8 w-8 shrink-0 text-muted-foreground hover:text-destructive hover:bg-destructive/10 mt-5 ml-1 opacity-0 group-hover:opacity-100 transition-opacity"
                                onClick={() => onRemoveGrade(idx)}
                            >
                                <Trash2 className="size-4" />
                            </Button>
                        </div>
                    ))}
                </div>

                <Separator />


                <div className="flex flex-col gap-3">
                    <Label className="text-sm">티켓 효과 설정 (Ticket Effect)</Label>
                    <div className="grid grid-cols-2 gap-3">
                        <div
                            className={`flex flex-col items-center gap-2 p-3 border rounded-lg cursor-pointer transition-all ${ticketEffectId === 1 ? 'border-primary bg-primary/5 ring-2 ring-primary/20' : 'hover:border-primary/50'}`}
                            onClick={() => onChangeTicketEffectId(1)}
                        >
                            <img src="/ticket_effect/logo.png" alt="효과 1" className="h-16 object-contain" />
                            <span className="text-xs font-medium">효과 1</span>
                        </div>
                        <div
                            className={`flex flex-col items-center gap-2 p-3 border rounded-lg cursor-pointer transition-all ${ticketEffectId === 2 ? 'border-primary bg-primary/5 ring-2 ring-primary/20' : 'hover:border-primary/50'}`}
                            onClick={() => onChangeTicketEffectId(2)}
                        >
                            <img src="/ticket_effect/logo2.png" alt="효과 2" className="h-16 object-contain" />
                            <span className="text-xs font-medium">효과 2</span>
                        </div>
                    </div>
                </div>
            </CardContent>

            {/* 풀스크린 이미지 확대 모달 */}
            {isImageModalOpen && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in duration-200"
                    onClick={() => setIsImageModalOpen(false)}
                >
                    <div className="relative max-w-5xl max-h-[90vh] w-full flex items-center justify-center" onClick={e => e.stopPropagation()}>
                        <Button
                            variant="ghost"
                            size="icon"
                            className="absolute -top-12 right-0 text-white hover:bg-white/20 hover:text-white rounded-full size-10"
                            onClick={() => setIsImageModalOpen(false)}
                        >
                            <X className="size-6" />
                        </Button>
                        <img
                            src={mapImageSrc}
                            alt="구역 안내도 크게 보기"
                            className="max-w-full max-h-[85vh] object-contain rounded-lg shadow-2xl ring-1 ring-white/10"
                            onError={(e) => {
                                (e.target as HTMLImageElement).src = '/sectionId.png';
                            }}
                        />
                    </div>
                </div>
            )}
        </Card>
    )
}
