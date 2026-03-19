"use client"

import Image from "next/image"
import Link from "next/link"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { MapPin, Calendar, BarChart3, Ticket } from "lucide-react"
import type { MyShowSummary } from "@/lib/mypage-api"
import { getShowDisplayMeta } from "@/lib/show-display"

interface EventCardProps {
  event: MyShowSummary
}

const POSTER_PLACEHOLDER = "/images/poster-1.jpg"

function formatShowPeriod(event: MyShowSummary) {
  if (event.show.showStartDate === event.show.showEndDate) {
    return event.show.showStartDate
  }

  return `${event.show.showStartDate} ~ ${event.show.showEndDate}`
}

export function EventCard({ event }: EventCardProps) {
  const posterSrc = event.posterUrl || POSTER_PLACEHOLDER
  const shouldBypassOptimization =
    posterSrc.startsWith("http://") || posterSrc.startsWith("https://")
  const displayMeta = getShowDisplayMeta(event)

  return (
    <Link href={`/shows/${event.showId}`}>
      <Card className="overflow-hidden transition-shadow hover:shadow-md cursor-pointer gap-0 py-0">
        <div className="relative aspect-[3/4] w-full overflow-hidden bg-muted">
          <Image
            src={posterSrc}
            alt={`${event.title} 포스터`}
            fill
            className="object-cover"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
            unoptimized={shouldBypassOptimization}
          />
        </div>
        <CardContent className="flex flex-col gap-2 p-4">
          <div className="flex flex-col gap-2">
            <h3 className="min-h-[3.25rem] text-base font-semibold leading-snug text-foreground line-clamp-2">
              {event.title}
            </h3>
            <div className="flex min-h-[2rem] flex-wrap items-start gap-1.5">
              {displayMeta.badges.map((badge) => (
                <Badge key={`${badge.phase}-${badge.label}`} variant="outline">
                  {badge.label}
                </Badge>
              ))}
            </div>
          </div>
          <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
            <Calendar className="size-3.5 shrink-0" />
            <span>{formatShowPeriod(event)}</span>
          </div>
          <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
            <MapPin className="size-3.5 shrink-0" />
            <span>{event.venue}</span>
          </div>
          <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
            <Ticket className="size-3.5 shrink-0" />
            <span>구매 제한 {event.purchaseLimit}매</span>
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

