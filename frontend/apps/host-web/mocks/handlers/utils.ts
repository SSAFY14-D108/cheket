import { HttpResponse } from "msw"

export function isAuthorized(request: Request) {
  return Boolean(request.headers.get("Authorization"))
}

export function createUnauthorizedResponse() {
  return HttpResponse.json(
    {
      httpStatusCode: 401,
      errorMessage: "로그인이 필요합니다.",
    },
    { status: 401 }
  )
}

export function createNotFoundResponse() {
  return HttpResponse.json(
    {
      httpStatusCode: 404,
      errorMessage: "존재하지 않는 공연입니다.",
    },
    { status: 404 }
  )
}
