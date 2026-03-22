"use client"

import { useMemo, useState } from "react"
import Link from "next/link"
import Image from "next/image"
import { ArrowLeft, ImagePlus, Upload } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { type HostShowDetail } from "@/lib/show-manage-api"
import { DescriptionEditor } from "./DescriptionEditor"
import { SettingsCardBasic } from "./SettingsCardBasic"
import { SettingsCardPolicies } from "./SettingsCardPolicies"
import { SettingsCardTickets } from "./SettingsCardTickets"
import { PLATFORM_FEE_BPS, PLATFORM_TOTAL_BPS } from "./showFormUtils"
import { useShowForm } from "./useShowForm"

interface ShowFormProps {
  mode: "create" | "edit"
  initialData?: HostShowDetail
}

const SETTLEMENT_CONFIRM_TEXT = "정산 비율은 추후에 수정할 수 없습니다."

function formatSharePercent(shareBps: string) {
  const percent = (Number(shareBps) || 0) / 100
  return `${new Intl.NumberFormat("ko-KR", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(percent)}%`
}

function parseSectionCount(sectionId: string) {
  return sectionId
    .split(",")
    .map((value) => value.trim())
    .filter(Boolean).length
}

export function ShowForm({ mode, initialData }: ShowFormProps) {
  const { toast } = useToast()
  const {
    isEdit,
    title,
    artistName,
    playtime,
    posterPreview,
    descriptionImagePreviews,
    description,
    venueId,
    venues,
    isLoadingVenues,
    venueLoadError,
    showStartAt,
    showEndAt,
    openAt,
    closeAt,
    purchaseLimit,
    grades,
    stakeholders,
    refundPolicy,
    sessionInfo,
    isSubmitting,
    setTitle,
    setArtistName,
    setPlaytime,
    setDescription,
    setPurchaseLimit,
    setShowStartAt,
    setShowEndAt,
    setOpenAt,
    setCloseAt,
    handlePosterChange,
    handleDescriptionImagesChange,
    handleRemoveDescriptionImage,
    handleVenueChange,
    addGrade,
    removeGrade,
    updateGrade,
    addStakeholder,
    removeStakeholder,
    updateStakeholder,
    addRefund,
    removeRefund,
    updateRefund,
    addSession,
    removeSession,
    updateSession,
    getValidationMessage,
    handleSubmit,
  } = useShowForm({ mode, initialData })

  const [step, setStep] = useState(1)
  const [showStep1Errors, setShowStep1Errors] = useState(false)
  const [showStep2Errors, setShowStep2Errors] = useState(false)
  const [showStep3Errors, setShowStep3Errors] = useState(false)
  const [showStep4Errors, setShowStep4Errors] = useState(false)
  const [isConfirmOpen, setIsConfirmOpen] = useState(false)
  const [confirmText, setConfirmText] = useState("")

  const headerTitle = isEdit ? "공연 수정" : "공연 등록"
  const submitLabel = isEdit ? "수정하기" : "등록하기"
  const isRemotePosterPreview =
    typeof posterPreview === "string" &&
    (posterPreview.startsWith("http://") || posterPreview.startsWith("https://"))

  const stakeholderShareBps = useMemo(
    () =>
      stakeholders
        .filter((stakeholder) => !stakeholder.isFixed)
        .reduce((sum, stakeholder) => sum + (Number(stakeholder.shareBps) || 0), 0),
    [stakeholders]
  )

  const settlementSummaryItems = useMemo(
    () =>
      stakeholders.map((stakeholder, index) => ({
        name: stakeholder.isFixed ? "플랫폼" : stakeholder.name.trim() || `이해관계자 ${index}`,
        shareText: formatSharePercent(stakeholder.shareBps),
        isFixed: Boolean(stakeholder.isFixed),
      })),
    [stakeholders]
  )

  const canConfirmCreate = confirmText.trim() === SETTLEMENT_CONFIRM_TEXT

  const validateStep1 = () =>
    Boolean(
      title.trim() &&
        artistName.trim() &&
        posterPreview &&
        description.trim() &&
        Number.isInteger(Number(playtime)) &&
        Number(playtime) > 0
    )

  const validateStep2 = () => Boolean(venueId && showStartAt && showEndAt && openAt && closeAt)

  const validateStep3 = () => {
    const hasPurchaseLimit = Number.isInteger(Number(purchaseLimit)) && Number(purchaseLimit) > 0
    const hasValidGrades =
      grades.length > 0 &&
      grades.every(
        (grade) =>
          grade.gradeName.trim() &&
          Number.isFinite(Number(grade.price)) &&
          Number(grade.price) > 0 &&
          parseSectionCount(grade.sectionId) > 0
      )
    const hasValidSessions =
      sessionInfo.length > 0 &&
      sessionInfo.every((session) => session.sessionDate && session.sessionStartTime)

    return hasPurchaseLimit && hasValidGrades && hasValidSessions
  }

  const goNextStep = () => {
    if (step === 1 && !validateStep1()) {
      setShowStep1Errors(true)
      window.scrollTo({ top: 0, behavior: "smooth" })
      return
    }

    if (step === 2 && !validateStep2()) {
      setShowStep2Errors(true)
      window.scrollTo({ top: 0, behavior: "smooth" })
      return
    }

    if (step === 3 && !validateStep3()) {
      setShowStep3Errors(true)
      window.scrollTo({ top: 0, behavior: "smooth" })
      return
    }

    window.scrollTo({ top: 0, behavior: "smooth" })
    setStep((current) => Math.min(4, current + 1))
  }

  const handlePrimarySubmit = () => {
    const validationMessage = getValidationMessage()

    if (validationMessage) {
      setShowStep1Errors(true)
      setShowStep2Errors(true)
      setShowStep3Errors(true)
      setShowStep4Errors(true)
      window.scrollTo({ top: 0, behavior: "smooth" })
      toast({
        title: "입력 정보 확인",
        description: validationMessage,
        variant: "destructive",
      })
      return
    }

    if (isEdit) {
      void handleSubmit()
      return
    }

    setConfirmText("")
    setIsConfirmOpen(true)
  }

  const handleConfirmCreate = () => {
    if (!canConfirmCreate) {
      return
    }

    setIsConfirmOpen(false)
    void handleSubmit()
  }

  return (
    <>
      <main className="mx-auto max-w-6xl px-6 py-10 mb-16">
        <div className="sticky top-0 z-40 -mx-6 -mt-10 mb-8 border-b bg-background/95 px-6 pb-4 pt-10 backdrop-blur supports-[backdrop-filter]:bg-background/80">
          <div className="mx-auto flex max-w-[900px] flex-col gap-5 md:flex-row md:items-center md:justify-between">
            <div className="flex shrink-0 items-center gap-3">
              <Link
                href={isEdit && initialData?.showId ? `/shows/${initialData.showId}` : "/mypage"}
                className="flex size-9 items-center justify-center rounded-md border bg-background transition-colors hover:bg-accent hover:text-accent-foreground"
                aria-label="뒤로가기"
              >
                <ArrowLeft className="size-4" />
              </Link>
              <h1 className="text-lg font-bold tracking-tight text-foreground whitespace-nowrap">{headerTitle}</h1>
            </div>

            <div className="flex w-full items-center gap-2 md:max-w-md">
              {["기본 정보", "일시/장소", "티켓/회차", "정책/정산"].map((label, idx) => {
                const current = idx + 1
                const isActive = step === current
                const isPast = step > current

                return (
                  <div
                    key={current}
                    className={`flex flex-1 flex-col gap-1.5 transition-all ${isPast ? "cursor-pointer hover:opacity-80" : ""}`}
                    onClick={() => isPast && setStep(current)}
                  >
                    <span
                      className={`text-[10px] sm:text-[11px] font-bold transition-colors ${
                        isActive ? "text-primary" : isPast ? "text-primary/70" : "text-muted-foreground/40"
                      }`}
                    >
                      {current}. {label}
                    </span>
                    <div className="h-1.5 w-full overflow-hidden rounded-full bg-muted">
                      <div
                        className={`h-full transition-all duration-500 ease-out ${
                          isPast ? "bg-primary/50 w-full" : isActive ? "bg-primary w-full" : "w-0 bg-transparent"
                        }`}
                      />
                    </div>
                  </div>
                )
              })}
            </div>

            <div className="flex shrink-0 items-center justify-end gap-2 md:w-[150px]">
              {step > 1 && (
                <Button
                  variant="outline"
                  size="sm"
                  className="h-9 w-16 font-bold"
                  onClick={() => {
                    window.scrollTo({ top: 0, behavior: "smooth" })
                    setStep((current) => Math.max(1, current - 1))
                  }}
                  disabled={isSubmitting}
                >
                  이전
                </Button>
              )}

              {step < 4 ? (
                <Button size="sm" className="h-9 w-16 font-bold shadow-sm" onClick={goNextStep}>
                  다음
                </Button>
              ) : (
                <Button
                  size="sm"
                  className="h-9 w-24 font-bold shadow-sm"
                  onClick={handlePrimarySubmit}
                  disabled={isSubmitting}
                >
                  {isSubmitting ? "처리 중..." : submitLabel}
                </Button>
              )}
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-8">
          {step === 1 && (
            <div className="mx-auto flex w-full max-w-[900px] animate-in flex-col gap-5 fade-in slide-in-from-bottom-4 duration-500">
              <Card className="p-5 sm:p-6">
                <div className="flex flex-col gap-6 md:flex-row md:items-start">
                  <div className="flex w-full shrink-0 flex-col gap-3 sm:w-[260px]">
                    <Label className={`text-[15px] font-bold ${!posterPreview && showStep1Errors ? "text-destructive" : "text-foreground"}`}>
                      대표 포스터 <span className="text-destructive">*</span>
                    </Label>
                    <label
                      htmlFor="poster-upload"
                      className={`group relative flex aspect-[3/4] w-full cursor-pointer flex-col items-center justify-center overflow-hidden rounded-xl border-2 border-dashed transition-colors ${
                        !posterPreview && showStep1Errors
                          ? "border-destructive bg-destructive/5 hover:bg-destructive/10"
                          : "border-muted-foreground/20 bg-muted/30 hover:bg-muted/60"
                      }`}
                    >
                      {posterPreview ? (
                        <>
                          <Image
                            src={posterPreview}
                            alt="포스터 미리보기"
                            fill
                            unoptimized={posterPreview.startsWith("data:") || isRemotePosterPreview}
                            className="object-cover transition-opacity group-hover:opacity-40"
                          />
                          <div className="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 transition-opacity group-hover:opacity-100">
                            <Button
                              type="button"
                              variant="secondary"
                              onClick={(event) => {
                                event.preventDefault()
                                document.getElementById("poster-upload")?.click()
                              }}
                              className="font-semibold shadow-md"
                            >
                              <ImagePlus className="mr-2 size-4" />
                              포스터 변경
                            </Button>
                          </div>
                        </>
                      ) : (
                        <div className="flex flex-col items-center justify-center gap-3 p-4 text-muted-foreground">
                          <div className="rounded-full bg-secondary p-3.5 transition-colors group-hover:bg-primary/10 group-hover:text-primary">
                            <Upload className="size-6" />
                          </div>
                          <div className="text-center">
                            <p className="font-bold text-foreground">포스터 업로드</p>
                            <p className="mt-1 text-[11px] font-medium text-muted-foreground">권장 3:4 / 5MB</p>
                          </div>
                        </div>
                      )}
                    </label>
                    <input
                      id="poster-upload"
                      type="file"
                      accept="image/jpeg,image/png,image/webp"
                      className="sr-only"
                      onChange={handlePosterChange}
                    />
                    {!posterPreview && showStep1Errors && (
                      <p className="text-[12px] font-medium text-destructive">대표 포스터를 등록해주세요.</p>
                    )}
                  </div>

                  <div className="flex w-full flex-1 flex-col justify-center gap-8">
                    <div className="flex flex-col gap-3">
                      <Label className={`text-[15px] font-bold ${!title.trim() && showStep1Errors ? "text-destructive" : "text-foreground"}`}>
                        공연 제목 <span className="text-destructive">*</span>
                      </Label>
                      <Input
                        placeholder="공연 제목을 입력하세요"
                        className={`h-14 px-4 text-lg font-bold ${!title.trim() && showStep1Errors ? "border-destructive bg-destructive/5 focus-visible:ring-destructive" : "bg-muted/20"}`}
                        value={title}
                        onChange={(event) => setTitle(event.target.value)}
                      />
                    </div>

                    <div className="flex flex-col gap-3">
                      <Label className={`text-[15px] font-bold ${!artistName.trim() && showStep1Errors ? "text-destructive" : "text-foreground"}`}>
                        아티스트 / 그룹명 <span className="text-destructive">*</span>
                      </Label>
                      <Input
                        placeholder="참여 아티스트를 입력하세요"
                        className={`h-14 px-4 text-lg font-bold ${!artistName.trim() && showStep1Errors ? "border-destructive bg-destructive/5 focus-visible:ring-destructive" : "bg-muted/20"}`}
                        value={artistName}
                        onChange={(event) => setArtistName(event.target.value)}
                      />
                    </div>

                    <div className="flex flex-col gap-3">
                      <Label className={`text-[15px] font-bold ${(!playtime.trim() || Number(playtime) <= 0) && showStep1Errors ? "text-destructive" : "text-foreground"}`}>
                        공연 시간(분) <span className="text-destructive">*</span>
                      </Label>
                      <Input
                        type="number"
                        placeholder="예: 120"
                        className={`h-14 px-4 text-lg font-bold ${(!playtime.trim() || Number(playtime) <= 0) && showStep1Errors ? "border-destructive bg-destructive/5 focus-visible:ring-destructive" : "bg-muted/20"}`}
                        value={playtime}
                        onChange={(event) => setPlaytime(event.target.value)}
                      />
                    </div>
                  </div>
                </div>
              </Card>

              <Card className="p-5 sm:p-6">
                <div className="flex flex-col gap-6 md:flex-row md:items-start">
                  <div className="flex w-full shrink-0 flex-col gap-3 sm:w-[260px]">
                    <div className="flex items-center justify-between">
                      <Label className="text-[15px] font-bold text-foreground">상세 이미지</Label>
                      <span className="text-[11px] font-medium text-muted-foreground">최대 50MB</span>
                    </div>

                    <input
                      id="description-image-upload"
                      type="file"
                      accept="image/jpeg,image/png,image/webp"
                      multiple
                      className="sr-only"
                      onChange={handleDescriptionImagesChange}
                    />

                    <div className="grid grid-cols-2 gap-3 pt-1">
                      <div
                        className="group flex aspect-square cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed bg-muted/30 text-muted-foreground transition-colors hover:bg-muted/60"
                        onClick={() => document.getElementById("description-image-upload")?.click()}
                      >
                        <div className="rounded-full bg-secondary p-2.5 transition-colors group-hover:bg-primary/10 group-hover:text-primary">
                          <ImagePlus className="size-5" />
                        </div>
                        <span className="text-[11px] font-bold text-foreground">추가하기</span>
                      </div>

                      {descriptionImagePreviews.map((preview, index) => (
                        <div key={preview + index} className="group relative aspect-square overflow-hidden rounded-xl border bg-muted/30 shadow-sm">
                          <Image
                            src={preview}
                            alt={`상세 이미지 ${index + 1}`}
                            fill
                            className="object-cover"
                            unoptimized={preview.startsWith("data:") || preview.startsWith("http")}
                          />
                          <div className="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 transition-opacity group-hover:opacity-100">
                            <button
                              type="button"
                              className="flex size-7 items-center justify-center rounded-full bg-destructive/90 text-white shadow-sm transition-colors hover:bg-destructive"
                              onClick={() => handleRemoveDescriptionImage(index)}
                            >
                              삭제
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="flex w-full flex-1 flex-col gap-3">
                    <Label className={`text-[15px] font-bold ${!description.trim() && showStep1Errors ? "text-destructive" : "text-foreground"}`}>
                      공연 요약 설명 <span className="text-destructive">*</span>
                    </Label>
                    <div className={`rounded-xl border shadow-sm [&_.tiptap]:h-[360px] [&_.tiptap]:p-6 [&_.tiptap]:text-[15px] [&_.tiptap]:leading-relaxed [&_.tiptap]:overflow-y-auto ${!description.trim() && showStep1Errors ? "border-destructive bg-destructive/5" : ""}`}>
                      <DescriptionEditor value={description} onChange={setDescription} />
                    </div>
                  </div>
                </div>
              </Card>
            </div>
          )}

          {step === 2 && (
            <div className="mx-auto flex w-full max-w-[800px] animate-in fade-in slide-in-from-bottom-4 flex-col gap-8 duration-500">
              <SettingsCardBasic
                venueId={venueId}
                venues={venues}
                isLoadingVenues={isLoadingVenues}
                venueLoadError={venueLoadError}
                showStartAt={showStartAt}
                showEndAt={showEndAt}
                openAt={openAt}
                closeAt={closeAt}
                showErrors={showStep2Errors}
                onChangeVenueId={handleVenueChange}
                onChangeShowRange={(startAt, endAt) => {
                  setShowStartAt(startAt)
                  setShowEndAt(endAt)
                }}
                onChangeReservationRange={(startAt, endAt) => {
                  setOpenAt(startAt)
                  setCloseAt(endAt)
                }}
              />
            </div>
          )}

          {step === 3 && (
            <div className="animate-in fade-in slide-in-from-bottom-4 flex flex-col gap-8 duration-500">
              <SettingsCardTickets
                venueId={venueId}
                showStartAt={showStartAt}
                showEndAt={showEndAt}
                posterPreview={posterPreview}
                purchaseLimit={purchaseLimit}
                grades={grades}
                onChangePurchaseLimit={setPurchaseLimit}
                onAddGrade={addGrade}
                onRemoveGrade={removeGrade}
                onUpdateGrade={updateGrade}
                sessionInfo={sessionInfo}
                onAddSession={addSession}
                onRemoveSession={removeSession}
                onUpdateSession={updateSession}
                showErrors={showStep3Errors}
              />
            </div>
          )}

          {step === 4 && (
            <div className="animate-in fade-in slide-in-from-bottom-4 flex flex-col gap-8 duration-500">
              <SettingsCardPolicies
                isEdit={isEdit}
                stakeholders={stakeholders}
                refundPolicy={refundPolicy}
                onAddStakeholder={addStakeholder}
                onRemoveStakeholder={removeStakeholder}
                onUpdateStakeholder={updateStakeholder}
                onAddRefund={addRefund}
                onRemoveRefund={removeRefund}
                onUpdateRefund={updateRefund}
                showErrors={showStep4Errors}
              />
            </div>
          )}
        </div>
      </main>

      <Dialog
        open={isConfirmOpen}
        onOpenChange={(open) => {
          setIsConfirmOpen(open)
          if (!open) {
            setConfirmText("")
          }
        }}
      >
        <DialogContent className="sm:max-w-xl">
          <DialogHeader>
            <DialogTitle>정산 비율 최종 확인</DialogTitle>
            <DialogDescription>
              공연 등록 후 정산 비율과 이해관계자 정보는 수정할 수 없습니다. 아래 정산 요약을 확인한 뒤 등록을 진행해주세요.
            </DialogDescription>
          </DialogHeader>

          <div className="grid gap-3 rounded-lg border bg-muted/30 p-4 text-sm">
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">총 정산 비율</span>
              <span className="font-semibold">{PLATFORM_TOTAL_BPS.toLocaleString()}bps</span>
            </div>
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">플랫폼 수수료</span>
              <span className="font-semibold">{PLATFORM_FEE_BPS.toLocaleString()}bps</span>
            </div>
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">이해관계자 입력 합계</span>
              <span className="font-semibold">{stakeholderShareBps.toLocaleString()}bps</span>
            </div>
          </div>

          <div className="rounded-lg border border-amber-300 bg-amber-50 p-4 text-sm text-amber-800">
            정산 비율은 등록 이후 변경할 수 없습니다. 등록 전에 이해관계자, 사업자번호/연락처, 분배 비율을 다시 확인해주세요.
          </div>

          <div className="space-y-2">
            <Label htmlFor="settlement-confirm-text">아래 문구를 그대로 입력해주세요.</Label>
            <div className="rounded-md bg-muted px-3 py-2 font-semibold text-foreground">
              {SETTLEMENT_CONFIRM_TEXT}
            </div>
            <Input
              id="settlement-confirm-text"
              value={confirmText}
              onChange={(event) => setConfirmText(event.target.value)}
              placeholder="안내 문구를 입력해주세요"
            />
          </div>

          <div className="space-y-2">
            <Label>정산 비율 요약</Label>
            <div className="max-h-64 space-y-2 overflow-y-auto rounded-md border bg-background p-3">
              {settlementSummaryItems.map((item) => (
                <div
                  key={`${item.name}-${item.shareText}`}
                  className="flex items-center justify-between gap-3 rounded-md bg-muted/40 px-3 py-2 text-sm"
                >
                  <span className={item.isFixed ? "font-semibold text-foreground" : "text-foreground"}>{item.name}</span>
                  <span className="font-semibold text-foreground">{item.shareText}</span>
                </div>
              ))}
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setIsConfirmOpen(false)
                setConfirmText("")
              }}
            >
              취소
            </Button>
            <Button onClick={handleConfirmCreate} disabled={!canConfirmCreate || isSubmitting}>
              {isSubmitting ? "등록 중..." : "확인하고 등록하기"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}
