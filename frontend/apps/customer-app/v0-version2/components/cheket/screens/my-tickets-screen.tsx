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
    if (activeFilter === 'all') {
      return ticket.status === 'SOLD' || ticket.status === 'LISTED'
    }

    if (activeFilter === 'holding') {
      return ticket.status === 'SOLD'
    }

    return ticket.status === 'LISTED'
  })

  return (
    <AppShell title="내 티켓">
      <div className="flex flex-col gap-4 p-4">
        <div className="flex items-center gap-3 overflow-x-auto pb-1 -mx-4 px-4 scrollbar-hide">
          {FILTER_OPTIONS.map((filter) => (
            <button
              key={filter.id}
              type="button"
              onClick={() => setActiveFilter(filter.id)}
              className={`flex-shrink-0 rounded-full px-3 py-1.5 text-xs font-medium transition-colors ${
                activeFilter === filter.id
                  ? 'bg-primary text-primary-foreground'
                  : 'bg-secondary text-muted-foreground'
              }`}
            >
              {filter.label}
            </button>
          ))}
        </div>

        {visibleTickets.length === 0 ? (
          <EmptyState
            title={
              activeFilter === 'all'
                ? '표시할 티켓이 없어요'
                : activeFilter === 'holding'
                  ? '보유 중인 티켓이 없어요'
                  : '판매 중인 티켓이 없어요'
            }
            description={
              activeFilter === 'all'
                ? '예매하거나 리세일 등록한 티켓이 여기 표시됩니다.'
                : activeFilter === 'holding'
                ? '예매한 NFT 티켓이 여기 표시됩니다.'
                : '리세일로 등록한 티켓이 여기 표시됩니다.'
            }
          />
        ) : (
          <div className="flex flex-col gap-3">
            {visibleTickets.map((ticket) => (
              <TicketCard
                key={ticket.id}
                ticket={ticket}
                onClick={() => navigate('ticket-detail', { ticketId: ticket.id })}
              />
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
