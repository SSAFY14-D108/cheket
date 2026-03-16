'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { AlertCircle, Check, Clock } from 'lucide-react'
import { TutorialHelpButton } from '../tutorial-dialog'

export function TxHistoryScreen() {
  const { goBack, txRecords, tickets } = useApp()

  const derivedRecords = tickets
    .filter((ticket) => /^tkt_\d{3}$/.test(ticket.id))
    .map((ticket, index) => ({
      id: `ticket-history-${ticket.id}`,
      txHash: `0x${ticket.id.replace(/[^a-zA-Z0-9]/g, '').padEnd(64, '0').slice(0, 64)}`,
      type: ticket.status === 'LISTED' ? 'RESALE_LIST' : ticket.status === 'EXPIRED' ? 'REFUND' : 'PURCHASE',
      status: 'CONFIRMED' as const,
      label: ticket.eventName,
      amount: ticket.resalePrice ?? ticket.originalPrice,
      createdAt: Date.now() - (index + 1) * 3600000,
      confirmedAt: Date.now() - (index + 1) * 3600000 + 180000,
      confirmations: 12,
    }))

  const allTxRecords = [...txRecords, ...derivedRecords]
    .filter((record, index, records) => records.findIndex((item) => item.id === record.id) === index)
    .sort((a, b) => b.createdAt - a.createdAt)

  const getTxTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      PURCHASE: '티켓 구매',
      RESALE_LIST: '재판매 등록',
      RESALE_BUY: '재판매 구매',
      TRANSFER: '티켓 양도',
      REFUND: '티켓 환불',
    }
    return labels[type] || type
  }

  const getTxTypeColor = (type: string) => {
    const colors: Record<string, string> = {
      PURCHASE: 'border-blue-200 bg-blue-50 text-blue-700',
      RESALE_LIST: 'border-purple-200 bg-purple-50 text-purple-700',
      RESALE_BUY: 'border-green-200 bg-green-50 text-green-700',
      TRANSFER: 'border-orange-200 bg-orange-50 text-orange-700',
      REFUND: 'border-amber-200 bg-amber-50 text-amber-700',
    }
    return colors[type] || 'border-gray-200 bg-gray-50 text-gray-700'
  }

  const getStatusIcon = (status: string) => {
    if (status === 'CONFIRMED') return <Check className="h-5 w-5 text-primary" />
    if (status === 'FAILED') return <AlertCircle className="h-5 w-5 text-destructive" />
    return <Clock className="h-5 w-5 text-muted-foreground" />
  }

  const getStatusLabel = (status: string) => {
    const labels: Record<string, string> = {
      CONFIRMED: '완료',
      FAILED: '실패',
      PENDING: '대기',
      CONFIRMING: '확인 중',
    }
    return labels[status] || status
  }

  const formatDate = (timestamp: number) => {
    const date = new Date(timestamp)
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const min = String(date.getMinutes()).padStart(2, '0')
    return `${month}.${day} ${hour}:${min}`
  }

  const formatHash = (hash: string) => `${hash.slice(0, 10)}...${hash.slice(-8)}`

  return (
    <AppShell
      title="거래 기록"
      showBack
      onBack={goBack}
      showBottomNav={false}
      rightElement={<TutorialHelpButton tutorialId="tx-history" />}
    >
      <div className="flex-1 overflow-y-auto">
        {allTxRecords.length === 0 ? (
          <div className="flex min-h-[60vh] flex-col items-center justify-center p-4 text-center">
            <div className="mb-3 flex h-16 w-16 items-center justify-center rounded-full bg-muted">
              <Clock className="h-8 w-8 text-muted-foreground" />
            </div>
            <p className="font-medium text-foreground">거래 기록이 아직 없어요.</p>
            <p className="mt-1 text-sm text-muted-foreground">
              구매, 재판매, 환불, 양도 내역이 생기면 이곳에서 확인할 수 있어요.
            </p>
          </div>
        ) : (
          <div className="divide-y divide-border">
            {allTxRecords.map((tx) => (
              <div key={tx.id} className="border-b border-border bg-card p-4">
                <div className="mb-3 flex items-start justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <p className="truncate font-medium text-foreground">{tx.label}</p>
                    <p className="mt-0.5 text-xs text-muted-foreground">{formatDate(tx.createdAt)}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    {getStatusIcon(tx.status)}
                    <span
                      className={`rounded-full border px-2 py-1 text-xs font-semibold ${
                        tx.status === 'CONFIRMED'
                          ? 'border-green-200 bg-green-50 text-green-700'
                          : tx.status === 'FAILED'
                            ? 'border-red-200 bg-red-50 text-red-700'
                            : 'border-border bg-muted text-muted-foreground'
                      }`}
                    >
                      {getStatusLabel(tx.status)}
                    </span>
                  </div>
                </div>

                <div className="mb-3">
                  <span className={`inline-block rounded-full border px-2.5 py-1 text-xs font-semibold ${getTxTypeColor(tx.type)}`}>
                    {getTxTypeLabel(tx.type)}
                  </span>
                </div>

                <div className="space-y-2 rounded-lg bg-muted p-3 text-xs">
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">TX Hash</span>
                    <code className="font-mono text-foreground">{formatHash(tx.txHash)}</code>
                  </div>
                  {tx.amount !== undefined ? (
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">금액</span>
                      <span className="font-semibold text-foreground">{tx.amount.toLocaleString()} CTK</span>
                    </div>
                  ) : null}
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">Confirmations</span>
                    <span className="font-semibold text-foreground">{tx.confirmations}/12</span>
                  </div>
                  {tx.errorMessage ? (
                    <div className="border-t border-border pt-2">
                      <p className="text-destructive">{tx.errorMessage}</p>
                    </div>
                  ) : null}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
