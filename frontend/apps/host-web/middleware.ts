import { NextResponse, type NextRequest } from "next/server"

const ACCESS_TOKEN_KEY = "host-web.access-token"
const PUBLIC_ONLY_PATHS = ["/", "/signup"]
const PROTECTED_PATH_PREFIXES = ["/mypage", "/shows"]

function isPublicOnlyPath(pathname: string) {
  return PUBLIC_ONLY_PATHS.includes(pathname)
}

function isProtectedPath(pathname: string) {
  return PROTECTED_PATH_PREFIXES.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`)
  )
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  const accessToken = request.cookies.get(ACCESS_TOKEN_KEY)?.value
  const isAuthenticated = Boolean(accessToken)

  if (!isAuthenticated && isProtectedPath(pathname)) {
    return NextResponse.redirect(new URL("/", request.url))
  }

  if (isAuthenticated && isPublicOnlyPath(pathname)) {
    return NextResponse.redirect(new URL("/mypage", request.url))
  }

  return NextResponse.next()
}

export const config = {
  matcher: ["/", "/signup", "/mypage/:path*", "/shows/:path*"],
}
