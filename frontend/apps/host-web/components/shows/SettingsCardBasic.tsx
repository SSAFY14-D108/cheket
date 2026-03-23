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
    showStartAt: string
    showEndAt: string
    openAt: string
    closeAt: string
    showErrors?: boolean
    onChangeVenueId: (id: string) => void
    onChangeShowRange: (start: string, end: string) => void
    onChangeReservationRange: (start: string, end: string) => void
}

export function SettingsCardBasic({
    venueId,
    venues,
    isLoadingVenues,
    venueLoadError,
    showStartAt,
    showEndAt,
    openAt,
    closeAt,
    showErrors = false,
    onChangeVenueId,
    onChangeShowRange,
    onChangeReservationRange,
}: SettingsCardBasicProps) {
    return (
        <Card>
            <CardHeader className="py-5">
                <CardTitle className="text-xl font-bold">공연 정보</CardTitle>
                <p className="text-xs text-muted-foreground mt-0.5">공연이 진행되는 장소와 기간을 설정하세요</p>
            </CardHeader>
            <CardContent className="flex flex-col gap-4 pb-4">
                <div className="flex flex-col gap-2 mb-2">
                    <Label className={`text-[15px] font-bold ${!venueId && showErrors ? "text-destructive" : ""}`}>
                        장소 선택 <span className="text-destructive">*</span>
                    </Label>
                    <select
                        className={`flex h-12 w-full rounded-md border bg-background px-3 py-1 text-base font-medium shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 disabled:cursor-not-allowed disabled:opacity-50 ${
                            !venueId && showErrors 
                                ? "border-destructive bg-destructive/5 focus-visible:ring-destructive" 
                                : "border-input transition-colors focus-visible:ring-ring"
                        }`}
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
                    {!venueId && showErrors && (
                        <p className="text-[11px] font-medium text-destructive">공연 장소를 선택해 주세요.</p>
                    )}
                    {venueLoadError && (
                        <p className="text-xs text-destructive">{venueLoadError}</p>
                    )}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-2">
                    <div className="flex flex-col gap-2">
                        <Label className={`text-[14px] font-bold ${(!showStartAt || !showEndAt) && showErrors ? "text-destructive" : ""}`}>
                            공연 진행 전체 기간 <span className="text-destructive">*</span>
                        </Label>
                        <DateTimeRangePicker
                            startAt={showStartAt}
                            endAt={showEndAt}
                            onChange={onChangeShowRange}
                            placeholder="YYYY.MM.DD - YYYY.MM.DD"
                            className="h-[52px] text-base font-medium shadow-sm"
                        />
                        {(!showStartAt || !showEndAt) && showErrors && (
                            <p className="text-[11px] font-medium text-destructive">공연 기간을 선택해 주세요.</p>
                        )}
                    </div>

                    <div className="flex flex-col gap-2">
                        <Label className={`text-[14px] font-bold ${(!openAt || !closeAt) && showErrors ? "text-destructive" : ""}`}>
                            예매 가능 기간 <span className="text-destructive">*</span>
                        </Label>
                        <DateTimeRangePicker
                            startAt={openAt}
                            endAt={closeAt}
                            onChange={onChangeReservationRange}
                            placeholder="YYYY.MM.DD - YYYY.MM.DD"
                            className="h-[52px] text-base font-medium shadow-sm"
                        />
                        {(!openAt || !closeAt) && showErrors && (
                            <p className="text-[11px] font-medium text-destructive">예매 기간을 선택해 주세요.</p>
                        )}
                    </div>
                </div>
            </CardContent>
        </Card>
    )
}
