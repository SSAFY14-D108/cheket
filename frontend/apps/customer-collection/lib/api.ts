import type { ApiResponse, CollectionTicketDto } from './types'

// 프록시 경로 사용 (CORS 회피): /proxy/api/* → https://j14d108.p.ssafy.io/api/*
const API_BASE = ''

export async function fetchCollectionTickets(token: string): Promise<CollectionTicketDto[]> {
  const res = await fetch(`${API_BASE}/proxy/api/v1/tickets/collection`, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    cache: 'no-store',
  })

  if (!res.ok) {
    throw new Error(`API error: ${res.status}`)
  }

  const json: ApiResponse<CollectionTicketDto[]> = await res.json()
  return json.data ?? []
}
