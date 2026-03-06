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
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import type { DashboardSession } from "@/lib/dashboard-types"

interface SessionBookingChartProps {
    sessions: DashboardSession[]
}

interface CustomTooltipProps {
    active?: boolean
    payload?: Array<{ value: number; name: string }>
    label?: string
}

function CustomTooltip({ active, payload, label }: CustomTooltipProps) {
    if (!active || !payload?.length) return null
    const reserved = payload.find((p) => p.name === "예매석")?.value ?? 0
    const total = payload.find((p) => p.name === "전체석")?.value ?? 0
    const rate = total > 0 ? ((reserved / total) * 100).toFixed(1) : "0"
    return (
        <div className="rounded-lg border border-border bg-card px-3 py-2 text-xs shadow-md">
            <p className="mb-1 font-semibold text-foreground">{label}</p>
            <p className="text-muted-foreground">예매석: <span className="font-medium text-foreground">{reserved.toLocaleString()}석</span></p>
            <p className="text-muted-foreground">전체석: <span className="font-medium text-foreground">{total.toLocaleString()}석</span></p>
            <p className="mt-1 font-semibold" style={{ color: "var(--chart-1)" }}>예매율 {rate}%</p>
        </div>
    )
}

export function SessionBookingChart({ sessions }: SessionBookingChartProps) {
    const data = sessions.map((s) => ({
        date: s.date.slice(5), // "YYYY-MM-DD" → "MM-DD"
        예매석: s.reservedSeats,
        전체석: s.capacity,
    }))

    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-base">회차별 예매 현황</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="h-72">
                    <ResponsiveContainer width="100%" height="100%">
                        <AreaChart data={data} margin={{ top: 10, right: 20, left: 0, bottom: 5 }}>
                            <defs>
                                <linearGradient id="totalGrad" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="5%" stopColor="var(--chart-3)" stopOpacity={0.25} />
                                    <stop offset="95%" stopColor="var(--chart-3)" stopOpacity={0} />
                                </linearGradient>
                                <linearGradient id="reservedGrad" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="5%" stopColor="var(--chart-1)" stopOpacity={0.4} />
                                    <stop offset="95%" stopColor="var(--chart-1)" stopOpacity={0.05} />
                                </linearGradient>
                            </defs>
                            <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                            <XAxis
                                dataKey="date"
                                tick={{ fill: "var(--muted-foreground)", fontSize: 11 }}
                                axisLine={false}
                                tickLine={false}
                            />
                            <YAxis
                                tick={{ fill: "var(--muted-foreground)", fontSize: 11 }}
                                axisLine={false}
                                tickLine={false}
                                tickFormatter={(v) => v.toLocaleString()}
                            />
                            <Tooltip content={<CustomTooltip />} />
                            {/* 전체석을 먼저 (배경) */}
                            <Area
                                type="monotone"
                                dataKey="전체석"
                                stroke="var(--chart-3)"
                                strokeWidth={1.5}
                                strokeDasharray="4 3"
                                fill="url(#totalGrad)"
                                dot={false}
                            />
                            {/* 예매석을 위에 */}
                            <Area
                                type="monotone"
                                dataKey="예매석"
                                stroke="var(--chart-1)"
                                strokeWidth={2.5}
                                fill="url(#reservedGrad)"
                                dot={{ fill: "var(--chart-1)", r: 4, strokeWidth: 0 }}
                                activeDot={{ r: 6, strokeWidth: 0 }}
                            />
                        </AreaChart>
                    </ResponsiveContainer>
                </div>
                {/* 범례 */}
                <div className="mt-3 flex items-center gap-5 pl-1 text-xs text-muted-foreground">
                    <span className="flex items-center gap-1.5">
                        <span className="inline-block size-2.5 rounded-sm" style={{ backgroundColor: "var(--chart-1)" }} />
                        예매석
                    </span>
                    <span className="flex items-center gap-1.5">
                        <span className="inline-block size-2.5 rounded-sm opacity-50" style={{ backgroundColor: "var(--chart-3)" }} />
                        전체석
                    </span>
                </div>
            </CardContent>
        </Card>
    )
}
