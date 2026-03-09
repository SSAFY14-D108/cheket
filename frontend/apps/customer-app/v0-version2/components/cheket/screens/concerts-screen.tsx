'use client'

import { useEffect, useMemo, useRef, useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { EventCard } from '../event-card'
import { EmptyState } from '../empty-state'
import { Search, SlidersHorizontal, X } from 'lucide-react'
import { cn } from '@/lib/utils'

type SortKey = '인기순' | '최신순' | '오픈임박순'

const REGIONS = ['전체', '서울', '경기', '인천', '부산', '대구', '대전', '광주', '제주']
const SORTS: SortKey[] = ['인기순', '최신순', '오픈임박순']
const PAGE_SIZE = 6

function popularityScore(id: string) {
  const order = ['evt_001', 'evt_002', 'evt_004', 'evt_006', 'evt_007', 'evt_008']
  const idx = order.indexOf(id)
  return idx === -1 ? 99 : idx
}

export function ConcertsScreen() {
  const { navigate, events } = useApp()
  const loadMoreRef = useRef<HTMLDivElement | null>(null)
  const [query, setQuery] = useState('')
  const [selectedRegions, setSelectedRegions] = useState<string[]>([])
  const [sort, setSort] = useState<SortKey>('인기순')
  const [showFilters, setShowFilters] = useState(false)
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)

  const toggleRegion = (region: string) => {
    if (region === '전체') {
      setSelectedRegions([])
      return
    }

    setSelectedRegions((prev) =>
      prev.includes(region) ? prev.filter((item) => item !== region) : [...prev, region]
    )
  }

  const filtered = useMemo(() => {
    let list = events.filter((event) => {
      const keyword = query.trim().toLowerCase()
      const matchQuery =
        keyword.length === 0 ||
        event.name.toLowerCase().includes(keyword) ||
        event.venue.toLowerCase().includes(keyword) ||
        (event.artistName ?? '').toLowerCase().includes(keyword)
      const matchRegion =
        selectedRegions.length === 0 || selectedRegions.includes(event.region)
      return matchQuery && matchRegion
    })

    if (sort === '인기순') {
      list = [...list].sort((a, b) => popularityScore(a.id) - popularityScore(b.id))
    } else if (sort === '최신순') {
      list = [...list].sort((a, b) => (b.openDate ?? '').localeCompare(a.openDate ?? ''))
    } else {
      list = [...list].sort((a, b) => (a.openDate ?? '9999').localeCompare(b.openDate ?? '9999'))
    }

    return list
  }, [events, query, selectedRegions, sort])

  const visibleEvents = filtered.slice(0, visibleCount)
  const hasMore = visibleCount < filtered.length
  const activeFilterCount = selectedRegions.length

  useEffect(() => {
    setVisibleCount(PAGE_SIZE)
  }, [query, selectedRegions, sort])

  useEffect(() => {
    const node = loadMoreRef.current
    if (!node || !hasMore) return

    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries
        if (entry?.isIntersecting) {
          setVisibleCount((prev) => Math.min(prev + PAGE_SIZE, filtered.length))
        }
      },
      { rootMargin: '160px 0px' }
    )

    observer.observe(node)
    return () => observer.disconnect()
  }, [filtered.length, hasMore])

  return (
    <AppShell title="공연">
      <div className="flex flex-col gap-3 p-4">
        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input
              className="w-full rounded-xl border border-border bg-secondary py-3 pl-10 pr-4 text-sm text-foreground placeholder:text-muted-foreground transition-colors focus:border-primary focus:outline-none"
              placeholder="공연명, 아티스트, 장소 검색"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </div>
          <button
            onClick={() => setShowFilters((value) => !value)}
            className={cn(
              'relative flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-xl border transition-colors',
              showFilters
                ? 'border-primary bg-primary text-primary-foreground'
                : 'border-border bg-secondary text-muted-foreground hover:text-foreground'
            )}
          >
            <SlidersHorizontal className="h-4 w-4" />
            {activeFilterCount > 0 && (
              <span className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full border border-background bg-primary text-[10px] font-bold text-primary-foreground">
                {activeFilterCount}
              </span>
            )}
          </button>
        </div>

        <div className="flex gap-1.5">
          {SORTS.map((item) => (
            <button
              key={item}
              onClick={() => setSort(item)}
              className={cn(
                'flex-1 rounded-xl border py-2 text-xs font-semibold transition-colors',
                sort === item
                  ? 'border-primary bg-primary text-primary-foreground'
                  : 'border-border bg-secondary text-muted-foreground hover:text-foreground'
              )}
            >
              {item}
            </button>
          ))}
        </div>

        {showFilters && (
          <div className="flex flex-col gap-3 rounded-2xl border border-border bg-secondary/50 p-4">
            <div>
              <p className="mb-2 text-xs font-semibold text-muted-foreground">지역</p>
              <div className="flex flex-wrap gap-2">
                {REGIONS.map((region) => (
                  <button
                    key={region}
                    onClick={() => toggleRegion(region)}
                    className={cn(
                      'rounded-full border px-3 py-1.5 text-xs font-medium transition-colors',
                      (region === '전체' && selectedRegions.length === 0) ||
                        selectedRegions.includes(region)
                        ? 'border-primary bg-primary text-primary-foreground'
                        : 'border-border bg-background text-muted-foreground hover:text-foreground'
                    )}
                  >
                    {region}
                  </button>
                ))}
              </div>
            </div>

            {activeFilterCount > 0 && (
              <button
                onClick={() => {
                  setSelectedRegions([])
                }}
                className="self-start text-xs text-muted-foreground hover:text-foreground"
              >
                <span className="inline-flex items-center gap-1.5">
                  <X className="h-3 w-3" />
                  필터 초기화
                </span>
              </button>
            )}
          </div>
        )}

        {activeFilterCount > 0 && !showFilters && (
          <div className="flex flex-wrap gap-2">
            {selectedRegions.map((region) => (
              <span
                key={region}
                className="flex items-center gap-1 rounded-full bg-primary/10 px-2.5 py-1 text-xs font-medium text-primary"
              >
                {region}
                <button onClick={() => toggleRegion(region)}>
                  <X className="h-3 w-3" />
                </button>
              </span>
            ))}
          </div>
        )}

        <p className="px-0.5 text-xs text-muted-foreground">
          총 <span className="font-semibold text-foreground">{filtered.length}</span>개의 공연
        </p>

        {filtered.length === 0 ? (
          <EmptyState
            title="공연이 없어요"
            description="검색어나 필터를 바꿔서 다시 확인해 보세요."
          />
        ) : (
          <>
            <div className="flex flex-col gap-3">
              {visibleEvents.map((event) => (
                <EventCard
                  key={event.id}
                  event={event}
                  onClick={() => navigate('event-detail', { eventId: event.id })}
                />
              ))}
            </div>

            <div
              ref={loadMoreRef}
              className={cn('flex items-center justify-center', hasMore ? 'h-10' : 'h-2')}
            >
              {hasMore && <span className="text-xs text-muted-foreground">공연을 더 불러오는 중...</span>}
            </div>
          </>
        )}
      </div>
    </AppShell>
  )
}
