// Shared types used across ShowForm sub-components
export interface Grade {
    gradeName: string
    price: string
    colorCode: string
    sectionId: string
    ticketEffectId?: string
}

export interface Stakeholder {
    id?: number
    role: 'ORGANIZER' | 'ARTIST'
    name: string
    phone?: string
    businessNo?: string
    shareBps: string
    verified?: boolean
    isFixed?: boolean
    isSelfHost?: boolean
}

export interface RefundItem {
    daysRemaining: string
    refundRate: string
}

export interface SessionItem {
    sessionDate: string
    sessionStartTime: string
    capacity?: string | number
}
