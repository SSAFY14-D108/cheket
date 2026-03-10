import type { HostShowDetail, ShowFormPayload } from "@/lib/show-manage-api"
import type { Grade, RefundItem, SessionItem, Stakeholder } from "./showFormTypes"

export interface ShowFormValues {
  mode: "create" | "edit"
  title: string
  artistName: string
  posterPreview: string | null
  venueId: string
  showStartAt: string
  showEndAt: string
  openAt: string
  closeAt: string
  description: string
  purchaseLimit: string
  grades: Grade[]
  stakeholders: Stakeholder[]
  refundPolicy: RefundItem[]
  sessionInfo: SessionItem[]
}

export function toLocalDateTimeValue(value?: string | null, fallbackTime = "00:00") {
  if (!value) {
    return ""
  }

  if (value.includes("T")) {
    return value.slice(0, 16)
  }

  return `${value}T${fallbackTime}`
}

export function toNumericString(value?: number | null) {
  return value === undefined || value === null ? "" : String(value)
}

export function parseSectionIds(rawValue: string) {
  return rawValue
    .split(",")
    .map((value) => Number(value.trim()))
    .filter((value) => Number.isInteger(value))
}

function toApiDateTimeValue(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

export function buildInitialGrades(initialData?: HostShowDetail): Grade[] {
  if (!initialData?.grade?.length) {
    return [{ gradeName: "", price: "", colorCode: "#7C6EF0", sectionId: "", ticketEffectId: "" }]
  }

  return initialData.grade.map((grade) => ({
    gradeName: grade.gradeName,
    price: String(grade.price),
    colorCode: grade.colorCode,
    sectionId: String(grade.sectionId),
    ticketEffectId: initialData.ticketEffectId ? String(initialData.ticketEffectId) : "",
  }))
}

export function buildInitialStakeholders(initialData?: HostShowDetail): Stakeholder[] {
  if (!initialData?.stakeholders?.length) {
    return [
      { role: "organizer", name: "", businessNo: "", shareBps: "" },
      { role: "artist", name: "", phone: "", shareBps: "" },
    ]
  }

  return initialData.stakeholders.map((stakeholder) => ({
    role: stakeholder.role,
    userId: stakeholder.userId,
    name: stakeholder.name ?? "",
    shareBps: String(stakeholder.shareBps),
  }))
}

export function buildInitialRefundPolicy(initialData?: HostShowDetail): RefundItem[] {
  if (!initialData?.refundPolicy?.length) {
    return [
      { daysRemaining: "14", refundRate: "100" },
      { daysRemaining: "7", refundRate: "70" },
    ]
  }

  return initialData.refundPolicy.map((policy) => ({
    daysRemaining: String(policy.daysRemaining),
    refundRate: String(policy.refundRate),
  }))
}

export function buildInitialSessionInfo(initialData?: HostShowDetail): SessionItem[] {
  if (!initialData?.sessionInfo?.length) {
    return [{ sessionId: "", sessionDate: "", sessionStartDate: "", capacity: "" }]
  }

  return initialData.sessionInfo.map((session) => ({
    sessionId: session.sessionId,
    sessionDate: session.sessionDate,
    sessionStartDate: session.sessionStartDate,
    capacity: session.capacity,
  }))
}

export function buildValidationMessage(values: ShowFormValues) {
  if (!values.title.trim()) {
    return "공연명을 입력해주세요."
  }

  if (values.mode === "create" && !values.artistName.trim()) {
    return "아티스트 또는 그룹명을 입력해주세요."
  }

  if (!values.posterPreview) {
    return "대표 포스터를 등록해주세요."
  }

  if (!values.venueId) {
    return "공연 장소를 선택해주세요."
  }

  if (!values.showStartAt || !values.showEndAt) {
    return "공연 진행 기간을 선택해주세요."
  }

  if (!values.openAt || !values.closeAt) {
    return "예매 가능 기간을 선택해주세요."
  }

  if (!values.description.trim()) {
    return "공연 설명을 입력해주세요."
  }

  if (!Number.isInteger(Number(values.purchaseLimit)) || Number(values.purchaseLimit) <= 0) {
    return "구매 제한 개수를 올바르게 입력해주세요."
  }

  if (
    values.grades.length === 0 ||
    values.grades.some(
      (grade) =>
        !grade.gradeName.trim() ||
        !Number.isFinite(Number(grade.price)) ||
        Number(grade.price) <= 0 ||
        parseSectionIds(grade.sectionId).length === 0
    )
  ) {
    return "좌석 등급, 가격, 구역 정보를 모두 입력해주세요."
  }

  if (
    values.stakeholders.length === 0 ||
    values.stakeholders.some(
      (stakeholder) =>
        !Number.isFinite(Number(stakeholder.shareBps)) || Number(stakeholder.shareBps) <= 0
    )
  ) {
    return "수익 분배 비율을 올바르게 입력해주세요."
  }

  const stakeholderShareSum = values.stakeholders.reduce(
    (sum, stakeholder) => sum + Number(stakeholder.shareBps),
    0
  )

  if (stakeholderShareSum !== 10000) {
    return "수익 분배 비율의 합계는 10000bps여야 합니다."
  }

  if (
    values.refundPolicy.length === 0 ||
    values.refundPolicy.some(
      (item) =>
        !Number.isFinite(Number(item.daysRemaining)) ||
        Number(item.daysRemaining) < 0 ||
        !Number.isFinite(Number(item.refundRate)) ||
        Number(item.refundRate) < 0 ||
        Number(item.refundRate) > 100
    )
  ) {
    return "환불 정책을 올바르게 입력해주세요."
  }

  if (
    values.sessionInfo.length === 0 ||
    values.sessionInfo.some((session) => !session.sessionDate || !session.sessionStartDate)
  ) {
    return "회차 정보의 날짜와 시간을 모두 입력해주세요."
  }

  return null
}

export function buildPayload(values: Omit<ShowFormValues, "mode">): ShowFormPayload {
  const sharedTicketEffectId = values.grades.find((grade) => grade.ticketEffectId)?.ticketEffectId

  const payload: ShowFormPayload = {
    title: values.title.trim(),
    posterUrl: values.posterPreview ?? "/images/poster-1.jpg",
    venueId: Number(values.venueId),
    show: {
      startAt: toApiDateTimeValue(values.showStartAt),
      endAt: toApiDateTimeValue(values.showEndAt),
    },
    reservation: {
      openAt: toApiDateTimeValue(values.openAt),
      closeAt: toApiDateTimeValue(values.closeAt),
    },
    description: values.description.trim(),
    purchaseLimit: Number(values.purchaseLimit),
    grade: values.grades.map((grade) => ({
      sectionId: parseSectionIds(grade.sectionId),
      gradeName: grade.gradeName.trim(),
      price: Number(grade.price),
      colorCode: grade.colorCode,
    })),
    stakeholders: values.stakeholders.map((stakeholder) => ({
      role: stakeholder.role,
      ...(stakeholder.userId ? { userId: stakeholder.userId } : {}),
      shareBps: Number(stakeholder.shareBps),
    })),
    refundPolicy: values.refundPolicy.map((item) => ({
      daysRemaining: Number(item.daysRemaining),
      refundRate: Number(item.refundRate),
    })),
    sessionInfo: values.sessionInfo.map((session, index) => ({
      sessionId: Number(session.sessionId) || index + 1,
      sessionDate: session.sessionDate,
      sessionStartDate: session.sessionStartDate,
    })),
  }

  if (values.artistName.trim()) {
    payload.artistName = values.artistName.trim()
  }

  if (sharedTicketEffectId) {
    payload.ticketEffectId = Number(sharedTicketEffectId)
  }

  return payload
}
