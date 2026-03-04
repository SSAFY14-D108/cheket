'use client'

import { useState } from 'react'
import { useApp } from '@/lib/app-context'
import { ChevronLeft, Check } from 'lucide-react'

export function SignupScreen() {
  const { navigate, login } = useApp()
  const [step, setStep] = useState<1 | 2>(1)
  const [name, setName] = useState('')
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
      setErrors((p) => ({ ...p, phone: '올바른 전화번호를 입력해주세요.' }))
      return
    }
    setCodeSent(true)
    setErrors({})
  }

  const verifyCode = () => {
    // Mock: any 6-digit code works
    if (code.length === 6) {
      setCodeVerified(true)
      setErrors({})
    } else {
      setErrors((p) => ({ ...p, code: '인증번호 6자리를 입력해주세요.' }))
    }
  }

  const handleNext = () => {
    const newErrors: Record<string, string> = {}
    if (!name) newErrors.name = '이름을 입력해주세요.'
    if (!codeVerified) newErrors.code = '전화번호 인증이 필요합니다.'
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }
    setStep(2)
  }

  const handleSignup = () => {
    const newErrors: Record<string, string> = {}
    if (password.length < 6) newErrors.password = '비밀번호는 6자 이상이어야 합니다.'
    if (password !== passwordConfirm) newErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.'
    if (!agreed) newErrors.agreed = '약관에 동의해주세요.'
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }
    // Mock signup → auto login
    login(phone, password)
  }

  return (
    <div className="min-h-full flex flex-col bg-background">
      {/* Header */}
      <header className="flex items-center px-4 py-3 border-b border-border">
        <button onClick={() => navigate('login')} className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-secondary transition-colors -ml-1">
          <ChevronLeft className="w-5 h-5 text-foreground" />
        </button>
        <h1 className="font-semibold text-base text-foreground ml-2">회원가입</h1>
      </header>

      {/* Step indicator */}
      <div className="flex px-6 py-4 gap-2">
        {[1, 2].map((s) => (
          <div
            key={s}
            className={`flex-1 h-1 rounded-full transition-colors ${s <= step ? 'bg-primary' : 'bg-secondary'}`}
          />
        ))}
      </div>

      <div className="flex-1 px-6 flex flex-col gap-4 pb-8">
        {step === 1 ? (
          <>
            <div>
              <label className="text-xs font-medium text-muted-foreground mb-1.5 block">이름</label>
              <input
                className="w-full bg-secondary border border-border rounded-xl py-3 px-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
                placeholder="실명을 입력해주세요"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
              {errors.name && <p className="text-red-400 text-xs mt-1">{errors.name}</p>}
            </div>

            <div>
              <label className="text-xs font-medium text-muted-foreground mb-1.5 block">전화번호</label>
              <div className="flex gap-2">
                <input
                  className="flex-1 bg-secondary border border-border rounded-xl py-3 px-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
                  placeholder="010-0000-0000"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  type="tel"
                />
                <button
                  onClick={sendSms}
                  className="bg-primary text-primary-foreground text-xs font-semibold px-4 rounded-xl hover:opacity-90 whitespace-nowrap"
                >
                  {codeSent ? '재발송' : 'SMS 인증'}
                </button>
              </div>
              {errors.phone && <p className="text-red-400 text-xs mt-1">{errors.phone}</p>}
            </div>

            {codeSent && (
              <div>
                <label className="text-xs font-medium text-muted-foreground mb-1.5 block">인증번호</label>
                <div className="flex gap-2">
                  <input
                    className="flex-1 bg-secondary border border-border rounded-xl py-3 px-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
                    placeholder="6자리 인증번호"
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    maxLength={6}
                    type="number"
                  />
                  <button
                    onClick={verifyCode}
                    disabled={codeVerified}
                    className="bg-secondary border border-border text-foreground text-xs font-semibold px-4 rounded-xl hover:border-primary/50 disabled:opacity-50 whitespace-nowrap"
                  >
                    {codeVerified ? (
                      <span className="flex items-center gap-1 text-primary">
                        <Check className="w-3.5 h-3.5" /> 완료
                      </span>
                    ) : '확인'}
                  </button>
                </div>
                {errors.code && <p className="text-red-400 text-xs mt-1">{errors.code}</p>}
                {codeVerified && <p className="text-primary text-xs mt-1">인증이 완료되었습니다.</p>}
              </div>
            )}

            <button
              onClick={handleNext}
              className="w-full bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all mt-auto"
            >
              다음
            </button>
          </>
        ) : (
          <>
            <div>
              <label className="text-xs font-medium text-muted-foreground mb-1.5 block">비밀번호 설정</label>
              <input
                className="w-full bg-secondary border border-border rounded-xl py-3 px-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
                placeholder="비밀번호 (6자 이상)"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              {errors.password && <p className="text-red-400 text-xs mt-1">{errors.password}</p>}
            </div>

            <div>
              <label className="text-xs font-medium text-muted-foreground mb-1.5 block">비밀번호 확인</label>
              <input
                className="w-full bg-secondary border border-border rounded-xl py-3 px-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
                placeholder="비밀번호 재입력"
                type="password"
                value={passwordConfirm}
                onChange={(e) => setPasswordConfirm(e.target.value)}
              />
              {errors.passwordConfirm && <p className="text-red-400 text-xs mt-1">{errors.passwordConfirm}</p>}
            </div>

            <div className="bg-secondary rounded-xl p-4 text-xs text-muted-foreground space-y-2">
              <p className="font-semibold text-foreground text-sm mb-2">약관 동의</p>
              <p>[필수] 서비스 이용약관 동의</p>
              <p>[필수] 개인정보 수집 및 이용 동의</p>
              <p>[선택] 마케팅 정보 수신 동의</p>
              <button
                onClick={() => setAgreed(!agreed)}
                className={`mt-2 flex items-center gap-2 font-semibold ${agreed ? 'text-primary' : 'text-foreground'}`}
              >
                <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center transition-colors ${agreed ? 'border-primary bg-primary' : 'border-border'}`}>
                  {agreed && <Check className="w-3 h-3 text-primary-foreground" />}
                </div>
                전체 동의
              </button>
              {errors.agreed && <p className="text-red-400">{errors.agreed}</p>}
            </div>

            <button
              onClick={handleSignup}
              className="w-full bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all mt-auto"
            >
              가입 완료
            </button>
          </>
        )}
      </div>
    </div>
  )
}
