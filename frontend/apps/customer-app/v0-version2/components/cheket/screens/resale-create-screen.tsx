'use client'

import { useState } from 'react'
import { AlertCircle, CheckCircle } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import type { ResaleItem } from '@/lib/types'

export function ResaleCreateScreen() {
  const { navParams, goBack, navigate, tickets, updateTicketStatus, addResaleItem, user, addTx } = useApp()
  const ticket = tickets.find((t) => t.id === navParams.ticketId)
  const [price, setPrice] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  if (!ticket || !user) return null

  const priceNum = parseInt(price.replace(/,/g, ''), 10)
  const isValid = !isNaN(priceNum) && priceNum > 0 && priceNum <= ticket.originalPrice
  const isOverPrice = !isNaN(priceNum) && priceNum > ticket.originalPrice

  const handleSubmit = () => {
    if (!isValid) {
      setError('판매 가격을 다시 확인해 주세요.')
      return
    }

    const resaleItem: ResaleItem = {
      id: `rs_${Date.now()}`,
      ticketId: ticket.id,
      eventName: ticket.eventName,
      eventDate: ticket.eventDate,
      venue: ticket.venue,
      poster: ticket.poster,
      seatLabel: ticket.seatLabel,
      grade: ticket.grade,
      originalPrice: ticket.originalPrice,
      resalePrice: priceNum,
      sellerId: user.id,
    }

    addResaleItem(resaleItem)
    updateTicketStatus(ticket.id, { status: 'LISTED', resalePrice: priceNum })
    addTx('RESALE_LIST', `${ticket.eventName} 재판매 등록`, priceNum)
    setSuccess(true)
  }

  if (success) {
    return (
      <div className="flex min-h-full flex-col items-center justify-center gap-6 bg-background p-6 text-center">
        <div className="flex h-20 w-20 items-center justify-center rounded-full border-2 border-primary bg-primary/10">
          <CheckCircle className="h-10 w-10 text-primary" />
        </div>

        <div>
          <h2 className="mb-2 text-xl font-bold text-foreground">판매 등록 완료!</h2>
          <p className="text-sm text-muted-foreground">
            {priceNum.toLocaleString()} CTK에 판매 등록되었어요.
            <br />
            판매 전까지는 언제든지 취소할 수 있어요.
          </p>
          <p className="mt-2 text-xs text-muted-foreground">등록 상태는 내 티켓에서 바로 확인할 수 있어요.</p>
        </div>

        <button
          onClick={() => navigate('my-tickets')}
          className="w-full rounded-xl bg-primary py-3.5 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98]"
        >
          내 티켓 보러가기
        </button>
      </div>
    )
  }

  return (
    <AppShell showBack onBack={goBack} title="판매 등록" showBottomNav={false}>
      <div className="flex flex-col gap-4 p-4">
        <div className="overflow-hidden rounded-xl border border-border bg-card">
          <div className="relative aspect-[2.5/1] w-full">
            <img src={ticket.poster} alt={ticket.eventName} className="h-full w-full object-cover" />
            <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
            <div className="absolute bottom-3 left-4 right-4">
              <h3 className="text-base font-bold text-white drop-shadow-lg">{ticket.eventName}</h3>
            </div>
          </div>

          <div className="flex flex-col gap-2 p-4">
            <div className="flex items-center justify-between">
              <span className="text-sm text-foreground">{ticket.seatLabel}</span>
              <span className="rounded-full bg-primary/10 px-2 py-0.5 text-xs font-semibold text-primary">
                {ticket.grade}
              </span>
            </div>
            <div className="flex items-center justify-between border-t border-border pt-2">
              <span className="text-xs text-muted-foreground">원가 (최대 판매가)</span>
              <span className="text-sm font-bold text-foreground">{ticket.originalPrice.toLocaleString()} CTK</span>
            </div>
          </div>
        </div>

        <div>
          <label className="mb-1.5 block text-xs font-medium text-muted-foreground">리세일 가격 설정</label>
          <div className="relative">
            <input
              className="w-full rounded-xl border border-border bg-secondary px-4 py-3.5 pr-14 text-sm text-foreground transition-colors placeholder:text-muted-foreground focus:border-primary focus:outline-none"
              placeholder="가격 입력"
              value={price}
              onChange={(e) => {
                setPrice(e.target.value)
                setError('')
              }}
              type="number"
              min={1}
              max={ticket.originalPrice}
            />
            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm font-medium text-muted-foreground">CTK</span>
          </div>

          {isOverPrice ? (
            <div className="mt-2 flex items-center gap-2">
              <AlertCircle className="h-3.5 w-3.5 text-red-400" />
              <p className="text-xs text-red-400">원가({ticket.originalPrice.toLocaleString()} CTK)를 초과할 수 없어요.</p>
            </div>
          ) : null}

          {error ? <p className="mt-1 text-xs text-red-400">{error}</p> : null}

          {isValid ? (
            <p className="mt-1 text-xs text-primary">
              원가 대비 {(((ticket.originalPrice - priceNum) / ticket.originalPrice) * 100).toFixed(0)}% 할인
            </p>
          ) : null}
        </div>

        <div className="rounded-xl bg-secondary p-4 text-xs leading-relaxed text-muted-foreground">
          <p className="mb-1 font-semibold text-foreground">안내사항</p>
          <p>리세일 가격은 정가 이하로만 설정 가능합니다.</p>
          <p>등록 후 리세일 마켓에 즉시 노출됩니다.</p>
          <p>판매 전까지 언제든지 취소할 수 있습니다.</p>
        </div>

        <p className="px-1 text-xs text-muted-foreground">등록이 완료되면 리세일 마켓에 바로 노출돼요.</p>

        <button
          onClick={handleSubmit}
          disabled={!isValid}
          className="mt-auto w-full rounded-xl bg-primary py-4 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-40"
        >
          판매 등록
        </button>
      </div>
    </AppShell>
  )
}
