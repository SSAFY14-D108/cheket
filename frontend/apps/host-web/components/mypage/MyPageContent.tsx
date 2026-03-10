"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { CompanyInfoCard } from "@/components/mypage/CompanyInfoCard"
import { EventCard } from "@/components/mypage/EventCard"
import { LogoutButton } from "@/components/mypage/LogoutButton"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useToast } from "@/hooks/use-toast"
import { ApiError } from "@/lib/api"
import {
  fetchMyCompanyInfo,
  fetchMyShows,
  fetchMyWalletBalance,
  type MyCompanyInfo,
  type MyShowSummary,
  type MyWalletBalance,
} from "@/lib/mypage-api"

export function MyPageContent() {
  const { toast } = useToast()
  const [company, setCompany] = useState<MyCompanyInfo | null>(null)
  const [shows, setShows] = useState<MyShowSummary[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [hasLoadError, setHasLoadError] = useState(false)

  useEffect(() => {
    let isCancelled = false

    async function loadMyPage() {
      try {
        const [companyInfo, showsPage, walletResult] = await Promise.all([
          fetchMyCompanyInfo(),
          fetchMyShows({ page: 0, size: 20 }),
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
        setShows(showsPage.shows)
      } catch (error) {
        if (isCancelled) {
          return
        }

        if (!(error instanceof ApiError && error.status === 401)) {
          setHasLoadError(true)

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
          setIsLoading(false)
        }
      }
    }

    void loadMyPage()

    return () => {
      isCancelled = true
    }
  }, [toast])

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
        {company ? (
          <CompanyInfoCard company={company} />
        ) : (
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">회사 정보</CardTitle>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">
              {isLoading
                ? "회사 정보를 불러오는 중입니다."
                : "회사 정보를 불러오지 못했습니다."}
            </CardContent>
          </Card>
        )}
      </section>

      <section className="mt-10">
        <h2 className="text-lg font-semibold text-foreground">등록한 공연</h2>
        {shows.length > 0 ? (
          <div className="mt-4 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {shows.map((event) => (
              <EventCard key={event.showId} event={event} />
            ))}
          </div>
        ) : (
          <Card className="mt-4">
            <CardContent className="pt-6 text-sm text-muted-foreground">
              {isLoading
                ? "공연 목록을 불러오는 중입니다."
                : hasLoadError
                  ? "공연 목록을 불러오지 못했습니다."
                  : "아직 등록한 공연이 없습니다."}
            </CardContent>
          </Card>
        )}
      </section>
    </main>
  )
}
