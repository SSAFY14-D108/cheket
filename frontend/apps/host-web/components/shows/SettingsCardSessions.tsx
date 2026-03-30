"use client"

import { Plus, Trash2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { formatDateTimeWithWeekday } from "@/lib/utils"
import { DateTimePicker } from "@/components/common/DateTimePicker"
import type { SessionItem } from "./showFormTypes"

interface SettingsCardSessionsProps {
  sessionInfo: SessionItem[]
  reservationEndAt: string
  showStartAt: string
  showEndAt: string
  sessionMinDate?: Date
  showErrors?: boolean
  onAddSession: () => void
  onRemoveSession: (idx: number) => void
  onUpdateSession: (idx: number, field: keyof SessionItem, val: string | number) => void
}

function formatDateTimeValue(value: string, emptyText: string) {
  if (!value) {
    return emptyText
  }

  return formatDateTimeWithWeekday(value)
}

function AutoCalculatedField({
  value,
  emptyText,
}: {
  value: string
  emptyText: string
}) {
  return (
    <div className="rounded-md bg-muted/20 px-4 py-3">
      <div className="text-[15px] font-medium leading-relaxed text-foreground">
        {formatDateTimeValue(value, emptyText)}
      </div>
    </div>
  )
}

export function SettingsCardSessions({
  sessionInfo,
  reservationEndAt,
  showStartAt,
  showEndAt,
  sessionMinDate,
  showErrors = false,
  onAddSession,
  onRemoveSession,
  onUpdateSession,
}: SettingsCardSessionsProps) {
  return (
    <Card className="rounded-[2rem] border-black/8 bg-white py-0 shadow-sm">
      <div className="border-b border-black/8 px-6 pb-3 pt-5">
        <h2 className="text-xl font-semibold tracking-[-0.03em] text-black">회차 정보</h2>
      </div>

      <CardContent className="flex flex-col gap-6 px-5 pb-5 pt-4 sm:px-6 sm:pb-6 sm:pt-4">
        <div className="w-full rounded-lg border bg-muted/10 p-4">
          <div className="mb-2 flex items-center justify-between">
            <Label
              className={`text-sm font-semibold ${
                sessionInfo.length === 0 && showErrors ? "text-destructive" : ""
              }`}
            >
              회차 <span className="text-destructive">*</span>
            </Label>

            <Button variant="outline" size="sm" className="h-7 px-2 text-xs" onClick={onAddSession}>
              <Plus className="mr-1 size-3.5" />
              추가
            </Button>
          </div>

          {sessionInfo.length === 0 && showErrors ? (
            <p className="mb-2 text-[10px] font-medium text-destructive">
              회차 정보를 1개 이상 추가해주세요.
            </p>
          ) : null}

          <div className="space-y-1">
            {sessionInfo.map((session, idx) => (
              <div
                key={`session-${idx}`}
                className="grid w-full grid-cols-1 gap-1.5 rounded-md border bg-background px-3 py-1.5 sm:inline-grid sm:w-auto sm:grid-cols-[96px_360px_28px] sm:items-center sm:gap-2"
              >
                <div className="w-24 shrink-0 text-xs font-semibold text-slate-700">
                  회차 {idx + 1}
                </div>

                <div
                  className={`w-full sm:w-[360px] ${
                    (!session.sessionDate || !session.sessionStartTime) && showErrors
                      ? "rounded-md border border-destructive bg-destructive/5 p-1"
                      : ""
                  }`}
                >
                  <DateTimePicker
                    value={
                      session.sessionDate && session.sessionStartTime
                        ? `${session.sessionDate}T${session.sessionStartTime}`
                        : undefined
                    }
                    onChange={(value) => {
                      const [date, time] = value.split("T")
                      onUpdateSession(idx, "sessionDate", date)
                      onUpdateSession(idx, "sessionStartTime", time)
                    }}
                    placeholder="공연 시작 날짜/시간"
                    minDate={
                      reservationEndAt
                        ? sessionMinDate
                          ? new Date(
                              Math.max(
                                new Date(reservationEndAt).getTime(),
                                sessionMinDate.getTime()
                              )
                            )
                          : new Date(reservationEndAt)
                        : sessionMinDate
                    }
                  />
                </div>

                <div className="flex items-center justify-end sm:justify-center">
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7 text-muted-foreground hover:bg-destructive/10 hover:text-destructive"
                    onClick={() => onRemoveSession(idx)}
                  >
                    <Trash2 className="size-3.5" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="flex flex-col gap-2">
          <Label
            className={`text-[14px] font-bold ${
              (!showStartAt || !showEndAt) && showErrors ? "text-destructive" : ""
            }`}
          >
            공연 진행 전체 기간 <span className="text-destructive">*</span>
          </Label>

          <AutoCalculatedField
            value={
              showStartAt && showEndAt
                ? `${formatDateTimeValue(showStartAt, "")} - ${formatDateTimeValue(showEndAt, "")}`
                : ""
            }
            emptyText="회차를 입력하면 자동으로 계산됩니다."
          />

          <p className="text-[11px] text-muted-foreground">
            가장 이른 회차 시작과 가장 늦은 회차 종료 기준으로 자동 계산됩니다.
          </p>

          {(!showStartAt || !showEndAt) && showErrors ? (
            <p className="text-[11px] font-medium text-destructive">
              회차 정보를 입력해주세요.
            </p>
          ) : null}
        </div>
      </CardContent>
    </Card>
  )
}
