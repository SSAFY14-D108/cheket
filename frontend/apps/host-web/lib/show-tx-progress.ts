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

function buildStorageKey() {
  return `host-web.pending-show-tx`
}

export function loadPendingShowTx() {
  if (!canUseStorage()) {
    return null
  }

  const rawValue = window.localStorage.getItem(buildStorageKey())

  if (!rawValue) {
    return null
  }

  try {
    const parsed = JSON.parse(rawValue) as Partial<PendingShowTx> & {
      isExpanded?: boolean
    }

    if (
      typeof parsed.showId !== "number" ||
      typeof parsed.txId !== "number" ||
      typeof parsed.startedAt !== "string"
    ) {
      window.localStorage.removeItem(buildStorageKey())
      return null
    }

    const showId = parsed.showId

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
    window.localStorage.removeItem(buildStorageKey())
    return null
  }
}

export function savePendingShowTx(pendingTx: PendingShowTx) {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.setItem(
    buildStorageKey(),
    JSON.stringify(pendingTx),
  )
}

export function clearPendingShowTx() {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.removeItem(buildStorageKey())
}
