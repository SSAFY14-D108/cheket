'use client'

import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { Calendar, MapPin, Share2, Award } from 'lucide-react'

export function CollectibleTicketDetailScreen() {
  const { navParams, goBack } = useApp()
  const { tickets } = useApp()
  const ticket = tickets.find((t) => t.id === navParams.ticketId)

  if (!ticket) return null

  return (
    <AppShell showBack onBack={goBack} title="소장 티켓" showBottomNav={false}>
      <div className="flex flex-col gap-4 p-4 pb-8">
        {/* Poster */}
        <div className="relative w-full aspect-[3/4] rounded-2xl overflow-hidden bg-secondary">
          <Image
            src={ticket.poster}
            alt={ticket.eventName}
            fill
            className="object-cover"
            sizes="390px"
            priority
          />
          {/* Overlay gradient */}
          <div className="absolute inset-0 bg-gradient-to-t from-background/90 via-background/20 to-transparent" />

          {/* NFT badge */}
          <div className="absolute top-3 right-3 bg-primary/90 text-primary-foreground text-xs font-bold px-3 py-1 rounded-full flex items-center gap-1.5">
            <Award className="w-3.5 h-3.5" />
            NFT 소장
          </div>

          {/* Bottom overlay info */}
          <div className="absolute bottom-4 left-4 right-4">
            <h2 className="font-bold text-xl text-white leading-tight mb-2 text-balance">
              {ticket.eventName}
            </h2>
            <div className="flex items-center gap-2 text-white/80 text-sm mb-1">
              <Calendar className="w-3.5 h-3.5 flex-shrink-0" />
              <span>{ticket.eventDate}</span>
            </div>
            <div className="flex items-center gap-2 text-white/80 text-sm">
              <MapPin className="w-3.5 h-3.5 flex-shrink-0" />
              <span>{ticket.venue}</span>
            </div>
          </div>
        </div>

        {/* Ticket details card */}
        <div className="bg-card border border-border rounded-xl p-4 flex flex-col gap-3">
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">좌석</span>
            <span className="text-sm font-semibold text-foreground">{ticket.seatLabel}</span>
          </div>
          <div className="h-px bg-border" />
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">등급</span>
            <span className="text-sm font-semibold text-primary">{ticket.grade}</span>
          </div>
          <div className="h-px bg-border" />
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">관람일</span>
            <span className="text-sm font-semibold text-foreground">
              {ticket.attendedDate ?? ticket.eventDate.split(' ')[0]}
            </span>
          </div>
          <div className="h-px bg-border" />
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">상태</span>
            <span className="text-xs bg-muted-foreground/20 text-muted-foreground px-2 py-0.5 rounded-full font-medium">
              사용 완료
            </span>
          </div>
        </div>

        {/* NFT details */}
        <div className="bg-secondary rounded-xl p-4 flex flex-col gap-2 text-xs">
          <p className="font-semibold text-sm text-foreground mb-1">NFT 정보</p>
          <div className="flex items-center justify-between">
            <span className="text-muted-foreground">Token ID</span>
            <span className="font-mono text-foreground">#{ticket.id.split('_').pop()?.toUpperCase()}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-muted-foreground">Owner</span>
            <span className="font-mono text-foreground">0x3a9F...dE42</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-muted-foreground">Contract</span>
            <span className="font-mono text-foreground">Cheket v1</span>
          </div>
        </div>

        {/* Share button (placeholder) */}
        <button
          className="w-full flex items-center justify-center gap-2 bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm hover:border-primary/50 active:scale-[0.98] transition-all"
          onClick={() => {}}
          aria-label="공유"
        >
          <Share2 className="w-4 h-4" />
          소장 티켓 공유하기
        </button>
      </div>
    </AppShell>
  )
}
