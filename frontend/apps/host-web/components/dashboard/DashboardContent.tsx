"use client"

import Link from "next/link"
import { ArrowLeft } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { SessionBookingChart } from "@/components/dashboard/SessionBookingChart"
import { SegmentedBar } from "@/components/dashboard/SegmentedBar"
import type { DashboardData } from "@/lib/dashboard-types"

interface DashboardContentProps {
  data: DashboardData
}

function formatEthAmount(value: number) {
  return value.toLocaleString(undefined, {
    minimumFractionDigits: 0,
    maximumFractionDigits: 4,
  })
}

export function DashboardContent({ data }: DashboardContentProps) {
  if (!data) {
    return (
      <main className="mx-auto max-w-6xl px-6 py-10">
        <div className="flex min-h-[40vh] items-center justify-center text-sm text-muted-foreground">
          대시보드 데이터를 불러오는 중입니다.
        </div>
      </main>
    )
  }

  const { totalSales, bookingRate, revenueSplit, reservations } = data
  const sessions = reservations.sessions ?? []
  const sessionCount = sessions.length
  const safeBookingRate = Number.isFinite(bookingRate.bookingRate) ? bookingRate.bookingRate : 0
  const safeReservedSeats = Number.isFinite(bookingRate.reservedSeats) ? bookingRate.reservedSeats : 0
  const safeCapacity = Number.isFinite(bookingRate.capacity) ? bookingRate.capacity : 0
  const partialErrors = data.partialErrors ?? []
  const sortedSessionDates = sessions
    .map((session) => session.date)
    .filter(Boolean)
    .sort((left, right) => left.localeCompare(right))
  const periodLabel =
    sortedSessionDates.length === 0
      ? ""
      : sortedSessionDates.length === 1
        ? sortedSessionDates[0]
        : `${sortedSessionDates[0]} ~ ${sortedSessionDates[sortedSessionDates.length - 1]}`
  const averageSessionBookingRate =
    sessionCount <= 1
      ? safeBookingRate
      : sessionCount > 0
      ? sessions.reduce((sum, session) => {
          const rate =
            session.capacity > 0 ? (session.reservedSeats / session.capacity) * 100 : 0
          return sum + rate
        }, 0) / sessionCount
      : 0

  const revenueSegments = (revenueSplit.splits ?? []).map((split, index) => ({
    label: split.displayName || split.role,
    value: split.rateBps / 100,
    color:
      ["var(--chart-1)", "var(--chart-2)", "var(--chart-3)", "var(--chart-4)", "var(--chart-5)"][
        index
      ] ?? "var(--chart-3)",
  }))

  return (
    <main className="mx-auto max-w-6xl px-6 py-10">
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
          <p className="text-sm text-muted-foreground">
            {reservations.venue || "-"}
            {periodLabel ? ` · ${periodLabel}` : ""}
          </p>
        </div>
      </div>

      {partialErrors.length > 0 && (
        <div className="mt-6 rounded-sm border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-800">
          일부 통계 데이터를 불러오지 못했습니다. 확인 가능한 데이터만 먼저 표시합니다.
        </div>
      )}

      <section className="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-2">
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

        <Card>
          <CardHeader>
            <CardTitle className="text-base">전체 예매율</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col gap-4">
              <div className="flex items-end gap-2">
                <span className="text-3xl font-bold text-foreground">
                  {safeBookingRate.toFixed(1)}%
                </span>
                <span className="pb-1 text-sm text-muted-foreground">
                  ({safeReservedSeats.toLocaleString()} / {safeCapacity.toLocaleString()}석)
                </span>
              </div>
              <div className="h-3 w-full overflow-hidden rounded-full bg-secondary">
                <div
                  className="h-full rounded-full transition-all"
                  style={{
                    width: `${Math.min(Math.max(safeBookingRate, 0), 100)}%`,
                    backgroundColor: "var(--chart-1)",
                  }}
                />
              </div>
              <div className="flex flex-wrap items-center gap-x-5 gap-y-2 text-sm">
                <div className="flex items-center gap-2">
                  <span className="text-muted-foreground">평균 회차 예매율</span>
                  <span className="font-semibold text-foreground">
                    {averageSessionBookingRate.toFixed(1)}%
                  </span>
                </div>
                <span className="hidden text-border sm:inline">|</span>
                <div className="flex items-center gap-2">
                  <span className="text-muted-foreground">총 진행 회차</span>
                  <span className="font-semibold text-foreground">
                    {sessionCount.toLocaleString()}회차
                  </span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </section>

      <section className="mt-6">
        <SessionBookingChart sessions={sessions} />
      </section>

      <section className="mt-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">수익 배분</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col gap-4">
              <p className="text-sm text-muted-foreground">
                총 수익:{" "}
                <span className="font-semibold text-foreground">
                  {formatEthAmount(revenueSplit.totalRevenue)} ETH
                </span>
              </p>
              <SegmentedBar title="배분 비율" segments={revenueSegments} />
              <div className="flex flex-col gap-1.5 text-xs text-muted-foreground">
                {(revenueSplit.splits ?? []).map((split, index) => (
                  <div
                    key={`${split.displayName || split.role}-${split.amount}-${index}`}
                    className="flex justify-between gap-4"
                  >
                    <span>{split.displayName || split.role}</span>
                    <span className="font-medium text-foreground">
                      {formatEthAmount(split.amount)} ETH
                    </span>
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
