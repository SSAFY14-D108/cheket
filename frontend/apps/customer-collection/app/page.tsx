'use client'

import { useCallback, useEffect, useRef, useState } from 'react'
import { CollectionScreen } from '@/components/cheket/collection-screen'
import { fetchCollectionTickets } from '@/lib/api'
import { mapDtoToTicket, type CollectionTicket, type CollectionTicketDto } from '@/lib/types'

declare global {
  interface Window {
    CheketCollectionBridge?: {
      onAppReady?: () => void
      onAppError?: (message: string) => void
    }
    CheketCollectionDataBridge?: {
      hasInitialCollectionTickets?: () => boolean
      getInitialCollectionTicketsJson?: () => string | null
    }
  }
}

export default function Page() {
  const [tickets, setTickets] = useState<CollectionTicket[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [nativeSeedUsed, setNativeSeedUsed] = useState(false)
  const nativeReadySentRef = useRef(false)

  const notifyNativeReady = useCallback(() => {
    if (nativeReadySentRef.current) return
    nativeReadySentRef.current = true
    window.CheketCollectionBridge?.onAppReady?.()
  }, [])

  const notifyNativeError = useCallback(
    (message: string) => {
      window.CheketCollectionBridge?.onAppError?.(message)
      if (!window.CheketCollectionBridge?.onAppError) {
        notifyNativeReady()
      }
    },
    [notifyNativeReady]
  )

  useEffect(() => {
    const nativeDataBridge = window.CheketCollectionDataBridge
    if (nativeDataBridge?.hasInitialCollectionTickets?.()) {
      try {
        const rawJson = nativeDataBridge.getInitialCollectionTicketsJson?.()
        const dtos = rawJson ? (JSON.parse(rawJson) as CollectionTicketDto[]) : []
        setTickets(dtos.map(mapDtoToTicket))
        setNativeSeedUsed(true)
        setLoading(false)
        return
      } catch (err) {
        console.error('[Collection] Failed to parse native seed:', err)
      }
    }

    const params = new URLSearchParams(window.location.search)
    const t = params.get('token')
    if (t) setToken(t)
    else {
      const stored = localStorage.getItem('cheket_token')
      if (stored) setToken(stored)
    }
  }, [])

  useEffect(() => {
    if (nativeSeedUsed) {
      setLoading(false)
      return
    }
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
  }, [nativeSeedUsed, token])

  useEffect(() => {
    if (!loading && error) {
      notifyNativeError(error)
    }
  }, [error, loading, notifyNativeError])

  useEffect(() => {
    if (!loading && !error && tickets.length === 0) {
      notifyNativeReady()
    }
  }, [error, loading, notifyNativeReady, tickets.length])

  console.log('[Collection] Rendering CollectionScreen with', tickets.length, 'tickets')

  // 로딩 중 — inline style로 WebView height 문제 회피
  if (loading) {
    return (
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        minHeight: '100vh',
        background: 'linear-gradient(180deg, #0a0a0a 0%, #111827 50%, #0a0a0a 100%)',
        gap: 20,
        fontFamily: 'Pretendard, -apple-system, sans-serif',
      }}>
        {/* 스피너 */}
        <div style={{
          width: 48,
          height: 48,
          border: '3px solid rgba(255,255,255,0.1)',
          borderTopColor: '#00C598',
          borderRadius: '50%',
          animation: 'spin 0.8s linear infinite',
        }} />
        <p style={{ color: '#9ca3af', fontSize: 14, margin: 0 }}>
          컬렉션을 불러오는 중...
        </p>
        <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      </div>
    )
  }

  if (error) {
    return (
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        minHeight: '100vh',
        background: '#0a0a0a',
        gap: 12,
        fontFamily: 'Pretendard, -apple-system, sans-serif',
      }}>
        <p style={{ color: '#f87171', fontSize: 14, margin: 0 }}>오류: {error}</p>
      </div>
    )
  }

  return <CollectionScreen tickets={tickets} onInitialVisualReady={notifyNativeReady} />
}
