export interface CompanyInfo {
  companyName: string
  businessNumber: string
  email: string
  walletAddress: string
  balance: number
}

export const mockCompany: CompanyInfo = {
  companyName: "스타라이트 엔터테인먼트",
  businessNumber: "123-45-67890",
  email: "admin@starlight-ent.com",
  walletAddress: "0x1a2B...9cD0",
  balance: 15.75,
}

export const MY_PAGE_COMPANY = {
  companyName: "스타라이트 엔터테인먼트",
  businessNo: "123-45-67890",
  email: "ssafy@gmail.com",
}

export const MY_WALLET_BALANCE = {
  balance: 350000,
  walletAddress: "0xAb5801a7D398351b8bE11C439e05C5b3259aec9B",
}
