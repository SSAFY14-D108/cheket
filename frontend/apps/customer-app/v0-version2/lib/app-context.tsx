'use client'

import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react'
import { Screen, Tab, NavParams, Ticket, User, ResaleItem, TxRecord, TxType, WalletTx, WalletTxType, Event, RefundRule } from './types'
import { MOCK_USER, MOCK_TICKETS, MOCK_RESALE_ITEMS, MOCK_EVENTS } from './mock-data'

// ── TX helper ────────────────────────────────────────────────────────────────
function randomHex(len: number) {
  return Array.from({ length: len }, () => Math.floor(Math.random() * 16).toString(16)).join('')
}
function makeTxHash() { return `0x${randomHex(64)}` }
function makeTxId()   { return `tx_${Date.now()}_${randomHex(4)}` }
function makeWalletTxId() { return `wtx_${Date.now()}_${randomHex(4)}` }

function parseTicketEventDate(value: string) {
  const match = value.match(/(\d{4})\.(\d{2})\.(\d{2})/)
  if (!match) return null
  const [, year, month, day] = match
  return new Date(Number(year), Number(month) - 1, Number(day), 0, 0, 0, 0)
}

function getDefaultRefundRules(): RefundRule[] {
  return [
    { id: 'default_r1', daysBefore: 7, feeRate: 0, label: '공연 7일 전까지' },
    { id: 'default_r2', daysBefore: 3, feeRate: 0.1, label: '공연 3일 전까지' },
    { id: 'default_r3', daysBefore: 1, feeRate: 0.2, label: '공연 1일 전까지' },
    { id: 'default_r4', daysBefore: 0, feeRate: 1, label: '공연 당일 이후' },
  ]
}

