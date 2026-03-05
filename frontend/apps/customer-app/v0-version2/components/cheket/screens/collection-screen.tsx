'use client'

import { useState, useCallback, useEffect, useRef, type CSSProperties } from 'react'
import Image from 'next/image'
import ReactCardFlip from 'react-card-flip'
import Tilt from 'react-parallax-tilt'
import { useApp } from '@/lib/app-context'
import { MOCK_TICKETS } from '@/lib/mock-data'
import { Ticket } from '@/lib/types'
import { AppShell } from '../app-shell'
import { Music2 } from 'lucide-react'

// Vertical ticket size including scallop edges
const CARD_WIDTH = 272
const CARD_HEIGHT = 460
const CARD_COMPACT_WIDTH = 152
const CARD_COMPACT_HEIGHT = 258
const CARD_COMPACT_SCALE = CARD_COMPACT_WIDTH / CARD_WIDTH
const SCALLOP_H = 8
const BODY_H = CARD_HEIGHT - SCALLOP_H * 2

type HoloVariant = 'rainbow' | 'aurora' | 'prism' | 'cosmos' | 'sunset' | 'neon'
const HOLO_VARIANTS: HoloVariant[] = ['rainbow', 'aurora', 'prism', 'cosmos', 'sunset', 'neon']

const GOLD_FOIL_BASE = {
  backgroundColor: '#d5b45a',
}

const GRADE_COLORS: Record<string, { bg: string; text: string }> = {
  VIP:       { bg: '#f59e0b', text: '#000' },
  'VIP PIT': { bg: '#f59e0b', text: '#000' },
  R:         { bg: '#ef4444', text: '#fff' },
  FLOOR:     { bg: '#3b82f6', text: '#fff' },
  'GA PIT':  { bg: '#ef4444', text: '#fff' },
  S:         { bg: '#3b82f6', text: '#fff' },
  A:         { bg: '#22c55e', text: '#fff' },
  STAND:     { bg: '#22c55e', text: '#fff' },
  '2F':      { bg: '#a855f7', text: '#fff' },
  '1F':      { bg: '#06b6d4', text: '#fff' },
  GA:        { bg: '#3b82f6', text: '#fff' },
}

function getGrade(grade: string) {
  return GRADE_COLORS[grade] ?? { bg: '#6b7280', text: '#fff' }
}

