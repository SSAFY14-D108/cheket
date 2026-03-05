'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { useState } from 'react'

export function SmsVerificationScreen() {
  const { goBack } = useApp()
  const [phone, setPhone] = useState('')
  const [verificationCode, setVerificationCode] = useState('')
  const [codeSent, setCodeSent] = useState(false)

  const handleSendCode = () => {
    // TODO: API call to send SMS
    setCodeSent(true)
  }

  const handleVerify = () => {
    // TODO: API call to verify code
    console.log('SMS verification submitted')
  }

  return (
    <AppShell>
      <div className="flex flex-col h-full">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-border sticky top-0 bg-background/95 backdrop-blur-sm">
          <button onClick={goBack} className="text-foreground">←</button>
          <h1 className="font-bold text-lg text-foreground">SMS 인증</h1>
          <div className="w-6" />
        </div>

        {/* Form */}
        <div className="flex-1 overflow-y-auto px-4 py-6">
          <div className="space-y-4">
            {/* Phone Number */}
            <div>
              <label className="text-xs font-semibold text-muted-foreground mb-2 block">전화번호</label>
              <div className="flex gap-2">
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="010-1234-5678"
                  className="flex-1 px-4 py-3 border border-border rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  disabled={codeSent}
                />
                <button
                  onClick={handleSendCode}
                  disabled={!phone || codeSent}
                  className="px-4 py-3 bg-secondary border border-border rounded-lg text-sm font-medium text-foreground hover:border-primary/40 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  {codeSent ? '재전송' : '인증'}
                </button>
              </div>
            </div>

            {/* Verification Code */}
            {codeSent && (
              <div>
                <label className="text-xs font-semibold text-muted-foreground mb-2 block">인증 코드</label>
                <input
                  type="text"
                  value={verificationCode}
                  onChange={(e) => setVerificationCode(e.target.value)}
                  placeholder="6자리 코드 입력"
                  maxLength={6}
                  className="w-full px-4 py-3 border border-border rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
            )}
          </div>
        </div>

        {/* Submit Button */}
        <div className="p-4 border-t border-border">
          <button
            onClick={handleVerify}
            disabled={!codeSent || !verificationCode}
            className="w-full bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          >
            인증 완료
          </button>
        </div>
      </div>
    </AppShell>
  )
}
