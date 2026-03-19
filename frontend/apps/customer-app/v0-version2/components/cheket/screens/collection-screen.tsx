'use client'

import { useCallback, useEffect, useMemo, useRef, useState, type CSSProperties } from 'react'
import Image from 'next/image'
import { motion } from 'framer-motion'
import ReactCardFlip from 'react-card-flip'
import Tilt from 'react-parallax-tilt'
import { ChevronLeft, ChevronRight, Music2, Settings2 } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import type { Ticket } from '@/lib/types'
import { AppShell } from '../app-shell'
import { TicketSparklesAura } from '../ticket-sparkles-aura'

const CARD_WIDTH = 270
const CARD_HEIGHT = 530
const CARD_COMPACT_WIDTH = 150
const CARD_COMPACT_HEIGHT = 295
const CARD_COMPACT_SCALE = CARD_COMPACT_WIDTH / CARD_WIDTH
const decodedPosterCache = new Set<string>()
const decodingPosterCache = new Map<string, Promise<void>>()

type HoloVariant = 'rainbow' | 'aurora' | 'prism' | 'cosmos' | 'sunset' | 'neon'
const HOLO_VARIANTS: HoloVariant[] = ['rainbow', 'aurora', 'prism', 'cosmos', 'sunset', 'neon']

type PokeEffect =
  | 'poke-holo'
  | 'poke-galaxy'
  | 'poke-v'
  | 'poke-vmax'
  | 'poke-rainbow'
  | 'poke-secret'
  | 'poke-vstar'
  | 'poke-fullart'
  | 'poke-trainer'
  | 'poke-radiant'
  | 'poke-gallery-holo'
  | 'poke-gallery-v'

type PaperEffect = 'paper-cotton' | 'paper-crumpled'

type EffectType = 'none' | 'gold-foil' | 'silver-foil' | 'rose-foil' | HoloVariant | PokeEffect | PaperEffect
type MetalEffect = 'gold-foil' | 'silver-foil' | 'rose-foil'
type NumberAuraType = 'none' | 'aura-rare' | 'milestone-rare' | 'spark-rare'
type NumberAuraSetting = 'auto' | NumberAuraType

interface FaceProps {
  ticket: Ticket
  onFlip: () => void
  metalEffect?: MetalEffect
  eagerLoad?: boolean
  holoActive?: boolean
  holoVariant?: HoloVariant
  holoLayerRef?: React.RefObject<HTMLDivElement | null>
  enableHolo?: boolean
  pokeEffect?: PokeEffect
  paperEffect?: PaperEffect
  numberAura?: NumberAuraType
}

const GRADE_COLORS: Record<string, { bg: string; text: string }> = {
  VIP: { bg: '#f59e0b', text: '#000' },
  'VIP PIT': { bg: '#f59e0b', text: '#000' },
  R: { bg: '#ef4444', text: '#fff' },
  FLOOR: { bg: '#3b82f6', text: '#fff' },
  'GA PIT': { bg: '#ef4444', text: '#fff' },
  S: { bg: '#3b82f6', text: '#fff' },
  A: { bg: '#22c55e', text: '#fff' },
  STAND: { bg: '#22c55e', text: '#fff' },
  '2F': { bg: '#a855f7', text: '#fff' },
  '1F': { bg: '#06b6d4', text: '#fff' },
  GA: { bg: '#3b82f6', text: '#fff' },
}

function clamp(value: number, min: number, max: number) {
  return Math.min(max, Math.max(min, value))
}

function mod(value: number, length: number) {
  if (length === 0) return 0
  return ((value % length) + length) % length
}

function getGrade(grade: string) {
  return GRADE_COLORS[grade] ?? { bg: '#6b7280', text: '#fff' }
}

function preloadPoster(src: string, eager = false) {
  if (!src) return Promise.resolve()
  if (decodedPosterCache.has(src)) return Promise.resolve()

  const existing = decodingPosterCache.get(src)
  if (existing) return existing

  if (typeof window === 'undefined') return Promise.resolve()

  const img = new window.Image()
  img.decoding = 'async'
  if (eager) {
    img.fetchPriority = 'high'
  }

  const promise = new Promise<void>((resolve) => {
    const finish = async () => {
      try {
        await img.decode?.()
      } catch {
        // Swallow decode failures and still mark the image as ready after load.
      }
      decodedPosterCache.add(src)
      resolve()
    }

    img.onload = () => {
      void finish()
    }
    img.onerror = () => {
      resolve()
    }
    img.src = src

    if (img.complete) {
      void finish()
    }
  }).finally(() => {
    decodingPosterCache.delete(src)
  })

  decodingPosterCache.set(src, promise)
  return promise
}

function useDecodedPoster(src: string, eagerLoad = false) {
  const [ready, setReady] = useState(() => decodedPosterCache.has(src))

  useEffect(() => {
    let cancelled = false

    if (decodedPosterCache.has(src)) {
      setReady(true)
      return () => {
        cancelled = true
      }
    }

    setReady(false)
    void preloadPoster(src, eagerLoad).then(() => {
      if (!cancelled) {
        setReady(true)
      }
    })

    return () => {
      cancelled = true
    }
  }, [src, eagerLoad])

  return ready
}

