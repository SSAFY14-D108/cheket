"use client"

import { useEffect } from "react"

let isWorkerStarted = false

export function MockServiceWorkerProvider() {
  useEffect(() => {
    if (process.env.NODE_ENV !== "development") {
      return
    }

    if (isWorkerStarted) {
      return
    }

    isWorkerStarted = true

    void import("@/mocks/browser")
      .then(({ worker }) => worker.start({ onUnhandledRequest: "bypass" }))
      .catch((error) => {
        console.error("Failed to start MSW worker", error)
      })
  }, [])

  return null
}
