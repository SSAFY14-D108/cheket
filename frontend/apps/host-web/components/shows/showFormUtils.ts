import type { CreateShowPayload, HostShowDetail, ShowFormPayload } from "@/lib/show-manage-api"
import type { Grade, RefundItem, SessionItem, Stakeholder } from "./showFormTypes"

const FIXED_PLATFORM_STAKEHOLDER: Stakeholder = {
  role: "ORGANIZER",
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
  playtime: string
  posterPreview: string | null
  posterFile: File | null
  venueId: string
  showStartAt: string
  showEndAt: string
  openAt: string
  closeAt: string
  description: string
  descriptionImageFiles: File[]
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

export function toLocalDateTimeString(value: string) {
  const normalizedDate = value.includes("T") ? value.slice(0, 10) : value
  return `${normalizedDate}T00:00:00`
}

export function toFullLocalDateTimeString(dateValue: string, timeValue: string) {
  const normalizedDate = dateValue.includes("T") ? dateValue.slice(0, 10) : dateValue
  const normalizedTime = timeValue.includes("T")
    ? timeValue.slice(11, 19)
    : timeValue.length === 5
      ? `${timeValue}:00`
      : timeValue

  return `${normalizedDate}T${normalizedTime}`
}

function normalizeSessionDateValue(value?: string | null) {
  if (!value) {
    return ""
  }

  return value.includes("T") ? value.slice(0, 10) : value
}

function normalizeSessionTimeValue(value?: string | null) {
  if (!value) {
    return ""
  }

  return value.includes("T") ? value.slice(11, 16) : value.slice(0, 5)
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

function toSessionTimestamp(sessionDate: string, sessionStartTime: string) {
  return new Date(
    `${normalizeSessionDateValue(sessionDate)}T${normalizeSessionTimeValue(sessionStartTime)}`
  ).getTime()
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
      { role: "ORGANIZER", name: "", businessNo: "", shareBps: "", verified: false },
      { role: "ARTIST", name: "", phone: "", shareBps: "", verified: false },
    ]
  }

  const mappedStakeholders: Stakeholder[] = initialData.stakeholders.map((stakeholder) => {
    const role: Stakeholder["role"] = stakeholder.role === "organizer" ? "ORGANIZER" : "ARTIST"
    const businessNo = role === "ORGANIZER" ? stakeholder.number || "" : ""
    const phone = role === "ARTIST" ? stakeholder.number || "" : ""
    const isFixed =
      stakeholder.name === FIXED_PLATFORM_STAKEHOLDER.name &&
      businessNo === FIXED_PLATFORM_STAKEHOLDER.businessNo

    return {
      role,
      name: stakeholder.name ?? "",
      phone,
      businessNo,
      shareBps: String(stakeholder.shareBps),
      verified: Boolean(stakeholder.userId || stakeholder.name),
      isFixed,
      ...(isFixed
        ? {
            businessNo: FIXED_PLATFORM_STAKEHOLDER.businessNo,
            shareBps: FIXED_PLATFORM_STAKEHOLDER.shareBps,
            verified: true,
            isFixed: true,
          }
        : {}),
    }
  })

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
    return [{ sessionDate: "", sessionStartTime: "", capacity: "" }]
  }

  return initialData.sessionInfo.map((session) => ({
    sessionDate: normalizeSessionDateValue(session.sessionDate),
    sessionStartTime: normalizeSessionTimeValue(
      session.sessionStartTime ?? session.sessionStartDate ?? ""
    ),
    capacity: session.capacity,
  }))
}

