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
      className="w-full flex gap-4 p-4 bg-card rounded-xl border border-border hover:border-primary/40 active:scale-[0.98] transition-all text-left"
    >
      <div className="relative w-24 h-32 rounded-lg overflow-hidden flex-shrink-0 bg-secondary">
        <Image
          src={event.poster}
          alt={event.name}
          fill
          className="object-cover"
          sizes="96px"
        />
      </div>
      <div className="flex-1 min-w-0 py-2 flex flex-col justify-between">
        <div className="flex flex-col gap-2">
          <div>
            <EventStatusBadge status={event.status} />
          </div>
          <h3 className="font-semibold text-sm text-foreground leading-tight line-clamp-2">
            {event.name}
          </h3>
        </div>
        <div className="flex flex-col gap-1.5">
          <div className="flex items-center gap-1 text-muted-foreground text-xs">
            <Calendar className="w-3 h-3 flex-shrink-0" />
            <span className="truncate">{event.date}</span>
          </div>
          <div className="flex items-center gap-1 text-muted-foreground text-xs">
            <MapPin className="w-3 h-3 flex-shrink-0" />
            <span className="truncate">{event.venue}</span>
          </div>
        </div>
      </div>
    </button>
  )
}
