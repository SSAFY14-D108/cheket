import { http, HttpResponse } from "msw"
import {
  getMockHostPassword,
  MOCK_ACCESS_TOKEN,
  MOCK_HOST_EMAIL,
  MOCK_NEW_ACCESS_TOKEN,
  MOCK_NEW_REFRESH_TOKEN,
  MOCK_REFRESH_TOKEN,
  setMockHostPassword,
} from "@/mocks/data/auth-store"

export const authHandlers = [
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
  http.post("*/api/v1/hosts", async ({ request }) => {
    const body = (await request.json()) as { password?: string }

    if (body.password?.trim()) {
      setMockHostPassword(body.password)
    }

    return HttpResponse.json(
      {
        httpStatusCode: 201,
        responseMessage: "회원 가입이 완료되었습니다.",
      },
      { status: 201 }
    )
  }),
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

    if (email !== MOCK_HOST_EMAIL || password !== getMockHostPassword()) {
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
