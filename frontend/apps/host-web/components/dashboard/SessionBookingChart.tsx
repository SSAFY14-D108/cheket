"use client"

import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts"
import type { DashboardSession } from "@/lib/dashboard-types"

interface SessionBookingChartProps {
  sessions: DashboardSession[]
}

interface CustomTooltipProps {
  active?: boolean
  payload?: Array<{ value: number; name: string }>
  label?: string
}

function formatSessionLabel(date: string) {
  return date ? date.slice(5) : "-"
}

function getSessionRate(reservedSeats: number, capacity: number) {
  return capacity > 0 ? (reservedSeats / capacity) * 100 : 0
}

function CustomTooltip({ active, payload, label }: CustomTooltipProps) {
  if (!active || !payload?.length) return null

  const reserved = payload.find((item) => item.name === "예매 좌석")?.value ?? 0
  const total = payload.find((item) => item.name === "전체 좌석")?.value ?? 0
  const rate = getSessionRate(reserved, total).toFixed(1)

  return (
    <div className="rounded-[1rem] border border-black/8 bg-white px-3 py-2 text-xs shadow-lg shadow-black/5">
      <p className="mb-1 font-semibold text-black">{label}</p>
      <p className="text-black/56">
        예매 좌석 <span className="font-medium text-black">{reserved.toLocaleString()}석</span>
      </p>
      <p className="text-black/56">
        전체 좌석 <span className="font-medium text-black">{total.toLocaleString()}석</span>
      </p>
      <p className="mt-1 font-semibold text-black">예매율 {rate}%</p>
    </div>
  )
}

export function SessionBookingChart({ sessions }: SessionBookingChartProps) {
  const data = sessions.map((session) => ({
    date: formatSessionLabel(session.date),
    "예매 좌석": session.reservedSeats,
    "전체 좌석": session.capacity,
  }))

  if (!data.length) {
    return (
      <section className="rounded-[1.6rem] border border-black/8 bg-white px-5 py-5">
        <div className="space-y-1">
          <p className="text-[0.72rem] font-medium uppercase tracking-[0.26em] text-black/36">
            Session trend
          </p>
          <h2 className="text-xl font-semibold tracking-[-0.04em] text-black">회차별 예매 추이</h2>
        </div>
        <div className="mt-6 flex h-72 items-center justify-center rounded-[1.2rem] border border-dashed border-black/10 bg-[#fafafa] text-sm text-black/45">
          등록된 회차 데이터가 없습니다.
        </div>
      </section>
    )
  }

  return (
    <section className="rounded-[1.6rem] border border-black/8 bg-white px-5 py-5">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div className="space-y-1">
          <p className="text-[0.72rem] font-medium uppercase tracking-[0.26em] text-black/36">
            Session trend
          </p>
          <h2 className="text-xl font-semibold tracking-[-0.04em] text-black">회차별 예매 추이</h2>
        </div>
        <div className="flex items-center gap-4 text-xs text-black/48">
          <span className="flex items-center gap-1.5">
            <span className="inline-block size-2.5 rounded-sm bg-black" />
            예매 좌석
          </span>
          <span className="flex items-center gap-1.5">
            <span className="inline-block size-2.5 rounded-sm bg-[#d4d4d8]" />
            전체 좌석
          </span>
        </div>
      </div>

      <div className="mt-6 h-72">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={data} margin={{ top: 10, right: 20, left: 0, bottom: 5 }}>
            <defs>
              <linearGradient id="totalGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#d4d4d8" stopOpacity={0.4} />
                <stop offset="95%" stopColor="#d4d4d8" stopOpacity={0.05} />
              </linearGradient>
              <linearGradient id="reservedGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#171717" stopOpacity={0.24} />
                <stop offset="95%" stopColor="#171717" stopOpacity={0.04} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(23,23,23,0.08)" />
            <XAxis
              dataKey="date"
              tick={{ fill: "rgba(23,23,23,0.45)", fontSize: 11 }}
              axisLine={false}
              tickLine={false}
            />
            <YAxis
              tick={{ fill: "rgba(23,23,23,0.45)", fontSize: 11 }}
              axisLine={false}
              tickLine={false}
              tickFormatter={(value: number) => value.toLocaleString()}
            />
            <Tooltip content={<CustomTooltip />} />
            <Area
              type="monotone"
              dataKey="전체 좌석"
              stroke="#c4c4cc"
              strokeWidth={1.5}
              strokeDasharray="4 3"
              fill="url(#totalGrad)"
              dot={false}
            />
            <Area
              type="monotone"
              dataKey="예매 좌석"
              stroke="#171717"
              strokeWidth={2.5}
              fill="url(#reservedGrad)"
              dot={{ fill: "#171717", r: 4, strokeWidth: 0 }}
              activeDot={{ r: 6, strokeWidth: 0 }}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      <div className="mt-5 overflow-hidden rounded-[1.2rem] border border-black/8">
        <div className="grid grid-cols-[88px_1fr_96px_112px] gap-3 bg-[#fafafa] px-4 py-2 text-[11px] font-medium text-black/44">
          <span>회차</span>
          <span>일자</span>
          <span className="text-right">예매율</span>
          <span className="text-right">예매 좌석</span>
        </div>
        {sessions.map((session, index) => {
          const sessionRate = getSessionRate(session.reservedSeats, session.capacity)

          return (
            <div
              key={session.sessionId}
              className="grid grid-cols-[88px_1fr_96px_112px] gap-3 border-t border-black/8 px-4 py-3 text-sm first:border-t-0"
            >
              <span className="font-medium text-black">회차 {index + 1}</span>
              <span className="text-black/72">{formatSessionLabel(session.date)}</span>
              <span className="text-right font-semibold text-black">
                {sessionRate.toFixed(1)}%
              </span>
              <span className="text-right text-black/52">
                {session.reservedSeats.toLocaleString()} / {session.capacity.toLocaleString()}석
              </span>
            </div>
          )
        })}
      </div>
    </section>
  )
}
