'use client'

import { useEffect, useState, useCallback } from 'react'
import {
  CollectibleTicketCard,
  getTicketEffect,
  isPokeEffect,
  isPaperEffect,
  type EffectType,
  type HoloVariant,
} from '@/components/cheket/collection-screen'
import type { CollectionTicket } from '@/lib/types'

const PLACEHOLDER_POSTER = '/placeholder.svg'

/** 쿼리 파라미터에서 초기값 추출 */
function getInitialParams() {
  if (typeof window === 'undefined') return { effect: 'none', poster: '' }
  const params = new URLSearchParams(window.location.search)
  return {
    effect: params.get('effect') || 'none',
    poster: params.get('poster') || '',
  }
}

function buildTicket(poster: string): CollectionTicket {
  return {
    id: 'preview-1',
    eventId: 'preview',
    eventName: '미리보기',
    eventDate: '',
    venue: '',
    poster: poster || PLACEHOLDER_POSTER,
    seatId: '',
    seatLabel: '',
    grade: '',
    numbering: '1',
    status: 'USED',
    effect: null,
  }
}

export default function PreviewPage() {
  const [effectName, setEffectName] = useState<string>('none')
  const [poster, setPoster] = useState<string>('')

  // 초기 쿼리 파라미터 로드
  useEffect(() => {
    const params = getInitialParams()
    setEffectName(params.effect)
    if (params.poster) setPoster(params.poster)
  }, [])

  // postMessage 수신: 효과 변경 / 포스터 변경
  useEffect(() => {
    function handleMessage(e: MessageEvent) {
      const data = e.data
      if (!data || typeof data !== 'object') return

      if (data.type === 'setEffect' && typeof data.effect === 'string') {
        setEffectName(data.effect)
      }
      if (data.type === 'setPoster' && typeof data.poster === 'string') {
        setPoster(data.poster)
      }
    }

    window.addEventListener('message', handleMessage)
    return () => window.removeEventListener('message', handleMessage)
  }, [])

  const ticket = buildTicket(poster)
  const effect: EffectType = getTicketEffect('preview-1', effectName === 'none' ? null : effectName)

  const pokeEffect = isPokeEffect(effect) ? effect : undefined
  const paperEffect = isPaperEffect(effect) ? effect : undefined
  const metalEffect =
    effect === 'gold-foil' || effect === 'silver-foil' || effect === 'rose-foil'
      ? effect
      : undefined
  const holoVariant: HoloVariant | undefined =
    !pokeEffect && !paperEffect && !metalEffect && effect !== 'none'
      ? (effect as HoloVariant)
      : undefined
  const enableHolo = !!holoVariant

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: '100dvw',
        height: '100dvh',
        background: 'transparent',
        overflow: 'hidden',
      }}
    >
      <CollectibleTicketCard
        ticket={ticket}
        metalEffect={metalEffect}
        holoVariant={holoVariant ?? 'rainbow'}
        enableHolo={enableHolo}
        pokeEffect={pokeEffect}
        paperEffect={paperEffect}
        displayScale={0.5}
        numberAura="none"
      />
    </div>
  )
}