function getTicketEffect(ticketId: string): EffectType {
  const num = Number(ticketId.replace(/\D/g, '')) || 0
  return HOLO_VARIANTS[num % HOLO_VARIANTS.length]
}

function getTicketNumberValue(ticketId: string) {
  return Number(ticketId.replace(/\D/g, '')) || 0
}

function isRepdigit(value: number) {
  const digits = String(value)
  return digits.length > 1 && digits.split('').every((digit) => digit === digits[0])
}

function getNumberAura(ticketId: string): NumberAuraType {
  const value = getTicketNumberValue(ticketId)
  if (!value) return 'none'
  if (value === 1) return 'aura-rare'
  if (value % 100 === 0) return 'milestone-rare'
  if (String(value).includes('777') || String(value).includes('888') || String(value).includes('999')) return 'spark-rare'
  if (isRepdigit(value)) return 'spark-rare'
  return 'none'
}

function isPokeEffect(effect: EffectType): effect is PokeEffect {
  return effect !== 'none' && effect.startsWith('poke-')
}

function isPaperEffect(effect: EffectType): effect is PaperEffect {
  return effect === 'paper-cotton' || effect === 'paper-crumpled'
}

function TicketFront({
  ticket,
  onFlip,
  metalEffect,
  eagerLoad = false,
  holoActive = false,
  holoVariant = 'rainbow',
  holoLayerRef,
  enableHolo = false,
  pokeEffect,
  paperEffect,
  numberAura,
}: FaceProps) {
  const isMetal = metalEffect === 'gold-foil' || metalEffect === 'silver-foil' || metalEffect === 'rose-foil'
  const metallicClass = metalEffect ? `ticket-metallic-${metalEffect}` : ''
  const isPaper = !!paperEffect
  const posterReady = useDecodedPoster(ticket.poster, eagerLoad)

  return (
    <div
      style={{ display: 'flex', flexDirection: 'column', height: CARD_HEIGHT, cursor: 'pointer', position: 'relative' }}
      onClick={onFlip}
    >
      <div
        className={isMetal ? `ticket-shape-megabox ${metallicClass}` : 'ticket-shape-megabox'}
        style={{
          position: 'absolute',
          inset: 0,
          overflow: 'hidden',
          background:
            metalEffect === 'gold-foil'
              ? '#d9ad2a'
              : metalEffect === 'silver-foil'
                ? '#c7d0dc'
                : metalEffect === 'rose-foil'
                  ? '#c79080'
                  : paperEffect === 'paper-cotton'
                    ? '#e8e0d2'
                    : paperEffect === 'paper-crumpled'
                      ? '#ddd3c4'
                      : '#0b0f1a',
        }}
      >
        <div style={{ position: 'relative', width: '100%', height: '100%', zIndex: 3 }}>
          {posterReady && (
            <div
              style={{
                position: 'absolute',
                inset: 0,
                backgroundImage: `url("${ticket.poster}")`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                backgroundRepeat: 'no-repeat',
                filter:
                  metalEffect === 'gold-foil'
                    ? 'grayscale(0.92) sepia(0.88) saturate(0.42) hue-rotate(-8deg) contrast(1.08) brightness(0.78)'
                    : metalEffect === 'silver-foil'
                      ? 'grayscale(0.98) sepia(0.18) saturate(0.18) hue-rotate(6deg) contrast(1.08) brightness(0.8)'
                      : metalEffect === 'rose-foil'
                        ? 'grayscale(0.76) sepia(0.72) saturate(0.34) hue-rotate(-18deg) contrast(1.08) brightness(0.8)'
                        : paperEffect === 'paper-cotton'
                          ? 'grayscale(0.01) sepia(0.01) saturate(0.995) brightness(1) contrast(1.01)'
                        : paperEffect === 'paper-crumpled'
                            ? 'grayscale(0.01) sepia(0.02) saturate(0.99) brightness(1) contrast(1.03)'
                            : 'none',
                transform: isMetal || isPaper ? 'scale(1.015)' : 'none',
                opacity: isMetal ? 0.88 : paperEffect === 'paper-crumpled' ? 0.99 : isPaper ? 0.96 : 1,
              }}
            />
          )}
          {isMetal && (
            <>
              <div className={`ticket-metallic-${metalEffect}__base`} />
              <div className={`ticket-metallic-${metalEffect}__brushed`} />
              <div className={`ticket-metallic-${metalEffect}__sheen`} />
              <div className={`ticket-metallic-${metalEffect}__print`} />
            </>
          )}
          {metalEffect === 'gold-foil' && <div className="ticket-metallic-gold-foil__tint" />}
          {metalEffect === 'silver-foil' && <div className="ticket-metallic-silver-foil__tint" />}
          {metalEffect === 'rose-foil' && <div className="ticket-metallic-rose-foil__tint" />}
          {paperEffect && (
            <>
              <div className={`ticket-paper-${paperEffect}__base`} />
              <div className={`ticket-paper-${paperEffect}__fiber`} />
              <div className={`ticket-paper-${paperEffect}__speckle`} />
              <div className={`ticket-paper-${paperEffect}__print`} />
            </>
          )}
          {enableHolo && !pokeEffect && (
            <div
              ref={holoLayerRef}
              className="ticket-holo-front-layer"
              style={
                {
                  '--mx': '50%',
                  '--my': '50%',
                  '--rx': '0deg',
                  '--ry': '0deg',
                } as CSSProperties
              }
            >
              <div className={`ticket-holo-rainbow ticket-holo-${holoVariant}${holoActive ? ' is-active' : ''}`} />
              <div className={`ticket-holo-glare ticket-holo-${holoVariant}${holoActive ? ' is-active' : ''}`} />
              <div className={`ticket-holo-foil ticket-holo-${holoVariant}${holoActive ? ' is-active' : ''}`} />
              <div className={`ticket-holo-noise ticket-holo-${holoVariant}`} />
            </div>
          )}
          {pokeEffect && (
            <div
              ref={holoLayerRef}
              className="ticket-holo-front-layer"
              style={
                {
                  '--mx': '50%',
                  '--my': '50%',
                  '--posx': '50%',
                  '--posy': '50%',
                } as CSSProperties
              }
            >
              <div className={`ticket-poke-shine${holoActive ? ' is-active' : ''}`} data-effect={pokeEffect} />
              <div className={`ticket-poke-glare${holoActive ? ' is-active' : ''}`} />
            </div>
          )}
          <div
            style={{
              position: 'absolute',
              inset: 0,
              background:
                metalEffect === 'gold-foil'
                  ? 'linear-gradient(180deg, rgba(20,16,4,0.06) 0%, rgba(96,78,10,0.16) 18%, rgba(124,100,16,0.28) 100%)'
                  : metalEffect === 'silver-foil'
                    ? 'linear-gradient(180deg, rgba(9,12,18,0.05) 0%, rgba(54,63,78,0.16) 20%, rgba(76,89,110,0.28) 100%)'
                    : metalEffect === 'rose-foil'
                      ? 'linear-gradient(180deg, rgba(16,11,11,0.05) 0%, rgba(92,55,52,0.18) 18%, rgba(126,78,72,0.3) 100%)'
                      : pokeEffect
                        ? 'linear-gradient(to top, rgba(11,15,26,0.36) 8%, rgba(11,15,26,0.08) 42%, rgba(11,15,26,0.02) 100%)'
                      : paperEffect === 'paper-cotton'
                        ? 'linear-gradient(180deg, rgba(112,90,56,0.04) 0%, rgba(145,118,82,0.08) 22%, rgba(88,68,40,0.16) 100%)'
                        : paperEffect === 'paper-crumpled'
                          ? 'linear-gradient(180deg, rgba(112,94,72,0.03) 0%, rgba(138,118,94,0.05) 22%, rgba(82,68,50,0.1) 100%)'
                          : 'linear-gradient(to top, rgba(11,15,26,0.58) 10%, rgba(11,15,26,0.12) 46%, rgba(11,15,26,0.04) 100%)',
            }}
          />
          <div
            style={{
              position: 'absolute',
              inset: 0,
              background:
                metalEffect === 'gold-foil'
                  ? 'linear-gradient(180deg, rgba(255,253,234,0.34) 0%, rgba(255,246,194,0.14) 18%, rgba(255,246,190,0) 42%)'
                  : metalEffect === 'silver-foil'
                    ? 'linear-gradient(180deg, rgba(255,255,255,0.24) 0%, rgba(239,245,252,0.1) 16%, rgba(248,251,255,0) 40%)'
                    : metalEffect === 'rose-foil'
                      ? 'linear-gradient(180deg, rgba(255,245,240,0.24) 0%, rgba(246,221,214,0.1) 16%, rgba(255,232,224,0) 40%)'
                    : pokeEffect
                      ? 'linear-gradient(to bottom, rgba(255,255,255,0.12) 0%, rgba(255,255,255,0.03) 18%, transparent 38%)'
                    : paperEffect === 'paper-cotton'
                      ? 'linear-gradient(180deg, rgba(255,251,241,0.18) 0%, rgba(255,248,232,0.08) 18%, rgba(255,244,226,0) 42%)'
                      : paperEffect === 'paper-crumpled'
                        ? 'linear-gradient(180deg, rgba(255,252,246,0.07) 0%, rgba(245,238,226,0.025) 18%, rgba(245,238,226,0) 42%)'
                        : 'linear-gradient(to bottom, rgba(0,0,0,0.12) 0%, transparent 24%)',
            }}
          />
        </div>
      </div>
    </div>
  )
}

