'use client'

import { useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'

export function PasswordChangeScreen() {
  const { goBack } = useApp()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  const handleSubmit = () => {
    console.log('Password change submitted')
  }

  return (
    <AppShell title="비밀번호 변경" showBack onBack={goBack} showBottomNav={false}>
      <div className="flex h-full flex-col">
        <div className="flex-1 overflow-y-auto px-4 py-6">
          <div className="space-y-4">
            <div>
              <label className="mb-2 block text-xs font-semibold text-muted-foreground">현재 비밀번호</label>
              <input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} placeholder="현재 비밀번호 입력" className="gradient-outline-surface w-full rounded-lg px-4 py-3 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none" />
            </div>

            <div>
              <label className="mb-2 block text-xs font-semibold text-muted-foreground">새 비밀번호</label>
              <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="새 비밀번호 입력" className="gradient-outline-surface w-full rounded-lg px-4 py-3 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none" />
            </div>

            <div>
              <label className="mb-2 block text-xs font-semibold text-muted-foreground">새 비밀번호 확인</label>
              <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="새 비밀번호를 다시 입력해 주세요" className="gradient-outline-surface w-full rounded-lg px-4 py-3 text-sm text-[#111111] placeholder:text-muted-foreground focus:outline-none" />
            </div>
          </div>
        </div>

        <div className="border-t border-border p-4">
          <button onClick={handleSubmit} disabled={!currentPassword || !newPassword || !confirmPassword} className="gradient-outline-button w-full rounded-xl py-3.5 text-sm font-semibold text-[#111111] disabled:opacity-40 disabled:cursor-not-allowed">
            변경하기
          </button>
        </div>
      </div>
    </AppShell>
  )
}
