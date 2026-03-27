"use client"

import { useEffect, useState, type ChangeEvent } from "react"
import { useRouter } from "next/navigation"
import { useToast } from "@/hooks/use-toast"
import { ApiError } from "@/lib/api"
import {
  createShow,
  fetchShowVenues,
  updateShow,
  type HostShowDetail,
  type HostShowVenueOption,
} from "@/lib/show-manage-api"
import type { Grade, RefundItem, SessionItem, Stakeholder } from "./showFormTypes"
import {
  buildInitialGrades,
  buildInitialRefundPolicy,
  buildInitialSessionInfo,
  buildInitialStakeholders,
  buildCreatePayload,
  deriveReservationEndFromSessions,
  deriveShowRangeFromSessions,
  buildUpdatePayload,
  buildValidationMessage,
  isContentOnlyEditableStatus,
  toLocalDateTimeValue,
  toNumericString,
} from "./showFormUtils"

const MAX_FILE_SIZE = 5 * 1024 * 1024
const MAX_TOTAL_SIZE = 50 * 1024 * 1024
const ALLOWED_IMAGE_TYPES = new Set(["image/jpeg", "image/png", "image/webp"])

function toRoundedMb(bytes: number) {
  return Math.round(bytes / (1024 * 1024))
}

interface UseShowFormParams {
  mode: "create" | "edit"
  initialData?: HostShowDetail
}

