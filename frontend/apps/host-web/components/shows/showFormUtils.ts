import type {
  CreateShowPayload,
  HostShowDetail,
  ShowFormPayload,
  UpdateShowPayload,
  HostShowStakeholder,
} from "@/lib/show-manage-api"
import type { Grade, RefundItem, SessionItem, Stakeholder } from "./showFormTypes"

export const PLATFORM_FEE_BPS = 800
export const PLATFORM_TOTAL_BPS = 10000

export const FIXED_PLATFORM_STAKEHOLDER: Stakeholder = {
  role: "ORGANIZER",
  name: "CHEKET",
  businessNo: "000-00-00000",
  shareBps: String(PLATFORM_FEE_BPS),
  verified: true,
  isFixed: true,
}

export interface ShowFormValues {
  mode: "create" | "edit"
  contentOnlyEdit?: boolean
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

export function isContentOnlyEditableStatus(status?: string | null) {
  return Boolean(status) && status !== "PENDING_CONTRACT"
}

export function getReservationStartMinDate(baseDate = new Date()) {
  const minDate = new Date(baseDate)
  minDate.setHours(0, 0, 0, 0)
  minDate.setDate(minDate.getDate() + 2)
  return minDate
}

export function parseSectionIds(rawValue: string) {
  return rawValue
    .split(",")
    .map((value) => Number(value.trim()))
    .filter((value) => Number.isInteger(value))
}

function toApiDateTimeValue(value: string) {
  if (!value) {
    return value
  }

  const withSeconds = value.length === 16 ? `${value}:00` : value
  return withSeconds.replace(/([zZ]|[+-]\d{2}:\d{2})$/, "")
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

function isSessionChanged(
  session: SessionItem,
  initialSession?: SessionItem
) {
  if (!initialSession) {
    return true
  }

  return (
    session.sessionDate !== initialSession.sessionDate ||
    session.sessionStartTime !== initialSession.sessionStartTime
  )
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

function isPlatformStakeholderRecord(stakeholder: HostShowStakeholder) {
  const stakeholderNumber = stakeholder.number?.trim() ?? ""
  const stakeholderName = stakeholder.name?.trim() ?? ""

  return (
    stakeholder.role === "ORGANIZER" &&
    stakeholder.shareBps === PLATFORM_FEE_BPS &&
    (
      (
        stakeholderName === FIXED_PLATFORM_STAKEHOLDER.name &&
        stakeholderNumber === FIXED_PLATFORM_STAKEHOLDER.businessNo
      ) ||
      (!stakeholder.id && !stakeholderName && !stakeholderNumber)
    )
  )
}

function isSelfHostStakeholderRecord(
  stakeholder: HostShowStakeholder,
  currentBusinessNo?: string | null
) {
  const stakeholderNumber = stakeholder.number?.replace(/\D/g, "") ?? ""
  const normalizedBusinessNo = currentBusinessNo?.replace(/\D/g, "") ?? ""

  return Boolean(
    normalizedBusinessNo &&
    stakeholder.role === "ORGANIZER" &&
    stakeholderNumber === normalizedBusinessNo
  )
}

function toSessionTimestamp(sessionDate: string, sessionStartTime: string) {
  return new Date(
    `${normalizeSessionDateValue(sessionDate)}T${normalizeSessionTimeValue(sessionStartTime)}`
  ).getTime()
}

function formatLocalDateTime(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, "0")
  const day = String(date.getDate()).padStart(2, "0")
  const hours = String(date.getHours()).padStart(2, "0")
  const minutes = String(date.getMinutes()).padStart(2, "0")
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

export function deriveShowRangeFromSessions(sessionInfo: SessionItem[], playtime: string) {
  const validSessions = sessionInfo
    .filter((session) => session.sessionDate && session.sessionStartTime)
    .map((session) => new Date(`${session.sessionDate}T${session.sessionStartTime}`))
    .filter((date) => !Number.isNaN(date.getTime()))

  if (validSessions.length === 0) {
    return { showStartAt: "", showEndAt: "" }
  }

  const sortedSessions = [...validSessions].sort((left, right) => left.getTime() - right.getTime())
  const startDate = sortedSessions[0]
  const lastSessionStart = sortedSessions[sortedSessions.length - 1]
  const durationMinutes = Number.parseInt(playtime, 10)
  const endDate = new Date(lastSessionStart)

  if (Number.isFinite(durationMinutes) && durationMinutes > 0) {
    endDate.setMinutes(endDate.getMinutes() + durationMinutes)
  }

  return {
    showStartAt: formatLocalDateTime(startDate),
    showEndAt: formatLocalDateTime(endDate),
  }
}

export function deriveReservationEndFromSessions(sessionInfo: SessionItem[]) {
  const validSessions = sessionInfo
    .filter((session) => session.sessionDate && session.sessionStartTime)
    .map((session) => new Date(`${session.sessionDate}T${session.sessionStartTime}`))
    .filter((date) => !Number.isNaN(date.getTime()))

  if (validSessions.length === 0) {
    return ""
  }

  const lastSessionStart = [...validSessions].sort((left, right) => left.getTime() - right.getTime())[validSessions.length - 1]
  const reservationEnd = new Date(lastSessionStart)
  reservationEnd.setHours(reservationEnd.getHours() - 1)

  return formatLocalDateTime(reservationEnd)
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

export function buildInitialStakeholders(
  initialData?: HostShowDetail,
  currentBusinessNo?: string | null
): Stakeholder[] {
  if (!initialData?.stakeholders?.length) {
    return [
      { ...FIXED_PLATFORM_STAKEHOLDER },
      { role: "ORGANIZER", name: "", businessNo: "", shareBps: "", verified: false },
      { role: "ARTIST", name: "", phone: "", shareBps: "", verified: false },
    ]
  }

  const mappedStakeholders: Stakeholder[] = initialData.stakeholders.map((stakeholder) => {
    const role: Stakeholder["role"] = stakeholder.role === "ORGANIZER" ? "ORGANIZER" : "ARTIST"
    const businessNo = role === "ORGANIZER" ? stakeholder.number || "" : ""
    const phone = role === "ARTIST" ? stakeholder.number || "" : ""
    const isFixed = isPlatformStakeholderRecord(stakeholder)
    const isSelfHost = isSelfHostStakeholderRecord(stakeholder, currentBusinessNo)

    return {
      id: stakeholder.id,
      role,
      name: isFixed ? FIXED_PLATFORM_STAKEHOLDER.name : stakeholder.name ?? "",
      phone,
      businessNo,
      shareBps: String(stakeholder.shareBps),
      verified: Boolean(stakeholder.id || stakeholder.name),
      isFixed,
      isSelfHost,
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

  const fixedStakeholderIndex = mappedStakeholders.findIndex((stakeholder) => stakeholder.isFixed)

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
      { daysRemaining: "10", refundRate: "0" },
      { daysRemaining: "7", refundRate: "10" },
      { daysRemaining: "3", refundRate: "20" },
      { daysRemaining: "1", refundRate: "30" },
    ]
  }

  return initialData.refundPolicy.map((policy) => ({
    daysRemaining: String(policy.daysRemaining),
    refundRate: String(Math.max(0, 100 - Number(policy.refundRate))),
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

export function buildValidationMessage(
  values: ShowFormValues,
  initialData?: HostShowDetail
) {
  if (!values.title.trim()) {
    return "공연명을 입력해주세요."
  }

  if (!values.contentOnlyEdit && !values.artistName.trim()) {
    return "아티스트 또는 그룹명을 입력해주세요."
  }

  if (!values.posterPreview) {
    return "대표 포스터를 등록해주세요."
  }

  if (values.mode === "create" && !values.posterFile) {
    return "대표 포스터 파일을 등록해주세요."
  }

  if (values.contentOnlyEdit) {
    return !values.description.trim() ? "怨듭뿰 ?ㅻ챸???낅젰?댁＜?몄슂." : null
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

  const reservationOpenAt = new Date(values.openAt)
  const reservationCloseAt = new Date(values.closeAt)
  const reservationMinDate = getReservationStartMinDate()

  if (
    values.mode === "create" &&
    !Number.isNaN(reservationOpenAt.getTime()) &&
    reservationOpenAt < reservationMinDate
  ) {
    return "예매 가능 시작일은 현재 날짜 기준 2일 뒤부터 설정할 수 있습니다."
  }

  if (
    !Number.isNaN(reservationOpenAt.getTime()) &&
    !Number.isNaN(reservationCloseAt.getTime()) &&
    reservationOpenAt >= reservationCloseAt
  ) {
    return "예매 시작일은 자동 계산된 예매 마감일보다 빨라야 합니다."
  }

  const firstSessionAt = new Date(values.showStartAt)

  if (
    !Number.isNaN(reservationOpenAt.getTime()) &&
    !Number.isNaN(firstSessionAt.getTime()) &&
    reservationOpenAt > firstSessionAt
  ) {
    return "예매 시작일은 첫 번째 회차 시작일보다 늦을 수 없습니다."
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

  if (values.mode === "create") {
    const distributableStakeholders = values.stakeholders.filter(
      (stakeholder) => !stakeholder.isFixed
    )
    const totalShareBps = values.stakeholders.reduce(
      (sum, stakeholder) => sum + (Number(stakeholder.shareBps) || 0),
      0
    )

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

    if (totalShareBps !== PLATFORM_TOTAL_BPS) {
      return `정산 비율의 총합은 ${PLATFORM_TOTAL_BPS.toLocaleString()}bps여야 합니다. (플랫폼 ${PLATFORM_FEE_BPS.toLocaleString()}bps 포함)`
    }
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
  const sessionMinDate = getReservationStartMinDate().getTime()
  const initialSessionInfo = buildInitialSessionInfo(initialData)

  if (
    values.sessionInfo.some((session, index) => {
      const sessionTimestamp = toSessionTimestamp(session.sessionDate, session.sessionStartTime)

      if (Number.isNaN(sessionTimestamp)) {
        return false
      }

      if (values.mode === "create") {
        return sessionTimestamp < sessionMinDate
      }

      return isSessionChanged(session, initialSessionInfo[index]) && sessionTimestamp < sessionMinDate
    })
  ) {
    return "회차는 현재 기준 2일 이후 일정부터 등록할 수 있습니다."
  }

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
      refundRate: Math.max(0, 100 - Number(item.refundRate)),
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

function areStringArraysEqual(left: string[], right: string[]) {
  if (left.length !== right.length) {
    return false
  }

  return left.every((value, index) => value === right[index])
}

function areNumberArraysEqual(left: number[], right: number[]) {
  if (left.length !== right.length) {
    return false
  }

  return left.every((value, index) => value === right[index])
}

function mapUpdateStakeholders(values: Stakeholder[]) {
  return values
    .filter((stakeholder) => !stakeholder.isFixed)
    .map((stakeholder) => ({
      role: stakeholder.role,
      id: stakeholder.id ?? 0,
      name: stakeholder.name.trim(),
      number:
        stakeholder.role === "ORGANIZER"
          ? stakeholder.businessNo?.trim() || ""
          : stakeholder.phone?.trim() || "",
      shareBps: Number(stakeholder.shareBps),
    }))
}

function mapInitialUpdateStakeholders(initialData?: HostShowDetail) {
  return (initialData?.stakeholders ?? [])
    .filter(
      (stakeholder) =>
        !(
          stakeholder.name === FIXED_PLATFORM_STAKEHOLDER.name &&
          stakeholder.number === FIXED_PLATFORM_STAKEHOLDER.businessNo
        )
    )
    .map((stakeholder) => ({
      role: stakeholder.role,
      id: stakeholder.id ?? 0,
      name: stakeholder.name ?? "",
      number: stakeholder.number ?? "",
      shareBps: stakeholder.shareBps,
    }))
}

export function buildUpdatePayload(
  values: Omit<ShowFormValues, "mode">,
  currentPreviews: string[] = [],
  initialData?: HostShowDetail,
  options?: {
    contentOnlyEdit?: boolean
  }
): UpdateShowPayload {
  const retainedUrls = currentPreviews.filter((url) => url.startsWith("http"))
  const contentOnlyEdit = Boolean(options?.contentOnlyEdit)
  const showPayload: Record<string, unknown> = {}
  const initialRetainedUrls = initialData?.descriptionImages ?? []
  const trimmedTitle = values.title.trim()
  const trimmedDescription = values.description.trim()

  if (trimmedTitle !== (initialData?.title ?? "")) {
    showPayload.title = trimmedTitle
  }

  if (trimmedDescription !== (initialData?.description ?? "")) {
    showPayload.description = trimmedDescription
  }

  if (!areStringArraysEqual(retainedUrls, initialRetainedUrls)) {
    showPayload.existingDescriptionImageUrls = retainedUrls
  }

  if (!contentOnlyEdit) {
    const fullPayload = buildPayload(values)

    if (Number(values.venueId) !== (initialData?.venue.venueId ?? 0)) {
      showPayload.venueId = fullPayload.venueId
    }

    if (values.artistName.trim() !== (initialData?.artistName ?? "")) {
      showPayload.artist = fullPayload.artist
    }

    if (Number(values.playtime) !== (initialData?.playtime ?? 0)) {
      showPayload.playtime = fullPayload.playtime
    }

    if (fullPayload.showStartDate !== (initialData?.show.showStartDate ?? "")) {
      showPayload.showStartDate = fullPayload.showStartDate
    }

    if (fullPayload.showEndDate !== (initialData?.show.showEndDate ?? "")) {
      showPayload.showEndDate = fullPayload.showEndDate
    }

    if (fullPayload.reservationStartDate !== (initialData?.reservation.startDate ?? "")) {
      showPayload.reservationStartDate = fullPayload.reservationStartDate
    }

    if (fullPayload.reservationEndDate !== (initialData?.reservation.endDate ?? "")) {
      showPayload.reservationEndDate = fullPayload.reservationEndDate
    }

    if (Number(values.purchaseLimit) !== (initialData?.purchaseLimit ?? 0)) {
      showPayload.purchaseLimit = fullPayload.purchaseLimit
    }

    const initialGrades = buildInitialGrades(initialData)
    const currentGrades = values.grades.map((grade) => ({
      gradeName: grade.gradeName.trim(),
      price: String(Number(grade.price)),
      colorCode: grade.colorCode,
      sectionIds: parseSectionIds(grade.sectionId),
      ticketEffectId: grade.ticketEffectId || "",
    }))
    const comparableInitialGrades = initialGrades.map((grade) => ({
      gradeName: grade.gradeName.trim(),
      price: String(Number(grade.price)),
      colorCode: grade.colorCode,
      sectionIds: parseSectionIds(grade.sectionId),
      ticketEffectId: grade.ticketEffectId || "",
    }))

    const gradesChanged =
      currentGrades.length !== comparableInitialGrades.length ||
      currentGrades.some((grade, index) => {
        const initialGrade = comparableInitialGrades[index]
        return (
          !initialGrade ||
          grade.gradeName !== initialGrade.gradeName ||
          grade.price !== initialGrade.price ||
          grade.colorCode !== initialGrade.colorCode ||
          grade.ticketEffectId !== initialGrade.ticketEffectId ||
          !areNumberArraysEqual(grade.sectionIds, initialGrade.sectionIds)
        )
      })

    if (gradesChanged) {
      showPayload.grade = fullPayload.grade
    }

    const initialRefundPolicy = buildInitialRefundPolicy(initialData)
    const refundChanged =
      values.refundPolicy.length !== initialRefundPolicy.length ||
      values.refundPolicy.some((item, index) => {
        const initialItem = initialRefundPolicy[index]
        return (
          !initialItem ||
          item.daysRemaining !== initialItem.daysRemaining ||
          item.refundRate !== initialItem.refundRate
        )
      })

    if (refundChanged) {
      showPayload.refundPolicy = fullPayload.refundPolicy
    }

    const initialSessionInfo = buildInitialSessionInfo(initialData)
    const sessionsChanged =
      values.sessionInfo.length !== initialSessionInfo.length ||
      values.sessionInfo.some((session, index) => {
        const initialSession = initialSessionInfo[index]
        return (
          !initialSession ||
          session.sessionDate !== initialSession.sessionDate ||
          session.sessionStartTime !== initialSession.sessionStartTime
        )
      })

    if (sessionsChanged) {
      showPayload.sessionInfo = fullPayload.sessionInfo
    }

    const currentStakeholders = mapUpdateStakeholders(values.stakeholders)
    const initialStakeholders = mapInitialUpdateStakeholders(initialData)
    const stakeholdersChanged =
      currentStakeholders.length !== initialStakeholders.length ||
      currentStakeholders.some((stakeholder, index) => {
        const initialStakeholder = initialStakeholders[index]
        return (
          !initialStakeholder ||
          stakeholder.role !== initialStakeholder.role ||
          stakeholder.id !== initialStakeholder.id ||
          stakeholder.name !== initialStakeholder.name ||
          stakeholder.number !== initialStakeholder.number ||
          stakeholder.shareBps !== initialStakeholder.shareBps
        )
      })

    if (stakeholdersChanged) {
      showPayload.stakeholders = currentStakeholders
    }
  }

  return {
    show: {
      ...showPayload,
    },
    posterImageFile: values.posterFile ?? null,
    descriptionImageFiles:
      values.descriptionImageFiles.length > 0 ? values.descriptionImageFiles : undefined,
  }
}
