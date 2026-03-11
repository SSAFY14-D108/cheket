import { http, HttpResponse } from "msw"
import { mockEvents, mockSectionsByVenue, mockTicketEffects, mockVenues } from "@/lib/mock-data"

const MOCK_ACCESS_TOKEN = "mock-token-1234"
const MOCK_REFRESH_TOKEN = "mock-refresh-token-5678"
const MOCK_NEW_ACCESS_TOKEN = "mock-access-token-new"
const MOCK_NEW_REFRESH_TOKEN = "mock-refresh-token-new"
const MOCK_HOST_EMAIL = "ssafy@gmail.com"
let MOCK_HOST_PASSWORD = "1qa2ws3ed!!"
const MY_PAGE_COMPANY = {
  companyName: "스타라이트 엔터테인먼트",
  businessNo: "123-45-67890",
  email: "ssafy@gmail.com",
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
const INITIAL_MY_PAGE_SHOWS = [
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

function cloneMockEvent(event: (typeof mockEvents)[number]) {
  return {
    ...event,
    venue: { ...event.venue },
    show: { ...event.show },
    reservation: { ...event.reservation },
    grade: event.grade.map((grade) => ({ ...grade })),
    stakeholders: event.stakeholders.map((stakeholder) => ({ ...stakeholder })),
    refundPolicy: event.refundPolicy.map((policy) => ({ ...policy })),
    sessionInfo: event.sessionInfo.map((session) => ({ ...session })),
  }
}

let mockEventStore = mockEvents.map(cloneMockEvent)
let myPageShowsStore = INITIAL_MY_PAGE_SHOWS.map((show) => ({
  ...show,
  show: { ...show.show },
  reservation: { ...show.reservation },
}))

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

function getShowDetailSnapshot(showIdValue: string | readonly string[] | undefined) {
  const showId = Number(showIdValue)

  if (!Number.isInteger(showId)) {
    return null
  }

  const event = mockEventStore.find((item) => item.showId === showId)

  if (!event) {
    return null
  }

  return {
    showId: event.showId,
    title: event.title,
    posterUrl: event.posterUrl,
    artistName: event.artistName,
    venue: {
      venueId: event.venue.venueId,
      name: event.venue.name,
      address: event.venue.address,
    },
    show: {
      showStartDate: event.show.startAt.slice(0, 10),
      showEndDate: event.show.endAt.slice(0, 10),
    },
    reservation: {
      startDate: event.reservation.openAt,
      endDate: event.reservation.closeAt,
    },
    description: event.description,
    purchaseLimit: event.purchaseLimit,
    likeCount: event.likes,
    grade: event.grade,
    stakeholders: event.stakeholders.map((stakeholder, index) => ({
      role: stakeholder.role,
      userId: 15 + index,
      shareBps: stakeholder.shareBps,
      name: stakeholder.name,
    })),
    refundPolicy: event.refundPolicy,
    sessionInfo: event.sessionInfo,
    status: event.status,
    createdAt: event.createdAt,
    updatedAt: event.updatedAt,
    ticketEffectId: 1,
  }
}

function getNextShowId() {
  const currentMaxShowId = mockEventStore.reduce(
    (maxShowId, event) => Math.max(maxShowId, event.showId),
    0
  )

  return currentMaxShowId + 1
}

function getVenueAddress(venueId: number) {
  return (
    mockEventStore.find((event) => event.venue.venueId === venueId)?.venue.address ??
    "주소 정보 없음"
  )
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
  http.post("*/api/v1/hosts/business-no/duplicate", async ({ request }) => {
    const body = (await request.json()) as { businessNo?: string }
    const businessNo = body.businessNo?.trim() ?? ""
    const businessNoPattern = /^\d{3}-\d{2}-\d{5}$/

    if (!businessNoPattern.test(businessNo)) {
      return HttpResponse.json(
        {
          httpStatusCode: 400,
          errorMessage: "잘못된 사업자번호 형식입니다.",
        },
        { status: 400 }
      )
    }

    if (businessNo === "000-00-00000") {
      return HttpResponse.json(
        {
          httpStatusCode: 409,
          errorMessage: "이미 등록된 사업자번호입니다.",
          data: {
            isDuplicate: true,
          },
        },
        { status: 409 }
      )
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "사용 가능한 사업자번호입니다.",
        data: {
          isDuplicate: false,
        },
      },
      { status: 200 }
    )
  }),
  // 회원가입
  http.post("*/api/v1/hosts", async ({ request }) => {
    const body = (await request.json()) as { password?: string }

    if (body.password?.trim()) {
      MOCK_HOST_PASSWORD = body.password
    }

    return HttpResponse.json(
      {
        httpStatusCode: 201,
        responseMessage: "회원 가입이 완료되었습니다.",
      },
      { status: 201 }
    )
  }),
  // 로그인
  http.post("*/api/v1/hosts/login", async ({ request }) => {
    const body = (await request.json()) as { email?: string; password?: string }
    const email = body.email?.trim() ?? ""
    const password = body.password?.trim() ?? ""

    if (!email || !password) {
      return HttpResponse.json(
        {
          httpStatusCode: 400,
          errorMessage: "이메일과 비밀번호를 입력해주세요.",
        },
        { status: 400 }
      )
    }

    if (email !== MOCK_HOST_EMAIL || password !== MOCK_HOST_PASSWORD) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "이메일 또는 비밀번호가 일치하지 않습니다.",
        },
        { status: 401 }
      )
    }

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
  http.put("*/api/v1/hosts", async ({ request }) => {
    if (!isAuthorized(request)) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
    }

    const body = (await request.json()) as {
      companyName?: string
      email?: string
    }

    const companyName = body.companyName?.trim()
    const email = body.email?.trim()

    if (!companyName && !email) {
      return HttpResponse.json(
        {
          httpStatusCode: 400,
          errorMessage: "잘못된 요청입니다.",
        },
        { status: 400 }
      )
    }

    if (companyName) {
      MY_PAGE_COMPANY.companyName = companyName
    }

    if (email) {
      MY_PAGE_COMPANY.email = email
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "회사 정보가 수정되었습니다.",
      },
      { status: 200 }
    )
  }),
  http.delete("*/api/v1/hosts", async ({ request }) => {
    if (!isAuthorized(request)) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
    }

    const body = (await request.json()) as { password?: string }

    if (body.password !== MOCK_HOST_PASSWORD) {
      return HttpResponse.json(
        {
          httpStatusCode: 400,
          errorMessage: "비밀번호가 일치하지 않습니다.",
        },
        { status: 400 }
      )
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "회원 탈퇴가 완료되었습니다.",
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
    const pagedShows = myPageShowsStore.slice(startIndex, startIndex + size)

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "내 공연 목록 조회 완료",
        data: {
          shows: pagedShows,
          page,
          size,
          totalElements: myPageShowsStore.length,
          totalPages: Math.ceil(myPageShowsStore.length / size) || 1,
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
  http.get("*/api/v1/hosts/shows/effect", async ({ request }) => {
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
        responseMessage: "티켓 효과 목록 조회 완료",
        data: mockTicketEffects,
      },
      { status: 200 }
    )
  }),
  http.get("*/api/v1/hosts/shows/:venueId/sections", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
    }

    const venueId = Number(params.venueId)

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "구역 목록 조회 완료",
        data: mockSectionsByVenue[venueId] ?? [],
      },
      { status: 200 }
    )
  }),
  http.get("*/api/v1/hosts/shows/:showId", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
    }

    const snapshot = getShowDetailSnapshot(params.showId)

    if (!snapshot) {
      return createNotFoundResponse()
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "공연 상세 조회 성공",
        data: snapshot,
      },
      { status: 200 }
    )
  }),
  http.post("*/api/v1/hosts/shows", async ({ request }) => {
    if (!isAuthorized(request)) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
    }

    const body = (await request.json()) as {
      title?: string
      posterUrl?: string
      venueId?: number
      artistName?: string
      show?: { startAt?: string; endAt?: string }
      reservation?: { openAt?: string; closeAt?: string }
      description?: string
      purchaseLimit?: number
      grade?: Array<{
        sectionId?: number[]
        gradeName?: string
        price?: number
        colorCode?: string
      }>
      stakeholders?: Array<{
        role?: "organizer" | "artist"
        shareBps?: number
      }>
      refundPolicy?: Array<{
        daysRemaining?: number
        refundRate?: number
      }>
      sessionInfo?: Array<{
        sessionId?: number
        sessionDate?: string
        sessionStartDate?: string
      }>
    }

    if (!body.title || !body.venueId) {
      return HttpResponse.json(
        {
          httpStatusCode: 400,
          errorMessage: "필수 입력값이 누락되었습니다.",
        },
        { status: 400 }
      )
    }

    const nextShowId = getNextShowId()
    const selectedVenue = mockVenues.find((venue) => venue.venueId === body.venueId)
    const venueName = selectedVenue?.name ?? `공연장 ${body.venueId}`
    const newShowListItem = {
      showId: nextShowId,
      title: body.title,
      posterUrl: body.posterUrl ?? "/images/poster-1.jpg",
      venue: venueName,
      purchaseLimit: body.purchaseLimit ?? 1,
      region: "SEOUL",
      show: {
        showStartDate: body.show?.startAt?.slice(0, 10) ?? "2026-01-01",
        showEndDate: body.show?.endAt?.slice(0, 10) ?? body.show?.startAt?.slice(0, 10) ?? "2026-01-01",
      },
      reservation: {
        startDate: body.reservation?.openAt ?? "2026-01-01T10:00:00",
        endDate: body.reservation?.closeAt ?? "2026-01-02T10:00:00",
      },
      status: "DRAFT",
    }
    const newEvent = {
      showId: nextShowId,
      title: body.title,
      artistName: body.artistName ?? "미정",
      posterUrl: body.posterUrl ?? "/images/poster-1.jpg",
      venue: {
        venueId: body.venueId,
        name: venueName,
        address: getVenueAddress(body.venueId),
      },
      show: {
        startAt: body.show?.startAt ?? "2026-01-01T19:00:00",
        endAt: body.show?.endAt ?? "2026-01-01T21:00:00",
      },
      reservation: {
        openAt: body.reservation?.openAt ?? "2026-01-01T10:00:00",
        closeAt: body.reservation?.closeAt ?? "2026-01-02T10:00:00",
      },
      description: body.description ?? "",
      purchaseLimit: body.purchaseLimit ?? 1,
      grade:
        body.grade?.map((grade) => ({
          sectionId: grade.sectionId?.[0] ?? 1,
          gradeName: grade.gradeName ?? "일반",
          price: grade.price ?? 0,
          colorCode: grade.colorCode ?? "#7C6EF0",
        })) ?? [],
      stakeholders:
        body.stakeholders?.map((stakeholder, index) => ({
          role: stakeholder.role ?? "artist",
          name: stakeholder.role === "organizer" ? `주최측 ${index + 1}` : `아티스트 ${index + 1}`,
          shareBps: stakeholder.shareBps ?? 0,
        })) ?? [],
      refundPolicy:
        body.refundPolicy?.map((policy) => ({
          daysRemaining: policy.daysRemaining ?? 0,
          refundRate: policy.refundRate ?? 0,
        })) ?? [],
      sessionInfo:
        body.sessionInfo?.map((session, index) => ({
          sessionId: session.sessionId ?? index + 1,
          sessionDate: session.sessionDate ?? "2026-01-01",
          sessionStartDate: session.sessionStartDate ?? "19:00",
          capacity: selectedVenue?.capacity ?? 1000,
        })) ?? [],
      status: "DRAFT",
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      capacity: selectedVenue?.capacity ?? 1000,
      soldSeats: 0,
      enteredCount: 0,
      notEnteredCount: 0,
      emptyCount: selectedVenue?.capacity ?? 1000,
      likes: 0,
    }

    myPageShowsStore.push(newShowListItem)
    mockEventStore.push(newEvent)

    return HttpResponse.json(
      {
        httpStatusCode: 201,
        responseMessage: "공연 등록 완료",
        data: {
          showId: nextShowId,
        },
      },
      { status: 201 }
    )
  }),
  http.put("*/api/v1/hosts/shows/:showId", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
    }

    const snapshot = getShowDetailSnapshot(params.showId)

    if (!snapshot) {
      return createNotFoundResponse()
    }

    const body = (await request.json()) as {
      title?: string
      posterUrl?: string
      artistName?: string
      venueId?: number
      show?: { startAt?: string; endAt?: string }
      reservation?: { openAt?: string; closeAt?: string }
      description?: string
      purchaseLimit?: number
    }

    if (
      !body.title &&
      !body.posterUrl &&
      !body.artistName &&
      !body.venueId &&
      !body.show?.startAt &&
      !body.show?.endAt &&
      !body.reservation?.openAt &&
      !body.reservation?.closeAt &&
      !body.description &&
      body.purchaseLimit === undefined
    ) {
      return HttpResponse.json(
        {
          httpStatusCode: 400,
          errorMessage: "수정할 정보가 없습니다.",
        },
        { status: 400 }
      )
    }

    const showId = Number(params.showId)
    const eventIndex = mockEventStore.findIndex((event) => event.showId === showId)
    const myPageShowIndex = myPageShowsStore.findIndex((show) => show.showId === showId)

    if (eventIndex >= 0) {
      const previousEvent = mockEventStore[eventIndex]
      const nextVenueId = body.venueId ?? previousEvent.venue.venueId
      const selectedVenue = mockVenues.find((venue) => venue.venueId === nextVenueId)

      mockEventStore[eventIndex] = {
        ...previousEvent,
        title: body.title ?? previousEvent.title,
        artistName: body.artistName ?? previousEvent.artistName,
        posterUrl: body.posterUrl ?? previousEvent.posterUrl,
        venue: {
          venueId: nextVenueId,
          name: selectedVenue?.name ?? previousEvent.venue.name,
          address: selectedVenue ? getVenueAddress(nextVenueId) : previousEvent.venue.address,
        },
        show: {
          startAt: body.show?.startAt ?? previousEvent.show.startAt,
          endAt: body.show?.endAt ?? previousEvent.show.endAt,
        },
        reservation: {
          openAt: body.reservation?.openAt ?? previousEvent.reservation.openAt,
          closeAt: body.reservation?.closeAt ?? previousEvent.reservation.closeAt,
        },
        description: body.description ?? previousEvent.description,
        purchaseLimit: body.purchaseLimit ?? previousEvent.purchaseLimit,
        updatedAt: new Date().toISOString(),
      }
    }

    if (myPageShowIndex >= 0) {
      const previousShow = myPageShowsStore[myPageShowIndex]
      const nextVenueId = body.venueId ?? mockEventStore[eventIndex]?.venue.venueId
      const selectedVenue = mockVenues.find((venue) => venue.venueId === nextVenueId)

      myPageShowsStore[myPageShowIndex] = {
        ...previousShow,
        title: body.title ?? previousShow.title,
        posterUrl: body.posterUrl ?? previousShow.posterUrl,
        venue: selectedVenue?.name ?? previousShow.venue,
        purchaseLimit: body.purchaseLimit ?? previousShow.purchaseLimit,
        show: {
          showStartDate: body.show?.startAt?.slice(0, 10) ?? previousShow.show.showStartDate,
          showEndDate: body.show?.endAt?.slice(0, 10) ?? previousShow.show.showEndDate,
        },
        reservation: {
          startDate: body.reservation?.openAt ?? previousShow.reservation.startDate,
          endDate: body.reservation?.closeAt ?? previousShow.reservation.endDate,
        },
      }
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "수정이 완료되었습니다.",
      },
      { status: 200 }
    )
  }),
  http.delete("*/api/v1/hosts/shows/:showId", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          errorMessage: "로그인이 필요합니다.",
        },
        { status: 401 }
      )
    }

    const snapshot = getShowDetailSnapshot(params.showId)

    if (!snapshot) {
      return createNotFoundResponse()
    }

    const showId = Number(params.showId)

    myPageShowsStore = myPageShowsStore.filter((show) => show.showId !== showId)
    mockEventStore = mockEventStore.filter((event) => event.showId !== showId)

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "공연 정보 삭제가 완료되었습니다.",
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
