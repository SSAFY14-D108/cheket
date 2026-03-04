'use client'

import { TicketStatus } from '@/lib/types'
import { cn } from '@/lib/utils'

const STATUS_MAP: Record<TicketStatus, { label: string; className: string }> = {
  SOLD: { label: '보유중', className: 'bg-blue-600/20 text-blue-400 border border-blue-600/30' },
  LISTED: { label: '판매중', className: 'bg-orange-600/20 text-orange-400 border border-orange-600/30' },
  USED: { label: '사용됨', className: 'bg-zinc-600/20 text-zinc-400 border border-zinc-600/30' },
  EXPIRED: { label: '만료됨', className: 'bg-red-600/20 text-red-400 border border-red-600/30' },
}

export function TicketStatusBadge({ status }: { status: TicketStatus }) {
  const { label, className } = STATUS_MAP[status]
  return (
    <span className={cn('text-[11px] font-semibold px-2 py-0.5 rounded-full', className)}>
      {label}
    </span>
  )
}

type EventStatusType = 'ON_SALE' | 'SOLD_OUT' | 'ENDED'
const EVENT_STATUS_MAP: Record<EventStatusType, { label: string; className: string }> = {
  ON_SALE: { label: '판매중', className: 'bg-primary/20 text-primary border border-primary/30' },
  SOLD_OUT: { label: '매진', className: 'bg-orange-600/20 text-orange-400 border border-orange-600/30' },
  ENDED: { label: '종료', className: 'bg-zinc-600/20 text-zinc-400 border border-zinc-600/30' },
}

export function EventStatusBadge({ status }: { status: EventStatusType }) {
  const { label, className } = EVENT_STATUS_MAP[status]
  return (
    <span className={cn('text-[11px] font-semibold px-2 py-0.5 rounded-full', className)}>
      {label}
    </span>
  )
}
