import { apiFetch } from "@/lib/api"

interface ApiResponse<T> {
  httpStatusCode: number
  responseMessage: string
  data: T
}

export type StakeholderSearchUserType = "HOST" | "USER"

export interface StakeholderSearchResult {
  id: number
  name: string
  number: string
}

export async function searchStakeholder(
  userType: StakeholderSearchUserType,
  number: string
) {
  const searchParams = new URLSearchParams({
    userType,
    number,
  })

  const response = await apiFetch<ApiResponse<StakeholderSearchResult>>(
    `/api/v1/auth/search?${searchParams.toString()}`,
    {
      method: "GET",
    }
  )

  return response.data
}
