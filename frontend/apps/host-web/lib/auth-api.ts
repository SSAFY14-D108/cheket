import { apiFetch } from "@/lib/api"

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

export async function signupHost(payload: SignupRequest) {
  return apiFetch<AuthSuccessResponse>("/api/v1/host", {
    method: "POST",
    body: JSON.stringify(payload),
  })
}

export async function loginHost(payload: LoginRequest) {
  return apiFetch<AuthSuccessResponse>("/api/v1/host/login", {
    method: "POST",
    body: JSON.stringify(payload),
  })
}
