"use client"

import { useState, type ChangeEvent } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { useToast } from "@/hooks/use-toast"
import { ApiError } from "@/lib/api"
import { mockVenues } from "@/lib/mock-data"
import {
  createShow,
  updateShow,
  type HostShowDetail,
} from "@/lib/show-manage-api"
import { ArrowLeft, Upload, ImagePlus, User, Music } from "lucide-react"
import { DescriptionEditor } from "./DescriptionEditor"
import { SettingsCardBasic } from "./SettingsCardBasic"
import { SettingsCardTickets } from "./SettingsCardTickets"
import { SettingsCardPolicies } from "./SettingsCardPolicies"
import type { Grade, Stakeholder, RefundItem, SessionItem } from "./showFormTypes"
import {
  buildInitialGrades,
  buildInitialRefundPolicy,
  buildInitialSessionInfo,
  buildInitialStakeholders,
  buildPayload,
  buildValidationMessage,
  toLocalDateTimeValue,
  toNumericString,
} from "./showFormUtils"

interface ShowFormProps {
  mode: "create" | "edit"
  initialData?: HostShowDetail
}

export function ShowForm({ mode, initialData }: ShowFormProps) {
  const router = useRouter()
  const { toast } = useToast()
  const isEdit = mode === "edit"

  const [title, setTitle] = useState(initialData?.title ?? "")
  const [artistName, setArtistName] = useState(initialData?.artistName ?? "")
  const [posterPreview, setPosterPreview] = useState<string | null>(initialData?.posterUrl ?? null)
  const [description, setDescription] = useState(initialData?.description ?? "")
  const [venueId, setVenueId] = useState(initialData?.venue.venueId?.toString() ?? "")
  const [showStartAt, setShowStartAt] = useState(
    toLocalDateTimeValue(initialData?.show.showStartDate, "00:00")
  )
  const [showEndAt, setShowEndAt] = useState(
    toLocalDateTimeValue(initialData?.show.showEndDate, "23:55")
  )
  const [openAt, setOpenAt] = useState(toLocalDateTimeValue(initialData?.reservation.startDate))
  const [closeAt, setCloseAt] = useState(toLocalDateTimeValue(initialData?.reservation.endDate))
  const [purchaseLimit, setPurchaseLimit] = useState(toNumericString(initialData?.purchaseLimit))
  const [grades, setGrades] = useState<Grade[]>(buildInitialGrades(initialData))
  const [stakeholders, setStakeholders] = useState<Stakeholder[]>(
    buildInitialStakeholders(initialData)
  )
  const [refundPolicy, setRefundPolicy] = useState<RefundItem[]>(
    buildInitialRefundPolicy(initialData)
  )
  const [sessionInfo, setSessionInfo] = useState<SessionItem[]>(buildInitialSessionInfo(initialData))
  const [isSubmitting, setIsSubmitting] = useState(false)

  const headerTitle = isEdit ? "공연 수정" : "공연 등록"
  const submitLabel = isEdit ? "수정하기" : "등록하기"

  const handlePosterChange = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]

    if (!file) {
      return
    }

    const reader = new FileReader()
    reader.onload = () => setPosterPreview(reader.result as string)
    reader.readAsDataURL(file)
  }

  const handleVenueChange = (nextVenueId: string) => {
    setVenueId(nextVenueId)

    const selectedVenue = mockVenues.find((venue) => venue.venueId.toString() === nextVenueId)

    if (selectedVenue) {
      setSessionInfo((previous) =>
        previous.map((session) => ({
          ...session,
          capacity: selectedVenue.capacity,
        }))
      )
    }

    setGrades((previous) => previous.map((grade) => ({ ...grade, sectionId: "" })))
  }

  const addGrade = () => {
    const sharedTicketEffectId = grades.find((grade) => grade.ticketEffectId)?.ticketEffectId ?? ""

    setGrades((previous) => [
      ...previous,
      {
        sectionId: "",
        gradeName: "",
        price: "",
        colorCode: "#aaaaaa",
        ticketEffectId: sharedTicketEffectId,
      },
    ])
  }

  const removeGrade = (targetIndex: number) => {
    setGrades((previous) => previous.filter((_, index) => index !== targetIndex))
  }

  const updateGrade = (targetIndex: number, field: keyof Grade, value: string) => {
    setGrades((previous) => {
      if (field === "ticketEffectId") {
        return previous.map((grade) => ({
          ...grade,
          ticketEffectId: value,
        }))
      }

      return previous.map((grade, index) =>
        index === targetIndex ? { ...grade, [field]: value } : grade
      )
    })
  }

  const addStakeholder = () => {
    setStakeholders((previous) => [...previous, { role: "artist", name: "", shareBps: "" }])
  }

  const removeStakeholder = (targetIndex: number) => {
    setStakeholders((previous) => previous.filter((_, index) => index !== targetIndex))
  }

  const updateStakeholder = (
    targetIndex: number,
    field: keyof Stakeholder,
    value: string | number
  ) => {
    setStakeholders((previous) =>
      previous.map((stakeholder, index) =>
        index === targetIndex ? { ...stakeholder, [field]: value } : stakeholder
      )
    )
  }

  const addRefund = () => {
    setRefundPolicy((previous) => [...previous, { daysRemaining: "", refundRate: "" }])
  }

  const removeRefund = (targetIndex: number) => {
    setRefundPolicy((previous) => previous.filter((_, index) => index !== targetIndex))
  }

  const updateRefund = (targetIndex: number, field: keyof RefundItem, value: string) => {
    setRefundPolicy((previous) =>
      previous.map((item, index) => (index === targetIndex ? { ...item, [field]: value } : item))
    )
  }

  const addSession = () => {
    const selectedVenue = mockVenues.find((venue) => venue.venueId.toString() === venueId)
    const defaultCapacity = selectedVenue ? selectedVenue.capacity : ""

    setSessionInfo((previous) => [
      ...previous,
      { sessionId: "", sessionDate: "", sessionStartDate: "", capacity: defaultCapacity },
    ])
  }

  const removeSession = (targetIndex: number) => {
    setSessionInfo((previous) => previous.filter((_, index) => index !== targetIndex))
  }

  const updateSession = (
    targetIndex: number,
    field: keyof SessionItem,
    value: string | number
  ) => {
    setSessionInfo((previous) =>
      previous.map((session, index) =>
        index === targetIndex ? { ...session, [field]: value } : session
      )
    )
  }

  const handleSubmit = async () => {
    const validationMessage = buildValidationMessage({
      mode,
      title,
      artistName,
      posterPreview,
      venueId,
      showStartAt,
      showEndAt,
      openAt,
      closeAt,
      description,
      purchaseLimit,
      grades,
      stakeholders,
      refundPolicy,
      sessionInfo,
    })

    if (validationMessage) {
      window.alert(validationMessage)
      return
    }

    const payload = buildPayload({
      title,
      artistName,
      posterPreview,
      venueId,
      showStartAt,
      showEndAt,
      openAt,
      closeAt,
      description,
      purchaseLimit,
      grades,
      stakeholders,
      refundPolicy,
      sessionInfo,
    })

    setIsSubmitting(true)

    try {
      if (isEdit && initialData?.showId) {
        const response = await updateShow(initialData.showId, payload)
        window.alert(response.responseMessage || "공연이 수정되었습니다.")
      } else {
        const response = await createShow(payload)
        window.alert(`공연이 등록되었습니다. (공연 ID: ${response.showId})`)
      }

      router.push("/mypage")
    } catch (error) {
      toast({
        title: isEdit ? "공연 수정 실패" : "공연 등록 실패",
        description:
          error instanceof ApiError
            ? error.message
            : "요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.",
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="mx-auto max-w-screen-xl px-4 py-8 md:px-6">
      <div className="mb-6 mt-2 flex flex-col gap-6 border-b pb-8">
        <div className="flex items-center gap-3">
          <Link
            href={isEdit && initialData?.showId ? `/shows/${initialData.showId}` : "/mypage"}
            className="flex size-9 items-center justify-center rounded-md border bg-background hover:bg-accent hover:text-accent-foreground"
            aria-label="뒤로가기"
          >
            <ArrowLeft className="size-4" />
          </Link>
          <span className="text-sm font-medium text-muted-foreground">{headerTitle}</span>
        </div>

        <div className="flex flex-col gap-6 rounded-xl border border-border/50 bg-muted/20 p-6">
          <div className="flex items-start gap-4">
            <div className="mt-2 hidden rounded-full bg-primary/10 p-2 text-primary/80 sm:block">
              <Music className="size-6" />
            </div>
            <div className="flex flex-1 flex-col gap-1.5">
              <Label className="ml-1 text-xs font-bold uppercase tracking-wider text-primary">
                공연 제목 (Title)
              </Label>
              <input
                type="text"
                placeholder="멋진 공연 제목을 입력하세요"
                className="w-full border-none bg-transparent px-1 text-3xl font-bold placeholder:text-muted-foreground/40 focus:outline-none focus:ring-0 md:text-4xl lg:text-5xl"
                value={title}
                onChange={(event) => setTitle(event.target.value)}
              />
            </div>
          </div>

          <div className="flex items-start gap-4">
            <div className="mt-1 hidden rounded-full bg-primary/10 p-2 text-primary/80 sm:block">
              <User className="size-5" />
            </div>
            <div className="flex flex-1 flex-col gap-1.5">
              <Label className="ml-1 text-xs font-bold uppercase tracking-wider text-primary">
                아티스트 / 그룹명 (Artist)
              </Label>
              <input
                type="text"
                placeholder="참여하는 아티스트 또는 그룹명을 입력하세요"
                className="w-full border-none bg-transparent px-1 text-xl font-semibold text-foreground/90 placeholder:text-muted-foreground/40 focus:outline-none focus:ring-0 md:text-2xl"
                value={artistName}
                onChange={(event) => setArtistName(event.target.value)}
              />
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 items-start gap-8 lg:grid-cols-12">
        <div className="flex flex-col gap-8 lg:col-span-8">
          <div className="rounded-lg border bg-card p-6 shadow-sm">
            <Label className="mb-4 block text-base font-semibold">대표 포스터</Label>
            <label
              htmlFor="poster-upload"
              className="group flex cursor-pointer flex-col items-center justify-center rounded-md border-2 border-dashed bg-muted/30 transition-colors hover:bg-muted/60"
            >
              {posterPreview ? (
                <div className="flex w-full flex-col items-center gap-3 p-4">
                  <div className="relative mx-auto aspect-[3/4] w-full max-w-sm overflow-hidden rounded-md border bg-muted/20">
                    {/* API and local upload previews can be data URLs or external URLs. */}
                    <img
                      src={posterPreview}
                      alt="포스터 미리보기"
                      className="h-full w-full object-contain"
                    />
                  </div>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    className="gap-2"
                    onClick={(event) => {
                      event.preventDefault()
                      document.getElementById("poster-upload")?.click()
                    }}
                  >
                    <ImagePlus className="size-4" />
                    포스터 변경
                  </Button>
                </div>
              ) : (
                <div className="mx-auto flex aspect-[3/4] w-full max-w-sm flex-col items-center justify-center gap-4 p-6 text-muted-foreground">
                  <div className="rounded-full bg-secondary p-4 group-hover:bg-background">
                    <Upload className="size-8" />
                  </div>
                  <div className="text-center">
                    <p className="font-medium text-foreground">클릭하여 포스터 업로드</p>
                    <p className="mt-1 text-sm">세로형 이미지 (권장 비율 3:4)</p>
                  </div>
                </div>
              )}
            </label>
            <input
              id="poster-upload"
              type="file"
              accept="image/*"
              className="sr-only"
              onChange={handlePosterChange}
            />
          </div>

          <DescriptionEditor value={description} onChange={setDescription} />
        </div>

        <div className="select-none lg:col-span-4">
          <div className="flex flex-col gap-6">
            <SettingsCardBasic
              venueId={venueId}
              showStartAt={showStartAt}
              showEndAt={showEndAt}
              openAt={openAt}
              closeAt={closeAt}
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

            <SettingsCardTickets
              venueId={venueId}
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
            />

            <SettingsCardPolicies
              stakeholders={stakeholders}
              refundPolicy={refundPolicy}
              onAddStakeholder={addStakeholder}
              onRemoveStakeholder={removeStakeholder}
              onUpdateStakeholder={updateStakeholder}
              onAddRefund={addRefund}
              onRemoveRefund={removeRefund}
              onUpdateRefund={updateRefund}
            />

            <div className="sticky bottom-6 mt-4">
              <Button
                className="h-14 w-full text-lg font-bold shadow-lg transition-all hover:shadow-xl"
                size="lg"
                onClick={handleSubmit}
                disabled={isSubmitting}
              >
                {isSubmitting ? "처리 중..." : submitLabel}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </main>
  )
}
