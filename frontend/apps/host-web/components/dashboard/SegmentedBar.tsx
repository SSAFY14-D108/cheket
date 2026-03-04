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

  return (
    <div className="flex flex-col gap-2">
      <h4 className="text-sm font-medium text-foreground">{title}</h4>
      {/* Bar */}
      <div className="flex h-6 w-full overflow-hidden rounded-sm">
        {segments.map((segment) => {
          const percentage = total > 0 ? (segment.value / total) * 100 : 0
          return (
            <div
              key={segment.label}
              className="flex items-center justify-center text-xs font-medium transition-all"
              style={{
                width: `${percentage}%`,
                backgroundColor: segment.color,
                color: "#fff",
                minWidth: percentage > 0 ? "2rem" : 0,
              }}
            >
              {percentage >= 10 && `${Math.round(percentage)}%`}
            </div>
          )
        })}
      </div>
      {/* Legend */}
      <div className="flex flex-wrap gap-4">
        {segments.map((segment) => {
          const percentage = total > 0 ? (segment.value / total) * 100 : 0
          return (
            <div key={segment.label} className="flex items-center gap-1.5">
              <span
                className="size-3 rounded-sm"
                style={{ backgroundColor: segment.color }}
              />
              <span className="text-xs text-muted-foreground">
                {segment.label} ({Math.round(percentage)}%)
              </span>
            </div>
          )
        })}
      </div>
    </div>
  )
}
