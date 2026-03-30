export const MOCK_ACCESS_TOKEN = "mock-token-1234"
export const MOCK_REFRESH_TOKEN = "mock-refresh-token-5678"
export const MOCK_NEW_ACCESS_TOKEN = "mock-access-token-new"
export const MOCK_NEW_REFRESH_TOKEN = "mock-refresh-token-new"
export const DEFAULT_MOCK_HOST_EMAIL = "ssafy@gmail.com"
export const DEFAULT_MOCK_HOST_BUSINESS_NO = "123-45-67890"

let mockHostEmail = DEFAULT_MOCK_HOST_EMAIL
let mockHostPassword = "1qa2ws3ed!!"
let mockHostBusinessNo = DEFAULT_MOCK_HOST_BUSINESS_NO

export function getMockHostEmail() {
  return mockHostEmail
}

export function getMockHostPassword() {
  return mockHostPassword
}

export function getMockHostBusinessNo() {
  return mockHostBusinessNo
}

export function registerMockHost(nextHost: {
  email: string
  password: string
  businessNo: string
}) {
  mockHostEmail = nextHost.email
  mockHostPassword = nextHost.password
  mockHostBusinessNo = nextHost.businessNo
}

export function setMockHostPassword(nextPassword: string) {
  mockHostPassword = nextPassword
}
