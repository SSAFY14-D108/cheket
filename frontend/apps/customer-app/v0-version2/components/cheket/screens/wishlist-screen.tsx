'use client'

import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { Heart, Calendar, MapPin, ChevronRight } from 'lucide-react'

export function WishlistScreen() {
  const { wishlist, toggleWishlist, navigate, goBack, events } = useApp()
  const wishlistedEvents = events.filter((event) => wishlist.includes(event.id))

  return (
    <AppShell title="찜한 공연" showBack onBack={goBack} showBottomNav={false}>
      <div className="flex min-h-full flex-col">
        <div className="border-b border-border px-4 py-4">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
              <Heart className="h-6 w-6 text-primary" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">찜한 공연 수</p>
              <p className="text-2xl font-bold text-foreground">{wishlist.length}개</p>
            </div>
          </div>
        </div>

        {wishlistedEvents.length === 0 ? (
          <div className="flex flex-1 flex-col items-center justify-center gap-4 p-8 text-center">
            <div className="flex h-20 w-20 items-center justify-center rounded-full bg-secondary">
              <Heart className="h-10 w-10 text-muted-foreground" />
            </div>
            <div>
              <p className="font-semibold text-foreground">아직 찜한 공연이 없습니다</p>
              <p className="mt-1 text-sm text-muted-foreground">
                관심 있는 공연을 찜하고 빠르게 다시 확인해 보세요.
              </p>
            </div>
            <button
              onClick={() => navigate('home')}
              className="mt-2 rounded-xl bg-primary px-6 py-2.5 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98]"
            >
              홈으로 이동
            </button>
          </div>
        ) : (
          <div className="flex flex-1 flex-col gap-3 p-4">
            {wishlistedEvents.map((event) => (
              <div key={event.id} className="overflow-hidden rounded-2xl border border-border bg-card">
                <button
                  onClick={() => navigate('event-detail', { eventId: event.id })}
                  className="flex w-full gap-3 p-3 text-left transition-all hover:bg-secondary/30 active:scale-[0.99]"
                >
                  <div className="relative h-32 w-24 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
                    <Image src={event.poster} alt={event.name} fill className="object-cover" />
                  </div>

                  <div className="flex min-w-0 flex-1 flex-col justify-between py-0.5">
                    <div>
                      <h3 className="line-clamp-2 text-sm font-semibold leading-snug text-foreground">
                        {event.name}
                      </h3>
                      <div className="mt-2 flex items-center gap-1.5 text-xs text-muted-foreground">
                        <Calendar className="h-3.5 w-3.5" />
                        <span>{event.date}</span>
                      </div>
                      <div className="mt-1 flex items-center gap-1.5 text-xs text-muted-foreground">
                        <MapPin className="h-3.5 w-3.5" />
                        <span className="truncate">{event.venue.split(',')[0]}</span>
                      </div>
                    </div>

                    <div className="mt-2 flex items-center justify-between">
                      <span className="text-sm font-bold text-primary">
                        {event.grades[0]?.price.toLocaleString()} CTK~
                      </span>
                      <ChevronRight className="h-4 w-4 text-muted-foreground" />
                    </div>
                  </div>
                </button>

                <div className="px-3 pb-3">
                  <button
                    onClick={() => toggleWishlist(event.id)}
                    className="flex w-full items-center justify-center gap-2 rounded-xl border border-border bg-secondary/50 py-2.5 text-sm text-muted-foreground transition-all hover:border-red-500/30 hover:bg-red-500/5 hover:text-red-500 active:scale-[0.98]"
                  >
                    <Heart className="h-4 w-4 fill-current" />
                    찜 해제
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