// Scallop edge ??half-circles punched out of the top or bottom edge
function ScallopEdge({ position, pageBg }: { position: 'top' | 'bottom'; pageBg: string }) {
  const r = 6
  const count = 16
  const W = count * r * 2

  const circles = Array.from({ length: count }).map((_, i) => {
    const cx = i * r * 2 + r
    return `M ${cx - r} ${position === 'top' ? 0 : r * 2} a ${r} ${r} 0 0 ${position === 'top' ? 1 : 0} ${r * 2} 0`
  })

  const path =
    position === 'top'
      ? `M0,0 ${circles.join(' ')} L${W},0 L${W},${r} L0,${r} Z`
      : `M0,${r} ${circles.join(' ')} L${W},${r * 2} L0,${r * 2} Z`

  return (
    <svg
      viewBox={`0 0 ${W} ${r * 2}`}
      style={{ width: '100%', height: r * 2, display: 'block' }}
      preserveAspectRatio="none"
    >
      <rect width={W} height={r * 2} fill="#0b0f1a" />
      <path d={path} fill={pageBg} />
    </svg>
  )
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

interface FaceProps {
  ticket: Ticket
  pageBg: string
  onFlip: () => void
  isGold?: boolean
  holoActive?: boolean
  holoVariant?: HoloVariant
  holoLayerRef?: React.RefObject<HTMLDivElement | null>
  enableHolo?: boolean
}

// ?? FRONT FACE ??????????????????????????????????????????????????????????????
function TicketFront({
  ticket,
  pageBg,
  onFlip,
  isGold = false,
  holoActive = false,
  holoVariant = 'rainbow',
  holoLayerRef,
  enableHolo = false,
}: FaceProps) {
  const ticketNo = `No.${ticket.id.replace('tkt_', '').padStart(4, '0')}`

  return (
    <div
      style={{ display: 'flex', flexDirection: 'column', height: CARD_HEIGHT, cursor: 'pointer' }}
      onClick={onFlip}
    >
      <ScallopEdge position="top" pageBg={pageBg} />

      <div style={{ height: BODY_H, position: 'relative', overflow: 'visible' }}>
        <div
          style={{
            position: 'absolute',
            inset: 0,
            overflow: 'hidden',
            ...(isGold ? GOLD_FOIL_BASE : { background: '#0b0f1a' }),
          }}
        >
          {isGold && <GoldFoilLayer />}

          <div style={{ position: 'relative', width: '100%', height: '100%', zIndex: 3 }}>
            <Image
              src={ticket.poster}
              alt={ticket.eventName}
              fill
              sizes={`${CARD_WIDTH}px`}
              style={{ objectFit: 'cover' }}
            />
            {enableHolo && (
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

            <div style={{ position: 'absolute', left: 14, right: 14, top: 12, zIndex: 4 }}>
              <p
                style={{
                  fontSize: 8,
                  letterSpacing: '0.26em',
                  textTransform: 'uppercase',
                  color: isGold ? 'rgba(24,20,10,0.72)' : 'rgba(255,255,255,0.82)',
                  fontWeight: 700,
                }}
              >
                Concert Ticket
              </p>
            </div>

            <div style={{ position: 'absolute', left: 14, right: 14, top: 44, zIndex: 4 }}>
              <h3
                style={{
                  fontSize: 42,
                  fontWeight: 900,
                  lineHeight: 0.9,
                  letterSpacing: '-0.03em',
                  color: isGold ? 'rgba(125,26,26,0.86)' : 'rgba(255,255,255,0.95)',
                  textTransform: 'uppercase',
                }}
              >
                CHEKET
              </h3>
              <p
                style={{
                  marginTop: 3,
                  fontSize: 18,
                  fontWeight: 800,
                  lineHeight: 1.06,
                  color: isGold ? 'rgba(100,22,22,0.84)' : 'rgba(255,255,255,0.95)',
                  textTransform: 'uppercase',
                }}
              >
                {ticket.eventName}
              </p>
            </div>

            <div
              style={{
                position: 'absolute',
                left: 14,
                right: 14,
                bottom: 12,
                zIndex: 4,
                display: 'flex',
                alignItems: 'flex-end',
                justifyContent: 'space-between',
              }}
            >
              <div>
                <p
                  style={{
                    fontSize: 7.5,
                    letterSpacing: '0.24em',
                    textTransform: 'uppercase',
                    color: isGold ? 'rgba(24,20,10,0.62)' : 'rgba(255,255,255,0.7)',
                    fontWeight: 700,
                  }}
                >
                  CHEKET
                </p>
                <p
                  style={{
                    fontFamily: 'monospace',
                    fontSize: 9,
                    color: isGold ? 'rgba(24,20,10,0.7)' : 'rgba(255,255,255,0.82)',
                  }}
                >
                  {ticketNo}
                </p>
              </div>
              <p
                style={{
                  fontSize: 8,
                  letterSpacing: '0.12em',
                  color: isGold ? 'rgba(24,20,10,0.58)' : 'rgba(255,255,255,0.68)',
                }}
              >
                TAP
              </p>
            </div>
          </div>
        </div>
      </div>

      <ScallopEdge position="bottom" pageBg={pageBg} />
    </div>
  )
}

function TicketBack({ ticket, pageBg, onFlip, isGold = false }: FaceProps) {
  const ticketNo = `No.${ticket.id.replace('tkt_', '').padStart(4, '0')}`
  const g = getGrade(ticket.grade)

  return (
    <div
      style={{ display: 'flex', flexDirection: 'column', height: CARD_HEIGHT, cursor: 'pointer' }}
      onClick={onFlip}
    >
      <ScallopEdge position="top" pageBg={pageBg} />

      <div style={{ height: BODY_H, position: 'relative', overflow: 'visible' }}>
        <div
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
              padding: '12px 14px',
              display: 'flex',
              flexDirection: 'column',
              color: isGold ? '#f8efd0' : 'rgba(255,255,255,0.9)',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <p style={{ fontSize: 8, letterSpacing: '0.2em', textTransform: 'uppercase', opacity: 0.9 }}>
                IU WORLD TOUR CONCERT
              </p>
              <p style={{ fontSize: 8, fontFamily: 'monospace', opacity: 0.9 }}>{ticketNo}</p>
            </div>

            <div style={{ marginTop: 10 }}>
              <p style={{ fontSize: 10, fontWeight: 900, lineHeight: 1.15, textTransform: 'uppercase' }}>
                {ticket.eventName}
              </p>
            </div>

            <div style={{ borderTop: '1px solid rgba(255,255,255,0.28)', marginTop: 10, paddingTop: 10, display: 'grid', gap: 7 }}>
              <BackRow label="Date" value={ticket.eventDate} isGold={isGold} />
              <BackRow label="Venue" value={ticket.venue} isGold={isGold} />
            </div>

            <div style={{ borderTop: '1px solid rgba(255,255,255,0.22)', marginTop: 10, paddingTop: 10 }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6 }}>
                <SeatBox label="Grade" value={ticket.grade} accent={isGold ? undefined : g.bg} isGold={isGold} />
                <SeatBox label="Seat" value={ticket.seatLabel} accent={isGold ? undefined : '#f8e28a'} isGold={isGold} />
              </div>
            </div>

            <div style={{ marginTop: 'auto', borderTop: '1px dashed rgba(255,255,255,0.24)', paddingTop: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <p style={{ fontSize: 16, fontWeight: 900 }}>
                {ticket.originalPrice.toLocaleString()}
                <span style={{ fontSize: 9, marginLeft: 3, opacity: 0.8 }}>원</span>
              </p>
              <p style={{ fontSize: 8, letterSpacing: '0.12em', opacity: 0.8 }}>TAP</p>
            </div>
          </div>
        </div>
      </div>

      <ScallopEdge position="bottom" pageBg={pageBg} />
    </div>
  )
}

function BackRow({ label, value, isGold = false }: { label: string; value: string; isGold?: boolean }) {
  return (
    <div style={{ display: 'flex', gap: 8, alignItems: 'baseline' }}>
      <span style={{
        fontSize: 6.5, letterSpacing: '0.12em', textTransform: 'uppercase',
        color: isGold ? 'rgba(24,20,10,0.58)' : 'rgba(255,255,255,0.22)', fontWeight: 700, minWidth: 20, flexShrink: 0,
      }}>{label}</span>
      <span style={{
        fontSize: 9, fontWeight: 600, color: isGold ? 'rgba(24,20,10,0.74)' : 'rgba(255,255,255,0.65)',
        lineHeight: 1.3, wordBreak: 'keep-all',
      }}>{value}</span>
    </div>
  )
}

function SeatBox({ label, value, accent, isGold = false }: {
  label: string; value: string; accent?: string; isGold?: boolean
}) {
  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center',
      gap: 3, paddingTop: 5, paddingBottom: 5, borderRadius: 7,
      backgroundColor: isGold ? 'rgba(20,18,10,0.07)' : accent ? `${accent}20` : 'rgba(255,255,255,0.04)',
      border: `1px solid ${isGold ? 'rgba(20,18,10,0.2)' : accent ? `${accent}40` : 'rgba(255,255,255,0.07)'}`,
    }}>
      <span style={{
        fontSize: 6, textTransform: 'uppercase', letterSpacing: '0.15em',
        color: isGold ? 'rgba(24,20,10,0.54)' : 'rgba(255,255,255,0.25)', fontWeight: 600,
      }}>{label}</span>
      <span style={{
        fontSize: 10, fontWeight: 800, lineHeight: 1,
        color: isGold ? '#231b09' : accent ? accent : 'rgba(255,255,255,0.82)',
      }}>{value}</span>
    </div>
  )
}

// ?? HOLOGRAPHIC OVERLAY ??????????????????????????????????????????????????????
function CollectibleTicketCard({
  ticket,
  isGold = false,
  compact = false,
  onOpen,
  holoVariant = 'rainbow',
}: {
  ticket: Ticket
  isGold?: boolean
  compact?: boolean
  onOpen?: () => void
  holoVariant?: HoloVariant
}) {
  const [flipped, setFlipped] = useState(false)
  const [holoActive, setHoloActive] = useState(false)
  const holoHostRef = useRef<HTMLDivElement>(null)
  const holoLayerRef = useRef<HTMLDivElement>(null)
  const holoRafRef = useRef<number | null>(null)
  const pageBg = '#FAFAFA'

  // Toggle flip on click - handle at the wrapper level
  const handleClick = useCallback(() => {
    if (compact) {
      onOpen?.()
      return
    }
    setFlipped(prev => !prev)
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

    const x = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width))
    const y = Math.max(0, Math.min(1, (clientY - rect.top) / rect.height))
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
    })
  }, [])

  return (
    <div
      style={{
        width: compact ? CARD_COMPACT_WIDTH : `min(100%, ${CARD_WIDTH}px)`,
        height: compact ? CARD_COMPACT_HEIGHT : CARD_HEIGHT,
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
          transform: compact ? `scale(${CARD_COMPACT_SCALE})` : 'none',
        }}
      >
        {compact ? (
          <TicketFront ticket={ticket} pageBg={pageBg} onFlip={() => {}} isGold={isGold} />
        ) : (
          <div
            ref={holoHostRef}
            className="ticket-holo-tilt"
            onMouseEnter={() => setHoloActive(true)}
            onMouseMove={(e) => updateHoloFromPointer(e.clientX, e.clientY)}
            onMouseLeave={() => {
              setHoloActive(false)
              const layer = holoLayerRef.current
              if (!layer) return
              layer.style.setProperty('--mx', '50%')
              layer.style.setProperty('--my', '50%')
              layer.style.setProperty('--rx', '0deg')
              layer.style.setProperty('--ry', '0deg')
            }}
          >
            <Tilt
              tiltMaxAngleX={10}
              tiltMaxAngleY={10}
              perspective={1200}
              scale={1.02}
              transitionSpeed={220}
              glareEnable={false}
            >
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
                  pageBg={pageBg}
                  onFlip={() => {}}
                  isGold={isGold}
                  enableHolo
                  holoActive={holoActive}
                  holoVariant={holoVariant}
                  holoLayerRef={holoLayerRef}
                />
              </div>
              <div key="back" style={{ width: CARD_WIDTH, height: CARD_HEIGHT }}>
                <TicketBack ticket={ticket} pageBg={pageBg} onFlip={() => {}} isGold={isGold} />
              </div>
            </ReactCardFlip>
            </Tilt>
          </div>
        )}
      </div>
    </div>
  )
}

