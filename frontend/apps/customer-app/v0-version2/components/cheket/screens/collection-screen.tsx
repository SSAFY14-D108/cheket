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

type EffectType = 'none' | 'gold-foil' | 'silver-foil' | 'rose-foil' | HoloVariant | PokeEffect
type MetalEffect = 'gold-foil' | 'silver-foil' | 'rose-foil'

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

function isPokeEffect(effect: EffectType): effect is PokeEffect {
  return effect !== 'none' && effect.startsWith('poke-')
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
}: FaceProps) {
  const isMetal = metalEffect === 'gold-foil' || metalEffect === 'silver-foil' || metalEffect === 'rose-foil'
  const metallicClass = metalEffect ? `ticket-metallic-${metalEffect}` : ''
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
                    ? 'sepia(0.82) hue-rotate(-12deg) saturate(1.32) contrast(1.08) brightness(0.95)'
                    : metalEffect === 'silver-foil'
                      ? 'grayscale(0.32) saturate(0.86) contrast(1.1) brightness(1.02) hue-rotate(4deg)'
                      : metalEffect === 'rose-foil'
                        ? 'sepia(0.56) hue-rotate(-24deg) saturate(1.18) contrast(1.06) brightness(0.97)'
                        : 'none',
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
                  ? 'linear-gradient(to top, rgba(98,62,0,0.34) 0%, rgba(98,62,0,0.14) 22%, rgba(255,229,120,0.06) 62%, rgba(255,244,195,0) 100%)'
                  : metalEffect === 'silver-foil'
                    ? 'linear-gradient(to top, rgba(45,57,74,0.24) 0%, rgba(45,57,74,0.1) 20%, rgba(230,238,248,0.06) 58%, rgba(246,249,252,0) 100%)'
                    : metalEffect === 'rose-foil'
                      ? 'linear-gradient(to top, rgba(110,58,46,0.28) 0%, rgba(110,58,46,0.12) 20%, rgba(255,214,204,0.05) 58%, rgba(255,242,236,0) 100%)'
                    : 'linear-gradient(to top, rgba(11,15,26,0.82) 12%, rgba(11,15,26,0.2) 52%, rgba(11,15,26,0.08) 100%)',
            }}
          />
          <div
            style={{
              position: 'absolute',
              inset: 0,
              background:
                metalEffect === 'gold-foil'
                  ? 'linear-gradient(to bottom, rgba(255,246,190,0.2) 0%, rgba(255,246,190,0) 30%)'
                  : metalEffect === 'silver-foil'
                    ? 'linear-gradient(to bottom, rgba(248,251,255,0.22) 0%, rgba(248,251,255,0) 30%)'
                    : metalEffect === 'rose-foil'
                      ? 'linear-gradient(to bottom, rgba(255,232,224,0.22) 0%, rgba(255,232,224,0) 30%)'
                    : 'linear-gradient(to bottom, rgba(0,0,0,0.2) 0%, transparent 30%)',
            }}
          />
        </div>
      </div>
    </div>
  )
}

