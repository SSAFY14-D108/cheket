"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { CompanyInfoCard } from "@/components/mypage/CompanyInfoCard"
import { EventCard } from "@/components/mypage/EventCard"
import { LogoutButton } from "@/components/mypage/LogoutButton"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useToast } from "@/hooks/use-toast"
import { ApiError } from "@/lib/api"
import { compareShowsByDisplayOrder } from "@/lib/show-display"
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

export function MyPageContent() {
  const { toast } = useToast()
  const [company, setCompany] = useState<MyCompanyInfo | null>(null)
  const [showsPage, setShowsPage] = useState<MyShowsPage | null>(null)
  const [isSummaryLoading, setIsSummaryLoading] = useState(true)
  const [isShowsLoading, setIsShowsLoading] = useState(true)
  const [hasSummaryLoadError, setHasSummaryLoadError] = useState(false)
  const [hasShowsLoadError, setHasShowsLoadError] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)

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

        if (isCancelled) {
          return
        }

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
              title: "지갑 정보 조회 실패",
              description:
                walletError instanceof ApiError
                  ? walletError.message
                  : "지갑 잔액 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.",
              variant: "destructive",
            })
          }
        }

        setCompany(nextCompany)
      } catch (error) {
        if (isCancelled) {
          return
        }

        if (!(error instanceof ApiError && error.status === 401)) {
          setHasSummaryLoadError(true)

          toast({
            title: "마이페이지 조회 실패",
            description:
              error instanceof ApiError
                ? error.message
                : "마이페이지 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.",
            variant: "destructive",
          })
        }
      } finally {
        if (!isCancelled) {
          setIsSummaryLoading(false)
        }
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

        if (isCancelled) {
          return
        }

        setShowsPage(nextShowsPage)
      } catch (error) {
        if (isCancelled) {
          return
        }

        if (!(error instanceof ApiError && error.status === 401)) {
          setHasShowsLoadError(true)

          toast({
            title: "공연 목록 조회 실패",
            description:
              error instanceof ApiError
                ? error.message
                : "등록한 공연 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.",
            variant: "destructive",
          })
        }
      } finally {
        if (!isCancelled) {
          setIsShowsLoading(false)
        }
      }
    }

    void loadShows()

    return () => {
      isCancelled = true
    }
  }, [toast])

  const allShows: MyShowSummary[] = [...(showsPage?.shows ?? [])].sort(compareShowsByDisplayOrder)
  const totalShows = showsPage?.totalElements ?? 0
  const totalPages = Math.ceil(allShows.length / DISPLAY_SHOWS_PAGE_SIZE)
  const isFirstPage = currentPage === 0
  const isLastPage = totalPages === 0 || currentPage >= totalPages - 1
  const visibleShows = allShows.slice(
    currentPage * DISPLAY_SHOWS_PAGE_SIZE,
    (currentPage + 1) * DISPLAY_SHOWS_PAGE_SIZE
  )

  useEffect(() => {
    if (totalPages > 0 && currentPage > totalPages - 1) {
      setCurrentPage(totalPages - 1)
    }
  }, [currentPage, totalPages])

  return (
    <main className="mx-auto max-w-6xl px-6 py-10">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-foreground">마이페이지</h1>
        <div className="flex gap-3">
          <Link
            href="/shows/create"
            className="rounded-sm bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
          >
            공연 등록
          </Link>
          <LogoutButton />
        </div>
      </div>

      <section className="mt-8">
        <div className="mb-3 flex items-center justify-end">
          <Link
            href="/mypage/settings"
            className="rounded-sm border border-input bg-background px-3 py-2 text-sm font-medium transition-colors hover:bg-accent hover:text-accent-foreground"
          >
            계정 설정
          </Link>
        </div>
        {company ? (
          <CompanyInfoCard company={company} />
        ) : (
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">회사 정보</CardTitle>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">
              {isSummaryLoading
                ? "회사 정보를 불러오는 중입니다."
                : hasSummaryLoadError
                  ? "회사 정보를 불러오지 못했습니다."
                  : "회사 정보가 없습니다."}
            </CardContent>
          </Card>
        )}
      </section>

      <section className="mt-10">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-lg font-semibold text-foreground">등록한 공연</h2>
            <p className="text-sm text-muted-foreground">
              총 {totalShows}개의 공연이 등록되어 있습니다.
            </p>
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCurrentPage((previous) => Math.max(previous - 1, 0))}
              disabled={isShowsLoading || isFirstPage}
            >
              이전
            </Button>
            <div className="min-w-24 text-center text-sm text-muted-foreground">
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
            >
              다음
            </Button>
          </div>
        </div>
        {visibleShows.length > 0 ? (
          <div className="mt-4 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {visibleShows.map((event) => (
              <EventCard key={event.showId} event={event} />
            ))}
          </div>
        ) : (
          <Card className="mt-4">
            <CardContent className="pt-6 text-sm text-muted-foreground">
              {isShowsLoading
                ? "공연 목록을 불러오는 중입니다."
                : hasShowsLoadError
                  ? "공연 목록을 불러오지 못했습니다."
                  : "아직 등록한 공연이 없습니다."}
            </CardContent>
          </Card>
        )}
      </section>
    </main>
  )
}
