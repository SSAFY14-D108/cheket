import type {
  DashboardBookingRate,
  DashboardReservations,
  DashboardRevenueSplit,
  DashboardTotalSales,
  WalletBalance,
} from "@/lib/dashboard-types"

export interface DashboardReservationSession {
  sessionId: number
  date: string
  capacity: number
  reservedSeats: number
}

export interface DashboardReservationData {
  showId: number
  title: string
  sessions: DashboardReservationSession[]
}

export const mockDashboardReservations: DashboardReservationData[] = [
  {
    showId: 42,
    title: "CHEKET LIVE: Spring Night",
    sessions: [
      { sessionId: 1, date: "2026-03-20", capacity: 1200, reservedSeats: 950 },
      { sessionId: 2, date: "2026-03-21", capacity: 1200, reservedSeats: 1050 },
    ],
  },
  {
    showId: 43,
    title: "봄날의 재즈 나이트",
    sessions: [{ sessionId: 3, date: "2026-04-20", capacity: 300, reservedSeats: 280 }],
  },
  {
    showId: 44,
    title: "Rolling Indie Night",
    sessions: [{ sessionId: 4, date: "2026-05-10", capacity: 200, reservedSeats: 180 }],
  },
  {
    showId: 45,
    title: "2026 한강 썸머 뮤직 페스티벌",
    sessions: [
      { sessionId: 5, date: "2026-07-25", capacity: 10000, reservedSeats: 7800 },
      { sessionId: 6, date: "2026-07-26", capacity: 10000, reservedSeats: 7700 },
    ],
  },
]

export const mockDashboardTotalSales: DashboardTotalSales = {
  showId: 1,
  title: "서울 윈터 콘서트 2026",
  totalPrimarySales: 375000000,
}

export const mockDashboardBookingRate: DashboardBookingRate = {
  showId: 1,
  title: "서울 윈터 콘서트 2026",
  capacity: 35000,
  reservedSeats: 15580,
  bookingRate: 44.5,
}

export const mockDashboardRevenueSplit: DashboardRevenueSplit = {
  showId: 1,
  title: "서울 윈터 콘서트 2026",
  totalRevenue: 15.75,
  splits: [
    { role: "소속사", rateBps: 5000, amount: 7.875 },
    { role: "가수", rateBps: 3000, amount: 4.725 },
    { role: "기획자", rateBps: 2000, amount: 3.15 },
  ],
}

export const mockDashboardReservationsNew: DashboardReservations = {
  showId: 1,
  title: "서울 윈터 콘서트 2026",
  venue: "올림픽공원 올림픽홀",
  sessions: [
    { sessionId: 1, date: "2026-03-01", capacity: 5000, reservedSeats: 4800 },
    { sessionId: 2, date: "2026-03-02", capacity: 5000, reservedSeats: 400 },
    { sessionId: 3, date: "2026-03-03", capacity: 5000, reservedSeats: 2600 },
    { sessionId: 4, date: "2026-03-04", capacity: 5000, reservedSeats: 1500 },
    { sessionId: 5, date: "2026-03-05", capacity: 5000, reservedSeats: 1300 },
    { sessionId: 6, date: "2026-03-06", capacity: 5000, reservedSeats: 800 },
    { sessionId: 7, date: "2026-03-07", capacity: 5000, reservedSeats: 580 },
  ],
}

export const mockWalletBalance: WalletBalance = {
  balance: 350000,
  walletAddress: "0xAb5801a7D398351b8bE11C439e05C5b3259aec9B",
}

