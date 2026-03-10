import { NextResponse } from 'next/server'
import { mapKopisPerformanceToEvent, mergeKopisDetailIntoEvent, parseKopisPerformanceDetail } from '@/lib/kopis'

export const dynamic = 'force-dynamic'

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

export async function GET(_: Request, context: { params: Promise<{ id: string }> }) {
  const serviceKey = process.env.KOPIS_SERVICE_KEY || process.env.NEXT_PUBLIC_KOPIS_SERVICE_KEY

  if (!serviceKey) {
    return NextResponse.json({ item: null, error: 'KOPIS_SERVICE_KEY is missing' }, { status: 200 })
  }

  const { id } = await context.params
  const kopisId = id.replace(/^kopis_/, '')

  try {
    const response = await fetch(
      `https://www.kopis.or.kr/openApi/restful/pblprfr/${kopisId}?service=${encodeURIComponent(serviceKey)}`,
      { cache: 'no-store' }
    )

    if (!response.ok) {
      return NextResponse.json({ item: null, error: `KOPIS detail request failed: ${response.status}` }, { status: 200 })
    }

    const xml = await readKopisXml(response)
    const detail = parseKopisPerformanceDetail(xml)

    if (!detail.id || !detail.name) {
      return NextResponse.json({ item: null, error: 'Invalid KOPIS detail response' }, { status: 200 })
    }

    const baseEvent = mapKopisPerformanceToEvent(detail)
    const item = mergeKopisDetailIntoEvent(baseEvent, detail)

    return NextResponse.json({ item, source: 'kopis' })
  } catch (error) {
    return NextResponse.json(
      {
        item: null,
        error: error instanceof Error ? error.message : 'Unknown KOPIS detail error',
      },
      { status: 200 }
    )
  }
}
