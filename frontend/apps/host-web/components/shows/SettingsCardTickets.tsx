"use client"

import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Plus, Trash2, Ticket, X, ZoomIn, ChevronDown, ChevronUp, Palette } from "lucide-react"
import { useState, useEffect } from "react"
import type { Grade, SessionItem } from "./showFormTypes"
import { DateTimePicker } from "@/components/common/DateTimePicker"
import { TicketEffectPreview } from "./TicketEffectPreview"
import { ApiError } from "@/lib/api"
import {
  fetchShowSections,
  fetchTicketEffects as fetchTicketEffectOptions,
  type HostShowSection,
  type HostShowTicketEffect,
} from "@/lib/show-manage-api"

interface SettingsCardTicketsProps {
  venueId: string
  showStartAt: string
  showEndAt: string
  posterPreview: string | null
  purchaseLimit: string
  grades: Grade[]
  sessionInfo: SessionItem[]
  showErrors?: boolean
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
}: SettingsCardTicketsProps) {
  const [isImageModalOpen, setIsImageModalOpen] = useState(false)
  const [sectionsError, setSectionsError] = useState<string | null>(null)
  const [ticketEffectsError, setTicketEffectsError] = useState<string | null>(null)
  const [sectionsRetryKey, setSectionsRetryKey] = useState(0)
  const [ticketEffectsRetryKey, setTicketEffectsRetryKey] = useState(0)
  const [expandedGradeIndex, setExpandedGradeIndex] = useState(0)

  const parseSelectedSectionIds = (sectionIdValue: string) =>
    sectionIdValue
      .split(",")
      .map((value) => value.trim())
      .filter(Boolean)

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

  const getVenueMapImg = (id: string) => {
    switch (id) {
      case "1":
        return "/venue_map/sectionId1.png"
      case "2":
        return "/venue_map/sectionid2.jpg"
      case "3":
        return "/venue_map/sectionid3.png"
      case "4":
        return "/venue_map/sectionid4.jpg"
      default:
        return "/venue_map/sectionId1.png"
    }
  }

  const mapImageSrc = getVenueMapImg(venueId)

  const [availableSections, setAvailableSections] = useState<HostShowSection[]>([])

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

  const [ticketEffects, setTicketEffects] = useState<HostShowTicketEffect[]>([])

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
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        setIsImageModalOpen(false)
      }
    }

    if (isImageModalOpen) {
      window.addEventListener("keydown", handleKeyDown)
    }

    return () => window.removeEventListener("keydown", handleKeyDown)
  }, [isImageModalOpen])

  return (
    <Card className="mx-auto w-full max-w-[980px]">
      <CardHeader className="py-4">
        <CardTitle className="flex items-center gap-2 text-lg">
          <Ticket className="size-5 text-primary" />
          티켓 설정
        </CardTitle>
      </CardHeader>

      <CardContent className="flex flex-col gap-6 pb-5">
        <div className="flex flex-col gap-1.5">
          <Label className={`text-xs ${!purchaseLimit && showErrors ? "text-destructive" : ""}`}>
            1인당 구매 가능 티켓 수 <span className="text-destructive">*</span>
          </Label>
          <div className="inline-flex w-fit items-center gap-2">
            <Input
              type="number"
              inputMode="numeric"
              placeholder="2"
              value={purchaseLimit}
              onChange={(e) => onChangePurchaseLimit(e.target.value)}
              className={`h-9 w-[96px] text-center ${!purchaseLimit && showErrors ? "border-destructive bg-destructive/5 focus-visible:ring-destructive" : ""}`}
            />
            <span className="text-sm font-medium text-muted-foreground">장</span>
          </div>
          {!purchaseLimit && showErrors && (
            <p className="text-[10px] font-medium text-destructive">1인당 구매 가능 티켓 수를 입력해주세요.</p>
          )}
        </div>

        <div className="rounded-lg border bg-muted/10 p-4">
          <div className="mb-3 flex items-center justify-between">
            <Label className={`text-sm ${grades.length === 0 && showErrors ? "text-destructive" : ""}`}>
              좌석 등급 및 가격 <span className="text-destructive">*</span>
            </Label>
            <Button
              variant="outline"
              size="sm"
              className="h-8 px-2"
              onClick={() => {
                onAddGrade()
                setExpandedGradeIndex(grades.length)
              }}
            >
              <Plus className="mr-1 size-3.5" />
              추가
            </Button>
          </div>
          {grades.length === 0 && showErrors && (
            <p className="mb-3 text-[10px] font-medium text-destructive">좌석 등급을 최소 1개 이상 추가해주세요.</p>
          )}

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

          <div className="grid gap-4 lg:grid-cols-[260px_minmax(0,1fr)]">
            <div className="rounded-md border bg-background p-3">
              <div className="mb-2 flex items-center justify-between">
                <Label className="text-xs text-muted-foreground">구역 번호 참고(sectionId)</Label>
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  className="h-7 px-2 text-[11px]"
                  onClick={() => setIsImageModalOpen(true)}
                >
                  <ZoomIn className="mr-1 size-3.5" />
                  크게 보기
                </Button>
              </div>
              <div
                className="flex cursor-pointer items-center justify-center overflow-hidden rounded border border-border/50 bg-muted/20"
                onClick={() => setIsImageModalOpen(true)}
              >
                <img
                  src={mapImageSrc}
                  alt="구역 안내도"
                  className="max-h-44 w-full object-contain"
                  onError={(e) => {
                    ;(e.target as HTMLImageElement).src = "/venue_map/sectionId1.png"
                  }}
                />
              </div>
            </div>

            <div className="max-h-[560px] overflow-y-auto pr-1">
              <div className="flex flex-col gap-3">
              {grades.map((grade, idx) => {
                const isExpanded = expandedGradeIndex === idx
                const rawSelected = grade.sectionId ? parseSelectedSectionIds(grade.sectionId) : []
                const validSelected = rawSelected.filter((id) =>
                  availableSections.some((sec) => sec.sectionId.toString() === id)
                )
                const usedSectionIds = new Set(
                  grades.flatMap((otherGrade, otherIndex) =>
                    otherIndex === idx ? [] : parseSelectedSectionIds(otherGrade.sectionId)
                  )
                )

                return (
                  <div key={`grade-${idx}`} className="relative rounded-lg border bg-background p-3 pl-5 shadow-sm">
                    <div
                      className="absolute bottom-0 left-0 top-0 w-2 rounded-l-lg"
                      style={{ backgroundColor: grade.colorCode || "#ccc" }}
                      title="등급 색상"
                    />

                    <div className="mb-3 flex items-center justify-between">
                      <button
                        type="button"
                        className="flex items-center gap-2"
                        onClick={() => setExpandedGradeIndex((current) => (current === idx ? -1 : idx))}
                      >
                        <span className="text-xs font-semibold text-muted-foreground">등급 {idx + 1}</span>
                        {isExpanded ? (
                          <ChevronUp className="size-3.5 text-muted-foreground" />
                        ) : (
                          <ChevronDown className="size-3.5 text-muted-foreground" />
                        )}
                      </button>
                      <div className="flex items-center gap-1.5">
                        <div className="relative">
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            className="h-7 w-7 p-0 text-muted-foreground hover:bg-muted/60"
                            title="등급 색상 선택"
                          >
                            <Palette className="size-3.5 text-muted-foreground" />
                          </Button>
                          <Input
                            type="color"
                            value={grade.colorCode || "#000000"}
                            onChange={(e) => onUpdateGrade(idx, "colorCode", e.target.value)}
                            className="absolute inset-0 h-full w-full cursor-pointer opacity-0"
                            aria-label={`등급 ${idx + 1} 색상 선택`}
                          />
                        </div>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7 text-muted-foreground hover:bg-destructive/10 hover:text-destructive"
                          onClick={() => onRemoveGrade(idx)}
                        >
                          <Trash2 className="size-3.5" />
                        </Button>
                      </div>
                    </div>

                    {!isExpanded && (
                      <div className="rounded-md border bg-muted/20 px-3 py-2 text-[11px] text-muted-foreground">
                        <span className="font-medium text-foreground">{grade.gradeName || `등급 ${idx + 1}`}</span>
                        {" · "}
                        <span>{grade.price ? `${Number(grade.price).toLocaleString()} CTK` : "가격 미입력"}</span>
                        {" · "}
                        <span>{validSelected.length > 0 ? `${validSelected.length}개 구역` : "구역 미선택"}</span>
                      </div>
                    )}

                    {isExpanded && <div className="space-y-3">
                      <div className="space-y-1.5">
                        <Label className="text-[11px] font-medium">등급명</Label>
                        <Input
                          placeholder="등급명(예: VIP, R석, S석)"
                          value={grade.gradeName}
                          onChange={(e) => onUpdateGrade(idx, "gradeName", e.target.value)}
                          className={`h-9 text-xs font-semibold ${!grade.gradeName.trim() && showErrors ? "border-destructive bg-destructive/5 focus-visible:ring-destructive" : ""}`}
                        />
                      </div>

                      <div className="space-y-1.5">
                        <Label className="text-[11px] font-medium">가격(CTK)</Label>
                        <div className="relative">
                          <Input
                            type="text"
                            placeholder="가격(예: 15 CTK)"
                            value={grade.price ? Number(grade.price).toLocaleString() : ""}
                            onChange={(e) => {
                              const rawValue = e.target.value.replace(/,/g, "")
                              if (!isNaN(Number(rawValue))) {
                                onUpdateGrade(idx, "price", rawValue)
                              }
                            }}
                            className={`h-9 pr-10 text-xs ${(!grade.price || Number(grade.price) <= 0) && showErrors ? "border-destructive bg-destructive/5 focus-visible:ring-destructive" : ""}`}
                          />
                          <span className="absolute right-2 top-1/2 -translate-y-1/2 text-[12px] text-muted-foreground font-medium">CTK</span>
                        </div>
                      </div>

                      <div className="space-y-1.5">
                        <Label className="text-[11px] font-medium">구역 선택</Label>
                        <Popover>
                          <PopoverTrigger asChild>
                            <Button
                              variant="outline"
                              role="combobox"
                              className={`h-9 w-full justify-between px-2 text-xs font-normal ${validSelected.length === 0 ? "text-muted-foreground" : ""} ${validSelected.length === 0 && showErrors ? "border-destructive bg-destructive/5" : ""}`}
                            >
                              <span className="truncate">
                                {validSelected.length > 0
                                  ? `${validSelected.length}개 구역 선택됨`
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
                                    const selectedSections = grade.sectionId
                                      ? parseSelectedSectionIds(grade.sectionId)
                                      : []
                                    const sectionIdString = section.sectionId.toString()
                                    const isSelected = selectedSections.includes(sectionIdString)
                                    const isDisabled = usedSectionIds.has(sectionIdString)

                                    const toggleSelection = () => {
                                      if (isDisabled && !isSelected) {
                                        return
                                      }

                                      const nextSelected = isSelected
                                        ? selectedSections.filter((s) => s !== sectionIdString)
                                        : [...selectedSections, sectionIdString]

                                      onUpdateGrade(
                                        idx,
                                        "sectionId",
                                        nextSelected.sort((a, b) => Number(a) - Number(b)).join(", ")
                                      )
                                    }

                                    return (
                                      <div
                                        key={section.sectionId}
                                        onClick={toggleSelection}
                                        className={`flex h-8 items-center justify-center rounded-md border px-3 text-xs font-medium transition-colors ${
                                          isSelected
                                            ? "bg-primary text-primary-foreground border-primary"
                                            : isDisabled
                                              ? "bg-muted text-muted-foreground border-border cursor-not-allowed opacity-50"
                                              : "bg-background hover:bg-muted border-border"
                                        }`}
                                        aria-disabled={isDisabled && !isSelected}
                                      >
                                        {section.sectionName}
                                      </div>
                                    )
                                  })}
                                </div>
                                {validSelected.length > 0 && (
                                  <div className="mt-3 border-t pt-2 text-muted-foreground whitespace-normal break-words leading-relaxed">
                                    선택: {validSelected
                                      .map((id) => {
                                        const sec = availableSections.find((a) => a.sectionId.toString() === id)
                                        return sec ? sec.sectionName : id
                                      })
                                      .join(", ")}
                                  </div>
                                )}
                              </>
                            ) : (
                              <div className="py-4 text-center text-muted-foreground">
                                해당 공연장의 구역 정보가 없습니다.
                              </div>
                            )}
                          </PopoverContent>
                        </Popover>
                      </div>
                    </div>}

                    <div className="mt-3">
                      <TicketEffectPreview
                        posterUrl={posterPreview}
                        selectedEffectId={grade.ticketEffectId}
                        onSelectEffect={(effectId) => onUpdateGrade(idx, "ticketEffectId", effectId)}
                        ticketEffects={ticketEffects}
                      />
                    </div>
                  </div>
                )
              })}
              </div>
            </div>
          </div>
        </div>
      </CardContent>

      {isImageModalOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in duration-200"
          onClick={() => setIsImageModalOpen(false)}
        >
          <div className="relative max-w-5xl max-h-[90vh] w-full flex items-center justify-center" onClick={(e) => e.stopPropagation()}>
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
                ;(e.target as HTMLImageElement).src = "/venue_map/sectionId1.png"
              }}
            />
          </div>
        </div>
      )}

      <Separator className="my-2" />

      <CardContent className="flex flex-col gap-4 pt-0 pb-4">
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

          {sessionInfo.length === 0 && showErrors && (
            <p className="mb-2 text-[10px] font-medium text-destructive">회차 정보를 최소 1개 이상 추가해주세요.</p>
          )}

          {invalidSessionCount > 0 && (
            <div className="mb-2 rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-[11px] text-amber-700">
              회차 {invalidSessionCount}개가 전체 공연 일정 범위를 벗어났습니다.
            </div>
          )}

            <div className="space-y-1">
              {sessionInfo.map((sess, idx) => (
                <div
                  key={`sess-${idx}`}
                  className="grid w-full grid-cols-1 gap-1.5 rounded-md border bg-background px-3 py-1.5 sm:inline-grid sm:w-auto sm:grid-cols-[96px_360px_28px] sm:items-center sm:gap-2"
                >
                  <div className="w-24 shrink-0 text-xs font-semibold text-slate-700">회차 {idx + 1}</div>
                  <div className={`w-full sm:w-[360px] ${(!sess.sessionDate || !sess.sessionStartTime) && showErrors ? "rounded-md border border-destructive bg-destructive/5 p-1" : ""}`}>
                  <DateTimePicker
                    value={sess.sessionDate && sess.sessionStartTime ? `${sess.sessionDate}T${sess.sessionStartTime}` : undefined}
                    onChange={(val) => {
                      const [date, time] = val.split("T")
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
                    className="h-7 w-7 text-muted-foreground hover:text-destructive hover:bg-destructive/10"
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
    </Card>
  )
}
