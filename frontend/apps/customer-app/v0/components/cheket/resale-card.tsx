'use client'

import Image from 'next/image'
import { ResaleItem } from '@/lib/types'
import { MapPin, Tag } from 'lucide-react'

interface ResaleCardProps {
  item: ResaleItem
  onClick: () => void
}

export function ResaleCard({ item, onClick }: ResaleCardProps) {
  const discount = item.originalPrice - item.resalePrice
  const discountPct = Math.round((discount / item.originalPrice) * 100)

  return (
    <button
      onClick={onClick}
      className="w-full flex gap-3 p-3 bg-card rounded-xl border border-border hover:border-primary/40 active:scale-[0.98] transition-all text-left"
    >
      <div className="relative w-20 h-20 rounded-lg overflow-hidden flex-shrink-0 bg-secondary">
        <Image src={item.poster} alt={item.eventName} fill className="object-cover" sizes="80px" />
      </div>
      <div className="flex-1 min-w-0 py-0.5">
        <h3 className="font-semibold text-sm text-foreground leading-tight line-clamp-1 mb-0.5">
          {item.eventName}
        </h3>
        <p className="text-xs text-muted-foreground mb-1">{item.seatLabel} · {item.grade}</p>
        <div className="flex items-center gap-1 text-muted-foreground text-xs mb-2">
          <MapPin className="w-3 h-3 flex-shrink-0" />
          <span className="truncate">{item.venue}</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="font-bold text-foreground text-sm">
            {item.resalePrice.toLocaleString()} CTK
          </span>
          {discount > 0 && (
            <span className="flex items-center gap-0.5 text-xs text-primary font-medium">
              <Tag className="w-3 h-3" />
              {discountPct}% 할인
            </span>
          )}
        </div>
        <p className="text-xs text-muted-foreground line-through">
          정가 {item.originalPrice.toLocaleString()} CTK
        </p>
      </div>
    </button>
  )
}
