"use client"

import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Plus, Trash2 } from "lucide-react"
import type { Stakeholder, RefundItem } from "./showFormTypes"

interface SettingsCardPoliciesProps {
    stakeholders: Stakeholder[]
    refundPolicy: RefundItem[]
    onAddStakeholder: () => void
    onRemoveStakeholder: (idx: number) => void
    onUpdateStakeholder: (idx: number, field: keyof Stakeholder, val: string) => void
    onAddRefund: () => void
    onRemoveRefund: (idx: number) => void
    onUpdateRefund: (idx: number, field: keyof RefundItem, val: string) => void
}

export function SettingsCardPolicies({
    stakeholders,
    refundPolicy,
    onAddStakeholder,
    onRemoveStakeholder,
    onUpdateStakeholder,
    onAddRefund,
    onRemoveRefund,
    onUpdateRefund,
}: SettingsCardPoliciesProps) {
    return (
        <Card>
            <CardHeader className="py-4">
                <CardTitle className="text-lg">정산 및 정책</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-5 pb-4">
                <div className="flex flex-col gap-2">
                    <div className="flex items-center justify-between">
                        <Label className="text-sm">수익 분배 (BPS)</Label>
                        <Button variant="ghost" size="icon" className="h-6 w-6" onClick={onAddStakeholder}>
                            <Plus className="size-3" />
                        </Button>
                    </div>
                    <div className="text-[10px] text-muted-foreground leading-tight">
                        플랫폼 8% 선공제 후 나머지 금액 분배. 총합 10000bps=100%
                    </div>

                    {stakeholders.map((sh, idx) => (
                        <div key={'sh' + idx} className="flex flex-col gap-1 border-b pb-2 mb-1 last:border-0">
                            <div className="flex gap-1">
                                <select
                                    className="h-8 rounded-md border border-input bg-background px-2 py-1 text-xs"
                                    value={sh.role}
                                    onChange={e => onUpdateStakeholder(idx, 'role', e.target.value)}
                                >
                                    <option value="organizer">주최사</option>
                                    <option value="artist">아티스트</option>
                                </select>
                                <Input placeholder="이름/법인명" value={sh.name} onChange={e => onUpdateStakeholder(idx, 'name', e.target.value)} className="h-8 text-xs flex-1" />
                                <Button variant="ghost" size="icon" className="h-8 w-8 text-muted-foreground shrink-0" onClick={() => onRemoveStakeholder(idx)}>
                                    <Trash2 className="size-3" />
                                </Button>
                            </div>
                            <div className="flex gap-1 items-start">
                                {sh.role === 'organizer' ? (
                                    <div className="flex flex-1 gap-1">
                                        <Input placeholder="사업자번호" value={sh.businessNo ?? ""} onChange={e => onUpdateStakeholder(idx, 'businessNo', e.target.value)} className="h-8 text-xs flex-1" />
                                        <Button variant="outline" size="sm" className="h-8 px-2 text-xs" onClick={() => alert("사업자번호 조회 연동 예정")}>
                                            조회
                                        </Button>
                                    </div>
                                ) : (
                                    <div className="flex flex-1 gap-1">
                                        <Input placeholder="연락처" value={sh.phone ?? ""} onChange={e => onUpdateStakeholder(idx, 'phone', e.target.value)} className="h-8 text-xs flex-1" />
                                        <Button variant="outline" size="sm" className="h-8 px-2 text-xs" onClick={() => alert("연락처 조회 연동 예정")}>
                                            조회
                                        </Button>
                                    </div>
                                )}
                                <div className="flex flex-col gap-0.5 w-[84px] shrink-0">
                                    <Input type="number" placeholder="비율(BPS)" value={sh.shareBps} onChange={e => onUpdateStakeholder(idx, 'shareBps', e.target.value)} className="h-8 text-xs w-full" />
                                    <span className="text-[9px] text-muted-foreground text-center">예: 70% ➔ 7000</span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                <Separator />

                <div className="flex flex-col gap-2">
                    <div className="flex items-center justify-between">
                        <Label className="text-sm">환불 정책</Label>
                        <Button variant="ghost" size="icon" className="h-6 w-6" onClick={onAddRefund}>
                            <Plus className="size-3" />
                        </Button>
                    </div>
                    {refundPolicy.map((ref, idx) => (
                        <div key={'ref' + idx} className="flex flex-col gap-1 text-xs border bg-muted/20 rounded p-2">
                            <div className="flex justify-between">
                                <span>정책 #{idx + 1}</span>
                                <Button variant="ghost" size="icon" className="h-4 w-4 text-muted-foreground" onClick={() => onRemoveRefund(idx)}>
                                    <Trash2 className="size-2.5" />
                                </Button>
                            </div>
                            <div className="flex items-center gap-1.5 text-xs">
                                <span className="text-muted-foreground whitespace-nowrap">공연</span>
                                <Input type="number" placeholder="예: 7" value={ref.daysRemaining} onChange={e => onUpdateRefund(idx, 'daysRemaining', e.target.value)} className="h-7 px-2 text-[11px] w-20 text-center" />
                                <span className="text-muted-foreground whitespace-nowrap">일 전, </span>
                                <Input type="number" step="1" min="0" max="100" placeholder="예: 70" value={ref.refundRate} onChange={e => onUpdateRefund(idx, 'refundRate', e.target.value)} className="h-7 px-2 text-[11px] w-20 text-center" />
                                <span className="text-muted-foreground whitespace-nowrap">% 환불</span>
                            </div>
                        </div>
                    ))}
                </div>
            </CardContent>
        </Card>
    )
}