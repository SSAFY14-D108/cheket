'use client'

import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { EmptyState } from '../empty-state'
import { Calendar, MapPin } from 'lucide-react'

export function ArchiveScreen() {
  const { tickets, navigate } = useApp()
  const usedTickets = tickets.filter((t) => t.status === 'USED')

  return (
    <AppShell>
      <div className="p-4">
        {usedTickets.length === 0 ? (
          <EmptyState
            title="아직 참석한 공연이 없습니다"
            description="공연에 참석하면 소장 기록이 이곳에 저장됩니다."
          />
        ) : (
          <>
            <p className="text-xs text-muted-foreground mb-4">{usedTickets.length}개의 공연 기록</p>
            <div className="grid grid-cols-2 gap-3">
              {usedTickets.map((ticket) => (
                <button
                  key={ticket.id}
                  onClick={() => navigate('collectible-ticket-detail', { ticketId: ticket.id })}
                  className="flex flex-col rounded-xl overflow-hidden bg-card border border-border hover:border-primary/40 active:scale-[0.98] transition-all text-left"
                >
                  <div className="relative w-full aspect-square bg-secondary">
                    <Image
                      src={ticket.poster}
                      alt={ticket.eventName}
                      fill
                      className="object-cover"
                      sizes="(max-width: 390px) 50vw, 195px"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-background/70 via-transparent to-transparent" />
                    <div className="absolute bottom-2 left-2 right-2">
                      <span className="text-[10px] bg-primary/90 text-primary-foreground px-1.5 py-0.5 rounded-full font-medium">
                        소장됨
                      </span>
                    </div>
                  </div>
                  <div className="p-2.5">
                    <p className="font-semibold text-xs text-foreground leading-tight line-clamp-2 mb-1">
                      {ticket.eventName}
                    </p>
                    <div className="flex items-center gap-1 text-[10px] text-muted-foreground mb-0.5">
                      <Calendar className="w-2.5 h-2.5" />
                      <span className="truncate">{ticket.eventDate.split(' ')[0]}</span>
                    </div>
                    <div className="flex items-center gap-1 text-[10px] text-muted-foreground">
                      <MapPin className="w-2.5 h-2.5" />
                      <span className="truncate">{ticket.seatLabel}</span>
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </>
        )}
      </div>
    </AppShell>
  )
}