function TicketBack({ ticket, onFlip }: FaceProps) {
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
  displayScale,
}: {
  ticket: Ticket
  metalEffect?: MetalEffect
  compact?: boolean
  onOpen?: () => void
  holoVariant?: HoloVariant
  enableHolo?: boolean
  pokeEffect?: PokeEffect
  displayScale?: number
}) {
  const showHolo = enableHolo ?? true
  const [flipped, setFlipped] = useState(false)
  const [holoActive, setHoloActive] = useState(false)
  const holoHostRef = useRef<HTMLDivElement>(null)
  const holoLayerRef = useRef<HTMLDivElement>(null)
  const holoRafRef = useRef<number | null>(null)

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
      }}
      onClick={handleClick}
    >
        <div
          style={{
            position: 'relative',
            width: CARD_WIDTH,
            height: CARD_HEIGHT,
            transformOrigin: 'top left',
            transform: resolvedScale === 1 ? 'none' : `scale(${resolvedScale})`,
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
                  />
                </div>
                <div key="back" style={{ width: CARD_WIDTH, height: CARD_HEIGHT }}>
                  <TicketBack ticket={ticket} onFlip={() => {}} />
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
}: {
  tickets: Ticket[]
  activeIndex: number
  onActiveIndexChange: (index: number) => void
  getEffect: (ticketId: string) => EffectType
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
            const pokeEffect = isPokeEffect(effect) ? effect : undefined
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
                      holoVariant={!pokeEffect && effect !== 'gold-foil' && effect !== 'silver-foil' && effect !== 'rose-foil' && effect !== 'none' ? (effect as HoloVariant) : 'rainbow'}
                      enableHolo={!pokeEffect && effect !== 'gold-foil' && effect !== 'silver-foil' && effect !== 'rose-foil' && effect !== 'none'}
                      pokeEffect={pokeEffect}
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
  const { navigate, navigateTab, tickets } = useApp()
  const collected = useMemo(() => tickets.filter((ticket) => ticket.status === 'USED'), [tickets])
  const [activeIndex, setActiveIndex] = useState(0)
  const [effectPickerOpen, setEffectPickerOpen] = useState(false)
  const [effectMap, setEffectMap] = useState<Record<string, EffectType>>({})

  const allEffects: { key: EffectType; label: string }[] = [
    { key: 'none', label: 'None' },
    { key: 'gold-foil', label: 'Gold Foil' },
    { key: 'silver-foil', label: 'Silver Foil' },
    { key: 'rose-foil', label: 'Rose Foil' },
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

  const setTicketEffect = useCallback((ticketId: string, effect: EffectType) => {
    setEffectMap((prev) => ({ ...prev, [ticketId]: effect }))
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
              className="rounded-xl bg-primary px-6 py-2.5 text-sm font-semibold text-primary-foreground"
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
                    onClick={() => navigate('ticket-detail', { ticketId: activeTicket.id })}
                    className="text-left"
                  >
                    <h2 className="text-lg font-semibold leading-tight text-foreground underline-offset-4 transition hover:text-primary hover:underline">
                      {activeTicket.eventName}
                    </h2>
                  </button>
                  <button
                    type="button"
                    aria-label="Open effect settings"
                    onClick={() => setEffectPickerOpen((prev) => !prev)}
                    className="mt-0.5 text-foreground/70 transition hover:text-foreground"
                  >
                    <Settings2 className="h-5 w-5 stroke-[1.8]" />
                  </button>
                </div>
              )}
            </div>
            {effectPickerOpen && activeTicket && (
              <div className="mx-auto mb-4 flex w-full max-w-xl flex-wrap justify-center gap-2 rounded-2xl bg-white/75 px-3 py-3 shadow-[0_12px_30px_rgba(15,23,42,0.08)] backdrop-blur">
                {allEffects.map(({ key, label }) => {
                  const isActive = getEffect(activeTicket.id) === key
                  const isMetalBtn = key === 'gold-foil' || key === 'silver-foil' || key === 'rose-foil'
                  const metalBorder = key === 'gold-foil' ? '#cf9710' : key === 'silver-foil' ? '#9dabbf' : '#c7868d'
                  const metalBg = key === 'gold-foil' ? '#fff0bf' : key === 'silver-foil' ? '#edf2f8' : '#fdecef'
                  const metalText = key === 'gold-foil' ? '#8f5c00' : key === 'silver-foil' ? '#5f6e83' : '#9a5561'

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
                            : '2px solid #00c598'
                          : '1px solid #d7d7d7',
                        background: isActive ? (isMetalBtn ? metalBg : '#e6faf5') : '#f5f5f5',
                        color: isActive ? (isMetalBtn ? metalText : '#00a37d') : '#777',
                      }}
                    >
                      {label}
                    </button>
                  )
                })}
              </div>
            )}
            <div className="mx-auto grid w-full max-w-[640px] grid-cols-[48px_minmax(0,1fr)_48px] items-start gap-2 pt-2">
              <button
                type="button"
                aria-label="Previous ticket"
                onClick={handlePrev}
                className="mt-[220px] flex h-14 w-12 items-center justify-center text-foreground/85 transition hover:text-foreground"
              >
                <ChevronLeft className="h-10 w-10 stroke-[1.6]" />
              </button>
              <div className="min-w-0">
                <CollectionCoverFlow
                  tickets={collected}
                  activeIndex={activeIndex}
                  onActiveIndexChange={setActiveIndex}
                  getEffect={getEffect}
                />
              </div>
              <button
                type="button"
                aria-label="Next ticket"
                onClick={handleNext}
                className="mt-[220px] flex h-14 w-12 items-center justify-center text-foreground/85 transition hover:text-foreground"
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
