import { apiFetch, apiRequest } from "@/lib/api"

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

export async function signupHost(payload: SignupRequest) {
  return apiFetch<AuthSuccessResponse>("/api/v1/host", {
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
