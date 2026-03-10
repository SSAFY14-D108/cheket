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
    totalSales,
    bookingRate,
    revenueSplit,
    reservations,
    wallet,
  }
}
