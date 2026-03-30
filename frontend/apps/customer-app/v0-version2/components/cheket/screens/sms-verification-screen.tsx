'use client'

import { useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'

export function SmsVerificationScreen() {
  const { goBack } = useApp()
  const [phone, setPhone] = useState('')
  const [verificationCode, setVerificationCode] = useState('')
  const [codeSent, setCodeSent] = useState(false)

  const handleSendCode = () => {
    setCodeSent(true)
  }

  const handleVerify = () => {
    console.log('SMS verification submitted')
  }

  return (
    <AppShell title="SMS 인증" showBack onBack={goBack} showBottomNav={false}>
      <div className="flex h-full flex-col">
        <div className="flex-1 overflow-y-auto px-4 py-6">
          <div className="space-y-4">
            <div>
              <label className="mb-2 block text-xs font-semibold text-muted-foreground">휴대폰 번호</label>
              <div className="flex gap-2">
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="010-1234-5678"
                  className="elevated-surface flex-1 rounded-lg px-4 py-3 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none"
                  disabled={codeSent}
                />
                <button onClick={handleSendCode} disabled={!phone || codeSent} className="elevated-surface rounded-lg px-4 py-3 text-sm font-medium text-[#111111] disabled:opacity-40 disabled:cursor-not-allowed">
                  {codeSent ? '전송됨' : '인증번호'}
                </button>
              </div>
            </div>

            {codeSent && (
              <div>
                <label className="mb-2 block text-xs font-semibold text-muted-foreground">인증번호</label>
                <input
                  type="text"
                  value={verificationCode}
                  onChange={(e) => setVerificationCode(e.target.value)}
                  placeholder="6자리 인증번호 입력"
                  maxLength={6}
                  className="elevated-surface w-full rounded-lg px-4 py-3 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none"
                />
              </div>
            )}
          </div>
        </div>

        <div className="border-t border-border p-4">
          <button onClick={handleVerify} disabled={!codeSent || !verificationCode} className="gradient-border-button w-full rounded-xl py-3.5 text-sm font-semibold text-[#111111] disabled:opacity-40 disabled:cursor-not-allowed">
            인증 완료
          </button>
        </div>
      </div>
    </AppShell>
  )
}