function getRefundPolicy(ticket: Ticket, event?: Event) {
  const eventDate = parseTicketEventDate(ticket.eventDate)
  const refundRules = [...(event?.refundRules ?? getDefaultRefundRules())].sort((a, b) => b.daysBefore - a.daysBefore)
  if (!eventDate) {
    return {
      refundable: false,
      feeRate: refundRules[refundRules.length - 1]?.feeRate ?? 1,
      feeAmount: ticket.originalPrice,
      refundAmount: 0,
      daysLeft: -1,
      appliedRule: refundRules[refundRules.length - 1] ?? null,
    }
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const diff = eventDate.getTime() - today.getTime()
  const daysLeft = Math.floor(diff / 86400000)

  const appliedRule = refundRules.find((rule) => daysLeft >= rule.daysBefore) ?? refundRules[refundRules.length - 1]
  const feeRate = appliedRule?.feeRate ?? 1

  const refundable = daysLeft >= 1 && ticket.status === 'SOLD'
  const feeAmount = refundable ? Math.floor(ticket.originalPrice * feeRate) : ticket.originalPrice
  const refundAmount = refundable ? ticket.originalPrice - feeAmount : 0

  return { refundable, feeRate, feeAmount, refundAmount, daysLeft, appliedRule }
}

interface AppContextValue {
  // Auth
  user: User | null
  login: (id: string, password: string) => boolean
  logout: () => void

  // Navigation
  screen: Screen
  activeTab: Tab
  navParams: NavParams
  navigate: (screen: Screen, params?: NavParams) => void
  navigateTab: (tab: Tab) => void
  goBack: () => void

  // Data
  tickets: Ticket[]
  resaleItems: ResaleItem[]
  events: Event[]
  addTicket: (ticket: Ticket) => void
  updateTicketStatus: (id: string, updates: Partial<Ticket>) => void
  addResaleItem: (item: ResaleItem) => void
  removeResaleItem: (id: string) => void
  buyResaleTicket: (resaleItemId: string) => string | false
  topUpCTK: (amount: number) => void
  transferTicket: (ticketId: string, recipientPhone: string) => { success: boolean; recipientName?: string; reason?: 'LIMIT_EXCEEDED' | 'USER_NOT_FOUND' | 'NETWORK' }
  refundTicket: (ticketId: string) => { success: boolean; refundAmount?: number; feeAmount?: number; reason?: 'NOT_FOUND' | 'NOT_ELIGIBLE' }
  
  // Wishlist
  wishlist: string[]
  toggleWishlist: (eventId: string) => void
  isWishlisted: (eventId: string) => boolean

  // Blockchain TX
  txRecords: TxRecord[]
  addTx: (type: TxType, label: string, amount?: number) => TxRecord

  // Wallet TX history
  walletTxs: WalletTx[]
  addWalletTx: (type: WalletTxType, label: string, amount: number) => WalletTx

  // Settings
  allowNotifications: boolean
  setAllowNotifications: (enabled: boolean) => void
}

const AppContext = createContext<AppContextValue | null>(null)

export function AppProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [screen, setScreen] = useState<Screen>('login')
  const [activeTab, setActiveTab] = useState<Tab>('home')
  const [navParams, setNavParams] = useState<NavParams>({})
  const [screenHistory, setScreenHistory] = useState<{ screen: Screen; params: NavParams }[]>([])
  const [tickets, setTickets] = useState<Ticket[]>(MOCK_TICKETS)
  const [resaleItems, setResaleItems] = useState<ResaleItem[]>(MOCK_RESALE_ITEMS)
  const [events] = useState<Event[]>(MOCK_EVENTS)
  const [wishlist, setWishlist] = useState<string[]>(['evt_001'])
  const [txRecords, setTxRecords] = useState<TxRecord[]>([])
  const [walletTxs, setWalletTxs] = useState<WalletTx[]>([
    {
      id: 'wtx_001',
      type: 'CHARGE',
      label: 'CTK 충전',
      amount: 3000,
      balance: 147400,
      createdAt: Date.now() - 86400000 * 10,
    },
    {
      id: 'wtx_002',
      type: 'PURCHASE',
      label: 'AESPA WORLD TOUR 티켓 구매',
      amount: -140000,
      balance: 144400,
      createdAt: Date.now() - 86400000 * 9,
    },
  ])
  const [allowNotifications, setAllowNotifications] = useState(true)

  const login = useCallback((id: string, _password: string): boolean => {
    if (id.length > 0) {
      setUser(MOCK_USER)
      setScreen('home')
      setActiveTab('home')
      return true
    }
    return false
  }, [])

  const logout = useCallback(() => {
    setUser(null)
    setScreen('login')
    setScreenHistory([])
  }, [])

  const navigate = useCallback((newScreen: Screen, params: NavParams = {}) => {
    setScreenHistory((prev) => [...prev, { screen, params: navParams }])
    setScreen(newScreen)
    setNavParams(params)
  }, [screen, navParams])

  const navigateTab = useCallback((tab: Tab) => {
    const tabScreenMap: Record<Tab, Screen> = {
      home: 'home',
      concerts: 'concerts',
      resale: 'resale-list',
      'my-tickets': 'my-tickets',
      collection: 'collection',
    }
    setActiveTab(tab)
    setScreen(tabScreenMap[tab])
    setNavParams({})
    setScreenHistory([])
  }, [])

  const goBack = useCallback(() => {
    setScreenHistory((prev) => {
      const newHistory = [...prev]
      const last = newHistory.pop()
      if (last) {
        setScreen(last.screen)
        setNavParams(last.params)
      }
      return newHistory
    })
  }, [])

  const addTicket = useCallback((ticket: Ticket) => {
    setTickets((prev) => [ticket, ...prev])
  }, [])

  const updateTicketStatus = useCallback((id: string, updates: Partial<Ticket>) => {
    setTickets((prev) => prev.map((t) => (t.id === id ? { ...t, ...updates } : t)))
  }, [])

  const addResaleItem = useCallback((item: ResaleItem) => {
    setResaleItems((prev) => [item, ...prev])
  }, [])

  const removeResaleItem = useCallback((id: string) => {
    setResaleItems((prev) => prev.filter((r) => r.id !== id))
  }, [])

  const buyResaleTicket = useCallback((resaleItemId: string): string | false => {
    const item = resaleItems.find((r) => r.id === resaleItemId)
    if (!item || !user) return false
    if (user.ctkBalance < item.resalePrice) return false

    const newId = `tkt_${Date.now()}`
    const newTicket: Ticket = {
      id: newId,
      eventId: item.eventId,
      eventName: item.eventName,
      eventDate: item.eventDate,
      venue: item.venue,
      poster: item.poster,
      seatId: `seat_${Date.now()}`,
      seatLabel: item.seatLabel,
      grade: item.grade,
      originalPrice: item.originalPrice,
      resalePrice: item.resalePrice,
      status: 'SOLD',
    }
    setTickets((prev) => [newTicket, ...prev])
    setResaleItems((prev) => prev.filter((r) => r.id !== resaleItemId))
    setUser((prev) => prev ? { ...prev, ctkBalance: prev.ctkBalance - item.resalePrice } : prev)
    return newId
  }, [resaleItems, user])

  const topUpCTK = useCallback((amount: number) => {
    setUser((prev) => {
      if (!prev) return prev
      const newBalance = prev.ctkBalance + amount
      // Add wallet TX
      const wtx: WalletTx = {
        id: makeWalletTxId(),
        type: 'CHARGE',
        label: `CTK ${amount.toLocaleString()} 충전`,
        amount,
        balance: newBalance,
        createdAt: Date.now(),
      }
      setWalletTxs((prev) => [wtx, ...prev])
      return { ...prev, ctkBalance: newBalance }
    })
  }, [])

  const addTx = useCallback((type: TxType, label: string, amount?: number): TxRecord => {
    const record: TxRecord = {
      id: makeTxId(),
      txHash: makeTxHash(),
      type,
      status: 'CONFIRMED',
      label,
      amount,
      createdAt: Date.now(),
      confirmedAt: Date.now(),
      confirmations: 12,
    }
    setTxRecords((prev) => [record, ...prev])
    return record
  }, [])

  const addWalletTx = useCallback((type: WalletTxType, label: string, amount: number): WalletTx => {
    const lastBalance = walletTxs.length > 0 ? walletTxs[0].balance : user?.ctkBalance || 0
    const newBalance = lastBalance + amount
    const wtx: WalletTx = {
      id: makeWalletTxId(),
      type,
      label,
      amount,
      balance: newBalance,
      createdAt: Date.now(),
    }
    setWalletTxs((prev) => [wtx, ...prev])
    return wtx
  }, [walletTxs, user])

  const toggleWishlist = useCallback((eventId: string) => {
    setWishlist((prev) => 
      prev.includes(eventId) 
        ? prev.filter((id) => id !== eventId)
        : [...prev, eventId]
    )
  }, [])

  const isWishlisted = useCallback((eventId: string) => {
    return wishlist.includes(eventId)
  }, [wishlist])

  // Mock phone → name lookup
  const MOCK_PHONE_BOOK: Record<string, string> = {
    '010-9876-5432': '박지연',
    '010-1234-5678': '김민준',
    '010-5555-4444': '이수진',
  }

  const transferTicket = useCallback((ticketId: string, recipientPhone: string): { success: boolean; recipientName?: string; reason?: 'LIMIT_EXCEEDED' | 'USER_NOT_FOUND' | 'NETWORK' } => {
    const recipientName = MOCK_PHONE_BOOK[recipientPhone]
    if (!recipientName) {
      return { success: false, reason: 'USER_NOT_FOUND' }
    }
    // Simulate limit check: if ticket grade is VIP and user already transferred one, fail
    const ticket = tickets.find((t) => t.id === ticketId)
    if (!ticket) return { success: false, reason: 'NETWORK' }

    // Mark ticket as USED (양도 완료)
    setTickets((prev) => prev.map((t) => t.id === ticketId ? { ...t, status: 'USED' as const } : t))
    return { success: true, recipientName }
  }, [tickets])

  const refundTicket = useCallback((ticketId: string): { success: boolean; refundAmount?: number; feeAmount?: number; reason?: 'NOT_FOUND' | 'NOT_ELIGIBLE' } => {
    const ticket = tickets.find((item) => item.id === ticketId)
    if (!ticket) return { success: false, reason: 'NOT_FOUND' }

    const event = events.find((item) => item.id === ticket.eventId)
    const policy = getRefundPolicy(ticket, event)
    if (!policy.refundable || !user) {
      return { success: false, reason: 'NOT_ELIGIBLE' }
    }

    setTickets((prev) =>
      prev.map((item) =>
        item.id === ticketId
          ? { ...item, status: 'EXPIRED' as const, resalePrice: undefined }
          : item
      )
    )

    setUser((prev) => (
      prev ? { ...prev, ctkBalance: prev.ctkBalance + policy.refundAmount } : prev
    ))

    setWalletTxs((prev) => {
      const lastBalance = prev.length > 0 ? prev[0].balance : user.ctkBalance
      return [
        {
          id: makeWalletTxId(),
          type: 'REFUND',
          label: `${ticket.eventName} 티켓 환불`,
          amount: policy.refundAmount,
          balance: lastBalance + policy.refundAmount,
          createdAt: Date.now(),
        },
        ...prev,
      ]
    })

    setTxRecords((prev) => [
      {
        id: makeTxId(),
        txHash: makeTxHash(),
        type: 'REFUND',
        status: 'CONFIRMED',
        label: `${ticket.eventName} 티켓 환불`,
        amount: policy.refundAmount,
        createdAt: Date.now(),
        confirmedAt: Date.now(),
        confirmations: 12,
      },
      ...prev,
    ])

    return {
      success: true,
      refundAmount: policy.refundAmount,
      feeAmount: policy.feeAmount,
    }
  }, [events, tickets, user])

  return (
    <AppContext.Provider
      value={{
        user, login, logout,
        screen, activeTab, navParams, navigate, navigateTab, goBack,
        tickets, resaleItems, events,
        addTicket, updateTicketStatus, addResaleItem, removeResaleItem, buyResaleTicket, topUpCTK, transferTicket, refundTicket,
        wishlist, toggleWishlist, isWishlisted,
        txRecords, addTx,
        walletTxs, addWalletTx,
        allowNotifications, setAllowNotifications,
      }}
    >
      {children}
    </AppContext.Provider>
  )
}

export function useApp() {
  const ctx = useContext(AppContext)
  if (!ctx) throw new Error('useApp must be used within AppProvider')
  return ctx
}
