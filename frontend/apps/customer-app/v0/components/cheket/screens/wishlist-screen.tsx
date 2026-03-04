'use client'

import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { MOCK_EVENTS } from '@/lib/mock-data'
import { AppShell } from '../app-shell'
import { Heart, Calendar, MapPin, ChevronRight } from 'lucide-react'

export function WishlistScreen() {
  const { wishlist, toggleWishlist, navigate, goBack } = useApp()

  const wishlistedEvents = MOCK_EVENTS.filter((e) => wishlist.includes(e.id))

  return (
    <AppShell title="찜한 콘서트" showBack onBack={goBack} showBottomNav={false}>
      <div className="flex flex-col min-h-full">
        {/* Header stats */}
        <div className="px-4 py-4 border-b border-border">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
              <Heart className="w-6 h-6 text-primary" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">찜한 콘서트</p>
              <p className="text-2xl font-bold text-foreground">{wishlist.length}개</p>
            </div>
          </div>
        </div>

        {/* Wishlist items */}
        {wishlistedEvents.length === 0 ? (
          <div className="flex-1 flex flex-col items-center justify-center gap-4 p-8 text-center">
            <div className="w-20 h-20 rounded-full bg-secondary flex items-center justify-center">
              <Heart className="w-10 h-10 text-muted-foreground" />
            </div>
            <div>
              <p className="font-semibold text-foreground">찜한 콘서트가 없습니다</p>
              <p className="text-sm text-muted-foreground mt-1">
                마음에 드는 콘서트를 찜해보세요!
              </p>
            </div>
            <button
              onClick={() => navigate('home')}
              className="mt-2 px-6 py-2.5 bg-primary text-primary-foreground text-sm font-semibold rounded-xl hover:opacity-90 active:scale-[0.98] transition-all"
            >
              콘서트 둘러보기
            </button>
          </div>
        ) : (
          <div className="flex-1 p-4 flex flex-col gap-3">
            {wishlistedEvents.map((event) => (
              <div
                key={event.id}
                className="bg-card border border-border rounded-2xl overflow-hidden"
              >
                {/* Event card content - clickable to detail */}
                <button
                  onClick={() => navigate('event-detail', { eventId: event.id })}
                  className="w-full flex gap-3 p-3 text-left hover:bg-secondary/30 active:scale-[0.99] transition-all"
                >
                  {/* Poster */}
                  <div className="relative w-24 h-32 rounded-xl overflow-hidden bg-secondary flex-shrink-0">
                    <Image
                      src={event.poster}
                      alt={event.name}
                      fill
                      className="object-cover"
                    />
                  </div>

                  {/* Info */}
                  <div className="flex-1 flex flex-col justify-between py-0.5 min-w-0">
                    <div>
                      <h3 className="font-semibold text-sm text-foreground leading-snug line-clamp-2">
                        {event.name}
                      </h3>
                      <div className="flex items-center gap-1.5 mt-2 text-xs text-muted-foreground">
                        <Calendar className="w-3.5 h-3.5" />
                        <span>{event.date}</span>
                      </div>
                      <div className="flex items-center gap-1.5 mt-1 text-xs text-muted-foreground">
                        <MapPin className="w-3.5 h-3.5" />
                        <span className="truncate">{event.venue.split(',')[0]}</span>
                      </div>
                    </div>

                    <div className="flex items-center justify-between mt-2">
                      <span className="text-sm font-bold text-primary">
                        {event.grades[0]?.price.toLocaleString()} CTK~
                      </span>
                      <ChevronRight className="w-4 h-4 text-muted-foreground" />
                    </div>
                  </div>
                </button>

                {/* Remove from wishlist button */}
                <div className="px-3 pb-3">
                  <button
                    onClick={() => toggleWishlist(event.id)}
                    className="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl bg-secondary/50 border border-border text-sm text-muted-foreground hover:text-red-500 hover:border-red-500/30 hover:bg-red-500/5 active:scale-[0.98] transition-all"
                  >
                    <Heart className="w-4 h-4 fill-current" />
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
