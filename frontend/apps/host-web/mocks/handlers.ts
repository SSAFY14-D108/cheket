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
]

function isAuthorized(request: Request) {
  return Boolean(request.headers.get("Authorization"))
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
