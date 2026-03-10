"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { ApiError } from "@/lib/api"
import { fetchShowDetail, type HostShowDetail } from "@/lib/show-manage-api"
import { ShowDetailView } from "./ShowDetailView"

interface ShowDetailContentProps {
  showId: string
}

export function ShowDetailContent({ showId }: ShowDetailContentProps) {
  const [showDetail, setShowDetail] = useState<HostShowDetail | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  useEffect(() => {
    let isCancelled = false

    async function loadShowDetail() {
      try {
        const detail = await fetchShowDetail(showId)

        if (!isCancelled) {
          setShowDetail(detail)
        }
      } catch (error) {
        if (!isCancelled) {
          setErrorMessage(
            error instanceof ApiError
              ? error.message
              : "공연 상세 정보를 불러오지 못했습니다."
          )
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadShowDetail()

    return () => {
      isCancelled = true
    }
  }, [showId])

  if (isLoading) {
    return (
      <main className="mx-auto max-w-5xl px-6 py-10">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">공연 정보를 불러오는 중입니다.</CardTitle>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            잠시만 기다려주세요.
          </CardContent>
        </Card>
      </main>
    )
  }

  if (!showDetail) {
    return (
      <main className="mx-auto max-w-5xl px-6 py-10">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">공연 정보를 불러오지 못했습니다.</CardTitle>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            {errorMessage ?? "잠시 후 다시 시도해주세요."}
          </CardContent>
        </Card>
      </main>
    )
  }

  return <ShowDetailView showDetail={showDetail} />
}
