'use client'

import { Calendar, Tag, Ticket } from 'lucide-react'
import { ResaleItem } from '@/lib/types'

interface ResaleCardProps {
  item: ResaleItem
  onClick: () => void
}

const LABELS = {
  separator: ' \u00b7 ',
  discount: '\ud560\uc778',
  originalPrice: '\uc815\uac00 ',
} as const

export function ResaleCard({ item, onClick }: ResaleCardProps) {
  const discount = item.originalPrice - item.resalePrice
  const discountPct = Math.round((discount / item.originalPrice) * 100)

  return (
    <button
      onClick={onClick}
      className="w-full rounded-xl border border-border bg-card px-3 py-2.5 text-left transition-all hover:border-primary/40 active:scale-[0.98]"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-1.5 text-sm font-semibold text-foreground">
            <Ticket className="h-3 w-3 flex-shrink-0 text-primary" />
            <span className="truncate">
              {item.seatLabel}
              {LABELS.separator}
              {item.grade}
            </span>
          </div>

          <div className="mt-0.5 flex items-center gap-1.5 text-[11px] text-muted-foreground">
            <Calendar className="h-3 w-3 flex-shrink-0" />
            <span className="truncate">{item.eventDate}</span>
          </div>

          <div className="mt-1.5 flex items-end gap-2">
            <span className="text-lg font-bold leading-none text-foreground">
              {item.resalePrice.toLocaleString()} CTK
            </span>
            <p className="text-[11px] text-muted-foreground line-through">
              {LABELS.originalPrice}
              {item.originalPrice.toLocaleString()} CTK
            </p>
          </div>
        </div>

        {discount > 0 && (
          <span className="mt-0.5 inline-flex flex-shrink-0 items-center gap-1 rounded-full bg-primary/10 px-2 py-0.5 text-[10px] font-semibold text-primary">
            <Tag className="h-2.5 w-2.5" />
            {discountPct}% {LABELS.discount}
          </span>
        )}
      </div>
    </button>
  )
}
