'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { ChevronLeft, Check, AlertCircle, Clock } from 'lucide-react'

export function TxHistoryScreen() {
  const { goBack, txRecords } = useApp()

  const getTxTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      PURCHASE: '티켓 구매',
      RESALE_LIST: '리세일 등록',
      RESALE_BUY: '리세일 구매',
      TRANSFER: '티켓 양도',
    }
    return labels[type] || type
  }

  const getTxTypeColor = (type: string) => {
    const colors: Record<string, string> = {
      PURCHASE: 'bg-blue-50 text-blue-700 border-blue-200',
      RESALE_LIST: 'bg-purple-50 text-purple-700 border-purple-200',
      RESALE_BUY: 'bg-green-50 text-green-700 border-green-200',
      TRANSFER: 'bg-orange-50 text-orange-700 border-orange-200',
    }
    return colors[type] || 'bg-gray-50 text-gray-700 border-gray-200'
  }

  const getStatusIcon = (status: string) => {
    if (status === 'CONFIRMED') return <Check className="w-5 h-5 text-primary" />
    if (status === 'FAILED') return <AlertCircle className="w-5 h-5 text-destructive" />
    return <Clock className="w-5 h-5 text-muted-foreground" />
  }

  const getStatusLabel = (status: string) => {
    const labels: Record<string, string> = {
      CONFIRMED: '완료',
      FAILED: '실패',
      PENDING: '대기중',
      CONFIRMING: '처리중',
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

  const formatHash = (hash: string) => {
    return `${hash.slice(0, 10)}...${hash.slice(-8)}`
  }

  return (
    <AppShell>
      <div className="min-h-screen bg-background flex flex-col">
        {/* Header */}
        <div className="sticky top-0 bg-card border-b border-border px-4 py-3 flex items-center gap-3 z-10">
          <button
            onClick={goBack}
            className="p-1 hover:bg-muted rounded-lg active:scale-95 transition-all"
            aria-label="돌아가기"
          >
            <ChevronLeft className="w-6 h-6 text-foreground" />
          </button>
          <h1 className="text-lg font-bold text-foreground">티켓 거래 내역</h1>
        </div>

        {/* TX List */}
        <div className="flex-1 overflow-y-auto">
          {txRecords.length === 0 ? (
            <div className="flex flex-col items-center justify-center min-h-[60vh] p-4 text-center">
              <div className="w-16 h-16 rounded-full bg-muted flex items-center justify-center mb-3">
                <Clock className="w-8 h-8 text-muted-foreground" />
              </div>
              <p className="text-foreground font-medium">거래 내역이 없습니다</p>
              <p className="text-sm text-muted-foreground mt-1">콘서트 티켓을 구매하면 내역이 표시됩니다</p>
            </div>
          ) : (
            <div className="divide-y divide-border">
              {txRecords.map((tx) => (
                <div key={tx.id} className="bg-card border-b border-border p-4">
                  <div className="flex items-start justify-between gap-3 mb-3">
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-foreground truncate">{tx.label}</p>
                      <p className="text-xs text-muted-foreground mt-0.5">
                        {formatDate(tx.createdAt)}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      {getStatusIcon(tx.status)}
                      <span className={`text-xs font-semibold px-2 py-1 rounded-full border ${
                        tx.status === 'CONFIRMED' 
                          ? 'bg-green-50 text-green-700 border-green-200'
                          : tx.status === 'FAILED'
                          ? 'bg-red-50 text-red-700 border-red-200'
                          : 'bg-muted text-muted-foreground border-border'
                      }`}>
                        {getStatusLabel(tx.status)}
                      </span>
                    </div>
                  </div>

                  {/* TX Type Badge */}
                  <div className="mb-3">
                    <span className={`text-xs font-semibold px-2.5 py-1 rounded-full border inline-block ${getTxTypeColor(tx.type)}`}>
                      {getTxTypeLabel(tx.type)}
                    </span>
                  </div>

                  {/* TX Details */}
                  <div className="bg-muted rounded-lg p-3 space-y-2 text-xs">
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">TX Hash</span>
                      <code className="font-mono text-foreground">{formatHash(tx.txHash)}</code>
                    </div>
                    {tx.amount !== undefined && (
                      <div className="flex items-center justify-between">
                        <span className="text-muted-foreground">금액</span>
                        <span className="font-semibold text-foreground">{tx.amount.toLocaleString()} CTK</span>
                      </div>
                    )}
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">Confirmations</span>
                      <span className="font-semibold text-foreground">{tx.confirmations}/12</span>
                    </div>
                    {tx.errorMessage && (
                      <div className="pt-2 border-t border-border">
                        <p className="text-destructive">{tx.errorMessage}</p>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </AppShell>
  )
}
