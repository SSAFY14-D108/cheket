// Shared types used across ShowForm sub-components
export interface Grade {
    gradeName: string
    price: string
    colorCode: string
    sectionId: string
    ticketEffectId?: string
}

export interface Stakeholder {
    role: 'organizer' | 'artist'
    userId?: number
    name: string
    phone?: string
    businessNo?: string
    shareBps: string
}

export interface RefundItem {
    daysRemaining: string
    refundRate: string
}

export interface SessionItem {
    sessionId: number | string
    sessionDate: string
    sessionStartDate: string
    capacity: string | number
}
