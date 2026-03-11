"use client"

import { useState, type ChangeEvent } from "react"
import { useRouter } from "next/navigation"
import { useToast } from "@/hooks/use-toast"
import { ApiError } from "@/lib/api"
import { mockVenues } from "@/mocks/data/show-store"
import { createShow, updateShow, type HostShowDetail } from "@/lib/show-manage-api"
import type { Grade, RefundItem, SessionItem, Stakeholder } from "./showFormTypes"
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

interface UseShowFormParams {
  mode: "create" | "edit"
  initialData?: HostShowDetail
}

export function useShowForm({ mode, initialData }: UseShowFormParams) {
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

  return {
    isEdit,
    title,
    artistName,
    posterPreview,
    description,
    venueId,
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
    setDescription,
    setPurchaseLimit,
    setShowStartAt,
    setShowEndAt,
    setOpenAt,
    setCloseAt,
    handlePosterChange,
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
    handleSubmit,
  }
}
