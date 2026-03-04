'use client'

import { useState } from 'react'
import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { AlertCircle, MapPin, Calendar, Tag } from 'lucide-react'

export function ResaleDetailScreen() {
  const { navParams, navigate, goBack, resaleItems, user, buyResaleTicket } = useApp()
  const item = resaleItems.find((r) => r.id === navParams.resaleItemId)
  const [error, setError] = useState('')

  if (!item) return null
  if (!user) return null

  const hasSufficientBalance = user.ctkBalance >= item.resalePrice
  const discount = item.originalPrice - item.resalePrice
  const discountPct = Math.round((discount / item.originalPrice) * 100)

  const handleBuy = () => {
    const newTicketId = buyResaleTicket(item.id)
    if (newTicketId) {
      navigate('resale-purchase-complete', {
        resaleItemId: item.id,
        purchasedTicketId: newTicketId,
      })
    } else {
      setError('구매에 실패했습니다. 잔액을 확인해주세요.')
    }
  }

  return (
    <AppShell showBack onBack={goBack} title="리세일 상세" showBottomNav={false}>
      <div className="flex flex-col gap-4 p-4">
        {/* Poster */}
        <div className="relative w-full aspect-video rounded-xl overflow-hidden bg-secondary">
          <Image src={item.poster} alt={item.eventName} fill className="object-cover" sizes="390px" />
        </div>

        {/* Event info */}
        <div className="bg-card rounded-xl border border-border p-4 flex flex-col gap-2">
          <h2 className="font-bold text-base text-foreground">{item.eventName}</h2>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Calendar className="w-4 h-4 text-primary" />
            <span>{item.eventDate}</span>
          </div>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <MapPin className="w-4 h-4 text-primary" />
            <span>{item.venue}</span>
          </div>
        </div>

        {/* Ticket info */}
        <div className="bg-card rounded-xl border border-border p-4 flex flex-col gap-3">
          <h3 className="font-semibold text-sm text-foreground">좌석 정보</h3>
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">좌석</span>
            <span className="text-sm font-medium text-foreground">{item.seatLabel} · {item.grade}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm text-muted-foreground">원가</span>
            <span className="text-sm text-muted-foreground line-through">{item.originalPrice.toLocaleString()} CTK</span>
          </div>
          <div className="h-px bg-border" />
          <div className="flex items-center justify-between">
            <span className="font-semibold text-sm text-foreground">리세일 가격</span>
            <div className="flex items-center gap-2">
              {discountPct > 0 && (
                <span className="flex items-center gap-0.5 text-xs text-primary font-medium">
                  <Tag className="w-3 h-3" />{discountPct}% 할인
                </span>
              )}
              <span className="font-bold text-primary text-base">{item.resalePrice.toLocaleString()} CTK</span>
            </div>
          </div>
        </div>

        {/* Balance warning */}
        {!hasSufficientBalance && (
          <div className="flex items-center gap-2 p-3 bg-red-50 border border-red-200 rounded-xl">
            <AlertCircle className="w-4 h-4 text-red-500 flex-shrink-0" />
            <p className="text-xs text-red-500">
              잔액이 부족합니다. 현재 잔액: {user.ctkBalance.toLocaleString()} CTK
            </p>
          </div>
        )}

        {/* Notice */}
        <div className="bg-secondary rounded-xl p-4 text-xs text-muted-foreground leading-relaxed">
          <p className="font-semibold text-foreground mb-1">유의사항</p>
          <p>• 리세일 가격은 정가 이하로만 설정 가능합니다.</p>
          <p>• 구매 즉시 에스크로 처리 후 소유권이 이전됩니다.</p>
          <p>• 블록체인 처리로 취소가 불가합니다.</p>
        </div>

        {error && (
          <div className="flex items-center gap-2 p-3 bg-red-50 border border-red-200 rounded-xl">
            <AlertCircle className="w-4 h-4 text-red-500" />
            <p className="text-xs text-red-500">{error}</p>
          </div>
        )}

        <button
          onClick={handleBuy}
          disabled={!hasSufficientBalance}
          className="w-full bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed"
        >
          구매하기
        </button>
      </div>
    </AppShell>
  )
}
