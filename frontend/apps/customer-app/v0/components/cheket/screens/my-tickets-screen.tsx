'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { TicketCard } from '../ticket-card'
import { EmptyState } from '../empty-state'

export function MyTicketsScreen() {
  const { tickets, navigate } = useApp()

  return (
    <AppShell>
      <div className="flex flex-col gap-4 p-4">
        <div className="flex items-center gap-3 overflow-x-auto pb-1 -mx-4 px-4 scrollbar-hide">
          {(['전체', '보유중', '판매중', '사용됨', '만료됨'] as const).map((label) => (
            <span
              key={label}
              className="flex-shrink-0 px-3 py-1.5 rounded-full text-xs font-medium bg-secondary text-muted-foreground"
            >
              {label}
            </span>
          ))}
        </div>

        {tickets.length === 0 ? (
          <EmptyState
            title="보유한 티켓이 없습니다"
            description="공연을 검색하고 첫 번째 NFT 티켓을 구매해보세요."
          />
        ) : (
          <div className="flex flex-col gap-3">
            {tickets.map((ticket) => (
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
