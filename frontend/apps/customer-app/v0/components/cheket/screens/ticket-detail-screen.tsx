'use client'

import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { TicketStatusBadge } from '../status-badge'
import { QrCode, ArrowRightLeft, ShoppingBag, X, Calendar, MapPin } from 'lucide-react'

export function TicketDetailScreen() {
  const { navParams, navigate, goBack, tickets } = useApp()
  const ticket = tickets.find((t) => t.id === navParams.ticketId)

  if (!ticket) return null

  return (
    <AppShell showBack onBack={goBack} title="티켓 상세" showBottomNav={false}>
      <div className="flex flex-col gap-4 p-4">
        {/* Poster + header */}
        <div className="relative w-full aspect-video rounded-xl overflow-hidden bg-secondary">
          <Image src={ticket.poster} alt={ticket.eventName} fill className="object-cover" sizes="390px" />
          <div className="absolute inset-0 bg-gradient-to-t from-background/90 via-transparent to-transparent" />
          <div className="absolute bottom-3 left-3">
            <TicketStatusBadge status={ticket.status} />
          </div>
        </div>

        {/* Ticket info card */}
        <div className="bg-card rounded-xl border border-border p-4 flex flex-col gap-3">
          <h2 className="font-bold text-base text-foreground">{ticket.eventName}</h2>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Calendar className="w-4 h-4 text-primary" />
            <span>{ticket.eventDate}</span>
          </div>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <MapPin className="w-4 h-4 text-primary" />
            <span>{ticket.venue}</span>
          </div>
          <div className="h-px bg-border" />
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">좌석</span>
            <span className="text-sm font-semibold text-foreground">{ticket.seatLabel}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">등급</span>
            <span className="text-sm font-semibold text-primary">{ticket.grade}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">원가</span>
            <span className="text-sm font-semibold text-foreground">{ticket.originalPrice.toLocaleString()} CTK</span>
          </div>
          {ticket.resalePrice && (
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">리세일 가격</span>
              <span className="text-sm font-semibold text-orange-400">{ticket.resalePrice.toLocaleString()} CTK</span>
            </div>
          )}
        </div>

        {/* NFT info */}
        <div className="bg-secondary rounded-xl p-4 text-xs text-muted-foreground">
          <p className="font-semibold text-foreground mb-1.5">NFT 정보</p>
          <p>Token ID: #{ticket.id.split('_').pop()?.toUpperCase()}</p>
          <p className="mt-1">Owner: 0x3a9F...dE42</p>
        </div>

        {/* Action buttons */}
        <div className="flex flex-col gap-3 mt-2">
          {ticket.status === 'SOLD' && (
            <>
              <button
                onClick={() => navigate('qr-checkin', { ticketId: ticket.id })}
                className="w-full flex items-center justify-center gap-2 bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all"
              >
                <QrCode className="w-4 h-4" /> QR 체크인
              </button>
              <div className="grid grid-cols-2 gap-3">
                <button
                  onClick={() => navigate('transfer', { ticketId: ticket.id })}
                  className="flex items-center justify-center gap-2 bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm hover:border-primary/50 active:scale-[0.98] transition-all"
                >
                  <ArrowRightLeft className="w-4 h-4" /> 양도하기
                </button>
                <button
                  onClick={() => navigate('resale-create', { ticketId: ticket.id })}
                  className="flex items-center justify-center gap-2 bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm hover:border-primary/50 active:scale-[0.98] transition-all"
                >
                  <ShoppingBag className="w-4 h-4" /> 판매하기
                </button>
              </div>
            </>
          )}
          {ticket.status === 'LISTED' && (
            <CancelListingButton ticketId={ticket.id} />
          )}
          {ticket.status === 'USED' && (
            <button
              disabled
              className="w-full flex items-center justify-center gap-2 bg-secondary text-muted-foreground font-semibold py-3.5 rounded-xl text-sm cursor-not-allowed opacity-60"
            >
              <QrCode className="w-4 h-4" /> 사용 완료
            </button>
          )}
        </div>
      </div>
    </AppShell>
  )
}

function CancelListingButton({ ticketId }: { ticketId: string }) {
  const { updateTicketStatus, removeResaleItem, resaleItems, goBack } = useApp()

  const handleCancel = () => {
    const resaleItem = resaleItems.find((r) => r.ticketId === ticketId)
    if (resaleItem) removeResaleItem(resaleItem.id)
    updateTicketStatus(ticketId, { status: 'SOLD', resalePrice: undefined })
    goBack()
  }

  return (
    <button
      onClick={handleCancel}
      className="w-full flex items-center justify-center gap-2 bg-red-600/20 border border-red-600/30 text-red-400 font-semibold py-3.5 rounded-xl text-sm hover:bg-red-600/30 active:scale-[0.98] transition-all"
    >
      <X className="w-4 h-4" /> 리스팅 취소
    </button>
  )
}
