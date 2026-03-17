import type {
  CreateShowPayload,
  HostShowDetail,
  UpdateShowPayload,
  ShowFormPayload,
} from "@/lib/show-manage-api"
import type { Grade, RefundItem, SessionItem, Stakeholder } from "./showFormTypes"

const FIXED_PLATFORM_STAKEHOLDER: Stakeholder = {
  role: "organizer",
  name: "CHEKET",
  businessNo: "000-00-00000",
  shareBps: "800",
  verified: true,
  isFixed: true,
}

export interface ShowFormValues {
  mode: "create" | "edit"
  title: string
  artistName: string
  posterPreview: string | null
  posterFile: File | null
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

function buildGradeGroupingKey(grade: {
  gradeName: string
  price: number
  colorCode: string
  ticketEffectId?: number | null
}) {
  return [
    grade.gradeName.trim(),
    grade.price,
    grade.colorCode.trim().toLowerCase(),
    grade.ticketEffectId ?? "",
  ].join("|")
}

function toSessionTimestamp(sessionDate: string, sessionStartDate: string) {
  return new Date(`${sessionDate}T${sessionStartDate}`).getTime()
}

export function buildInitialGrades(initialData?: HostShowDetail): Grade[] {
  if (!initialData?.grade?.length) {
    return [{ gradeName: "", price: "", colorCode: "#7C6EF0", sectionId: "", ticketEffectId: "" }]
  }

  const gradeMap = new Map<string, Grade>()

  initialData.grade.forEach((grade) => {
    const key = buildGradeGroupingKey(grade)
    const existingGrade = gradeMap.get(key)

    if (existingGrade) {
      const mergedSectionIds = [...parseSectionIds(existingGrade.sectionId), grade.sectionId]
      const uniqueSectionIds = [...new Set(mergedSectionIds)].sort((left, right) => left - right)
      existingGrade.sectionId = uniqueSectionIds.join(", ")
      return
    }

    gradeMap.set(key, {
      gradeName: grade.gradeName,
      price: String(grade.price),
      colorCode: grade.colorCode,
      sectionId: String(grade.sectionId),
      ticketEffectId:
        grade.ticketEffectId !== undefined && grade.ticketEffectId !== null
          ? String(grade.ticketEffectId)
          : "",
    })
  })

  return Array.from(gradeMap.values())
}

export function buildInitialStakeholders(initialData?: HostShowDetail): Stakeholder[] {
  if (!initialData?.stakeholders?.length) {
    return [
      { ...FIXED_PLATFORM_STAKEHOLDER },
      { role: "organizer", name: "", businessNo: "", shareBps: "", verified: false },
      { role: "artist", name: "", phone: "", shareBps: "", verified: false },
    ]
  }

  const mappedStakeholders = initialData.stakeholders.map((stakeholder) => ({
    role: stakeholder.role,
    userId: stakeholder.userId,
    name: stakeholder.name ?? "",
    phone: stakeholder.role === "artist" ? stakeholder.number || "" : "",
    businessNo: stakeholder.role === "organizer" ? stakeholder.number || "" : "",
    shareBps: String(stakeholder.shareBps),
    verified: Boolean(stakeholder.userId),
    isFixed: stakeholder.name === FIXED_PLATFORM_STAKEHOLDER.name,
    ...(stakeholder.name === FIXED_PLATFORM_STAKEHOLDER.name
      ? {
          businessNo: FIXED_PLATFORM_STAKEHOLDER.businessNo,
          shareBps: FIXED_PLATFORM_STAKEHOLDER.shareBps,
          verified: true,
          isFixed: true,
        }
      : {}),
  }))

  const fixedStakeholderIndex = mappedStakeholders.findIndex(
    (stakeholder) =>
      stakeholder.name === FIXED_PLATFORM_STAKEHOLDER.name &&
      stakeholder.businessNo === FIXED_PLATFORM_STAKEHOLDER.businessNo
  )

  if (fixedStakeholderIndex < 0) {
    return [{ ...FIXED_PLATFORM_STAKEHOLDER }, ...mappedStakeholders]
  }

  const [fixedStakeholder] = mappedStakeholders.splice(fixedStakeholderIndex, 1)

  return [
    {
      ...fixedStakeholder,
      verified: true,
      isFixed: true,
      businessNo: FIXED_PLATFORM_STAKEHOLDER.businessNo,
      shareBps: FIXED_PLATFORM_STAKEHOLDER.shareBps,
    },
    ...mappedStakeholders,
  ]
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
    sessionStartDate: session.sessionStartDate ?? session.sessionStartTime ?? "",
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

  if (values.mode === "create" && !values.posterFile) {
    return "대표 포스터 파일을 등록해주세요."
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

  if (values.stakeholders.some((stakeholder) => !stakeholder.verified)) {
    return "모든 이해관계자의 조회(인증)가 완료되어야 합니다."
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

  const showStartDate = new Date(values.showStartAt).getTime()
  const showEndDate = new Date(values.showEndAt).getTime()

  if (
    values.sessionInfo.some((session) => {
      const sessionTimestamp = toSessionTimestamp(session.sessionDate, session.sessionStartDate)

      return sessionTimestamp < showStartDate || sessionTimestamp > showEndDate
    })
  ) {
    return "회차 일자가 전체 일정 범위에 포함되지 않습니다."
  }

  return null
}

export function buildPayload(values: Omit<ShowFormValues, "mode">): ShowFormPayload {
  const normalizedArtistName = values.artistName.trim()
  const sortedSessionInfo = [...values.sessionInfo].sort((left, right) => {
    const leftTimestamp = toSessionTimestamp(left.sessionDate, left.sessionStartDate)
    const rightTimestamp = toSessionTimestamp(right.sessionDate, right.sessionStartDate)

    return leftTimestamp - rightTimestamp
  })

  const payload: ShowFormPayload = {
    title: values.title.trim(),
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
      ...(grade.ticketEffectId ? { ticketEffectId: Number(grade.ticketEffectId) } : {}),
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
    sessionInfo: sortedSessionInfo.map((session, index) => ({
      sessionId: Number(session.sessionId) || index + 1,
      sessionDate: session.sessionDate,
      sessionStartDate: session.sessionStartDate,
    })),
  }

  if (normalizedArtistName) {
    payload.artistName = normalizedArtistName
  }

  return payload
}

export function buildCreatePayload(values: Omit<ShowFormValues, "mode">): CreateShowPayload {
  if (!values.posterFile) {
    throw new Error("대표 포스터 파일이 필요합니다.")
  }

  return {
    show: buildPayload(values),
    posterImageFile: values.posterFile,
  }
}

export function buildUpdatePayload(values: Omit<ShowFormValues, "mode">): UpdateShowPayload {
  const basePayload = buildPayload(values)

  return {
    title: basePayload.title,
    posterUrl: values.posterPreview ?? undefined,
    ...(basePayload.artistName ? { artistName: basePayload.artistName } : {}),
    venueId: basePayload.venueId,
    show: basePayload.show,
    reservation: basePayload.reservation,
    description: basePayload.description,
    purchaseLimit: basePayload.purchaseLimit,
    grade: basePayload.grade.flatMap((grade) =>
      grade.sectionId.map((sectionId) => ({
        sectionId,
        gradeName: grade.gradeName,
        price: grade.price,
        colorCode: grade.colorCode,
        ...(grade.ticketEffectId ? { ticketEffectId: grade.ticketEffectId } : {}),
      }))
    ),
    stakeholders: basePayload.stakeholders,
    refundPolicy: basePayload.refundPolicy,
    sessionInfo: basePayload.sessionInfo,
  }
}
