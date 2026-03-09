'use client'

import React, { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import type {
  Event,
  NavParams,
  RefundRule,
  ResaleItem,
  Screen,
  Tab,
  Ticket,
  TxRecord,
  TxType,
  User,
  WalletTx,
  WalletTxType,
} from './types'
import { MOCK_EVENTS, MOCK_RESALE_ITEMS, MOCK_TICKETS, MOCK_USER } from './mock-data'

function randomHex(length: number) {
  return Array.from({ length }, () => Math.floor(Math.random() * 16).toString(16)).join('')
}

function makeTxHash() {
  return `0x${randomHex(64)}`
}

function makeTxId() {
  return `tx_${Date.now()}_${randomHex(4)}`
}

function makeWalletTxId() {
  return `wtx_${Date.now()}_${randomHex(4)}`
}

function parseTicketEventDate(value: string) {
  const match = value.match(/(\d{4})\.(\d{2})\.(\d{2})/)
  if (!match) return null

  const [, year, month, day] = match
  return new Date(Number(year), Number(month) - 1, Number(day), 0, 0, 0, 0)
}

function getDefaultRefundRules(): RefundRule[] {
  return [
    { id: 'default_r1', daysBefore: 7, feeRate: 0, label: '공연 7일 전까지 전액 환불' },
    { id: 'default_r2', daysBefore: 3, feeRate: 0.1, label: '공연 3일 전까지 수수료 10%' },
    { id: 'default_r3', daysBefore: 1, feeRate: 0.2, label: '공연 1일 전까지 수수료 20%' },
    { id: 'default_r4', daysBefore: 0, feeRate: 1, label: '공연 당일 환불 불가' },
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

  return {
    refundable,
    feeRate,
    feeAmount,
    refundAmount,
    daysLeft,
    appliedRule,
  }
}

interface AppContextValue {
  user: User | null
  login: (id: string, password: string) => boolean
  logout: () => void
  screen: Screen
  activeTab: Tab
  navParams: NavParams
  navigate: (screen: Screen, params?: NavParams) => void
  navigateTab: (tab: Tab) => void
  goBack: () => void
  tickets: Ticket[]
  resaleItems: ResaleItem[]
  events: Event[]
  addTicket: (ticket: Ticket) => void
  updateTicketStatus: (id: string, updates: Partial<Ticket>) => void
  addResaleItem: (item: ResaleItem) => void
  removeResaleItem: (id: string) => void
  buyResaleTicket: (resaleItemId: string) => string | false
  topUpCTK: (amount: number) => void
  transferTicket: (
    ticketId: string,
    recipientPhone: string
  ) => { success: boolean; recipientName?: string; reason?: 'LIMIT_EXCEEDED' | 'USER_NOT_FOUND' | 'NETWORK' }
  refundTicket: (
    ticketId: string
  ) => { success: boolean; refundAmount?: number; feeAmount?: number; reason?: 'NOT_FOUND' | 'NOT_ELIGIBLE' }
  wishlist: string[]
  toggleWishlist: (eventId: string) => void
  isWishlisted: (eventId: string) => boolean
  txRecords: TxRecord[]
  addTx: (type: TxType, label: string, amount?: number) => TxRecord
  walletTxs: WalletTx[]
  addWalletTx: (type: WalletTxType, label: string, amount: number) => WalletTx
  allowNotifications: boolean
  setAllowNotifications: (enabled: boolean) => void
}

const AppContext = createContext<AppContextValue | null>(null)

const INITIAL_WALLET_TXS: WalletTx[] = [
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
    label: 'AESPA WORLD TOUR 2026 티켓 구매',
    amount: -140000,
    balance: 144400,
    createdAt: Date.now() - 86400000 * 9,
  },
]

const MOCK_PHONE_BOOK: Record<string, string> = {
  '010-9876-5432': '김서연',
  '010-1234-5678': '김민준',
  '010-5555-4444': '이지안',
}

export function AppProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [screen, setScreen] = useState<Screen>('login')
  const [activeTab, setActiveTab] = useState<Tab>('home')
  const [navParams, setNavParams] = useState<NavParams>({})
  const [screenHistory, setScreenHistory] = useState<Array<{ screen: Screen; params: NavParams }>>([])
  const [tickets, setTickets] = useState<Ticket[]>(MOCK_TICKETS)
  const [resaleItems, setResaleItems] = useState<ResaleItem[]>(MOCK_RESALE_ITEMS)
  const [events] = useState<Event[]>(MOCK_EVENTS)
  const [wishlist, setWishlist] = useState<string[]>(['evt_001'])
  const [txRecords, setTxRecords] = useState<TxRecord[]>([])
  const [walletTxs, setWalletTxs] = useState<WalletTx[]>(INITIAL_WALLET_TXS)
  const [allowNotifications, setAllowNotifications] = useState(true)

  const login = useCallback((id: string, _password: string) => {
    if (id.length === 0) return false

    setUser(MOCK_USER)
    setScreen('home')
    setActiveTab('home')
    return true
  }, [])

  const logout = useCallback(() => {
    setUser(null)
    setScreen('login')
    setScreenHistory([])
  }, [])

  const navigate = useCallback(
    (newScreen: Screen, params: NavParams = {}) => {
      setScreenHistory((prev) => [...prev, { screen, params: navParams }])
      setScreen(newScreen)
      setNavParams(params)
    },
    [navParams, screen]
  )

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
      const nextHistory = [...prev]
      const last = nextHistory.pop()

      if (last) {
        setScreen(last.screen)
        setNavParams(last.params)
      }

      return nextHistory
    })
  }, [])

  const addTicket = useCallback((ticket: Ticket) => {
    setTickets((prev) => [ticket, ...prev])
  }, [])

  const updateTicketStatus = useCallback((id: string, updates: Partial<Ticket>) => {
    setTickets((prev) => prev.map((ticket) => (ticket.id === id ? { ...ticket, ...updates } : ticket)))
  }, [])

  const addResaleItem = useCallback((item: ResaleItem) => {
    setResaleItems((prev) => [item, ...prev])
  }, [])

  const removeResaleItem = useCallback((id: string) => {
    setResaleItems((prev) => prev.filter((item) => item.id !== id))
  }, [])

  const buyResaleTicket = useCallback(
    (resaleItemId: string): string | false => {
      const item = resaleItems.find((resaleItem) => resaleItem.id === resaleItemId)
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
      setResaleItems((prev) => prev.filter((resaleItem) => resaleItem.id !== resaleItemId))
      setUser((prev) => (prev ? { ...prev, ctkBalance: prev.ctkBalance - item.resalePrice } : prev))

      return newId
    },
    [resaleItems, user]
  )

  const topUpCTK = useCallback((amount: number) => {
    setUser((prev) => {
      if (!prev) return prev

      const newBalance = prev.ctkBalance + amount
      const walletTx: WalletTx = {
        id: makeWalletTxId(),
        type: 'CHARGE',
        label: `CTK ${amount.toLocaleString()} 충전`,
        amount,
        balance: newBalance,
        createdAt: Date.now(),
      }

      setWalletTxs((current) => [walletTx, ...current])

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

  const addWalletTx = useCallback(
    (type: WalletTxType, label: string, amount: number): WalletTx => {
      const lastBalance = walletTxs.length > 0 ? walletTxs[0].balance : user?.ctkBalance || 0
      const newBalance = lastBalance + amount
      const walletTx: WalletTx = {
        id: makeWalletTxId(),
        type,
        label,
        amount,
        balance: newBalance,
        createdAt: Date.now(),
      }

      setWalletTxs((prev) => [walletTx, ...prev])
      return walletTx
    },
    [user, walletTxs]
  )

  const toggleWishlist = useCallback((eventId: string) => {
    setWishlist((prev) => (prev.includes(eventId) ? prev.filter((id) => id !== eventId) : [...prev, eventId]))
  }, [])

  const isWishlisted = useCallback((eventId: string) => wishlist.includes(eventId), [wishlist])

  const transferTicket = useCallback(
    (
      ticketId: string,
      recipientPhone: string
    ): { success: boolean; recipientName?: string; reason?: 'LIMIT_EXCEEDED' | 'USER_NOT_FOUND' | 'NETWORK' } => {
      const recipientName = MOCK_PHONE_BOOK[recipientPhone]
      if (!recipientName) {
        return { success: false, reason: 'USER_NOT_FOUND' }
      }

      const ticket = tickets.find((item) => item.id === ticketId)
      if (!ticket) {
        return { success: false, reason: 'NETWORK' }
      }

      setTickets((prev) =>
        prev.map((item) => (item.id === ticketId ? { ...item, status: 'USED' as const } : item))
      )

      return { success: true, recipientName }
    },
    [tickets]
  )

  const refundTicket = useCallback(
    (
      ticketId: string
    ): { success: boolean; refundAmount?: number; feeAmount?: number; reason?: 'NOT_FOUND' | 'NOT_ELIGIBLE' } => {
      const ticket = tickets.find((item) => item.id === ticketId)
      if (!ticket) return { success: false, reason: 'NOT_FOUND' }

      const event = events.find((item) => item.id === ticket.eventId)
      const policy = getRefundPolicy(ticket, event)

      if (!policy.refundable || !user) {
        return { success: false, reason: 'NOT_ELIGIBLE' }
      }

      setTickets((prev) =>
        prev.map((item) =>
          item.id === ticketId ? { ...item, status: 'EXPIRED' as const, resalePrice: undefined } : item
        )
      )

      setUser((prev) => (prev ? { ...prev, ctkBalance: prev.ctkBalance + policy.refundAmount } : prev))

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
    },
    [events, tickets, user]
  )

  return (
    <AppContext.Provider
      value={{
        user,
        login,
        logout,
        screen,
        activeTab,
        navParams,
        navigate,
        navigateTab,
        goBack,
        tickets,
        resaleItems,
        events,
        addTicket,
        updateTicketStatus,
        addResaleItem,
        removeResaleItem,
        buyResaleTicket,
        topUpCTK,
        transferTicket,
        refundTicket,
        wishlist,
        toggleWishlist,
        isWishlisted,
        txRecords,
        addTx,
        walletTxs,
        addWalletTx,
        allowNotifications,
        setAllowNotifications,
      }}
    >
      {children}
    </AppContext.Provider>
  )
}

export function useApp() {
  const context = useContext(AppContext)
  if (!context) throw new Error('useApp must be used within AppProvider')
  return context
}
