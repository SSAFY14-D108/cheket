import { http, HttpResponse } from "msw"

const MOCK_ACCESS_TOKEN = "mock-token-1234"
const MOCK_REFRESH_TOKEN = "mock-refresh-token-5678"
const MOCK_NEW_ACCESS_TOKEN = "mock-access-token-new"
const MOCK_NEW_REFRESH_TOKEN = "mock-refresh-token-new"
const MY_PAGE_COMPANY = {
  companyName: "스타라이트 엔터테인먼트",
  businessNo: "123-45-67890",
  email: "admin@starlight-ent.com",
}
const MY_WALLET_BALANCE = {
  balance: 350000,
  walletAddress: "0xAb5801a7D398351b8bE11C439e05C5b3259aec9B",
}
const DASHBOARD_DATA_BY_SHOW_ID = {
  42: {
    reservations: {
      showId: 42,
      title: "CHEKET LIVE: Spring Night",
      venue: "올림픽공원 올림픽홀",
      sessions: [
        { sessionId: 1, date: "2026-03-20", capacity: 1200, reservedSeats: 950 },
        { sessionId: 2, date: "2026-03-21", capacity: 1200, reservedSeats: 1050 },
      ],
    },
    revenueSplit: {
      showId: 42,
      title: "CHEKET LIVE: Spring Night",
      totalRevenue: 15.75,
      splits: [
        { role: "소속사", rateBps: 5000, amount: 7.875 },
        { role: "가수", rateBps: 3000, amount: 4.725 },
        { role: "기획자", rateBps: 2000, amount: 3.15 },
      ],
    },
    bookingRate: {
      showId: 42,
      title: "CHEKET LIVE: Spring Night",
      capacity: 2400,
      reservedSeats: 2000,
      bookingRate: 83.3,
    },
    totalSales: {
      showId: 42,
      title: "CHEKET LIVE: Spring Night",
      totalPrimarySales: 375000000,
    },
  },
  43: {
    reservations: {
      showId: 43,
      title: "봄날의 재즈 나이트",
      venue: "블루노트 서울",
      sessions: [
        { sessionId: 3, date: "2026-04-20", capacity: 300, reservedSeats: 280 },
      ],
    },
    revenueSplit: {
      showId: 43,
      title: "봄날의 재즈 나이트",
      totalRevenue: 8.4,
      splits: [
        { role: "주최측", rateBps: 6000, amount: 5.04 },
        { role: "아티스트", rateBps: 4000, amount: 3.36 },
      ],
    },
    bookingRate: {
      showId: 43,
      title: "봄날의 재즈 나이트",
      capacity: 300,
      reservedSeats: 280,
      bookingRate: 93.3,
    },
    totalSales: {
      showId: 43,
      title: "봄날의 재즈 나이트",
      totalPrimarySales: 22400000,
    },
  },
  44: {
    reservations: {
      showId: 44,
      title: "한여름 밤의 인디페스타",
      venue: "난지한강공원",
      sessions: [
        { sessionId: 4, date: "2026-08-15", capacity: 10000, reservedSeats: 7800 },
        { sessionId: 5, date: "2026-08-16", capacity: 10000, reservedSeats: 7200 },
      ],
    },
    revenueSplit: {
      showId: 44,
      title: "한여름 밤의 인디페스타",
      totalRevenue: 42.5,
      splits: [
        { role: "주최측", rateBps: 5500, amount: 23.375 },
        { role: "아티스트", rateBps: 3000, amount: 12.75 },
        { role: "기획", rateBps: 1500, amount: 6.375 },
      ],
    },
    bookingRate: {
      showId: 44,
      title: "한여름 밤의 인디페스타",
      capacity: 20000,
      reservedSeats: 15000,
      bookingRate: 75,
    },
    totalSales: {
      showId: 44,
      title: "한여름 밤의 인디페스타",
      totalPrimarySales: 412500000,
    },
  },
  45: {
    reservations: {
      showId: 45,
      title: "가을 클래식 선율",
      venue: "예술의전당 콘서트홀",
      sessions: [
        { sessionId: 6, date: "2026-10-10", capacity: 1800, reservedSeats: 1120 },
        { sessionId: 7, date: "2026-10-11", capacity: 1800, reservedSeats: 980 },
        { sessionId: 8, date: "2026-10-12", capacity: 1800, reservedSeats: 860 },
      ],
    },
    revenueSplit: {
      showId: 45,
      title: "가을 클래식 선율",
      totalRevenue: 18.2,
      splits: [
        { role: "주최측", rateBps: 5000, amount: 9.1 },
        { role: "오케스트라", rateBps: 3500, amount: 6.37 },
        { role: "지휘", rateBps: 1500, amount: 2.73 },
      ],
    },
    bookingRate: {
      showId: 45,
      title: "가을 클래식 선율",
      capacity: 5400,
      reservedSeats: 2960,
      bookingRate: 54.8,
    },
    totalSales: {
      showId: 45,
      title: "가을 클래식 선율",
      totalPrimarySales: 162800000,
    },
  },
} as const
const MY_PAGE_SHOWS = [
  {
    showId: 42,
    title: "CHEKET LIVE: Spring Night",
    posterUrl: "/images/poster-1.jpg",
    venue: "올림픽공원 올림픽홀",
    purchaseLimit: 2,
    region: "SEOUL",
    show: {
      showStartDate: "2026-03-20",
      showEndDate: "2026-03-28",
    },
    reservation: {
      startDate: "2026-03-05T12:00:00",
      endDate: "2026-03-20T18:00:00",
    },
    status: "DRAFT",
  },
  {
    showId: 43,
    title: "봄날의 재즈 나이트",
    posterUrl: "/images/poster-2.jpg",
    venue: "블루노트 서울",
    purchaseLimit: 4,
    region: "SEOUL",
    show: {
      showStartDate: "2026-04-20",
      showEndDate: "2026-04-20",
    },
    reservation: {
      startDate: "2026-03-15T14:00:00",
      endDate: "2026-04-19T23:59:59",
    },
    status: "UPCOMING",
  },
  {
    showId: 44,
    title: "한여름 밤의 인디페스타",
    posterUrl: "/images/poster-3.jpg",
    venue: "난지한강공원",
    purchaseLimit: 4,
    region: "SEOUL",
    show: {
      showStartDate: "2026-08-15",
      showEndDate: "2026-08-16",
    },
    reservation: {
      startDate: "2026-07-20T12:00:00",
      endDate: "2026-08-14T23:59:59",
    },
    status: "TICKETING",
  },
  {
    showId: 45,
    title: "가을 클래식 선율",
    posterUrl: "/images/poster-4.jpg",
    venue: "예술의전당 콘서트홀",
    purchaseLimit: 2,
    region: "SEOUL",
    show: {
      showStartDate: "2026-10-10",
      showEndDate: "2026-10-12",
    },
    reservation: {
      startDate: "2026-09-01T10:00:00",
      endDate: "2026-10-09T18:00:00",
    },
    status: "UPCOMING",
  },
]

