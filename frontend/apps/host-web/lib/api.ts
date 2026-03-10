import {
  clearAuthTokens,
  getAccessToken,
  getRefreshToken,
  setAccessToken,
  setRefreshToken,
} from "@/lib/auth-storage"

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = "ApiError"
    this.status = status
  }
}

export interface ApiResult<TResponse> {
  data: TResponse
  headers: Headers
  status: number
}

interface TokenReissueResponse {
  httpStatusCode: number
  responseMessage: string
  data: {
    accessToken: string
    refreshToken: string
  }
}

const TOKEN_REISSUE_PATH = "/api/v1/auth/reissue"
const RETRY_HEADER = "X-Auth-Retry"
const PUBLIC_AUTH_REQUESTS = [
  { method: "POST", path: "/api/v1/hosts" },
  { method: "POST", path: "/api/v1/hosts/login" },
] as const

let refreshPromise: Promise<TokenReissueResponse["data"]> | null = null

function buildUrl(path: string) {
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL?.trim() ?? ""

  if (!baseUrl) {
    return path
  }

  return `${baseUrl.replace(/\/$/, "")}${path}`
}

function normalizeMethod(method?: string) {
  return (method ?? "GET").toUpperCase()
}

function isSameRequestTarget(path: string, method: string, target: { path: string; method: string }) {
  return path === target.path && method === target.method
}

function isPublicAuthRequest(path: string, method: string) {
  return PUBLIC_AUTH_REQUESTS.some((target) => isSameRequestTarget(path, method, target))
}

function isTokenReissueRequest(path: string, method: string) {
  return isSameRequestTarget(path, method, {
    path: TOKEN_REISSUE_PATH,
    method: "POST",
  })
}

function buildHeaders(path: string, method: string, init?: RequestInit) {
  const headers = new Headers(init?.headers)
  const token = getAccessToken()

  if (!headers.has("Content-Type") && !(init?.body instanceof FormData)) {
    headers.set("Content-Type", "application/json")
  }

  if (token && !headers.has("Authorization") && !isPublicAuthRequest(path, method)) {
    headers.set("Authorization", toAuthorizationHeader(token))
  }

  return headers
}

function toAuthorizationHeader(token: string) {
  return token.startsWith("Bearer ") ? token : `Bearer ${token}`
}

async function parseResponseBody(response: Response) {
  if (response.status === 204) {
    return null
  }

  const contentType = response.headers.get("content-type") ?? ""

  if (contentType.includes("application/json")) {
    return response.json()
  }

  return null
}

function shouldAttemptRefresh(path: string, method: string, status: number, headers: Headers) {
  return (
    status === 401 &&
    !headers.has(RETRY_HEADER) &&
    !isPublicAuthRequest(path, method) &&
    !isTokenReissueRequest(path, method)
  )
}

function redirectToLogin() {
  if (typeof window !== "undefined") {
    window.location.href = "/"
  }
}

function notifyAuthExpired() {
  if (typeof window !== "undefined") {
    window.alert("로그인이 만료되었습니다. 다시 로그인해주세요.")
  }
}

function handleAuthFailure() {
  clearAuthTokens()
  notifyAuthExpired()
  redirectToLogin()
}

async function requestTokenReissue(expiredAccessToken: string | null) {
  const refreshToken = getRefreshToken()

  if (!refreshToken) {
    handleAuthFailure()
    throw new ApiError("인증이 만료되었습니다. 다시 로그인해주세요.", 401)
  }

  const response = await fetch(buildUrl(TOKEN_REISSUE_PATH), {
    method: "POST",
    headers: new Headers({
      "Content-Type": "application/json",
      ...(expiredAccessToken
        ? { Authorization: toAuthorizationHeader(expiredAccessToken) }
        : {}),
      [RETRY_HEADER]: "true",
    }),
    body: JSON.stringify({ refreshToken }),
  })

  const payload = (await parseResponseBody(response)) as TokenReissueResponse | null

  if (!response.ok || !payload?.data?.accessToken || !payload?.data?.refreshToken) {
    handleAuthFailure()

    const message =
      payload?.responseMessage || "인증이 만료되었습니다. 다시 로그인해주세요."

    throw new ApiError(message, response.status || 401)
  }

  setAccessToken(payload.data.accessToken)
  setRefreshToken(payload.data.refreshToken)

  return payload.data
}

async function refreshTokens(expiredAccessToken: string | null) {
  if (!refreshPromise) {
    refreshPromise = requestTokenReissue(expiredAccessToken).finally(() => {
      refreshPromise = null
    })
  }

  return refreshPromise
}

export async function apiRequest<TResponse>(
  path: string,
  init?: RequestInit
): Promise<ApiResult<TResponse>> {
  const method = normalizeMethod(init?.method)
  const headers = buildHeaders(path, method, init)
  const response = await fetch(buildUrl(path), {
    ...init,
    headers,
  })

  if (shouldAttemptRefresh(path, method, response.status, headers)) {
    try {
      const newTokens = await refreshTokens(headers.get("Authorization"))
      const retryHeaders = buildHeaders(path, method, init)

      retryHeaders.set("Authorization", toAuthorizationHeader(newTokens.accessToken))
      retryHeaders.set(RETRY_HEADER, "true")

      const retryResponse = await fetch(buildUrl(path), {
        ...init,
        headers: retryHeaders,
      })

      const retryPayload = await parseResponseBody(retryResponse)

      if (!retryResponse.ok) {
        const message =
          retryPayload?.errorMessage ||
          retryPayload?.responseMessage ||
          "요청 처리 중 오류가 발생했습니다."

        throw new ApiError(message, retryResponse.status)
      }

      return {
        data: retryPayload as TResponse,
        headers: retryResponse.headers,
        status: retryResponse.status,
      }
    } catch (error) {
      if (error instanceof ApiError) {
        throw error
      }

      throw new ApiError("인증 갱신 중 오류가 발생했습니다.", 401)
    }
  }

  const payload = await parseResponseBody(response)

  if (!response.ok) {
    const message =
      payload?.errorMessage ||
      payload?.responseMessage ||
      "요청 처리 중 오류가 발생했습니다."

    throw new ApiError(message, response.status)
  }

  return {
    data: payload as TResponse,
    headers: response.headers,
    status: response.status,
  }
}

export async function apiFetch<TResponse>(
  path: string,
  init?: RequestInit
): Promise<TResponse> {
  const result = await apiRequest<TResponse>(path, init)
  return result.data
}
