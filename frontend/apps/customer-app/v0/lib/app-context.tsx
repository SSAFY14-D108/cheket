'use client'

import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react'
import { Screen, Tab, NavParams, Ticket, User, ResaleItem } from './types'
import { MOCK_USER, MOCK_TICKETS, MOCK_RESALE_ITEMS } from './mock-data'

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
}

const AppContext = createContext<AppContextValue | null>(null)
const MOCK_PHONE_BOOK: Record<string, string> = {
  '010-9876-5432': '박지연',
  '010-1234-5678': '김민준',
  '010-5555-4444': '이수진',
}

export function AppProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [screen, setScreen] = useState<Screen>('login')
  const [activeTab, setActiveTab] = useState<Tab>('home')
  const [navParams, setNavParams] = useState<NavParams>({})
  const [, setScreenHistory] = useState<{ screen: Screen; params: NavParams }[]>([])
  const [tickets, setTickets] = useState<Ticket[]>(MOCK_TICKETS)
  const [resaleItems, setResaleItems] = useState<ResaleItem[]>(MOCK_RESALE_ITEMS)
  const [wishlist, setWishlist] = useState<string[]>(['evt_001']) // Default: one wishlisted item

  const login = useCallback((id: string, password: string): boolean => {
    void password
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
      eventId: item.ticketId,
      eventName: item.eventName,
      eventDate: item.eventDate,
      venue: item.venue,
      poster: item.poster,
      seatId: `seat_${Date.now()}`,
      seatLabel: item.seatLabel,
      grade: item.grade,
      originalPrice: item.originalPrice,
      status: 'SOLD',
    }
    setTickets((prev) => [newTicket, ...prev])
    setResaleItems((prev) => prev.filter((r) => r.id !== resaleItemId))
    setUser((prev) => prev ? { ...prev, ctkBalance: prev.ctkBalance - item.resalePrice } : prev)
    return newId
  }, [resaleItems, user])

  const topUpCTK = useCallback((amount: number) => {
    setUser((prev) => prev ? { ...prev, ctkBalance: prev.ctkBalance + amount } : prev)
  }, [])

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
        tickets, resaleItems,
        addTicket, updateTicketStatus, addResaleItem, removeResaleItem, buyResaleTicket, topUpCTK, transferTicket,
        wishlist, toggleWishlist, isWishlisted,
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
