'use client'

import { useState, useMemo } from 'react'
import { useApp } from '@/lib/app-context'
import { MOCK_EVENTS } from '@/lib/mock-data'
import { AppShell } from '../app-shell'
import { EventCard } from '../event-card'
import { EmptyState } from '../empty-state'
import { Search, SlidersHorizontal, X } from 'lucide-react'
import { cn } from '@/lib/utils'

type SortKey = '인기순' | '최신순' | '마감임박순'

const REGIONS = ['전체', '서울', '경기', '인천', '부산', '대구', '광주', '대전', '기타']
const SORTS: SortKey[] = ['인기순', '최신순', '마감임박순']

// Popularity score: just index in mock array (lower index = more popular)
function popularityScore(id: string) {
  const order = ['evt_001', 'evt_002', 'evt_004', 'evt_003', 'evt_005']
  const idx = order.indexOf(id)
  return idx === -1 ? 99 : idx
}

export function ConcertsScreen() {
  const { navigate } = useApp()
  const [query, setQuery] = useState('')
  const [activeRegion, setActiveRegion] = useState('전체')
  const [sort, setSort] = useState<SortKey>('인기순')
  const [hideSoldOut, setHideSoldOut] = useState(false)
  const [showFilters, setShowFilters] = useState(false)

  const filtered = useMemo(() => {
    let list = MOCK_EVENTS.filter((e) => {
      const matchQuery =
        e.name.toLowerCase().includes(query.toLowerCase()) ||
        e.venue.toLowerCase().includes(query.toLowerCase())
      const matchRegion = activeRegion === '전체' || e.region === activeRegion
      const matchSoldOut = hideSoldOut ? e.status !== 'SOLD_OUT' : true
      return matchQuery && matchRegion && matchSoldOut
    })

    if (sort === '인기순') {
      list = [...list].sort((a, b) => popularityScore(a.id) - popularityScore(b.id))
    } else if (sort === '최신순') {
      list = [...list].sort((a, b) =>
        (b.openDate ?? '').localeCompare(a.openDate ?? '')
      )
    } else if (sort === '마감임박순') {
      list = [...list].sort((a, b) =>
        (a.openDate ?? '9999').localeCompare(b.openDate ?? '9999')
      )
    }

    return list
  }, [query, activeRegion, sort, hideSoldOut])

  const activeFilterCount =
    (activeRegion !== '전체' ? 1 : 0) + (hideSoldOut ? 1 : 0)

  return (
    <AppShell title="공연">
      <div className="flex flex-col gap-3 p-4">

        {/* Search bar */}
        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <input
              className="w-full bg-secondary border border-border rounded-xl py-3 pl-10 pr-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors"
              placeholder="공연명, 장소로 검색"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </div>
          {/* Filter toggle button */}
          <button
            onClick={() => setShowFilters((v) => !v)}
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

        {/* Sort tabs — always visible */}
        <div className="flex gap-1.5">
          {SORTS.map((s) => (
            <button
              key={s}
              onClick={() => setSort(s)}
              className={cn(
                'flex-1 py-2 rounded-xl text-xs font-semibold transition-colors border',
                sort === s
                  ? 'bg-primary text-primary-foreground border-primary'
                  : 'bg-secondary text-muted-foreground border-border hover:text-foreground'
              )}
            >
              {s}
            </button>
          ))}
        </div>

        {/* Expandable filter panel */}
        {showFilters && (
          <div className="flex flex-col gap-3 bg-secondary/50 border border-border rounded-2xl p-4">
            {/* Region */}
            <div>
              <p className="text-xs font-semibold text-muted-foreground mb-2">지역</p>
              <div className="flex flex-wrap gap-2">
                {REGIONS.map((r) => (
                  <button
                    key={r}
                    onClick={() => setActiveRegion(r)}
                    className={cn(
                      'px-3 py-1.5 rounded-full text-xs font-medium transition-colors border',
                      activeRegion === r
                        ? 'bg-primary text-primary-foreground border-primary'
                        : 'bg-background text-muted-foreground border-border hover:text-foreground'
                    )}
                  >
                    {r}
                  </button>
                ))}
              </div>
            </div>

            {/* Sold-out toggle */}
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-muted-foreground">매진 공연 숨기기</span>
              <button
                onClick={() => setHideSoldOut((v) => !v)}
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

            {/* Reset */}
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

        {/* Active filter chips */}
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
                매진 제외
                <button onClick={() => setHideSoldOut(false)}>
                  <X className="w-3 h-3" />
                </button>
              </span>
            )}
          </div>
        )}

        {/* Result count */}
        <p className="text-xs text-muted-foreground px-0.5">
          총 <span className="text-foreground font-semibold">{filtered.length}</span>개의 공연
        </p>

        {/* Event list */}
        {filtered.length === 0 ? (
          <EmptyState
            title="공연이 없습니다"
            description="필터 조건을 변경해 보세요."
          />
        ) : (
          <div className="flex flex-col gap-3">
            {filtered.map((event) => (
              <EventCard
                key={event.id}
                event={event}
                onClick={() => navigate('event-detail', { eventId: event.id })}
              />
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
