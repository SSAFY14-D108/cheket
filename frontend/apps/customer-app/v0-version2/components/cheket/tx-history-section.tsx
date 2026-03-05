'use client'

import { useApp } from '@/lib/app-context'
import { TxRecord, TxType, TxStatus } from '@/lib/types'
import { useState } from 'react'
import {
  ShoppingCart, Tag, ArrowRightLeft, RefreshCw,
  CheckCircle2, XCircle, Clock, ChevronDown, ChevronUp, ExternalLink
} from 'lucide-react'

// ── helpers ───────────────────────────────────────────────────────────────────

function txTypeLabel(type: TxType) {
  switch (type) {
    case 'PURCHASE':    return '티켓 구매'
    case 'RESALE_LIST': return '리세일 등록'
    case 'RESALE_BUY':  return '리세일 구매'
    case 'TRANSFER':    return '티켓 양도'
  }
}

function txTypeIcon(type: TxType) {
  switch (type) {
    case 'PURCHASE':    return ShoppingCart
    case 'RESALE_LIST': return Tag
    case 'RESALE_BUY':  return ShoppingCart
    case 'TRANSFER':    return ArrowRightLeft
  }
}

function formatTime(ts: number) {
  const d = new Date(ts)
  return d.toLocaleString('ko-KR', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function shortHash(hash: string) {
  return `${hash.slice(0, 8)}...${hash.slice(-6)}`
}

// ── Status badge ──────────────────────────────────────────────────────────────

function StatusBadge({ status, confirmations }: { status: TxStatus; confirmations: number }) {
  if (status === 'CONFIRMED') {
    return (
      <span className="inline-flex items-center gap-1 text-xs font-semibold text-emerald-600 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-full">
        <CheckCircle2 className="w-3 h-3" /> 확인됨
      </span>
    )
  }
  if (status === 'FAILED') {
    return (
      <span className="inline-flex items-center gap-1 text-xs font-semibold text-red-500 bg-red-50 border border-red-200 px-2 py-0.5 rounded-full">
        <XCircle className="w-3 h-3" /> 실패
      </span>
    )
  }
  if (status === 'CONFIRMING') {
    return (
      <span className="inline-flex items-center gap-1 text-xs font-semibold text-amber-600 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded-full">
        <RefreshCw className="w-3 h-3 animate-spin" /> {confirmations}/12 확인중
      </span>
    )
  }
  // PENDING
  return (
    <span className="inline-flex items-center gap-1 text-xs font-semibold text-blue-500 bg-blue-50 border border-blue-200 px-2 py-0.5 rounded-full">
      <Clock className="w-3 h-3 animate-pulse" /> 대기중
    </span>
  )
}

// ── Progress bar ──────────────────────────────────────────────────────────────

function ConfirmProgress({ status, confirmations }: { status: TxStatus; confirmations: number }) {
  if (status === 'CONFIRMED') return null
  if (status === 'FAILED')    return null

  const pct = Math.min((confirmations / 12) * 100, 100)
  return (
    <div className="mt-2">
      <div className="flex items-center justify-between mb-1">
        <span className="text-[10px] text-muted-foreground">블록 확인</span>
        <span className="text-[10px] text-muted-foreground">{confirmations} / 12</span>
      </div>
      <div className="h-1.5 w-full bg-secondary rounded-full overflow-hidden">
        <div
          className="h-full bg-primary rounded-full transition-all duration-700"
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  )
}

// ── Single TX row ─────────────────────────────────────────────────────────────

function TxRow({ tx }: { tx: TxRecord }) {
  const [open, setOpen] = useState(false)
  const Icon = txTypeIcon(tx.type)
  const isInFlight = tx.status === 'PENDING' || tx.status === 'CONFIRMING'

  return (
    <div className={`bg-card border rounded-xl overflow-hidden transition-all ${
      isInFlight ? 'border-primary/30' : tx.status === 'FAILED' ? 'border-red-200' : 'border-border'
    }`}>
      {/* Header row */}
      <button
        className="w-full flex items-center gap-3 p-3.5 text-left hover:bg-secondary/50 transition-colors"
        onClick={() => setOpen((v) => !v)}
        aria-expanded={open}
      >
        {/* Icon */}
        <div className={`w-9 h-9 rounded-full flex items-center justify-center flex-shrink-0 ${
          tx.status === 'FAILED' ? 'bg-red-100' : 'bg-primary/10'
        }`}>
          <Icon className={`w-4 h-4 ${tx.status === 'FAILED' ? 'text-red-500' : 'text-primary'}`} />
        </div>

        {/* Text */}
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-foreground truncate">{tx.label}</p>
          <p className="text-xs text-muted-foreground mt-0.5">{txTypeLabel(tx.type)} · {formatTime(tx.createdAt)}</p>
        </div>

        {/* Badge + chevron */}
        <div className="flex items-center gap-2 flex-shrink-0">
          <StatusBadge status={tx.status} confirmations={tx.confirmations} />
          {open ? <ChevronUp className="w-3.5 h-3.5 text-muted-foreground" /> : <ChevronDown className="w-3.5 h-3.5 text-muted-foreground" />}
        </div>
      </button>

      {/* Expanded detail */}
      {open && (
        <div className="px-4 pb-4 flex flex-col gap-2 border-t border-border pt-3">
          <ConfirmProgress status={tx.status} confirmations={tx.confirmations} />

          <div className="grid grid-cols-2 gap-x-4 gap-y-2 mt-1">
            {tx.amount !== undefined && (
              <>
                <span className="text-xs text-muted-foreground">결제 금액</span>
                <span className="text-xs font-semibold text-foreground text-right">{tx.amount.toLocaleString()} CTK</span>
              </>
            )}
            <span className="text-xs text-muted-foreground">TX 해시</span>
            <span className="text-xs font-mono text-foreground text-right">{shortHash(tx.txHash)}</span>

            {tx.confirmedAt && (
              <>
                <span className="text-xs text-muted-foreground">확인 시각</span>
                <span className="text-xs text-foreground text-right">{formatTime(tx.confirmedAt)}</span>
              </>
            )}
            {tx.errorMessage && (
              <>
                <span className="text-xs text-muted-foreground">오류</span>
                <span className="text-xs text-red-500 text-right">{tx.errorMessage}</span>
              </>
            )}
          </div>

          {/* Mock explorer link */}
          <a
            href="#"
            onClick={(e) => e.preventDefault()}
            className="inline-flex items-center gap-1 text-xs text-primary mt-1 hover:underline self-start"
          >
            <ExternalLink className="w-3 h-3" /> 블록 익스플로러에서 보기
          </a>
        </div>
      )}
    </div>
  )
}

// ── Main export ───────────────────────────────────────────────────────────────

export function TxHistorySection() {
  const { txRecords } = useApp()

  const inFlight = txRecords.filter((t) => t.status === 'PENDING' || t.status === 'CONFIRMING')
  const finished = txRecords.filter((t) => t.status === 'CONFIRMED' || t.status === 'FAILED')

  if (txRecords.length === 0) {
    return (
      <div className="bg-card border border-border rounded-xl p-6 text-center">
        <div className="w-12 h-12 rounded-full bg-secondary mx-auto mb-3 flex items-center justify-center">
          <RefreshCw className="w-6 h-6 text-muted-foreground" />
        </div>
        <p className="text-sm font-medium text-foreground mb-1">거래 내역 없음</p>
        <p className="text-xs text-muted-foreground">티켓을 구매하거나 리세일·양도하면<br />여기서 블록체인 트랜잭션 상태를 확인할 수 있습니다.</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-3">
      {/* In-flight */}
      {inFlight.length > 0 && (
        <div>
          <div className="flex items-center gap-2 mb-2">
            <RefreshCw className="w-3.5 h-3.5 text-primary animate-spin" />
            <span className="text-xs font-semibold text-primary uppercase tracking-wide">처리중 ({inFlight.length})</span>
          </div>
          <div className="flex flex-col gap-2">
            {inFlight.map((tx) => <TxRow key={tx.id} tx={tx} />)}
          </div>
        </div>
      )}

      {/* Finished */}
      {finished.length > 0 && (
        <div>
          {inFlight.length > 0 && <div className="h-px bg-border my-1" />}
          <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-2">완료된 거래</p>
          <div className="flex flex-col gap-2">
            {finished.map((tx) => <TxRow key={tx.id} tx={tx} />)}
          </div>
        </div>
      )}
    </div>
  )
}
