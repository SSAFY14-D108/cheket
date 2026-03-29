"use client"

import { useEffect, useMemo, useState } from "react"
import { ChevronDown, Palette, Plus, Ticket, Trash2, X, ZoomIn } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Separator } from "@/components/ui/separator"
import { DateTimePicker } from "@/components/common/DateTimePicker"
import { ApiError } from "@/lib/api"
import {
  fetchShowSections,
  fetchTicketEffects as fetchTicketEffectOptions,
  type HostShowSection,
  type HostShowTicketEffect,
} from "@/lib/show-manage-api"
import { TicketEffectPreview } from "./TicketEffectPreview"
import { VenueSeatMap } from "./VenueSeatMap"
import type { Grade, SessionItem } from "./showFormTypes"

interface SettingsCardTicketsProps {
  venueId: string
  showStartAt: string
  showEndAt: string
  posterPreview: string | null
  purchaseLimit: string
  grades: Grade[]
  sessionInfo: SessionItem[]
  showErrors?: boolean
  showSessionSection?: boolean
  onChangePurchaseLimit: (val: string) => void
  onAddGrade: () => void
  onRemoveGrade: (idx: number) => void
  onUpdateGrade: (idx: number, field: keyof Grade, val: string) => void
  onAddSession: () => void
  onRemoveSession: (idx: number) => void
  onUpdateSession: (idx: number, field: keyof SessionItem, val: string | number) => void
}

function parseSelectedSectionIds(sectionIdValue: string) {
  return sectionIdValue
    .split(",")
    .map((value) => value.trim())
    .filter(Boolean)
}

function isGradeComplete(grade: Grade) {
  return (
    Boolean(grade.gradeName.trim()) &&
    Number.isFinite(Number(grade.price)) &&
    Number(grade.price) > 0 &&
    parseSelectedSectionIds(grade.sectionId).length > 0
  )
}

function sanitizeNonNegativeInteger(value: string) {
  return value.replace(/[^\d]/g, "")
}

function buildSectionColorEntries(grades: Grade[]) {
  const entries: Array<{ sectionId: string; colorCode: string }> = []
  for (const grade of grades) {
    const color = grade.colorCode || "#d4d4d8"
    const ids = grade.sectionId
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean)
    for (const id of ids) {
      entries.push({ sectionId: id, colorCode: color })
    }
  }
  return entries
}

