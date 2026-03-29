"use client"

import { useEffect, useMemo, useRef, useState } from "react"
import { Loader2, Minus, Plus, RotateCcw } from "lucide-react"
import { Button } from "@/components/ui/button"
import { fetchVenueSeats, type VenueSectionSeats } from "@/lib/show-manage-api"

interface SectionColorEntry {
  sectionId: string
  colorCode: string
}

interface VenueSeatMapProps {
  venueId: string
  sectionColors: SectionColorEntry[]
  className?: string
}

const SEAT_SIZE = 6
const SEAT_GAP = 2
const CELL = SEAT_SIZE + SEAT_GAP
const PADDING = 24
const DEFAULT_SEAT_COLOR = "#d4d4d8"
const SECTION_LABEL_FONT_SIZE = 10

export function VenueSeatMap({ venueId, sectionColors, className }: VenueSeatMapProps) {
  const [sections, setSections] = useState<VenueSectionSeats[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [zoom, setZoom] = useState(1)
  const [retryKey, setRetryKey] = useState(0)
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!venueId) {
      setSections([])
      return
    }

    let cancelled = false
    setLoading(true)
    setError(null)

    fetchVenueSeats(venueId)
      .then((data) => {
        if (!cancelled) {
          setSections(data ?? [])
          setZoom(1)
        }
      })
      .catch((err) => {
        if (!cancelled) {
          console.error("좌석 배치도 로딩 실패:", err)
          setError("좌석 배치도를 불러오지 못했습니다.")
          setSections([])
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [venueId, retryKey])

  const colorMap = useMemo(() => {
    const map = new Map<string, string>()
    for (const entry of sectionColors) {
      map.set(entry.sectionId, entry.colorCode)
    }
    return map
  }, [sectionColors])

  const { viewBox, seatElements, labelElements } = useMemo(() => {
    const allSeats = sections.flatMap((s) => s.seats)
    if (allSeats.length === 0) {
      return { viewBox: "0 0 100 100", seatElements: [], labelElements: [] }
    }

    let minRow = Infinity, maxRow = -Infinity
    let minCol = Infinity, maxCol = -Infinity

    for (const seat of allSeats) {
      if (seat.rowNum < minRow) minRow = seat.rowNum
      if (seat.rowNum > maxRow) maxRow = seat.rowNum
      if (seat.colNum < minCol) minCol = seat.colNum
      if (seat.colNum > maxCol) maxCol = seat.colNum
    }

    const width = (maxCol - minCol + 1) * CELL + PADDING * 2
    const height = (maxRow - minRow + 1) * CELL + PADDING * 2

    const seats: Array<{
      key: string
      x: number
      y: number
      color: string
      seatNo: string
      sectionName: string
    }> = []

    const labels: Array<{
      key: string
      x: number
      y: number
      name: string
      color: string
    }> = []

    for (const section of sections) {
      const sectionIdStr = section.sectionId.toString()
      const sectionColor = colorMap.get(sectionIdStr) || DEFAULT_SEAT_COLOR

      let sumX = 0
      let sumY = 0

      for (const seat of section.seats) {
        const x = (seat.colNum - minCol) * CELL + PADDING
        const y = (seat.rowNum - minRow) * CELL + PADDING

        sumX += x
        sumY += y

        seats.push({
          key: `seat-${seat.seatId}`,
          x,
          y,
          color: sectionColor,
          seatNo: seat.seatNo,
          sectionName: section.sectionName,
        })
      }

      if (section.seats.length > 0) {
        labels.push({
          key: `label-${section.sectionId}`,
          x: sumX / section.seats.length + SEAT_SIZE / 2,
          y: sumY / section.seats.length + SEAT_SIZE / 2,
          name: section.sectionName,
          color: sectionColor,
        })
      }
    }

    return {
      viewBox: `0 0 ${width} ${height}`,
      seatElements: seats,
      labelElements: labels,
    }
  }, [sections, colorMap])

  const handleZoomIn = () => setZoom((z) => Math.min(z + 0.25, 3))
  const handleZoomOut = () => setZoom((z) => Math.max(z - 0.25, 0.5))
  const handleZoomReset = () => setZoom(1)

  if (loading) {
    return (
      <div className={`flex items-center justify-center rounded-xl border bg-muted/20 p-8 ${className ?? ""}`}>
        <Loader2 className="size-5 animate-spin text-muted-foreground" />
        <span className="ml-2 text-xs text-muted-foreground">좌석 배치도 로딩중...</span>
      </div>
    )
  }

  if (error) {
    return (
      <div className={`flex flex-col items-center justify-center gap-2 rounded-xl border bg-muted/20 p-6 ${className ?? ""}`}>
        <p className="text-xs text-muted-foreground">{error}</p>
        <Button
          type="button"
          variant="outline"
          size="sm"
          className="h-7 text-xs"
          onClick={() => setRetryKey((k) => k + 1)}
        >
          다시 시도
        </Button>
      </div>
    )
  }

  if (sections.length === 0) {
    return (
      <div className={`flex items-center justify-center rounded-xl border border-dashed bg-muted/10 p-6 ${className ?? ""}`}>
        <p className="text-xs text-muted-foreground">좌석 배치 정보가 없습니다.</p>
      </div>
    )
  }

  return (
    <div className={className}>
      <div className="mb-2 flex items-center justify-end gap-1">
        <Button type="button" variant="ghost" size="icon" className="h-6 w-6" onClick={handleZoomOut} title="축소">
          <Minus className="size-3" />
        </Button>
        <span className="w-10 text-center text-[10px] text-muted-foreground">{Math.round(zoom * 100)}%</span>
        <Button type="button" variant="ghost" size="icon" className="h-6 w-6" onClick={handleZoomIn} title="확대">
          <Plus className="size-3" />
        </Button>
        <Button type="button" variant="ghost" size="icon" className="h-6 w-6" onClick={handleZoomReset} title="초기화">
          <RotateCcw className="size-3" />
        </Button>
      </div>

      <div
        ref={containerRef}
        className="overflow-auto rounded-xl border border-border/50 bg-slate-50"
        style={{ maxHeight: 240 }}
      >
        <svg
          viewBox={viewBox}
          style={{
            width: `${zoom * 100}%`,
            minWidth: "100%",
            height: "auto",
            display: "block",
          }}
        >
          {seatElements.map((seat) => (
            <rect
              key={seat.key}
              x={seat.x}
              y={seat.y}
              width={SEAT_SIZE}
              height={SEAT_SIZE}
              rx={1.5}
              fill={seat.color}
              stroke={seat.color === DEFAULT_SEAT_COLOR ? "#a1a1aa" : seat.color}
              strokeWidth={0.5}
              strokeOpacity={0.6}
            >
              <title>{`${seat.sectionName} - ${seat.seatNo}`}</title>
            </rect>
          ))}

          {labelElements.map((label) => (
            <g key={label.key}>
              <rect
                x={label.x - 20}
                y={label.y - SECTION_LABEL_FONT_SIZE / 2 - 4}
                width={40}
                height={SECTION_LABEL_FONT_SIZE + 6}
                rx={4}
                fill="white"
                fillOpacity={0.85}
                stroke={label.color === DEFAULT_SEAT_COLOR ? "#71717a" : label.color}
                strokeWidth={0.8}
              />
              <text
                x={label.x}
                y={label.y + 1}
                textAnchor="middle"
                dominantBaseline="central"
                fontSize={SECTION_LABEL_FONT_SIZE}
                fontWeight={600}
                fill={label.color === DEFAULT_SEAT_COLOR ? "#3f3f46" : label.color}
              >
                {label.name}
              </text>
            </g>
          ))}
        </svg>
      </div>
    </div>
  )
}
