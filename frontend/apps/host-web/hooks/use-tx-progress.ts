"use client"

import * as React from "react"
import type { HostShowDetail, HostShowContractApproval, TxStatus } from "@/lib/show-manage-api"
import { savePendingShowTx, clearPendingShowTx, type PendingShowTx } from "@/lib/show-tx-progress"

type DisplayMode = "modal" | "dock"

interface ActionPayload {
  showDetail: HostShowDetail
  contractApprovals: HostShowContractApproval[]
  txId: number
  status: TxStatus
  startedAt: string
  displayMode: DisplayMode
}

type Action =
  | { type: "START_TX"; payload: ActionPayload }
  | { type: "UPDATE_STATUS"; status: TxStatus }
  | { type: "UPDATE_DISPLAY_MODE"; displayMode: DisplayMode }
  | { type: "DISMISS_TX" }

interface State {
  isActive: boolean
  txId: number | null
  status: TxStatus | null
  displayMode: DisplayMode
  showDetail: HostShowDetail | null
  contractApprovals: HostShowContractApproval[] | null
  startedAt: string | null
}

const initialState: State = {
  isActive: false,
  txId: null,
  status: null,
  displayMode: "modal",
  showDetail: null,
  contractApprovals: null,
  startedAt: null,
}

let memoryState: State = { ...initialState }

const listeners: Array<(state: State) => void> = []

function dispatch(action: Action) {
  const nextState = reducer(memoryState, action)

  if (Object.is(nextState, memoryState)) {
    return
  }

  memoryState = nextState
  listeners.forEach((listener) => {
    listener(memoryState)
  })
}

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case "START_TX": {
      const { payload } = action

      const pendingTx: PendingShowTx = {
        showId: payload.showDetail.showId,
        txId: payload.txId,
        status: payload.status,
        startedAt: payload.startedAt,
        displayMode: payload.displayMode,
      }
      savePendingShowTx(pendingTx)

      return {
        isActive: true,
        txId: payload.txId,
        status: payload.status,
        displayMode: payload.displayMode,
        showDetail: payload.showDetail,
        contractApprovals: payload.contractApprovals,
        startedAt: payload.startedAt,
      }
    }

    case "UPDATE_STATUS": {
      if (!state.isActive || !state.showDetail) return state
      if (state.status === action.status) return state

      const nextState = { ...state, status: action.status }

      if (action.status === "PENDING" || action.status === "SUBMITTED") {
        savePendingShowTx({
          showId: state.showDetail.showId,
          txId: state.txId!,
          status: action.status,
          startedAt: state.startedAt!,
          displayMode: state.displayMode,
        })
      }

      return nextState
    }

    case "UPDATE_DISPLAY_MODE": {
      if (!state.isActive || !state.showDetail) return state
      if (state.displayMode === action.displayMode) return state

      const nextState = { ...state, displayMode: action.displayMode }

      if (state.status === "PENDING" || state.status === "SUBMITTED") {
        savePendingShowTx({
          showId: state.showDetail.showId,
          txId: state.txId!,
          status: state.status,
          startedAt: state.startedAt!,
          displayMode: action.displayMode,
        })
      }

      return nextState
    }

    case "DISMISS_TX": {
      if (state.showDetail) {
        clearPendingShowTx()
      }
      return { ...initialState }
    }

    default:
      return state
  }
}

export function startTxProgress(payload: ActionPayload) {
  dispatch({ type: "START_TX", payload })
}

export function dismissTxProgress() {
  dispatch({ type: "DISMISS_TX" })
}

export function updateTxStatus(status: TxStatus) {
  dispatch({ type: "UPDATE_STATUS", status })
}

export function updateTxDisplayMode(displayMode: DisplayMode) {
  dispatch({ type: "UPDATE_DISPLAY_MODE", displayMode })
}

export function useTxProgress() {
  const [state, setState] = React.useState<State>(memoryState)

  React.useEffect(() => {
    listeners.push(setState)
    return () => {
      const index = listeners.indexOf(setState)
      if (index > -1) {
        listeners.splice(index, 1)
      }
    }
  }, [])

  return state
}
