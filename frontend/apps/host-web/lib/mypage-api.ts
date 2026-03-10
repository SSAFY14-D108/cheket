import { apiFetch } from "@/lib/api"

interface ApiResponse<T> {
  httpStatusCode: number
  responseMessage: string
  data: T
}

export interface MyCompanyInfo {
  companyName: string
  businessNo: string
  email: string
  walletAddress?: string | null
  balance?: number | null
}

export interface MyWalletBalance {
  balance: number
  walletAddress: string
}

export interface MyShowSummary {
  showId: number
  title: string
  posterUrl: string
  venue: string
  purchaseLimit: number
  region: string
  show: {
    showStartDate: string
    showEndDate: string
  }
  reservation: {
    startDate: string
    endDate: string
  }
  status: string
}

export interface MyShowsPage {
  shows: MyShowSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

interface FetchMyShowsParams {
  page?: number
  size?: number
}

export async function fetchMyCompanyInfo() {
  const response = await apiFetch<ApiResponse<MyCompanyInfo>>("/api/v1/hosts", {
    method: "GET",
  })

  return response.data
}

export async function fetchMyShows(params: FetchMyShowsParams = {}) {
  const searchParams = new URLSearchParams()

  if (params.page !== undefined) {
    searchParams.set("page", String(params.page))
  }

  if (params.size !== undefined) {
    searchParams.set("size", String(params.size))
  }

  const query = searchParams.toString()
  const path = query ? `/api/v1/hosts/shows?${query}` : "/api/v1/hosts/shows"
  const response = await apiFetch<ApiResponse<MyShowsPage>>(path, {
    method: "GET",
  })

  return response.data
}

export async function fetchMyWalletBalance() {
  const response = await apiFetch<ApiResponse<MyWalletBalance>>("/api/v1/wallets/balance", {
    method: "GET",
  })

  return response.data
}
