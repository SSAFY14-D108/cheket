"use client"

import { Card, CardContent } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import type { HostShowVenueOption } from "@/lib/show-manage-api"

interface SettingsCardBasicProps {
  venueId: string
  venues: HostShowVenueOption[]
  isLoadingVenues: boolean
  venueLoadError: string
  showErrors?: boolean
  onChangeVenueId: (id: string) => void
}

export function SettingsCardBasic({
  venueId,
  venues,
  isLoadingVenues,
  venueLoadError,
  showErrors = false,
  onChangeVenueId,
}: SettingsCardBasicProps) {
  return (
    <Card className="rounded-[2rem] border-black/8 bg-white py-0 shadow-sm">
      <div className="border-b border-black/8 px-6 pb-3 pt-5">
        <h2 className="text-xl font-semibold tracking-[-0.03em] text-black">공연장 선택</h2>
      </div>
      <CardContent className="px-5 pb-5 pt-4 sm:px-6 sm:pb-6 sm:pt-4">
        <div className="flex flex-col gap-2">
          <Label
            className={`text-[15px] font-bold ${!venueId && showErrors ? "text-destructive" : ""}`}
          >
            장소 선택 <span className="text-destructive">*</span>
          </Label>
          <select
            className={`flex h-12 w-full rounded-md border bg-background px-3 py-1 text-base font-medium shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 disabled:cursor-not-allowed disabled:opacity-50 ${
              !venueId && showErrors
                ? "border-destructive bg-destructive/5 focus-visible:ring-destructive"
                : "border-input focus-visible:ring-ring"
            }`}
            value={venueId}
            onChange={(event) => onChangeVenueId(event.target.value)}
            disabled={isLoadingVenues || Boolean(venueLoadError)}
          >
            <option value="" disabled>
              {isLoadingVenues ? "공연장 정보를 불러오는 중..." : "공연 장소를 선택해주세요"}
            </option>
            {venues.map((venue) => (
              <option key={venue.venueId} value={venue.venueId}>
                {venue.name} (최대 {venue.capacity}명)
              </option>
            ))}
          </select>
          {!venueId && showErrors ? (
            <p className="text-[11px] font-medium text-destructive">공연 장소를 선택해주세요.</p>
          ) : null}
          {venueLoadError ? <p className="text-xs text-destructive">{venueLoadError}</p> : null}
        </div>
      </CardContent>
    </Card>
  )
}
