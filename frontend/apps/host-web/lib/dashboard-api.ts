import { ApiError, apiFetch } from "@/lib/api"
import { fetchMyWalletBalance } from "@/lib/mypage-api"
import type {
  DashboardBookingRate,
  DashboardData,
  DashboardReservations,
  DashboardRevenueSplit,
  DashboardTotalSales,
  WalletBalance,
} from "@/lib/dashboard-types"

interface ApiResponse<T> {
  httpStatusCode: number
  responseMessage: string
  data: T
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

function normalizeReservations(data: Partial<DashboardReservations> | null | undefined): DashboardReservations {
  const sessions = Array.isArray(data?.sessions)
    ? data.sessions.map((session) => ({
        sessionId: toSafeNumber(session.sessionId),
        date: toSafeString(session.date),
        capacity: toSafeNumber(session.capacity),
        reservedSeats: toSafeNumber(session.reservedSeats),
      }))
    : []

  return {
    showId: toSafeNumber(data?.showId),
    title: toSafeString(data?.title, "공연"),
    venue: toSafeString(data?.venue, "-"),
    sessions,
  }
}

function normalizeRevenueSplit(
  data: Partial<DashboardRevenueSplit> | null | undefined
): DashboardRevenueSplit {
  const splits = Array.isArray(data?.splits)
    ? data.splits.map((split) => ({
        role: toSafeString(split.role, "미분류"),
        rateBps: toSafeNumber(split.rateBps),
        amount: toSafeNumber(split.amount),
      }))
    : []

  return {
    showId: toSafeNumber(data?.showId),
    title: toSafeString(data?.title, "공연"),
    totalRevenue: toSafeNumber(data?.totalRevenue),
    splits,
  }
}

function normalizeBookingRate(
  data: Partial<DashboardBookingRate> | null | undefined
): DashboardBookingRate {
  return {
    showId: toSafeNumber(data?.showId),
    title: toSafeString(data?.title, "공연"),
    capacity: toSafeNumber(data?.capacity),
    reservedSeats: toSafeNumber(data?.reservedSeats),
    bookingRate: toSafeNumber(data?.bookingRate),
  }
}

function normalizeTotalSales(
  data: Partial<DashboardTotalSales> | null | undefined
): DashboardTotalSales {
  return {
    showId: toSafeNumber(data?.showId),
    title: toSafeString(data?.title, "공연"),
    totalPrimarySales: toSafeNumber(data?.totalPrimarySales),
  }
}

function normalizeWalletBalance(wallet: Partial<WalletBalance> | null): WalletBalance | null {
  if (!wallet) {
    return null
  }

  return {
    balance: toSafeNumber(wallet.balance),
    walletAddress: toSafeString(wallet.walletAddress, "-"),
  }
}

function buildDashboardPath(showId: string | number, suffix: string) {
  return `/api/v1/hosts/shows/${showId}/dashboard/${suffix}`
}

function resolveSettledValue<T>(
  result: PromiseSettledResult<T>,
  label: string,
  partialErrors: string[]
) {
  if (result.status === "fulfilled") {
    return result.value
  }

  if (result.reason instanceof ApiError && result.reason.status === 401) {
    throw result.reason
  }

  const message =
    result.reason instanceof ApiError
      ? result.reason.message
      : `${label} 데이터를 불러오는데 실패했습니다.`

  partialErrors.push(`${label}: ${message}`)
  return null
}

export async function fetchDashboardReservations(showId: string | number) {
  const response = await apiFetch<ApiResponse<DashboardReservations>>(
    buildDashboardPath(showId, "reservations"),
    { method: "GET" }
  )

  return response.data
}

export async function fetchDashboardRevenueSplit(showId: string | number) {
  const response = await apiFetch<ApiResponse<DashboardRevenueSplit>>(
    buildDashboardPath(showId, "revenue-split"),
    { method: "GET" }
  )

  return response.data
}

export async function fetchDashboardBookingRate(showId: string | number) {
  const response = await apiFetch<ApiResponse<DashboardBookingRate>>(
    buildDashboardPath(showId, "booking-rate"),
    { method: "GET" }
  )

  return response.data
}

export async function fetchDashboardTotalSales(showId: string | number) {
  const response = await apiFetch<ApiResponse<DashboardTotalSales>>(
    buildDashboardPath(showId, "total-sales"),
    { method: "GET" }
  )

  return response.data
}

async function fetchDashboardWalletBalance(): Promise<WalletBalance | null> {
  try {
    return await fetchMyWalletBalance()
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      throw error
    }

    return null
  }
}

export async function fetchDashboardData(showId: string | number): Promise<DashboardData> {
  const partialErrors: string[] = []
  const [reservationsResult, revenueSplitResult, bookingRateResult, totalSalesResult, walletResult] =
    await Promise.allSettled([
    fetchDashboardReservations(showId),
    fetchDashboardRevenueSplit(showId),
    fetchDashboardBookingRate(showId),
    fetchDashboardTotalSales(showId),
    fetchDashboardWalletBalance(),
  ])

  const reservations = resolveSettledValue(
    reservationsResult,
    "회차별 예매 현황",
    partialErrors
  )
  const revenueSplit = resolveSettledValue(revenueSplitResult, "수익 배분", partialErrors)
  const bookingRate = resolveSettledValue(bookingRateResult, "예매율", partialErrors)
  const totalSales = resolveSettledValue(totalSalesResult, "총 판매금액", partialErrors)
  const wallet = resolveSettledValue(walletResult, "지갑 잔액", partialErrors)

  const derivedTitle =
    reservations?.title ??
    revenueSplit?.title ??
    bookingRate?.title ??
    totalSales?.title ??
    "공연"

  const normalizedReservations = normalizeReservations(reservations)
  const normalizedRevenueSplit = normalizeRevenueSplit(revenueSplit)
  const normalizedBookingRate = normalizeBookingRate(bookingRate)
  const normalizedTotalSales = normalizeTotalSales(totalSales)

  return {
    totalSales: { ...normalizedTotalSales, title: normalizedTotalSales.title || derivedTitle },
    bookingRate: { ...normalizedBookingRate, title: normalizedBookingRate.title || derivedTitle },
    revenueSplit: {
      ...normalizedRevenueSplit,
      title: normalizedRevenueSplit.title || derivedTitle,
    },
    reservations: {
      ...normalizedReservations,
      title: normalizedReservations.title || derivedTitle,
    },
    wallet: normalizeWalletBalance(wallet),
    partialErrors,
  }
}
