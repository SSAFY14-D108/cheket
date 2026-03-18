'use client'

import Image from 'next/image'
import { Calendar, MapPin } from 'lucide-react'
import type { Event } from '@/lib/types'
import { EventStatusBadge } from './status-badge'

interface EventCardProps {
  event: Event
  onClick: () => void
}

export function EventCard({ event, onClick }: EventCardProps) {
  return (
    <button
      onClick={onClick}
      className="elevated-surface-soft flex w-full items-center gap-3 rounded-2xl p-3 text-left transition-all hover:shadow-[0_12px_28px_rgba(15,23,42,0.07)] active:scale-[0.98]"
    >
      <div className="relative h-28 w-[84px] flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
        <Image src={event.poster} alt={event.name} fill className="object-cover" sizes="84px" />
      </div>

      <div className="flex min-w-0 flex-1 flex-col justify-center">
        <div className="mb-1 flex items-start justify-between gap-2">
          <h3 className="line-clamp-2 flex-1 text-sm font-bold leading-tight text-gray-900">{event.name}</h3>
          {event.status === 'SOLD_OUT' ? <EventStatusBadge status={event.status} /> : null}
        </div>

        <div className="mb-1 flex items-center gap-1 text-[13px] text-gray-500">
          <Calendar className="h-3.5 w-3.5 flex-shrink-0" />
          <span className="truncate">{event.date}</span>
        </div>

        <div className="flex items-center gap-1 text-[13px] text-gray-500">
          <MapPin className="h-3.5 w-3.5 flex-shrink-0" />
          <span className="truncate">{event.venue}</span>
        </div>
      </div>
    </button>
  )
}
