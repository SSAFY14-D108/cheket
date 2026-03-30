"use client"

import Image from "next/image"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Calendar, MapPin } from "lucide-react"
import type { MyShowSummary } from "@/lib/mypage-api"
import { getShowDisplayMeta } from "@/lib/show-display"

interface EventCardProps {
  event: MyShowSummary
}

const POSTER_PLACEHOLDER = "/images/poster-1.jpg"

const PERFORMANCE_BADGE_STYLES = {
  UPCOMING: "border-black/10 bg-[#f4f4f5] text-black/72",
  LIVE: "border-[#d9e2f2] bg-[#eef4ff] text-[#35558a]",
  ENDED: "border-black/10 bg-white text-black/60",
  CANCELLED: "border-red-200 bg-red-50 text-red-700",
} as const

const RESERVATION_BADGE_STYLES = {
  TICKETING: "border-black/10 bg-white text-black/60",
  UPCOMING: "border-black/10 bg-white text-black/46",
  CLOSED: "border-black/10 bg-[#f7f7f8] text-black/46",
} as const

const CONTRACT_BADGE_CLASS = "border-amber-200 bg-amber-50 text-amber-700"
const CONTRACT_REJECTED_BADGE_CLASS = "border-rose-200 bg-rose-50 text-rose-700"

function formatShowPeriod(event: MyShowSummary) {
  if (event.show.showStartDate === event.show.showEndDate) {
    return event.show.showStartDate
  }

  return `${event.show.showStartDate} ~ ${event.show.showEndDate}`
}

export function EventCard({ event }: EventCardProps) {
  const router = useRouter()
  const posterSrc = event.posterUrl || POSTER_PLACEHOLDER
  const shouldBypassOptimization =
    posterSrc.startsWith("http://") || posterSrc.startsWith("https://")
  const displayMeta = getShowDisplayMeta(event)
  const performanceBadgeClass =
    PERFORMANCE_BADGE_STYLES[displayMeta.performance.phase] ??
    "border-black/10 bg-white text-black/60"
  const reservationBadgeClass =
    RESERVATION_BADGE_STYLES[displayMeta.reservation.phase] ??
    "border-black/10 bg-white text-black/50"
  const showPendingContractBadge = event.status === "PENDING_CONTRACT"
  const showRejectedContractBadge = event.status === "CONTRACT_REJECTED"

  return (
    <article
      className="overflow-hidden rounded-[1.35rem] border border-black/8 bg-white transition-transform duration-200 hover:-translate-y-1"
      onClick={() => router.push(`/shows/${event.showId}`)}
    >
      <div className="relative aspect-[4/5] w-full overflow-hidden bg-black/[0.04]">
        <Image
          src={posterSrc}
          alt={`${event.title} 포스터`}
          fill
          className="object-cover"
          sizes="(max-width: 768px) 100vw, (max-width: 1280px) 50vw, 25vw"
          unoptimized={shouldBypassOptimization}
        />
      </div>

      <div className="space-y-3 p-4">
        <div className="space-y-2">
          <h3 className="line-clamp-2 min-h-[2.6rem] text-base font-semibold leading-snug tracking-[-0.02em] text-black">
            {event.title}
          </h3>

          <div className="flex min-h-6 flex-wrap gap-1.5">
            {showPendingContractBadge ? (
              <span
                className={`inline-flex items-center rounded-full border px-2.5 py-1 text-[11px] font-semibold ${CONTRACT_BADGE_CLASS}`}
              >
                승인 대기중
              </span>
            ) : null}

            {showRejectedContractBadge ? (
              <span
                className={`inline-flex items-center rounded-full border px-2.5 py-1 text-[11px] font-semibold ${CONTRACT_REJECTED_BADGE_CLASS}`}
              >
                승인 거절
              </span>
            ) : null}

            <span
              className={`inline-flex items-center rounded-full border px-2.5 py-1 text-[11px] font-semibold ${performanceBadgeClass}`}
            >
              {displayMeta.performance.label}
            </span>

            <span
              className={`inline-flex items-center rounded-full border px-2.5 py-1 text-[11px] font-medium ${reservationBadgeClass}`}
            >
              {displayMeta.reservation.label}
            </span>
          </div>
        </div>

        <div className="space-y-2 text-[13px] text-black/52">
          <div className="flex items-center gap-2">
            <Calendar className="size-3.5 shrink-0" />
            <span>{formatShowPeriod(event)}</span>
          </div>
          <div className="flex items-center gap-2">
            <MapPin className="size-3.5 shrink-0" />
            <span>{event.venue}</span>
          </div>
        </div>

        <div className="flex items-center justify-between border-t border-black/8 pt-3">
          <span className="text-[13px] text-black/45">
            구매 제한 {event.purchaseLimit}매
          </span>
          <Link
            href={`/shows/${event.showId}/dashboard`}
            className="rounded-full bg-[#171717] px-3.5 py-1.5 text-[11px] font-semibold text-white transition-colors hover:bg-black/85"
            onClick={(e) => e.stopPropagation()}
          >
            대시보드
          </Link>
        </div>
      </div>
    </article>
  )
}
