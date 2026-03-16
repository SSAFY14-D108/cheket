'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { ArrowDownLeft, ArrowUpRight, LogIn } from 'lucide-react'
import { TutorialHelpButton } from '../tutorial-dialog'

const TX_TYPE_LABELS: Record<string, { label: string; icon: React.ReactNode; color: string }> = {
  CHARGE: { label: 'CTK 충전', icon: <ArrowDownLeft className="h-5 w-5" />, color: 'text-primary' },
  PURCHASE: { label: '티켓 구매', icon: <ArrowUpRight className="h-5 w-5" />, color: 'text-destructive' },
  RESALE_BUY: { label: '재판매 구매', icon: <ArrowUpRight className="h-5 w-5" />, color: 'text-destructive' },
  RESALE_SELL: { label: '재판매 판매', icon: <ArrowDownLeft className="h-5 w-5" />, color: 'text-primary' },
  TRANSFER_SEND: { label: '티켓 양도', icon: <ArrowUpRight className="h-5 w-5" />, color: 'text-destructive' },
  TRANSFER_RECEIVE: { label: '티켓 받기', icon: <ArrowDownLeft className="h-5 w-5" />, color: 'text-primary' },
  REFUND: { label: '환불', icon: <ArrowDownLeft className="h-5 w-5" />, color: 'text-primary' },
}

export function WalletHistoryScreen() {
  const { walletTxs, goBack } = useApp()

  const formatDate = (ts: number) => {
    const date = new Date(ts)
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`
  }

  return (
    <AppShell
      title="지갑 거래 내역"
      showBack
      onBack={goBack}
      showBottomNav={false}
      rightElement={<TutorialHelpButton tutorialId="wallet-history" />}
    >
      <div className="flex flex-1 flex-col gap-2 px-4 py-4">
        {walletTxs.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <LogIn className="mb-3 h-12 w-12 text-muted-foreground/30" />
            <p className="text-sm text-muted-foreground">거래 내역이 아직 없어요.</p>
          </div>
        ) : (
          walletTxs.map((tx) => {
            const meta = TX_TYPE_LABELS[tx.type] || TX_TYPE_LABELS.CHARGE
            const isIn = tx.amount >= 0

            return (
              <div
                key={tx.id}
                className="flex items-center gap-3 rounded-xl border border-border bg-card p-3 transition-colors hover:bg-secondary/50"
              >
                <div className={`flex h-10 w-10 items-center justify-center rounded-full bg-secondary ${meta.color}`}>
                  {meta.icon}
                </div>

                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium text-foreground">{tx.label}</p>
                  <p className="text-xs text-muted-foreground">{formatDate(tx.createdAt)}</p>
                </div>

                <div className="flex-shrink-0 text-right">
                  <p className={`text-sm font-semibold ${isIn ? 'text-primary' : 'text-destructive'}`}>
                    {isIn ? '+' : ''}
                    {tx.amount.toLocaleString()} CTK
                  </p>
                  <p className="text-xs text-muted-foreground">잔액: {tx.balance.toLocaleString()}</p>
                </div>
              </div>
            )
          })
        )}
      </div>
    </AppShell>
  )
}
