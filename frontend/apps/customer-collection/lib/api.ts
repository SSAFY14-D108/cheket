import type { ApiResponse, CollectionTicketDto } from './types'

// 프록시 경로 사용 (CORS 회피): /proxy/api/* → https://j14d108.p.ssafy.io/api/*
const API_BASE = ''
const MAX_RETRIES = 2
const RETRY_DELAY_MS = 800

async function fetchWithRetry(
  url: string,
  options: RequestInit,
  retries = MAX_RETRIES
): Promise<Response> {
  for (let attempt = 0; attempt <= retries; attempt++) {
    try {
      const res = await fetch(url, options)
      if (res.ok || attempt === retries) return res
      // 서버 에러(5xx)만 재시도, 4xx는 즉시 반환
      if (res.status < 500) return res
    } catch (err) {
      if (attempt === retries) throw err
    }
    await new Promise((r) => setTimeout(r, RETRY_DELAY_MS * (attempt + 1)))
  }
  // unreachable, but TS needs it
  throw new Error('fetchWithRetry exhausted')
}

export async function fetchCollectionTickets(token: string): Promise<CollectionTicketDto[]> {
  const res = await fetchWithRetry(
    `${API_BASE}/proxy/api/v1/tickets/collection/onchain`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      cache: 'no-store',
    }
  )

  if (!res.ok) {
    throw new Error(`API error: ${res.status}`)
  }

  const json: ApiResponse<CollectionTicketDto[]> = await res.json()
  return json.data ?? []
}