export function buildValidationMessage(values: ShowFormValues) {
  if (!values.title.trim()) {
    return "공연명을 입력해주세요."
  }

  if (!values.artistName.trim()) {
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

  if (!Number.isInteger(Number(values.playtime)) || Number(values.playtime) <= 0) {
    return "공연 시간을 올바르게 입력해주세요."
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

  const distributableStakeholders = values.stakeholders.filter((stakeholder) => !stakeholder.isFixed)

  if (
    distributableStakeholders.length === 0 ||
    distributableStakeholders.some(
      (stakeholder) =>
        !Number.isFinite(Number(stakeholder.shareBps)) || Number(stakeholder.shareBps) <= 0
    )
  ) {
    return "수익 분배 비율을 올바르게 입력해주세요."
  }

  if (distributableStakeholders.some((stakeholder) => !stakeholder.verified)) {
    return "모든 이해관계자의 조회(인증)가 완료되어야 합니다."
  }

  const stakeholderShareSum = distributableStakeholders.reduce(
    (sum, stakeholder) => sum + Number(stakeholder.shareBps),
    0
  )

  if (stakeholderShareSum !== 9200) {
    return "수익 분배 비율의 합계는 9200bps여야 합니다."
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
    values.sessionInfo.some((session) => !session.sessionDate || !session.sessionStartTime)
  ) {
    return "회차 정보의 날짜와 시간을 모두 입력해주세요."
  }

  const showStartDate = new Date(values.showStartAt).getTime()
  const showEndDate = new Date(values.showEndAt).getTime()

  if (
    values.sessionInfo.some((session) => {
      const sessionTimestamp = toSessionTimestamp(session.sessionDate, session.sessionStartTime)
      return sessionTimestamp < showStartDate || sessionTimestamp > showEndDate
    })
  ) {
    return "회차 일자가 전체 일정 범위에 포함되지 않습니다."
  }

  return null
}

export function buildPayload(values: Omit<ShowFormValues, "mode">): ShowFormPayload {
  const sortedSessionInfo = [...values.sessionInfo].sort((left, right) => {
    const leftTimestamp = toSessionTimestamp(left.sessionDate, left.sessionStartTime)
    const rightTimestamp = toSessionTimestamp(right.sessionDate, right.sessionStartTime)
    return leftTimestamp - rightTimestamp
  })

  return {
    title: values.title.trim(),
    venueId: Number(values.venueId),
    artist: values.artistName.trim(),
    playtime: Number(values.playtime),
    showStartDate: toApiDateTimeValue(values.showStartAt),
    showEndDate: toApiDateTimeValue(values.showEndAt),
    reservationStartDate: toApiDateTimeValue(values.openAt),
    reservationEndDate: toApiDateTimeValue(values.closeAt),
    description: values.description.trim(),
    purchaseLimit: Number(values.purchaseLimit),
    grade: values.grades.map((grade) => ({
      gradeName: grade.gradeName.trim(),
      price: Number(grade.price),
      colorCode: grade.colorCode,
      sectionIds: parseSectionIds(grade.sectionId),
      ...(grade.ticketEffectId ? { ticketEffectId: Number(grade.ticketEffectId) } : {}),
    })),
    stakeholders: values.stakeholders
      .filter((stakeholder) => !stakeholder.isFixed)
      .map((stakeholder) => ({
        role: stakeholder.role,
        businessNo:
          stakeholder.role === "ORGANIZER" ? stakeholder.businessNo?.trim() || null : null,
        phoneNumber: stakeholder.role === "ARTIST" ? stakeholder.phone?.trim() || null : null,
        shareBps: Number(stakeholder.shareBps),
      })),
    refundPolicy: values.refundPolicy.map((item) => ({
      daysRemaining: Number(item.daysRemaining),
      refundRate: Number(item.refundRate),
    })),
    sessionInfo: sortedSessionInfo.map((session) => ({
      sessionDate: toLocalDateTimeString(session.sessionDate),
      sessionStartTime: toFullLocalDateTimeString(session.sessionDate, session.sessionStartTime),
    })),
  }
}

export function buildCreatePayload(values: Omit<ShowFormValues, "mode">): CreateShowPayload {
  if (!values.posterFile) {
    throw new Error("대표 포스터 파일이 필요합니다.")
  }

  return {
    show: buildPayload(values),
    posterImageFile: values.posterFile,
    descriptionImageFiles: values.descriptionImageFiles,
  }
}

export function buildUpdatePayload(values: Omit<ShowFormValues, "mode">): CreateShowPayload {
  return {
    show: buildPayload(values),
    posterImageFile: values.posterFile ?? null,
    descriptionImageFiles: values.descriptionImageFiles,
  }
}
