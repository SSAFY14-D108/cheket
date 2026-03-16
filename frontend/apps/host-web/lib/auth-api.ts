import { ApiError, apiFetch, apiRequest } from "@/lib/api"

export interface ApiMessageResponse {
  httpStatusCode: number
  responseMessage: string
}

export interface SignupRequest {
  companyName: string
  businessNo: string
  email: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginTokenData {
  accessToken: string
  refreshToken: string
}

export interface LoginResponse extends ApiMessageResponse {
  data: LoginTokenData
}

export type SignupResponse = ApiMessageResponse
export type LogoutResponse = ApiMessageResponse

export interface BusinessNoDuplicateResult {
  isDuplicated: boolean
}

export interface BusinessNoDuplicateResponse extends ApiMessageResponse {
  data: BusinessNoDuplicateResult
}

export async function signupHost(payload: SignupRequest) {
  return apiFetch<SignupResponse>("/api/v1/hosts", {
    method: "POST",
    body: JSON.stringify(payload),
  })
}

export async function loginHost(payload: LoginRequest) {
  const result = await apiRequest<LoginResponse>("/api/v1/hosts/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  })

  return result.data
}

export async function logoutHost() {
  return apiFetch<LogoutResponse>("/api/v1/hosts/logout", {
    method: "POST",
  })
}

export async function checkBusinessNoDuplicate(businessNo: string) {
  const response = await apiFetch<BusinessNoDuplicateResponse>(
    "/api/v1/hosts/business-no/duplicate",
    {
      method: "POST",
      body: JSON.stringify({ businessNo }),
    }
  )

  if (typeof response.data?.isDuplicated !== "boolean") {
    throw new ApiError("사업자번호 중복확인 응답 형식이 올바르지 않습니다.", 500)
  }

  return response.data
}
