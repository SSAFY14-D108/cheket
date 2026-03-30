"use client"

import * as React from "react"
import { format } from "date-fns"
import { ko } from "date-fns/locale"
import { Calendar as CalendarIcon, Clock } from "lucide-react"

import { cn } from "@/lib/utils"
import { formatDateTimeWithWeekday } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

interface DateTimePickerProps {
  value?: string
  onChange: (value: string) => void
  placeholder?: string
  minDate?: Date
  maxDate?: Date
}

function isSameCalendarDay(left: Date, right: Date) {
  return (
    left.getFullYear() === right.getFullYear() &&
    left.getMonth() === right.getMonth() &&
    left.getDate() === right.getDate()
  )
}

export function DateTimePicker({
  value,
  onChange,
  placeholder = "YYYY.MM.DD HH:mm",
  minDate,
  maxDate,
}: DateTimePickerProps) {
  const [date, setDate] = React.useState<Date | undefined>(
    value ? new Date(value) : undefined
  )
  const [hour, setHour] = React.useState<string>(
    value ? new Date(value).getHours().toString().padStart(2, "0") : "12"
  )
  const [minute, setMinute] = React.useState<string>(
    value ? new Date(value).getMinutes().toString().padStart(2, "0") : "00"
  )

  React.useEffect(() => {
    if (!value) {
      setDate(undefined)
      return
    }

    const parsedDate = new Date(value)
    if (!Number.isNaN(parsedDate.getTime())) {
      setDate(parsedDate)
      setHour(parsedDate.getHours().toString().padStart(2, "0"))
      setMinute(parsedDate.getMinutes().toString().padStart(2, "0"))
    }
  }, [value])

  const normalizedMinDate = React.useMemo(() => {
    if (!minDate) {
      return undefined
    }

    return new Date(minDate)
  }, [minDate])

  const normalizedMaxDate = React.useMemo(() => {
    if (!maxDate) {
      return undefined
    }

    return new Date(maxDate)
  }, [maxDate])

  const updateValue = React.useCallback(
    (selectedDate: Date, selectedHour: string, selectedMinute: string) => {
      const nextDate = new Date(selectedDate)
      nextDate.setHours(parseInt(selectedHour, 10), parseInt(selectedMinute, 10), 0, 0)

      const year = nextDate.getFullYear()
      const month = String(nextDate.getMonth() + 1).padStart(2, "0")
      const day = String(nextDate.getDate()).padStart(2, "0")

      onChange(`${year}-${month}-${day}T${selectedHour}:${selectedMinute}`)
    },
    [onChange]
  )

  const isHourDisabled = React.useCallback(
    (hourValue: string) => {
      if (!date) {
        return false
      }

      const numericHour = parseInt(hourValue, 10)

      if (normalizedMinDate && isSameCalendarDay(date, normalizedMinDate)) {
        if (numericHour < normalizedMinDate.getHours()) {
          return true
        }
      }

      if (normalizedMaxDate && isSameCalendarDay(date, normalizedMaxDate)) {
        if (numericHour > normalizedMaxDate.getHours()) {
          return true
        }
      }

      return false
    },
    [date, normalizedMaxDate, normalizedMinDate]
  )

  const isMinuteDisabled = React.useCallback(
    (minuteValue: string) => {
      if (!date) {
        return false
      }

      const numericHour = parseInt(hour, 10)
      const numericMinute = parseInt(minuteValue, 10)

      if (
        normalizedMinDate &&
        isSameCalendarDay(date, normalizedMinDate) &&
        numericHour === normalizedMinDate.getHours() &&
        numericMinute < normalizedMinDate.getMinutes()
      ) {
        return true
      }

      if (
        normalizedMaxDate &&
        isSameCalendarDay(date, normalizedMaxDate) &&
        numericHour === normalizedMaxDate.getHours() &&
        numericMinute > normalizedMaxDate.getMinutes()
      ) {
        return true
      }

      return false
    },
    [date, hour, normalizedMaxDate, normalizedMinDate]
  )

  const getFirstAvailableHour = React.useCallback(() => {
    for (let i = 0; i < 24; i += 1) {
      const candidate = i.toString().padStart(2, "0")
      if (!isHourDisabled(candidate)) {
        return candidate
      }
    }

    return "00"
  }, [isHourDisabled])

  const getFirstAvailableMinute = React.useCallback(
    (baseHour: string) => {
      for (let i = 0; i < 12; i += 1) {
        const candidate = String(i * 5).padStart(2, "0")

        const numericHour = parseInt(baseHour, 10)
        const numericMinute = parseInt(candidate, 10)

        if (
          normalizedMinDate &&
          date &&
          isSameCalendarDay(date, normalizedMinDate) &&
          numericHour === normalizedMinDate.getHours() &&
          numericMinute < normalizedMinDate.getMinutes()
        ) {
          continue
        }

        if (
          normalizedMaxDate &&
          date &&
          isSameCalendarDay(date, normalizedMaxDate) &&
          numericHour === normalizedMaxDate.getHours() &&
          numericMinute > normalizedMaxDate.getMinutes()
        ) {
          continue
        }

        return candidate
      }

      return "00"
    },
    [date, normalizedMaxDate, normalizedMinDate]
  )

  const handleDateSelect = (selectedDate: Date | undefined) => {
    setDate(selectedDate)

    if (!selectedDate) {
      return
    }

    const nextHour = isHourDisabled(hour) ? getFirstAvailableHour() : hour
    const nextMinute = isMinuteDisabled(minute) ? getFirstAvailableMinute(nextHour) : minute

    setHour(nextHour)
    setMinute(nextMinute)
    updateValue(selectedDate, nextHour, nextMinute)
  }

  const handleTimeChange = (type: "hour" | "minute", nextValue: string) => {
    let nextHour = hour
    let nextMinute = minute

    if (type === "hour") {
      nextHour = nextValue
      setHour(nextValue)

      if (isMinuteDisabled(nextMinute)) {
        nextMinute = getFirstAvailableMinute(nextHour)
        setMinute(nextMinute)
      }
    } else {
      nextMinute = nextValue
      setMinute(nextValue)
    }

    if (date) {
      updateValue(date, nextHour, nextMinute)
    }
  }

  const hours = Array.from({ length: 24 }, (_, i) => i.toString().padStart(2, "0"))
  const minutes = Array.from({ length: 12 }, (_, i) => (i * 5).toString().padStart(2, "0"))

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          className={cn(
            "h-9 w-full justify-start px-3 text-left font-normal",
            !date && "text-muted-foreground"
          )}
        >
          <CalendarIcon className="mr-2 h-4 w-4 shrink-0" />
          <span className="truncate">
            {date && value
              ? formatDateTimeWithWeekday(value)
              : placeholder}
          </span>
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0" align="start">
        <Calendar
          mode="single"
          selected={date}
          onSelect={handleDateSelect}
          initialFocus
          locale={ko}
          disabled={(day) => {
            const normalizedDay = new Date(day)
            normalizedDay.setHours(12, 0, 0, 0)

            if (normalizedMinDate) {
              const minDay = new Date(normalizedMinDate)
              minDay.setHours(0, 0, 0, 0)
              if (normalizedDay < minDay) {
                return true
              }
            }

            if (normalizedMaxDate) {
              const maxDay = new Date(normalizedMaxDate)
              maxDay.setHours(23, 59, 59, 999)
              if (normalizedDay > maxDay) {
                return true
              }
            }

            return false
          }}
        />

        <div className="flex items-center justify-center gap-2 border-t bg-muted/20 p-3">
          <Clock className="mr-1 h-4 w-4 shrink-0 text-muted-foreground" />

          <Select value={hour} onValueChange={(nextValue) => handleTimeChange("hour", nextValue)}>
            <SelectTrigger className="h-8 w-[70px] bg-background text-xs">
              <SelectValue placeholder="시" />
            </SelectTrigger>
            <SelectContent className="max-h-56">
              {hours.map((hourValue) => (
                <SelectItem
                  key={hourValue}
                  value={hourValue}
                  className="text-xs"
                  disabled={isHourDisabled(hourValue)}
                >
                  {hourValue}시
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <span className="text-muted-foreground">:</span>

          <Select value={minute} onValueChange={(nextValue) => handleTimeChange("minute", nextValue)}>
            <SelectTrigger className="h-8 w-[70px] bg-background text-xs">
              <SelectValue placeholder="분" />
            </SelectTrigger>
            <SelectContent className="max-h-56">
              {minutes.map((minuteValue) => (
                <SelectItem
                  key={minuteValue}
                  value={minuteValue}
                  className="text-xs"
                  disabled={isMinuteDisabled(minuteValue)}
                >
                  {minuteValue}분
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </PopoverContent>
    </Popover>
  )
}
