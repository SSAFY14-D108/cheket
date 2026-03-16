"use client"

import { useState } from "react"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Plus, Trash2, Search } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { ApiError } from "@/lib/api"
import { searchStakeholder } from "@/lib/stakeholder-api"
import type { Stakeholder, RefundItem } from "./showFormTypes"

interface SettingsCardPoliciesProps {
    stakeholders: Stakeholder[]
    refundPolicy: RefundItem[]
    onAddStakeholder: () => void
    onRemoveStakeholder: (idx: number) => void
    onUpdateStakeholder: (idx: number, field: keyof Stakeholder, val: string | number | boolean) => void
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
    const { toast } = useToast()
    const [searchingIndexes, setSearchingIndexes] = useState<Record<number, boolean>>({})
    const stakeholderShareSum = stakeholders.reduce(
        (sum, stakeholder) => sum + (Number(stakeholder.shareBps) || 0),
        0
    )
    const remainingShareBps = 10000 - stakeholderShareSum
    const isStakeholderShareValid = stakeholderShareSum === 10000

    const handleVerify = async (idx: number) => {
        const stakeholder = stakeholders[idx]
        const rawNumber = stakeholder.role === "organizer"
            ? stakeholder.businessNo ?? ""
            : stakeholder.phone ?? ""
        const number = rawNumber.trim()

        if (!number) {
            toast({
                title: "조회값 확인",
                description: stakeholder.role === "organizer"
                    ? "사업자번호를 입력해주세요."
                    : "전화번호를 입력해주세요.",
                variant: "destructive",
            })
            return
        }

        setSearchingIndexes((previous) => ({ ...previous, [idx]: true }))

        try {
            const result = await searchStakeholder(
                stakeholder.role === "organizer" ? "HOST" : "USER",
                number
            )

            onUpdateStakeholder(idx, "name", result.name)
            onUpdateStakeholder(idx, "userId", result.id)
            onUpdateStakeholder(idx, "verified", true)
            onUpdateStakeholder(
                idx,
                stakeholder.role === "organizer" ? "businessNo" : "phone",
                result.number
            )

            toast({
                title: "조회 성공",
                description: `${result.name}님이 확인되었습니다.`,
            })
        } catch (error) {
            onUpdateStakeholder(idx, "verified", false)
            onUpdateStakeholder(idx, "name", "")
            onUpdateStakeholder(idx, "userId", 0)

            toast({
                title: "조회 실패",
                description:
                    error instanceof ApiError
                        ? error.message
                        : "이해관계자 조회 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                variant: "destructive",
            })
        } finally {
            setSearchingIndexes((previous) => ({ ...previous, [idx]: false }))
        }
    }

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
                    <div
                        className={`rounded-md border px-3 py-2 text-[11px] ${isStakeholderShareValid
                                ? "border-emerald-300 bg-emerald-50 text-emerald-700"
                                : "border-amber-300 bg-amber-50 text-amber-700"
                            }`}
                    >
                        현재 합계 {stakeholderShareSum.toLocaleString()} / 10,000bps
                        {!isStakeholderShareValid && `, ${remainingShareBps > 0 ? `${remainingShareBps.toLocaleString()}bps 더 입력` : `${Math.abs(remainingShareBps).toLocaleString()}bps 초과`}`}
                    </div>

                    {stakeholders.map((sh, idx) => (
                        <div key={'sh' + idx} className="flex flex-col gap-1 border-b pb-2 mb-1 last:border-0">
                            {/* 1줄: 역할 + 연락처/사업자번호 + 조회 + 삭제 */}
                            <div className="flex gap-1 items-center w-full">
                                <select
                                    className="h-8 rounded-md border border-input bg-background px-2 py-1 text-xs shrink-0 disabled:opacity-70"
                                    value={sh.role}
                                    onChange={e => {
                                        const nextRole = e.target.value as Stakeholder["role"]
                                        onUpdateStakeholder(idx, 'role', nextRole)
                                        onUpdateStakeholder(idx, 'verified', false)
                                        onUpdateStakeholder(idx, 'name', '')
                                        onUpdateStakeholder(idx, 'userId', 0)
                                        onUpdateStakeholder(idx, 'phone', '')
                                        onUpdateStakeholder(idx, 'businessNo', '')
                                    }}
                                >
                                    <option value="organizer">사업자</option>
                                    <option value="artist">개인</option>
                                </select>

                                {sh.role === 'organizer' ? (
                                    <Input
                                        placeholder="사업자번호 (숫자만)"
                                        value={sh.businessNo ?? ""}
                                        onChange={e => {
                                            onUpdateStakeholder(idx, 'businessNo', e.target.value)
                                            onUpdateStakeholder(idx, 'verified', false)
                                            onUpdateStakeholder(idx, 'name', '')
                                            onUpdateStakeholder(idx, 'userId', 0)
                                        }}
                                        className="h-8 text-xs flex-1 min-w-0"
                                    />
                                ) : (
                                    <Input
                                        placeholder="연락처 (예: 010-1234-5678)"
                                        value={sh.phone ?? ""}
                                        onChange={e => {
                                            onUpdateStakeholder(idx, 'phone', e.target.value)
                                            onUpdateStakeholder(idx, 'verified', false)
                                            onUpdateStakeholder(idx, 'name', '')
                                            onUpdateStakeholder(idx, 'userId', 0)
                                        }}
                                        className="h-8 text-xs flex-1 min-w-0"
                                    />
                                )}

                                <Button
                                    variant="outline"
                                    size="sm"
                                    className="h-8 px-2 text-xs shrink-0"
                                    onClick={() => void handleVerify(idx)}
                                    disabled={Boolean(searchingIndexes[idx])}
                                >
                                    <Search className="size-3 mr-1" />
                                    {searchingIndexes[idx] ? '조회 중...' : '조회'}
                                </Button>
                                <Button variant="ghost" size="icon" className="h-8 w-8 text-muted-foreground shrink-0" onClick={() => onRemoveStakeholder(idx)}>
                                    <Trash2 className="size-3" />
                                </Button>
                            </div>

                            {/* 2줄: 이름(자동입력) + BPS 비율 */}
                            <div className="flex items-center gap-1.5 mt-0.5">
                                <Input placeholder="이름/법인명 (조회 시 자동입력)" value={sh.name} onChange={e => onUpdateStakeholder(idx, 'name', e.target.value)} className="h-8 text-xs flex-1 bg-muted/30" readOnly />
                                <span
                                    className={`rounded-full px-2 py-1 text-[10px] font-medium ${sh.verified
                                            ? 'bg-emerald-100 text-emerald-700'
                                            : 'bg-muted text-muted-foreground'
                                        }`}
                                >
                                    {sh.verified ? '✓ 인증됨' : '미인증'}
                                </span>
                                <Input
                                    type="number"
                                    placeholder="비율(BPS)"
                                    value={sh.shareBps}
                                    onChange={e => onUpdateStakeholder(idx, 'shareBps', e.target.value)}
                                    className="h-8 text-xs w-[100px]"
                                />
                                <span className="text-[9px] text-muted-foreground shrink-0 whitespace-nowrap">예: 70%→7000</span>
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
