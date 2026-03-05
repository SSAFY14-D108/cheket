'use client'

import { useState } from 'react'
import { useApp } from '@/lib/app-context'
import { Eye, EyeOff, Lock, User } from 'lucide-react'

export function LoginScreen() {
  const { login, navigate } = useApp()
  const [id, setId] = useState('')
  const [password, setPassword] = useState('')
  const [showPw, setShowPw] = useState(false)
  const [error, setError] = useState('')

  const handleLogin = () => {
    if (!id || !password) {
      setError('아이디와 비밀번호를 입력해주세요.')
      return
    }
    const ok = login(id, password)
    if (!ok) setError('아이디 또는 비밀번호가 잘못되었습니다.')
  }

  return (
    <div className="min-h-full flex flex-col bg-background">
      {/* Logo area */}
      <div className="flex flex-col items-center justify-center pt-20 pb-10">
        <div className="w-16 h-16 rounded-2xl bg-primary/10 border border-primary/30 flex items-center justify-center mb-4">
          <span className="text-primary font-bold text-2xl">C</span>
        </div>
        <h1 className="text-3xl font-bold text-primary tracking-tight">cheket</h1>
        <p className="text-muted-foreground text-sm mt-2">NFT 티켓팅 플랫폼</p>
      </div>

      {/* Form */}
      <div className="flex-1 px-6 flex flex-col gap-4">
        <div className="relative">
          <User className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            className="w-full bg-secondary border border-border rounded-xl py-3.5 pl-10 pr-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
            placeholder="아이디"
            value={id}
            onChange={(e) => { setId(e.target.value); setError('') }}
            autoComplete="username"
          />
        </div>

        <div className="relative">
          <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            className="w-full bg-secondary border border-border rounded-xl py-3.5 pl-10 pr-10 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
            placeholder="비밀번호"
            type={showPw ? 'text' : 'password'}
            value={password}
            onChange={(e) => { setPassword(e.target.value); setError('') }}
            autoComplete="current-password"
          />
          <button
            onClick={() => setShowPw(!showPw)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground"
            aria-label={showPw ? '비밀번호 숨기기' : '비밀번호 보기'}
          >
            {showPw ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
          </button>
        </div>

        {error && (
          <p className="text-red-400 text-xs text-center">{error}</p>
        )}

        <button
          onClick={handleLogin}
          className="w-full bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all mt-2"
        >
          로그인
        </button>

        <div className="flex items-center gap-4 my-2">
          <div className="flex-1 h-px bg-border" />
          <span className="text-muted-foreground text-xs">또는</span>
          <div className="flex-1 h-px bg-border" />
        </div>

        <SignUpButton />

        {/* Help links */}
        <div className="flex items-center justify-center gap-2 text-xs mt-3">
          <button
            onClick={() => navigate('find-account')}
            className="text-primary underline hover:opacity-75 transition-opacity"
          >
            이메일 찾기
          </button>
          <span className="text-border">·</span>
          <button
            onClick={() => navigate('password-reset')}
            className="text-primary underline hover:opacity-75 transition-opacity"
          >
            비밀번호 찾기
          </button>
        </div>

        <p className="text-muted-foreground text-xs text-center mt-4">
          로그인 시 <span className="text-primary underline cursor-pointer">이용약관</span> 및{' '}
          <span className="text-primary underline cursor-pointer">개인정보처리방침</span>에 동의합니다
        </p>
      </div>
    </div>
  )
}

function SignUpButton() {
  const { navigate } = useApp()
  return (
    <button
      onClick={() => navigate('signup')}
      className="w-full bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm hover:border-primary/50 active:scale-[0.98] transition-all"
    >
      회원가입
    </button>
  )
}
