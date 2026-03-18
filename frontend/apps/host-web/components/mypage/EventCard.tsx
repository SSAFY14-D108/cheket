"use client"

import Image from "next/image"
import Link from "next/link"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { MapPin, Calendar, BarChart3, Ticket } from "lucide-react"
import type { MyShowSummary } from "@/lib/mypage-api"

interface EventCardProps {
  event: MyShowSummary
}

const POSTER_PLACEHOLDER = "/images/poster-1.jpg"

const STATUS_LABELS: Record<string, string> = {
  DRAFT: "임시저장",
  UPCOMING: "공개 예정",
  TICKETING: "예매 중",
  CLOSED: "종료",
}

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
          <div className="flex items-start justify-between gap-3">
            <h3 className="text-base font-semibold text-foreground leading-snug text-balance">
              {event.title}
            </h3>
            <Badge variant="outline">
              {STATUS_LABELS[event.status] ?? event.status}
            </Badge>
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

