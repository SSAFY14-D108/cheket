const DEFAULT_API_BASE_URL = "http://localhost:8080"

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = "ApiError"
    this.status = status
  }
}

function buildUrl(path: string) {
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL?.trim() || DEFAULT_API_BASE_URL
  return `${baseUrl}${path}`
}

export async function apiFetch<TResponse>(
  path: string,
  init?: RequestInit
): Promise<TResponse> {
  const response = await fetch(buildUrl(path), {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
  })

  const contentType = response.headers.get("content-type") ?? ""
  const isJson = contentType.includes("application/json")
  const payload = isJson ? await response.json() : null

  if (!response.ok) {
    const message =
      payload?.errorMessage ||
      payload?.responseMessage ||
      "요청 처리 중 오류가 발생했습니다."

    throw new ApiError(message, response.status)
  }

  return payload as TResponse
}
