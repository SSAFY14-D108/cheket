"use client"

import Link from "next/link"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { BookingChart } from "@/components/dashboard/BookingChart"
import { SegmentedBar } from "@/components/dashboard/SegmentedBar"
import type { Event, DailyBooking, CompanyInfo } from "@/lib/mock-data"
import { ArrowLeft } from "lucide-react"

interface DashboardContentProps {
  event: Event
  dailyBookings: DailyBooking[]
  company: CompanyInfo
}

export function DashboardContent({ event, dailyBookings, company }: DashboardContentProps) {
  const bookingRate = ((event.soldSeats / event.totalSeats) * 100).toFixed(1)

  const admissionSegments = [
    { label: "입장", value: event.enteredCount, color: "var(--chart-1)" },
    { label: "미입장", value: event.notEnteredCount, color: "var(--chart-2)" },
    { label: "공석", value: event.emptyCount, color: "var(--chart-3)" },
  ]

  const revenueSegments = event.revenueDistribution.map((r, i) => ({
    label: r.label,
    value: r.percentage,
    color: ["var(--chart-1)", "var(--chart-2)", "var(--chart-3)"][i] ?? "var(--chart-3)",
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
        <h1 className="text-2xl font-bold text-foreground">{event.title}</h1>
      </div>

      {/* 판매 금액 + 잔액 */}
      <section className="mt-8">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">총 판매 현황</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex flex-col gap-1">
                <span className="text-sm text-muted-foreground">현재 보유 잔액</span>
                <span className="text-2xl font-bold text-foreground">
                  {company.balance} ETH
                </span>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={handleDeposit}
                  className="rounded-sm bg-primary px-5 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
                >
                  충전하기
                </button>
                <button
                  onClick={handleWithdraw}
                  className="rounded-sm border border-border bg-card px-5 py-2 text-sm font-medium text-foreground transition-colors hover:bg-secondary"
                >
                  출금하기
                </button>
              </div>
            </div>
          </CardContent>
        </Card>
      </section>

      {/* 꺾은선 그래프 */}
      <section className="mt-6">
        <BookingChart data={dailyBookings} />
      </section>

      {/* 예매율 + 바 차트들 */}
      <section className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* 예매율 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">예매율</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col gap-4">
              <div className="flex items-end gap-2">
                <span className="text-3xl font-bold text-foreground">{bookingRate}%</span>
                <span className="pb-1 text-sm text-muted-foreground">
                  ({event.soldSeats.toLocaleString()} / {event.totalSeats.toLocaleString()}석)
                </span>
              </div>
              <div className="h-3 w-full overflow-hidden rounded-full bg-secondary">
                <div
                  className="h-full rounded-full transition-all"
                  style={{
                    width: `${bookingRate}%`,
                    backgroundColor: "var(--chart-1)",
                  }}
                />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 입장률 + 수익 배분 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">비율 시각화</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col gap-6">
              <SegmentedBar
                title="공연 당일 입장률 현황"
                segments={admissionSegments}
              />
              <SegmentedBar
                title="수익 배분"
                segments={revenueSegments}
              />
            </div>
          </CardContent>
        </Card>
      </section>
    </main>
  )
}
