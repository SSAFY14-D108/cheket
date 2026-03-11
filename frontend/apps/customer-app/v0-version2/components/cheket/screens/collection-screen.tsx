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

type EffectType = 'none' | 'gold' | HoloVariant | PokeEffect

interface FaceProps {
  ticket: Ticket
  onFlip: () => void
  isGold?: boolean
  holoActive?: boolean
  holoVariant?: HoloVariant
  holoLayerRef?: React.RefObject<HTMLDivElement | null>
  enableHolo?: boolean
  pokeEffect?: PokeEffect
}

const GOLD_FOIL_BASE = {
  backgroundColor: '#d5b45a',
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

function getTicketEffect(ticketId: string): EffectType {
  const num = Number(ticketId.replace(/\D/g, '')) || 0
  return HOLO_VARIANTS[num % HOLO_VARIANTS.length]
}

function isPokeEffect(effect: EffectType): effect is PokeEffect {
  return effect !== 'none' && effect.startsWith('poke-')
}

function GoldFoilLayer() {
  return (
    <>
      <div className="gold-foil-base" />
      <div className="gold-foil-grain" />
      <div className="gold-foil-sheen" />
    </>
  )
}

function TicketFront({
  ticket,
  onFlip,
  isGold = false,
  holoActive = false,
  holoVariant = 'rainbow',
  holoLayerRef,
  enableHolo = false,
  pokeEffect,
}: FaceProps) {
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
          ...(isGold ? GOLD_FOIL_BASE : { background: '#0b0f1a' }),
        }}
      >
        <div style={{ position: 'relative', width: '100%', height: '100%', zIndex: 3 }}>
          <Image
            src={ticket.poster}
            alt={ticket.eventName}
            fill
            sizes={`${CARD_WIDTH}px`}
            style={{ objectFit: 'cover', opacity: isGold ? 0.7 : 1 }}
          />
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
              background: isGold
                ? 'linear-gradient(to top, rgba(49,34,8,0.58) 10%, rgba(49,34,8,0.15) 48%, rgba(255,234,180,0.16) 100%)'
                : 'linear-gradient(to top, rgba(11,15,26,0.82) 12%, rgba(11,15,26,0.2) 52%, rgba(11,15,26,0.08) 100%)',
            }}
          />
          <div
            style={{
              position: 'absolute',
              inset: 0,
              background: 'linear-gradient(to bottom, rgba(0,0,0,0.2) 0%, transparent 30%)',
            }}
          />
          {isGold && (
            <div style={{ position: 'absolute', inset: 0, zIndex: 2, opacity: 0.55, mixBlendMode: 'overlay' }}>
              <GoldFoilLayer />
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function TicketBack({ ticket, onFlip, isGold = false }: FaceProps) {
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
          ...(isGold ? GOLD_FOIL_BASE : { background: '#0b0f1a' }),
        }}
      >
        {isGold && <GoldFoilLayer />}

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
            background: isGold
              ? 'linear-gradient(to bottom, rgba(70,44,14,0.66) 0%, rgba(55,36,11,0.74) 100%)'
              : 'linear-gradient(to bottom, rgba(9,13,24,0.9) 0%, rgba(9,13,24,0.93) 100%)',
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
            color: isGold ? '#f8efd0' : 'rgba(255,255,255,0.9)',
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
            <BackRow label="Date" value={ticket.attendedDate ?? ticket.eventDate.split(' ')[0]} isGold={isGold} />
            <BackRow label="Venue" value={ticket.venue} isGold={isGold} />
          </div>

          <div style={{ borderTop: '1px solid rgba(255,255,255,0.22)', marginTop: 18, paddingTop: 14 }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
              <SeatBox label="Grade" value={ticket.grade} accent={isGold ? undefined : grade.bg} isGold={isGold} />
              <SeatBox label="Seat" value={ticket.seatLabel} accent={isGold ? undefined : '#f8e28a'} isGold={isGold} />
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

function BackRow({ label, value, isGold = false }: { label: string; value: string; isGold?: boolean }) {
  return (
    <div style={{ display: 'flex', gap: 10, alignItems: 'baseline' }}>
      <span
        style={{
          fontSize: 9,
          letterSpacing: '0.12em',
          textTransform: 'uppercase',
          color: isGold ? 'rgba(24,20,10,0.58)' : 'rgba(255,255,255,0.3)',
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
          color: isGold ? 'rgba(24,20,10,0.74)' : 'rgba(255,255,255,0.75)',
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
  isGold = false,
}: {
  label: string
  value: string
  accent?: string
  isGold?: boolean
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
        backgroundColor: isGold ? 'rgba(20,18,10,0.07)' : accent ? `${accent}20` : 'rgba(255,255,255,0.04)',
        border: `1px solid ${isGold ? 'rgba(20,18,10,0.2)' : accent ? `${accent}40` : 'rgba(255,255,255,0.07)'}`,
      }}
    >
      <span
        style={{
          fontSize: 9,
          textTransform: 'uppercase',
          letterSpacing: '0.15em',
          color: isGold ? 'rgba(24,20,10,0.54)' : 'rgba(255,255,255,0.3)',
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
          color: isGold ? '#231b09' : accent ?? 'rgba(255,255,255,0.82)',
        }}
      >
        {value}
      </span>
    </div>
  )
}

function CollectibleTicketCard({
  ticket,
  isGold = false,
  compact = false,
  onOpen,
  holoVariant = 'rainbow',
  enableHolo,
  pokeEffect,
  displayScale,
}: {
  ticket: Ticket
  isGold?: boolean
  compact?: boolean
  onOpen?: () => void
  holoVariant?: HoloVariant
  enableHolo?: boolean
  pokeEffect?: PokeEffect
  displayScale?: number
}) {
  const showHolo = enableHolo ?? !isGold
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
            isGold={isGold}
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
                    isGold={isGold}
                    enableHolo={showHolo}
                    holoActive={holoActive}
                    holoVariant={holoVariant}
                    holoLayerRef={holoLayerRef}
                    pokeEffect={pokeEffect}
                  />
                </div>
                <div key="back" style={{ width: CARD_WIDTH, height: CARD_HEIGHT }}>
                  <TicketBack ticket={ticket} onFlip={() => {}} isGold={isGold} />
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
                      compact={!isFocus}
                      displayScale={isFocus ? 0.8 : undefined}
                      isGold={effect === 'gold'}
                      holoVariant={!pokeEffect && effect !== 'gold' && effect !== 'none' ? (effect as HoloVariant) : 'rainbow'}
                      enableHolo={!pokeEffect && effect !== 'gold' && effect !== 'none'}
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
  const { navigateTab, tickets } = useApp()
  const collected = useMemo(() => tickets.filter((ticket) => ticket.status === 'USED'), [tickets])
  const [activeIndex, setActiveIndex] = useState(0)
  const [effectPickerOpen, setEffectPickerOpen] = useState(false)
  const [effectMap, setEffectMap] = useState<Record<string, EffectType>>({})

  const allEffects: { key: EffectType; label: string }[] = [
    { key: 'none', label: 'None' },
    { key: 'gold', label: 'Gold' },
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
    <AppShell title="컬렉션">
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
                  <h2 className="text-lg font-semibold leading-tight text-foreground">{activeTicket.eventName}</h2>
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
                  const isGoldBtn = key === 'gold'

                  return (
                    <button
                      key={key}
                      type="button"
                      onClick={() => setTicketEffect(activeTicket.id, key)}
                      className="rounded-full px-3 py-1.5 text-[11px] font-semibold transition"
                      style={{
                        border: isActive
                          ? isGoldBtn
                            ? '2px solid #d5b45a'
                            : '2px solid #00c598'
                          : '1px solid #d7d7d7',
                        background: isActive ? (isGoldBtn ? '#fdf3d7' : '#e6faf5') : '#f5f5f5',
                        color: isActive ? (isGoldBtn ? '#9a7b2a' : '#00a37d') : '#777',
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
