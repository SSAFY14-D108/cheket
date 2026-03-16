'use client'

import { useState } from 'react'
import { ArrowRightLeft, CheckCircle2, ChevronDown, ChevronUp, Clock, ExternalLink, RefreshCw, ShoppingCart, Tag, XCircle } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { TxRecord, TxStatus, TxType } from '@/lib/types'

function txTypeLabel(type: TxType) {
  switch (type) {
    case 'PURCHASE':
      return '티켓 구매'
    case 'RESALE_LIST':
      return '재판매 등록'
    case 'RESALE_BUY':
      return '재판매 구매'
    case 'TRANSFER':
      return '티켓 양도'
  }
}

function txTypeIcon(type: TxType) {
  switch (type) {
    case 'PURCHASE':
      return ShoppingCart
    case 'RESALE_LIST':
      return Tag
    case 'RESALE_BUY':
      return ShoppingCart
    case 'TRANSFER':
      return ArrowRightLeft
  }
}

function formatTime(ts: number) {
  const d = new Date(ts)
  return d.toLocaleString('ko-KR', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function shortHash(hash: string) {
  return `${hash.slice(0, 8)}...${hash.slice(-6)}`
}

function StatusBadge({ status, confirmations }: { status: TxStatus; confirmations: number }) {
  if (status === 'CONFIRMED') {
    return (
      <span className="inline-flex items-center gap-1 rounded-full border border-[#d7dde6] bg-white px-2 py-0.5 text-xs font-semibold text-[#333333]">
        <CheckCircle2 className="h-3 w-3" /> 완료
      </span>
    )
  }
  if (status === 'FAILED') {
    return (
      <span className="inline-flex items-center gap-1 rounded-full border border-red-200 bg-red-50 px-2 py-0.5 text-xs font-semibold text-red-500">
        <XCircle className="h-3 w-3" /> 실패
      </span>
    )
  }
  if (status === 'CONFIRMING') {
    return (
      <span className="inline-flex items-center gap-1 rounded-full border border-amber-200 bg-amber-50 px-2 py-0.5 text-xs font-semibold text-amber-600">
        <RefreshCw className="h-3 w-3 animate-spin" /> {confirmations}/12 확인중
      </span>
    )
  }
  return (
    <span className="inline-flex items-center gap-1 rounded-full border border-[#d7dde6] bg-white px-2 py-0.5 text-xs font-semibold text-[#6b7280]">
      <Clock className="h-3 w-3 animate-pulse" /> 대기중
    </span>
  )
}

function ConfirmProgress({ status, confirmations }: { status: TxStatus; confirmations: number }) {
  if (status === 'CONFIRMED' || status === 'FAILED') return null

  const pct = Math.min((confirmations / 12) * 100, 100)
  return (
    <div className="mt-2">
      <div className="mb-1 flex items-center justify-between">
        <span className="text-[10px] text-muted-foreground">블록체인 확인</span>
        <span className="text-[10px] text-muted-foreground">{confirmations} / 12</span>
      </div>
      <div className="h-1.5 w-full overflow-hidden rounded-full bg-secondary">
        <div className="h-full rounded-full bg-[#9aa4b2] transition-all duration-700" style={{ width: `${pct}%` }} />
      </div>
    </div>
  )
}

function TxRow({ tx }: { tx: TxRecord }) {
  const [open, setOpen] = useState(false)
  const Icon = txTypeIcon(tx.type)
  const isInFlight = tx.status === 'PENDING' || tx.status === 'CONFIRMING'

  return (
    <div className={`rounded-xl overflow-hidden transition-all ${tx.status === 'FAILED' ? 'border border-red-200 bg-red-50/30' : isInFlight ? 'gradient-outline-surface-soft' : 'gradient-outline-surface'}`}>
      <button className="w-full flex items-center gap-3 p-3.5 text-left hover:bg-secondary/30 transition-colors" onClick={() => setOpen((v) => !v)} aria-expanded={open}>
        <div className={`flex h-9 w-9 items-center justify-center rounded-full flex-shrink-0 ${tx.status === 'FAILED' ? 'bg-red-100' : 'gradient-outline-icon-button'}`}>
          <Icon className={`h-4 w-4 ${tx.status === 'FAILED' ? 'text-red-500' : 'text-[#333333]'}`} />
        </div>

        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-medium text-foreground">{tx.label}</p>
          <p className="mt-0.5 text-xs text-muted-foreground">
            {txTypeLabel(tx.type)} · {formatTime(tx.createdAt)}
          </p>
        </div>

        <div className="flex items-center gap-2 flex-shrink-0">
          <StatusBadge status={tx.status} confirmations={tx.confirmations} />
          {open ? <ChevronUp className="h-3.5 w-3.5 text-muted-foreground" /> : <ChevronDown className="h-3.5 w-3.5 text-muted-foreground" />}
        </div>
      </button>

      {open && (
        <div className="flex flex-col gap-2 border-t border-border px-4 pb-4 pt-3">
          <ConfirmProgress status={tx.status} confirmations={tx.confirmations} />

          <div className="mt-1 grid grid-cols-2 gap-x-4 gap-y-2">
            {tx.amount !== undefined && (
              <>
                <span className="text-xs text-muted-foreground">금액</span>
                <span className="text-right text-xs font-semibold text-foreground">{tx.amount.toLocaleString()} CTK</span>
              </>
            )}
            <span className="text-xs text-muted-foreground">TX Hash</span>
            <span className="text-right text-xs font-mono text-foreground">{shortHash(tx.txHash)}</span>

            {tx.confirmedAt && (
              <>
                <span className="text-xs text-muted-foreground">확정 시각</span>
                <span className="text-right text-xs text-foreground">{formatTime(tx.confirmedAt)}</span>
              </>
            )}
            {tx.errorMessage && (
              <>
                <span className="text-xs text-muted-foreground">오류</span>
                <span className="text-right text-xs text-red-500">{tx.errorMessage}</span>
              </>
            )}
          </div>

          <a href="#" onClick={(e) => e.preventDefault()} className="inline-flex items-center gap-1 self-start mt-1 text-xs text-[#6b7280] hover:underline">
            <ExternalLink className="h-3 w-3" /> 블록체인 탐색기에서 보기
          </a>
        </div>
      )}
    </div>
  )
}

export function TxHistorySection() {
  const { txRecords } = useApp()

  const inFlight = txRecords.filter((t) => t.status === 'PENDING' || t.status === 'CONFIRMING')
  const finished = txRecords.filter((t) => t.status === 'CONFIRMED' || t.status === 'FAILED')

  if (txRecords.length === 0) {
    return (
      <div className="gradient-outline-surface rounded-xl p-6 text-center">
        <div className="gradient-outline-icon-button mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full">
          <RefreshCw className="h-6 w-6 text-muted-foreground" />
        </div>
        <p className="mb-1 text-sm font-medium text-foreground">거래 기록이 없어요</p>
        <p className="text-xs text-muted-foreground">티켓 구매, 재판매, 양도 내역이 생기면 이곳에서 블록체인 기록과 함께 확인할 수 있어요.</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-3">
      {inFlight.length > 0 && (
        <div>
          <div className="mb-2 flex items-center gap-2">
            <RefreshCw className="h-3.5 w-3.5 animate-spin text-[#6b7280]" />
            <span className="text-xs font-semibold uppercase tracking-wide text-[#6b7280]">처리중 ({inFlight.length})</span>
          </div>
          <div className="flex flex-col gap-2">{inFlight.map((tx) => <TxRow key={tx.id} tx={tx} />)}</div>
        </div>
      )}

      {finished.length > 0 && (
        <div>
          {inFlight.length > 0 && <div className="my-1 h-px bg-border" />}
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">완료된 거래</p>
          <div className="flex flex-col gap-2">{finished.map((tx) => <TxRow key={tx.id} tx={tx} />)}</div>
        </div>
      )}
    </div>
  )
}
