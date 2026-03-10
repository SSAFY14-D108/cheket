import { ApiError, apiFetch, apiRequest } from "@/lib/api"

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

export interface AuthSuccessResponse {
  httpStatusCode: number
  responseMessage: string
}

export interface LoginTokenData {
  accessToken: string
  refreshToken: string
}

export interface LoginResponse extends AuthSuccessResponse {
  data: LoginTokenData
}

export type LogoutResponse = AuthSuccessResponse
export interface BusinessNoDuplicateResult {
  isDuplicate: boolean
}

interface BusinessNoDuplicateResponse extends AuthSuccessResponse {
  data: {
    isDuplicate: boolean
  }
}

export async function signupHost(payload: SignupRequest) {
  return apiFetch<AuthSuccessResponse>("/api/v1/hosts", {
    method: "POST",
    body: JSON.stringify(payload),
  })
}

export async function loginHost(payload: LoginRequest) {
  const result = await apiRequest<LoginResponse>("/api/v1/hosts/login", {
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
  try {
    const response = await apiFetch<BusinessNoDuplicateResponse>(
      "/api/v1/hosts/business-no/duplicate",
      {
        method: "POST",
        body: JSON.stringify({ businessNo }),
      }
    )

    if (typeof response.data?.isDuplicate !== "boolean") {
      throw new ApiError("사업자번호 중복확인 응답 형식이 올바르지 않습니다.", 500)
    }

    return response.data
  } catch (error) {
    // apiFetch treats 409 as an error, so we normalize the duplicate case here.
    if (error instanceof ApiError && error.status === 409) {
      return { isDuplicate: true } satisfies BusinessNoDuplicateResult
    }

    throw error
  }
}
