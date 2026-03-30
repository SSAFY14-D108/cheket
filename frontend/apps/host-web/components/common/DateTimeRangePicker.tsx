"use client"

import * as React from "react"
import { format } from "date-fns"
import { ko } from "date-fns/locale"
import { Calendar as CalendarIcon } from "lucide-react"
import type { DateRange } from "react-day-picker"

import { cn } from "@/lib/utils"
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
import { Label } from "@/components/ui/label"

interface DateTimeRangePickerProps {
    startAt?: string
    endAt?: string
    onChange: (startAt: string, endAt: string) => void
    placeholder?: string
    className?: string
    minDate?: Date
}

function toTwoDigits(value: number) {
    return value.toString().padStart(2, "0")
}

function toDateString(date: Date, hour: string, minute: string) {
    const year = date.getFullYear()
    const month = toTwoDigits(date.getMonth() + 1)
    const day = toTwoDigits(date.getDate())
    return `${year}-${month}-${day}T${hour}:${minute}:00`
}

export function DateTimeRangePicker({
    startAt,
    endAt,
    onChange,
    placeholder = "기간 선택",
    className,
    minDate,
}: DateTimeRangePickerProps) {
    const [isOpen, setIsOpen] = React.useState(false)
    const [date, setDate] = React.useState<DateRange | undefined>({
        from: startAt ? new Date(startAt) : undefined,
        to: endAt ? new Date(endAt) : undefined,
    })
    const [startHour, setStartHour] = React.useState(
        startAt ? toTwoDigits(new Date(startAt).getHours()) : "12"
    )
    const [startMinute, setStartMinute] = React.useState(
        startAt ? toTwoDigits(new Date(startAt).getMinutes()) : "00"
    )
    const [endHour, setEndHour] = React.useState(
        endAt ? toTwoDigits(new Date(endAt).getHours()) : "13"
    )
    const [endMinute, setEndMinute] = React.useState(
        endAt ? toTwoDigits(new Date(endAt).getMinutes()) : "00"
    )

    React.useEffect(() => {
        if (!startAt) {
            return
        }

        const nextDate = new Date(startAt)
        if (Number.isNaN(nextDate.getTime())) {
            return
        }

        setDate((previous) => ({ ...previous, from: nextDate }))
        setStartHour(toTwoDigits(nextDate.getHours()))
        setStartMinute(toTwoDigits(nextDate.getMinutes()))
    }, [startAt])

    React.useEffect(() => {
        if (!endAt) {
            return
        }

        const nextDate = new Date(endAt)
        if (Number.isNaN(nextDate.getTime())) {
            return
        }

        setDate((previous) => ({
            from: previous?.from ?? nextDate,
            to: nextDate,
        }))
        setEndHour(toTwoDigits(nextDate.getHours()))
        setEndMinute(toTwoDigits(nextDate.getMinutes()))
    }, [endAt])

    const hours = Array.from({ length: 24 }, (_, index) => toTwoDigits(index))
    const minutes = Array.from({ length: 12 }, (_, index) => toTwoDigits(index * 5))

    const normalizedMinDate = React.useMemo(() => {
        if (!minDate) {
            return undefined
        }

        const nextMinDate = new Date(minDate)
        nextMinDate.setHours(0, 0, 0, 0)
        return nextMinDate
    }, [minDate])

    const handleApply = () => {
        if (!date?.from || !date?.to) {
            return
        }

        onChange(
            toDateString(date.from, startHour, startMinute),
            toDateString(date.to, endHour, endMinute)
        )
        setIsOpen(false)
    }

    return (
        <Popover open={isOpen} onOpenChange={setIsOpen}>
            <PopoverTrigger asChild>
                <Button
                    id="date-range"
                    variant="outline"
                    className={cn(
                        "h-9 w-full justify-start px-3 text-left font-normal",
                        className,
                        !date?.from && "text-muted-foreground"
                    )}
                >
                    <CalendarIcon className="mr-2 h-4 w-4 flex-shrink-0" />
                    <span className="truncate">
                        {date?.from ? (
                            date.to ? (
                                <>
                                    {format(date.from, "yyyy-MM-dd", { locale: ko })} {startHour}:{startMinute} -{" "}
                                    {format(date.to, "yyyy-MM-dd", { locale: ko })} {endHour}:{endMinute}
                                </>
                            ) : (
                                format(date.from, "yyyy-MM-dd", { locale: ko })
                            )
                        ) : (
                            placeholder
                        )}
                    </span>
                </Button>
            </PopoverTrigger>

            <PopoverContent className="w-auto p-0" align="start">
                <Calendar
                    initialFocus
                    mode="range"
                    defaultMonth={date?.from}
                    selected={date}
                    onSelect={setDate}
                    numberOfMonths={2}
                    locale={ko}
                    disabled={(day) => {
                        if (!normalizedMinDate) {
                            return false
                        }

                        const normalizedDay = new Date(day)
                        normalizedDay.setHours(0, 0, 0, 0)
                        return normalizedDay < normalizedMinDate
                    }}
                />

                <div className="flex flex-col gap-3 border-t bg-muted/20 p-3">
                    <div className="grid grid-cols-2 gap-4">
                        <div className="flex flex-col gap-1.5">
                            <Label className="text-xs font-semibold text-muted-foreground">시작 시간</Label>
                            <div className="flex items-center gap-1">
                                <Select value={startHour} onValueChange={setStartHour}>
                                    <SelectTrigger className="h-8 w-[82px] bg-background text-xs">
                                        <SelectValue placeholder="시" />
                                    </SelectTrigger>
                                    <SelectContent className="max-h-56">
                                        {hours.map((hour) => (
                                            <SelectItem key={hour} value={hour} className="text-xs">
                                                {hour}시
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                                <span className="text-muted-foreground">:</span>
                                <Select value={startMinute} onValueChange={setStartMinute}>
                                    <SelectTrigger className="h-8 w-[82px] bg-background text-xs">
                                        <SelectValue placeholder="분" />
                                    </SelectTrigger>
                                    <SelectContent className="max-h-56">
                                        {minutes.map((minute) => (
                                            <SelectItem key={minute} value={minute} className="text-xs">
                                                {minute}분
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        <div className="flex flex-col gap-1.5">
                            <Label className="text-xs font-semibold text-muted-foreground">종료 시간</Label>
                            <div className="flex items-center gap-1">
                                <Select value={endHour} onValueChange={setEndHour}>
                                    <SelectTrigger className="h-8 w-[82px] bg-background text-xs">
                                        <SelectValue placeholder="시" />
                                    </SelectTrigger>
                                    <SelectContent className="max-h-56">
                                        {hours.map((hour) => (
                                            <SelectItem key={hour} value={hour} className="text-xs">
                                                {hour}시
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                                <span className="text-muted-foreground">:</span>
                                <Select value={endMinute} onValueChange={setEndMinute}>
                                    <SelectTrigger className="h-8 w-[82px] bg-background text-xs">
                                        <SelectValue placeholder="분" />
                                    </SelectTrigger>
                                    <SelectContent className="max-h-56">
                                        {minutes.map((minute) => (
                                            <SelectItem key={minute} value={minute} className="text-xs">
                                                {minute}분
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>
                    </div>

                    <Button className="mt-2 w-full" size="sm" onClick={handleApply} disabled={!date?.from || !date?.to}>
                        기간 적용
                    </Button>
                </div>
            </PopoverContent>
        </Popover>
    )
}
