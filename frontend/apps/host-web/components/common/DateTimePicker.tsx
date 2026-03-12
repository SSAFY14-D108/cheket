"use client"

import * as React from "react"
import { format } from "date-fns"
import { Calendar as CalendarIcon, Clock } from "lucide-react"

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

interface DateTimePickerProps {
    value?: string
    onChange: (value: string) => void
    placeholder?: string
    minDate?: Date
    maxDate?: Date
}

export function DateTimePicker({
    value,
    onChange,
    placeholder = "날짜/시간 선택",
    minDate,
    maxDate,
}: DateTimePickerProps) {
    const [date, setDate] = React.useState<Date | undefined>(
        value ? new Date(value) : undefined
    )
    const [hour, setHour] = React.useState<string>(
        value ? new Date(value).getHours().toString().padStart(2, '0') : "12"
    )
    const [minute, setMinute] = React.useState<string>(
        value ? new Date(value).getMinutes().toString().padStart(2, '0') : "00"
    )

    React.useEffect(() => {
        if (value) {
            const parsedDate = new Date(value)
            if (!isNaN(parsedDate.getTime())) {
                setDate(parsedDate)
                setHour(parsedDate.getHours().toString().padStart(2, '0'))
                setMinute(parsedDate.getMinutes().toString().padStart(2, '0'))
            }
        }
    }, [value])

    const handleDateSelect = (selectedDate: Date | undefined) => {
        setDate(selectedDate)
        if (selectedDate) {
            updateValue(selectedDate, hour, minute)
        }
    }

    const handleTimeChange = (type: 'hour' | 'minute', val: string) => {
        let newHour = hour
        let newMinute = minute
        if (type === 'hour') {
            newHour = val
            setHour(val)
        } else {
            newMinute = val
            setMinute(val)
        }

        if (date) {
            updateValue(date, newHour, newMinute)
        }
    }

    const updateValue = (d: Date, h: string, m: string) => {
        const newDate = new Date(d)
        newDate.setHours(parseInt(h), parseInt(m), 0, 0)

        const year = newDate.getFullYear()
        const month = String(newDate.getMonth() + 1).padStart(2, '0')
        const day = String(newDate.getDate()).padStart(2, '0')

        onChange(`${year}-${month}-${day}T${h}:${m}`)
    }

    const hours = Array.from({ length: 24 }, (_, i) => i.toString().padStart(2, '0'))
    const minutes = Array.from({ length: 12 }, (_, i) => (i * 5).toString().padStart(2, '0'))

    const normalizedMinDate = React.useMemo(() => {
        if (!minDate) {
            return undefined
        }

        const nextMinDate = new Date(minDate)
        nextMinDate.setHours(0, 0, 0, 0)
        return nextMinDate
    }, [minDate])

    const normalizedMaxDate = React.useMemo(() => {
        if (!maxDate) {
            return undefined
        }

        const nextMaxDate = new Date(maxDate)
        nextMaxDate.setHours(23, 59, 59, 999)
        return nextMaxDate
    }, [maxDate])

    return (
        <Popover>
            <PopoverTrigger asChild>
                <Button
                    variant={"outline"}
                    className={cn(
                        "w-full justify-start text-left font-normal h-9 px-3",
                        !date && "text-muted-foreground"
                    )}
                >
                    <CalendarIcon className="mr-2 h-4 w-4 flex-shrink-0" />
                    <span className="truncate">
                        {date && value ? (
                            format(date, "yyyy-MM-dd") + ` ${hour}:${minute}`
                        ) : (
                            placeholder
                        )}
                    </span>
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0" align="start">
                <Calendar
                    mode="single"
                    selected={date}
                    onSelect={handleDateSelect}
                    initialFocus
                    disabled={(day) => {
                        const normalizedDay = new Date(day)
                        normalizedDay.setHours(12, 0, 0, 0)

                        if (normalizedMinDate && normalizedDay < normalizedMinDate) {
                            return true
                        }

                        if (normalizedMaxDate && normalizedDay > normalizedMaxDate) {
                            return true
                        }

                        return false
                    }}
                />
                <div className="p-3 border-t flex items-center justify-center gap-2 bg-muted/20">
                    <Clock className="w-4 h-4 text-muted-foreground mr-1 flex-shrink-0" />
                    <Select value={hour} onValueChange={(v) => handleTimeChange('hour', v)}>
                        <SelectTrigger className="w-[70px] h-8 text-xs bg-background">
                            <SelectValue placeholder="시" />
                        </SelectTrigger>
                        <SelectContent className="max-h-56">
                            {hours.map(h => (
                                <SelectItem key={h} value={h} className="text-xs">{h}시</SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                    <span className="text-muted-foreground">:</span>
                    <Select value={minute} onValueChange={(v) => handleTimeChange('minute', v)}>
                        <SelectTrigger className="w-[70px] h-8 text-xs bg-background">
                            <SelectValue placeholder="분" />
                        </SelectTrigger>
                        <SelectContent className="max-h-56">
                            {minutes.map(m => (
                                <SelectItem key={m} value={m} className="text-xs">{m}분</SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>
            </PopoverContent>
        </Popover>
    )
}
