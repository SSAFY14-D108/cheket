// ──────────────────────────────────────────────────────────────────────────────
// Dashboard API response types
// ──────────────────────────────────────────────────────────────────────────────

// GET /api/v1/hosts/shows/{showId}/dashboard/total-sales
export interface DashboardTotalSales {
    showId: number
    title: string
    totalPrimarySales: number // 원화
}

// GET /api/v1/hosts/shows/{showId}/dashboard/booking-rate
export interface DashboardBookingRate {
    showId: number
    title: string
    capacity: number      // 전체 세션 좌석 합산
    reservedSeats: number
    bookingRate: number   // 0 ~ 100
}

// GET /api/v1/hosts/shows/{showId}/dashboard/revenue-split
export interface RevenueSplitItem {
    role: string
    rateBps: number // 예: 5000 = 50%
    amount: number  // ETH
}

export interface DashboardRevenueSplit {
    showId: number
    title: string
    totalRevenue: number // ETH
    splits: RevenueSplitItem[]
}

// GET /api/v1/hosts/shows/{showId}/dashboard/reservations
export interface DashboardSession {
    sessionId: number
    date: string        // "YYYY-MM-DD"
    capacity: number    // 해당 세션 좌석 수
    reservedSeats: number
}

export interface DashboardReservations {
    showId: number
    title: string
    venue: string
    sessions: DashboardSession[]
}

// GET /api/v1/wallets/balance
export interface WalletBalance {
    balance: number       // 원화
    walletAddress: string
}

// ── 통합 props (DashboardContent에 전달) ────────────────────────────────────
export interface DashboardData {
    totalSales: DashboardTotalSales
    bookingRate: DashboardBookingRate
    revenueSplit: DashboardRevenueSplit
    reservations: DashboardReservations
    wallet: WalletBalance | null
    partialErrors?: string[]
}