function TicketBack({ ticket, onFlip, numberAura }: FaceProps) {
  const grade = getGrade(ticket.grade)

  return (
    <div
      style={{ display: 'flex', flexDirection: 'column', height: CARD_HEIGHT, cursor: 'pointer', position: 'relative' }}
      onClick={onFlip}
    >
      <div
        className="ticket-shape-megabox"
        style={{
          position: 'absolute',
          inset: 0,
          overflow: 'hidden',
          background: '#0b0f1a',
        }}
      >
        <Image
          src={ticket.poster}
          alt={ticket.eventName}
          fill
          sizes={`${CARD_WIDTH}px`}
          style={{ objectFit: 'cover', opacity: 0.26 }}
        />
        <div
          style={{
            position: 'absolute',
            inset: 0,
            background: 'linear-gradient(to bottom, rgba(9,13,24,0.9) 0%, rgba(9,13,24,0.93) 100%)',
            zIndex: 2,
          }}
        />

        <div
          style={{
            position: 'relative',
            zIndex: 3,
            height: '100%',
            padding: '18px',
            display: 'flex',
            flexDirection: 'column',
            color: 'rgba(255,255,255,0.9)',
          }}
        >
          <p style={{ fontSize: 11, letterSpacing: '0.2em', textTransform: 'uppercase', opacity: 0.7, fontWeight: 600 }}>
            Collectible Ticket
          </p>

          <div style={{ marginTop: 14 }}>
            <p style={{ fontSize: 18, fontWeight: 900, lineHeight: 1.2, textTransform: 'uppercase' }}>
              {ticket.eventName}
            </p>
          </div>

          <div style={{ borderTop: '1px solid rgba(255,255,255,0.28)', marginTop: 18, paddingTop: 14, display: 'grid', gap: 10 }}>
            <BackRow label="Date" value={ticket.eventDate.split(' ')[0]} />
            <BackRow label="Venue" value={ticket.venue} />
          </div>

          <div style={{ borderTop: '1px solid rgba(255,255,255,0.22)', marginTop: 18, paddingTop: 14 }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
              <SeatBox label="Grade" value={ticket.grade} accent={grade.bg} />
              <SeatBox label="Seat" value={ticket.seatLabel} accent="#f8e28a" />
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

function BackRow({ label, value }: { label: string; value: string }) {
  return (
    <div style={{ display: 'flex', gap: 10, alignItems: 'baseline' }}>
      <span
        style={{
          fontSize: 9,
          letterSpacing: '0.12em',
          textTransform: 'uppercase',
          color: 'rgba(255,255,255,0.3)',
          fontWeight: 700,
          minWidth: 36,
          flexShrink: 0,
        }}
      >
        {label}
      </span>
      <span
        style={{
          fontSize: 13,
          fontWeight: 600,
          color: 'rgba(255,255,255,0.75)',
          lineHeight: 1.3,
          wordBreak: 'keep-all',
        }}
      >
        {value}
      </span>
    </div>
  )
}

function SeatBox({
  label,
  value,
  accent,
}: {
  label: string
  value: string
  accent?: string
}) {
  return (
    <div
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 5,
        paddingTop: 10,
        paddingBottom: 10,
        borderRadius: 10,
        backgroundColor: accent ? `${accent}20` : 'rgba(255,255,255,0.04)',
        border: `1px solid ${accent ? `${accent}40` : 'rgba(255,255,255,0.07)'}`,
      }}
    >
      <span
        style={{
          fontSize: 9,
          textTransform: 'uppercase',
          letterSpacing: '0.15em',
          color: 'rgba(255,255,255,0.3)',
          fontWeight: 600,
        }}
      >
        {label}
      </span>
      <span
        style={{
          fontSize: 16,
          fontWeight: 800,
          lineHeight: 1,
          color: accent ?? 'rgba(255,255,255,0.82)',
        }}
      >
        {value}
      </span>
    </div>
  )
}

function CollectibleTicketCard({
  ticket,
  metalEffect,
  compact = false,
  onOpen,
  holoVariant = 'rainbow',
  enableHolo,
  pokeEffect,
  paperEffect,
  displayScale,
  numberAura,
}: {
  ticket: Ticket
  metalEffect?: MetalEffect
  compact?: boolean
  onOpen?: () => void
  holoVariant?: HoloVariant
  enableHolo?: boolean
  pokeEffect?: PokeEffect
  paperEffect?: PaperEffect
  displayScale?: number
  numberAura?: NumberAuraType
}) {
  const showHolo = enableHolo ?? true
  const [flipped, setFlipped] = useState(false)
  const [holoActive, setHoloActive] = useState(false)
  const holoHostRef = useRef<HTMLDivElement>(null)
  const holoLayerRef = useRef<HTMLDivElement>(null)
  const holoRafRef = useRef<number | null>(null)
  const resolvedNumberAura = numberAura ?? getNumberAura(ticket.id)

  const handleClick = useCallback(() => {
    if (compact) {
      onOpen?.()
      return
    }
    setFlipped((prev) => !prev)
  }, [compact, onOpen])

  useEffect(() => {
    return () => {
      if (holoRafRef.current !== null) {
        cancelAnimationFrame(holoRafRef.current)
      }
    }
  }, [])

  const updateHoloFromPointer = useCallback((clientX: number, clientY: number) => {
    const host = holoHostRef.current
    if (!host) return
    const rect = host.getBoundingClientRect()
    if (!rect.width || !rect.height) return

    const x = clamp((clientX - rect.left) / rect.width, 0, 1)
    const y = clamp((clientY - rect.top) / rect.height, 0, 1)
    const mx = x * 100
    const my = y * 100
    const rx = (0.5 - y) * 10
    const ry = (x - 0.5) * 10

    if (holoRafRef.current !== null) {
      cancelAnimationFrame(holoRafRef.current)
    }

    holoRafRef.current = requestAnimationFrame(() => {
      const live = holoLayerRef.current
      const dx = (x - 0.5) * 6
      const dy = (y - 0.5) * 6
      host.style.setProperty('--mx', `${mx}%`)
      host.style.setProperty('--my', `${my}%`)
      host.style.setProperty('--rx', `${rx}deg`)
      host.style.setProperty('--ry', `${ry}deg`)
      host.style.setProperty('--posx', `${mx}%`)
      host.style.setProperty('--posy', `${my}%`)
      host.style.setProperty('--dx', `${dx}%`)
      host.style.setProperty('--dy', `${dy}%`)
      if (!live) return
      live.style.setProperty('--mx', `${mx}%`)
      live.style.setProperty('--my', `${my}%`)
      live.style.setProperty('--rx', `${rx}deg`)
      live.style.setProperty('--ry', `${ry}deg`)
      live.style.setProperty('--posx', `${mx}%`)
      live.style.setProperty('--posy', `${my}%`)
    })
  }, [])

  const resolvedScale = compact ? CARD_COMPACT_SCALE : displayScale ?? 1
  const frameWidth = CARD_WIDTH * resolvedScale
  const frameHeight = CARD_HEIGHT * resolvedScale

  return (
    <div
      style={{
        width: frameWidth,
        height: frameHeight,
        cursor: 'pointer',
        margin: '0 auto',
        position: 'relative',
        overflow: 'visible',
      }}
      onClick={handleClick}
    >
        {resolvedNumberAura !== 'none' ? (
          <div className={`ticket-number-aura ${resolvedNumberAura}${compact ? ' is-compact' : ''}`} aria-hidden="true">
            <div className="ticket-number-aura__halo" />
            <div className="ticket-number-aura__spark ticket-number-aura__spark--top" />
            <div className="ticket-number-aura__spark ticket-number-aura__spark--side" />
            {resolvedNumberAura === 'spark-rare' ? !compact ? (
              <TicketSparklesAura />
            ) : (
              <>
                <div className="ticket-number-aura__spark ticket-number-aura__spark--extra" />
                <div className="ticket-number-aura__spark ticket-number-aura__spark--top-right" />
                <div className="ticket-number-aura__spark ticket-number-aura__spark--mid-left" />
                <div className="ticket-number-aura__spark ticket-number-aura__spark--bottom-right" />
                <div className="ticket-number-aura__spark ticket-number-aura__spark--bottom-left" />
              </>
            ) : null}
          </div>
        ) : null}
        <div
          style={{
            position: 'relative',
            width: CARD_WIDTH,
            height: CARD_HEIGHT,
            transformOrigin: 'top left',
            transform: resolvedScale === 1 ? 'none' : `scale(${resolvedScale})`,
            zIndex: 1,
          }}
        >
          {compact ? (
            <TicketFront
              ticket={ticket}
              onFlip={() => {}}
              metalEffect={metalEffect}
              eagerLoad={false}
              enableHolo={showHolo}
              holoVariant={holoVariant}
              pokeEffect={pokeEffect}
              paperEffect={paperEffect}
              numberAura={resolvedNumberAura}
            />
          ) : (
            <div
              ref={holoHostRef}
              className="ticket-holo-tilt"
              onMouseEnter={() => setHoloActive(true)}
              onMouseMove={(event) => updateHoloFromPointer(event.clientX, event.clientY)}
              onMouseLeave={() => {
                setHoloActive(false)
                const host = holoHostRef.current
                if (host) {
                  host.style.setProperty('--mx', '50%')
                  host.style.setProperty('--my', '50%')
                  host.style.setProperty('--rx', '0deg')
                  host.style.setProperty('--ry', '0deg')
                  host.style.setProperty('--posx', '50%')
                  host.style.setProperty('--posy', '50%')
                  host.style.setProperty('--dx', '0%')
                  host.style.setProperty('--dy', '0%')
                }
                const layer = holoLayerRef.current
                if (!layer) return
                layer.style.setProperty('--mx', '50%')
                layer.style.setProperty('--my', '50%')
                layer.style.setProperty('--rx', '0deg')
                layer.style.setProperty('--ry', '0deg')
                layer.style.setProperty('--posx', '50%')
                layer.style.setProperty('--posy', '50%')
              }}
            >
              <Tilt tiltMaxAngleX={10} tiltMaxAngleY={10} perspective={1200} scale={1.02} transitionSpeed={220} glareEnable={false}>
                <ReactCardFlip
                  isFlipped={flipped}
                  flipDirection="horizontal"
                  flipSpeedFrontToBack={0.7}
                  flipSpeedBackToFront={0.7}
                  containerStyle={{ width: CARD_WIDTH, height: CARD_HEIGHT }}
                >
                  <div key="front" style={{ width: CARD_WIDTH, height: CARD_HEIGHT }}>
                    <TicketFront
                      ticket={ticket}
                      onFlip={() => {}}
                      metalEffect={metalEffect}
                      eagerLoad
                      enableHolo={showHolo}
                      holoActive={holoActive}
                      holoVariant={holoVariant}
                      holoLayerRef={holoLayerRef}
                      pokeEffect={pokeEffect}
                      paperEffect={paperEffect}
                      numberAura={resolvedNumberAura}
                    />
                  </div>
                  <div key="back" style={{ width: CARD_WIDTH, height: CARD_HEIGHT }}>
                    <TicketBack ticket={ticket} onFlip={() => {}} numberAura={resolvedNumberAura} />
                  </div>
                </ReactCardFlip>
            </Tilt>
            </div>
          )}
      </div>
    </div>
  )
}

function CollectionCoverFlow({
  tickets,
  activeIndex,
  onActiveIndexChange,
  getEffect,
  getNumberAura,
}: {
  tickets: Ticket[]
  activeIndex: number
  onActiveIndexChange: (index: number) => void
  getEffect: (ticketId: string) => EffectType
  getNumberAura: (ticketId: string) => NumberAuraType
}) {
  const viewportRef = useRef<HTMLDivElement>(null)
  const [viewportWidth, setViewportWidth] = useState(360)

  useEffect(() => {
    const node = viewportRef.current
    if (!node) return

    const update = () => setViewportWidth(node.clientWidth || 360)
    update()

    const observer = new ResizeObserver(update)
    observer.observe(node)

    return () => observer.disconnect()
  }, [])

  const angleStep = tickets.length > 0 ? 360 / tickets.length : 0
  const normalizedIndex = mod(activeIndex, tickets.length)
  const baseRotation = -activeIndex * angleStep
  const radius = clamp(viewportWidth * 0.31, 112, 152)

  return (
    <div className="collection-carousel-shell">
      <div ref={viewportRef} className="collection-carousel-viewport">
        <motion.div
          className="collection-carousel-rotator"
          animate={{ rotateY: baseRotation }}
          transition={{ type: 'spring', stiffness: 120, damping: 20, mass: 0.9 }}
        >
          {tickets.map((ticket, index) => {
            const rawOffset = index - normalizedIndex
            const offset =
              tickets.length > 0
                ? rawOffset - Math.round(rawOffset / tickets.length) * tickets.length
                : rawOffset
            const absOffset = Math.abs(offset)
            const effect = getEffect(ticket.id)
            const numberAura = getNumberAura(ticket.id)
            const pokeEffect = isPokeEffect(effect) ? effect : undefined
            const paperEffect = isPaperEffect(effect) ? effect : undefined
            const armRotation = index * angleStep
            const isFocus = absOffset < 0.45

            const metalEffect =
              effect === 'gold-foil' || effect === 'silver-foil' || effect === 'rose-foil'
                ? effect
                : undefined

            return (
              <div
                key={ticket.id}
                className="collection-carousel-arm"
                style={{
                  transform: `rotateY(${armRotation}deg) translateZ(${radius}px)`,
                  zIndex: isFocus ? 20 : 10 - Math.round(absOffset),
                }}
              >
                <div className="collection-carousel-card-anchor">
                  <motion.div
                    className="collection-carousel-card"
                    initial={false}
                    animate={{
                      y: isFocus ? -6 : absOffset * 8 - 6,
                      scale: isFocus ? 1.02 : clamp(0.88 - absOffset * 0.12, 0.68, 0.88),
                      opacity: isFocus ? 1 : clamp(0.78 - absOffset * 0.22, 0.12, 0.78),
                      filter: `brightness(${isFocus ? 1.04 : clamp(0.95 - absOffset * 0.1, 0.72, 0.95)}) saturate(${isFocus ? 1.04 : clamp(0.98 - absOffset * 0.06, 0.84, 0.98)})`,
                    }}
                    transition={{ type: 'spring', stiffness: 180, damping: 24, mass: 0.7 }}
                    onClick={() => {
                      if (!isFocus) {
                        const delta = offset > 0 ? 1 : -1
                        onActiveIndexChange(activeIndex + delta)
                      }
                    }}
                    style={{ pointerEvents: isFocus || absOffset <= 1.2 ? 'auto' : 'none' }}
                  >
                    <div className={`collection-carousel-glow${isFocus ? ' is-active' : ''}`} />
                    <CollectibleTicketCard
                      ticket={ticket}
                      metalEffect={metalEffect}
                      compact={!isFocus}
                      displayScale={isFocus ? 0.8 : undefined}
                      holoVariant={!pokeEffect && !paperEffect && effect !== 'gold-foil' && effect !== 'silver-foil' && effect !== 'rose-foil' && effect !== 'none' ? (effect as HoloVariant) : 'rainbow'}
                      enableHolo={!pokeEffect && !paperEffect && effect !== 'gold-foil' && effect !== 'silver-foil' && effect !== 'rose-foil' && effect !== 'none'}
                      pokeEffect={pokeEffect}
                      paperEffect={paperEffect}
                      numberAura={numberAura}
                    />
                  </motion.div>
                </div>
              </div>
            )
          })}
        </motion.div>
      </div>
    </div>
  )
}

export function CollectionScreen() {
  const { navigate, navigateTab, tickets, goBack } = useApp()
  const collected = useMemo(() => tickets.filter((ticket) => ticket.status === 'USED'), [tickets])
  const [activeIndex, setActiveIndex] = useState(0)
  const [effectPickerOpen, setEffectPickerOpen] = useState(false)
  const [effectMap, setEffectMap] = useState<Record<string, EffectType>>({})
  const [numberAuraMap, setNumberAuraMap] = useState<Record<string, NumberAuraSetting>>({})

  const allEffects: { key: EffectType; label: string }[] = [
    { key: 'none', label: 'None' },
    { key: 'gold-foil', label: 'Gold Foil' },
    { key: 'silver-foil', label: 'Silver Foil' },
    { key: 'rose-foil', label: 'Rose Foil' },
    { key: 'paper-cotton', label: 'Cotton Paper' },
    { key: 'paper-crumpled', label: 'Crumpled Paper' },
    { key: 'rainbow', label: 'Rainbow' },
    { key: 'aurora', label: 'Aurora' },
    { key: 'prism', label: 'Prism' },
    { key: 'cosmos', label: 'Cosmos' },
    { key: 'sunset', label: 'Sunset' },
    { key: 'neon', label: 'Neon' },
    { key: 'poke-holo', label: 'Poke Holo' },
    { key: 'poke-galaxy', label: 'Galaxy' },
    { key: 'poke-v', label: 'Poke V' },
    { key: 'poke-vmax', label: 'VMAX' },
    { key: 'poke-vstar', label: 'VSTAR' },
    { key: 'poke-rainbow', label: 'Secret Rainbow' },
    { key: 'poke-secret', label: 'Secret Gold' },
    { key: 'poke-fullart', label: 'Full Art' },
    { key: 'poke-trainer', label: 'Trainer' },
    { key: 'poke-radiant', label: 'Radiant' },
    { key: 'poke-gallery-holo', label: 'Gallery Holo' },
    { key: 'poke-gallery-v', label: 'Gallery V' },
  ]
  const numberAuraOptions: { key: NumberAuraSetting; label: string }[] = [
    { key: 'auto', label: 'Auto' },
    { key: 'none', label: 'None' },
    { key: 'aura-rare', label: 'Aura Rare' },
    { key: 'milestone-rare', label: 'Milestone Rare' },
    { key: 'spark-rare', label: 'Spark Rare' },
  ]

  useEffect(() => {
    if (collected.length === 0) {
      setActiveIndex(0)
      return
    }

    setActiveIndex((prev) => mod(prev, collected.length))
  }, [collected.length])

  useEffect(() => {
    collected.forEach((ticket, index) => {
      void preloadPoster(ticket.poster, index < 3)
    })
  }, [collected])

  const getEffect = useCallback(
    (ticketId: string): EffectType => effectMap[ticketId] ?? getTicketEffect(ticketId),
    [effectMap]
  )
  const getResolvedNumberAura = useCallback(
    (ticketId: string): NumberAuraType => {
      const override = numberAuraMap[ticketId]
      return !override || override === 'auto' ? getNumberAura(ticketId) : override
    },
    [numberAuraMap]
  )

  const setTicketEffect = useCallback((ticketId: string, effect: EffectType) => {
    setEffectMap((prev) => ({ ...prev, [ticketId]: effect }))
  }, [])
  const setTicketNumberAura = useCallback((ticketId: string, aura: NumberAuraSetting) => {
    setNumberAuraMap((prev) => ({ ...prev, [ticketId]: aura }))
  }, [])

  const normalizedActiveIndex = mod(activeIndex, collected.length)
  const activeTicket = collected[normalizedActiveIndex] ?? null
  const handlePrev = useCallback(() => {
    setActiveIndex((prev) => prev - 1)
  }, [])

  const handleNext = useCallback(() => {
    setActiveIndex((prev) => prev + 1)
  }, [])

  return (
    <AppShell title="티켓 컬렉션">
      <div className="flex h-full flex-col overflow-hidden pb-24">
        {collected.length === 0 ? (
          <div className="flex flex-1 flex-col items-center justify-center gap-4 px-8 py-16 text-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-muted">
              <Music2 className="h-8 w-8 text-muted-foreground" />
            </div>
            <div>
              <p className="font-semibold text-foreground">No collected tickets yet</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Tickets marked as used will appear here as collectible cards.
              </p>
            </div>
            <button
              onClick={() => navigateTab('concerts')}
              className="gradient-border-button px-6 py-2.5 text-sm"
            >
              Browse concerts
            </button>
          </div>
        ) : (
          <div className="flex flex-1 flex-col px-4 pt-4">
            <div className="min-h-[108px] pt-10 text-center">
              {activeTicket && (
                <div className="flex items-start justify-center gap-2">
                  <button
                    type="button"
                    onClick={() => navigate('collectible-ticket-detail', { ticketId: activeTicket.id })}
                    className="text-left"
                  >
                    <h2 className="text-lg font-semibold leading-tight text-foreground underline-offset-4 transition hover:text-foreground hover:underline">
                      {activeTicket.eventName}
                    </h2>
                  </button>
                  <button
                    type="button"
                    aria-label="Open effect settings"
                    onClick={() => setEffectPickerOpen((prev) => !prev)}
                    className="gradient-border-icon-button mt-0.5 h-8 w-8 text-foreground/70 transition hover:text-foreground"
                  >
                    <Settings2 className="h-5 w-5 stroke-[1.8]" />
                  </button>
                </div>
              )}
            </div>
            {effectPickerOpen && activeTicket && (
              <div className="elevated-surface-soft mx-auto mb-4 flex w-full max-w-xl flex-col gap-3 rounded-2xl px-3 py-3 shadow-[0_12px_30px_rgba(15,23,42,0.08)] backdrop-blur">
                <div>
                  <p className="mb-2 px-1 text-[11px] font-semibold uppercase tracking-[0.16em] text-[#7a8491]">Ticket Effect</p>
                  <div className="flex flex-wrap justify-center gap-2">
                    {allEffects.map(({ key, label }) => {
                      const isActive = getEffect(activeTicket.id) === key
                      const isMetalBtn = key === 'gold-foil' || key === 'silver-foil' || key === 'rose-foil'
                      const isPaperBtn = key === 'paper-cotton' || key === 'paper-crumpled'
                      const metalBorder = key === 'gold-foil' ? '#cf9710' : key === 'silver-foil' ? '#9dabbf' : '#c7868d'
                      const metalBg = key === 'gold-foil' ? '#fff0bf' : key === 'silver-foil' ? '#edf2f8' : '#fdecef'
                      const metalText = key === 'gold-foil' ? '#8f5c00' : key === 'silver-foil' ? '#5f6e83' : '#9a5561'
                      const paperBorder = key === 'paper-cotton' ? '#d8ccb9' : '#c6baa7'
                      const paperBg = key === 'paper-cotton' ? '#f7f1e6' : '#efe7db'
                      const paperText = key === 'paper-cotton' ? '#7a6248' : '#6f6254'

                      return (
                        <button
                          key={key}
                          type="button"
                          onClick={() => setTicketEffect(activeTicket.id, key)}
                          className="rounded-full px-3 py-1.5 text-[11px] font-semibold transition"
                          style={{
                            border: isActive
                              ? isMetalBtn
                                ? `2px solid ${metalBorder}`
                                : isPaperBtn
                                  ? `2px solid ${paperBorder}`
                                  : '2px solid #d7dde6'
                              : '1px solid #d7d7d7',
                            background: isActive
                              ? isMetalBtn
                                ? metalBg
                                : isPaperBtn
                                  ? paperBg
                                    : '#ffffff'
                              : '#f5f5f5',
                            color: isActive
                              ? isMetalBtn
                                ? metalText
                                : isPaperBtn
                                  ? paperText
                                  : '#333333'
                              : '#777',
                          }}
                        >
                          {label}
                        </button>
                      )
                    })}
                  </div>
                </div>
                <div className="border-t border-[#edf1f4] pt-3">
                  <p className="mb-2 px-1 text-[11px] font-semibold uppercase tracking-[0.16em] text-[#7a8491]">Number Aura</p>
                  <div className="flex flex-wrap justify-center gap-2">
                    {numberAuraOptions.map(({ key, label }) => {
                      const isActive = (numberAuraMap[activeTicket.id] ?? 'auto') === key
                      return (
                        <button
                          key={key}
                          type="button"
                          onClick={() => setTicketNumberAura(activeTicket.id, key)}
                          className={`rounded-full px-3 py-1.5 text-[11px] font-semibold transition ${
                            isActive
                              ? 'gradient-border-button text-[#333333]'
                              : 'border border-[#d7d7d7] bg-[#f5f5f5] text-[#777777]'
                          }`}
                        >
                          {label}
                        </button>
                      )
                    })}
                  </div>
                </div>
              </div>
            )}
            <div className="mx-auto grid w-full max-w-[640px] grid-cols-[48px_minmax(0,1fr)_48px] items-start gap-2 pt-2">
              <button
                type="button"
                aria-label="Previous ticket"
                onClick={handlePrev}
                className="gradient-border-icon-button mt-[220px] flex h-14 w-12 items-center justify-center text-foreground/85 transition hover:text-foreground"
              >
                <ChevronLeft className="h-10 w-10 stroke-[1.6]" />
              </button>
              <div className="min-w-0">
                <CollectionCoverFlow
                  tickets={collected}
                  activeIndex={activeIndex}
                  onActiveIndexChange={setActiveIndex}
                  getEffect={getEffect}
                  getNumberAura={getResolvedNumberAura}
                />
              </div>
              <button
                type="button"
                aria-label="Next ticket"
                onClick={handleNext}
                className="gradient-border-icon-button mt-[220px] flex h-14 w-12 items-center justify-center text-foreground/85 transition hover:text-foreground"
              >
                <ChevronRight className="h-10 w-10 stroke-[1.6]" />
              </button>
            </div>
          </div>
        )}
      </div>
    </AppShell>
  )
}
