"use client"

import * as React from "react"
import { format } from "date-fns"
import { ko } from "date-fns/locale"
import { Calendar as CalendarIcon, Clock } from "lucide-react"
import { DateRange } from "react-day-picker"

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
}

export function DateTimeRangePicker({ startAt, endAt, onChange, placeholder = "기간 선택" }: DateTimeRangePickerProps) {
    const [isOpen, setIsOpen] = React.useState(false)
    const [date, setDate] = React.useState<DateRange | undefined>({
        from: startAt ? new Date(startAt) : undefined,
        to: endAt ? new Date(endAt) : undefined,
    })

    // Start Time
    const [startHour, setStartHour] = React.useState<string>(
        startAt ? new Date(startAt).getHours().toString().padStart(2, '0') : "12"
    )
    const [startMinute, setStartMinute] = React.useState<string>(
        startAt ? new Date(startAt).getMinutes().toString().padStart(2, '0') : "00"
    )

    // End Time
    const [endHour, setEndHour] = React.useState<string>(
        endAt ? new Date(endAt).getHours().toString().padStart(2, '0') : "13"
    )
    const [endMinute, setEndMinute] = React.useState<string>(
        endAt ? new Date(endAt).getMinutes().toString().padStart(2, '0') : "00"
    )

    // Sync props to state if they change externally
    React.useEffect(() => {
        if (startAt) {
            const d = new Date(startAt)
            if (!isNaN(d.getTime())) {
                setDate(prev => ({ ...prev, from: d }))
                setStartHour(d.getHours().toString().padStart(2, '0'))
                setStartMinute(d.getMinutes().toString().padStart(2, '0'))
            }
        }
    }, [startAt])

    React.useEffect(() => {
        if (endAt) {
            const d = new Date(endAt)
            if (!isNaN(d.getTime())) {
                setDate(prev => {
                    const fromDate = prev?.from ?? d;
                    return { from: fromDate, to: d }
                })
                setEndHour(d.getHours().toString().padStart(2, '0'))
                setEndMinute(d.getMinutes().toString().padStart(2, '0'))
            }
        }
    }, [endAt])

    const buildDateString = (d: Date, h: string, m: string) => {
        const newDate = new Date(d)
        newDate.setHours(parseInt(h), parseInt(m), 0, 0)
        const year = newDate.getFullYear()
        const month = String(newDate.getMonth() + 1).padStart(2, '0')
        const day = String(newDate.getDate()).padStart(2, '0')
        return `${year}-${month}-${day}T${h}:${m}:00`
    }

    const handleApply = () => {
        if (date?.from && date?.to) {
            const startStr = buildDateString(date.from, startHour, startMinute)
            const endStr = buildDateString(date.to, endHour, endMinute)
            onChange(startStr, endStr)
            setIsOpen(false)
        }
    }

    const hours = Array.from({ length: 24 }, (_, i) => i.toString().padStart(2, '0'))
    const minutes = Array.from({ length: 12 }, (_, i) => (i * 5).toString().padStart(2, '0'))

    return (
        <Popover open={isOpen} onOpenChange={setIsOpen}>
            <PopoverTrigger asChild>
                <Button
                    id="date"
                    variant={"outline"}
                    className={cn(
                        "w-full justify-start text-left font-normal h-9 px-3",
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
                />

                {/* Time Selection Row */}
                <div className="flex flex-col gap-3 p-3 border-t bg-muted/20">
                    <div className="grid grid-cols-2 gap-4">
                        {/* Start Time Selectors */}
                        <div className="flex flex-col gap-1.5">
                            <Label className="text-xs text-muted-foreground font-semibold">시작 시간</Label>
                            <div className="flex items-center gap-1">
                                <Select value={startHour} onValueChange={setStartHour}>
                                    <SelectTrigger className="w-[82px] h-8 text-xs bg-background">
                                        <SelectValue placeholder="시" />
                                    </SelectTrigger>
                                    <SelectContent className="max-h-56">
                                        {hours.map(h => <SelectItem key={h} value={h} className="text-xs">{h}시</SelectItem>)}
                                    </SelectContent>
                                </Select>
                                <span className="text-muted-foreground">:</span>
                                <Select value={startMinute} onValueChange={setStartMinute}>
                                    <SelectTrigger className="w-[82px] h-8 text-xs bg-background">
                                        <SelectValue placeholder="분" />
                                    </SelectTrigger>
                                    <SelectContent className="max-h-56">
                                        {minutes.map(m => <SelectItem key={m} value={m} className="text-xs">{m}분</SelectItem>)}
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        {/* End Time Selectors */}
                        <div className="flex flex-col gap-1.5">
                            <Label className="text-xs text-muted-foreground font-semibold">종료 시간</Label>
                            <div className="flex items-center gap-1">
                                <Select value={endHour} onValueChange={setEndHour}>
                                    <SelectTrigger className="w-[82px] h-8 text-xs bg-background">
                                        <SelectValue placeholder="시" />
                                    </SelectTrigger>
                                    <SelectContent className="max-h-56">
                                        {hours.map(h => <SelectItem key={h} value={h} className="text-xs">{h}시</SelectItem>)}
                                    </SelectContent>
                                </Select>
                                <span className="text-muted-foreground">:</span>
                                <Select value={endMinute} onValueChange={setEndMinute}>
                                    <SelectTrigger className="w-[82px] h-8 text-xs bg-background">
                                        <SelectValue placeholder="분" />
                                    </SelectTrigger>
                                    <SelectContent className="max-h-56">
                                        {minutes.map(m => <SelectItem key={m} value={m} className="text-xs">{m}분</SelectItem>)}
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>
                    </div>

                    <Button
                        className="w-full mt-2"
                        size="sm"
                        onClick={handleApply}
                        disabled={!date?.from || !date?.to}
                    >
                        기간 설정 적용하기
                    </Button>
                </div>
            </PopoverContent>
        </Popover>
    )
}
