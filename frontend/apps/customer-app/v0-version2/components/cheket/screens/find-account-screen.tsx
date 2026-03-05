'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { useState } from 'react'

export function FindAccountScreen() {
  const { goBack } = useApp()
  const [phone, setPhone] = useState('')
  const [result, setResult] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const handleSearch = () => {
    if (!phone || phone.length < 10) {
      setError('올바른 전화번호를 입력해주세요.')
      return
    }
    // TODO: API call to find email by phone
    setResult('kim***@example.com')
    setError(null)
  }

  return (
    <AppShell>
      <div className="flex flex-col h-full">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-border sticky top-0 bg-background/95 backdrop-blur-sm">
          <button onClick={goBack} className="text-foreground">←</button>
          <h1 className="font-bold text-lg text-foreground">계정 찾기</h1>
          <div className="w-6" />
        </div>

        {/* Form */}
        <div className="flex-1 overflow-y-auto px-4 py-6">
          {/* Search Type Tabs */}
          <p className="text-sm font-semibold text-foreground mb-4">전화번호로 가입 이메일 찾기</p>

          {/* Input */}
          <div className="space-y-4">
            <div>
              <label className="text-xs font-semibold text-muted-foreground mb-2 block">전화번호</label>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="010-1234-5678"
                className="w-full px-4 py-3 border border-border rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
              />
              {error && <p className="text-red-400 text-xs mt-2">{error}</p>}
            </div>

            {/* Result */}
            {result && (
              <div className="p-4 bg-accent/20 border border-accent rounded-lg">
                <p className="text-xs text-muted-foreground mb-1">가입된 이메일</p>
                <p className="text-sm font-semibold text-foreground">{result}</p>
              </div>
            )}
          </div>
        </div>

        {/* Submit Button */}
        <div className="p-4 border-t border-border">
          <button
            onClick={handleSearch}
            disabled={!phone}
            className="w-full bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          >
            이메일 찾기
          </button>
        </div>
      </div>
    </AppShell>
  )
}
