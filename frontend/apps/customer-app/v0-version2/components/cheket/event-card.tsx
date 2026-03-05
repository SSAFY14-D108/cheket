'use client'

import Image from 'next/image'
import { Event } from '@/lib/types'
import { EventStatusBadge } from './status-badge'
import { MapPin, Calendar } from 'lucide-react'

interface EventCardProps {
  event: Event
  onClick: () => void
}

export function EventCard({ event, onClick }: EventCardProps) {
  return (
    <button
      onClick={onClick}
      className="w-full flex gap-3 p-3 bg-card rounded-xl border border-border hover:border-primary/40 active:scale-[0.98] transition-all text-left"
    >
      <div className="relative w-20 h-20 rounded-lg overflow-hidden flex-shrink-0 bg-secondary">
        <Image
          src={event.poster}
          alt={event.name}
          fill
          className="object-cover"
          sizes="80px"
        />
      </div>
      <div className="flex-1 min-w-0 py-0.5">
        <div className="flex items-start justify-between gap-2 mb-1">
          <h3 className="font-semibold text-sm text-foreground leading-tight line-clamp-2 flex-1">
            {event.name}
          </h3>
          <EventStatusBadge status={event.status} />
        </div>
        <div className="flex items-center gap-1 text-muted-foreground text-xs mb-1">
          <Calendar className="w-3 h-3 flex-shrink-0" />
          <span className="truncate">{event.date}</span>
        </div>
        <div className="flex items-center gap-1 text-muted-foreground text-xs">
          <MapPin className="w-3 h-3 flex-shrink-0" />
          <span className="truncate">{event.venue}</span>
        </div>
      </div>
    </button>
  )
}
