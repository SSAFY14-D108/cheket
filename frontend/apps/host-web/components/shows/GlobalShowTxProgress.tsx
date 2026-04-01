"use client"

import { useEffect, useRef, useState } from "react"
import { useTxProgress, startTxProgress, updateTxStatus, updateTxDisplayMode, dismissTxProgress } from "@/hooks/use-tx-progress"
import { ShowTxProgressDock } from "./ShowTxProgressDock"
import { loadPendingShowTx } from "@/lib/show-tx-progress"
import { fetchShowDetail, fetchShowContracts, type TxStatus } from "@/lib/show-manage-api"

export function GlobalShowTxProgress() {
  const txState = useTxProgress()
  const [isInitializing, setIsInitializing] = useState(true)
  const hasInitializedRef = useRef(false)

  // 마운트 시 딱 한 번만 실행: localStorage 기반 복원
  // txState.isActive를 의존 배열에 넣으면 startTxProgress() 호출 후
  // isActive 변경 → effect 재실행 → 무한 루프 발생하므로 [] 사용
  useEffect(() => {
    if (hasInitializedRef.current) return
    hasInitializedRef.current = true

    let isCancelled = false

    async function initializeFromStorage() {
      // 이미 전역 상태에 활성 tx가 있으면(e.g. 상세 페이지에서 방금 시작) 그냥 초기화 완료
      if (txState.isActive) {
        setIsInitializing(false)
        return
      }

      const pendingTx = loadPendingShowTx()

      if (!pendingTx) {
        setIsInitializing(false)
        return
      }

      try {
        const [showDetail, contractApprovals] = await Promise.all([
          fetchShowDetail(pendingTx.showId),
          fetchShowContracts(pendingTx.showId),
        ])

        if (!isCancelled) {
          startTxProgress({
            showDetail,
            contractApprovals,
            txId: pendingTx.txId,
            status: pendingTx.status,
            startedAt: pendingTx.startedAt,
            displayMode: pendingTx.displayMode,
          })
        }
      } catch (error) {
        console.error("Failed to restore pending tx state:", error)
      } finally {
        if (!isCancelled) {
          setIsInitializing(false)
        }
      }
    }

    void initializeFromStorage()

    return () => {
      isCancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  if (isInitializing || !txState.isActive || !txState.showDetail || !txState.contractApprovals || !txState.txId) {
    return null
  }

  const handleMinimize = () => {
    updateTxDisplayMode("dock")
  }

  const handleRestore = () => {
    updateTxDisplayMode("modal")
  }

  const handleStatusChange = (status: TxStatus) => {
    updateTxStatus(status)
  }

  const handleDismiss = () => {
    dismissTxProgress()
  }

  const handleSettled = (status: Extract<TxStatus, "CONFIRMED" | "FAILED">) => {
    // We can show a toast here if we want, but it's optional
    // ShowDetailView already showed a toast if it was mounted.
    // Given the widget is persistent, it will show the FAILED/CONFIRMED state directly in the UI.
  }

  return (
    <ShowTxProgressDock
      txId={txState.txId}
      initialStatus={txState.status || "PENDING"}
      displayMode={txState.displayMode}
      showDetail={txState.showDetail}
      contractApprovals={txState.contractApprovals}
      onMinimize={handleMinimize}
      onRestore={handleRestore}
      onStatusChange={handleStatusChange}
      onDismiss={handleDismiss}
      onSettled={handleSettled}
    />
  )
}
