import type { TxStatus } from "@/lib/show-manage-api"

export interface PendingShowTx {
  showId: number
  txId: number
  status: TxStatus
  startedAt: string
  displayMode: "modal" | "dock"
}

function canUseStorage() {
  return typeof window !== "undefined" && typeof window.localStorage !== "undefined"
}

function buildStorageKey(showId: number) {
  return `host-web.pending-show-tx.${showId}`
}

export function loadPendingShowTx(showId: number) {
  if (!canUseStorage()) {
    return null
  }

  const rawValue = window.localStorage.getItem(buildStorageKey(showId))

  if (!rawValue) {
    return null
  }

  try {
    const parsed = JSON.parse(rawValue) as Partial<PendingShowTx> & {
      isExpanded?: boolean
    }

    if (
      parsed.showId !== showId ||
      typeof parsed.txId !== "number" ||
        typeof parsed.startedAt !== "string"
      ) {
        window.localStorage.removeItem(buildStorageKey(showId))
        return null
      }

    const status =
      parsed.status === "SUBMITTED" ||
      parsed.status === "CONFIRMED" ||
      parsed.status === "FAILED"
        ? parsed.status
        : "PENDING"

    const displayMode =
      parsed.displayMode === "dock" || parsed.displayMode === "modal"
        ? parsed.displayMode
        : parsed.isExpanded
          ? "modal"
          : "dock"

    return {
      showId,
      txId: parsed.txId,
      status,
      startedAt: parsed.startedAt,
      displayMode,
    } satisfies PendingShowTx
  } catch {
    window.localStorage.removeItem(buildStorageKey(showId))
    return null
  }
}

export function savePendingShowTx(pendingTx: PendingShowTx) {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.setItem(
    buildStorageKey(pendingTx.showId),
    JSON.stringify(pendingTx),
  )
}

export function clearPendingShowTx(showId: number) {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.removeItem(buildStorageKey(showId))
}
