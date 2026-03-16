'use client'

import { useState } from 'react'
import { AlertTriangle, ChevronRight } from 'lucide-react'
import { useApp } from '@/lib/app-context'
import { AppShell } from '../app-shell'
import { cn } from '@/lib/utils'

const REASONS = ['서비스 사용 빈도가 낮아요.', '혜택이나 기능이 기대와 달랐어요.', '다른 계정을 사용하려고 해요.', '개인정보 처리에 대한 우려가 있어요.', '기타']

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
    logout()
  }

  return (
    <AppShell title="회원 탈퇴" showBack onBack={step === 'confirm' ? () => setStep('reason') : goBack} showBottomNav={false} hideProfileIcon>
      <div className="flex h-full flex-col">
        <div className="flex flex-1 flex-col gap-6 overflow-y-auto px-4 py-6">
          {step === 'reason' ? (
            <>
              <div className="flex gap-3 rounded-xl border border-red-200 bg-red-50 p-4">
                <AlertTriangle className="mt-0.5 h-5 w-5 flex-shrink-0 text-red-500" />
                <div className="flex flex-col gap-1">
                  <p className="text-sm font-semibold text-red-600">탈퇴 전 꼭 확인해 주세요</p>
                  <ul className="list-inside list-disc space-y-1 text-xs text-red-500">
                    <li>보유 중인 티켓과 거래 내역은 복구할 수 없어요.</li>
                    <li>CTK 잔액은 탈퇴 후 되돌릴 수 없어요.</li>
                    <li>탈퇴 후에는 같은 계정으로 즉시 복구되지 않습니다.</li>
                  </ul>
                </div>
              </div>

              <div>
                <p className="mb-3 text-sm font-bold text-[#111111]">탈퇴 사유를 선택해 주세요</p>
                <div className="flex flex-col gap-2">
                  {REASONS.map((reason) => (
                    <button
                      key={reason}
                      onClick={() => setSelectedReason(reason)}
                      className={cn(
                        'gradient-outline-surface flex w-full items-center justify-between rounded-xl px-4 py-3.5 text-left text-sm transition-all',
                        selectedReason === reason ? 'font-medium text-[#111111]' : 'text-foreground'
                      )}
                    >
                      {reason}
                      {selectedReason === reason && (
                        <div className="gradient-outline-icon-button flex h-4 w-4 items-center justify-center rounded-full">
                          <div className="h-1.5 w-1.5 rounded-full bg-[#111111]" />
                        </div>
                      )}
                    </button>
                  ))}
                </div>
              </div>
            </>
          ) : (
            <>
              <div className="flex flex-col gap-4">
                <div className="flex flex-col items-center gap-3 py-4 text-center">
                  <div className="flex h-16 w-16 items-center justify-center rounded-full bg-red-100">
                    <AlertTriangle className="h-8 w-8 text-red-500" />
                  </div>
                  <p className="text-base font-bold text-[#111111]">정말 탈퇴하시겠어요?</p>
                  <p className="text-sm leading-relaxed text-muted-foreground">
                    탈퇴하면 계정과 관련 데이터가 모두 삭제되며
                    <br />
                    다시 복구할 수 없습니다.
                  </p>
                </div>

                <div className="gradient-outline-surface rounded-xl p-4 text-sm text-muted-foreground">
                  <p className="mb-2 font-medium text-[#111111]">탈퇴 사유</p>
                  <p>{selectedReason}</p>
                </div>

                <button onClick={() => setConfirmed(!confirmed)} className="gradient-outline-surface flex items-center gap-3 rounded-xl px-4 py-3 text-left transition-all">
                  <div className={cn('flex h-5 w-5 items-center justify-center rounded border-2 transition-all', confirmed ? 'border-red-500 bg-red-500' : 'border-border')}>
                    {confirmed && (
                      <svg className="h-3 w-3 text-white" fill="none" viewBox="0 0 12 12">
                        <path d="M2 6l3 3 5-5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    )}
                  </div>
                  <span className="text-sm text-foreground">위 내용을 확인했고, 탈퇴에 동의합니다.</span>
                </button>
              </div>
            </>
          )}
        </div>

        <div className="border-t border-border p-4">
          {step === 'reason' ? (
            <button onClick={handleNext} disabled={!selectedReason} className="gradient-outline-button flex w-full items-center justify-center gap-1 rounded-xl py-3.5 text-sm font-semibold text-[#111111] disabled:opacity-40 disabled:cursor-not-allowed">
              다음 <ChevronRight className="h-4 w-4" />
            </button>
          ) : (
            <button onClick={handleWithdraw} disabled={!confirmed} className="w-full rounded-xl bg-red-500 py-3.5 text-sm font-semibold text-white transition-all hover:bg-red-600 disabled:cursor-not-allowed disabled:opacity-40">
              회원 탈퇴
            </button>
          )}
        </div>
      </div>
    </AppShell>
  )
}
