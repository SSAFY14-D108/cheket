"use client"

import Link from "next/link"
import { ArrowLeft } from "lucide-react"
import { SessionBookingChart } from "@/components/dashboard/SessionBookingChart"
import { SegmentedBar } from "@/components/dashboard/SegmentedBar"
import type { DashboardData } from "@/lib/dashboard-types"

interface DashboardContentProps {
  data: DashboardData
}

const REVENUE_SPLIT_COLORS = ["#111111", "#4b5563", "#9ca3af", "#d1d5db", "#e5e7eb"]

function formatEthAmount(value: number) {
  return value.toLocaleString(undefined, {
    minimumFractionDigits: 0,
    maximumFractionDigits: 4,
  })
}

function formatSharePercent(rateBps: number) {
  return `${(rateBps / 100).toFixed(rateBps % 100 === 0 ? 0 : 1)}%`
}

export function DashboardContent({ data }: DashboardContentProps) {
  if (!data) {
    return (
      <main className="mx-auto max-w-7xl px-6 py-10 lg:px-10 lg:py-12">
        <div className="flex min-h-[40vh] items-center justify-center rounded-[2rem] border border-dashed border-black/10 bg-white text-sm text-black/45">
          대시보드 데이터를 불러오지 못했습니다.
        </div>
      </main>
    )
  }

  const { totalSales, revenueSplit, reservations } = data
  const sessions = reservations.sessions ?? []
  const partialErrors = data.partialErrors ?? []
  const derivedReservedSeats = sessions.reduce(
    (sum, session) => sum + (Number.isFinite(session.reservedSeats) ? session.reservedSeats : 0),
    0
  )
  const derivedCapacity = sessions.reduce(
    (sum, session) => sum + (Number.isFinite(session.capacity) ? session.capacity : 0),
    0
  )
  const derivedBookingRate =
    derivedCapacity > 0 ? (derivedReservedSeats / derivedCapacity) * 100 : 0

  const sortedSessionDates = sessions
    .map((session) => session.date)
    .filter(Boolean)
    .sort((left, right) => left.localeCompare(right))

  const periodLabel =
    sortedSessionDates.length === 0
      ? "일정 없음"
      : sortedSessionDates.length === 1
        ? sortedSessionDates[0]
        : `${sortedSessionDates[0]} - ${sortedSessionDates[sortedSessionDates.length - 1]}`

  const revenueSegments = (revenueSplit.splits ?? []).map((split, index) => ({
    label: split.displayName || split.role,
    value: split.rateBps / 100,
    color: REVENUE_SPLIT_COLORS[index % REVENUE_SPLIT_COLORS.length],
  }))

  return (
    <main className="mx-auto max-w-7xl px-6 py-8 lg:px-10 lg:py-10">
      <section className="overflow-hidden rounded-[2rem] border border-black/8 bg-white">
        <div className="border-b border-black/6 px-6 py-5 lg:px-8">
          <div className="flex items-start gap-4">
            <Link
              href="/mypage"
              className="mt-1 flex size-11 shrink-0 items-center justify-center rounded-full border border-black/10 bg-white text-black transition-colors hover:bg-black/[0.03]"
              aria-label="마이페이지로 돌아가기"
            >
              <ArrowLeft className="size-4" />
            </Link>

            <div className="space-y-3">
              <div className="space-y-1">
                <p className="text-[0.72rem] font-medium uppercase tracking-[0.28em] text-black/38">
                  Show dashboard
                </p>
                <h1 className="text-[clamp(2rem,4vw,3.2rem)] font-semibold tracking-[-0.06em] text-black">
                  {reservations.title}
                </h1>
              </div>

              <div className="flex flex-wrap gap-2 text-sm text-black/55">
                <span className="rounded-full bg-black/[0.04] px-3 py-1.5">
                  {reservations.venue || "장소 정보 없음"}
                </span>
                <span className="rounded-full bg-black/[0.04] px-3 py-1.5">{periodLabel}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="grid gap-6 px-6 py-6 lg:grid-cols-[minmax(0,1.5fr)_minmax(300px,0.85fr)] lg:px-8 lg:py-8">
          <div className="space-y-6">
            {partialErrors.length > 0 ? (
              <div className="rounded-[1.4rem] border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
                일부 지표는 불러오지 못했습니다. 현재 확인 가능한 데이터만 표시합니다.
              </div>
            ) : null}

            <section className="rounded-[1.6rem] border border-black/8 bg-white px-5 py-5">
              <div className="space-y-1">
                <p className="text-[0.72rem] font-medium uppercase tracking-[0.24em] text-black/34">
                  Total sales
                </p>
                <h2 className="text-xl font-semibold tracking-[-0.04em] text-black">총 판매 금액</h2>
              </div>
              <div className="mt-5 flex items-end justify-between gap-4">
                <p className="text-[clamp(2rem,4vw,3rem)] font-semibold tracking-[-0.06em] text-black">
                  {totalSales.totalPrimarySales.toLocaleString()}원
                </p>
                <p className="pb-1 text-sm text-black/42">1차 판매 기준</p>
              </div>
            </section>

            <SessionBookingChart sessions={sessions} />
          </div>

          <aside className="space-y-6">
            <section className="rounded-[1.6rem] border border-black/8 bg-white px-5 py-5">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-[0.72rem] font-medium uppercase tracking-[0.26em] text-black/36">
                    Booking status
                  </p>
                  <h2 className="mt-2 text-xl font-semibold tracking-[-0.04em] text-black">
                    현재 예매 현황
                  </h2>
                </div>
                <span className="rounded-full bg-[#f4f4f5] px-3 py-1.5 text-xs font-medium text-black/56">
                  Live
                </span>
              </div>

              <div className="mt-6 space-y-4">
                <div>
                  <div className="flex items-end justify-between gap-3">
                    <p className="text-4xl font-semibold tracking-[-0.06em] text-black">
                      {derivedBookingRate.toFixed(1)}%
                    </p>
                    <p className="pb-1 text-sm text-black/46">
                      {derivedReservedSeats.toLocaleString()} / {derivedCapacity.toLocaleString()}석
                    </p>
                  </div>
                  <div className="mt-4 h-2.5 overflow-hidden rounded-full bg-black/[0.06]">
                    <div
                      className="h-full rounded-full bg-black transition-all"
                      style={{ width: `${Math.min(Math.max(derivedBookingRate, 0), 100)}%` }}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3 border-t border-black/8 pt-4 text-sm">
                  <div className="space-y-1">
                    <p className="text-black/42">예약 좌석</p>
                    <p className="font-semibold text-black">
                      {derivedReservedSeats.toLocaleString()}석
                    </p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-black/42">전체 좌석</p>
                    <p className="font-semibold text-black">{derivedCapacity.toLocaleString()}석</p>
                  </div>
                </div>
              </div>
            </section>

            <section className="rounded-[1.6rem] border border-black/8 bg-white px-5 py-5">
              <div className="space-y-1">
                <p className="text-[0.72rem] font-medium uppercase tracking-[0.26em] text-black/36">
                  Revenue split
                </p>
                <h2 className="text-xl font-semibold tracking-[-0.04em] text-black">정산 비율</h2>
              </div>

              <p className="mt-4 text-sm text-black/48">
                총 정산 금액{" "}
                <span className="font-semibold text-black">
                  {formatEthAmount(revenueSplit.totalRevenue)} SSF
                </span>
              </p>

              <div className="mt-5">
                <SegmentedBar title="참여자별 배분" segments={revenueSegments} />
              </div>

              <div className="mt-5 max-h-80 space-y-2 overflow-y-auto pr-1">
                {(revenueSplit.splits ?? []).map((split, index) => (
                  <div
                    key={`${split.displayName || split.role}-${split.amount}-${index}`}
                    className="flex items-center justify-between gap-4 rounded-[1rem] bg-[#f7f7f8] px-4 py-3 text-sm"
                  >
                    <div className="flex items-center gap-2.5">
                      <span
                        className="size-2.5 rounded-full"
                        style={{
                          backgroundColor:
                            REVENUE_SPLIT_COLORS[index % REVENUE_SPLIT_COLORS.length],
                        }}
                      />
                      <div className="min-w-0">
                        <p className="truncate text-black/72">
                          {split.displayName || split.role}
                        </p>
                        <p className="mt-0.5 text-xs text-black/42">
                          {formatEthAmount(split.amount)} SSF
                        </p>
                      </div>
                    </div>
                    <span className="shrink-0 font-semibold text-black">
                      {formatSharePercent(split.rateBps)}
                    </span>
                  </div>
                ))}
              </div>
            </section>
          </aside>
        </div>
      </section>
    </main>
  )
}
