"use client"

import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Plus, Trash2 } from "lucide-react"
import type { Grade, SessionItem } from "./showFormTypes"

interface SettingsCardTicketsProps {
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
    return (
        <Card>
            <CardHeader className="py-4">
                <CardTitle className="text-lg">티켓 설정</CardTitle>
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
                    {grades.map((grade, idx) => (
                        <div key={'grade' + idx} className="flex gap-2">
                            <Input placeholder="등급(VIP)" value={grade.gradeName} onChange={e => onUpdateGrade(idx, 'gradeName', e.target.value)} className="h-8 text-xs w-24" />
                            <Input type="number" placeholder="가격" value={grade.price} onChange={e => onUpdateGrade(idx, 'price', e.target.value)} className="h-8 text-xs flex-1" />
                            <Input type="color" value={grade.colorCode} onChange={e => onUpdateGrade(idx, 'colorCode', e.target.value)} className="h-8 w-10 p-0.5 border-none bg-transparent" title="색상 지정" />
                            <Button variant="ghost" size="icon" className="h-8 w-8 shrink-0 text-muted-foreground" onClick={() => onRemoveGrade(idx)}>
                                <Trash2 className="size-3" />
                            </Button>
                        </div>
                    ))}
                </div>

                <Separator />

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
                            <div className="flex flex-col gap-2 mt-1">
                                <div className="flex flex-col gap-1.5">
                                    <Label className="text-[10px]">공연 일자</Label>
                                    <Input type="date" value={sess.sessionDate} onChange={e => onUpdateSession(idx, 'sessionDate', e.target.value)} className="h-8 text-xs" />
                                </div>
                                <div className="flex flex-col gap-1.5">
                                    <Label className="text-[10px]">시작 시간</Label>
                                    <Input type="time" value={sess.sessionStartDate} onChange={e => onUpdateSession(idx, 'sessionStartDate', e.target.value)} className="h-8 text-xs" />
                                </div>
                                <div className="flex flex-col gap-1.5 mt-1">
                                    <Label className="text-[10px]">수용 인원</Label>
                                    <Input
                                        type="number"
                                        placeholder="수용인원"
                                        value={sess.capacity}
                                        onChange={e => onUpdateSession(idx, 'capacity', e.target.value)}
                                        className="h-8 px-2"
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
