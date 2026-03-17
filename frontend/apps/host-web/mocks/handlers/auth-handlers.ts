import { http, HttpResponse } from "msw"
import {
  getMockHostBusinessNo,
  getMockHostEmail,
  getMockHostPassword,
  MOCK_ACCESS_TOKEN,
  MOCK_NEW_ACCESS_TOKEN,
  MOCK_NEW_REFRESH_TOKEN,
  MOCK_REFRESH_TOKEN,
  registerMockHost,
} from "@/mocks/data/auth-store"
import { mockAuthUsers } from "@/mocks/data/user-store"
import { createUnauthorizedResponse, isAuthorized } from "./utils"

export const authHandlers = [
  http.get("*/api/v1/auth/search", async ({ request }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    const url = new URL(request.url)
    const userType = url.searchParams.get("userType")
    const number = url.searchParams.get("number")?.trim() ?? ""
    const normalizedNumber = number.replace(/-/g, "")

    const foundUser = mockAuthUsers.find((user) => {
      if (userType === "HOST") {
        return user.businessNo?.replace(/-/g, "") === normalizedNumber
      }

      if (userType === "USER") {
        return user.phone.replace(/-/g, "") === normalizedNumber
      }

      return false
    })

    if (!foundUser) {
      return HttpResponse.json(
        {
          httpStatusCode: 404,
          errorMessage:
            userType === "HOST"
              ? "해당 사업자등록번호로 가입된 주최측이 없습니다."
              : "해당 전화번호로 가입된 사용자가 없습니다.",
        },
        { status: 404 }
      )
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "조회에 성공했습니다.",
        data: {
          id: foundUser.userId,
          name: foundUser.name,
          number: userType === "HOST" ? foundUser.businessNo : foundUser.phone,
        },
      },
      { status: 200 }
    )
  }),
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

    if (businessNo === getMockHostBusinessNo()) {
      return HttpResponse.json(
        {
          httpStatusCode: 200,
          responseMessage: "이미 등록된 사업자 등록번호입니다.",
          data: {
            isDuplicated: true,
          },
        },
        { status: 200 }
      )
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "사용 가능한 사업자 등록번호입니다.",
        data: {
          isDuplicated: false,
        },
      },
      { status: 200 }
    )
  }),
  http.post("*/api/v1/hosts", async ({ request }) => {
    const body = (await request.json()) as {
      companyName?: string
      businessNo?: string
      email?: string
      password?: string
    }
    const companyName = body.companyName?.trim() ?? ""
    const businessNo = body.businessNo?.trim() ?? ""
    const email = body.email?.trim() ?? ""
    const password = body.password?.trim() ?? ""
    const businessNoPattern = /^\d{3}-\d{2}-\d{5}$/

    if (!companyName || !businessNo || !email || !password) {
      return HttpResponse.json(
        {
          httpStatusCode: 400,
          errorMessage: "잘못된 요청입니다.",
        },
        { status: 400 }
      )
    }

    if (!businessNoPattern.test(businessNo)) {
      return HttpResponse.json(
        {
          httpStatusCode: 400,
          errorMessage: "잘못된 요청입니다.",
        },
        { status: 400 }
      )
    }

    if (email === getMockHostEmail()) {
      return HttpResponse.json(
        {
          httpStatusCode: 409,
          errorMessage: "이미 존재하는 이메일입니다.",
        },
        { status: 409 }
      )
    }

    if (businessNo === getMockHostBusinessNo()) {
      return HttpResponse.json(
        {
          httpStatusCode: 409,
          errorMessage: "이미 존재하는 사업자등록번호입니다.",
        },
        { status: 409 }
      )
    }

    registerMockHost({
      email,
      password,
      businessNo,
    })

    return HttpResponse.json(
      {
        httpStatusCode: 201,
        responseMessage: "회원 가입이 완료되었습니다.",
      },
      { status: 201 }
    )
  }),
  http.post("*/api/v1/hosts/auth/login", async ({ request }) => {
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

    if (email !== getMockHostEmail()) {
      return HttpResponse.json(
        {
          httpStatusCode: 404,
          errorMessage: "이메일 또는 비밀번호가 일치하지 않습니다.",
        },
        { status: 404 }
      )
    }

    if (password !== getMockHostPassword()) {
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
  http.post("*/api/v1/hosts/auth/logout", async () => {
    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "로그아웃 완료",
      },
      { status: 200 }
    )
  }),
]

export const authTokenHandlers = [
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
