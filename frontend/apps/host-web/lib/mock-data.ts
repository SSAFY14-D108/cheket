export interface Event {
  id: string
  title: string
  date: string
  location: string
  posterUrl: string
  totalSeats: number
  soldSeats: number
  enteredCount: number
  notEnteredCount: number
  emptyCount: number
  likes: number
  description: string
  bookingStartDate: string
  bookingEndDate: string
  refundPolicy: string
  maxPurchase: number
  seatPrices: { section: string; price: number }[]
  revenueDistribution: { label: string; percentage: number }[]
}

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

export const mockEvents: Event[] = [
  {
    id: "1",
    title: "서울 윈터 콘서트 2026",
    date: "2026-03-15",
    location: "올림픽공원 체조경기장",
    posterUrl: "/images/poster-1.jpg",
    totalSeats: 5000,
    soldSeats: 4200,
    enteredCount: 3800,
    notEnteredCount: 400,
    emptyCount: 800,
    likes: 1240,
    description: "2026년 겨울을 뜨겁게 달굴 대규모 콘서트! 최고의 아티스트들이 한자리에 모여 잊을 수 없는 무대를 선사합니다.",
    bookingStartDate: "2026-02-01",
    bookingEndDate: "2026-03-14",
    refundPolicy: "공연 7일 전까지 100% 환불, 3일 전까지 50% 환불, 이후 환불 불가",
    maxPurchase: 4,
    seatPrices: [
      { section: "VIP", price: 180000 },
      { section: "R석", price: 130000 },
      { section: "S석", price: 90000 },
      { section: "A석", price: 60000 },
    ],
    revenueDistribution: [
      { label: "소속사", percentage: 50 },
      { label: "가수", percentage: 30 },
      { label: "기획자", percentage: 20 },
    ],
  },
  {
    id: "2",
    title: "봄날의 재즈 나이트",
    date: "2026-04-20",
    location: "블루노트 서울",
    posterUrl: "/images/poster-2.jpg",
    totalSeats: 300,
    soldSeats: 280,
    enteredCount: 250,
    notEnteredCount: 30,
    emptyCount: 20,
    likes: 560,
    description: "봄밤의 감성을 채워줄 재즈 공연. 국내외 최고의 재즈 뮤지션들이 펼치는 특별한 무대입니다.",
    bookingStartDate: "2026-03-15",
    bookingEndDate: "2026-04-19",
    refundPolicy: "공연 5일 전까지 100% 환불, 이후 환불 불가",
    maxPurchase: 2,
    seatPrices: [
      { section: "VIP", price: 120000 },
      { section: "일반", price: 80000 },
    ],
    revenueDistribution: [
      { label: "소속사", percentage: 50 },
      { label: "가수", percentage: 30 },
      { label: "기획자", percentage: 20 },
    ],
  },
  {
    id: "3",
    title: "록 페스티벌 2026",
    date: "2026-06-10",
    location: "난지한강공원",
    posterUrl: "/images/poster-3.jpg",
    totalSeats: 10000,
    soldSeats: 7500,
    enteredCount: 0,
    notEnteredCount: 0,
    emptyCount: 2500,
    likes: 3200,
    description: "여름을 깨우는 록 페스티벌! 국내 최대 규모의 야외 록 공연으로, 10팀 이상의 밴드가 출연합니다.",
    bookingStartDate: "2026-04-01",
    bookingEndDate: "2026-06-09",
    refundPolicy: "공연 14일 전까지 100% 환불, 7일 전까지 50% 환불, 이후 환불 불가",
    maxPurchase: 6,
    seatPrices: [
      { section: "VIP", price: 200000 },
      { section: "일반 입장", price: 99000 },
    ],
    revenueDistribution: [
      { label: "소속사", percentage: 50 },
      { label: "가수", percentage: 30 },
      { label: "기획자", percentage: 20 },
    ],
  },
  {
    id: "4",
    title: "인디 뮤직 쇼케이스",
    date: "2026-05-05",
    location: "홍대 롤링홀",
    posterUrl: "/images/poster-4.jpg",
    totalSeats: 200,
    soldSeats: 180,
    enteredCount: 170,
    notEnteredCount: 10,
    emptyCount: 20,
    likes: 420,
    description: "떠오르는 인디 뮤지션들의 신선한 무대를 만나보세요. 음악의 새로운 가능성을 경험할 수 있는 쇼케이스입니다.",
    bookingStartDate: "2026-04-10",
    bookingEndDate: "2026-05-04",
    refundPolicy: "공연 3일 전까지 100% 환불, 이후 환불 불가",
    maxPurchase: 2,
    seatPrices: [
      { section: "스탠딩", price: 33000 },
    ],
    revenueDistribution: [
      { label: "소속사", percentage: 50 },
      { label: "가수", percentage: 30 },
      { label: "기획자", percentage: 20 },
    ],
  },
]

export interface DailyBooking {
  date: string
  count: number
}

export const mockDailyBookings: DailyBooking[] = [
  { date: "02/01", count: 120 },
  { date: "02/05", count: 340 },
  { date: "02/10", count: 580 },
  { date: "02/15", count: 720 },
  { date: "02/20", count: 950 },
  { date: "02/25", count: 1100 },
  { date: "03/01", count: 1400 },
  { date: "03/05", count: 2100 },
  { date: "03/10", count: 3200 },
  { date: "03/14", count: 4200 },
]
