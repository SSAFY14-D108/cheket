// Shared types used across ShowForm sub-components
export interface Grade {
    sectionId: number | string
    gradeName: string
    price: string
    colorCode: string
}

export interface Stakeholder {
    role: "organizer" | "artist"
    name: string
    businessNo?: string
    phone?: string
    shareBps: string
}

export interface RefundItem {
    daysRemaining: string
    refundRate: string
    feeDescription: string
}

export interface SessionItem {
    sessionId: number | string
    sessionDate: string
    sessionStartDate: string
    capacity: string
}
