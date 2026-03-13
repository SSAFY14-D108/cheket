'use client'

import { useEffect, useRef, useState } from 'react'
import Image from 'next/image'
import {
  BadgeInfo,
  Calendar,
  ChevronRight,
  Clock3,
  Heart,
  ImageIcon,
  MapPin,
  Receipt,
  UserRound,
  Users,
} from 'lucide-react'
import { useApp } from '@/lib/app-context'
import type { Event } from '@/lib/types'
import { AppShell } from '../app-shell'
import { EventStatusBadge } from '../status-badge'

function needsKopisDetail(event: Event) {
  return (
    event.id.startsWith('kopis_') &&
    (!event.priceInfo ||
      !event.runtime ||
      !event.ageRating ||
      !(event.introImages && event.introImages.length > 0) ||
      event.grades.every((grade) => grade.price === 0))
  )
}

export function EventDetailScreen() {
  const { navParams, navigate, goBack, isWishlisted, toggleWishlist, wishlist, events, updateEvent } = useApp()
  const event = events.find((item) => item.id === navParams.eventId)
  const [detailEvent, setDetailEvent] = useState<Event | null>(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const fetchedEventIdsRef = useRef<Set<string>>(new Set())

  useEffect(() => {
    let cancelled = false

    async function loadDetail() {
      if (!event) {
        setDetailEvent(null)
        return
      }

      setDetailEvent(event)
      if (!needsKopisDetail(event)) return
      if (fetchedEventIdsRef.current.has(event.id)) return

      try {
        setDetailLoading(true)
        const response = await fetch(`/api/kopis/events/${event.id}`, { cache: 'no-store' })
        const data = await response.json()

        if (!cancelled && data.item) {
          fetchedEventIdsRef.current.add(event.id)
          setDetailEvent(data.item)
          updateEvent(event.id, data.item)
        }
      } catch {
        if (!cancelled) {
          setDetailEvent(event)
        }
      } finally {
        if (!cancelled) {
          setDetailLoading(false)
        }
      }
    }

    void loadDetail()

    return () => {
      cancelled = true
    }
  }, [event?.id, event?.priceInfo, event?.runtime, event?.ageRating, event?.introImages, event?.grades, updateEvent])

  if (!event) return null

  const displayEvent = detailEvent ?? event
  const wishlisted = isWishlisted(displayEvent.id)
  const canBuy = displayEvent.status === 'ON_SALE'
  const refundRules = displayEvent.refundRules ?? []
  const hasStructuredGrades = displayEvent.grades.some((grade) => grade.price > 0)

  return (
    <AppShell showBack onBack={goBack} title={displayEvent.name} showBottomNav>
      <div className="flex flex-col">
        <div className="relative overflow-hidden bg-secondary">
          <div className="relative h-60 w-full">
            <Image src={displayEvent.poster} alt={displayEvent.name} fill className="scale-110 object-cover" sizes="390px" priority />
            <div className="absolute inset-0 bg-black/35" />
            <div className="absolute inset-0 bg-gradient-to-b from-white/10 via-black/20 to-background" />

            <div className="absolute bottom-[-42px] left-4 flex items-end gap-4">
              <div className="relative h-56 w-36 overflow-hidden rounded-2xl border border-white/20 bg-transparent shadow-2xl">
                <Image src={displayEvent.poster} alt={displayEvent.name} fill className="object-contain object-top" sizes="144px" priority />
              </div>
              <div className="pb-5">
                <EventStatusBadge status={displayEvent.status} />
              </div>
            </div>

            <button
              onClick={() => toggleWishlist(displayEvent.id)}
              className="absolute right-4 top-4 z-20 flex h-11 w-11 items-center justify-center rounded-full bg-black/45 backdrop-blur-sm transition-all hover:bg-black/60 active:scale-95"
              aria-label={wishlisted ? '찜 해제' : '찜 추가'}
            >
              <Heart className={`h-5 w-5 ${wishlisted ? 'fill-red-500 text-red-500' : 'text-white'}`} />
              <span className="absolute right-0 top-0 flex h-5 min-w-5 translate-x-1/3 -translate-y-1/3 items-center justify-center rounded-full bg-primary px-1 text-[10px] font-bold text-primary-foreground">
                {wishlist.length}
              </span>
            </button>
          </div>
        </div>

        <div className="flex flex-col gap-4 px-4 pb-4 pt-16">
          <div>
            <h2 className="mb-2 text-lg font-bold leading-tight text-foreground">{displayEvent.name}</h2>
            {displayEvent.artistName && displayEvent.artistName !== '대중음악' && <p className="mb-2 text-sm text-muted-foreground">{displayEvent.artistName}</p>}

            <div className="flex flex-col gap-1.5">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Calendar className="h-4 w-4 flex-shrink-0 text-primary" />
                <span>{displayEvent.date}</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <MapPin className="h-4 w-4 flex-shrink-0 text-primary" />
                <span>{displayEvent.venue}</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Users className="h-4 w-4 flex-shrink-0 text-primary" />
                <span>1인당 최대 {displayEvent.maxPerUser}매 구매 가능</span>
              </div>
            </div>
          </div>

          {detailLoading && (
            <div className="rounded-xl border border-border bg-card p-4 text-sm text-muted-foreground">
              공연 상세 정보를 불러오는 중입니다.
            </div>
          )}

          {(displayEvent.runtime || displayEvent.ageRating || displayEvent.cast) && (
            <div className="grid gap-2 rounded-xl border border-border bg-card p-4">
              {displayEvent.runtime && (
                <div className="flex items-start gap-2 text-sm text-muted-foreground">
                  <Clock3 className="mt-0.5 h-4 w-4 shrink-0 text-primary" />
                  <span>{displayEvent.runtime}</span>
                </div>
              )}
              {displayEvent.ageRating && (
                <div className="flex items-start gap-2 text-sm text-muted-foreground">
                  <BadgeInfo className="mt-0.5 h-4 w-4 shrink-0 text-primary" />
                  <span>{displayEvent.ageRating}</span>
                </div>
              )}
              {displayEvent.cast && (
                <div className="flex items-start gap-2 text-sm text-muted-foreground">
                  <UserRound className="mt-0.5 h-4 w-4 shrink-0 text-primary" />
                  <span>{displayEvent.cast}</span>
                </div>
              )}
            </div>
          )}

          {displayEvent.description && (
            <p className="rounded-xl bg-secondary p-4 text-sm leading-relaxed text-muted-foreground">{displayEvent.description}</p>
          )}

          <div>
            <h3 className="mb-3 text-sm font-semibold text-foreground">가격 안내</h3>
            {hasStructuredGrades ? (
              <div className="flex flex-col gap-2">
                {displayEvent.grades.map((grade, index) => (
                  <div key={`${grade.name}-${grade.price}-${index}`} className="flex items-center justify-between rounded-xl bg-secondary px-4 py-3">
                    <div className="flex items-center gap-3">
                      <span className="text-sm font-semibold text-foreground">{grade.name}</span>
                      <span className={`text-xs ${grade.remaining === 0 ? 'text-red-400' : 'text-muted-foreground'}`}>
                        {grade.remaining === 0 ? '매진' : '예매 가능'}
                      </span>
                    </div>
                    <span className="text-sm font-bold text-foreground">{grade.price.toLocaleString()} CTK</span>
                  </div>
                ))}
              </div>
            ) : displayEvent.priceInfo ? (
              <div className="rounded-xl bg-secondary px-4 py-3 text-sm leading-relaxed text-foreground">{displayEvent.priceInfo}</div>
            ) : (
              <div className="flex flex-col gap-2">
                {displayEvent.grades.map((grade, index) => (
                  <div key={`${grade.name}-${grade.price}-${index}`} className="flex items-center justify-between rounded-xl bg-secondary px-4 py-3">
                    <div className="flex items-center gap-3">
                      <span className="text-sm font-semibold text-foreground">{grade.name}</span>
                      <span className={`text-xs ${grade.remaining === 0 ? 'text-red-400' : 'text-muted-foreground'}`}>
                        {grade.remaining === 0 ? '매진' : `잔여 ${grade.remaining}석`}
                      </span>
                    </div>
                    <span className="text-sm font-bold text-foreground">{grade.price.toLocaleString()} CTK</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {displayEvent.introImages && displayEvent.introImages.length > 0 && (
            <div>
              <div className="mb-3 flex items-center gap-2">
                <ImageIcon className="h-4 w-4 text-primary" />
                <h3 className="text-sm font-semibold text-foreground">상세 이미지</h3>
              </div>
              <div className="overflow-hidden rounded-xl border border-border bg-card">
                {displayEvent.introImages.map((imageUrl, index) => (
                  <div key={`${imageUrl}-${index}`} className="overflow-hidden">
                    <Image
                      src={imageUrl}
                      alt={`${displayEvent.name} 상세 이미지 ${index + 1}`}
                      width={1200}
                      height={1600}
                      className="block h-auto w-full object-contain"
                      sizes="390px"
                    />
                  </div>
                ))}
              </div>
            </div>
          )}

          {refundRules.length > 0 && (
            <div className="rounded-xl border border-border bg-card p-4">
              <div className="mb-3 flex items-center gap-2">
                <Receipt className="h-4 w-4 text-primary" />
                <h3 className="text-sm font-semibold text-foreground">환불 규정</h3>
              </div>
              <div className="flex flex-col gap-2">
                {refundRules.map((rule) => (
                  <div key={rule.id} className="flex items-center justify-between text-sm text-muted-foreground">
                    <span>{rule.label}</span>
                    <span>{rule.feeRate >= 1 ? '환불 불가' : `수수료 ${Math.round(rule.feeRate * 100)}%`}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* spacer for fixed bottom button */}
          <div className="h-16" />
        </div>
      </div>

      {/* Fixed bottom CTA */}
      <div className="sticky bottom-0 z-30 border-t border-border bg-background/95 px-4 py-3 backdrop-blur-sm">
        <button
          onClick={() => navigate('event-date-selection', { eventId: displayEvent.id })}
          disabled={!canBuy}
          className="flex w-full items-center justify-center gap-2 rounded-xl bg-primary py-4 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-40"
        >
          예매하기
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>
    </AppShell>
  )
}
