'use client'

import { useState } from 'react'
import Image from 'next/image'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { Calendar, MapPin, CheckCircle2, AlertCircle, User } from 'lucide-react'

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
      setVerifyError('올바른 전화번호를 입력해주세요.')
      return
    }
    setIsVerifying(true)
    setTimeout(() => {
      // Mock lookup — 010-9876-5432 and 010-1234-5678 are valid
      const MOCK_BOOK: Record<string, string> = {
        '010-9876-5432': '박지연',
        '010-1234-5678': '김민준',
        '010-5555-4444': '이수진',
      }
      const found = MOCK_BOOK[phone]
      if (found) {
        setVerifiedName(found)
        setVerifyError('')
      } else {
        setVerifyError('CHEKET 회원이 아니거나 등록되지 않은 번호입니다.')
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
        addTx('TRANSFER', `${ticket.eventName} 양도 — ${verifiedName}`)
        navigate('transfer-complete', {
          ticketId: ticket.id,
          recipientName: result.recipientName,
          recipientPhone: phone,
        })
      } else {
        navigate('transfer-failed', {
          ticketId: ticket.id,
          recipientName: verifiedName,
          recipientPhone: phone,
          transferFailureReason: result.reason ?? 'NETWORK',
        })
      }
    }, 1200)
  }

  return (
    <AppShell showBack onBack={goBack} title="지인에게 양도" showBottomNav={false}>
      <div className="p-4 flex flex-col gap-5">

        {/* Ticket summary */}
        <div>
          <p className="text-xs font-semibold text-muted-foreground mb-2 uppercase tracking-wide">양도할 티켓</p>
          <div className="bg-card border border-border rounded-2xl overflow-hidden flex items-center gap-3 p-3">
            <div className="relative w-14 h-14 rounded-xl overflow-hidden flex-shrink-0 bg-secondary">
              <Image src={ticket.poster} alt={ticket.eventName} fill className="object-cover" sizes="56px" />
            </div>
            <div className="flex flex-col gap-1 min-w-0">
              <p className="font-semibold text-sm text-foreground truncate">{ticket.eventName}</p>
              <div className="flex items-center gap-1 text-xs text-muted-foreground">
                <Calendar className="w-3 h-3" />
                <span>{ticket.eventDate}</span>
              </div>
              <div className="flex items-center gap-1 text-xs text-muted-foreground">
                <MapPin className="w-3 h-3" />
                <span className="truncate">{ticket.venue}</span>
              </div>
              <span className="inline-flex items-center gap-1 text-xs font-semibold text-primary mt-0.5">
                <span className="w-1.5 h-1.5 rounded-full bg-primary" />
                {ticket.grade} {ticket.seatLabel}
              </span>
            </div>
          </div>
        </div>

        {/* Recipient input */}
        <div>
          <p className="text-xs font-semibold text-muted-foreground mb-2 uppercase tracking-wide">받는 사람</p>
          <p className="text-sm font-medium text-foreground mb-2">휴대폰 번호</p>
          <div className="flex gap-2">
            <input
              type="tel"
              value={phone}
              onChange={handlePhoneChange}
              placeholder="010-0000-0000"
              className="flex-1 bg-secondary border border-border rounded-xl py-3 px-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
            />
            <button
              onClick={handleVerify}
              disabled={isVerifying || phone.replace(/\D/g, '').length < 11}
              className="px-4 py-3 bg-primary text-primary-foreground text-sm font-semibold rounded-xl hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed whitespace-nowrap"
            >
              {isVerifying ? '확인중...' : '확인'}
            </button>
          </div>

          {/* Verified user card */}
          {verifiedName && (
            <div className="mt-3 flex items-center gap-3 bg-primary/10 border border-primary/30 rounded-xl p-3">
              <div className="w-9 h-9 rounded-full bg-primary/20 flex items-center justify-center flex-shrink-0">
                <User className="w-4 h-4 text-primary" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-foreground">{verifiedName} 님에게 양도합니다</p>
                <p className="text-xs text-primary mt-0.5">CHEKET 회원 인증 완료</p>
              </div>
              <CheckCircle2 className="w-5 h-5 text-primary flex-shrink-0" />
            </div>
          )}

          {/* Error */}
          {verifyError && (
            <div className="mt-3 flex items-center gap-2 bg-destructive/10 border border-destructive/30 rounded-xl p-3">
              <AlertCircle className="w-4 h-4 text-destructive flex-shrink-0" />
              <p className="text-xs text-destructive">{verifyError}</p>
            </div>
          )}
        </div>

        {/* Notice */}
        <div className="bg-amber-500/10 border border-amber-500/20 rounded-xl p-4">
          <div className="flex items-center gap-2 mb-2">
            <AlertCircle className="w-4 h-4 text-amber-400" />
            <p className="text-sm font-semibold text-amber-400">유의사항</p>
          </div>
          <ul className="text-xs text-muted-foreground space-y-1 leading-relaxed">
            <li>• 양도가 완료되면 즉시 티켓이 전송됩니다.</li>
            <li>• 양도 과정에서 발생하는 수수료는 없습니다.</li>
            <li>• 완료된 양도는 취소할 수 없습니다.</li>
          </ul>
        </div>

        <button
          onClick={handleTransfer}
          disabled={!verifiedName || isSubmitting}
          className="w-full bg-primary text-primary-foreground font-semibold py-4 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed"
        >
          {isSubmitting ? '양도 처리중...' : `양도하기 →`}
        </button>
      </div>
    </AppShell>
  )
}
