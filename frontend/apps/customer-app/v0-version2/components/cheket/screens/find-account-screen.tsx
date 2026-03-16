'use client'

import { useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'

export function FindAccountScreen() {
  const { goBack } = useApp()
  const [phone, setPhone] = useState('')
  const [result, setResult] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const handleSearch = () => {
    if (!phone || phone.length < 10) {
      setError('전화번호를 다시 입력해 주세요.')
      return
    }
    setResult('kim***@example.com')
    setError(null)
  }

  return (
    <AppShell title="아이디 찾기" showBack onBack={goBack} showBottomNav={false}>
      <div className="flex h-full flex-col">
        <div className="flex-1 overflow-y-auto px-4 py-6">
          <p className="mb-4 text-sm font-semibold text-[#111111]">휴대폰 번호로 가입한 이메일을 찾을 수 있어요.</p>

          <div className="space-y-4">
            <div>
              <label className="mb-2 block text-xs font-semibold text-muted-foreground">휴대폰 번호</label>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="010-1234-5678"
                className="gradient-outline-surface w-full rounded-lg px-4 py-3 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none"
              />
              {error && <p className="mt-2 text-xs text-red-400">{error}</p>}
            </div>

            {result && (
              <div className="gradient-outline-surface rounded-lg p-4">
                <p className="mb-1 text-xs text-muted-foreground">찾은 이메일</p>
                <p className="text-sm font-semibold text-[#111111]">{result}</p>
              </div>
            )}
          </div>
        </div>

        <div className="border-t border-border p-4">
          <button onClick={handleSearch} disabled={!phone} className="gradient-outline-button w-full rounded-xl py-3.5 text-sm font-semibold text-[#111111] disabled:opacity-40 disabled:cursor-not-allowed">
            아이디 찾기
          </button>
        </div>
      </div>
    </AppShell>
  )
}
