const ACCESS_TOKEN_KEY = "host-web.access-token"
const REFRESH_TOKEN_KEY = "host-web.refresh-token"

function canUseStorage() {
  return typeof window !== "undefined" && typeof window.localStorage !== "undefined"
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
}

export function clearAccessToken() {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.removeItem(ACCESS_TOKEN_KEY)
}

export function clearRefreshToken() {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.removeItem(REFRESH_TOKEN_KEY)
}

export function clearAuthTokens() {
  clearAccessToken()
  clearRefreshToken()
}
