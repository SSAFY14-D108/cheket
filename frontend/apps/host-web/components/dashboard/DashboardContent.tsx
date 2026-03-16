"use client"

import Link from "next/link"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { SessionBookingChart } from "@/components/dashboard/SessionBookingChart"
import { SegmentedBar } from "@/components/dashboard/SegmentedBar"
import type { DashboardData } from "@/lib/dashboard-types"
import { ArrowLeft } from "lucide-react"

interface DashboardContentProps {
  data: DashboardData
}

export function DashboardContent({ data }: DashboardContentProps) {
  const { totalSales, bookingRate, revenueSplit, reservations, wallet } = data

  const revenueSegments = (revenueSplit.splits ?? []).map((s, i) => ({
    label: s.role,
    value: s.rateBps / 100, // bps → 퍼센트
    color: ["var(--chart-1)", "var(--chart-2)", "var(--chart-3)", "var(--chart-4)", "var(--chart-5)"][i] ?? "var(--chart-3)",
  }))

  const handleDeposit = () => {
    alert("충전하기 기능은 스마트 컨트랙트 연동 후 사용할 수 있습니다.")
  }

  const handleWithdraw = () => {
    alert("출금하기 기능은 스마트 컨트랙트 연동 후 사용할 수 있습니다.")
  }

  return (
    <main className="mx-auto max-w-6xl px-6 py-10">
      {/* 헤더 */}
      <div className="flex items-center gap-3">
        <Link
          href="/mypage"
          className="flex size-9 items-center justify-center rounded-sm bg-secondary text-secondary-foreground transition-colors hover:bg-secondary/80"
          aria-label="마이페이지로 돌아가기"
        >
          <ArrowLeft className="size-4" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-foreground">{reservations.title}</h1>
          <p className="text-sm text-muted-foreground">{reservations.venue || "-"}</p>
        </div>
      </div>

      {/* 총 판매현황 + 지갑 잔액 */}
      <section className="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-2">
        {/* 총 판매금액 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">총 판매금액</CardTitle>
          </CardHeader>
          <CardContent>
            <span className="text-3xl font-bold text-foreground">
              {totalSales.totalPrimarySales.toLocaleString()}원
            </span>
            <p className="mt-1 text-xs text-muted-foreground">1차 판매 기준</p>
          </CardContent>
        </Card>

        {/* 지갑 잔액 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">지갑 잔액</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                {wallet ? (
                  <>
                    <span className="text-3xl font-bold text-foreground">
                      {wallet.balance.toLocaleString()} CTK
                    </span>
                    <p className="mt-1 truncate text-xs text-muted-foreground">
                      {wallet.walletAddress}
                    </p>
                  </>
                ) : (
                  <>
                    <span className="text-3xl font-bold text-foreground">-</span>
                    <p className="mt-1 text-xs text-muted-foreground">
                      지갑 정보를 불러오지 못했습니다.
                    </p>
                  </>
                )}
              </div>
              <div className="flex gap-2 shrink-0">
                <button
                  onClick={handleDeposit}
                  className="rounded-sm bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
                >
                  충전하기
                </button>
                <button
                  onClick={handleWithdraw}
                  className="rounded-sm border border-border bg-card px-4 py-2 text-sm font-medium text-foreground transition-colors hover:bg-secondary"
                >
                  출금하기
                </button>
              </div>
            </div>
          </CardContent>
        </Card>
      </section>

      {/* 회차별 예매 현황 막대 그래프 */}
      <section className="mt-6">
        <SessionBookingChart sessions={reservations.sessions} />
      </section>

      {/* 예매율 + 수익 배분 */}
      <section className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* 예매율 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">전체 예매율</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col gap-4">
              <div className="flex items-end gap-2">
                <span className="text-3xl font-bold text-foreground">
                  {bookingRate.bookingRate.toFixed(1)}%
                </span>
                <span className="pb-1 text-sm text-muted-foreground">
                  ({bookingRate.reservedSeats.toLocaleString()} / {bookingRate.capacity.toLocaleString()}석)
                </span>
              </div>
              <div className="h-3 w-full overflow-hidden rounded-full bg-secondary">
                <div
                  className="h-full rounded-full transition-all"
                  style={{
                    width: `${bookingRate.bookingRate}%`,
                    backgroundColor: "var(--chart-1)",
                  }}
                />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 수익 배분 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">수익 배분</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col gap-4">
              <p className="text-sm text-muted-foreground">
                총 수익: <span className="font-semibold text-foreground">{revenueSplit.totalRevenue} ETH</span>
              </p>
              <SegmentedBar title="배분 비율" segments={revenueSegments} />
              <div className="flex flex-col gap-1.5 text-xs text-muted-foreground">
                {(revenueSplit.splits ?? []).map((s) => (
                  <div key={s.role} className="flex justify-between">
                    <span>{s.role}</span>
                    <span className="font-medium text-foreground">{s.amount.toFixed(4)} ETH</span>
                  </div>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>
      </section>
    </main>
  )
}
