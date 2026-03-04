import type {NavigatorScreenParams} from '@react-navigation/native';
import type {
  PurchaseFailureReason,
  TransferFailureReason,
} from '../types/common';

// ---- Root stack ----
export type RootStackParamList = {
  Auth: undefined;
  MainTabs: NavigatorScreenParams<MainTabParamList>;
  MyPage: undefined;
  Wallet: undefined;
};

// ---- Auth stack ----
export type AuthStackParamList = {
  Login: undefined;
  SignUp: undefined;
};

// ---- Main bottom tabs ----
export type MainTabParamList = {
  HomeTab: NavigatorScreenParams<HomeStackParamList>;
  ConcertsTab: NavigatorScreenParams<ConcertsStackParamList>;
  ResaleTab: NavigatorScreenParams<ResaleStackParamList>;
  MyTicketsTab: NavigatorScreenParams<MyTicketsStackParamList>;
  CollectionTab: NavigatorScreenParams<CollectionStackParamList>;
};

// ---- Home stack ----
export type HomeStackParamList = {
  Home: undefined;
  EventDetail: {eventId: string};
  WaitingQueue: {eventId: string};
  SeatSelection: {eventId: string; dateId?: string};
  Payment: {eventId: string; seatIds: string[]};
  PurchaseFailed: {reason: PurchaseFailureReason};
  Wishlist: undefined;
};

// ---- Concerts stack ----
export type ConcertsStackParamList = {
  Concerts: undefined;
  ConcertsEventDetail: {eventId: string};
};

// ---- Resale stack ----
export type ResaleStackParamList = {
  ResaleList: undefined;
  ResaleDetail: {resaleId: string};
  ResalePurchaseComplete: {resaleId: string; purchasedTicketId: string};
};

// ---- MyTickets stack ----
export type MyTicketsStackParamList = {
  MyTickets: undefined;
  TicketDetail: {ticketId: string};
  QrCheckin: {ticketId: string};
  Transfer: {ticketId: string};
  TransferComplete: {ticketId: string; recipientName: string};
  TransferFailed: {reason: TransferFailureReason};
  ResaleCreate: {ticketId: string};
};

// ---- Collection stack ----
export type CollectionStackParamList = {
  Collection: undefined;
  Archive: undefined;
  CollectibleTicketDetail: {ticketId: string};
};
