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
      className="elevated-surface-soft flex w-full gap-3 rounded-xl p-3 text-left transition-all hover:shadow-[0_12px_28px_rgba(15,23,42,0.06)] active:scale-[0.98]"
    >
      <div className="relative h-20 w-20 flex-shrink-0 overflow-hidden rounded-lg bg-secondary">
        <Image src={ticket.poster} alt={ticket.eventName} fill className="object-cover" sizes="80px" />
      </div>

      <div className="min-w-0 flex-1 py-0.5">
        <div className="mb-1 flex items-start justify-between gap-2">
          <h3 className="flex-1 line-clamp-2 text-sm font-semibold leading-tight text-foreground">
            {ticket.eventName}
          </h3>
          <TicketStatusBadge status={ticket.status} />
        </div>

        <p className="mb-1 text-xs font-medium text-[#333333]">
          {ticket.seatLabel} · {ticket.grade}
        </p>

        <div className="mb-0.5 flex items-center gap-1 text-xs text-muted-foreground">
          <Calendar className="h-3 w-3 flex-shrink-0" />
          <span className="truncate">{ticket.eventDate}</span>
        </div>

        <div className="flex items-center gap-1 text-xs text-muted-foreground">
          <MapPin className="h-3 w-3 flex-shrink-0" />
          <span className="truncate">{ticket.venue}</span>
        </div>
      </div>
    </button>
  )
}