export const DASHBOARD_DATA_BY_SHOW_ID = {
  42: {
    reservations: {
      showId: 42,
      title: "CHEKET LIVE: Spring Night",
      venue: "올림픽공원 올림픽홀",
      sessions: [
        { sessionId: 1, date: "2026-03-20", capacity: 1200, reservedSeats: 950 },
        { sessionId: 2, date: "2026-03-21", capacity: 1200, reservedSeats: 1050 },
      ],
    },
    revenueSplit: {
      showId: 42,
      title: "CHEKET LIVE: Spring Night",
      totalRevenue: 15.75,
      splits: [
        { role: "소속사", rateBps: 5000, amount: 7.875 },
        { role: "가수", rateBps: 3000, amount: 4.725 },
        { role: "기획자", rateBps: 2000, amount: 3.15 },
      ],
    },
    bookingRate: {
      showId: 42,
      title: "CHEKET LIVE: Spring Night",
      capacity: 2400,
      reservedSeats: 2000,
      bookingRate: 83.3,
    },
    totalSales: {
      showId: 42,
      title: "CHEKET LIVE: Spring Night",
      totalPrimarySales: 375000000,
    },
  },
  43: {
    reservations: {
      showId: 43,
      title: "봄날의 재즈 나이트",
      venue: "블루노트 서울",
      sessions: [{ sessionId: 3, date: "2026-04-20", capacity: 300, reservedSeats: 280 }],
    },
    revenueSplit: {
      showId: 43,
      title: "봄날의 재즈 나이트",
      totalRevenue: 8.4,
      splits: [
        { role: "주최측", rateBps: 6000, amount: 5.04 },
        { role: "아티스트", rateBps: 4000, amount: 3.36 },
      ],
    },
    bookingRate: {
      showId: 43,
      title: "봄날의 재즈 나이트",
      capacity: 300,
      reservedSeats: 280,
      bookingRate: 93.3,
    },
    totalSales: {
      showId: 43,
      title: "봄날의 재즈 나이트",
      totalPrimarySales: 22400000,
    },
  },
  44: {
    reservations: {
      showId: 44,
      title: "한여름 밤의 인디페스타",
      venue: "난지한강공원",
      sessions: [
        { sessionId: 4, date: "2026-08-15", capacity: 10000, reservedSeats: 7800 },
        { sessionId: 5, date: "2026-08-16", capacity: 10000, reservedSeats: 7200 },
      ],
    },
    revenueSplit: {
      showId: 44,
      title: "한여름 밤의 인디페스타",
      totalRevenue: 42.5,
      splits: [
        { role: "주최측", rateBps: 5500, amount: 23.375 },
        { role: "아티스트", rateBps: 3000, amount: 12.75 },
        { role: "기획", rateBps: 1500, amount: 6.375 },
      ],
    },
    bookingRate: {
      showId: 44,
      title: "한여름 밤의 인디페스타",
      capacity: 20000,
      reservedSeats: 15000,
      bookingRate: 75,
    },
    totalSales: {
      showId: 44,
      title: "한여름 밤의 인디페스타",
      totalPrimarySales: 412500000,
    },
  },
  45: {
    reservations: {
      showId: 45,
      title: "가을 클래식 선율",
      venue: "예술의전당 콘서트홀",
      sessions: [
        { sessionId: 6, date: "2026-10-10", capacity: 1800, reservedSeats: 1120 },
        { sessionId: 7, date: "2026-10-11", capacity: 1800, reservedSeats: 980 },
        { sessionId: 8, date: "2026-10-12", capacity: 1800, reservedSeats: 860 },
      ],
    },
    revenueSplit: {
      showId: 45,
      title: "가을 클래식 선율",
      totalRevenue: 18.2,
      splits: [
        { role: "주최측", rateBps: 5000, amount: 9.1 },
        { role: "오케스트라", rateBps: 3500, amount: 6.37 },
        { role: "지휘", rateBps: 1500, amount: 2.73 },
      ],
    },
    bookingRate: {
      showId: 45,
      title: "가을 클래식 선율",
      capacity: 5400,
      reservedSeats: 2960,
      bookingRate: 54.8,
    },
    totalSales: {
      showId: 45,
      title: "가을 클래식 선율",
      totalPrimarySales: 162800000,
    },
  },
} as const

export function getDashboardSnapshot(showIdValue: string | readonly string[] | undefined) {
  const showId = Number(showIdValue)

  if (!Number.isInteger(showId)) {
    return null
  }

  return DASHBOARD_DATA_BY_SHOW_ID[showId as keyof typeof DASHBOARD_DATA_BY_SHOW_ID] ?? null
}
