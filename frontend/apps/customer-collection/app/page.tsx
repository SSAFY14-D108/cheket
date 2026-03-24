'use client'

import { useEffect, useState } from 'react'
import { CollectionScreen } from '@/components/cheket/collection-screen'
import { fetchCollectionTickets } from '@/lib/api'
import { mapDtoToTicket, type CollectionTicket } from '@/lib/types'

export default function Page() {
  const [tickets, setTickets] = useState<CollectionTicket[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [token, setToken] = useState<string | null>(null)

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const t = params.get('token')
    if (t) setToken(t)
    else {
      const stored = localStorage.getItem('cheket_token')
      if (stored) setToken(stored)
    }
  }, [])

  useEffect(() => {
    if (!token) { setLoading(false); return }
    localStorage.setItem('cheket_token', token)
    console.log('[Collection] Fetching with token:', token.substring(0, 20) + '...')
    fetchCollectionTickets(token)
      .then((dtos) => {
        console.log('[Collection] Got', dtos.length, 'tickets')
        setTickets(dtos.map(mapDtoToTicket))
      })
      .catch((err) => {
        console.error('[Collection] Failed to fetch:', err)
        setError(err.message)
      })
      .finally(() => setLoading(false))
  }, [token])

  console.log('[Collection] Rendering CollectionScreen with', tickets.length, 'tickets')

  if (loading) {
    return (
      <div className="flex h-full min-h-screen items-center justify-center bg-background">
        <p className="text-muted-foreground">컬렉션을 불러오는 중...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex h-full min-h-screen items-center justify-center bg-background">
        <p className="text-destructive">오류: {error}</p>
      </div>
    )
  }

  return <CollectionScreen tickets={tickets} />
}
