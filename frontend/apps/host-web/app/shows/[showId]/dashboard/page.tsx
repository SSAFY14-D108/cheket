"use client"

import { useParams } from "next/navigation"
import { DashboardContent } from "@/components/dashboard/DashboardContent"
import { useDashboardData } from "@/hooks/use-dashboard-data"

export default function DashboardPage() {
  const params = useParams()
  const rawShowId = params.showId
  const showId = Array.isArray(rawShowId) ? rawShowId[0] : rawShowId

  const { data, loading, error } = useDashboardData(showId)

  if (loading) {
    return (
      <div className="min-h-screen bg-white">
        <div className="flex min-h-[60vh] items-center justify-center">
          <div className="flex flex-col items-center gap-3 text-muted-foreground">
            <div className="size-8 animate-spin rounded-full border-2 border-current border-t-transparent" />
            <p className="text-sm">로딩 중입니다.</p>
          </div>
        </div>
      </div>
    )
  }

  if (error || !data) {
    return (
      <div className="min-h-screen bg-white">
        <div className="flex min-h-[60vh] items-center justify-center">
          <p className="text-sm text-destructive">
            {error ?? "대시보드 정보를 불러오지 못했습니다."}
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-white">
      <DashboardContent data={data} />
    </div>
  )
}
