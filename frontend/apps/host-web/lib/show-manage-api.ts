import { apiFetch } from "@/lib/api"

interface ApiResponse<T> {
  httpStatusCode: number
  responseMessage: string
  data: T
}

interface ApiMessageResponse {
  httpStatusCode: number
  responseMessage: string
}

export interface HostShowVenueInfo {
  venueId: number
  name: string
  address: string
}

export interface HostShowPeriod {
  showStartDate: string
  showEndDate: string
}

export interface HostReservationPeriod {
  startDate: string
  endDate: string
}

export interface HostShowGrade {
  sectionId: number
  gradeName: string
  price: number
  colorCode: string
  ticketEffectId?: number | null
  ticketEffect?: string | null
}

export interface HostShowStakeholder {
  role: "organizer" | "artist"
  userId?: number
  shareBps: number
  name?: string
  number?: string
}

export interface HostShowRefundPolicy {
  daysRemaining: number
  refundRate: number
}

export interface HostShowSessionInfo {
  sessionId: number
  sessionDate: string
  sessionStartDate?: string
  sessionStartTime?: string
  capacity: number
}

export interface HostShowDetail {
  showId: number
  title: string
  posterUrl: string
  artistName?: string
  playtime?: number
  venue: HostShowVenueInfo
  show: HostShowPeriod
  reservation: HostReservationPeriod
  description: string
  purchaseLimit: number
  likeCount: number
  grade: HostShowGrade[]
  stakeholders: HostShowStakeholder[]
  refundPolicy: HostShowRefundPolicy[]
  sessionInfo: HostShowSessionInfo[]
  status: string
  createdAt: string
  updatedAt: string
}

interface RawHostShowGrade {
  sectionId?: unknown
  gradeName?: unknown
  price?: unknown
  colorCode?: unknown
  ticketEffectId?: unknown
  ticketEffect?: unknown
}

interface RawHostShowStakeholder {
  role?: unknown
  userId?: unknown
  businessNo?: unknown
  phoneNumber?: unknown
  shareBps?: unknown
  name?: unknown
  number?: unknown
}

interface RawHostShowSessionInfo {
  sessionId?: unknown
  sessionDate?: unknown
  sessionStartDate?: unknown
  sessionStartTime?: unknown
  capacity?: unknown
}

interface RawHostShowDetail {
  showId?: unknown
  title?: unknown
  posterUrl?: unknown
  artist?: unknown
  venue?: {
    venueId?: unknown
    name?: unknown
    address?: unknown
  }
  show?: {
    showStartDate?: unknown
    showEndDate?: unknown
  }
  reservation?: {
    startDate?: unknown
    endDate?: unknown
  }
  description?: unknown
  playtime?: unknown
  purchaseLimit?: unknown
  likeCount?: unknown
  grade?: RawHostShowGrade[]
  stakeholders?: RawHostShowStakeholder[]
  refundPolicy?: Array<{
    daysRemaining?: unknown
    refundRate?: unknown
  }>
  sessionInfo?: RawHostShowSessionInfo[]
  status?: unknown
  createdAt?: unknown
  updatedAt?: unknown
}

function toSafeNumber(value: unknown, fallback = 0) {
  return typeof value === "number" && Number.isFinite(value)
    ? value
    : typeof value === "string" && value.trim() && Number.isFinite(Number(value))
      ? Number(value)
      : fallback
}

function toSafeString(value: unknown, fallback = "") {
  return typeof value === "string" ? value : fallback
}

function toOptionalSafeNumber(value: unknown) {
  return typeof value === "number" && Number.isFinite(value)
    ? value
    : typeof value === "string" && value.trim() && Number.isFinite(Number(value))
      ? Number(value)
      : undefined
}

function normalizeSessionDate(value: unknown) {
  const safeValue = toSafeString(value)

  if (!safeValue) {
    return ""
  }

  return safeValue.includes("T") ? safeValue.slice(0, 10) : safeValue
}

function normalizeSessionStartLabel(value: unknown) {
  const safeValue = toSafeString(value)

  if (!safeValue) {
    return ""
  }

  return safeValue.includes("T") ? safeValue.slice(11, 16) : safeValue.slice(0, 5)
}

