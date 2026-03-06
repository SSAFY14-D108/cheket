'use client'

import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { MOCK_EVENTS } from '@/lib/mock-data'
import { AppShell } from '../app-shell'
import { EventStatusBadge } from '../status-badge'
import { MapPin, Calendar, Users, ChevronRight, Heart, Receipt } from 'lucide-react'

export function EventDetailScreen() {
  const { navParams, navigate, goBack, isWishlisted, toggleWishlist, wishlist } = useApp()
  const event = MOCK_EVENTS.find((e) => e.id === navParams.eventId)

  if (!event) return null

  const canBuy = event.status === 'ON_SALE'
  const wishlisted = isWishlisted(event.id)
  const refundRules = event.refundRules ?? []

  return (
    <AppShell showBack onBack={goBack} title={event.name} showBottomNav>
      <div className="flex flex-col">
        <div className="relative w-full aspect-[3/2] bg-secondary overflow-visible">
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

          {refundRules.length > 0 && (
            <div className="bg-card rounded-xl border border-border p-4">
              <div className="flex items-center gap-2 mb-3">
                <Receipt className="w-4 h-4 text-primary" />
                <h3 className="font-semibold text-sm text-foreground">환불 규정</h3>
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
            onClick={() => toggleWishlist(event.id)}
            className="absolute top-3 right-3 left-auto z-20 w-11 h-11 rounded-full bg-black/45 backdrop-blur-sm flex items-center justify-center hover:bg-black/60 active:scale-95 transition-all"
            aria-label={wishlisted ? '찜 해제' : '찜하기'}
          >
            <Heart className={`w-5 h-5 transition-colors ${wishlisted ? 'fill-red-500 text-red-500' : 'text-white'}`} />
            <span className="absolute top-0 right-0 translate-x-1/3 -translate-y-1/3 min-w-5 h-5 px-1 rounded-full bg-primary text-primary-foreground text-[10px] font-bold flex items-center justify-center">
              {wishlist.length}
            </span>
          </button>
        </div>

        <div className="p-4 flex flex-col gap-4">
          <div>
            <h2 className="font-bold text-lg text-foreground leading-tight mb-2">{event.name}</h2>
            {event.artistName && (
              <p className="text-sm text-muted-foreground mb-2">{event.artistName}</p>
            )}
            <div className="flex flex-col gap-1.5">
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Calendar className="w-4 h-4 text-primary flex-shrink-0" />
                <span>{event.date}</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <MapPin className="w-4 h-4 text-primary flex-shrink-0" />
                <span>{event.venue}</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Users className="w-4 h-4 text-primary flex-shrink-0" />
                <span>1인 최대 {event.maxPerUser}매 구매 가능</span>
              </div>
            </div>
          </div>

          {event.description && (
            <p className="text-sm text-muted-foreground leading-relaxed bg-secondary rounded-xl p-4">
              {event.description}
            </p>
          )}

          <div>
            <h3 className="font-semibold text-sm text-foreground mb-3">가격 안내</h3>
            <div className="flex flex-col gap-2">
              {event.grades.map((grade) => (
                <div key={grade.name} className="flex items-center justify-between px-4 py-3 bg-secondary rounded-xl">
                  <div className="flex items-center gap-3">
                    <span className="font-semibold text-sm text-foreground">{grade.name}</span>
                    <span className={`text-xs ${grade.remaining === 0 ? 'text-red-400' : 'text-muted-foreground'}`}>
                      {grade.remaining === 0 ? '매진' : `잔여 ${grade.remaining}석`}
                    </span>
                  </div>
                  <span className="font-bold text-foreground text-sm">{grade.price.toLocaleString()} CTK</span>
                </div>
              ))}
            </div>
          </div>

          <button
            onClick={() => navigate('event-date-selection', { eventId: event.id })}
            disabled={!canBuy}
            className="w-full flex items-center justify-center gap-2 bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed mt-2"
          >
            예매하기
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>
    </AppShell>
  )
}

