'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { useState } from 'react'

export function PasswordResetScreen() {
  const { goBack } = useApp()
  const [email, setEmail] = useState('')
  const [verificationCode, setVerificationCode] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [step, setStep] = useState<'email' | 'verify' | 'reset'>('email')
  const [errors, setErrors] = useState<Record<string, string>>({})

  const handleSendEmail = () => {
    if (!email || !email.includes('@')) {
      setErrors({ email: '올바른 이메일을 입력해주세요.' })
      return
    }
    // TODO: API call to send reset email
    setStep('verify')
    setErrors({})
  }

  const handleVerify = () => {
    if (verificationCode.length !== 6) {
      setErrors({ code: '6자리 인증번호를 입력해주세요.' })
      return
    }
    // TODO: API call to verify code
    setStep('reset')
    setErrors({})
  }

  const handleReset = () => {
    const newErrors: Record<string, string> = {}
    if (newPassword.length < 6) newErrors.password = '비밀번호는 6자 이상이어야 합니다.'
    if (newPassword !== confirmPassword) newErrors.confirm = '비밀번호가 일치하지 않습니다.'
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }
    // TODO: API call to reset password
    goBack()
  }

  return (
    <AppShell>
      <div className="flex flex-col h-full">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-border sticky top-0 bg-background/95 backdrop-blur-sm">
          <button onClick={goBack} className="text-foreground">←</button>
          <h1 className="font-bold text-lg text-foreground">비밀번호 재설정</h1>
          <div className="w-6" />
        </div>

        {/* Form */}
        <div className="flex-1 overflow-y-auto px-4 py-6">
          <div className="space-y-4">
            {/* Step 1: Email */}
            {step === 'email' && (
              <div>
                <label className="text-xs font-semibold text-muted-foreground mb-2 block">이메일</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="example@email.com"
                  className="w-full px-4 py-3 border border-border rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                />
                {errors.email && <p className="text-red-400 text-xs mt-2">{errors.email}</p>}
              </div>
            )}

            {/* Step 2: Verification */}
            {step === 'verify' && (
              <div>
                <p className="text-xs text-muted-foreground mb-4">{email}로 발송된 인증 코드를 입력해주세요.</p>
                <label className="text-xs font-semibold text-muted-foreground mb-2 block">인증 코드</label>
                <input
                  type="text"
                  value={verificationCode}
                  onChange={(e) => setVerificationCode(e.target.value)}
                  placeholder="6자리 코드"
                  maxLength={6}
                  className="w-full px-4 py-3 border border-border rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                />
                {errors.code && <p className="text-red-400 text-xs mt-2">{errors.code}</p>}
              </div>
            )}

            {/* Step 3: New Password */}
            {step === 'reset' && (
              <>
                <div>
                  <label className="text-xs font-semibold text-muted-foreground mb-2 block">새 비밀번호</label>
                  <input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="새 비밀번호 (6자 이상)"
                    className="w-full px-4 py-3 border border-border rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  {errors.password && <p className="text-red-400 text-xs mt-2">{errors.password}</p>}
                </div>
                <div>
                  <label className="text-xs font-semibold text-muted-foreground mb-2 block">비밀번호 확인</label>
                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="비밀번호 재입력"
                    className="w-full px-4 py-3 border border-border rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  {errors.confirm && <p className="text-red-400 text-xs mt-2">{errors.confirm}</p>}
                </div>
              </>
            )}
          </div>
        </div>

        {/* Submit Button */}
        <div className="p-4 border-t border-border">
          <button
            onClick={step === 'email' ? handleSendEmail : step === 'verify' ? handleVerify : handleReset}
            disabled={
              (step === 'email' && !email) ||
              (step === 'verify' && !verificationCode) ||
              (step === 'reset' && (!newPassword || !confirmPassword))
            }
            className="w-full bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          >
            {step === 'email' ? '코드 전송' : step === 'verify' ? '확인' : '재설정'}
          </button>
        </div>
      </div>
    </AppShell>
  )
}
