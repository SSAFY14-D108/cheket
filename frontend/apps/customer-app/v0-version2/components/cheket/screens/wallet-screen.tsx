'use client'

import { useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { Wallet, Copy, CheckCircle2, Plus, ChevronRight } from 'lucide-react'
import { cn } from '@/lib/utils'

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
      <div className="flex flex-col gap-4 p-4 pb-8">
        {/* Balance card */}
        <div className="bg-gradient-to-br from-primary/20 to-primary/5 border border-primary/20 rounded-2xl p-5">
          <div className="flex items-center gap-2 mb-4">
            <Wallet className="w-5 h-5 text-primary" />
            <span className="font-semibold text-sm text-foreground">CTK 잔액</span>
          </div>
          <div className="flex items-end gap-2 mb-1">
            <span className="font-bold text-4xl text-foreground">{user.ctkBalance.toLocaleString()}</span>
            <span className="text-base text-muted-foreground mb-1">CTK</span>
          </div>
          <p className="text-sm text-muted-foreground">
            ≈ {(user.ctkBalance * 1.2).toLocaleString()}원
          </p>

          <div className="mt-4 pt-4 border-t border-primary/10">
            <p className="text-xs text-muted-foreground mb-1.5">지갑 주소</p>
            <div className="flex items-center justify-between gap-2 bg-background/40 rounded-lg px-3 py-2">
              <span className="font-mono text-xs text-foreground truncate">{user.walletAddress}</span>
              <button
                onClick={copyAddress}
                className="flex-shrink-0 text-muted-foreground hover:text-foreground transition-colors"
                aria-label="주소 복사"
              >
                {copied
                  ? <CheckCircle2 className="w-4 h-4 text-primary" />
                  : <Copy className="w-4 h-4" />
                }
              </button>
            </div>
            {copied && <p className="text-xs text-primary mt-1">복사되었습니다!</p>}
          </div>
        </div>

        {/* Top up section */}
        <div className="bg-card border border-border rounded-xl p-4">
          <h3 className="font-semibold text-sm text-foreground mb-3">테스트 CTK 충전</h3>
          <div className="grid grid-cols-2 gap-2">
            {TOP_UP_AMOUNTS.map((amount) => (
              <button
                key={amount}
                onClick={() => handleTopUp(amount)}
                disabled={topping}
                className={cn(
                  'flex items-center justify-center gap-1.5 py-3 rounded-xl border text-sm font-semibold transition-all',
                  'bg-secondary border-border text-foreground hover:border-primary/50 active:scale-[0.97]',
                  'disabled:opacity-50 disabled:cursor-not-allowed'
                )}
              >
                <Plus className="w-3.5 h-3.5" />
                {amount.toLocaleString()} CTK
              </button>
            ))}
          </div>

          {topUpSuccess !== null && (
            <div className="mt-3 flex items-center justify-center gap-2 py-2.5 bg-primary/10 border border-primary/20 rounded-xl">
              <CheckCircle2 className="w-4 h-4 text-primary" />
              <p className="text-sm text-primary font-medium">
                +{topUpSuccess.toLocaleString()} CTK 충전 완료!
              </p>
            </div>
          )}

          <p className="text-xs text-muted-foreground text-center mt-3">
            실제 충전 기능은 앱 출시 후 지원됩니다.
          </p>
        </div>

        {/* Transaction history button */}
        <button
          onClick={() => navigate('wallet-history')}
          className="w-full bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm hover:bg-accent/30 active:scale-[0.98] transition-all flex items-center justify-between px-4"
        >
          <span>지갑 거래 내역</span>
          <ChevronRight className="w-4 h-4 text-muted-foreground" />
        </button>
      </div>
    </AppShell>
  )
}
