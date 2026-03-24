"use client"

import { useMemo, useState } from "react"
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Plus, Search, Trash2 } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { ApiError } from "@/lib/api"
import { searchStakeholder } from "@/lib/stakeholder-api"
import type { RefundItem, Stakeholder } from "./showFormTypes"
import {
  FIXED_PLATFORM_STAKEHOLDER,
  PLATFORM_FEE_BPS,
  PLATFORM_TOTAL_BPS,
} from "./showFormUtils"

const CHART_COLORS = ["#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6", "#ec4899"]

function formatPhoneNumber(value: string): string {
  const digits = value.replace(/\D/g, "").slice(0, 11)
  if (digits.length <= 3) return digits
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
}

function formatBusinessNo(value: string): string {
  const digits = value.replace(/\D/g, "").slice(0, 10)
  if (digits.length <= 3) return digits
  if (digits.length <= 5) return `${digits.slice(0, 3)}-${digits.slice(3)}`
  return `${digits.slice(0, 3)}-${digits.slice(3, 5)}-${digits.slice(5)}`
}

interface SettingsCardPoliciesProps {
  isEdit: boolean
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
  showErrors?: boolean
}

export function SettingsCardPolicies({
  isEdit,
  stakeholders,
  refundPolicy,
  onAddStakeholder,
  onRemoveStakeholder,
  onUpdateStakeholder,
  onAddRefund,
  onRemoveRefund,
  onUpdateRefund,
  showErrors = false,
}: SettingsCardPoliciesProps) {
  const { toast } = useToast()
  const [searchingIndexes, setSearchingIndexes] = useState<Record<number, boolean>>({})

  const fixedStakeholder = stakeholders.find((stakeholder) => stakeholder.isFixed)
  const editableStakeholders = stakeholders
    .map((stakeholder, index) => ({ stakeholder, index }))
    .filter(({ stakeholder }) => !stakeholder.isFixed)
  // UI 전용 상태 모델: { id, type, identifier, name, percentage }
  const stakeholderRows = useMemo(
    () =>
      editableStakeholders.map(({ stakeholder, index }) => ({
        id: `stakeholder-${index}`,
        sourceIndex: index,
        type: stakeholder.role === "ORGANIZER" ? "사업자" : "개인",
        identifier: stakeholder.role === "ORGANIZER" ? stakeholder.businessNo ?? "" : stakeholder.phone ?? "",
        name: stakeholder.name ?? "",
        percentage: stakeholder.shareBps ? String(Number(stakeholder.shareBps) / 100) : "",
        stakeholder,
      })),
    [editableStakeholders]
  )
  // UI 전용 상태 모델: { id, daysBefore, refundRate }
  const refundPolicies = useMemo(
    () =>
      refundPolicy
        .map((policy, index) => ({
          id: `refund-${index}`,
          sourceIndex: index,
          daysBefore: policy.daysRemaining,
          refundRate: policy.refundRate,
        }))
        .sort((a, b) => Number(b.daysBefore || 0) - Number(a.daysBefore || 0))
        .map((policy, index, policies) => {
          const currentDays = Number(policy.daysBefore)
          const previousDays = Number(policies[index - 1]?.daysBefore)

          let rangeLabel = "구간을 입력하세요"

          if (Number.isFinite(currentDays)) {
            if (index === 0) {
              rangeLabel = currentDays === 0 ? "관람 당일" : `관람 ${currentDays}일 전 이상`
            } else if (Number.isFinite(previousDays)) {
              const endDays = previousDays - 1
              rangeLabel =
                endDays === currentDays
                  ? currentDays === 0
                    ? "관람 당일"
                    : `관람 ${currentDays}일 전`
                  : endDays <= 0
                    ? currentDays === 0
                      ? "관람 당일"
                      : `관람 ${currentDays}일 전 ~ 당일`
                    : `관람 ${currentDays}~${endDays}일 전`
            } else {
              rangeLabel = currentDays === 0 ? "관람 당일" : `관람 ${currentDays}일 전 ~ 당일`
            }
          }

          return {
            ...policy,
            rangeLabel,
          }
        }),
    [refundPolicy]
  )

  const totalShareBps = stakeholders.reduce(
    (sum, stakeholder) => sum + (Number(stakeholder.shareBps) || 0),
    0
  )
  const editableShareBps = editableStakeholders.reduce(
    (sum, { stakeholder }) => sum + (Number(stakeholder.shareBps) || 0),
    0
  )
  const remainingShareBps = PLATFORM_TOTAL_BPS - totalShareBps
  const isStakeholderShareValid = totalShareBps === PLATFORM_TOTAL_BPS

  const chartData = useMemo(() => {
    const base = stakeholders.map((stakeholder, index) => ({
      name: stakeholder.isFixed ? "플랫폼" : stakeholder.name.trim() || `이해관계자 ${index + 1}`,
      value: Number(stakeholder.shareBps) || 0,
      color: stakeholder.isFixed ? "#64748b" : CHART_COLORS[index % CHART_COLORS.length],
    }))

    if (remainingShareBps > 0) {
      base.push({
        name: "미할당",
        value: remainingShareBps,
        color: "#e2e8f0",
      })
    }

    return base
  }, [stakeholders, remainingShareBps])

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
            ? "사업자등록번호를 입력해주세요."
            : "연락처를 입력해주세요.",
        variant: "destructive",
      })
      return
    }

    setSearchingIndexes((prev) => ({ ...prev, [idx]: true }))

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
        title: "조회 완료",
        description: `${result.name} 정보를 확인했습니다.`,
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
      setSearchingIndexes((prev) => ({ ...prev, [idx]: false }))
    }
  }

  return (
    <Card>
      <CardHeader className="py-4">
        <CardTitle className="text-lg">정산 및 정책</CardTitle>
      </CardHeader>

      <CardContent className="space-y-5 pb-4">
        <section className="space-y-2">
          <Label className={`text-sm ${!isStakeholderShareValid && showErrors ? "text-destructive" : ""}`}>
            수익 분배(BPS) <span className="text-destructive">*</span>
          </Label>

          <div className="rounded-lg border bg-muted/10 p-4">
            <div className="grid items-start gap-3 lg:grid-cols-[360px_minmax(0,1fr)]">
              <div className="h-[270px] rounded-md border bg-background p-2">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={chartData}
                      dataKey="value"
                      cx="50%"
                      cy="50%"
                      innerRadius={72}
                      outerRadius={114}
                      paddingAngle={2}
                      stroke="none"
                    >
                      {chartData.map((entry, index) => (
                        <Cell key={`distribution-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip
                      formatter={(value, _name, props) => {
                        const numericValue = Number(value ?? 0)

                        return [
                          `${(numericValue / 100).toLocaleString()}% (${numericValue.toLocaleString()} bps)`,
                          props?.payload?.name ?? "구성 비율",
                        ]
                      }}
                      contentStyle={{
                        borderRadius: "8px",
                        fontSize: "12px",
                        padding: "8px",
                        border: "1px solid #e2e8f0",
                      }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>

              <div className="flex h-full flex-col justify-center gap-2">
                <div className="grid w-full grid-cols-2 gap-x-4 gap-y-1.5">
                  <div className="rounded-sm bg-background/80 px-1.5 py-1">
                    <p className="text-[11px] leading-tight text-muted-foreground">총합</p>
                    <p className="text-2xl font-semibold leading-none">
                      {PLATFORM_TOTAL_BPS.toLocaleString()} <span className="text-xs font-medium text-muted-foreground">bps</span>
                    </p>
                  </div>
                  <div className="rounded-sm bg-background/80 px-1.5 py-1">
                    <p className="text-[11px] leading-tight text-muted-foreground">플랫폼 수수료</p>
                    <p className="text-2xl font-semibold leading-none">
                      {PLATFORM_FEE_BPS.toLocaleString()} <span className="text-xs font-medium text-muted-foreground">bps</span>
                    </p>
                  </div>
                  <div className="rounded-sm bg-background/80 px-1.5 py-1">
                    <p className="text-[11px] leading-tight text-muted-foreground">이해관계자 입력 합계</p>
                    <p className="text-2xl font-semibold leading-none">
                      {editableShareBps.toLocaleString()} <span className="text-xs font-medium text-muted-foreground">bps</span>
                    </p>
                  </div>
                  <div className="rounded-sm bg-background/80 px-1.5 py-1">
                    <p className="text-[11px] leading-tight text-muted-foreground">남은 비율</p>
                    <p
                      className={`text-2xl font-semibold leading-none ${
                        remainingShareBps === 0 ? "text-emerald-600" : "text-amber-600"
                      }`}
                    >
                      {remainingShareBps >= 0
                        ? `${(remainingShareBps / 100).toLocaleString()}%`
                        : `-${(Math.abs(remainingShareBps) / 100).toLocaleString()}%`}
                    </p>
                  </div>
                </div>

                <div className="rounded-md border bg-background px-3 py-1.5 text-[11px] text-muted-foreground">
                  총 정산 비율은 {(PLATFORM_TOTAL_BPS / 100).toLocaleString()}%이며 플랫폼 수수료{" "}
                  {(PLATFORM_FEE_BPS / 100).toLocaleString()}%는 자동 고정됩니다.
                </div>

                {isEdit && (
                  <div className="rounded-md border border-slate-300 bg-slate-50 px-3 py-1.5 text-[11px] text-slate-600">
                    정산 비율과 이해관계자 정보는 등록 후 수정할 수 없습니다.
                  </div>
                )}

                {!isStakeholderShareValid && (
                  <div className="rounded-md border border-amber-300 bg-amber-50 px-3 py-1.5 text-xs text-amber-700">
                    현재 합계 {totalShareBps.toLocaleString()} / 10,000 bps ({Math.max(0, remainingShareBps).toLocaleString()} bps 추가 필요)
                  </div>
                )}
              </div>
            </div>
          </div>
        </section>

        <section className="space-y-2">
          <div className="flex items-center justify-between">
            <Label className="text-sm">이해관계자 설정</Label>
            {!isEdit && (
              <Button variant="outline" size="sm" className="h-7 px-2 text-xs" onClick={onAddStakeholder}>
                <Plus className="mr-1 size-3.5" />
                추가
              </Button>
            )}
          </div>

          <div className="rounded-lg border bg-muted/10 p-3">
            <div className="hidden lg:grid lg:grid-cols-[150px_520px_190px_40px] items-center gap-x-4 border-b border-border px-3 pb-2 text-[11px] text-muted-foreground">
              <div>유형</div>
              <div>입력/조회</div>
              <div>비율(%)</div>
              <div className="text-center">삭제</div>
            </div>

            {fixedStakeholder && (
              <div className="grid grid-cols-1 gap-2 px-3 py-2 text-xs lg:grid-cols-[150px_520px_190px_40px] lg:items-center lg:gap-x-4">
                <div>
                  <span className="rounded bg-slate-100 px-2 py-1 text-[11px] font-medium text-slate-700">
                    플랫폼
                  </span>
                </div>
                <div className="text-muted-foreground">
                  {fixedStakeholder.name || FIXED_PLATFORM_STAKEHOLDER.name}
                  {fixedStakeholder.businessNo ? ` (${fixedStakeholder.businessNo})` : ""}
                </div>
                <div className="font-semibold">
                  {(Number(fixedStakeholder.shareBps || PLATFORM_FEE_BPS) / 100).toLocaleString()}
                </div>
                <div className="text-center text-muted-foreground">-</div>
              </div>
            )}

            <div className="divide-y divide-border/70">
            {stakeholderRows.map((row, displayIndex) => {
              const sh = row.stakeholder
              const idx = row.sourceIndex
              return (
              <div
                key={row.id}
                className="grid grid-cols-1 gap-2 px-3 py-3 lg:grid-cols-[150px_520px_190px_40px] lg:items-center lg:gap-x-4"
              >
                <div>
                  <select
                    className="h-8 w-full rounded-md border border-border bg-background px-2 text-xs"
                    value={sh.role}
                    onChange={(event) => {
                      const nextRole = event.target.value as Stakeholder["role"]
                      onUpdateStakeholder(idx, "role", nextRole)
                      onUpdateStakeholder(idx, "verified", false)
                      onUpdateStakeholder(idx, "name", "")
                      onUpdateStakeholder(idx, "phone", "")
                      onUpdateStakeholder(idx, "businessNo", "")
                    }}
                    disabled={isEdit}
                  >
                    <option value="ORGANIZER">사업자</option>
                    <option value="ARTIST">개인</option>
                  </select>
                </div>

                <div className="min-w-0">
                  <div className="grid grid-cols-[minmax(0,1fr)_72px_minmax(0,1fr)] items-center gap-2 min-w-0">
                    {sh.role === "ORGANIZER" ? (
                      <Input
                        placeholder="사업자등록번호(- 제외)"
                        value={row.identifier}
                        onChange={(event) => {
                          onUpdateStakeholder(idx, "businessNo", formatBusinessNo(event.target.value))
                          onUpdateStakeholder(idx, "verified", false)
                          onUpdateStakeholder(idx, "name", "")
                        }}
                        className={`h-8 min-w-0 text-xs ${isEdit ? "bg-muted/50" : ""}`}
                        readOnly={isEdit}
                      />
                    ) : (
                      <Input
                        placeholder="연락처(- 제외)"
                        value={row.identifier}
                        onChange={(event) => {
                          onUpdateStakeholder(idx, "phone", formatPhoneNumber(event.target.value))
                          onUpdateStakeholder(idx, "verified", false)
                          onUpdateStakeholder(idx, "name", "")
                        }}
                        className={`h-8 min-w-0 text-xs ${isEdit ? "bg-muted/50" : ""}`}
                        readOnly={isEdit}
                      />
                    )}
                    <Button
                      variant="outline"
                      size="sm"
                      className="h-8 w-[72px] px-0 text-xs"
                      onClick={() => void handleVerify(idx)}
                      disabled={Boolean(searchingIndexes[idx]) || isEdit}
                    >
                      <Search className="mr-1 size-3.5" />
                      {searchingIndexes[idx] ? "조회중" : "조회"}
                    </Button>
                    <Input
                      placeholder="조회 시 이름 자동 표시"
                      value={row.name}
                      readOnly
                      className="h-8 min-w-0 border-0 bg-muted/60 px-2 text-xs text-muted-foreground shadow-none focus-visible:ring-0"
                    />
                  </div>
                </div>

                <div className="lg:self-center">
                  <div className="flex items-center gap-1.5">
                    <Input
                      type="number"
                      step="0.01"
                      min="0"
                      max="100"
                      placeholder="0"
                      value={row.percentage}
                      onChange={(event) => {
                        const value = event.target.value
                        if (value === "") {
                          onUpdateStakeholder(idx, "shareBps", "")
                          return
                        }

                        const parsed = parseFloat(value)
                        if (!Number.isNaN(parsed)) {
                          onUpdateStakeholder(idx, "shareBps", String(Math.round(parsed * 100)))
                        }
                      }}
                      className={`h-8 w-[78px] text-right text-xs ${isEdit ? "bg-muted/50" : ""}`}
                      readOnly={isEdit}
                    />
                    <span className="text-xs text-muted-foreground">%</span>
                    <span className="text-[11px] text-muted-foreground">
                      ({(Number(row.percentage || 0) * 100).toLocaleString()} bps)
                    </span>
                  </div>
                </div>

                <div className="flex justify-end lg:self-center lg:justify-center">
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7 text-muted-foreground hover:bg-destructive/10 hover:text-destructive"
                    onClick={() => onRemoveStakeholder(idx)}
                    disabled={isEdit}
                    aria-label={`이해관계자 ${displayIndex + 1} 삭제`}
                  >
                    <Trash2 className="size-3.5" />
                  </Button>
                </div>
              </div>
            )})}
            </div>
          </div>
        </section>

        <Separator />

        <section className="space-y-2">
          <div className="flex items-center justify-between">
            <Label className={`text-sm ${refundPolicy.length === 0 && showErrors ? "text-destructive" : ""}`}>
              환불 정책 <span className="text-destructive">*</span>
            </Label>
            <Button variant="outline" size="sm" className="h-7 px-2 text-xs" onClick={onAddRefund}>
              <Plus className="mr-1 size-3.5" />
              추가
            </Button>
          </div>

          <div className="rounded-lg border border-border/80 bg-muted/10 px-4 py-3">
            <p className="text-sm font-medium text-foreground">
              각 행은 취소 수수료가 적용되는 구간의 시작 기준입니다. 아래 표에서 실제 적용 구간이 자동으로 계산됩니다.
            </p>
          </div>

          {refundPolicy.length === 0 && showErrors && (
            <p className="text-[10px] font-medium text-destructive">
              환불 정책을 최소 1개 이상 추가해 주세요.
            </p>
          )}

          <div className="rounded-lg border bg-muted/10 p-3">
            <div className="grid grid-cols-[1.3fr_0.9fr_0.9fr_56px] items-center gap-x-4 border-b border-border px-3 pb-2 text-[11px] text-muted-foreground">
              <div>적용 구간</div>
              <div>시작 기준</div>
              <div>수수료</div>
              <div className="text-center">삭제</div>
            </div>

            <div className="divide-y divide-border/70">
            {refundPolicies.map((policy) => (
              <div
                key={policy.id}
                className="grid grid-cols-[1.3fr_0.9fr_0.9fr_56px] items-center gap-x-4 px-3 py-2 text-xs"
              >
                <div>
                  <span className="inline-flex rounded-full bg-background px-2.5 py-1 text-[11px] font-medium text-foreground">
                    {policy.rangeLabel}
                  </span>
                </div>

                <div className="flex items-center gap-1">
                  <Input
                    type="number"
                    placeholder="14"
                    value={policy.daysBefore}
                    onChange={(event) => onUpdateRefund(policy.sourceIndex, "daysRemaining", event.target.value)}
                    className="h-7 w-[72px] px-2 text-center text-[11px]"
                  />
                  <span className="text-muted-foreground">일 전부터</span>
                </div>

                <div className="flex items-center gap-1">
                  <Input
                    type="number"
                    step="1"
                    min="0"
                    max="100"
                    placeholder="0"
                    value={policy.refundRate}
                    onChange={(event) => onUpdateRefund(policy.sourceIndex, "refundRate", event.target.value)}
                    className="h-7 w-[72px] px-2 text-center text-[11px]"
                  />
                  <span className="text-muted-foreground">%</span>
                </div>

                <div className="flex justify-center">
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-6 w-6 text-muted-foreground hover:bg-destructive/10 hover:text-destructive"
                    onClick={() => onRemoveRefund(policy.sourceIndex)}
                  >
                    <Trash2 className="size-3.5" />
                  </Button>
                </div>
              </div>
            ))}
            </div>
          </div>
        </section>
      </CardContent>
    </Card>
  )
}
