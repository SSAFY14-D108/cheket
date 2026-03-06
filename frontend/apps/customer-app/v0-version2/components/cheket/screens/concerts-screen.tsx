'use client'

import { useEffect, useMemo, useRef, useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { EventCard } from '../event-card'
import { EmptyState } from '../empty-state'
import { Search, SlidersHorizontal, X } from 'lucide-react'
import { cn } from '@/lib/utils'

type SortKey = '인기순' | '최신순' | '오픈임박순'

const REGIONS = ['전체', '서울', '부산', '인천', '대구', '광주', '경기', '경남', '전북']
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
  const [activeRegion, setActiveRegion] = useState('전체')
  const [sort, setSort] = useState<SortKey>('인기순')
  const [hideSoldOut, setHideSoldOut] = useState(false)
  const [showFilters, setShowFilters] = useState(false)
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)

  const filtered = useMemo(() => {
    let list = events.filter((event) => {
      const keyword = query.trim().toLowerCase()
      const matchQuery =
        keyword.length === 0 ||
        event.name.toLowerCase().includes(keyword) ||
        event.venue.toLowerCase().includes(keyword) ||
        (event.artistName ?? '').toLowerCase().includes(keyword)
      const matchRegion = activeRegion === '전체' || event.region === activeRegion
      const matchSoldOut = hideSoldOut ? event.status !== 'SOLD_OUT' : true
      return matchQuery && matchRegion && matchSoldOut
    })

    if (sort === '인기순') {
      list = [...list].sort((a, b) => popularityScore(a.id) - popularityScore(b.id))
    } else if (sort === '최신순') {
      list = [...list].sort((a, b) => (b.openDate ?? '').localeCompare(a.openDate ?? ''))
    } else {
      list = [...list].sort((a, b) => (a.openDate ?? '9999').localeCompare(b.openDate ?? '9999'))
    }

    return list
  }, [activeRegion, events, hideSoldOut, query, sort])

  const visibleEvents = filtered.slice(0, visibleCount)
  const hasMore = visibleCount < filtered.length
  const activeFilterCount = (activeRegion !== '전체' ? 1 : 0) + (hideSoldOut ? 1 : 0)

  useEffect(() => {
    setVisibleCount(PAGE_SIZE)
  }, [query, activeRegion, sort, hideSoldOut])

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
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <input
              className="w-full bg-secondary border border-border rounded-xl py-3 pl-10 pr-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
              placeholder="공연명, 아티스트, 장소 검색"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </div>
          <button
            onClick={() => setShowFilters((value) => !value)}
            className={cn(
              'relative flex-shrink-0 w-12 h-12 flex items-center justify-center rounded-xl border transition-colors',
              showFilters
                ? 'bg-primary border-primary text-primary-foreground'
                : 'bg-secondary border-border text-muted-foreground hover:text-foreground'
            )}
          >
            <SlidersHorizontal className="w-4 h-4" />
            {activeFilterCount > 0 && (
              <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-primary text-primary-foreground text-[10px] font-bold flex items-center justify-center border border-background">
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
                'flex-1 py-2 rounded-xl text-xs font-semibold transition-colors border',
                sort === item
                  ? 'bg-primary text-primary-foreground border-primary'
                  : 'bg-secondary text-muted-foreground border-border hover:text-foreground'
              )}
            >
              {item}
            </button>
          ))}
        </div>

        {showFilters && (
          <div className="flex flex-col gap-3 bg-secondary/50 border border-border rounded-2xl p-4">
            <div>
              <p className="text-xs font-semibold text-muted-foreground mb-2">지역</p>
              <div className="flex flex-wrap gap-2">
                {REGIONS.map((region) => (
                  <button
                    key={region}
                    onClick={() => setActiveRegion(region)}
                    className={cn(
                      'px-3 py-1.5 rounded-full text-xs font-medium transition-colors border',
                      activeRegion === region
                        ? 'bg-primary text-primary-foreground border-primary'
                        : 'bg-background text-muted-foreground border-border hover:text-foreground'
                    )}
                  >
                    {region}
                  </button>
                ))}
              </div>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-muted-foreground">매진 공연 숨기기</span>
              <button
                onClick={() => setHideSoldOut((value) => !value)}
                className={cn(
                  'w-11 h-6 rounded-full transition-colors relative',
                  hideSoldOut ? 'bg-primary' : 'bg-border'
                )}
              >
                <span
                  className={cn(
                    'absolute top-0.5 w-5 h-5 rounded-full bg-white shadow transition-transform',
                    hideSoldOut ? 'translate-x-5' : 'translate-x-0.5'
                  )}
                />
              </button>
            </div>

            {activeFilterCount > 0 && (
              <button
                onClick={() => {
                  setActiveRegion('전체')
                  setHideSoldOut(false)
                }}
                className="flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground self-start"
              >
                <X className="w-3 h-3" />
                필터 초기화
              </button>
            )}
          </div>
        )}

        {activeFilterCount > 0 && !showFilters && (
          <div className="flex flex-wrap gap-2">
            {activeRegion !== '전체' && (
              <span className="flex items-center gap-1 px-2.5 py-1 rounded-full bg-primary/10 text-primary text-xs font-medium">
                {activeRegion}
                <button onClick={() => setActiveRegion('전체')}>
                  <X className="w-3 h-3" />
                </button>
              </span>
            )}
            {hideSoldOut && (
              <span className="flex items-center gap-1 px-2.5 py-1 rounded-full bg-primary/10 text-primary text-xs font-medium">
                매진 숨김
                <button onClick={() => setHideSoldOut(false)}>
                  <X className="w-3 h-3" />
                </button>
              </span>
            )}
          </div>
        )}

        <p className="text-xs text-muted-foreground px-0.5">
          총 <span className="text-foreground font-semibold">{filtered.length}</span>개의 공연
        </p>

        {filtered.length === 0 ? (
          <EmptyState
            title="공연이 없습니다"
            description="검색어나 필터 조건을 다시 확인해보세요."
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

            <div ref={loadMoreRef} className="h-16 flex items-center justify-center">
              {hasMore ? (
                <span className="text-xs text-muted-foreground">공연을 더 불러오는 중...</span>
              ) : (
                <span className="text-xs text-muted-foreground">모든 공연을 확인했습니다.</span>
              )}
            </div>
          </>
        )}
      </div>
    </AppShell>
  )
}