export function useShowForm({ mode, initialData }: UseShowFormParams) {
  const router = useRouter()
  const { toast } = useToast()
  const isEdit = mode === "edit"
  const isContentOnlyEdit = isEdit && isContentOnlyEditableStatus(initialData?.status)

  const [title, setTitle] = useState(initialData?.title ?? "")
  const [artistName, setArtistName] = useState(initialData?.artistName ?? "")
  const [playtime, setPlaytime] = useState(toNumericString(initialData?.playtime))
  const [posterPreview, setPosterPreview] = useState<string | null>(initialData?.posterUrl ?? null)
  const [posterFile, setPosterFile] = useState<File | null>(null)
  const [description, setDescription] = useState(initialData?.description ?? "")
  const [descriptionImageFiles, setDescriptionImageFiles] = useState<File[]>([])
  const [descriptionImagePreviews, setDescriptionImagePreviews] = useState<string[]>(
    isEdit ? (initialData?.descriptionImages ?? []) : []
  )
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
  const [venues, setVenues] = useState<HostShowVenueOption[]>([])
  const [isLoadingVenues, setIsLoadingVenues] = useState(true)
  const [venueLoadError, setVenueLoadError] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const loadVenues = async () => {
      setIsLoadingVenues(true)
      setVenueLoadError("")

      try {
        const venueOptions = await fetchShowVenues()

        if (!isMounted) {
          return
        }

        setVenues(venueOptions)
      } catch (error) {
        if (!isMounted) {
          return
        }

        const message =
          error instanceof ApiError
            ? error.message
            : "怨듭뿰??紐⑸줉??遺덈윭?ㅼ? 紐삵뻽?듬땲?? ?좎떆 ???ㅼ떆 ?쒕룄?댁＜?몄슂."

        setVenueLoadError(message)
        toast({
          title: "怨듭뿰??紐⑸줉 議고쉶 ?ㅽ뙣",
          description: message,
          variant: "destructive",
        })
      } finally {
        if (isMounted) {
          setIsLoadingVenues(false)
        }
      }
    }

    void loadVenues()

    return () => {
      isMounted = false
    }
  }, [toast])

  useEffect(() => {
    const nextRange = deriveShowRangeFromSessions(sessionInfo, playtime)

    setShowStartAt((previous) => (previous === nextRange.showStartAt ? previous : nextRange.showStartAt))
    setShowEndAt((previous) => (previous === nextRange.showEndAt ? previous : nextRange.showEndAt))
  }, [sessionInfo, playtime])

  useEffect(() => {
    const nextCloseAt = deriveReservationEndFromSessions(sessionInfo)
    setCloseAt((previous) => (previous === nextCloseAt ? previous : nextCloseAt))
  }, [sessionInfo])

  const handlePosterChange = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]

    if (!file) {
      return
    }

    if (!ALLOWED_IMAGE_TYPES.has(file.type)) {
      toast({
        title: "吏?먰븯吏 ?딅뒗 ?뚯씪 ?뺤떇",
        description: "????ъ뒪?곕뒗 JPEG, PNG, WEBP ?뺤떇留??낅줈??媛?ν빀?덈떎.",
        variant: "destructive",
      })
      event.target.value = ""
      return
    }

    if (file.size > MAX_FILE_SIZE) {
      toast({
        title: "?뚯씪 ?⑸웾 珥덇낵",
        description: "????ъ뒪?곕뒗 5MB ?댄븯留??낅줈??媛?ν빀?덈떎.",
        variant: "destructive",
      })
      event.target.value = ""
      return
    }

    const nextRequestSize =
      file.size + descriptionImageFiles.reduce((total, descriptionFile) => total + descriptionFile.size, 0)

    if (nextRequestSize > MAX_TOTAL_SIZE) {
      toast({
        title: "?꾩껜 ?⑸웾 珥덇낵",
        description: `?대쾲 ?붿껌???대?吏 珥앺빀? 50MB瑜??섏쓣 ???놁뒿?덈떎. (?꾩옱 ${toRoundedMb(nextRequestSize)}MB)`,
        variant: "destructive",
      })
      event.target.value = ""
      return
    }

    const reader = new FileReader()
    reader.onload = () => {
      setPosterPreview(reader.result as string)
      setPosterFile(file)
    }
    reader.readAsDataURL(file)
  }

  const handleDescriptionImagesChange = (event: ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files

    if (!files || files.length === 0) {
      return
    }

    const fileArray = Array.from(files)

    const invalidTypeFiles = fileArray.filter((file) => !ALLOWED_IMAGE_TYPES.has(file.type))
    if (invalidTypeFiles.length > 0) {
      toast({
        title: "吏?먰븯吏 ?딅뒗 ?뚯씪 ?뺤떇",
        description: "?곸꽭 ?대?吏??JPEG, PNG, WEBP ?뺤떇留??낅줈??媛?ν빀?덈떎.",
        variant: "destructive",
      })
      event.target.value = ""
      return
    }

    const oversizedFiles = fileArray.filter((file) => file.size > MAX_FILE_SIZE)
    if (oversizedFiles.length > 0) {
      toast({
        title: "?뚯씪 ?⑸웾 珥덇낵",
        description: "媛??곸꽭 ?대?吏??5MB ?댄븯留??낅줈??媛?ν빀?덈떎.",
        variant: "destructive",
      })
      event.target.value = ""
      return
    }

    const currentTotalSize = descriptionImageFiles.reduce((total, file) => total + file.size, 0)
    const incomingTotalSize = fileArray.reduce((total, file) => total + file.size, 0)
    const posterSize = posterFile?.size ?? 0
    const nextTotalSize = posterSize + currentTotalSize + incomingTotalSize

    if (nextTotalSize > MAX_TOTAL_SIZE) {
      toast({
        title: "?꾩껜 ?⑸웾 珥덇낵",
        description: `?대쾲 ?붿껌???대?吏 珥앺빀? 50MB瑜??섏쓣 ???놁뒿?덈떎. (?꾩옱 ${toRoundedMb(nextTotalSize)}MB)`,
        variant: "destructive",
      })
      event.target.value = ""
      return
    }

    setDescriptionImageFiles((previous) => [...previous, ...fileArray])

    const readers = fileArray.map(
      (file) =>
        new Promise<string>((resolve) => {
          const reader = new FileReader()
          reader.onload = () => resolve(reader.result as string)
          reader.readAsDataURL(file)
        })
    )

    Promise.all(readers).then((previews) => {
      setDescriptionImagePreviews((previous) => [...previous, ...previews])
    })
    event.target.value = ""
  }

  const handleRemoveDescriptionImage = (targetIndex: number) => {
    const targetPreview = descriptionImagePreviews[targetIndex]
    const isExistingRemoteImage =
      typeof targetPreview === "string" &&
      (targetPreview.startsWith("http://") || targetPreview.startsWith("https://"))

    if (!isExistingRemoteImage) {
      const localFileIndex = descriptionImagePreviews
        .slice(0, targetIndex + 1)
        .filter(
          (preview) =>
            typeof preview === "string" &&
            !preview.startsWith("http://") &&
            !preview.startsWith("https://")
        ).length - 1

      setDescriptionImageFiles((previous) =>
        previous.filter((_, index) => index !== localFileIndex)
      )
    }

    setDescriptionImagePreviews((previous) => previous.filter((_, index) => index !== targetIndex))
  }

  const handleVenueChange = (nextVenueId: string) => {
    setVenueId(nextVenueId)

    const selectedVenue = venues.find((venue) => venue.venueId.toString() === nextVenueId)

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
    setGrades((previous) => [
      ...previous,
      {
        sectionId: "",
        gradeName: "",
        price: "",
        colorCode: "#aaaaaa",
        ticketEffectId: "",
      },
    ])
  }

  const removeGrade = (targetIndex: number) => {
    setGrades((previous) => previous.filter((_, index) => index !== targetIndex))
  }

  const updateGrade = (targetIndex: number, field: keyof Grade, value: string) => {
    setGrades((previous) =>
      previous.map((grade, index) =>
        index === targetIndex ? { ...grade, [field]: value } : grade
      )
    )
  }

  const addStakeholder = () => {
    setStakeholders((previous) => [
      ...previous,
      { role: "ARTIST", name: "", phone: "", shareBps: "", verified: false },
    ])
  }

  const removeStakeholder = (targetIndex: number) => {
    setStakeholders((previous) => {
      if (previous[targetIndex]?.isFixed) {
        return previous
      }

      return previous.filter((_, index) => index !== targetIndex)
    })
  }

  const updateStakeholder = (
    targetIndex: number,
    field: keyof Stakeholder,
    value: string | number | boolean
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
    const selectedVenue = venues.find((venue) => venue.venueId.toString() === venueId)
    const defaultCapacity = selectedVenue ? selectedVenue.capacity : ""

    setSessionInfo((previous) => [
      ...previous,
      { sessionDate: "", sessionStartTime: "", capacity: defaultCapacity },
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

  const getValidationMessage = () =>
    buildValidationMessage({
      mode,
      contentOnlyEdit: isContentOnlyEdit,
      title,
      artistName,
      playtime,
      posterPreview,
      posterFile,
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
      descriptionImageFiles,
    }, initialData)

  const handleSubmit = async () => {
    const validationMessage = getValidationMessage()

    if (validationMessage) {
      toast({
        title: "입력값을 확인해 주세요",
        description: validationMessage,
        variant: "destructive",
      })
      return
    }

    setIsSubmitting(true)

    try {
      if (isEdit && initialData?.showId) {
        const response = await updateShow(
          initialData.showId,
          buildUpdatePayload({
            title,
            artistName,
            playtime,
            posterPreview,
            posterFile,
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
            descriptionImageFiles,
          }, descriptionImagePreviews, initialData, { contentOnlyEdit: isContentOnlyEdit })
        )
        window.alert(response.responseMessage || "공연 수정 완료")
      } else {
        const response = await createShow(
          buildCreatePayload({
            title,
            artistName,
            playtime,
            posterPreview,
            posterFile,
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
            descriptionImageFiles,
          })
        )
        window.alert(
          response
            ? `공연이 등록되었습니다. (공연 ID: ${response})`
            : "공연이 등록되었습니다."
        )
      }

      router.push("/mypage")
    } catch (error) {
      toast({
        title: isEdit ? "공연 수정 실패" : "공연 등록 실패",
        description:
          error instanceof ApiError
            ? error.message
            : "?붿껌??泥섎━?섏? 紐삵뻽?듬땲?? ?좎떆 ???ㅼ떆 ?쒕룄?댁＜?몄슂.",
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return {
    isEdit,
    isContentOnlyEdit,
    title,
    artistName,
    playtime,
    posterPreview,
    description,
    descriptionImageFiles,
    descriptionImagePreviews,
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
    venues,
    isLoadingVenues,
    venueLoadError,
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
  }
}

