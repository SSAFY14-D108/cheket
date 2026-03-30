"use client"

import { useEffect, useState, type ReactNode } from "react"

let isWorkerStarted = false
let workerStartPromise: Promise<void> | null = null

interface MockServiceWorkerProviderProps {
  children: ReactNode
}

function isMockingEnabled() {
  return process.env.NEXT_PUBLIC_API_MOCKING === "true"
}

function ensureWorkerStarted() {
  if (!workerStartPromise) {
    workerStartPromise = import("@/mocks/browser")
      .then(({ worker }) => worker.start({ onUnhandledRequest: "bypass" }))
      .then(() => {
        isWorkerStarted = true
      })
  }

  return workerStartPromise
}

export function MockServiceWorkerProvider({ children }: MockServiceWorkerProviderProps) {
  const [isReady, setIsReady] = useState(
    process.env.NODE_ENV !== "development" || !isMockingEnabled() || isWorkerStarted
  )

  useEffect(() => {
    if (process.env.NODE_ENV !== "development" || !isMockingEnabled()) {
      return
    }

    if (isWorkerStarted) {
      return
    }

    void ensureWorkerStarted()
      .then(() => {
        setIsReady(true)
      })
      .catch((error) => {
        console.error("Failed to start MSW worker", error)
        setIsReady(true)
      })
  }, [])

  if (!isReady) {
    return null
  }

  return <>{children}</>
}
