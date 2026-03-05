'use client'

import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { User, Phone, Wallet, LogOut, ChevronRight, Copy, Heart, Settings } from 'lucide-react'
import { useState } from 'react'

export function MyPageScreen() {
  const { user, logout, navigate, goBack, wishlist } = useApp()
  const [copied, setCopied] = useState(false)

  if (!user) return null

  const copyAddress = () => {
    navigator.clipboard.writeText(user.walletAddress).catch(() => {})
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const menuItems = [
    { label: '공지사항', icon: null },
    { label: '이용약관', icon: null },
    { label: '개인정보처리방침', icon: null },
    { label: '고객센터', icon: null },
  ]

  return (
    <AppShell title="마이 페이지" showBack onBack={goBack} showBottomNav={false} hideProfileIcon>
      <div className="flex flex-col gap-4 p-4 pb-8">
        {/* Profile header */}
        <div className="bg-card rounded-xl border border-border p-5">
          <div className="flex items-center gap-4 mb-4">
            <div className="w-14 h-14 rounded-full bg-primary/10 border border-primary/30 flex items-center justify-center flex-shrink-0">
              <User className="w-7 h-7 text-primary" />
            </div>
            <div>
              <h2 className="font-bold text-base text-foreground">{user.name}</h2>
              <p className="text-sm text-muted-foreground">Cheket 회원</p>
            </div>
          </div>

          <div className="flex flex-col gap-2.5">
            <div className="flex items-center gap-2 text-sm">
              <Phone className="w-4 h-4 text-primary flex-shrink-0" />
              <span className="text-muted-foreground">전화번호</span>
              <span className="ml-auto text-foreground font-medium">{user.phone}</span>
            </div>
            <div className="flex items-center gap-2 text-sm">
              <Wallet className="w-4 h-4 text-primary flex-shrink-0" />
              <span className="text-muted-foreground">지갑 주소</span>
              <span className="ml-auto text-foreground font-mono text-xs">{user.walletAddress}</span>
              <button onClick={copyAddress} className="text-muted-foreground hover:text-foreground transition-colors" aria-label="주소 복사">
                <Copy className="w-3.5 h-3.5" />
              </button>
            </div>
            {copied && <p className="text-xs text-primary text-right">복사되었습니다!</p>}
          </div>
        </div>

        {/* CTK Balance */}
        <div className="bg-primary/10 border border-primary/20 rounded-xl p-5">
          <p className="text-xs text-muted-foreground mb-1">내 CTK 잔액</p>
          <div className="flex items-end gap-2">
            <span className="font-bold text-3xl text-primary">{user.ctkBalance.toLocaleString()}</span>
            <span className="text-sm text-muted-foreground mb-0.5">CTK</span>
          </div>
          <p className="text-xs text-muted-foreground mt-2">
            ≈ {(user.ctkBalance * 1.2).toLocaleString()}원
          </p>
          <button className="mt-3 bg-primary text-primary-foreground text-xs font-semibold px-4 py-2 rounded-lg hover:opacity-90 transition-opacity"
            onClick={() => navigate('wallet')}
          >
            지갑 관리
          </button>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-3 gap-3">
          <div className="bg-card border border-border rounded-xl p-3 text-center">
            <p className="font-bold text-xl text-foreground">2</p>
            <p className="text-xs text-muted-foreground mt-0.5">보유 티켓</p>
          </div>
          <div className="bg-card border border-border rounded-xl p-3 text-center">
            <p className="font-bold text-xl text-foreground">1</p>
            <p className="text-xs text-muted-foreground mt-0.5">관람 완료</p>
          </div>
          <button 
            onClick={() => navigate('wishlist')}
            className="bg-card border border-border rounded-xl p-3 text-center hover:border-primary/40 active:scale-[0.98] transition-all"
          >
            <div className="flex items-center justify-center gap-1">
              <Heart className="w-4 h-4 text-primary" />
              <p className="font-bold text-xl text-foreground">{wishlist.length}</p>
            </div>
            <p className="text-xs text-muted-foreground mt-0.5">찜한 콘서트</p>
          </button>
        </div>

        {/* TX History Button */}
        <button
          onClick={() => navigate('tx-history')}
          className="w-full bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm hover:bg-accent/30 active:scale-[0.98] transition-all flex items-center justify-between px-4"
        >
          <span>티켓 거래 내역</span>
          <ChevronRight className="w-4 h-4 text-muted-foreground" />
        </button>

        {/* Settings Button */}
        <button
          onClick={() => navigate('settings')}
          className="w-full bg-secondary border border-border text-foreground font-semibold py-3.5 rounded-xl text-sm hover:bg-accent/30 active:scale-[0.98] transition-all flex items-center justify-between px-4"
        >
          <div className="flex items-center gap-2">
            <Settings className="w-4 h-4" />
            <span>설정</span>
          </div>
          <ChevronRight className="w-4 h-4 text-muted-foreground" />
        </button>

        {/* Menu */}
        <div className="bg-card rounded-xl border border-border overflow-hidden">
          {menuItems.map(({ label }, i) => (
            <button
              key={label}
              className="w-full flex items-center justify-between px-4 py-3.5 hover:bg-secondary transition-colors text-sm text-foreground border-b border-border last:border-none"
            >
              {label}
              <ChevronRight className="w-4 h-4 text-muted-foreground" />
            </button>
          ))}
        </div>

        {/* Logout */}
        <button
          onClick={logout}
          className="w-full flex items-center justify-center gap-2 bg-secondary border border-border text-red-400 font-semibold py-3.5 rounded-xl text-sm hover:bg-red-600/10 hover:border-red-600/30 active:scale-[0.98] transition-all"
        >
          <LogOut className="w-4 h-4" /> 로그아웃
        </button>

        {/* Withdraw */}
        <button
          onClick={() => navigate('withdraw')}
          className="w-full text-center text-xs text-muted-foreground underline underline-offset-2 hover:text-foreground transition-colors py-1"
        >
          회원 탈퇴
        </button>

        <p className="text-xs text-muted-foreground text-center">Cheket v1.0.0</p>
      </div>
    </AppShell>
  )
}
