"use client"

import { Label } from "@/components/ui/label"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { DateTimeRangePicker } from "@/components/common/DateTimeRangePicker"
import type { HostShowVenueOption } from "@/lib/show-manage-api"

interface SettingsCardBasicProps {
    venueId: string
    venues: HostShowVenueOption[]
    isLoadingVenues: boolean
    venueLoadError: string
    playtime: string
    showStartAt: string
    showEndAt: string
    openAt: string
    closeAt: string
    onChangeVenueId: (id: string) => void
    onChangePlaytime: (value: string) => void
    onChangeShowRange: (start: string, end: string) => void
    onChangeReservationRange: (start: string, end: string) => void
}

export function SettingsCardBasic({
    venueId,
    venues,
    isLoadingVenues,
    venueLoadError,
    playtime,
    showStartAt,
    showEndAt,
    openAt,
    closeAt,
    onChangeVenueId,
    onChangePlaytime,
    onChangeShowRange,
    onChangeReservationRange,
}: SettingsCardBasicProps) {
    return (
        <Card>
            <CardHeader className="py-4">
                <CardTitle className="text-lg">공연 정보</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-4 pb-4">
                <div className="flex flex-col gap-1.5">
                    <Label className="text-xs">장소 선택</Label>
                    <select
                        className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
                        value={venueId}
                        onChange={(e) => onChangeVenueId(e.target.value)}
                        disabled={isLoadingVenues || Boolean(venueLoadError)}
                    >
                        <option value="" disabled>
                            {isLoadingVenues ? "공연장 목록 불러오는 중..." : "장소를 선택하세요"}
                        </option>
                        {venues.map((venue) => (
                            <option key={venue.venueId} value={venue.venueId}>
                                {venue.name} (최대 {venue.capacity}명)
                            </option>
                        ))}
                    </select>
                    {venueLoadError && (
                        <p className="text-xs text-destructive">{venueLoadError}</p>
                    )}
                </div>
                <div className="flex flex-col gap-1.5">
                    <Label className="text-xs">공연 시간 (분)</Label>
                    <input
                        type="number"
                        min="1"
                        placeholder="예: 120"
                        className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                        value={playtime}
                        onChange={(event) => onChangePlaytime(event.target.value)}
                    />
                </div>
                <Separator />

                <Label className="text-sm font-medium">공연 진행 전체 기간</Label>
                <DateTimeRangePicker
                    startAt={showStartAt}
                    endAt={showEndAt}
                    onChange={onChangeShowRange}
                    placeholder="공연 시작일 - 종료일 선택"
                />

                <Separator />

                <Label className="text-sm font-medium">예매 가능 기간</Label>
                <DateTimeRangePicker
                    startAt={openAt}
                    endAt={closeAt}
                    onChange={onChangeReservationRange}
                    placeholder="예매 시작일 - 마감일 선택"
                />
            </CardContent>
        </Card>
    )
}
