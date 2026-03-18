'use client'

import { useState } from 'react'
import { Check, ChevronLeft } from 'lucide-react'
import { useApp } from '@/lib/app-context'

export function SignupScreen() {
  const { navigate, login } = useApp()
  const [step, setStep] = useState<1 | 2>(1)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [emailVerified, setEmailVerified] = useState(false)
  const [phone, setPhone] = useState('')
  const [code, setCode] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [codeVerified, setCodeVerified] = useState(false)
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [agreed, setAgreed] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})

  const sendSms = () => {
    if (!phone || phone.length < 10) {
      setErrors((p) => ({ ...p, phone: '전화번호를 다시 입력해 주세요.' }))
      return
    }
    setCodeSent(true)
    setErrors({})
  }

  const verifyCode = () => {
    if (code.length === 6) {
      setCodeVerified(true)
      setErrors({})
    } else {
      setErrors((p) => ({ ...p, code: '인증번호 6자리를 입력해 주세요.' }))
    }
  }

  const checkEmailDuplicate = () => {
    if (!email || !email.includes('@')) {
      setErrors((p) => ({ ...p, email: '이메일 형식을 확인해 주세요.' }))
      return
    }
    setEmailVerified(true)
    setErrors({})
  }

  const handleNext = () => {
    const newErrors: Record<string, string> = {}
    if (!name) newErrors.name = '이름을 입력해 주세요.'
    if (!emailVerified) newErrors.email = '이메일 중복 확인이 필요해요.'
    if (!codeVerified) newErrors.code = '휴대폰 인증을 완료해 주세요.'
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }
    setStep(2)
  }

  const handleSignup = () => {
    const newErrors: Record<string, string> = {}
    if (password.length < 6) newErrors.password = '비밀번호는 6자 이상이어야 해요.'
    if (password !== passwordConfirm) newErrors.passwordConfirm = '비밀번호가 서로 다릅니다.'
    if (!agreed) newErrors.agreed = '약관 동의가 필요해요.'
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }
    login(phone, password)
  }

  return (
    <div className="min-h-full flex flex-col bg-background">
      <header className="flex items-center border-b border-border px-4 py-3">
        <button onClick={() => navigate('login')} className="neutral-icon-button -ml-1 flex h-8 w-8 items-center justify-center rounded-full">
          <ChevronLeft className="h-5 w-5 text-foreground" />
        </button>
        <h1 className="ml-2 text-base font-semibold text-[#111111]">회원가입</h1>
      </header>

      <div className="flex gap-2 px-6 py-4">
        {[1, 2].map((s) => (
          <div key={s} className={`flex-1 h-1 rounded-full transition-colors ${s <= step ? 'bg-[#cfd6df]' : 'bg-secondary'}`} />
        ))}
      </div>

      <div className="flex-1 px-6 flex flex-col gap-4 pb-8">
        {step === 1 ? (
          <>
            <div>
              <label className="mb-1.5 block text-xs font-medium text-muted-foreground">이름</label>
              <input className="elevated-surface w-full rounded-xl py-3 px-4 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none" placeholder="이름을 입력해 주세요" value={name} onChange={(e) => setName(e.target.value)} />
              {errors.name && <p className="mt-1 text-xs text-red-400">{errors.name}</p>}
            </div>

            <div>
              <label className="mb-1.5 block text-xs font-medium text-muted-foreground">이메일</label>
              <div className="flex gap-2">
                <input
                  className="elevated-surface flex-1 rounded-xl py-3 px-4 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none"
                  placeholder="example@email.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  type="email"
                  disabled={emailVerified}
                />
                <button onClick={checkEmailDuplicate} disabled={emailVerified} className="gradient-border-button rounded-xl px-4 text-xs font-semibold text-[#111111] disabled:opacity-50">
                  {emailVerified ? '완료' : '중복 확인'}
                </button>
              </div>
              {errors.email && <p className="mt-1 text-xs text-red-400">{errors.email}</p>}
              {emailVerified && <p className="mt-1 text-xs text-[#6b7280]">사용 가능한 이메일입니다.</p>}
            </div>

            <div>
              <label className="mb-1.5 block text-xs font-medium text-muted-foreground">휴대폰 번호</label>
              <div className="flex gap-2">
                <input
                  className="elevated-surface flex-1 rounded-xl py-3 px-4 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none"
                  placeholder="010-0000-0000"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  type="tel"
                />
                <button onClick={sendSms} className="gradient-border-button rounded-xl px-4 text-xs font-semibold text-[#111111] whitespace-nowrap">
                  {codeSent ? '재전송' : 'SMS 인증'}
                </button>
              </div>
              {errors.phone && <p className="mt-1 text-xs text-red-400">{errors.phone}</p>}
            </div>

            {codeSent && (
              <div>
                <label className="mb-1.5 block text-xs font-medium text-muted-foreground">인증번호</label>
                <div className="flex gap-2">
                  <input
                    className="elevated-surface flex-1 rounded-xl py-3 px-4 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none"
                    placeholder="6자리 인증번호"
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    maxLength={6}
                    type="number"
                  />
                  <button onClick={verifyCode} disabled={codeVerified} className="elevated-surface rounded-xl px-4 text-xs font-semibold text-[#111111] disabled:opacity-50 whitespace-nowrap">
                    {codeVerified ? (
                      <span className="flex items-center gap-1">
                        <Check className="h-3.5 w-3.5" />
                        완료
                      </span>
                    ) : (
                      '확인'
                    )}
                  </button>
                </div>
                {errors.code && <p className="mt-1 text-xs text-red-400">{errors.code}</p>}
                {codeVerified && <p className="mt-1 text-xs text-[#6b7280]">휴대폰 인증이 완료되었어요.</p>}
              </div>
            )}

            <button onClick={handleNext} className="gradient-border-button mt-auto w-full rounded-xl py-3.5 text-sm font-semibold text-[#111111]">
              다음
            </button>
          </>
        ) : (
          <>
            <div>
              <label className="mb-1.5 block text-xs font-medium text-muted-foreground">비밀번호 설정</label>
              <input
                className="elevated-surface w-full rounded-xl py-3 px-4 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none"
                placeholder="비밀번호 (6자 이상)"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              {errors.password && <p className="mt-1 text-xs text-red-400">{errors.password}</p>}
            </div>

            <div>
              <label className="mb-1.5 block text-xs font-medium text-muted-foreground">비밀번호 확인</label>
              <input
                className="elevated-surface w-full rounded-xl py-3 px-4 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none"
                placeholder="비밀번호를 다시 입력해 주세요"
                type="password"
                value={passwordConfirm}
                onChange={(e) => setPasswordConfirm(e.target.value)}
              />
              {errors.passwordConfirm && <p className="mt-1 text-xs text-red-400">{errors.passwordConfirm}</p>}
            </div>

            <div className="elevated-surface-soft rounded-xl p-4 text-xs text-muted-foreground space-y-2">
              <p className="font-semibold text-[#111111] text-sm mb-2">약관 동의</p>
              <p>[필수] 서비스 이용약관 동의</p>
              <p>[필수] 개인정보 수집 및 이용 동의</p>
              <p>[선택] 마케팅 정보 수신 동의</p>
              <button onClick={() => setAgreed(!agreed)} className={`mt-2 flex items-center gap-2 font-semibold ${agreed ? 'text-[#111111]' : 'text-foreground'}`}>
                <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center transition-colors ${agreed ? 'border-[#cfd6df] bg-white' : 'border-border'}`}>
                  {agreed && <Check className="w-3 h-3 text-[#111111]" />}
                </div>
                전체 약관에 동의합니다
              </button>
              {errors.agreed && <p className="text-red-400">{errors.agreed}</p>}
            </div>

            <button onClick={handleSignup} className="gradient-border-button w-full rounded-xl py-3.5 text-sm font-semibold text-[#111111] mt-auto">
              가입 완료
            </button>
          </>
        )}
      </div>
    </div>
  )
}
