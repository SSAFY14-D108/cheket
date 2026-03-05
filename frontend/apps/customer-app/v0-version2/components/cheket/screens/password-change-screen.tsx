'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { useState } from 'react'

export function PasswordChangeScreen() {
  const { goBack } = useApp()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  const handleSubmit = () => {
    // TODO: API call to change password
    console.log('Password change submitted')
  }

  return (
    <AppShell>
      <div className="flex flex-col h-full">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-border sticky top-0 bg-background/95 backdrop-blur-sm">
          <button onClick={goBack} className="text-foreground">←</button>
          <h1 className="font-bold text-lg text-foreground">비밀번호 변경</h1>
          <div className="w-6" />
        </div>

        {/* Form */}
        <div className="flex-1 overflow-y-auto px-4 py-6">
          <div className="space-y-4">
            {/* Current Password */}
            <div>
              <label className="text-xs font-semibold text-muted-foreground mb-2 block">현재 비밀번호</label>
              <input
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                placeholder="현재 비밀번호 입력"
                className="w-full px-4 py-3 border border-border rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            {/* New Password */}
            <div>
              <label className="text-xs font-semibold text-muted-foreground mb-2 block">새 비밀번호</label>
              <input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="새 비밀번호 입력"
                className="w-full px-4 py-3 border border-border rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            {/* Confirm Password */}
            <div>
              <label className="text-xs font-semibold text-muted-foreground mb-2 block">새 비밀번호 확인</label>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="새 비밀번호 확인"
                className="w-full px-4 py-3 border border-border rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>
          </div>
        </div>

        {/* Submit Button */}
        <div className="p-4 border-t border-border">
          <button
            onClick={handleSubmit}
            disabled={!currentPassword || !newPassword || !confirmPassword}
            className="w-full bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          >
            변경하기
          </button>
        </div>
      </div>
    </AppShell>
  )
}
