"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { Plus, Settings, Wallet } from "lucide-react"
import { CompanyInfoCard } from "@/components/mypage/CompanyInfoCard"
import { EventCard } from "@/components/mypage/EventCard"
import { LogoutButton } from "@/components/mypage/LogoutButton"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { useToast } from "@/hooks/use-toast"
import { ApiError } from "@/lib/api"
import { getShowDisplayMeta } from "@/lib/show-display"
import {
  fetchMyCompanyInfo,
  fetchMyShows,
  fetchMyWalletBalance,
  type MyCompanyInfo,
  type MyShowsPage,
  type MyShowSummary,
  type MyWalletBalance,
} from "@/lib/mypage-api"

const FETCH_SHOWS_SIZE = 100
const DISPLAY_SHOWS_PAGE_SIZE = 8
type ShowFilter = "all" | "pending" | "upcoming" | "live" | "ended"

function sortShowsByLatest(left: MyShowSummary, right: MyShowSummary) {
  const leftTimestamp = new Date(left.show.showStartDate).getTime()
  const rightTimestamp = new Date(right.show.showStartDate).getTime()

  return rightTimestamp - leftTimestamp
}

export function MyPageContent() {
  const { toast } = useToast()
  const [company, setCompany] = useState<MyCompanyInfo | null>(null)
  const [showsPage, setShowsPage] = useState<MyShowsPage | null>(null)
  const [isSummaryLoading, setIsSummaryLoading] = useState(true)
  const [isShowsLoading, setIsShowsLoading] = useState(true)
  const [hasSummaryLoadError, setHasSummaryLoadError] = useState(false)
  const [hasShowsLoadError, setHasShowsLoadError] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [activeTab, setActiveTab] = useState<"info" | "shows">("shows")
  const [showFilter, setShowFilter] = useState<ShowFilter>("all")
  const [searchQuery, setSearchQuery] = useState("")

  useEffect(() => {
    let isCancelled = false

    async function loadMySummary() {
      try {
        const [companyInfo, walletResult] = await Promise.all([
          fetchMyCompanyInfo(),
          fetchMyWalletBalance()
            .then((wallet) => ({ status: "fulfilled" as const, value: wallet }))
            .catch((error: unknown) => ({ status: "rejected" as const, reason: error })),
        ])

        if (isCancelled) return

        const nextCompany: MyCompanyInfo = { ...companyInfo }

        if (walletResult.status === "fulfilled") {
          const wallet: MyWalletBalance = walletResult.value
          nextCompany.walletAddress = wallet.walletAddress
          nextCompany.balance = wallet.balance
        } else {
          nextCompany.walletAddress = null
          nextCompany.balance = null

          const walletError = walletResult.reason

          if (!(walletError instanceof ApiError && walletError.status === 401)) {
            toast({
              title: "지갑 정보를 불러오지 못했습니다",
              description:
                walletError instanceof ApiError
                  ? walletError.message
                  : "지갑 정보를 가져오는 중 문제가 발생했습니다.",
              variant: "destructive",
            })
          }
        }

        setCompany(nextCompany)
      } catch (error) {
        if (isCancelled) return

        if (!(error instanceof ApiError && error.status === 401)) {
          setHasSummaryLoadError(true)
          toast({
            title: "회사 정보를 불러오지 못했습니다",
            description:
              error instanceof ApiError
                ? error.message
                : "회사 정보를 가져오는 중 문제가 발생했습니다.",
            variant: "destructive",
          })
        }
      } finally {
        if (!isCancelled) setIsSummaryLoading(false)
      }
    }

    void loadMySummary()

    return () => {
      isCancelled = true
    }
  }, [toast])

  useEffect(() => {
    let isCancelled = false

    async function loadShows() {
      setIsShowsLoading(true)
      setHasShowsLoadError(false)

      try {
        const nextShowsPage = await fetchMyShows({
          page: 0,
          size: FETCH_SHOWS_SIZE,
        })

        if (isCancelled) return
        setShowsPage(nextShowsPage)
      } catch (error) {
        if (isCancelled) return

        if (!(error instanceof ApiError && error.status === 401)) {
          setHasShowsLoadError(true)
          toast({
            title: "공연 목록을 불러오지 못했습니다",
            description:
              error instanceof ApiError
                ? error.message
                : "공연 목록을 가져오는 중 문제가 발생했습니다.",
            variant: "destructive",
          })
        }
      } finally {
        if (!isCancelled) setIsShowsLoading(false)
      }
    }

    void loadShows()

    return () => {
      isCancelled = true
    }
  }, [toast])

  const allShows: MyShowSummary[] = [...(showsPage?.shows ?? [])].sort(sortShowsByLatest)
  const normalizedSearchQuery = searchQuery.trim().toLowerCase()
  const filteredShows = allShows.filter((show) => {
    const matchesSearch =
      normalizedSearchQuery.length === 0 ||
      show.title.toLowerCase().includes(normalizedSearchQuery)

    if (!matchesSearch) return false
    if (showFilter === "all") return true
    if (showFilter === "pending") return show.status === "PENDING_CONTRACT"

    const performancePhase = getShowDisplayMeta(show).performance.phase

    if (showFilter === "upcoming") return performancePhase === "UPCOMING"
    if (showFilter === "live") return performancePhase === "LIVE"
    return performancePhase === "ENDED"
  })

  const totalShows = showsPage?.totalElements ?? 0
  const totalPages = Math.ceil(filteredShows.length / DISPLAY_SHOWS_PAGE_SIZE)
  const isFirstPage = currentPage === 0
  const isLastPage = totalPages === 0 || currentPage >= totalPages - 1
  const visibleShows = filteredShows.slice(
    currentPage * DISPLAY_SHOWS_PAGE_SIZE,
    (currentPage + 1) * DISPLAY_SHOWS_PAGE_SIZE
  )
  const liveShowsCount = allShows.filter(
    (show) => getShowDisplayMeta(show).performance.phase === "LIVE"
  ).length
  const filterOptions: Array<{ key: ShowFilter; label: string; count: number }> = [
    { key: "all", label: "전체", count: allShows.length },
    {
      key: "pending",
      label: "승인대기중",
      count: allShows.filter((show) => show.status === "PENDING_CONTRACT").length,
    },
    {
      key: "upcoming",
      label: "공연 예정",
      count: allShows.filter((show) => getShowDisplayMeta(show).performance.phase === "UPCOMING")
        .length,
    },
    {
      key: "live",
      label: "공연 중",
      count: allShows.filter((show) => getShowDisplayMeta(show).performance.phase === "LIVE").length,
    },
    {
      key: "ended",
      label: "공연 종료",
      count: allShows.filter((show) => getShowDisplayMeta(show).performance.phase === "ENDED")
        .length,
    },
  ]

  useEffect(() => {
    if (totalPages > 0 && currentPage > totalPages - 1) {
      setCurrentPage(totalPages - 1)
    }
  }, [currentPage, totalPages])

  useEffect(() => {
    setCurrentPage(0)
  }, [showFilter, searchQuery])

  return (
    <main className="min-h-svh bg-white">
      <div className="mx-auto max-w-[1160px] px-5 py-6">
        <section className="overflow-hidden rounded-[1.4rem] border border-black/8 bg-white px-5 py-5 shadow-sm">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div className="space-y-3">
              <p className="text-[11px] font-medium uppercase tracking-[0.22em] text-black/42">
                My Page
              </p>
              <div className="space-y-2">
                <h1 className="text-[1.8rem] font-semibold tracking-[-0.04em] text-black sm:text-[2rem]">
                  {company?.companyName ?? "회사 정보"}
                </h1>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-2">
              <Link
                href="/shows/create"
                className="inline-flex items-center gap-2 rounded-[1rem] bg-[#171717] px-3.5 py-2 text-sm font-semibold text-white transition-colors hover:bg-black/85"
              >
                <Plus className="size-4" />
                공연 등록
              </Link>
              <Link
                href="/mypage/settings"
                className="inline-flex items-center gap-2 rounded-[1rem] border border-black/10 bg-white px-3.5 py-2 text-sm font-semibold text-black/72 transition-colors hover:bg-black/[0.03]"
              >
                <Settings className="size-4" />
                설정
              </Link>
              <LogoutButton />
            </div>
          </div>

          <div className="mt-5 grid gap-3 md:grid-cols-3">
            <div className="rounded-[1.1rem] border border-black/8 bg-white p-4">
              <p className="text-sm text-black/45">전체 공연</p>
              <p className="mt-2 text-[1.65rem] font-semibold tracking-[-0.04em] text-black">
                {totalShows}
              </p>
            </div>
            <div className="rounded-[1.1rem] border border-black/8 bg-white p-4">
              <p className="text-sm text-black/45">진행 중 공연</p>
              <p className="mt-2 text-[1.65rem] font-semibold tracking-[-0.04em] text-black">
                {liveShowsCount}
              </p>
            </div>
            <div className="rounded-[1.1rem] border border-black/8 bg-white p-4">
              <div className="flex items-center gap-2 text-sm text-black/45">
                <Wallet className="size-4" />
                지갑 잔액
              </div>
              <p className="mt-2 text-[1.65rem] font-semibold tracking-[-0.04em] text-black">
                {company?.balance !== null && company?.balance !== undefined
                  ? `${Number(company.balance).toLocaleString()} SSF`
                  : "-"}
              </p>
            </div>
          </div>
        </section>

        <div className="mt-6 flex gap-6 border-b border-black/10">
          <button
            onClick={() => setActiveTab("shows")}
            className={`pb-3 text-sm font-semibold transition-colors ${
              activeTab === "shows"
                ? "border-b-2 border-black text-black"
                : "text-black/40 hover:text-black/70"
            }`}
          >
            공연 목록
          </button>
          <button
            onClick={() => setActiveTab("info")}
            className={`pb-3 text-sm font-semibold transition-colors ${
              activeTab === "info"
                ? "border-b-2 border-black text-black"
                : "text-black/40 hover:text-black/70"
            }`}
          >
            회사 정보
          </button>
        </div>

        {activeTab === "shows" ? (
          <section className="mt-6">
            <div className="flex flex-col gap-3">
              <div className="flex items-center justify-end gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage((previous) => Math.max(previous - 1, 0))}
                  disabled={isShowsLoading || isFirstPage}
                  className="rounded-full border-black/10 bg-white px-4"
                >
                  이전
                </Button>
                <div className="min-w-16 text-center text-sm text-black/45">
                  {totalPages > 0 ? `${currentPage + 1} / ${totalPages}` : "-"}
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() =>
                    setCurrentPage((previous) =>
                      totalPages > 0 ? Math.min(previous + 1, totalPages - 1) : previous
                    )
                  }
                  disabled={isShowsLoading || isLastPage}
                  className="rounded-full border-black/10 bg-white px-4"
                >
                  다음
                </Button>
              </div>

              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex flex-1 flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
                  <div className="flex flex-wrap gap-2">
                    {filterOptions.map((option) => {
                      const active = showFilter === option.key

                      return (
                        <button
                          key={option.key}
                          type="button"
                          onClick={() => setShowFilter(option.key)}
                          className={`inline-flex items-center gap-2 rounded-full border px-3.5 py-2 text-sm font-medium transition-colors ${
                            active
                              ? "border-black bg-black text-white"
                              : "border-black/10 bg-white text-black/62 hover:bg-black/[0.03]"
                          }`}
                        >
                          <span>{option.label}</span>
                          <span
                            className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${
                              active ? "bg-white/14 text-white" : "bg-black/[0.05] text-black/56"
                            }`}
                          >
                            {option.count}
                          </span>
                        </button>
                      )
                    })}
                  </div>

                  <div className="w-full lg:w-72 lg:min-w-72">
                    <Input
                      value={searchQuery}
                      onChange={(event) => setSearchQuery(event.target.value)}
                      placeholder="공연 제목 검색"
                      className="h-10 rounded-full border-black/10 bg-white px-4"
                    />
                  </div>
                </div>
              </div>
            </div>

            {visibleShows.length > 0 ? (
              <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
                {visibleShows.map((event) => (
                  <EventCard key={event.showId} event={event} />
                ))}
              </div>
            ) : (
              <Card className="mt-4 rounded-[1.35rem] border-black/8 bg-white shadow-none">
                <CardContent className="py-8 text-center text-sm text-black/50">
                  {isShowsLoading
                    ? "공연 목록을 불러오는 중입니다."
                    : hasShowsLoadError
                      ? "공연 목록을 불러오지 못했습니다."
                      : "조건에 맞는 공연이 없습니다."}
                </CardContent>
              </Card>
            )}
          </section>
        ) : (
          <section className="mt-6">
            {company ? (
              <CompanyInfoCard company={company} />
            ) : (
              <Card className="rounded-[1.35rem] border-black/8 bg-white shadow-none">
                <CardContent className="py-8 text-center text-sm text-black/50">
                  {isSummaryLoading
                    ? "회사 정보를 불러오는 중입니다."
                    : hasSummaryLoadError
                      ? "회사 정보를 불러오지 못했습니다."
                      : "회사 정보가 없습니다."}
                </CardContent>
              </Card>
            )}
          </section>
        )}
      </div>
    </main>
  )
}
