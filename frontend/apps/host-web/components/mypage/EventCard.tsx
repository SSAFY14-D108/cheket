"use client"

import Image from "next/image"
import Link from "next/link"
import { Card, CardContent } from "@/components/ui/card"
import { MapPin, Calendar, BarChart3 } from "lucide-react"
import { mockVenues } from "@/lib/mock-data"
import type { Event } from "@/lib/mock-data"

interface EventCardProps {
  event: Event
}

const POSTER_PLACEHOLDER = "/images/poster-1.jpg"

export function EventCard({ event }: EventCardProps) {
  return (
    <Link href={`/shows/${event.showId}`}>
      <Card className="overflow-hidden transition-shadow hover:shadow-md cursor-pointer gap-0 py-0">
        <div className="relative aspect-[3/4] w-full overflow-hidden bg-muted">
          <Image
            src={event.posterUrl || POSTER_PLACEHOLDER}
            alt={`${event.title} 포스터`}
            fill
            className="object-cover"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
          />
        </div>
        <CardContent className="flex flex-col gap-2 p-4">
          <h3 className="text-base font-semibold text-foreground leading-snug text-balance">
            {event.title}
          </h3>
          <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
            <Calendar className="size-3.5 shrink-0" />
            <span>{event.show.startAt.substring(0, 10)}</span>
          </div>
          <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
            <MapPin className="size-3.5 shrink-0" />
            <span>{event.venue.name}</span>
          </div>
          <Link
            href={`/shows/${event.showId}/dashboard`}
            className="mt-1 flex items-center justify-center gap-1.5 rounded-sm bg-secondary py-1.5 text-xs font-medium text-secondary-foreground transition-colors hover:bg-secondary/80"
            onClick={(e) => e.stopPropagation()}
          >
            <BarChart3 className="size-3" />
            대시보드
          </Link>
        </CardContent>
      </Card>
    </Link>
  )
}

