"use client"

import { useEffect, useState } from "react"
import { useParams } from "next/navigation"
import { DashboardContent } from "@/components/dashboard/DashboardContent"
import type { DashboardData } from "@/lib/dashboard-types"
import {
    mockDashboardTotalSales,
    mockDashboardBookingRate,
    mockDashboardRevenueSplit,
    mockDashboardReservationsNew,
    mockWalletBalance,
} from "@/lib/mock-data"

/** 대시보드 자동 갱신 주기 (ms) ← 여기서 조정하세요. 예: 10_000 = 10초, 60_000 = 1분 */
const REFRESH_INTERVAL_MS = 30_000

// TODO: 백엔드 준비되면 아래 mock fetch 함수들을 실제 API 호출로 교체
async function fetchDashboardData(showId: string): Promise<DashboardData> {
    // 실제 API 연동 시 아래 주석을 해제하고 mock 데이터를 제거합니다.
    // const [totalSales, bookingRate, revenueSplit, reservations, wallet] = await Promise.all([
    //   fetch(`/api/v1/hosts/shows/${showId}/dashboard/total-sales`).then(r => r.json()).then(r => r.data),
    //   fetch(`/api/v1/hosts/shows/${showId}/dashboard/booking-rate`).then(r => r.json()).then(r => r.data),
    //   fetch(`/api/v1/hosts/shows/${showId}/dashboard/revenue-split`).then(r => r.json()).then(r => r.data),
    //   fetch(`/api/v1/hosts/shows/${showId}/dashboard/reservations`).then(r => r.json()).then(r => r.data),
    //   fetch(`/api/v1/wallets/balance`).then(r => r.json()).then(r => r.data),
    // ])

    // Mock: 임시로 mock 데이터 반환
    await new Promise((r) => setTimeout(r, 300)) // 로딩 시뮬레이션
    return {
        totalSales: mockDashboardTotalSales,
        bookingRate: mockDashboardBookingRate,
        revenueSplit: mockDashboardRevenueSplit,
        reservations: mockDashboardReservationsNew,
        wallet: mockWalletBalance,
    }
}

export default function DashboardPage() {
    const params = useParams()
    const showId = params.showId as string

    const [data, setData] = useState<DashboardData | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        if (!showId) return

        const load = () => {
            fetchDashboardData(showId)
                .then(setData)
                .catch(() => setError("대시보드 데이터를 불러오는데 실패했습니다."))
                .finally(() => setLoading(false))
        }

        setLoading(true)
        load() // 최초 1회 즉시 로드

        const intervalId = setInterval(load, REFRESH_INTERVAL_MS) // 주기적 갱신
        return () => clearInterval(intervalId) // 페이지 이탈 시 정지
    }, [showId])

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
