import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

const KOREAN_WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"] as const

function parseDisplayDate(value: string) {
  if (!value) {
    return null
  }

  const normalizedValue = /^\d{4}-\d{2}-\d{2}$/.test(value)
    ? `${value}T00:00:00`
    : value
  const parsedDate = new Date(normalizedValue)

  return Number.isNaN(parsedDate.getTime()) ? null : parsedDate
}

export function formatDateWithWeekday(value: string) {
  const parsedDate = parseDisplayDate(value)

  if (!parsedDate) {
    return value
  }

  const year = parsedDate.getFullYear()
  const month = String(parsedDate.getMonth() + 1).padStart(2, "0")
  const day = String(parsedDate.getDate()).padStart(2, "0")
  const weekday = KOREAN_WEEKDAYS[parsedDate.getDay()]

  return `${year}-${month}-${day} (${weekday})`
}

export function formatDateTimeWithWeekday(value: string) {
  const parsedDate = parseDisplayDate(value)

  if (!parsedDate) {
    return value.replace("T", " ")
  }

  const dateLabel = formatDateWithWeekday(
    `${parsedDate.getFullYear()}-${String(parsedDate.getMonth() + 1).padStart(2, "0")}-${String(
      parsedDate.getDate(),
    ).padStart(2, "0")}`,
  )
  const hours = String(parsedDate.getHours()).padStart(2, "0")
  const minutes = String(parsedDate.getMinutes()).padStart(2, "0")

  return `${dateLabel} ${hours}:${minutes}`
}
