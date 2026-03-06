"use client"

import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Plus, Trash2, Ticket, X, ZoomIn, ChevronDown } from "lucide-react"
import { useState, useEffect } from "react"
import type { Grade, SessionItem } from "./showFormTypes"
import { DateTimePicker } from "@/components/common/DateTimePicker"
import { mockSectionsByVenue, type Section, mockTicketEffects, type TicketEffect } from "@/lib/mock-data"
import { TicketEffectPreview } from "./TicketEffectPreview"

interface SettingsCardTicketsProps {
    venueId: string
    posterPreview: string | null
    purchaseLimit: string
    grades: Grade[]
    sessionInfo: SessionItem[]
    onChangePurchaseLimit: (val: string) => void
    onAddGrade: () => void
    onRemoveGrade: (idx: number) => void
    onUpdateGrade: (idx: number, field: keyof Grade, val: string) => void
    onAddSession: () => void
    onRemoveSession: (idx: number) => void
    onUpdateSession: (idx: number, field: keyof SessionItem, val: string | number) => void
}

export function SettingsCardTickets({
    venueId,
    posterPreview,
    purchaseLimit,
    grades,
    sessionInfo,
    onChangePurchaseLimit,
    onAddGrade,
    onRemoveGrade,
    onUpdateGrade,
    onAddSession,
    onRemoveSession,
    onUpdateSession,
}: SettingsCardTicketsProps) {
    const [isImageModalOpen, setIsImageModalOpen] = useState(false)

    // 장소 ID에 따른 도면 이미지 매핑
    const getVenueMapImg = (id: string) => {
        switch (id) {
            case '1': return '/venue_map/sectionId1.png'
            case '2': return '/venue_map/sectionid2.jpg'
            case '3': return '/venue_map/sectionid3.png'
            case '4': return '/venue_map/sectionid4.jpg'
            default: return '/venue_map/sectionId1.png' // fallback
        }
    }

    const mapImageSrc = getVenueMapImg(venueId)

    // 선택된 장소의 구역(Section) 정보 API 연동 (상태 관리)
    const [availableSections, setAvailableSections] = useState<Section[]>([])

    // venueId가 바뀔 때마다 백엔드 API를 찔러서 구역 데이터를 가져옵니다.
    useEffect(() => {
        if (!venueId) {
            setAvailableSections([])
            return
        }

        const fetchSections = async () => {
            try {
                // 향후 실제 API 연결 시 아래의 주석 처리된 코드를 사용하면 완벽히 똑같습니다.
                // const response = await fetch(`https://api.cheket.com/api/v1/venues/${venueId}/sections`);
                // const json = await response.json();
                // setAvailableSections(json.data); // "data: []" 배열만 바로 State에 저장

                // --- (현재는 API가 없으므로) 모의 데이터로 대체 ---
                const mockResponseData = mockSectionsByVenue[Number(venueId)] || []
                setAvailableSections(mockResponseData)
            } catch (error) {
                console.error("구역 정보를 불러오는데 실패했습니다.", error)
                setAvailableSections([])
            }
        }

        fetchSections()
    }, [venueId])

    // 티켓 효과 목록 API 연동 (상태 관리)
    const [ticketEffects, setTicketEffects] = useState<TicketEffect[]>([])

    // 컴포넌트 마운트 시 티켓 효과 목록을 가져옵니다.
    useEffect(() => {
        const fetchTicketEffects = async () => {
            try {
                // 향후 실제 API 연결 시 아래 주석 처리된 코드를 사용하면 됩니다.
                // const response = await fetch('https://api.cheket.com/api/v1/ticket-effects');
                // const json = await response.json();
                // setTicketEffects(json.data);

                // --- 모의 데이터 대체 ---
                setTicketEffects(mockTicketEffects)
            } catch (error) {
                console.error("티켓 효과 목록을 불러오는데 실패했습니다.", error)
                setTicketEffects([])
            }
        }
        fetchTicketEffects()
    }, [])

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
                                    (e.target as HTMLImageElement).src = '/venue_map/sectionId1.png';
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
                        <div key={'grade' + idx} className="flex flex-col gap-2 p-3 pl-5 bg-muted/10 border rounded-lg overflow-hidden relative group transition-colors hover:border-primary/40 shadow-sm">
                            {/* 왼쪽 색상 바 (클릭하면 색상 변경) */}
                            <div
                                className="absolute left-0 top-0 bottom-0 w-2 cursor-pointer hover:w-3 transition-all duration-200"
                                style={{ backgroundColor: grade.colorCode || '#ccc' }}
                                title="클릭하여 색상 변경"
                            >
                                <Input
                                    type="color"
                                    value={grade.colorCode || '#000000'}
                                    onChange={e => onUpdateGrade(idx, 'colorCode', e.target.value)}
                                    className="absolute inset-0 opacity-0 w-full h-full cursor-pointer"
                                />
                            </div>

                            {/* 1줄: 등급명 + 삭제 */}
                            <div className="flex items-center gap-2">
                                <Input placeholder="등급명 (예: VIP, R석, S석)" value={grade.gradeName} onChange={e => onUpdateGrade(idx, 'gradeName', e.target.value)} className="h-8 text-xs font-semibold flex-1" />
                                <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-7 w-7 shrink-0 text-muted-foreground hover:text-destructive hover:bg-destructive/10 opacity-0 group-hover:opacity-100 transition-opacity"
                                    onClick={() => onRemoveGrade(idx)}
                                >
                                    <Trash2 className="size-3.5" />
                                </Button>
                            </div>

                            {/* 2줄: 가격 + 구역 */}
                            <div className="flex items-center gap-2">
                                <div className="flex-1 relative">
                                    <Input
                                        type="text"
                                        placeholder="가격 (예: 150,000)"
                                        value={grade.price ? Number(grade.price).toLocaleString() : ''}
                                        onChange={e => {
                                            const rawValue = e.target.value.replace(/,/g, '');
                                            if (!isNaN(Number(rawValue))) {
                                                onUpdateGrade(idx, 'price', rawValue)
                                            }
                                        }}
                                        className="h-8 text-xs pl-5 pr-2"
                                    />
                                    <span className="absolute left-2 top-1/2 -translate-y-1/2 text-[10px] text-muted-foreground font-medium">₩</span>
                                </div>
                                <div className="w-[35%] shrink-0">
                                    {(() => {
                                        const rawSelected = grade.sectionId ? grade.sectionId.split(',').map(s => s.trim()).filter(Boolean) : []
                                        const validSelected = rawSelected.filter(id => availableSections.some(sec => sec.sectionId.toString() === id))

                                        return (
                                            <Popover>
                                                <PopoverTrigger asChild>
                                                    <Button
                                                        variant="outline"
                                                        role="combobox"
                                                        className={`w-full h-8 text-xs justify-between px-2 font-normal ${validSelected.length === 0 ? 'text-muted-foreground' : ''}`}
                                                    >
                                                        <span className="truncate">
                                                            {validSelected.length > 0
                                                                ? `${validSelected.length}개 구역 선택됨`
                                                                : "구역 선택 (다중)"}
                                                        </span>
                                                        <ChevronDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                                                    </Button>
                                                </PopoverTrigger>
                                                <PopoverContent className="w-[250px] p-3 text-xs" align="end">
                                                    <div className="font-semibold mb-2">구역 선택</div>
                                                    {availableSections.length > 0 ? (
                                                        <>
                                                            <div className="flex flex-wrap gap-2">
                                                                {availableSections.map(section => {
                                                                    const selectedSections = grade.sectionId ? grade.sectionId.split(',').map(s => s.trim()) : []
                                                                    const isSelected = selectedSections.includes(section.sectionId.toString())

                                                                    const toggleSelection = () => {
                                                                        let newSelected;
                                                                        if (isSelected) {
                                                                            newSelected = selectedSections.filter(s => s !== section.sectionId.toString())
                                                                        } else {
                                                                            newSelected = [...selectedSections, section.sectionId.toString()]
                                                                        }
                                                                        onUpdateGrade(idx, 'sectionId', newSelected.sort((a, b) => Number(a) - Number(b)).join(', '))
                                                                    }

                                                                    return (
                                                                        <div
                                                                            key={section.sectionId}
                                                                            onClick={toggleSelection}
                                                                            className={`flex items-center justify-center px-3 h-8 text-xs font-medium rounded-md cursor-pointer transition-colors border ${isSelected
                                                                                ? 'bg-primary text-primary-foreground border-primary'
                                                                                : 'bg-background hover:bg-muted border-border'
                                                                                }`}
                                                                        >
                                                                            {section.sectionName}
                                                                        </div>
                                                                    )
                                                                })}
                                                            </div>
                                                            {validSelected.length > 0 && (
                                                                <div className="mt-3 pt-2 text-muted-foreground border-t whitespace-normal break-words leading-relaxed">
                                                                    선택됨: {
                                                                        validSelected.map(id => {
                                                                            const sec = availableSections.find(a => a.sectionId.toString() === id);
                                                                            return sec ? sec.sectionName : id;
                                                                        }).join(', ')
                                                                    }
                                                                </div>
                                                            )}
                                                        </>
                                                    ) : (
                                                        <div className="text-muted-foreground text-center py-4">해당 공연장의 구역 정보가 없습니다.</div>
                                                    )}
                                                </PopoverContent>
                                            </Popover>
                                        );
                                    })()}
                                </div>
                            </div>

                            {/* 3줄: 실시간 티켓 효과 및 미리보기 썸네일 */}
                            <TicketEffectPreview
                                posterUrl={posterPreview}
                                selectedEffectId={grade.ticketEffectId}
                                onSelectEffect={(effectId) => onUpdateGrade(idx, 'ticketEffectId', effectId)}
                                ticketEffects={ticketEffects}
                            />
                        </div>
                    ))}
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
                                (e.target as HTMLImageElement).src = '/venue_map/sectionId1.png';
                            }}
                        />
                    </div>
                </div>
            )}

            <Separator className="my-2" />

            <CardContent className="flex flex-col gap-4 pt-0 pb-4">
                <div className="flex flex-col gap-2">
                    <div className="flex items-center justify-between">
                        <Label className="text-sm">회차 정보 (Session)</Label>
                        <Button variant="ghost" size="icon" className="h-6 w-6" onClick={onAddSession}>
                            <Plus className="size-3" />
                        </Button>
                    </div>
                    {sessionInfo.map((sess, idx) => (
                        <div key={'sess' + idx} className="flex flex-col gap-2 bg-muted/40 p-3 rounded border text-xs">
                            <div className="flex justify-between items-center font-medium">
                                <span>Session {idx + 1}</span>
                                <Button variant="ghost" size="icon" className="h-5 w-5" onClick={() => onRemoveSession(idx)}>
                                    <Trash2 className="size-3" />
                                </Button>
                            </div>
                            <div className="flex flex-col gap-2 mt-2">
                                <div className="flex flex-col gap-1.5">
                                    <Label className="text-[10px]">공연 일자 및 시간</Label>
                                    <DateTimePicker
                                        value={sess.sessionDate && sess.sessionStartDate ? `${sess.sessionDate}T${sess.sessionStartDate}` : undefined}
                                        onChange={(val) => {
                                            const [date, time] = val.split("T")
                                            onUpdateSession(idx, 'sessionDate', date)
                                            onUpdateSession(idx, 'sessionStartDate', time)
                                        }}
                                        placeholder="공연 시작 날짜/시간"
                                    />
                                </div>

                            </div>
                        </div>
                    ))}
                </div>
            </CardContent>
        </Card>
    )
}
