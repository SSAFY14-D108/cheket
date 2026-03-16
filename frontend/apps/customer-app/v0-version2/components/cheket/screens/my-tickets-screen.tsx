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
        <button type="button" onClick={() => navigate('collection')} className="rounded-2xl bg-white px-4 py-3 text-left shadow-[0_10px_26px_rgba(15,23,42,0.04)] transition-colors">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-foreground">컬렉션</p>
              <p className="mt-1 text-xs text-muted-foreground">사용 완료된 티켓을 컬렉터블 카드로 확인합니다.</p>
            </div>
            <span className="text-sm font-semibold text-[#111111]">보기</span>
          </div>
        </button>

        <div className="-mx-4 flex items-center gap-2 overflow-x-auto px-4 pb-1 scrollbar-hide">
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

        {visibleTickets.length === 0 ? (
          <EmptyState
            title={activeFilter === 'all' ? '표시할 티켓이 없어요.' : activeFilter === 'holding' ? '보유 중인 티켓이 없어요.' : '판매 중인 티켓이 없어요.'}
            description={
              activeFilter === 'all'
                ? '예매하거나 거래한 티켓이 생기면 이곳에서 확인할 수 있어요.'
                : activeFilter === 'holding'
                  ? '예매 완료된 티켓이 생기면 여기에서 확인할 수 있어요.'
                  : '재판매 등록된 티켓이 생기면 여기에서 확인할 수 있어요.'
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
