'use client'

import Image from 'next/image'
import { Ticket } from '@/lib/types'
import { TicketStatusBadge } from './status-badge'
import { MapPin, Calendar } from 'lucide-react'

interface TicketCardProps {
  ticket: Ticket
  onClick: () => void
}

export function TicketCard({ ticket, onClick }: TicketCardProps) {
  return (
    <button
      onClick={onClick}
      className="w-full flex gap-3 p-3 bg-card rounded-xl border border-border hover:border-primary/40 active:scale-[0.98] transition-all text-left"
    >
      <div className="relative w-20 h-20 rounded-lg overflow-hidden flex-shrink-0 bg-secondary">
        <Image src={ticket.poster} alt={ticket.eventName} fill className="object-cover" sizes="80px" />
      </div>
      <div className="flex-1 min-w-0 py-0.5">
        <div className="flex items-start justify-between gap-2 mb-1">
          <h3 className="font-semibold text-sm text-foreground leading-tight line-clamp-2 flex-1">
            {ticket.eventName}
          </h3>
          <TicketStatusBadge status={ticket.status} />
        </div>
        <p className="text-xs text-primary font-medium mb-1">{ticket.seatLabel} · {ticket.grade}</p>
        <div className="flex items-center gap-1 text-muted-foreground text-xs mb-0.5">
          <Calendar className="w-3 h-3 flex-shrink-0" />
          <span className="truncate">{ticket.eventDate}</span>
        </div>
        <div className="flex items-center gap-1 text-muted-foreground text-xs">
          <MapPin className="w-3 h-3 flex-shrink-0" />
          <span className="truncate">{ticket.venue}</span>
        </div>
      </div>
    </button>
  )
}