// ?? SCREEN ???????????????????????????????????????????????????????????????????
export function CollectionScreen() {
  const { navigateTab } = useApp()
  const collected = MOCK_TICKETS.filter((t) => t.status === 'USED')
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null)
  const getVariant = (ticket: Ticket) => {
    const num = Number(ticket.id.replace(/\D/g, '')) || 0
    return HOLO_VARIANTS[num % HOLO_VARIANTS.length]
  }

  return (
    <AppShell
      title="컬렉션"
      rightElement={
        <span className="text-xs text-muted-foreground font-medium">{collected.length}장</span>
      }
    >
      <div className="flex flex-col h-full overflow-y-auto pb-24">
        <div className="px-4 pt-4 pb-2">
          <p className="text-xs text-muted-foreground leading-relaxed">
            관람한 공연의 소장 티켓을 모아보세요. 카드를 탭하면 상세 정보를 볼 수 있어요.
          </p>
        </div>

        {collected.length === 0 ? (
          <div className="flex flex-col items-center justify-center flex-1 gap-4 px-8 py-16">
            <div className="w-16 h-16 rounded-2xl bg-muted flex items-center justify-center">
              <Music2 className="w-8 h-8 text-muted-foreground" />
            </div>
            <div className="text-center">
              <p className="font-semibold text-foreground">아직 컬렉션이 없어요</p>
              <p className="text-sm text-muted-foreground mt-1">
                공연을 관람하면 티켓이 여기에 모입니다
              </p>
            </div>
            <button
              onClick={() => navigateTab('concerts')}
              className="px-6 py-2.5 rounded-xl bg-primary text-primary-foreground text-sm font-semibold"
            >
              공연 둘러보기
            </button>
          </div>
        ) : (
          <div className="px-4 pt-2 grid grid-cols-2 gap-3 justify-items-center">
            {collected.map((ticket, idx) => (
              <CollectibleTicketCard
                key={ticket.id}
                ticket={ticket}
                isGold={idx === 0}
                compact
                holoVariant={getVariant(ticket)}
                onOpen={() => setSelectedTicket(ticket)}
              />
            ))}
          </div>
        )}
      </div>

      {selectedTicket && (
        <div
          className="fixed inset-0 z-[60] bg-black/70 flex items-center justify-center p-4"
          onClick={() => setSelectedTicket(null)}
        >
          <div
            className="relative w-full max-w-sm flex flex-col items-center gap-3"
            onClick={(e) => e.stopPropagation()}
          >
            <CollectibleTicketCard
              ticket={selectedTicket}
              isGold={selectedTicket.id === collected[0]?.id}
              holoVariant={getVariant(selectedTicket)}
            />
            <button
              type="button"
              onClick={() => setSelectedTicket(null)}
              className="px-4 py-2 rounded-lg bg-white text-black text-sm font-semibold"
            >
              닫기
            </button>
          </div>
        </div>
      )}
    </AppShell>
  )
}



