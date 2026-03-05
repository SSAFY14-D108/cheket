'use client'

import { useState } from 'react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { AlertTriangle, ChevronRight } from 'lucide-react'
import { cn } from '@/lib/utils'

const REASONS = [
  '서비스를 더 이상 이용하지 않아요',
  '개인정보가 걱정돼요',
  '다른 계정으로 새로 가입하고 싶어요',
  '앱 사용이 불편해요',
  '기타',
]

export function WithdrawScreen() {
  const { goBack, logout } = useApp()
  const [selectedReason, setSelectedReason] = useState<string | null>(null)
  const [confirmed, setConfirmed] = useState(false)
  const [step, setStep] = useState<'reason' | 'confirm'>('reason')

  const handleNext = () => {
    if (!selectedReason) return
    setStep('confirm')
  }

  const handleWithdraw = () => {
    if (!confirmed) return
    // TODO: API call to delete account
    logout()
  }

  return (
    <AppShell title="회원 탈퇴" showBack onBack={step === 'confirm' ? () => setStep('reason') : goBack} showBottomNav={false} hideProfileIcon>
      <div className="flex flex-col h-full">
        <div className="flex-1 overflow-y-auto px-4 py-6 flex flex-col gap-6">

          {step === 'reason' ? (
            <>
              {/* Warning banner */}
              <div className="flex gap-3 bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-900/50 rounded-xl p-4">
                <AlertTriangle className="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
                <div className="flex flex-col gap-1">
                  <p className="text-sm font-semibold text-red-600 dark:text-red-400">탈퇴 전 확인해주세요</p>
                  <ul className="text-xs text-red-500 dark:text-red-400 space-y-1 list-disc list-inside">
                    <li>보유 티켓 및 NFT 컬렉션이 모두 삭제됩니다.</li>
                    <li>CTK 잔액은 환불되지 않습니다.</li>
                    <li>탈퇴 후 동일 계정으로 재가입이 어렵습니다.</li>
                  </ul>
                </div>
              </div>

              {/* Reason selection */}
              <div>
                <p className="text-sm font-bold text-foreground mb-3">탈퇴 사유를 선택해주세요</p>
                <div className="flex flex-col gap-2">
                  {REASONS.map((reason) => (
                    <button
                      key={reason}
                      onClick={() => setSelectedReason(reason)}
                      className={cn(
                        'w-full flex items-center justify-between px-4 py-3.5 rounded-xl border text-sm text-left transition-all',
                        selectedReason === reason
                          ? 'border-primary bg-primary/5 text-primary font-medium'
                          : 'border-border bg-card text-foreground hover:border-primary/40'
                      )}
                    >
                      {reason}
                      {selectedReason === reason && (
                        <div className="w-4 h-4 rounded-full bg-primary flex items-center justify-center flex-shrink-0">
                          <div className="w-1.5 h-1.5 rounded-full bg-primary-foreground" />
                        </div>
                      )}
                    </button>
                  ))}
                </div>
              </div>
            </>
          ) : (
            <>
              {/* Final confirm step */}
              <div className="flex flex-col gap-4">
                <div className="flex flex-col items-center text-center gap-3 py-4">
                  <div className="w-16 h-16 rounded-full bg-red-100 dark:bg-red-950/40 flex items-center justify-center">
                    <AlertTriangle className="w-8 h-8 text-red-500" />
                  </div>
                  <p className="text-base font-bold text-foreground">정말 탈퇴하시겠습니까?</p>
                  <p className="text-sm text-muted-foreground leading-relaxed">
                    회원 탈퇴 시 모든 데이터가 삭제되며<br />복구할 수 없습니다.
                  </p>
                </div>

                <div className="bg-secondary border border-border rounded-xl p-4 text-sm text-muted-foreground">
                  <p className="font-medium text-foreground mb-2">탈퇴 사유</p>
                  <p>{selectedReason}</p>
                </div>

                <button
                  onClick={() => setConfirmed(!confirmed)}
                  className="flex items-center gap-3 px-4 py-3 bg-card border border-border rounded-xl hover:border-red-400/50 transition-all"
                >
                  <div className={cn(
                    'w-5 h-5 rounded border-2 flex items-center justify-center flex-shrink-0 transition-all',
                    confirmed ? 'bg-red-500 border-red-500' : 'border-border'
                  )}>
                    {confirmed && (
                      <svg className="w-3 h-3 text-white" fill="none" viewBox="0 0 12 12">
                        <path d="M2 6l3 3 5-5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    )}
                  </div>
                  <span className="text-sm text-foreground">위 내용을 확인했으며 탈퇴에 동의합니다.</span>
                </button>
              </div>
            </>
          )}
        </div>

        {/* Bottom button */}
        <div className="p-4 border-t border-border">
          {step === 'reason' ? (
            <button
              onClick={handleNext}
              disabled={!selectedReason}
              className="w-full bg-primary text-primary-foreground font-semibold py-3.5 rounded-xl text-sm hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-1"
            >
              다음 <ChevronRight className="w-4 h-4" />
            </button>
          ) : (
            <button
              onClick={handleWithdraw}
              disabled={!confirmed}
              className="w-full bg-red-500 text-white font-semibold py-3.5 rounded-xl text-sm hover:bg-red-600 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed"
            >
              회원 탈퇴
            </button>
          )}
        </div>
      </div>
    </AppShell>
  )
}
