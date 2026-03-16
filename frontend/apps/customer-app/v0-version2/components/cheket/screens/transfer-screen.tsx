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
      setVerifyError('전화번호를 정확히 입력해 주세요.')
      return
    }

    setIsVerifying(true)
    setTimeout(() => {
      const mockBook: Record<string, string> = {
        '010-9876-5432': '김체킷',
        '010-1234-5678': '박티켓',
        '010-5555-4444': '이양도',
      }

      const found = mockBook[phone]
      if (found) {
        setVerifiedName(found)
        setVerifyError('')
      } else {
        setVerifyError('CHEKET 회원 정보를 찾을 수 없어요.')
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
    <AppShell showBack onBack={goBack} title="지인에게 양도" showBottomNav={false}>
      <div className="flex flex-col gap-5 p-4">
        <div>
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">양도할 티켓</p>
          <div className="flex items-center gap-3 overflow-hidden rounded-2xl border border-border bg-card p-3">
            <div className="relative h-14 w-14 flex-shrink-0 overflow-hidden rounded-xl bg-secondary">
              <Image src={ticket.poster} alt={ticket.eventName} fill className="object-cover" sizes="56px" />
            </div>
            <div className="min-w-0 space-y-1">
              <p className="truncate text-sm font-semibold text-foreground">{ticket.eventName}</p>
              <div className="flex items-center gap-1 text-xs text-muted-foreground">
                <Calendar className="h-3 w-3" />
                <span>{ticket.eventDate}</span>
              </div>
              <div className="flex items-center gap-1 text-xs text-muted-foreground">
                <MapPin className="h-3 w-3" />
                <span className="truncate">{ticket.venue}</span>
              </div>
              <span className="mt-0.5 inline-flex items-center gap-1 text-xs font-semibold text-primary">
                <span className="h-1.5 w-1.5 rounded-full bg-primary" />
                {ticket.grade} {ticket.seatLabel}
              </span>
            </div>
          </div>
        </div>

        <div>
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">받는 사람 정보</p>
          <p className="mb-2 text-sm font-medium text-foreground">전화번호 확인</p>
          <div className="flex gap-2">
            <input
              type="tel"
              value={phone}
              onChange={handlePhoneChange}
              placeholder="010-0000-0000"
              className="flex-1 rounded-xl border border-border bg-secondary px-4 py-3 text-sm text-foreground transition-colors placeholder:text-muted-foreground focus:border-primary focus:outline-none"
            />
            <button
              onClick={handleVerify}
              disabled={isVerifying || phone.replace(/\D/g, '').length < 11}
              className="whitespace-nowrap rounded-xl bg-primary px-4 py-3 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-40"
            >
              {isVerifying ? '확인 중...' : '확인'}
            </button>
          </div>

          {verifiedName ? (
            <div className="mt-3 flex items-center gap-3 rounded-xl border border-primary/30 bg-primary/10 p-3">
              <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full bg-primary/20">
                <User className="h-4 w-4 text-primary" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-sm font-semibold text-foreground">{verifiedName} 님에게 양도할 수 있어요.</p>
                <p className="mt-0.5 text-xs text-primary">CHEKET 회원 확인 완료</p>
              </div>
              <CheckCircle2 className="h-5 w-5 flex-shrink-0 text-primary" />
            </div>
          ) : null}

          {verifyError ? (
            <div className="mt-3 flex items-center gap-2 rounded-xl border border-destructive/30 bg-destructive/10 p-3">
              <AlertCircle className="h-4 w-4 flex-shrink-0 text-destructive" />
              <p className="text-xs text-destructive">{verifyError}</p>
            </div>
          ) : null}
        </div>

        <div className="rounded-xl border border-amber-500/20 bg-amber-500/10 p-4">
          <div className="mb-2 flex items-center gap-2">
            <AlertCircle className="h-4 w-4 text-amber-400" />
            <p className="text-sm font-semibold text-amber-400">유의사항</p>
          </div>
          <ul className="space-y-1 text-xs leading-relaxed text-muted-foreground">
            <li>양도 완료 후에는 티켓 상태가 즉시 변경될 수 있어요.</li>
            <li>잘못 전달한 티켓은 되돌리기 어려울 수 있어요.</li>
            <li>수신자 확인 후 진행하는 것을 권장해요.</li>
          </ul>
        </div>

        <p className="px-1 text-xs text-muted-foreground">양도 후에는 되돌리기 어려우니 받는 사람 정보를 다시 확인해 주세요.</p>

        <button
          onClick={handleTransfer}
          disabled={!verifiedName || isSubmitting}
          className="w-full rounded-xl bg-primary py-4 text-sm font-semibold text-primary-foreground transition-all hover:opacity-90 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-40"
        >
          {isSubmitting ? '양도 처리 중...' : '양도하기'}
        </button>
      </div>
    </AppShell>
  )
}
