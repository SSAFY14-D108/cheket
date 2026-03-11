import { NextResponse } from 'next/server'
import { mapKopisPerformanceToEvent, parseKopisPerformanceList, SIGNGU_TO_REGION } from '@/lib/kopis'

export const dynamic = 'force-dynamic'
const DEFAULT_GENRE_CODE = 'CCCD'

function detectKopisEncoding(response: Response, buffer: ArrayBuffer) {
  const contentType = response.headers.get('content-type')?.toLowerCase() ?? ''
  const headerCharset = contentType.match(/charset=([^;]+)/)?.[1]?.trim()

  if (headerCharset) return headerCharset

  const asciiHead = Buffer.from(buffer).subarray(0, 256).toString('latin1').toLowerCase()
  const xmlCharset = asciiHead.match(/encoding=["']([^"']+)["']/)?.[1]?.trim()

  if (xmlCharset) return xmlCharset
  return 'utf-8'
}

function normalizeEncodingName(encoding: string) {
  const normalized = encoding.toLowerCase()
  if (
    normalized.includes('euc-kr') ||
    normalized.includes('ks_c_5601') ||
    normalized.includes('ksc5601') ||
    normalized.includes('cp949') ||
    normalized.includes('windows-949')
  ) {
    return 'euc-kr'
  }

  return 'utf-8'
}

async function readKopisXml(response: Response) {
  const buffer = await response.arrayBuffer()
  const encoding = normalizeEncodingName(detectKopisEncoding(response, buffer))

  try {
    return new TextDecoder(encoding).decode(buffer)
  } catch {
    return new TextDecoder('utf-8').decode(buffer)
  }
}

function getDefaultDateRange() {
  const now = new Date()
  const start = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`

  const endDate = new Date(now)
  endDate.setDate(endDate.getDate() + 30)
  const end = `${endDate.getFullYear()}${String(endDate.getMonth() + 1).padStart(2, '0')}${String(endDate.getDate()).padStart(2, '0')}`

  return { start, end }
}

export async function GET(request: Request) {
  const serviceKey = process.env.KOPIS_SERVICE_KEY || process.env.NEXT_PUBLIC_KOPIS_SERVICE_KEY

  if (!serviceKey) {
    return NextResponse.json(
      {
        items: [],
        source: 'mock',
        error: 'KOPIS_SERVICE_KEY is missing',
      },
      { status: 200 }
    )
  }

  const { searchParams } = new URL(request.url)
  const { start, end } = getDefaultDateRange()

  const signgucode = searchParams.get('signgucode') ?? ''

  const params = new URLSearchParams({
    service: serviceKey,
    stdate: searchParams.get('stdate') ?? start,
    eddate: searchParams.get('eddate') ?? end,
    cpage: searchParams.get('cpage') ?? '1',
    rows: searchParams.get('rows') ?? '30',
    shcate: searchParams.get('shcate') ?? DEFAULT_GENRE_CODE,
  })

  if (signgucode) {
    params.set('signgucode', signgucode)
  }

  try {
    const response = await fetch(`https://www.kopis.or.kr/openApi/restful/pblprfr?${params.toString()}`, {
      cache: 'no-store',
    })

    if (!response.ok) {
      return NextResponse.json(
        {
          items: [],
          source: 'mock',
          error: `KOPIS request failed: ${response.status}`,
        },
        { status: 200 }
      )
    }

    const xml = await readKopisXml(response)
    const regionHint = signgucode ? (SIGNGU_TO_REGION[signgucode] ?? '') : ''
    const performances = parseKopisPerformanceList(xml).filter(
      (item) => item.id.length > 0 && item.name.length > 0
    )
    const items = performances.map((p) => mapKopisPerformanceToEvent(p, regionHint, signgucode || undefined))

    return NextResponse.json({
      items,
      source: 'kopis',
      total: items.length,
      signgucode: signgucode || undefined,
      region: regionHint || undefined,
    })
  } catch (error) {
    return NextResponse.json(
      {
        items: [],
        source: 'mock',
        error: error instanceof Error ? error.message : 'Unknown KOPIS error',
      },
      { status: 200 }
    )
  }
}
