import { http, HttpResponse } from "msw"

const MOCK_ACCESS_TOKEN = "mock-token-1234"
const MOCK_REFRESH_TOKEN = "mock-refresh-token-5678"
const MOCK_NEW_ACCESS_TOKEN = "mock-access-token-new"
const MOCK_NEW_REFRESH_TOKEN = "mock-refresh-token-new"

export const handlers = [
  // 회원가입
  http.post("*/api/v1/hosts", async () => {
    return HttpResponse.json(
      {
        httpStatusCode: 201,
        responseMessage: "회원가입이 완료되었습니다.",
      },
      { status: 201 }
    )
  }),
  // 로그인
  http.post("*/api/v1/hosts/login", async () => {
    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "로그인 완료",
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
  http.post("*/api/v1/hosts/auth/reissue", async ({ request }) => {
    const body = (await request.json()) as { refreshToken?: string }

    if (!body.refreshToken) {
      return HttpResponse.json(
        {
          httpStatusCode: 401,
          responseMessage: "리프레시 토큰이 유효하지 않습니다.",
        },
        { status: 401 }
      )
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "토큰 재발급 완료",
        data: {
          accessToken: MOCK_NEW_ACCESS_TOKEN,
          refreshToken: MOCK_NEW_REFRESH_TOKEN,
        },
      },
      { status: 200 }
    )
  }),
]
