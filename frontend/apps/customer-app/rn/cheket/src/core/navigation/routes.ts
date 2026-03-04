// ---- Auth ----
export const AUTH_ROUTES = {
  LOGIN: 'Login',
  SIGN_UP: 'SignUp',
} as const;

// ---- Home stack ----
export const HOME_ROUTES = {
  HOME: 'Home',
  EVENT_DETAIL: 'EventDetail',
  WAITING_QUEUE: 'WaitingQueue',
  SEAT_SELECTION: 'SeatSelection',
  PAYMENT: 'Payment',
  PURCHASE_FAILED: 'PurchaseFailed',
  WISHLIST: 'Wishlist',
} as const;

// ---- Concerts stack ----
export const CONCERTS_ROUTES = {
  CONCERTS: 'Concerts',
  EVENT_DETAIL: 'ConcertsEventDetail',
} as const;

// ---- Resale stack ----
export const RESALE_ROUTES = {
  RESALE_LIST: 'ResaleList',
  RESALE_DETAIL: 'ResaleDetail',
  RESALE_PURCHASE_COMPLETE: 'ResalePurchaseComplete',
} as const;

// ---- MyTickets stack ----
export const MY_TICKETS_ROUTES = {
  MY_TICKETS: 'MyTickets',
  TICKET_DETAIL: 'TicketDetail',
  QR_CHECKIN: 'QrCheckin',
  TRANSFER: 'Transfer',
  TRANSFER_COMPLETE: 'TransferComplete',
  TRANSFER_FAILED: 'TransferFailed',
  RESALE_CREATE: 'ResaleCreate',
} as const;

// ---- Collection stack ----
export const COLLECTION_ROUTES = {
  COLLECTION: 'Collection',
  ARCHIVE: 'Archive',
  COLLECTIBLE_TICKET_DETAIL: 'CollectibleTicketDetail',
} as const;

// ---- Tab names ----
export const TAB_ROUTES = {
  HOME_TAB: 'HomeTab',
  CONCERTS_TAB: 'ConcertsTab',
  RESALE_TAB: 'ResaleTab',
  MY_TICKETS_TAB: 'MyTicketsTab',
  COLLECTION_TAB: 'CollectionTab',
} as const;

// ---- Root-level routes ----
export const ROOT_ROUTES = {
  AUTH: 'Auth',
  MAIN_TABS: 'MainTabs',
  MY_PAGE: 'MyPage',
  WALLET: 'Wallet',
} as const;
