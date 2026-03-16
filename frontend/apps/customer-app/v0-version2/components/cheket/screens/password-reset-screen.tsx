'use client'

import { useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'

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
      setErrors({ email: '이메일 형식을 확인해 주세요.' })
      return
    }
    setStep('verify')
    setErrors({})
  }

  const handleVerify = () => {
    if (verificationCode.length !== 6) {
      setErrors({ code: '6자리 인증번호를 입력해 주세요.' })
      return
    }
    setStep('reset')
    setErrors({})
  }

  const handleReset = () => {
    const newErrors: Record<string, string> = {}
    if (newPassword.length < 6) newErrors.password = '비밀번호는 6자 이상이어야 해요.'
    if (newPassword !== confirmPassword) newErrors.confirm = '비밀번호가 서로 다릅니다.'
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }
    goBack()
  }

  return (
    <AppShell title="비밀번호 찾기" showBack onBack={goBack} showBottomNav={false}>
      <div className="flex h-full flex-col">
        <div className="flex-1 overflow-y-auto px-4 py-6">
          <div className="space-y-4">
            {step === 'email' && (
              <div>
                <label className="mb-2 block text-xs font-semibold text-muted-foreground">이메일</label>
                <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="example@email.com" className="gradient-outline-surface w-full rounded-lg px-4 py-3 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none" />
                {errors.email && <p className="mt-2 text-xs text-red-400">{errors.email}</p>}
              </div>
            )}

            {step === 'verify' && (
              <div>
                <p className="mb-4 text-xs text-muted-foreground">{email} 주소로 발송된 인증번호를 입력해 주세요.</p>
                <label className="mb-2 block text-xs font-semibold text-muted-foreground">인증번호</label>
                <input type="text" value={verificationCode} onChange={(e) => setVerificationCode(e.target.value)} placeholder="6자리 인증번호" maxLength={6} className="gradient-outline-surface w-full rounded-lg px-4 py-3 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none" />
                {errors.code && <p className="mt-2 text-xs text-red-400">{errors.code}</p>}
              </div>
            )}

            {step === 'reset' && (
              <>
                <div>
                  <label className="mb-2 block text-xs font-semibold text-muted-foreground">새 비밀번호</label>
                  <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="새 비밀번호 (6자 이상)" className="gradient-outline-surface w-full rounded-lg px-4 py-3 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none" />
                  {errors.password && <p className="mt-2 text-xs text-red-400">{errors.password}</p>}
                </div>
                <div>
                  <label className="mb-2 block text-xs font-semibold text-muted-foreground">비밀번호 확인</label>
                  <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="비밀번호를 다시 입력해 주세요" className="gradient-outline-surface w-full rounded-lg px-4 py-3 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none" />
                  {errors.confirm && <p className="mt-2 text-xs text-red-400">{errors.confirm}</p>}
                </div>
              </>
            )}
          </div>
        </div>

        <div className="border-t border-border p-4">
          <button
            onClick={step === 'email' ? handleSendEmail : step === 'verify' ? handleVerify : handleReset}
            disabled={(step === 'email' && !email) || (step === 'verify' && !verificationCode) || (step === 'reset' && (!newPassword || !confirmPassword))}
            className="gradient-outline-button w-full rounded-xl py-3.5 text-sm font-semibold text-[#111111] disabled:opacity-40 disabled:cursor-not-allowed"
          >
            {step === 'email' ? '인증번호 받기' : step === 'verify' ? '확인' : '비밀번호 재설정'}
          </button>
        </div>
      </div>
    </AppShell>
  )
}
