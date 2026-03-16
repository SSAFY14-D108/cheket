'use client'

import { useState } from 'react'
import Image from 'next/image'
import { AlertCircle, Calendar, CheckCircle2, MapPin, User } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'

export function TransferScreen() {
  const { navParams, goBack, navigate, tickets, transferTicket, addTx } = useApp()
  const ticket = tickets.find((t) => t.id === navParams.ticketId)

  const [phone, setPhone] = useState('')
  const [verifiedName, setVerifiedName] = useState<string | null>(null)
  const [verifyError, setVerifyError] = useState('')
  const [isVerifying, setIsVerifying] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (!ticket) return null

  const formatPhone = (value: string) => {
    const digits = value.replace(/\D/g, '').slice(0, 11)
    if (digits.length <= 3) return digits
    if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`
    return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`
  }

  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPhone(formatPhone(e.target.value))
    setVerifiedName(null)
    setVerifyError('')
  }

  const handleVerify = () => {
    if (phone.replace(/\D/g, '').length < 11) {
      setVerifyError('전화번호 11자리를 정확히 입력해 주세요.')
      return
    }

    setIsVerifying(true)
    setTimeout(() => {
      const mockBook: Record<string, string> = {
        '010-9876-5432': '이지현',
        '010-1234-5678': '김민준',
        '010-5555-4444': '박서윤',
      }

      const found = mockBook[phone]
      if (found) {
        setVerifiedName(found)
        setVerifyError('')
      } else {
        setVerifyError('CHEKET 가입 이력이 없는 번호예요.')
        setVerifiedName(null)
      }
      setIsVerifying(false)
    }, 800)
  }

  const handleTransfer = () => {
    if (!verifiedName) return

    setIsSubmitting(true)
    setTimeout(() => {
      const result = transferTicket(ticket.id, phone)
      setIsSubmitting(false)

      if (result.success) {
        addTx('TRANSFER', `${ticket.eventName} 티켓 양도`)
        navigate('transfer-complete', {
          ticketId: ticket.id,
          recipientName: result.recipientName,
          recipientPhone: phone,
        })
        return
      }

      navigate('transfer-failed', {
        ticketId: ticket.id,
        recipientName: verifiedName,
        recipientPhone: phone,
        transferFailureReason: result.reason ?? 'NETWORK',
      })
    }, 1200)
  }

  return (
    <AppShell showBack onBack={goBack} title="티켓 양도" showBottomNav={false}>
      <div className="flex flex-col gap-5 p-4">
        <div>
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">Transfer Ticket</p>
          <div className="gradient-outline-surface flex items-center gap-3 overflow-hidden rounded-2xl p-3">
            <div className="relative h-14 w-14 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={ticket.poster} alt={ticket.eventName} fill className="object-cover" sizes="56px" />
            </div>
            <div className="min-w-0 space-y-1">
              <p className="truncate text-sm font-semibold text-[#111111]">{ticket.eventName}</p>
              <div className="flex items-center gap-1 text-xs text-muted-foreground">
                <Calendar className="h-3 w-3 text-[#6b7280]" />
                <span>{ticket.eventDate}</span>
              </div>
              <div className="flex items-center gap-1 text-xs text-muted-foreground">
                <MapPin className="h-3 w-3 text-[#6b7280]" />
                <span className="truncate">{ticket.venue}</span>
              </div>
              <span className="mt-0.5 inline-flex items-center gap-1 text-xs font-semibold text-[#333333]">
                <span className="h-1.5 w-1.5 rounded-full bg-[#9aa4b2]" />
                {ticket.grade} {ticket.seatLabel}
              </span>
            </div>
          </div>
        </div>

        <div>
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">Recipient</p>
          <p className="mb-2 text-sm font-medium text-[#111111]">받는 사람 전화번호</p>
          <div className="flex gap-2">
            <input
              type="tel"
              value={phone}
              onChange={handlePhoneChange}
              placeholder="010-0000-0000"
              className="gradient-outline-surface flex-1 rounded-xl px-4 py-3 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none"
            />
            <button
              onClick={handleVerify}
              disabled={isVerifying || phone.replace(/\D/g, '').length < 11}
              className="gradient-outline-button whitespace-nowrap rounded-xl px-4 py-3 text-sm font-semibold text-[#111111] transition-all active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-40"
            >
              {isVerifying ? '확인 중...' : '번호 확인'}
            </button>
          </div>

          {verifiedName ? (
            <div className="gradient-outline-surface mt-3 flex items-center gap-3 rounded-xl p-3">
              <div className="gradient-outline-icon-button flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full">
                <User className="h-4 w-4 text-[#333333]" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-sm font-semibold text-[#111111]">{verifiedName} 님에게 티켓을 보낼 수 있어요.</p>
                <p className="mt-0.5 text-xs text-muted-foreground">CHEKET 가입이 확인된 사용자예요.</p>
              </div>
              <CheckCircle2 className="h-5 w-5 flex-shrink-0 text-[#6b7280]" />
            </div>
          ) : null}

          {verifyError ? (
            <div className="mt-3 flex items-center gap-2 rounded-xl border border-destructive/20 bg-destructive/10 p-3">
              <AlertCircle className="h-4 w-4 flex-shrink-0 text-destructive" />
              <p className="text-xs text-destructive">{verifyError}</p>
            </div>
          ) : null}
        </div>

        <div className="rounded-xl bg-amber-50 px-4 py-4">
          <div className="mb-2 flex items-center gap-2">
            <AlertCircle className="h-4 w-4 text-amber-500" />
            <p className="text-sm font-semibold text-[#111111]">안내사항</p>
          </div>
          <ul className="space-y-1 text-xs leading-relaxed text-muted-foreground">
            <li>양도 완료 후에는 티켓 소유권이 즉시 이전돼요.</li>
            <li>받는 사람 정보가 정확한지 다시 한 번 확인해 주세요.</li>
            <li>판매 중인 티켓은 양도할 수 없어요.</li>
          </ul>
        </div>

        <p className="px-1 text-xs text-muted-foreground">양도 후에는 되돌리기 어려우니 받는 사람 정보를 다시 확인하세요.</p>

        <button
          onClick={handleTransfer}
          disabled={!verifiedName || isSubmitting}
          className="gradient-outline-button w-full rounded-xl py-4 text-sm font-semibold text-[#111111] transition-all active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-40"
        >
          {isSubmitting ? '양도 처리 중...' : '양도하기'}
        </button>
      </div>
    </AppShell>
  )
}
