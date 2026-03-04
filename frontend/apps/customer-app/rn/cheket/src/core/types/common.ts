export type Id = string;
export type Nullable<T> = T | null;

export type TicketStatus = 'SOLD' | 'LISTED' | 'USED' | 'EXPIRED';
export type SeatStatus = 'AVAILABLE' | 'LOCKED' | 'SOLD';
export type EventStatus = 'ON_SALE' | 'SOLD_OUT' | 'ENDED';
export type Tab = 'home' | 'concerts' | 'resale' | 'my-tickets' | 'collection';
export type PurchaseFailureReason =
  | 'SOLD_OUT'
  | 'LOCK_FAILED'
  | 'LIMIT_EXCEEDED'
  | 'INSUFFICIENT_BALANCE'
  | 'NETWORK';
export type WaitingQueueState = 'WAITING' | 'READY_TO_ENTER' | 'EXPIRED';
export type TransferFailureReason =
  | 'LIMIT_EXCEEDED'
  | 'USER_NOT_FOUND'
  | 'NETWORK';

export interface Grade {
  name: string;
  price: number;
  remaining: number;
  color?: string;
}

export interface EventDate {
  id: string;
  label: string;
  day: string;
}

export interface Event {
  id: string;
  name: string;
  date: string;
  dates?: EventDate[];
  venue: string;
  region: string;
  poster: string;
  status: EventStatus;
  maxPerUser: number;
  grades: Grade[];
  openDate?: string;
  description?: string;
}

export interface Seat {
  id: string;
  row: string;
  number: number;
  grade: string;
  price: number;
  status: SeatStatus;
}

export interface Ticket {
  id: string;
  eventId: string;
  eventName: string;
  eventDate: string;
  venue: string;
  poster: string;
  seatId: string;
  seatLabel: string;
  grade: string;
  originalPrice: number;
  status: TicketStatus;
  resalePrice?: number;
  attendedDate?: string;
}

export interface ResaleItem {
  id: string;
  ticketId: string;
  eventName: string;
  eventDate: string;
  venue: string;
  poster: string;
  seatLabel: string;
  grade: string;
  originalPrice: number;
  resalePrice: number;
  sellerId: string;
}

export interface User {
  id: string;
  name: string;
  phone: string;
  walletAddress: string;
  ctkBalance: number;
}

export interface BannerSlide {
  id: string;
  eventId: string;
  image: string;
  title: string;
  subtitle: string;
  venue: string;
  dates: string;
}

export interface CategoryIcon {
  id: string;
  label: string;
  icon: string;
}

export interface RankingItem {
  rank: number;
  eventId: string;
  name: string;
  venue: string;
  poster: string;
  genre: string;
}

export interface OpenScheduleItem {
  id: string;
  eventId: string;
  name: string;
  openLabel: string;
  openType: string;
  tags: string[];
  poster: string;
  isToday: boolean;
}

export interface DiscountItem {
  id: string;
  eventId: string;
  name: string;
  venue: string;
  dates: string;
  discountType: string;
  discountPct: number;
  finalPrice: number;
  countdownLabel: string;
  isTimeDeal: boolean;
  poster: string;
}

export interface Transaction {
  id: string;
  type: 'in' | 'out';
  label: string;
  date: string;
  amount: number;
}
