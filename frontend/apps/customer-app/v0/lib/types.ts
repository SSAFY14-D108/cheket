export type TicketStatus = 'SOLD' | 'LISTED' | 'USED' | 'EXPIRED'
export type SeatStatus = 'AVAILABLE' | 'LOCKED' | 'SOLD'
export type EventStatus = 'ON_SALE' | 'SOLD_OUT' | 'ENDED'
export type Tab = 'home' | 'concerts' | 'resale' | 'my-tickets' | 'collection'

export type PurchaseFailureReason =
  | 'SOLD_OUT'
  | 'LOCK_FAILED'
  | 'LIMIT_EXCEEDED'
  | 'INSUFFICIENT_BALANCE'
  | 'NETWORK'

export type WaitingQueueState = 'WAITING' | 'READY_TO_ENTER' | 'EXPIRED'

export type Screen =
  | 'login'
  | 'signup'
  | 'home'
  | 'concerts'
  | 'event-detail'
  | 'waiting-queue'
  | 'seat-selection'
  | 'payment'
  | 'purchase-failed'
  | 'resale-list'
  | 'resale-detail'
  | 'resale-purchase-complete'
  | 'my-tickets'
  | 'ticket-detail'
  | 'resale-create'
  | 'qr-checkin'
  | 'archive'
  | 'collectible-ticket-detail'
  | 'my-page'
  | 'wallet'
  | 'collection'
  | 'transfer'
  | 'transfer-complete'
  | 'transfer-failed'
  | 'wishlist'

export interface Grade {
  name: string
  price: number
  remaining: number
  color?: string
}

export interface EventDate {
  id: string
  label: string      // e.g. "2026.04.12 (토) 18:00"
  day: string        // e.g. "DAY 1"
}

export interface Event {
  id: string
  name: string
  date: string
  dates?: EventDate[]   // multiple performance dates
  venue: string
  region: string        // e.g. "서울", "경기", "부산"
  poster: string
  status: EventStatus
  maxPerUser: number
  grades: Grade[]
  openDate?: string     // for deadline sorting, e.g. "2026-04-01"
  description?: string
}

export interface Seat {
  id: string
  row: string
  number: number
  grade: string
  price: number
  status: SeatStatus
}

export interface Ticket {
  id: string
  eventId: string
  eventName: string
  eventDate: string
  venue: string
  poster: string
  seatId: string
  seatLabel: string
  grade: string
  originalPrice: number
  status: TicketStatus
  resalePrice?: number
  attendedDate?: string
}

export interface ResaleItem {
  id: string
  ticketId: string
  eventName: string
  eventDate: string
  venue: string
  poster: string
  seatLabel: string
  grade: string
  originalPrice: number
  resalePrice: number
  sellerId: string
}

export interface User {
  id: string
  name: string
  phone: string
  walletAddress: string
  ctkBalance: number
}

export interface NavParams {
  eventId?: string
  resaleEventName?: string
  ticketId?: string
  resaleItemId?: string
  /** ID of the newly issued ticket after resale purchase, for the complete screen */
  purchasedTicketId?: string
  seats?: Seat[]
  totalPrice?: number
  failureReason?: PurchaseFailureReason
  /** Transfer (양도) related params */
  recipientName?: string
  recipientPhone?: string
  transferFailureReason?: 'LIMIT_EXCEEDED' | 'USER_NOT_FOUND' | 'NETWORK'
}
