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
  onUpdateStakeholder: (
    idx: number,
    field: keyof Stakeholder,
    val: string | number | boolean
  ) => void
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
  const distributableStakeholders = stakeholders.filter((stakeholder) => !stakeholder.isFixed)
  const stakeholderShareSum = distributableStakeholders.reduce(
    (sum, stakeholder) => sum + (Number(stakeholder.shareBps) || 0),
    0
  )
  const remainingShareBps = 9200 - stakeholderShareSum
  const isStakeholderShareValid = stakeholderShareSum === 9200

  const handleVerify = async (idx: number) => {
    const stakeholder = stakeholders[idx]
    const rawNumber =
      stakeholder.role === "ORGANIZER" ? stakeholder.businessNo ?? "" : stakeholder.phone ?? ""
    const number = rawNumber.trim()

    if (!number) {
      toast({
        title: "조회값 확인",
        description:
          stakeholder.role === "ORGANIZER"
            ? "사업자번호를 입력해주세요."
            : "전화번호를 입력해주세요.",
        variant: "destructive",
      })
      return
    }

    setSearchingIndexes((previous) => ({ ...previous, [idx]: true }))

    try {
      const result = await searchStakeholder(
        stakeholder.role === "ORGANIZER" ? "HOST" : "USER",
        number
      )

      onUpdateStakeholder(idx, "name", result.name)
      onUpdateStakeholder(idx, "verified", true)
      onUpdateStakeholder(
        idx,
        stakeholder.role === "ORGANIZER" ? "businessNo" : "phone",
        result.number
      )

      toast({
        title: "조회 성공",
        description: `${result.name}님이 확인되었습니다.`,
      })
    } catch (error) {
      onUpdateStakeholder(idx, "verified", false)
      onUpdateStakeholder(idx, "name", "")

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
          <div className="text-[10px] leading-tight text-muted-foreground">
            플랫폼 800bps 선공제 후 나머지 금액 분배. 입력 대상 합계는 9200bps입니다.
          </div>
          <div
            className={`rounded-md border px-3 py-2 text-[11px] ${
              isStakeholderShareValid
                ? "border-emerald-300 bg-emerald-50 text-emerald-700"
                : "border-amber-300 bg-amber-50 text-amber-700"
            }`}
          >
            현재 합계 {stakeholderShareSum.toLocaleString()} / 9,200bps
            {!isStakeholderShareValid &&
              `, ${
                remainingShareBps > 0
                  ? `${remainingShareBps.toLocaleString()}bps 더 입력`
                  : `${Math.abs(remainingShareBps).toLocaleString()}bps 초과`
              }`}
          </div>

          {stakeholders.map((sh, idx) => (
            <div key={`sh${idx}`} className="mb-1 flex flex-col gap-1 border-b pb-2 last:border-0">
              {sh.isFixed && (
                <div className="text-[10px] font-medium text-muted-foreground">
                  플랫폼 수수료 (변경 불가)
                </div>
              )}

              <div className="flex w-full items-center gap-1">
                <select
                  className="h-8 shrink-0 rounded-md border border-input bg-background px-2 py-1 text-xs disabled:opacity-70"
                  value={sh.role}
                  onChange={(event) => {
                    const nextRole = event.target.value as Stakeholder["role"]
                    onUpdateStakeholder(idx, "role", nextRole)
                    onUpdateStakeholder(idx, "verified", false)
                    onUpdateStakeholder(idx, "name", "")
                    onUpdateStakeholder(idx, "phone", "")
                    onUpdateStakeholder(idx, "businessNo", "")
                  }}
                  disabled={sh.isFixed}
                >
                  <option value="ORGANIZER">사업자</option>
                  <option value="ARTIST">개인</option>
                </select>

                {sh.role === "ORGANIZER" ? (
                  <Input
                    placeholder="사업자번호 (숫자만)"
                    value={sh.businessNo ?? ""}
                    onChange={(event) => {
                      onUpdateStakeholder(idx, "businessNo", event.target.value)
                      onUpdateStakeholder(idx, "verified", false)
                      onUpdateStakeholder(idx, "name", "")
                    }}
                    className={`h-8 min-w-0 flex-1 text-xs ${sh.isFixed ? "bg-muted/50" : ""}`}
                    readOnly={sh.isFixed}
                  />
                ) : (
                  <Input
                    placeholder="연락처 (예: 010-1234-5678)"
                    value={sh.phone ?? ""}
                    onChange={(event) => {
                      onUpdateStakeholder(idx, "phone", event.target.value)
                      onUpdateStakeholder(idx, "verified", false)
                      onUpdateStakeholder(idx, "name", "")
                    }}
                    className={`h-8 min-w-0 flex-1 text-xs ${sh.isFixed ? "bg-muted/50" : ""}`}
                    readOnly={sh.isFixed}
                  />
                )}

                {!sh.isFixed && (
                  <Button
                    variant="outline"
                    size="sm"
                    className="h-8 shrink-0 px-2 text-xs"
                    onClick={() => void handleVerify(idx)}
                    disabled={Boolean(searchingIndexes[idx])}
                  >
                    <Search className="mr-1 size-3" />
                    {searchingIndexes[idx] ? "조회 중..." : "조회"}
                  </Button>
                )}

                {!sh.isFixed && (
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-8 w-8 shrink-0 text-muted-foreground"
                    onClick={() => onRemoveStakeholder(idx)}
                  >
                    <Trash2 className="size-3" />
                  </Button>
                )}
              </div>

              <div className="mt-0.5 flex items-center gap-1.5">
                <Input
                  placeholder="이름/법인명 (조회 시 자동입력)"
                  value={sh.name}
                  onChange={(event) => onUpdateStakeholder(idx, "name", event.target.value)}
                  className="h-8 flex-1 bg-muted/30 text-xs"
                  readOnly
                />
                <span
                  className={`rounded-full px-2 py-1 text-[10px] font-medium ${
                    sh.verified
                      ? "bg-emerald-100 text-emerald-700"
                      : "bg-muted text-muted-foreground"
                  }`}
                >
                  {sh.verified ? "인증됨" : "미인증"}
                </span>
                <Input
                  type="number"
                  placeholder="비율(BPS)"
                  value={sh.shareBps}
                  onChange={(event) => onUpdateStakeholder(idx, "shareBps", event.target.value)}
                  className={`h-8 w-[100px] text-xs ${sh.isFixed ? "bg-muted/50" : ""}`}
                  readOnly={sh.isFixed}
                />
                <span className="shrink-0 whitespace-nowrap text-[9px] text-muted-foreground">
                  예: 70%→7000
                </span>
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
            <div
              key={`ref${idx}`}
              className="flex flex-col gap-1 rounded border bg-muted/20 p-2 text-xs"
            >
              <div className="flex justify-between">
                <span>정책 #{idx + 1}</span>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-4 w-4 text-muted-foreground"
                  onClick={() => onRemoveRefund(idx)}
                >
                  <Trash2 className="size-2.5" />
                </Button>
              </div>
              <div className="flex items-center gap-1.5 text-xs">
                <span className="whitespace-nowrap text-muted-foreground">공연</span>
                <Input
                  type="number"
                  placeholder="예: 7"
                  value={ref.daysRemaining}
                  onChange={(event) => onUpdateRefund(idx, "daysRemaining", event.target.value)}
                  className="h-7 w-20 px-2 text-center text-[11px]"
                />
                <span className="whitespace-nowrap text-muted-foreground">일 전, </span>
                <Input
                  type="number"
                  step="1"
                  min="0"
                  max="100"
                  placeholder="예: 70"
                  value={ref.refundRate}
                  onChange={(event) => onUpdateRefund(idx, "refundRate", event.target.value)}
                  className="h-7 w-20 px-2 text-center text-[11px]"
                />
                <span className="whitespace-nowrap text-muted-foreground">% 환불</span>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
