export type TicketStatus = 'SOLD' | 'LISTED' | 'USED' | 'EXPIRED'
export type SeatStatus = 'AVAILABLE' | 'LOCKED' | 'SOLD'
export type EventStatus = 'ON_SALE' | 'SOLD_OUT' | 'ENDED'
export type Tab = 'home' | 'concerts' | 'resale' | 'my-tickets' | 'collection'

export type TxType = 'PURCHASE' | 'RESALE_LIST' | 'RESALE_BUY' | 'TRANSFER'
export type TxStatus = 'PENDING' | 'CONFIRMING' | 'CONFIRMED' | 'FAILED'

export type WalletTxType = 'CHARGE' | 'PURCHASE' | 'RESALE_BUY' | 'RESALE_SELL' | 'TRANSFER_SEND' | 'TRANSFER_RECEIVE'

export interface TxRecord {
  id: string              // uuid
  txHash: string          // 0x…
  type: TxType
  status: TxStatus
  label: string           // human-readable e.g. "AESPA WORLD TOUR 2026 티켓 구매"
  amount?: number         // CTK amount
  createdAt: number       // Date.now()
  confirmedAt?: number
  confirmations: number   // block confirmations (out of 12)
  errorMessage?: string
}

export interface WalletTx {
  id: string
  type: WalletTxType
  label: string           // e.g. "CTK 충전" or "AESPA 티켓 구매"
  amount: number          // CTK amount (positive for in, negative for out)
  balance: number         // balance after transaction
  createdAt: number       // Date.now()
}

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
  | 'resale-tickets'
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
  | 'tx-history'
  | 'wallet-history'
  | 'settings'
  | 'password-change'
  | 'sms-verification'
  | 'find-account'
  | 'password-reset'
  | 'withdraw'

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
  eventId: string
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
