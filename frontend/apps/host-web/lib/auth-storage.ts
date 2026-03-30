const ACCESS_TOKEN_KEY = "host-web.access-token"
const REFRESH_TOKEN_KEY = "host-web.refresh-token"
const COOKIE_PATH = "path=/"

function canUseStorage() {
  return typeof window !== "undefined" && typeof window.localStorage !== "undefined"
}

function canUseDocument() {
  return typeof document !== "undefined"
}

function writeCookie(key: string, value: string) {
  if (!canUseDocument()) {
    return
  }

  document.cookie = `${key}=${encodeURIComponent(value)}; ${COOKIE_PATH}; SameSite=Lax`
}

function clearCookie(key: string) {
  if (!canUseDocument()) {
    return
  }

  document.cookie = `${key}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; ${COOKIE_PATH}; SameSite=Lax`
}

export function getAccessToken() {
  if (!canUseStorage()) {
    return null
  }

  return window.localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function setAccessToken(token: string) {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.setItem(ACCESS_TOKEN_KEY, token)
  writeCookie(ACCESS_TOKEN_KEY, token)
}

export function getRefreshToken() {
  if (!canUseStorage()) {
    return null
  }

  return window.localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setRefreshToken(token: string) {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.setItem(REFRESH_TOKEN_KEY, token)
  writeCookie(REFRESH_TOKEN_KEY, token)
}

export function clearAccessToken() {
  if (canUseStorage()) {
    window.localStorage.removeItem(ACCESS_TOKEN_KEY)
  }

  clearCookie(ACCESS_TOKEN_KEY)
}

export function clearRefreshToken() {
  if (canUseStorage()) {
    window.localStorage.removeItem(REFRESH_TOKEN_KEY)
  }

  clearCookie(REFRESH_TOKEN_KEY)
}

export function clearAuthTokens() {
  clearAccessToken()
  clearRefreshToken()
}
