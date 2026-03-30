import type { Event } from './types'

export async function fetchKopisEventDetail(eventId: string): Promise<Event | null> {
  if (!eventId.startsWith('kopis_')) return null

  try {
    const response = await fetch(`/api/kopis/events/${eventId}`, { cache: 'no-store' })
    const data = await response.json()
    return data.item ?? null
  } catch {
    return null
  }
}
