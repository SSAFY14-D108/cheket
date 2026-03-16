'use client'

import { useMemo, useState } from 'react'
import Image from 'next/image'
import { AlertCircle, Calendar, MapPin, Tag } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'

interface ResaleDetailContentProps {
  resaleItemId: string
  embedded?: boolean
}

const LABELS = {
  title: '\uc7ac\ud310\ub9e4 \ud2f0\ucf13',
  buy: '\uad6c\ub9e4\ud558\uae30',
  ticketInfo: '\ud2f0\ucf13 \uc815\ubcf4',
  seat: '\uc88c\uc11d',
  separator: ' \u00b7 ',
  originalPrice: '\uc815\uac00',
  resalePrice: '\uc7ac\ud310\ub9e4\uac00',
  discount: '\ud560\uc778',
  noticeTitle: '\uad6c\ub9e4 \uc548\ub0b4',
  noticeBody1: '\uc7ac\ud310\ub9e4 \ud2f0\ucf13\uc740 \uad6c\ub9e4 \uc989\uc2dc \ub0b4 \ud2f0\ucf13\uc73c\ub85c \uc774\ub3d9\ub429\ub2c8\ub2e4.',
  noticeBody2:
    '\uad6c\ub9e4\uac00 \uc644\ub8cc\ub418\uba74 \ud310\ub9e4\uc790\uc758 \ub9ac\uc2a4\ud305\uc740 \uc790\ub3d9\uc73c\ub85c \uc885\ub8cc\ub429\ub2c8\ub2e4.',
  noticeBody3:
    '\uc794\uc561\uc774 \ubd80\uc871\ud558\uba74 \uad6c\ub9e4\uac00 \ubd88\uac00\ud558\ubbc0\ub85c \ucda9\uc804 \ud6c4 \ub2e4\uc2dc \uc2dc\ub3c4\ud574\uc8fc\uc138\uc694.',
  insufficientPrefix: '\uc794\uc561\uc774 \ubd80\uc871\ud569\ub2c8\ub2e4. \ud604\uc7ac \uc794\uc561: ',
  insufficientSuffix: ' CTK',
  buyFailed:
    '\uad6c\ub9e4\ub97c \uc9c4\ud589\ud560 \uc218 \uc5c6\uc2b5\ub2c8\ub2e4. \uc7a0\uc2dc \ud6c4 \ub2e4\uc2dc \uc2dc\ub3c4\ud574\uc8fc\uc138\uc694.',
  purchaseLabelSuffix: ' \uc7ac\ud310\ub9e4 \uad6c\ub9e4',
} as const

