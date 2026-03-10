import type { Event, EventDate, EventStatus, Grade, RefundRule } from './types'

export interface KopisPerformance {
  id: string
  name: string
  startDate: string
  endDate: string
  venue: string
  poster: string
  area: string
  genre: string
  state: string
  openRun: string
}

export interface KopisPerformanceDetail extends KopisPerformance {
  story: string
  priceInfo: string
  runtime: string
  ageRating: string
  cast: string
  crew: string
  introImages: string[]
}

const KR_DAY_LABELS = ['일', '월', '화', '수', '목', '금', '토'] as const

const DEFAULT_REFUND_RULES: RefundRule[] = [
  { id: 'kopis_r1', daysBefore: 7, feeRate: 0, label: '공연 7일 전까지' },
  { id: 'kopis_r2', daysBefore: 3, feeRate: 0.1, label: '공연 3일 전까지' },
  { id: 'kopis_r3', daysBefore: 1, feeRate: 0.2, label: '공연 1일 전까지' },
  { id: 'kopis_r4', daysBefore: 0, feeRate: 1, label: '공연 당일 이후' },
]

const PRICE_COLORS = ['#ef4444', '#3b82f6', '#22c55e', '#f59e0b', '#a855f7', '#06b6d4']

const SEOUL_HINTS = ['서울', '대학로', '예술의전당', '올림픽', '잠실', '고척', '세종']

function looksMojibake(value: string) {
  if (!value) return false
  return /[ÃÂÐØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïð]/.test(value)
}

function safeKopisText(value: string, fallback: string) {
  const trimmed = value.trim()
  if (!trimmed || looksMojibake(trimmed)) return fallback
  return trimmed
}

function extractTagValue(xml: string, tag: string) {
  const match = xml.match(new RegExp(`<${tag}>(.*?)</${tag}>`, 's'))
  return match?.[1]?.trim() ?? ''
}

function decodeXmlText(value: string) {
  return value
    .replaceAll('&amp;', '&')
    .replaceAll('&lt;', '<')
    .replaceAll('&gt;', '>')
    .replaceAll('&quot;', '"')
    .replaceAll('&#39;', "'")
    .replaceAll('<![CDATA[', '')
    .replaceAll(']]>', '')
    .trim()
}

function extractTagValues(xml: string, tag: string) {
  return Array.from(xml.matchAll(new RegExp(`<${tag}>(.*?)</${tag}>`, 'gs'))).map((match) =>
    decodeXmlText(match[1] ?? '')
  )
}

function normalizeDate(value: string) {
  const digits = value.replace(/[^\d]/g, '')
  if (digits.length !== 8) return value.trim()
  return `${digits.slice(0, 4)}.${digits.slice(4, 6)}.${digits.slice(6, 8)}`
}

function formatEventPeriod(startDate: string, endDate: string) {
  const start = normalizeDate(startDate)
  const end = normalizeDate(endDate)

  if (!start) return ''
  if (!end || start === end) return start
  return `${start} ~ ${end.slice(5)}`
}

function parseDateString(value: string) {
  const normalized = normalizeDate(value)
  const match = normalized.match(/^(\d{4})\.(\d{2})\.(\d{2})$/)
  if (!match) return null

  return new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]))
}

function formatDateLabel(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const weekday = KR_DAY_LABELS[date.getDay()]
  return `${year}.${month}.${day} (${weekday})`
}

function extractTimes(runtime: string) {
  const times = Array.from(runtime.matchAll(/\b(\d{1,2}:\d{2})\b/g)).map((match) => match[1])
  return [...new Set(times)]
}

function buildEventDates(eventId: string, startDate: string, endDate: string, runtime: string) {
  const start = parseDateString(startDate)
  const end = parseDateString(endDate) ?? start
  if (!start || !end) return []

  const times = extractTimes(runtime)
  const dates: EventDate[] = []

  let cursor = new Date(start)
  let dayIndex = 1

  while (cursor <= end) {
    const baseLabel = formatDateLabel(cursor)

    if (times.length > 0) {
      times.forEach((time, timeIndex) => {
        dates.push({
          id: `${eventId}_d${dayIndex}_${timeIndex + 1}`,
          label: `${baseLabel} ${time}`,
          day: `DAY ${dayIndex}`,
        })
      })
    } else {
      dates.push({
        id: `${eventId}_d${dayIndex}`,
        label: baseLabel,
        day: `DAY ${dayIndex}`,
      })
    }

    cursor.setDate(cursor.getDate() + 1)
    dayIndex += 1
  }

  return dates
}

