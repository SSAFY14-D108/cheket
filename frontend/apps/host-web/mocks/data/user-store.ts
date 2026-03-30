export interface AuthUser {
  userId: number
  name: string
  phone: string
  businessNo?: string
}

export const mockAuthUsers: AuthUser[] = [
  { userId: 15, name: "홍길동", phone: "01012345678", businessNo: "123-45-67890" },
  { userId: 16, name: "김철수", phone: "01011111111" },
  { userId: 17, name: "이영희", phone: "01022222222" },
  { userId: 18, name: "CHEKET공식", phone: "01000000000", businessNo: "000-00-00000" },
]