function normalizeShowDetail(raw: RawHostShowDetail): HostShowDetail {
  return {
    showId: toSafeNumber(raw.showId),
    title: toSafeString(raw.title),
    posterUrl: toSafeString(raw.posterUrl),
    artistName: toSafeString(raw.artist),
    playtime: toOptionalSafeNumber(raw.playtime),
    venue: {
      venueId: toSafeNumber(raw.venue?.venueId),
      name: toSafeString(raw.venue?.name),
      address: toSafeString(raw.venue?.address),
    },
    show: {
      showStartDate: toSafeString(raw.show?.showStartDate),
      showEndDate: toSafeString(raw.show?.showEndDate),
    },
    reservation: {
      startDate: toSafeString(raw.reservation?.startDate),
      endDate: toSafeString(raw.reservation?.endDate),
    },
    description: toSafeString(raw.description),
    purchaseLimit: toSafeNumber(raw.purchaseLimit),
    likeCount: toSafeNumber(raw.likeCount),
    grade: Array.isArray(raw.grade)
      ? raw.grade.map((grade) => ({
          sectionId: toSafeNumber(grade.sectionId),
          gradeName: toSafeString(grade.gradeName),
          price: toSafeNumber(grade.price),
          colorCode: toSafeString(grade.colorCode),
          ticketEffectId:
            grade.ticketEffectId === undefined || grade.ticketEffectId === null
              ? null
              : toSafeNumber(grade.ticketEffectId),
          ticketEffect: toSafeString(grade.ticketEffect, ""),
        }))
      : [],
    stakeholders: Array.isArray(raw.stakeholders)
      ? raw.stakeholders.map((stakeholder) => ({
          role:
            stakeholder.role === "organizer" || stakeholder.role === "ORGANIZER"
              ? "organizer"
              : "artist",
          userId: toOptionalSafeNumber(stakeholder.userId),
          shareBps: toSafeNumber(stakeholder.shareBps),
          name: toSafeString(stakeholder.name, ""),
          number: toSafeString(
            stakeholder.number ?? stakeholder.businessNo ?? stakeholder.phoneNumber,
            ""
          ),
        }))
      : [],
    refundPolicy: Array.isArray(raw.refundPolicy)
      ? raw.refundPolicy.map((policy) => ({
          daysRemaining: toSafeNumber(policy.daysRemaining),
          refundRate: toSafeNumber(policy.refundRate),
        }))
      : [],
    sessionInfo: Array.isArray(raw.sessionInfo)
      ? raw.sessionInfo.map((session) => ({
          sessionId: toSafeNumber(session.sessionId),
          sessionDate: normalizeSessionDate(session.sessionDate),
          sessionStartDate: normalizeSessionStartLabel(
            session.sessionStartDate ?? session.sessionStartTime
          ),
          sessionStartTime: toSafeString(session.sessionStartTime, ""),
          capacity: toSafeNumber(session.capacity),
        }))
      : [],
    status: toSafeString(raw.status),
    createdAt: toSafeString(raw.createdAt),
    updatedAt: toSafeString(raw.updatedAt),
  }
}

export interface HostShowSection {
  sectionId: number
  sectionName: string
}

export interface HostShowTicketEffect {
  id: number
  effect: string
}

export interface HostShowVenueOption {
  venueId: number
  name: string
  capacity: number
}

export interface ShowFormPayload {
  title: string
  venueId: number
  artist: string
  playtime: number
  showStartDate: string
  showEndDate: string
  reservationStartDate: string
  reservationEndDate: string
  description: string
  purchaseLimit: number
  grade: Array<{
    sectionIds: number[]
    gradeName: string
    price: number
    colorCode: string
    ticketEffectId?: number
  }>
  stakeholders: Array<{
    role: "ORGANIZER" | "ARTIST"
    businessNo: string | null
    phoneNumber: string | null
    shareBps: number
  }>
  refundPolicy: Array<{
    daysRemaining: number
    refundRate: number
  }>
  sessionInfo: Array<{
    sessionDate: string
    sessionStartTime: string
  }>
}

export interface CreateShowPayload {
  show: ShowFormPayload
  posterImageFile?: File | null
  descriptionImageFiles?: File[]
}

function buildShowDetailPath(showId: string | number) {
  return `/api/v1/hosts/shows/${showId}`
}

function buildShowMutationPath(showId: string | number) {
  return `/api/v1/hosts/shows/${showId}`
}

function buildTicketEffectsPath() {
  return "/api/v1/hosts/shows/effect"
}

function buildShowVenuesPath() {
  return "/api/v1/shows/venue"
}

function buildCreateShowPath() {
  return "/api/v1/hosts/shows"
}

function buildShowSectionsPath(venueId: string | number) {
  // 공연장 구역 목록 조회
  return `/api/v1/hosts/venues/${venueId}/sections`
}

export async function fetchShowDetail(showId: string | number) {
  const response = await apiFetch<ApiResponse<RawHostShowDetail>>(buildShowDetailPath(showId), {
    method: "GET",
  })

  return normalizeShowDetail(response.data)
}

export async function fetchShowSections(venueId: string | number) {
  const response = await apiFetch<ApiResponse<HostShowSection[]>>(buildShowSectionsPath(venueId), {
    method: "GET",
  })

  return response.data
}

export async function fetchTicketEffects() {
  const response = await apiFetch<ApiResponse<HostShowTicketEffect[]>>(buildTicketEffectsPath(), {
    method: "GET",
  })

  return response.data
}

export async function fetchShowVenues() {
  const response = await apiFetch<ApiResponse<HostShowVenueOption[]>>(buildShowVenuesPath(), {
    method: "GET",
  })

  return response.data
}

export async function createShow(payload: CreateShowPayload) {
  if (!payload.posterImageFile) {
    throw new Error("대표 포스터 파일이 필요합니다.")
  }

  const formData = new FormData()

  formData.append(
    "show",
    new Blob([JSON.stringify(payload.show)], {
      type: "application/json",
    })
  )
  formData.append("posterImage", payload.posterImageFile)
  payload.descriptionImageFiles?.forEach((imageFile) => {
    formData.append("descriptionImages", imageFile)
  })

  const response = await apiFetch<ApiResponse<number>>(buildCreateShowPath(), {
    method: "POST",
    body: formData,
  })

  return response.data
}

export async function updateShow(showId: string | number, payload: CreateShowPayload) {
  const formData = new FormData()

  formData.append(
    "show",
    new Blob([JSON.stringify(payload.show)], {
      type: "application/json",
    })
  )

  if (payload.posterImageFile) {
    formData.append("posterImage", payload.posterImageFile)
  }

  payload.descriptionImageFiles?.forEach((imageFile) => {
    formData.append("descriptionImages", imageFile)
  })

  const response = await apiFetch<ApiMessageResponse>(buildShowMutationPath(showId), {
    method: "PUT",
    body: formData,
  })

  return response
}

export async function deleteShow(showId: string | number) {
  const response = await apiFetch<ApiMessageResponse>(buildShowMutationPath(showId), {
    method: "DELETE",
  })

  return response
}
