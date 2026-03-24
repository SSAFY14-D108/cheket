interface Segment {
  label: string
  value: number
  color: string
}

interface SegmentedBarProps {
  segments: Segment[]
  title: string
}

export function SegmentedBar({ segments, title }: SegmentedBarProps) {
  const total = segments.reduce((sum, s) => sum + s.value, 0)
  const getSegmentKey = (segment: Segment, index: number) =>
    `${segment.label}-${segment.value}-${index}`

  if (!segments.length) {
    return (
      <div className="flex flex-col gap-3">
        <h4 className="text-sm font-medium text-black">{title}</h4>
        <div className="rounded-[1rem] border border-dashed border-black/10 bg-[#fafafa] px-4 py-5 text-center text-xs text-black/42">
          정산 비율 데이터가 없습니다.
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4">
      <h4 className="text-sm font-medium text-black">{title}</h4>
      <div className="flex h-10 w-full overflow-hidden rounded-full bg-black/[0.06]">
        {segments.map((segment, index) => {
          const percentage = total > 0 ? (segment.value / total) * 100 : 0
          return (
            <div
              key={getSegmentKey(segment, index)}
              className="flex items-center justify-center text-xs font-semibold tracking-wide transition-all"
              style={{
                width: `${percentage}%`,
                backgroundColor: segment.color,
                color: index >= 2 ? "#171717" : "#fff",
                minWidth: percentage > 0 ? "2rem" : 0,
              }}
            >
              {percentage >= 8 && `${Math.round(percentage)}%`}
            </div>
          )
        })}
      </div>
      <div className="flex flex-wrap gap-2">
        {segments.map((segment, index) => {
          const percentage = total > 0 ? (segment.value / total) * 100 : 0
          return (
            <div
              key={getSegmentKey(segment, index)}
              className="flex items-center gap-2 rounded-full bg-[#f7f7f8] px-3 py-1.5"
            >
              <span className="size-2.5 rounded-full" style={{ backgroundColor: segment.color }} />
              <span className="text-xs font-medium text-black/72">
                {segment.label} ({Math.round(percentage)}%)
              </span>
            </div>
          )
        })}
      </div>
    </div>
  )
}
