import { http, HttpResponse } from "msw"
import { getMockHostPassword } from "@/mocks/data/auth-store"
import { MY_PAGE_COMPANY, MY_WALLET_BALANCE } from "@/mocks/data/mypage-store"
import { myPageShowsStore } from "@/mocks/data/show-store"
import { createUnauthorizedResponse, isAuthorized } from "./utils"

export const mypageHandlers = [
  http.get("*/api/v1/hosts", async ({ request }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
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
      return createUnauthorizedResponse()
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
      return createUnauthorizedResponse()
    }

    const body = (await request.json()) as { password?: string }

    if (body.password !== getMockHostPassword()) {
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
      return createUnauthorizedResponse()
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
      return createUnauthorizedResponse()
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
]
