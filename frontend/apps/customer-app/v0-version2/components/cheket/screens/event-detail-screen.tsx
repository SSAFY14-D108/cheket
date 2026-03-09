'use client'

import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { EventStatusBadge } from '../status-badge'
import { MapPin, Calendar, Users, ChevronRight, Heart, Receipt } from 'lucide-react'

export function EventDetailScreen() {
  const { navParams, navigate, goBack, isWishlisted, toggleWishlist, wishlist, events } = useApp()
  const event = events.find((item) => item.id === navParams.eventId)

  if (!event) return null

  const canBuy = event.status === 'ON_SALE'
  const wishlisted = isWishlisted(event.id)
  const refundRules = event.refundRules ?? []

  return (
    <AppShell showBack onBack={goBack} title={event.name} showBottomNav>
      <div className="flex flex-col">
        <div className="relative w-full aspect-[3/2] overflow-hidden bg-secondary">
          <Image
            src={event.poster}
            alt={event.name}
            fill
            className="object-cover"
            sizes="390px"
            priority
          />
          <div className="absolute inset-0 bg-gradient-to-t from-background/80 via-transparent to-transparent" />
          <div className="absolute bottom-3 left-3">
            <EventStatusBadge status={event.status} />
          </div>
          <button
            onClick={() => toggleWishlist(event.id)}
            className="absolute top-3 right-3 z-20 flex h-11 w-11 items-center justify-center rounded-full bg-black/45 backdrop-blur-sm transition-all hover:bg-black/60 active:scale-95"
            aria-label={wishlisted ? '찜 해제' : '찜하기'}
          >
            <Heart className={`h-5 w-5 ${wishlisted ? 'fill-red-500 text-red-500' : 'text-white'}`} />
            <span className="absolute top-0 right-0 flex h-5 min-w-5 translate-x-1/3 -translate-y-1/3 items-center justify-center rounded-full bg-primary px-1 text-[10px] font-bold text-primary-foreground">
              {wishlist.length}
            </span>
          </button>
        </div>

        <div className="flex flex-col gap-4 p-4">
          <div>
            <h2 className="mb-2 text-lg font-bold leading-tight text-foreground">{event.name}</h2>
            {event.artistName && (
              <p className="mb-2 text-sm text-muted-foreground">{event.artistName}</p>
            )}
            <div className="flex flex-col gap-1.5">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Calendar className="h-4 w-4 flex-shrink-0 text-primary" />
                <span>{event.date}</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <MapPin className="h-4 w-4 flex-shrink-0 text-primary" />
                <span>{event.venue}</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Users className="h-4 w-4 flex-shrink-0 text-primary" />
                <span>1인 최대 {event.maxPerUser}매 구매 가능</span>
              </div>
            </div>
          </div>

          {event.description && (
            <p className="rounded-xl bg-secondary p-4 text-sm leading-relaxed text-muted-foreground">
              {event.description}
            </p>
          )}

          <div>
            <h3 className="mb-3 text-sm font-semibold text-foreground">가격 안내</h3>
            <div className="flex flex-col gap-2">
              {event.grades.map((grade) => (
                <div key={grade.name} className="flex items-center justify-between rounded-xl bg-secondary px-4 py-3">
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
          </div>

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

          <button
            onClick={() => navigate('event-date-selection', { eventId: event.id })}
            disabled={!canBuy}
            className="mt-2 flex w-full items-center justify-center gap-2 rounded-xl bg-primary py-4 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-40"
          >
            예매하기
            <ChevronRight className="h-4 w-4" />
          </button>
        </div>
      </div>
    </AppShell>
  )
}
