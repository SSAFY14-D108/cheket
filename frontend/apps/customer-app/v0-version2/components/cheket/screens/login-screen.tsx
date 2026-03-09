'use client'

import Image from 'next/image'
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
      setError('아이디와 비밀번호를 입력해 주세요.')
      return
    }

    const ok = login(id, password)
    if (!ok) setError('로그인에 실패했습니다. 입력 정보를 다시 확인해 주세요.')
  }

  return (
    <div className="min-h-full bg-background">
      <div className="mx-auto flex min-h-full w-full max-w-[390px] flex-col px-6 pb-8 pt-10">
        <div className="mb-8 flex flex-col items-center">
          <Image
            src="/cheket-ticket.webp"
            alt="cheket ticket"
            width={640}
            height={320}
            className="h-auto w-[200px] object-contain"
            priority
          />
          <h1 className="mt-5 text-center text-[24px] font-bold tracking-[-0.04em] text-foreground">
            로그인
          </h1>
          <p className="mt-2 text-center text-sm text-muted-foreground">
            cheket 계정으로 티켓 서비스를 이용하세요
          </p>
        </div>

        <div className="flex flex-1 flex-col gap-4">
          <label className="block">
            <span className="mb-1.5 block text-xs font-medium text-muted-foreground">아이디</span>
            <div className="relative">
              <User className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <input
                className="w-full rounded-xl border border-border bg-secondary py-3.5 pl-11 pr-4 text-sm text-foreground placeholder:text-muted-foreground focus:border-primary focus:outline-none transition-colors"
                placeholder="아이디 입력"
                value={id}
                onChange={(e) => {
                  setId(e.target.value)
                  setError('')
                }}
                autoComplete="username"
              />
            </div>
          </label>

          <label className="block">
            <span className="mb-1.5 block text-xs font-medium text-muted-foreground">비밀번호</span>
            <div className="relative">
              <Lock className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <input
                className="w-full rounded-xl border border-border bg-secondary py-3.5 pl-11 pr-12 text-sm text-foreground placeholder:text-muted-foreground focus:border-primary focus:outline-none transition-colors"
                placeholder="비밀번호 입력"
                type={showPw ? 'text' : 'password'}
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value)
                  setError('')
                }}
                autoComplete="current-password"
              />
              <button
                type="button"
                onClick={() => setShowPw(!showPw)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground"
                aria-label={showPw ? '비밀번호 숨기기' : '비밀번호 보기'}
              >
                {showPw ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
          </label>

          {error ? <p className="text-xs text-red-500">{error}</p> : null}

          <button
            onClick={handleLogin}
            className="mt-2 w-full rounded-xl bg-primary py-3.5 text-sm font-semibold text-primary-foreground transition-all hover:opacity-95 active:scale-[0.98]"
          >
            로그인
          </button>

          <button
            onClick={() => navigate('signup')}
            className="w-full rounded-xl border border-border bg-secondary py-3.5 text-sm font-semibold text-foreground transition-all hover:border-primary/50 active:scale-[0.98]"
          >
            회원가입
          </button>

          <div className="mt-2 flex items-center justify-center gap-2 text-xs">
            <button
              onClick={() => navigate('find-account')}
              className="text-primary underline-offset-4 hover:underline"
            >
              이메일 찾기
            </button>
            <span className="text-border">·</span>
            <button
              onClick={() => navigate('password-reset')}
              className="text-primary underline-offset-4 hover:underline"
            >
              비밀번호 찾기
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
