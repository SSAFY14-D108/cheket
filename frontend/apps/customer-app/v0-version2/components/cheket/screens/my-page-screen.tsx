'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import {
  User,
  Phone,
  Mail,
  LogOut,
  ChevronRight,
  Heart,
  Settings,
  Wallet,
  Ticket,
  BadgeCheck,
  Receipt,
} from 'lucide-react'

export function MyPageScreen() {
  const { user, logout, navigate, navigateTab, wishlist, tickets } = useApp()

  if (!user) return null

  const holdingCount = tickets.filter((ticket) => ticket.status === 'SOLD' || ticket.status === 'LISTED').length
  const attendedCount = tickets.filter((ticket) => ticket.status === 'USED').length

  const quickLinks = [
    {
      label: '보유티켓',
      value: `${holdingCount}장`,
      icon: Ticket,
      onClick: () => navigateTab('my-tickets'),
    },
    {
      label: '관람완료',
      value: `${attendedCount}건`,
      icon: BadgeCheck,
      onClick: () => navigate('collection'),
    },
    {
      label: '찜한공연',
      value: `${wishlist.length}건`,
      icon: Heart,
      onClick: () => navigate('wishlist'),
    },
    {
      label: '거래내역',
      value: '확인',
      icon: Receipt,
      onClick: () => navigate('tx-history'),
    },
  ]

  const menuItems = [
    { label: '공연 문의내역' },
    { label: '공지사항' },
    { label: '이벤트 및 혜택 알림' },
    { label: '고객센터' },
  ]

  return (
    <AppShell title="마이페이지" hideProfileIcon>
      <div className="flex flex-col gap-4 p-4 pb-8">
        <section className="rounded-2xl border border-border bg-card p-5">
          <div className="flex items-start gap-4">
            <div
              className="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-full border-[3px] border-transparent bg-white"
              style={{
                backgroundImage:
                  'linear-gradient(#FFFFFF, #FFFFFF), linear-gradient(135deg, rgba(226, 218, 255, 0.76), rgba(196, 247, 224, 0.74), rgba(202, 230, 255, 0.76))',
                backgroundOrigin: 'border-box',
                backgroundClip: 'padding-box, border-box',
              }}
            >
              <User className="h-7 w-7 text-[#333333]" />
            </div>
            <div className="min-w-0 flex-1">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-xs font-medium uppercase tracking-[0.18em] text-muted-foreground">
                    My Account
                  </p>
                  <h2 className="mt-1 text-lg font-bold text-foreground">{user.name}</h2>
                </div>
                <button
                  onClick={() => navigate('settings')}
                  className="flex h-9 w-9 items-center justify-center rounded-full border-2 border-transparent bg-white text-muted-foreground transition-colors hover:text-foreground"
                  style={{
                    backgroundImage:
                      'linear-gradient(#FFFFFF, #FFFFFF), linear-gradient(135deg, rgba(226, 218, 255, 0.76), rgba(196, 247, 224, 0.74), rgba(202, 230, 255, 0.76))',
                    backgroundOrigin: 'border-box',
                    backgroundClip: 'padding-box, border-box',
                  }}
                  aria-label="설정"
                >
                  <Settings className="h-4 w-4" />
                </button>
              </div>

              <div className="mt-4 grid gap-2.5 text-sm">
                <div className="flex items-center gap-2">
                  <Phone className="h-4 w-4 flex-shrink-0 text-[#333333]" />
                  <span className="text-muted-foreground">전화번호</span>
                  <span className="ml-auto font-medium text-foreground">{user.phone}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Mail className="h-4 w-4 flex-shrink-0 text-[#333333]" />
                  <span className="text-muted-foreground">이메일</span>
                  <span className="ml-auto truncate text-foreground">{user.email}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section
          className="rounded-2xl border-2 border-transparent bg-white p-5"
          style={{
            backgroundImage:
              'linear-gradient(#FFFFFF, #FFFFFF), linear-gradient(135deg, rgba(226, 218, 255, 0.76), rgba(196, 247, 224, 0.74), rgba(202, 230, 255, 0.76))',
            backgroundOrigin: 'border-box',
            backgroundClip: 'padding-box, border-box',
          }}
        >
          <p className="text-xs font-medium uppercase tracking-[0.16em] text-muted-foreground">Wallet</p>
          <div className="mt-2 flex items-end gap-2">
            <span className="text-3xl font-bold text-[#111111]">{user.ctkBalance.toLocaleString()}</span>
            <span className="mb-1 text-sm text-muted-foreground">CTK</span>
          </div>
          <p className="mt-1 text-sm text-muted-foreground">
            예상 환산 금액 약 {(user.ctkBalance * 1.2).toLocaleString()}원
          </p>
          <button
            className="mt-4 inline-flex items-center gap-2 rounded-xl border-2 border-transparent bg-white px-4 py-2.5 text-sm font-semibold text-[#111111] transition-all active:scale-[0.98]"
            style={{
              backgroundImage:
                'linear-gradient(#FFFFFF, #FFFFFF), linear-gradient(135deg, rgba(226, 218, 255, 0.76), rgba(196, 247, 224, 0.74), rgba(202, 230, 255, 0.76))',
              backgroundOrigin: 'border-box',
              backgroundClip: 'padding-box, border-box',
            }}
            onClick={() => navigate('wallet')}
          >
            <Wallet className="h-4 w-4 text-[#333333]" />
            지갑 보기
          </button>
        </section>

        <section className="rounded-2xl border border-border bg-card p-4">
          <div className="mb-3">
            <h3 className="text-sm font-semibold text-foreground">빠른 이동</h3>
            <p className="mt-1 text-xs text-muted-foreground">자주 보는 메뉴를 바로 이동할 수 있어요.</p>
          </div>

          <div className="grid grid-cols-2 gap-3">
            {quickLinks.map(({ label, value, icon: Icon, onClick }) => (
              <button
                key={label}
                onClick={onClick}
                className="rounded-xl border-2 border-transparent bg-white p-4 text-left shadow-[0_10px_24px_rgba(15,23,42,0.04)] transition-all active:scale-[0.98]"
                style={{
                  backgroundImage:
                    'linear-gradient(#FFFFFF, #FFFFFF), linear-gradient(135deg, rgba(226, 218, 255, 0.5), rgba(196, 247, 224, 0.46), rgba(202, 230, 255, 0.5))',
                  backgroundOrigin: 'border-box',
                  backgroundClip: 'padding-box, border-box',
                }}
              >
                <div className="flex items-center justify-between">
                  <Icon className="h-4 w-4 text-[#333333]" />
                  <span className="text-xs font-semibold text-foreground">{value}</span>
                </div>
                <p className="mt-5 text-sm font-semibold text-foreground">{label}</p>
              </button>
            ))}
          </div>
        </section>

        <section className="overflow-hidden rounded-2xl border border-border bg-card">
          <div className="border-b border-border px-4 py-3">
            <h3 className="text-sm font-semibold text-foreground">계정 및 서비스</h3>
          </div>
          {menuItems.map(({ label }) => (
            <button
              key={label}
              className="flex w-full items-center justify-between border-b border-border px-4 py-3.5 text-sm text-foreground transition-colors last:border-none hover:bg-secondary"
            >
              <span>{label}</span>
              <ChevronRight className="h-4 w-4 text-muted-foreground" />
            </button>
          ))}
        </section>

        <button
          onClick={logout}
          className="flex w-full items-center justify-center gap-2 rounded-xl border-2 border-transparent py-4 text-sm font-semibold text-[#111111] transition-all active:scale-[0.98]"
          style={{
            backgroundImage:
              'linear-gradient(#FFFFFF, #FFFFFF), linear-gradient(135deg, rgba(226, 218, 255, 0.76), rgba(196, 247, 224, 0.74), rgba(202, 230, 255, 0.76))',
            backgroundOrigin: 'border-box',
            backgroundClip: 'padding-box, border-box',
          }}
        >
          <LogOut className="h-4 w-4" />
          로그아웃
        </button>

        <button
          onClick={() => navigate('withdraw')}
          className="py-1 text-center text-xs text-muted-foreground underline underline-offset-2 transition-colors hover:text-foreground"
        >
          회원 탈퇴
        </button>

        <p className="text-center text-xs text-muted-foreground">Cheket v1.0.0</p>
      </div>
    </AppShell>
  )
}
