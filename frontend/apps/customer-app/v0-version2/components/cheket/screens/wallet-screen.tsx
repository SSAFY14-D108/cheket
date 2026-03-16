'use client'

import { useState } from 'react'
import { CheckCircle2, ChevronRight, Copy, Plus, Wallet } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { cn } from '@/lib/utils'
import { AppShell } from '../app-shell'

const TOP_UP_AMOUNTS = [5000, 10000, 30000, 50000]

export function WalletScreen() {
  const { user, goBack, topUpCTK, navigate } = useApp()
  const [copied, setCopied] = useState(false)
  const [topping, setTopping] = useState(false)
  const [topUpSuccess, setTopUpSuccess] = useState<number | null>(null)

  if (!user) return null

  const copyAddress = () => {
    navigator.clipboard.writeText(user.walletAddress).catch(() => {})
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const handleTopUp = (amount: number) => {
    setTopping(true)
    setTimeout(() => {
      topUpCTK(amount)
      setTopUpSuccess(amount)
      setTopping(false)
      setTimeout(() => setTopUpSuccess(null), 2500)
    }, 800)
  }

  return (
    <AppShell showBack onBack={goBack} title="지갑" showBottomNav={false} hideProfileIcon>
      <div className="flex flex-col gap-4 bg-gray-50 p-4 pb-8">
        <div
          className="rounded-2xl p-5"
          style={{
            border: '2px solid transparent',
            backgroundImage:
              'linear-gradient(#FFFFFF, #FFFFFF), linear-gradient(135deg, rgba(226, 218, 255, 0.76), rgba(196, 247, 224, 0.74), rgba(202, 230, 255, 0.76))',
            backgroundOrigin: 'border-box',
            backgroundClip: 'padding-box, border-box',
          }}
        >
          <div className="mb-4 flex items-center gap-2">
            <Wallet className="h-5 w-5 text-[#333333]" />
            <span className="text-sm font-semibold text-foreground">CTK 잔액</span>
          </div>
          <div className="mb-1 flex items-end gap-2">
            <span className="text-4xl font-bold text-foreground">{user.ctkBalance.toLocaleString()}</span>
            <span className="mb-1 text-base text-muted-foreground">CTK</span>
          </div>
          <p className="text-sm text-muted-foreground">약 {(user.ctkBalance * 1.2).toLocaleString()}원</p>

          <div className="mt-4 border-t border-[#e7ebf0] pt-4">
            <p className="mb-1.5 text-xs text-muted-foreground">지갑 주소</p>
            <div className="rounded-lg bg-gray-100 px-3 py-2">
              <div className="flex items-center justify-between gap-2">
                <span className="truncate font-mono text-xs text-foreground">{user.walletAddress}</span>
                <button onClick={copyAddress} className="flex-shrink-0 text-muted-foreground hover:text-foreground transition-colors" aria-label="주소 복사">
                  {copied ? <CheckCircle2 className="h-4 w-4 text-[#333333]" /> : <Copy className="h-4 w-4" />}
                </button>
              </div>
            </div>
            {copied && <p className="mt-1 text-xs text-[#333333]">복사했어요.</p>}
          </div>
        </div>

        <div className="rounded-2xl bg-white p-4 shadow-[0_10px_26px_rgba(15,23,42,0.04)]">
          <h3 className="mb-3 text-sm font-semibold text-foreground">테스트용 CTK 충전</h3>
          <div className="grid grid-cols-2 gap-2">
            {TOP_UP_AMOUNTS.map((amount) => (
              <button
                key={amount}
                onClick={() => handleTopUp(amount)}
                disabled={topping}
                className={cn('rounded-xl bg-gray-100 py-3 text-sm font-semibold text-foreground transition-all active:scale-[0.97]', 'disabled:cursor-not-allowed disabled:opacity-50')}
              >
                <span className="inline-flex items-center gap-1.5">
                  <Plus className="h-3.5 w-3.5" />
                  {amount.toLocaleString()} CTK
                </span>
              </button>
            ))}
          </div>

          {topUpSuccess !== null && (
            <div className="mt-3 flex items-center justify-center gap-2 rounded-xl bg-[#eef2f1] py-2.5">
              <CheckCircle2 className="h-4 w-4 text-[#333333]" />
              <p className="text-sm font-medium text-[#111111]">+{topUpSuccess.toLocaleString()} CTK 충전 완료</p>
            </div>
          )}

          <p className="mt-3 text-center text-xs text-muted-foreground">실제 충전 기능은 추후 연결 예정입니다.</p>
        </div>

        <button onClick={() => navigate('wallet-history')} className="gradient-outline-button flex w-full items-center justify-between rounded-xl px-4 py-3.5 text-sm">
          <span>지갑 거래 내역</span>
          <ChevronRight className="h-4 w-4 text-muted-foreground" />
        </button>
      </div>
    </AppShell>
  )
}