function mapStatus(state: string): EventStatus {
  const normalized = state.trim()
  if (!normalized) return 'ON_SALE'

  if (
    normalized.includes('매진') ||
    normalized.includes('판매마감') ||
    normalized.includes('공연완료') ||
    normalized.includes('종료')
  ) {
    return 'SOLD_OUT'
  }

  return 'ON_SALE'
}

function mapRegion(area: string, venue: string) {
  const normalizedArea = safeKopisText(area, '').trim()
  if (normalizedArea) return normalizedArea

  if (SEOUL_HINTS.some((keyword) => venue.includes(keyword))) return '서울'
  if (venue.includes('부산')) return '부산'
  if (venue.includes('대구')) return '대구'
  if (venue.includes('인천')) return '인천'
  if (venue.includes('광주')) return '광주'
  if (venue.includes('대전')) return '대전'
  if (venue.includes('제주')) return '제주'
  if (venue.includes('경기') || venue.includes('수원') || venue.includes('고양') || venue.includes('성남')) {
    return '경기'
  }
  if (venue.includes('경남') || venue.includes('창원')) return '경남'
  if (venue.includes('전북') || venue.includes('전주')) return '전북'

  return '기타'
}

function normalizePriceGradeName(name: string) {
  const cleaned = name
    .replace(/\s+/g, ' ')
    .replace(/\(.*?\)/g, '')
    .replace(/[^\p{L}\p{N}\s]/gu, '')
    .trim()

  if (!cleaned) return '전석'
  if (cleaned.includes('전석')) return '전석'
  if (cleaned.endsWith('석') || cleaned.endsWith('권') || cleaned.endsWith('존')) return cleaned
  return `${cleaned}석`
}

function parsePrice(value: string) {
  const digits = value.replace(/[^\d]/g, '')
  return digits ? Number(digits) : 0
}

function extractBestPrice(value: string) {
  const matches = Array.from(value.matchAll(/\d[\d,]*/g)).map((match) => parsePrice(match[0]))
  const candidates = matches.filter((price) => price > 0)

  if (candidates.length === 0) return 0
  return Math.max(...candidates)
}

