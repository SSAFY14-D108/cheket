"use client"

import { useEffect, useState } from "react"
import { useTxProgress, startTxProgress, updateTxStatus, updateTxDisplayMode, dismissTxProgress } from "@/hooks/use-tx-progress"
import { ShowTxProgressDock } from "./ShowTxProgressDock"
import { loadPendingShowTx } from "@/lib/show-tx-progress"
import { fetchShowDetail, fetchShowContracts, type TxStatus } from "@/lib/show-manage-api"

export function GlobalShowTxProgress() {
  const txState = useTxProgress()
  const [isInitializing, setIsInitializing] = useState(true)

  useEffect(() => {
    let isCancelled = false

    async function initializeFromStorage() {
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
  }, [txState.isActive])

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
