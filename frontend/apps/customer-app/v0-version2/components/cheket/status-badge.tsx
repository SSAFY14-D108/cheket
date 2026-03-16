'use client'

import type { TicketStatus } from '@/lib/types'
import { cn } from '@/lib/utils'

const STATUS_MAP: Record<TicketStatus, { label: string; className: string }> = {
  SOLD: { label: '보유중', className: 'border border-blue-600/30 bg-blue-600/15 text-blue-500' },
  LISTED: { label: '판매중', className: 'border border-orange-500/30 bg-orange-500/15 text-orange-500' },
  USED: { label: '사용완료', className: 'border border-zinc-500/30 bg-zinc-500/15 text-zinc-500' },
  EXPIRED: { label: '만료됨', className: 'border border-red-500/30 bg-red-500/15 text-red-500' },
}

export function TicketStatusBadge({ status }: { status: TicketStatus }) {
  const { label, className } = STATUS_MAP[status]
  return <span className={cn('rounded-full px-2 py-0.5 text-[11px] font-semibold', className)}>{label}</span>
}

type EventStatusType = 'ON_SALE' | 'SOLD_OUT'

const EVENT_STATUS_MAP: Record<EventStatusType, { label: string; className: string }> = {
  ON_SALE: { label: '예매중', className: 'border border-[#d7dde6] bg-white text-[#333333]' },
  SOLD_OUT: { label: '매진', className: 'border border-orange-500/30 bg-orange-500/15 text-orange-500' },
}

export function EventStatusBadge({ status }: { status: EventStatusType }) {
  const { label, className } = EVENT_STATUS_MAP[status]
  return <span className={cn('rounded-full px-2 py-0.5 text-[11px] font-semibold', className)}>{label}</span>
}
