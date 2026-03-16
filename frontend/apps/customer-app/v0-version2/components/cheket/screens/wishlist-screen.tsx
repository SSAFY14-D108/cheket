'use client'

import Image from 'next/image'
import { Calendar, ChevronRight, Heart, MapPin } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'

export function WishlistScreen() {
  const { wishlist, toggleWishlist, navigate, goBack, events } = useApp()
  const wishlistedEvents = events.filter((event) => wishlist.includes(event.id))

  return (
    <AppShell title="찜한 공연" showBack onBack={goBack} showBottomNav={false}>
      <div className="flex min-h-full flex-col bg-[#f7f8fa]">
        <div className="border-b border-border px-4 py-4">
          <div className="flex items-center gap-3">
            <div className="gradient-outline-icon-button flex h-12 w-12 items-center justify-center rounded-full">
              <Heart className="h-6 w-6 text-[#333333]" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">찜한 공연 수</p>
              <p className="text-2xl font-bold text-[#111111]">{wishlist.length}개</p>
            </div>
          </div>
        </div>

        {wishlistedEvents.length === 0 ? (
          <div className="flex flex-1 flex-col items-center justify-center gap-4 p-8 text-center">
            <div className="gradient-outline-icon-button flex h-20 w-20 items-center justify-center rounded-full">
              <Heart className="h-10 w-10 text-muted-foreground" />
            </div>
            <div>
              <p className="font-semibold text-[#111111]">아직 찜한 공연이 없어요</p>
              <p className="mt-1 text-sm text-muted-foreground">관심 있는 공연을 찜해두면 여기에서 빠르게 다시 볼 수 있어요.</p>
            </div>
            <button onClick={() => navigate('home')} className="gradient-outline-button mt-2 rounded-xl px-6 py-2.5 text-sm font-semibold text-[#111111]">
              홈 둘러보기
            </button>
          </div>
        ) : (
          <div className="flex flex-1 flex-col gap-3 p-4">
            {wishlistedEvents.map((event) => (
              <div key={event.id} className="gradient-outline-surface overflow-hidden rounded-2xl">
                <button
                  onClick={() => navigate('event-detail', { eventId: event.id })}
                  className="flex w-full gap-3 p-3 text-left transition-all hover:bg-secondary/20 active:scale-[0.99]"
                >
                  <div className="relative h-32 w-24 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
                    <Image src={event.poster} alt={event.name} fill className="object-cover" />
                  </div>

                  <div className="flex min-w-0 flex-1 flex-col justify-between py-0.5">
                    <div>
                      <h3 className="line-clamp-2 text-sm font-semibold leading-snug text-[#111111]">{event.name}</h3>
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
                      <span className="text-sm font-bold text-[#111111]">{event.grades[0]?.price.toLocaleString()} CTK~</span>
                      <ChevronRight className="h-4 w-4 text-muted-foreground" />
                    </div>
                  </div>
                </button>

                <div className="px-3 pb-3">
                  <button
                    onClick={() => toggleWishlist(event.id)}
                    className="gradient-outline-surface-soft flex w-full items-center justify-center gap-2 rounded-xl py-2.5 text-sm text-[#333333] transition-all active:scale-[0.98]"
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
