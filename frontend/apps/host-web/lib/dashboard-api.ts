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
  return typeof value === "number" && Number.isFinite(value) ? value : fallback
}

function toSafeString(value: unknown, fallback = "") {
  return typeof value === "string" ? value : fallback
}

function normalizeReservations(data: DashboardReservations): DashboardReservations {
  const sessions = Array.isArray(data.sessions)
    ? data.sessions.map((session) => ({
        sessionId: toSafeNumber(session.sessionId),
        date: toSafeString(session.date),
        capacity: toSafeNumber(session.capacity),
        reservedSeats: toSafeNumber(session.reservedSeats),
      }))
    : []

  return {
    showId: toSafeNumber(data.showId),
    title: toSafeString(data.title, "공연"),
    venue: toSafeString(data.venue, "-"),
    sessions,
  }
}

function normalizeRevenueSplit(data: DashboardRevenueSplit): DashboardRevenueSplit {
  const splits = Array.isArray(data.splits)
    ? data.splits.map((split) => ({
        role: toSafeString(split.role, "미분류"),
        rateBps: toSafeNumber(split.rateBps),
        amount: toSafeNumber(split.amount),
      }))
    : []

  return {
    showId: toSafeNumber(data.showId),
    title: toSafeString(data.title, "공연"),
    totalRevenue: toSafeNumber(data.totalRevenue),
    splits,
  }
}

function normalizeBookingRate(data: DashboardBookingRate): DashboardBookingRate {
  return {
    showId: toSafeNumber(data.showId),
    title: toSafeString(data.title, "공연"),
    capacity: toSafeNumber(data.capacity),
    reservedSeats: toSafeNumber(data.reservedSeats),
    bookingRate: toSafeNumber(data.bookingRate),
  }
}

function normalizeTotalSales(data: DashboardTotalSales): DashboardTotalSales {
  return {
    showId: toSafeNumber(data.showId),
    title: toSafeString(data.title, "공연"),
    totalPrimarySales: toSafeNumber(data.totalPrimarySales),
  }
}

function normalizeWalletBalance(wallet: WalletBalance | null): WalletBalance | null {
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
  const [reservations, revenueSplit, bookingRate, totalSales, wallet] = await Promise.all([
    fetchDashboardReservations(showId),
    fetchDashboardRevenueSplit(showId),
    fetchDashboardBookingRate(showId),
    fetchDashboardTotalSales(showId),
    fetchDashboardWalletBalance(),
  ])

  return {
    totalSales: normalizeTotalSales(totalSales),
    bookingRate: normalizeBookingRate(bookingRate),
    revenueSplit: normalizeRevenueSplit(revenueSplit),
    reservations: normalizeReservations(reservations),
    wallet: normalizeWalletBalance(wallet),
  }
}