export function parseKopisPriceInfo(priceInfo: string, status: EventStatus): Grade[] {
  const normalized = priceInfo
    .replace(/<br\s*\/?>/gi, ',')
    .replace(/\r?\n/g, ',')
    .replace(/\//g, ',')
    .trim()

  if (!normalized) return []

  const matches = Array.from(
    normalized.matchAll(/([A-Za-z0-9\u3131-\uD79D\s]+?(?:석|권|존)?|(?:전석))\s*([0-9][0-9,]*)/g)
  )

  const grades = matches
    .map<Grade | null>((match, index) => {
      const price = parsePrice(match[2] ?? '')
      if (price === 0) return null

      return {
        name: normalizePriceGradeName(match[1] ?? ''),
        price,
        remaining: status === 'ON_SALE' ? 999 : 0,
        color: PRICE_COLORS[index % PRICE_COLORS.length],
      }
    })
    .filter((item): item is Grade => Boolean(item))

  if (grades.length > 0) return grades

  const fallbackPrice = extractBestPrice(normalized)
  if (fallbackPrice === 0) return []

  return [
    {
      name: '전석',
      price: fallbackPrice,
      remaining: status === 'ON_SALE' ? 999 : 0,
      color: PRICE_COLORS[0],
    },
  ]
}

export function parseKopisPerformanceList(xml: string): KopisPerformance[] {
  const dbMatches = xml.match(/<db>([\s\S]*?)<\/db>/g) ?? []

  return dbMatches.map((dbXml) => ({
    id: decodeXmlText(extractTagValue(dbXml, 'mt20id')),
    name: decodeXmlText(extractTagValue(dbXml, 'prfnm')),
    startDate: decodeXmlText(extractTagValue(dbXml, 'prfpdfrom')),
    endDate: decodeXmlText(extractTagValue(dbXml, 'prfpdto')),
    venue: decodeXmlText(extractTagValue(dbXml, 'fcltynm')),
    poster: decodeXmlText(extractTagValue(dbXml, 'poster')),
    area: decodeXmlText(extractTagValue(dbXml, 'area')),
    genre: decodeXmlText(extractTagValue(dbXml, 'genrenm')),
    state: decodeXmlText(extractTagValue(dbXml, 'prfstate')),
    openRun: decodeXmlText(extractTagValue(dbXml, 'openrun')),
  }))
}

export function parseKopisPerformanceDetail(xml: string): KopisPerformanceDetail {
  return {
    id: decodeXmlText(extractTagValue(xml, 'mt20id')),
    name: decodeXmlText(extractTagValue(xml, 'prfnm')),
    startDate: decodeXmlText(extractTagValue(xml, 'prfpdfrom')),
    endDate: decodeXmlText(extractTagValue(xml, 'prfpdto')),
    venue: decodeXmlText(extractTagValue(xml, 'fcltynm')),
    poster: decodeXmlText(extractTagValue(xml, 'poster')),
    area: decodeXmlText(extractTagValue(xml, 'area')),
    genre: decodeXmlText(extractTagValue(xml, 'genrenm')),
    state: decodeXmlText(extractTagValue(xml, 'prfstate')),
    openRun: decodeXmlText(extractTagValue(xml, 'openrun')),
    story: decodeXmlText(extractTagValue(xml, 'sty')),
    priceInfo: decodeXmlText(extractTagValue(xml, 'pcseguidance')),
    runtime: decodeXmlText(extractTagValue(xml, 'dtguidance')),
    ageRating: decodeXmlText(extractTagValue(xml, 'prfage')),
    cast: decodeXmlText(extractTagValue(xml, 'prfcast')),
    crew: decodeXmlText(extractTagValue(xml, 'prfcrew')),
    introImages: extractTagValues(xml, 'styurl').filter(Boolean),
  }
}

export function mapKopisPerformanceToEvent(performance: KopisPerformance): Event {
  const status = mapStatus(performance.state)
  const eventId = `kopis_${performance.id}`
  const region = mapRegion(performance.area, performance.venue)
  const date = formatEventPeriod(performance.startDate, performance.endDate)
  const name = safeKopisText(performance.name, `KOPIS 공연 ${performance.id}`)
  const genre = safeKopisText(performance.genre, '공연')
  const venueName = safeKopisText(performance.venue, `${region} 공연장`)
  const description = [
    safeKopisText(performance.genre, '공연'),
    safeKopisText(performance.state, status === 'ON_SALE' ? '예매 가능' : '매진'),
    safeKopisText(performance.openRun, ''),
  ]
    .filter(Boolean)
    .join(' · ')

  return {
    id: eventId,
    name,
    artistName: genre,
    date,
    dates: buildEventDates(eventId, performance.startDate, performance.endDate, ''),
    venue: venueName ? `${venueName}, ${region}` : region,
    region,
    poster: performance.poster || '/posters/concert1.jpg',
    status,
    maxPerUser: 4,
    grades: [
      {
        name: '전석',
        price: 0,
        remaining: status === 'ON_SALE' ? 1 : 0,
        color: '#22c55e',
      },
    ],
    refundRules: DEFAULT_REFUND_RULES.map((rule) => ({ ...rule, id: `${performance.id}_${rule.id}` })),
    openDate: normalizeDate(performance.startDate).replaceAll('.', '-'),
    description: description || 'KOPIS 공연 정보를 불러왔습니다.',
  }
}

export function mergeKopisDetailIntoEvent(event: Event, detail: KopisPerformanceDetail): Event {
  const status = mapStatus(detail.state)
  const region = mapRegion(detail.area, detail.venue)
  const safeName = safeKopisText(detail.name, event.name)
  const safeGenre = safeKopisText(detail.genre, event.artistName || '공연')
  const safeVenue = safeKopisText(detail.venue, event.venue.replace(`, ${event.region}`, ''))
  const venue = safeVenue ? `${safeVenue}, ${region}` : event.venue
  const grades = parseKopisPriceInfo(detail.priceInfo, status)
  const dates = buildEventDates(event.id, detail.startDate, detail.endDate, detail.runtime)
  const description = safeKopisText(detail.story, event.description || 'KOPIS 공연 상세 정보입니다.')
  const runtime = safeKopisText(detail.runtime, event.runtime || '')
  const ageRating = safeKopisText(detail.ageRating, event.ageRating || '')
  const cast = safeKopisText(detail.cast, event.cast || '')
  const crew = safeKopisText(detail.crew, event.crew || '')

  return {
    ...event,
    name: safeName,
    artistName: safeGenre,
    date: formatEventPeriod(detail.startDate, detail.endDate) || event.date,
    dates: dates.length > 0 ? dates : event.dates,
    venue,
    region,
    poster: detail.poster || event.poster,
    status,
    description,
    priceInfo: safeKopisText(detail.priceInfo, event.priceInfo || ''),
    runtime,
    ageRating,
    cast,
    crew,
    introImages: detail.introImages.length > 0 ? detail.introImages : event.introImages,
    grades: grades.length > 0 ? grades : event.grades,
  }
}
