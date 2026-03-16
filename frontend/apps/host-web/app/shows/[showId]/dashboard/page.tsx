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
            <div className="flex min-h-[60vh] items-center justify-center">
                <div className="flex flex-col items-center gap-3 text-muted-foreground">
                    <div className="size-8 animate-spin rounded-full border-2 border-current border-t-transparent" />
                    <p className="text-sm">대시보드 로딩 중...</p>
                </div>
            </div>
        )
    }

    if (error || !data) {
        return (
            <div className="flex min-h-[60vh] items-center justify-center">
                <p className="text-sm text-destructive">{error ?? "데이터를 찾을 수 없습니다."}</p>
            </div>
        )
    }

    return <DashboardContent data={data} />
}