export function ResaleDetailContent({
  resaleItemId,
  embedded = false,
}: ResaleDetailContentProps) {
  const { resaleItems, user, buyResaleTicket, addTx, navigate } = useApp()
  const [error, setError] = useState('')

  const item = useMemo(
    () => resaleItems.find((resaleItem) => resaleItem.id === resaleItemId),
    [resaleItemId, resaleItems]
  )

  if (!item || !user) return null

  const hasSufficientBalance = user.ctkBalance >= item.resalePrice
  const discount = item.originalPrice - item.resalePrice
  const discountPct = Math.round((discount / item.originalPrice) * 100)

  const handleBuy = () => {
    const newTicketId = buyResaleTicket(item.id)
    if (newTicketId) {
      addTx('RESALE_BUY', `${item.eventName}${LABELS.purchaseLabelSuffix}`, item.resalePrice)
      navigate('resale-purchase-complete', {
        resaleItemId: item.id,
        purchasedTicketId: newTicketId,
      })
      return
    }

    setError(LABELS.buyFailed)
  }

  return (
    <div className={`flex flex-col gap-3 ${embedded ? 'px-4 pb-5' : 'p-4'}`}>
      {embedded ? (
        <div className="gradient-outline-surface-soft flex gap-3 rounded-2xl p-3">
          <div className="relative h-20 w-20 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
            <Image
              src={item.poster}
              alt={item.eventName}
              fill
              className="object-cover"
              sizes="80px"
            />
          </div>
          <div className="min-w-0 flex-1">
            <h2 className="line-clamp-2 text-sm font-bold text-foreground">{item.eventName}</h2>
            <div className="mt-1 flex items-center gap-1.5 text-xs text-muted-foreground">
              <Calendar className="h-3.5 w-3.5 text-[#333333]" />
              <span className="truncate">{item.eventDate}</span>
            </div>
            <div className="mt-1 flex items-center gap-1.5 text-xs text-muted-foreground">
              <MapPin className="h-3.5 w-3.5 text-[#333333]" />
              <span className="truncate">{item.venue}</span>
            </div>
            <div className="mt-2 text-sm font-bold text-[#111111]">
              {item.resalePrice.toLocaleString()} CTK
            </div>
          </div>
        </div>
      ) : (
        <>
          <div className="relative aspect-video w-full overflow-hidden rounded-2xl bg-secondary">
            <Image src={item.poster} alt={item.eventName} fill className="object-cover" sizes="390px" />
          </div>

          <div className="gradient-outline-surface-soft flex flex-col gap-2 rounded-2xl p-4">
            <h2 className="text-base font-bold text-foreground">{item.eventName}</h2>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Calendar className="h-4 w-4 text-[#333333]" />
              <span>{item.eventDate}</span>
            </div>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <MapPin className="h-4 w-4 text-[#333333]" />
              <span>{item.venue}</span>
            </div>
          </div>
        </>
      )}

      <div className={`gradient-outline-surface-soft flex flex-col rounded-2xl ${embedded ? 'gap-2 p-3' : 'gap-3 p-4'}`}>
        <h3 className="text-sm font-semibold text-foreground">{LABELS.ticketInfo}</h3>
        <div className="flex items-center justify-between gap-3">
          <span className={`${embedded ? 'text-xs' : 'text-sm'} text-muted-foreground`}>{LABELS.seat}</span>
          <span className={`${embedded ? 'text-xs' : 'text-sm'} font-medium text-foreground`}>
            {item.seatLabel}
            {LABELS.separator}
            {item.grade}
          </span>
        </div>
        <div className="flex items-center justify-between gap-3">
          <span className={`${embedded ? 'text-xs' : 'text-sm'} text-muted-foreground`}>{LABELS.originalPrice}</span>
          <span className={`${embedded ? 'text-xs' : 'text-sm'} text-muted-foreground line-through`}>
            {item.originalPrice.toLocaleString()} CTK
          </span>
        </div>
        <div className="h-px bg-border" />
        <div className="flex items-center justify-between gap-3">
          <span className={`${embedded ? 'text-xs' : 'text-sm'} font-semibold text-foreground`}>{LABELS.resalePrice}</span>
          <div className="flex items-center gap-2">
            {discountPct > 0 && (
              <span className={`flex items-center gap-1 ${embedded ? 'text-[11px]' : 'text-xs'} font-medium text-[#333333]`}>
                <Tag className={`${embedded ? 'h-2.5 w-2.5' : 'h-3 w-3'}`} />
                {discountPct}% {LABELS.discount}
              </span>
            )}
            <span className={`${embedded ? 'text-sm' : 'text-base'} font-bold text-[#111111]`}>
              {item.resalePrice.toLocaleString()} CTK
            </span>
          </div>
        </div>
      </div>

      {!hasSufficientBalance && (
        <div className={`flex items-center gap-2 rounded-2xl border border-red-200 bg-red-50 ${embedded ? 'p-2.5' : 'p-3'}`}>
          <AlertCircle className={`${embedded ? 'h-3.5 w-3.5' : 'h-4 w-4'} flex-shrink-0 text-red-500`} />
          <p className={`${embedded ? 'text-[11px]' : 'text-xs'} text-red-500`}>
            {LABELS.insufficientPrefix}
            {user.ctkBalance.toLocaleString()}
            {LABELS.insufficientSuffix}
          </p>
        </div>
      )}

      <div className={`gradient-outline-surface-soft rounded-2xl ${embedded ? 'p-3 text-[11px]' : 'p-4 text-xs'} leading-relaxed text-muted-foreground`}>
        <p className="mb-1 font-semibold text-foreground">{LABELS.noticeTitle}</p>
        <p>{LABELS.noticeBody1}</p>
        <p>{LABELS.noticeBody2}</p>
        <p>{LABELS.noticeBody3}</p>
      </div>

      {error && (
        <div className={`flex items-center gap-2 rounded-2xl border border-red-200 bg-red-50 ${embedded ? 'p-2.5' : 'p-3'}`}>
          <AlertCircle className={`${embedded ? 'h-3.5 w-3.5' : 'h-4 w-4'} text-red-500`} />
          <p className={`${embedded ? 'text-[11px]' : 'text-xs'} text-red-500`}>{error}</p>
        </div>
      )}

      <button
        onClick={handleBuy}
        disabled={!hasSufficientBalance}
        className={`gradient-outline-button w-full ${embedded ? 'py-3 text-sm' : 'py-4 text-sm'} disabled:cursor-not-allowed disabled:opacity-40`}
      >
        {LABELS.buy}
      </button>
    </div>
  )
}

export function ResaleDetailScreen() {
  const { navParams, goBack } = useApp()
  const resaleItemId = navParams.resaleItemId as string | undefined

  if (!resaleItemId) return null

  return (
    <AppShell showBack onBack={goBack} title={LABELS.title} showBottomNav={false}>
      <ResaleDetailContent resaleItemId={resaleItemId} />
    </AppShell>
  )
}
