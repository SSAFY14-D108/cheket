import { http, HttpResponse } from "msw"
import { getDashboardSnapshot } from "@/mocks/data/dashboard-store"
import { createNotFoundResponse, createUnauthorizedResponse, isAuthorized } from "./utils"

export const dashboardHandlers = [
  http.get("*/api/v1/hosts/shows/:showId/dashboard/reservations", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    const snapshot = getDashboardSnapshot(params.showId)

    if (!snapshot) {
      return createNotFoundResponse()
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "회차별 예매 현황 조회 완료",
        data: snapshot.reservations,
      },
      { status: 200 }
    )
  }),
  http.get("*/api/v1/hosts/shows/:showId/dashboard/revenue-split", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    const snapshot = getDashboardSnapshot(params.showId)

    if (!snapshot) {
      return createNotFoundResponse()
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "수입 배분 조회 완료",
        data: snapshot.revenueSplit,
      },
      { status: 200 }
    )
  }),
  http.get("*/api/v1/hosts/shows/:showId/dashboard/booking-rate", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    const snapshot = getDashboardSnapshot(params.showId)

    if (!snapshot) {
      return createNotFoundResponse()
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "예매율 조회 완료",
        data: snapshot.bookingRate,
      },
      { status: 200 }
    )
  }),
  http.get("*/api/v1/hosts/shows/:showId/dashboard/total-sales", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    const snapshot = getDashboardSnapshot(params.showId)

    if (!snapshot) {
      return createNotFoundResponse()
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "총 판매금액 조회 완료",
        data: snapshot.totalSales,
      },
      { status: 200 }
    )
  }),
]
