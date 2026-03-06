'use client'

import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react'
import { Screen, Tab, NavParams, Ticket, User, ResaleItem, TxRecord, TxType, WalletTx, WalletTxType, Event } from './types'
import { MOCK_USER, MOCK_TICKETS, MOCK_RESALE_ITEMS, MOCK_EVENTS } from './mock-data'

// ── TX helper ────────────────────────────────────────────────────────────────
function randomHex(len: number) {
  return Array.from({ length: len }, () => Math.floor(Math.random() * 16).toString(16)).join('')
}
function makeTxHash() { return `0x${randomHex(64)}` }
function makeTxId()   { return `tx_${Date.now()}_${randomHex(4)}` }
function makeWalletTxId() { return `wtx_${Date.now()}_${randomHex(4)}` }

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

  return (
    <AppContext.Provider
      value={{
        user, login, logout,
        screen, activeTab, navParams, navigate, navigateTab, goBack,
        tickets, resaleItems, events,
        addTicket, updateTicketStatus, addResaleItem, removeResaleItem, buyResaleTicket, topUpCTK, transferTicket,
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