function isAuthorized(request: Request) {
  return Boolean(request.headers.get("Authorization"))
}

function getDashboardSnapshot(showIdValue: string | readonly string[] | undefined) {
  const showId = Number(showIdValue)

  if (!Number.isInteger(showId)) {
    return null
  }

  return DASHBOARD_DATA_BY_SHOW_ID[showId as keyof typeof DASHBOARD_DATA_BY_SHOW_ID] ?? null
}

function createNotFoundResponse() {
  return HttpResponse.json(
    {
      httpStatusCode: 404,
      errorMessage: "존재하지 않는 공연입니다.",
    },
    { status: 404 }
  )
}

export const handlers = [
  // 회원가입
  http.post("*/api/v1/hosts", async () => {
    return HttpResponse.json(
      {
        httpStatusCode: 201,
        responseMessage: "회원 가입이 완료되었습니다.",
      },
      { status: 201 }
    )
  }),
  // 로그인
  http.post("*/api/v1/hosts/login", async () => {
    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "로그인 성공",
        data: {
          accessToken: MOCK_ACCESS_TOKEN,
          refreshToken: MOCK_REFRESH_TOKEN,
        },
      },
      { status: 200 }
    )
  }),
  http.post("*/api/v1/hosts/logout", async () => {
    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "로그아웃 완료",
      },
      { status: 200 }
    )
  }),
  http.get("*/api/v1/hosts", async ({ request }) => {
    if (!isAuthorized(request)) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "조회에 성공했습니다.",
        data: MY_PAGE_COMPANY,
      },
      { status: 200 }
    )
  }),
  http.get("*/api/v1/hosts/shows", async ({ request }) => {
    if (!isAuthorized(request)) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
    }

    const url = new URL(request.url)
    const page = Number(url.searchParams.get("page") ?? "0")
    const size = Number(url.searchParams.get("size") ?? "20")
    const startIndex = page * size
    const pagedShows = MY_PAGE_SHOWS.slice(startIndex, startIndex + size)

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "내 공연 목록 조회 완료",
        data: {
          shows: pagedShows,
          page,
          size,
          totalElements: MY_PAGE_SHOWS.length,
          totalPages: Math.ceil(MY_PAGE_SHOWS.length / size) || 1,
        },
      },
      { status: 200 }
    )
  }),
  http.get("*/api/v1/wallets/balance", async ({ request }) => {
    if (!isAuthorized(request)) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "잔액 조회 성공",
        data: MY_WALLET_BALANCE,
      },
      { status: 200 }
    )
  }),
  http.get("*/api/v1/hosts/shows/:showId/dashboard/reservations", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
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
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
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
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
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
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
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
  http.post("*/api/v1/auth/reissue", async ({ request }) => {
    const body = (await request.json()) as { refreshToken?: string }

    if (!body.refreshToken) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "유효하지 않은 Refresh Token입니다.",
        },
        { status: 401 }
      )
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "토큰 재발급 성공",
        data: {
          accessToken: MOCK_NEW_ACCESS_TOKEN,
          refreshToken: MOCK_NEW_REFRESH_TOKEN,
        },
      },
      { status: 200 }
    )
  }),
]