export function SettingsCardTickets({
  venueId,
  showStartAt,
  showEndAt,
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
  showErrors = false,
  showSessionSection = true,
}: SettingsCardTicketsProps) {
  const [isSeatMapModalOpen, setIsSeatMapModalOpen] = useState(false)
  const [sectionsError, setSectionsError] = useState<string | null>(null)
  const [ticketEffectsError, setTicketEffectsError] = useState<string | null>(null)
  const [sectionsRetryKey, setSectionsRetryKey] = useState(0)
  const [ticketEffectsRetryKey, setTicketEffectsRetryKey] = useState(0)
  const [availableSections, setAvailableSections] = useState<HostShowSection[]>([])
  const [ticketEffects, setTicketEffects] = useState<HostShowTicketEffect[]>([])
  const [activeGradeIndex, setActiveGradeIndex] = useState(0)

  const invalidSessionCount =
    showStartAt && showEndAt
      ? sessionInfo.filter((session) => {
          if (!session.sessionDate || !session.sessionStartTime) {
            return false
          }

          const sessionTimestamp = new Date(`${session.sessionDate}T${session.sessionStartTime}`).getTime()
          const showStartTimestamp = new Date(showStartAt).getTime()
          const showEndTimestamp = new Date(showEndAt).getTime()

          return sessionTimestamp < showStartTimestamp || sessionTimestamp > showEndTimestamp
        }).length
      : 0

  const sectionColors = useMemo(() => buildSectionColorEntries(grades), [grades])
  const safeActiveGradeIndex =
    grades.length === 0 ? 0 : Math.min(activeGradeIndex, grades.length - 1)
  const activeGrade = grades[safeActiveGradeIndex] ?? null
  const invalidGradeIndexes = grades.reduce<number[]>((accumulator, grade, index) => {
    if (!isGradeComplete(grade)) {
      accumulator.push(index)
    }
    return accumulator
  }, [])

  useEffect(() => {
    let isCancelled = false

    const fetchSections = async () => {
      try {
        const sections = venueId ? await fetchShowSections(venueId) : []

        if (!isCancelled) {
          setAvailableSections(sections)
          setSectionsError(null)
        }
      } catch (error) {
        if (!isCancelled) {
          console.error("구역 정보를 불러오는데 실패했습니다.", error)
          setAvailableSections([])
          setSectionsError("구역 정보를 불러오지 못했습니다. 다시 시도해주세요.")
        }
      }
    }

    void fetchSections()

    return () => {
      isCancelled = true
    }
  }, [venueId, sectionsRetryKey])

  useEffect(() => {
    let isCancelled = false

    const loadTicketEffects = async () => {
      try {
        const effects = await fetchTicketEffectOptions()

        if (!isCancelled) {
          setTicketEffects(effects)
          setTicketEffectsError(null)
        }
      } catch (error) {
        if (!isCancelled) {
          setTicketEffects([])
        }

        if (!(error instanceof ApiError && error.status === 401) && !isCancelled) {
          console.error("티켓 효과 목록을 불러오는데 실패했습니다.", error)
          setTicketEffectsError("티켓 효과 목록을 불러오지 못했습니다. 다시 시도해주세요.")
        }

        if (error instanceof ApiError && error.status === 401 && !isCancelled) {
          setTicketEffectsError("티켓 효과 목록을 불러오지 못했습니다. 다시 시도해주세요.")
        }
      }
    }

    void loadTicketEffects()

    return () => {
      isCancelled = true
    }
  }, [ticketEffectsRetryKey])

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setIsSeatMapModalOpen(false)
      }
    }

    if (isSeatMapModalOpen) {
      window.addEventListener("keydown", handleKeyDown)
    }

    return () => window.removeEventListener("keydown", handleKeyDown)
  }, [isSeatMapModalOpen])

  const usedSectionIds = useMemo(() => {
    if (!activeGrade) {
      return new Set<string>()
    }

    return new Set(
      grades.flatMap((grade, index) =>
        index === safeActiveGradeIndex ? [] : parseSelectedSectionIds(grade.sectionId)
      )
    )
  }, [activeGrade, grades, safeActiveGradeIndex])

  const activeSelectedSectionIds = activeGrade ? parseSelectedSectionIds(activeGrade.sectionId) : []
  const validSelectedSections = activeSelectedSectionIds.filter((id) =>
    availableSections.some((section) => section.sectionId.toString() === id)
  )

  const handleRemoveActiveGrade = () => {
    if (!activeGrade) return
    onRemoveGrade(safeActiveGradeIndex)
    if (safeActiveGradeIndex > 0) {
      setActiveGradeIndex(safeActiveGradeIndex - 1)
    }
  }

  const handleToggleActiveSection = (sectionId: string) => {
    if (!activeGrade) return

    const isSelected = activeSelectedSectionIds.includes(sectionId)
    const isDisabled = usedSectionIds.has(sectionId)

    if (isDisabled && !isSelected) {
      return
    }

    const nextSelected = isSelected
      ? activeSelectedSectionIds.filter((selected) => selected !== sectionId)
      : [...activeSelectedSectionIds, sectionId]

    onUpdateGrade(
      activeGradeIndex,
      "sectionId",
      nextSelected.sort((left, right) => Number(left) - Number(right)).join(", ")
    )
  }

  return (
    <Card className="mx-auto w-full max-w-[72rem] rounded-[2rem] border-black/8 bg-white shadow-sm">
      <CardHeader className="border-b border-black/8 px-6 pb-3 pt-5">
        <CardTitle className="flex items-center gap-2 text-xl font-semibold tracking-[-0.03em] text-black">
          <Ticket className="size-5 text-black" />
          티켓 설정
        </CardTitle>
      </CardHeader>

      <CardContent className="flex flex-col gap-6 px-5 pb-5 pt-4 sm:px-6 sm:pb-6 sm:pt-4">
        <div className="flex flex-col gap-2">
          <Label className={`text-[15px] font-bold ${!purchaseLimit && showErrors ? "text-destructive" : ""}`}>
            1인당 구매 가능 티켓 수 <span className="text-destructive">*</span>
          </Label>
          <div className="inline-flex w-fit items-center gap-2">
            <Input
              type="number"
              inputMode="numeric"
              min="1"
              value={purchaseLimit}
              onChange={(event) =>
                onChangePurchaseLimit(sanitizeNonNegativeInteger(event.target.value))
              }
              className={`h-11 w-[140px] rounded-2xl text-center text-lg font-semibold ${
                !purchaseLimit && showErrors
                  ? "border-destructive bg-destructive/5 focus-visible:ring-destructive"
                  : "bg-black/[0.02]"
              }`}
            />
            <span className="text-base font-medium text-muted-foreground">장</span>
          </div>
          {!purchaseLimit && showErrors ? (
            <p className="text-[11px] font-medium text-destructive">1인당 구매 가능 티켓 수를 입력해주세요.</p>
          ) : null}
        </div>

        <div className="rounded-[1.5rem] border bg-muted/10 p-4">
          <div className="mb-4 flex items-center justify-between">
            <Label className={`text-[15px] font-bold ${grades.length === 0 && showErrors ? "text-destructive" : ""}`}>
              좌석 등급 및 가격 <span className="text-destructive">*</span>
            </Label>
            <Button
              variant="outline"
              size="sm"
              className="h-10 rounded-full px-4 font-semibold"
              onClick={() => {
                onAddGrade()
                setActiveGradeIndex(grades.length)
              }}
            >
              <Plus className="mr-1 size-4" />
              추가
            </Button>
          </div>

          {grades.length === 0 && showErrors ? (
            <p className="mb-3 text-[11px] font-medium text-destructive">좌석 등급을 최소 1개 이상 추가해주세요.</p>
          ) : null}

          {(sectionsError || ticketEffectsError) && (
            <div className="mb-3 rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-[11px] text-amber-700">
              {sectionsError || ticketEffectsError}
              <Button
                type="button"
                variant="link"
                className="h-auto px-1 py-0 text-[11px] text-amber-700"
                onClick={() => {
                  if (sectionsError) {
                    setSectionsRetryKey((previous) => previous + 1)
                  }
                  if (ticketEffectsError) {
                    setTicketEffectsRetryKey((previous) => previous + 1)
                  }
                }}
              >
                다시 시도
              </Button>
            </div>
          )}

          <div className="grid gap-4 xl:grid-cols-[260px_220px_minmax(0,1fr)]">
            <div className="rounded-[1.25rem] border bg-background p-4">
              <div className="mb-2 flex items-center justify-between">
                <Label className="text-xs text-muted-foreground">좌석 배치도</Label>
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  className="h-7 px-2 text-[11px]"
                  onClick={() => setIsSeatMapModalOpen(true)}
                >
                  <ZoomIn className="mr-1 size-3.5" />
                  크게 보기
                </Button>
              </div>
              <div
                className="cursor-pointer"
                onClick={() => setIsSeatMapModalOpen(true)}
              >
                <VenueSeatMap
                  venueId={venueId}
                  sectionColors={sectionColors}
                />
              </div>
            </div>

            <div className="rounded-[1.25rem] border bg-background p-3">
              <div className="mb-3 flex items-center justify-between">
                <Label className="text-xs text-muted-foreground">등급 목록</Label>
                <span className="text-xs text-muted-foreground">{grades.length}개</span>
              </div>
              <div className="flex flex-col gap-2">
                {grades.length > 0 ? (
                  grades.map((grade, idx) => {
                    const selectedCount = parseSelectedSectionIds(grade.sectionId).length
                    const isActive = idx === safeActiveGradeIndex
                    const isInvalid = invalidGradeIndexes.includes(idx)

                    return (
                      <button
                        key={`grade-tab-${idx}`}
                        type="button"
                        onClick={() => setActiveGradeIndex(idx)}
                        className={`flex w-full items-start justify-between rounded-2xl border px-3 py-3 text-left transition-colors ${
                          isActive
                            ? isInvalid && showErrors
                              ? "border-destructive bg-destructive/5"
                              : "border-black/15 bg-black/[0.04]"
                            : isInvalid && showErrors
                              ? "border-destructive/60 bg-destructive/5 hover:border-destructive"
                              : "border-transparent bg-black/[0.02] hover:border-black/8 hover:bg-black/[0.035]"
                        }`}
                      >
                        <div className="min-w-0">
                          <div className="flex items-center gap-2">
                            <span
                              className="h-2.5 w-2.5 rounded-full"
                              style={{
                                backgroundColor:
                                  isInvalid && showErrors
                                    ? "#ef4444"
                                    : grade.colorCode || "#8b5cf6",
                              }}
                            />
                            <span className="text-xs font-semibold text-foreground">등급 {idx + 1}</span>
                          </div>
                          <p className="mt-2 truncate text-sm font-semibold text-foreground">
                            {grade.gradeName.trim() || "새 좌석 등급"}
                          </p>
                          <p className="mt-1 text-[11px] text-muted-foreground">
                            {grade.price ? `${Number(grade.price).toLocaleString()} SSF` : "가격 미입력"}
                          </p>
                          {isInvalid && showErrors ? (
                            <p className="mt-1 text-[11px] font-medium text-destructive">
                              필수값 미입력
                            </p>
                          ) : null}
                        </div>
                        <span
                          className={`shrink-0 rounded-full px-2 py-1 text-[11px] ${
                            isInvalid && showErrors
                              ? "bg-destructive/10 text-destructive"
                              : "bg-white text-muted-foreground"
                          }`}
                        >
                          {selectedCount}구역
                        </span>
                      </button>
                    )
                  })
                ) : (
                  <div className="rounded-2xl border border-dashed border-black/10 bg-black/[0.02] px-4 py-6 text-center text-sm text-muted-foreground">
                    좌석 등급을 추가해주세요.
                  </div>
                )}
              </div>
            </div>

            <div className="rounded-[1.25rem] border bg-background p-4 shadow-sm">
              {activeGrade ? (
                <>
                  <div className="mb-4 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span
                        className="h-3 w-3 rounded-full"
                        style={{ backgroundColor: activeGrade.colorCode || "#8b5cf6" }}
                      />
                      <span className="text-sm font-semibold text-foreground">
                        {activeGrade.gradeName.trim() || `등급 ${safeActiveGradeIndex + 1}`}
                      </span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <div className="relative">
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8 p-0 text-muted-foreground hover:bg-muted/60"
                          title="등급 색상 선택"
                        >
                          <Palette className="size-4 text-muted-foreground" />
                        </Button>
                        <Input
                          type="color"
                          value={activeGrade.colorCode || "#000000"}
                          onChange={(event) => onUpdateGrade(safeActiveGradeIndex, "colorCode", event.target.value)}
                          className="absolute inset-0 h-full w-full cursor-pointer opacity-0"
                          aria-label={`등급 ${safeActiveGradeIndex + 1} 색상 선택`}
                        />
                      </div>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-muted-foreground hover:bg-destructive/10 hover:text-destructive"
                        onClick={handleRemoveActiveGrade}
                      >
                        <Trash2 className="size-4" />
                      </Button>
                    </div>
                  </div>

                  <div className="space-y-4">
                    <div className="space-y-1.5">
                      <Label className="text-[11px] font-medium">등급명</Label>
                      <Input
                        placeholder="등급명(예: VIP, R석, S석)"
                        value={activeGrade.gradeName}
                        onChange={(event) =>
                          onUpdateGrade(
                            safeActiveGradeIndex,
                            "gradeName",
                            event.target.value.toUpperCase()
                          )
                        }
                        className={`h-11 rounded-2xl text-sm font-semibold ${
                          !activeGrade.gradeName.trim() && showErrors
                            ? "border-destructive bg-destructive/5 focus-visible:ring-destructive"
                            : "bg-black/[0.02]"
                        }`}
                      />
                    </div>

                    <div className="space-y-1.5">
                      <Label className="text-[11px] font-medium">가격(SSF)</Label>
                      <div className="relative">
                        <Input
                          type="text"
                          placeholder="가격(예: 15 SSF)"
                          value={activeGrade.price ? Number(activeGrade.price).toLocaleString() : ""}
                          onChange={(event) => {
                            const rawValue = sanitizeNonNegativeInteger(
                              event.target.value.replace(/,/g, "")
                            )
                            onUpdateGrade(safeActiveGradeIndex, "price", rawValue)
                          }}
                          className={`h-11 rounded-2xl pr-12 text-sm ${
                            (!activeGrade.price || Number(activeGrade.price) <= 0) && showErrors
                              ? "border-destructive bg-destructive/5 focus-visible:ring-destructive"
                              : "bg-black/[0.02]"
                          }`}
                        />
                        <span className="absolute right-3 top-1/2 -translate-y-1/2 text-[12px] font-medium text-muted-foreground">
                          SSF
                        </span>
                      </div>
                    </div>

                    <div className="space-y-1.5">
                      <Label className="text-[11px] font-medium">구역 선택</Label>
                      <Popover>
                        <PopoverTrigger asChild>
                          <Button
                            variant="outline"
                            role="combobox"
                            className={`h-11 w-full justify-between rounded-2xl px-3 text-sm font-normal ${
                              validSelectedSections.length === 0 ? "text-muted-foreground" : ""
                            } ${
                              validSelectedSections.length === 0 && showErrors
                                ? "border-destructive bg-destructive/5"
                                : "bg-black/[0.02]"
                            }`}
                          >
                            <span className="truncate">
                              {validSelectedSections.length > 0
                                ? `${validSelectedSections.length}개 구역 선택됨`
                                : "구역 선택 (다중)"}
                            </span>
                            <ChevronDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                          </Button>
                        </PopoverTrigger>
                        <PopoverContent className="w-[260px] p-3 text-xs" align="start">
                          <div className="mb-2 font-semibold">구역 선택</div>
                          {availableSections.length > 0 ? (
                            <>
                              <div className="flex flex-wrap gap-2">
                                {availableSections.map((section) => {
                                  const sectionIdString = section.sectionId.toString()
                                  const isSelected = activeSelectedSectionIds.includes(sectionIdString)
                                  const isDisabled = usedSectionIds.has(sectionIdString)

                                  return (
                                    <div
                                      key={section.sectionId}
                                      onClick={() => handleToggleActiveSection(sectionIdString)}
                                      className={`flex h-8 items-center justify-center rounded-md border px-3 text-xs font-medium transition-colors ${
                                        isSelected
                                          ? "border-primary bg-primary text-primary-foreground"
                                          : isDisabled
                                            ? "cursor-not-allowed border-border bg-muted text-muted-foreground opacity-50"
                                            : "border-border bg-background hover:bg-muted"
                                      }`}
                                      aria-disabled={isDisabled && !isSelected}
                                    >
                                      {section.sectionName}
                                    </div>
                                  )
                                })}
                              </div>
                              {validSelectedSections.length > 0 ? (
                                <div className="mt-3 whitespace-normal break-words border-t pt-2 leading-relaxed text-muted-foreground">
                                  선택:{" "}
                                  {validSelectedSections
                                    .map((id) => {
                                      const section = availableSections.find(
                                        (item) => item.sectionId.toString() === id
                                      )
                                      return section ? section.sectionName : id
                                    })
                                    .join(", ")}
                                </div>
                              ) : null}
                            </>
                          ) : (
                            <div className="py-4 text-center text-muted-foreground">
                              해당 공연장의 구역 정보가 없습니다.
                            </div>
                          )}
                        </PopoverContent>
                      </Popover>
                    </div>

                    <div className="space-y-2">
                      <Label className="text-[11px] font-medium">티켓 효과</Label>
                      <TicketEffectPreview
                        posterUrl={posterPreview}
                          selectedEffectId={activeGrade.ticketEffectId}
                          onSelectEffect={(effectId) => onUpdateGrade(safeActiveGradeIndex, "ticketEffectId", effectId)}
                        ticketEffects={ticketEffects}
                      />
                    </div>
                  </div>
                </>
              ) : (
                <div className="flex h-full min-h-[320px] items-center justify-center rounded-[1rem] border border-dashed border-black/10 bg-black/[0.02] px-6 text-center text-sm text-muted-foreground">
                  좌석 등급을 추가하면 여기서 바로 편집할 수 있습니다.
                </div>
              )}
            </div>
          </div>
        </div>
      </CardContent>

      {isSeatMapModalOpen ? (
        <div
          className="fixed inset-0 z-50 flex animate-in fade-in items-center justify-center bg-black/80 p-4 backdrop-blur-sm duration-200"
          onClick={() => setIsSeatMapModalOpen(false)}
        >
          <div
            className="relative w-full max-w-5xl rounded-2xl bg-white p-6 shadow-2xl"
            onClick={(event) => event.stopPropagation()}
          >
            <Button
              variant="ghost"
              size="icon"
              className="absolute right-3 top-3 size-8 rounded-full"
              onClick={() => setIsSeatMapModalOpen(false)}
            >
              <X className="size-5" />
            </Button>
            <h3 className="mb-4 text-base font-semibold">좌석 배치도</h3>
            <VenueSeatMap
              venueId={venueId}
              sectionColors={sectionColors}
              className="[&>div:last-child]:!max-h-[70vh]"
            />
          </div>
        </div>
      ) : null}

      {showSessionSection ? (
        <>
          <Separator className="my-2" />

          <CardContent className="flex flex-col gap-4 pb-4 pt-0">
            <div className="w-full rounded-lg border bg-muted/10 p-4">
              <div className="mb-2 flex items-center justify-between">
                <Label className={`text-sm font-semibold ${sessionInfo.length === 0 && showErrors ? "text-destructive" : ""}`}>
                  회차 <span className="text-destructive">*</span>
                </Label>
                <Button variant="outline" size="sm" className="h-7 px-2 text-xs" onClick={onAddSession}>
                  <Plus className="mr-1 size-3.5" />
                  추가
                </Button>
              </div>

              {sessionInfo.length === 0 && showErrors ? (
                <p className="mb-2 text-[10px] font-medium text-destructive">회차 정보를 최소 1개 이상 추가해주세요.</p>
              ) : null}

              {invalidSessionCount > 0 ? (
                <div className="mb-2 rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-[11px] text-amber-700">
                  회차 {invalidSessionCount}개가 전체 공연 일정 범위를 벗어났습니다.
                </div>
              ) : null}

              <div className="space-y-1">
                {sessionInfo.map((session, idx) => (
                  <div
                    key={`sess-${idx}`}
                    className="grid w-full grid-cols-1 gap-1.5 rounded-md border bg-background px-3 py-1.5 sm:inline-grid sm:w-auto sm:grid-cols-[96px_360px_28px] sm:items-center sm:gap-2"
                  >
                    <div className="w-24 shrink-0 text-xs font-semibold text-slate-700">회차 {idx + 1}</div>
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
                        minDate={showStartAt ? new Date(showStartAt) : undefined}
                        maxDate={showEndAt ? new Date(showEndAt) : undefined}
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
          </CardContent>
        </>
      ) : null}
    </Card>
  )
}
