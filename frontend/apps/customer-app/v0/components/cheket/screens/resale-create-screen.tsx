'use client'

import { useState } from 'react'
import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { AlertCircle, CheckCircle } from 'lucide-react'
import { ResaleItem } from '@/lib/types'

export function ResaleCreateScreen() {
  const { navParams, goBack, navigate, tickets, updateTicketStatus, addResaleItem, user } = useApp()
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
      setError('정가 이하의 가격을 입력해주세요.')
      return
    }
    const resaleItem: ResaleItem = {
      id: `rs_${ticket.id}_${priceNum}`,
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
    setSuccess(true)
  }

  if (success) {
    return (
      <div className="min-h-full flex flex-col items-center justify-center bg-background p-6 gap-6 text-center">
        <div className="w-20 h-20 rounded-full bg-primary/10 border-2 border-primary flex items-center justify-center">
          <CheckCircle className="w-10 h-10 text-primary" />
        </div>
        <div>
          <h2 className="font-bold text-xl text-foreground mb-2">등록 완료!</h2>
          <p className="text-muted-foreground text-sm">
            {priceNum.toLocaleString()} CTK에 리세일 등록되었습니다.<br />
            상태가 <span className="text-orange-400 font-semibold">판매중</span>으로 변경되었습니다.
          </p>
        </div>
        <button
          onClick={() => navigate('my-tickets')}
          className="w-full bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all"
        >
          내 티켓 보기
        </button>
      </div>
    )
  }

  return (
    <AppShell showBack onBack={goBack} title="판매 등록" showBottomNav={false}>
      <div className="p-4 flex flex-col gap-4">
        {/* Ticket summary with poster */}
        <div className="bg-card rounded-xl border border-border overflow-hidden">
          {/* Concert poster */}
          <div className="relative w-full aspect-[2.5/1]">
            <Image
              src={ticket.poster} 
              alt={ticket.eventName}
              fill
              sizes="390px"
              className="object-cover"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
            <div className="absolute bottom-3 left-4 right-4">
              <h3 className="font-bold text-white text-base drop-shadow-lg">{ticket.eventName}</h3>
            </div>
          </div>
          
          {/* Ticket details */}
          <div className="p-4 flex flex-col gap-2">
            <div className="flex items-center justify-between">
              <span className="text-sm text-foreground">{ticket.seatLabel}</span>
              <span className="text-xs text-primary font-semibold px-2 py-0.5 bg-primary/10 rounded-full">{ticket.grade}</span>
            </div>
            <div className="flex items-center justify-between pt-2 border-t border-border">
              <span className="text-xs text-muted-foreground">원가 (최대 판매가)</span>
              <span className="text-sm font-bold text-foreground">{ticket.originalPrice.toLocaleString()} CTK</span>
            </div>
          </div>
        </div>

        {/* Price input */}
        <div>
          <label className="text-xs font-medium text-muted-foreground mb-1.5 block">리세일 가격 설정</label>
          <div className="relative">
            <input
              className="w-full bg-secondary border border-border rounded-xl py-3.5 px-4 pr-14 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
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
            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm text-muted-foreground font-medium">CTK</span>
          </div>

          {isOverPrice && (
            <div className="flex items-center gap-2 mt-2">
              <AlertCircle className="w-3.5 h-3.5 text-red-400" />
              <p className="text-xs text-red-400">정가({ticket.originalPrice.toLocaleString()} CTK) 이하로 설정해야 합니다.</p>
            </div>
          )}
          {error && <p className="text-xs text-red-400 mt-1">{error}</p>}
          {isValid && (
            <p className="text-xs text-primary mt-1">
              정가 대비 {((ticket.originalPrice - priceNum) / ticket.originalPrice * 100).toFixed(0)}% 할인
            </p>
          )}
        </div>

        <div className="bg-secondary rounded-xl p-4 text-xs text-muted-foreground leading-relaxed">
          <p className="font-semibold text-foreground mb-1">안내사항</p>
          <p>• 리세일 가격은 정가 이하로만 설정 가능합니다.</p>
          <p>• 등록 후 리세일 마켓에 즉시 노출됩니다.</p>
          <p>• 판매 전까지 언제든지 취소할 수 있습니다.</p>
        </div>

        <button
          onClick={handleSubmit}
          disabled={!isValid}
          className="w-full bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed mt-auto"
        >
          양도 등록
        </button>
      </div>
    </AppShell>
  )
}
