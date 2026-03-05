'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { ChevronLeft, ArrowUpRight, ArrowDownLeft, LogIn } from 'lucide-react'

const TX_TYPE_LABELS: Record<string, { label: string; icon: React.ReactNode; color: string }> = {
  CHARGE: { label: 'CTK 충전', icon: <ArrowDownLeft className="w-5 h-5" />, color: 'text-primary' },
  PURCHASE: { label: '티켓 구매', icon: <ArrowUpRight className="w-5 h-5" />, color: 'text-destructive' },
  RESALE_BUY: { label: '리세일 구매', icon: <ArrowUpRight className="w-5 h-5" />, color: 'text-destructive' },
  RESALE_SELL: { label: '리세일 판매', icon: <ArrowDownLeft className="w-5 h-5" />, color: 'text-primary' },
  TRANSFER_SEND: { label: '양도 송신', icon: <ArrowUpRight className="w-5 h-5" />, color: 'text-destructive' },
  TRANSFER_RECEIVE: { label: '양도 수신', icon: <ArrowDownLeft className="w-5 h-5" />, color: 'text-primary' },
}

export function WalletHistoryScreen() {
  const { walletTxs, goBack } = useApp()

  const formatDate = (ts: number) => {
    const d = new Date(ts)
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
  }

  return (
    <AppShell>
      <div className="flex flex-col min-h-screen bg-background">
        {/* Header */}
        <div className="flex items-center gap-3 px-4 py-3 border-b border-border sticky top-0 bg-background">
          <button onClick={goBack} className="p-1.5 hover:bg-secondary rounded-lg active:scale-95 transition-all">
            <ChevronLeft className="w-5 h-5 text-foreground" />
          </button>
          <h1 className="text-lg font-bold text-foreground">지갑 거래 내역</h1>
        </div>

        {/* TX List */}
        <div className="flex-1 px-4 py-4 flex flex-col gap-2 overflow-y-auto">
          {walletTxs.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <LogIn className="w-12 h-12 text-muted-foreground mb-3 opacity-30" />
              <p className="text-sm text-muted-foreground">거래 내역이 없습니다.</p>
            </div>
          ) : (
            walletTxs.map((tx) => {
              const meta = TX_TYPE_LABELS[tx.type] || TX_TYPE_LABELS.CHARGE
              const isIn = tx.amount >= 0
              return (
                <div key={tx.id} className="flex items-center gap-3 p-3 bg-card border border-border rounded-xl hover:bg-secondary/50 transition-colors">
                  {/* Icon */}
                  <div className={`w-10 h-10 rounded-full bg-secondary flex items-center justify-center ${meta.color}`}>
                    {meta.icon}
                  </div>

                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-foreground truncate">{tx.label}</p>
                    <p className="text-xs text-muted-foreground">{formatDate(tx.createdAt)}</p>
                  </div>

                  {/* Amount */}
                  <div className="text-right flex-shrink-0">
                    <p className={`text-sm font-semibold ${isIn ? 'text-primary' : 'text-destructive'}`}>
                      {isIn ? '+' : ''}{tx.amount.toLocaleString()} CTK
                    </p>
                    <p className="text-xs text-muted-foreground">잔액: {tx.balance.toLocaleString()}</p>
                  </div>
                </div>
              )
            })
          )}
        </div>
      </div>
    </AppShell>
  )
}
