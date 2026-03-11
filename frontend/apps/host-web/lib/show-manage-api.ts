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
}

export interface HostShowStakeholder {
  role: "organizer" | "artist"
  userId: number
  shareBps: number
  name?: string
}

export interface HostShowRefundPolicy {
  daysRemaining: number
  refundRate: number
}

export interface HostShowSessionInfo {
  sessionId: number
  sessionDate: string
  sessionStartDate: string
  capacity: number
}

export interface HostShowDetail {
  showId: number
  title: string
  posterUrl: string
  artistName?: string
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

export interface HostShowSection {
  sectionId: number
  sectionName: string
}

export interface HostShowTicketEffect {
  id: number
  effect: string
}

export interface ShowFormPayload {
  title: string
  posterUrl: string
  venueId: number
  artistName?: string
  show: {
    startAt: string
    endAt: string
  }
  reservation: {
    openAt: string
    closeAt: string
  }
  description: string
  purchaseLimit: number
  grade: Array<{
    sectionId: number[]
    gradeName: string
    price: number
    colorCode: string
    ticketEffectId?: number
  }>
  stakeholders: Array<{
    role: "organizer" | "artist"
    userId?: number
    shareBps: number
  }>
  refundPolicy: Array<{
    daysRemaining: number
    refundRate: number
  }>
  sessionInfo: Array<{
    sessionId: number
    sessionDate: string
    sessionStartDate: string
  }>
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

function buildCreateShowPath() {
  return "/api/v1/hosts/shows"
}

function buildShowSectionsPath(venueId: string | number) {
  // 공연장 구역 목록 조회
  return `/api/v1/hosts/shows/${venueId}/sections`
}

export async function fetchShowDetail(showId: string | number) {
  const response = await apiFetch<ApiResponse<HostShowDetail>>(buildShowDetailPath(showId), {
    method: "GET",
  })

  return response.data
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

export async function createShow(payload: ShowFormPayload) {
  const response = await apiFetch<ApiResponse<{ showId: number }>>(buildCreateShowPath(), {
    method: "POST",
    body: JSON.stringify(payload),
  })

  return response.data
}

export async function updateShow(showId: string | number, payload: ShowFormPayload) {
  const response = await apiFetch<ApiMessageResponse>(buildShowMutationPath(showId), {
    method: "PUT",
    body: JSON.stringify(payload),
  })

  return response
}

export async function deleteShow(showId: string | number) {
  const response = await apiFetch<ApiMessageResponse>(buildShowMutationPath(showId), {
    method: "DELETE",
  })

  return response
}
