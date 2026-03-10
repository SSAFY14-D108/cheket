export const MOCK_ACCESS_TOKEN = "mock-token-1234"
export const MOCK_REFRESH_TOKEN = "mock-refresh-token-5678"
export const MOCK_NEW_ACCESS_TOKEN = "mock-access-token-new"
export const MOCK_NEW_REFRESH_TOKEN = "mock-refresh-token-new"
export const MOCK_HOST_EMAIL = "ssafy@gmail.com"

let mockHostPassword = "1qa2ws3ed!!"

export function getMockHostPassword() {
  return mockHostPassword
}

export function setMockHostPassword(nextPassword: string) {
  mockHostPassword = nextPassword
}
