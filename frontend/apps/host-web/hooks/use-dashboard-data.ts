"use client"

import { useEffect, useRef, useState } from "react"
import { ApiError } from "@/lib/api"
import { fetchDashboardData } from "@/lib/dashboard-api"
import type { DashboardData } from "@/lib/dashboard-types"

const REFRESH_INTERVAL_MS = 30_000

export function useDashboardData(showId: string | undefined) {
  const [data, setData] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(Boolean(showId))
  const [error, setError] = useState<string | null>(showId ? null : "공연 정보를 찾을 수 없습니다.")
  const hasLoadedDataRef = useRef(false)

  useEffect(() => {
    if (!showId) {
      return
    }

    const currentShowId = showId
    let isCancelled = false
    hasLoadedDataRef.current = false

    async function loadDashboard() {
      try {
        const nextData = await fetchDashboardData(currentShowId)

        if (isCancelled) {
          return
        }

        hasLoadedDataRef.current = true
        setData(nextData)
        setError(null)
      } catch (error) {
        if (isCancelled) {
          return
        }

        const message =
          error instanceof ApiError
            ? error.message
            : "대시보드 데이터를 불러오는데 실패했습니다."

        if (!hasLoadedDataRef.current) {
          setError(message)
        }
      } finally {
        if (!isCancelled) {
          setLoading(false)
        }
      }
    }

    void loadDashboard()

    const intervalId = setInterval(() => {
      void loadDashboard()
    }, REFRESH_INTERVAL_MS)

    return () => {
      isCancelled = true
      clearInterval(intervalId)
    }
  }, [showId])

  return {
    data,
    loading,
    error,
  }
}
