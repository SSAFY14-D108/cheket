'use client'

import { useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { TicketCard } from '../ticket-card'
import { EmptyState } from '../empty-state'

type TicketFilter = 'all' | 'holding' | 'listing'

const FILTER_OPTIONS: { id: TicketFilter; label: string }[] = [
  { id: 'all', label: '전체' },
  { id: 'holding', label: '보유중' },
  { id: 'listing', label: '판매중' },
]

export function MyTicketsScreen() {
  const { tickets, navigate } = useApp()
  const [activeFilter, setActiveFilter] = useState<TicketFilter>('all')

  const visibleTickets = tickets.filter((ticket) => {
    if (activeFilter === 'all') return ticket.status === 'SOLD' || ticket.status === 'LISTED'
    if (activeFilter === 'holding') return ticket.status === 'SOLD'
    return ticket.status === 'LISTED'
  })

  return (
    <AppShell title="내 티켓">
      <div className="flex flex-col gap-4 bg-gray-50 p-4">
        <div className="flex items-center justify-between gap-3">
          <div className="-mx-4 flex min-w-0 items-center gap-2 overflow-x-auto px-4 pb-1 scrollbar-hide">
            {FILTER_OPTIONS.map((filter) => (
              <button
                key={filter.id}
                type="button"
                onClick={() => setActiveFilter(filter.id)}
                className={`flex-shrink-0 rounded-full px-3 py-1.5 text-xs font-medium transition-colors ${
                  activeFilter === filter.id ? 'bg-[#eef2f1] text-[#111111]' : 'bg-transparent text-muted-foreground'
                }`}
              >
                {filter.label}
              </button>
            ))}
          </div>
          <button
            type="button"
            onClick={() => navigate('collection')}
            className="gradient-border-button flex-shrink-0 rounded-full px-4 py-2 text-xs font-semibold text-foreground shadow-[0_10px_26px_rgba(15,23,42,0.04)]"
          >
            컬렉션
          </button>
        </div>

        {visibleTickets.length === 0 ? (
          <EmptyState
            title={
              activeFilter === 'all'
                ? '티켓이 없어요'
                : activeFilter === 'holding'
                  ? '보유 중인 티켓이 없어요'
                  : '판매 중인 티켓이 없어요'
            }
            description={
              activeFilter === 'all'
                ? '예매하거나 양도받은 티켓이 여기에 표시됩니다.'
                : activeFilter === 'holding'
                  ? '아직 보유 중인 티켓이 없습니다.'
                  : '현재 판매 중인 티켓이 없습니다.'
            }
          />
        ) : (
          <div className="flex flex-col gap-3">
            {visibleTickets.map((ticket) => (
              <TicketCard key={ticket.id} ticket={ticket} onClick={() => navigate('ticket-detail', { ticketId: ticket.id })} />
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
