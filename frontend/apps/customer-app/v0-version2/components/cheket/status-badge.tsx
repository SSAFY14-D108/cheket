'use client'

import type { TicketStatus } from '@/lib/types'
import { cn } from '@/lib/utils'

const STATUS_MAP: Record<TicketStatus, { label: string; className: string }> = {
  SOLD: { label: '보유중', className: 'badge-status text-[#5a6fff] shadow-[0_6px_14px_rgba(90,111,255,0.12)]' },
  LISTED: { label: '판매중', className: 'badge-status text-[#f08a3c] shadow-[0_6px_14px_rgba(240,138,60,0.12)]' },
  USED: { label: '사용완료', className: 'badge-status text-[#707a86] shadow-[0_6px_14px_rgba(112,122,134,0.1)]' },
  EXPIRED: { label: '만료됨', className: 'badge-status text-[#e05a5a] shadow-[0_6px_14px_rgba(224,90,90,0.12)]' },
}

export function TicketStatusBadge({ status }: { status: TicketStatus }) {
  const { label, className } = STATUS_MAP[status]
  return <span className={cn(className)}>{label}</span>
}

type EventStatusType = 'ON_SALE' | 'SOLD_OUT'

const EVENT_STATUS_MAP: Record<EventStatusType, { label: string; className: string }> = {
  ON_SALE: { label: '예매중', className: 'badge-status text-[#5f6b76]' },
  SOLD_OUT: { label: '매진', className: 'badge-status text-[#f08a3c] shadow-[0_6px_14px_rgba(240,138,60,0.12)]' },
}

export function EventStatusBadge({ status }: { status: EventStatusType }) {
  const { label, className } = EVENT_STATUS_MAP[status]
  return <span className={cn(className)}>{label}</span>
}
