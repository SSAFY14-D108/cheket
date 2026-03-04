'use client'
/* eslint-disable @next/next/no-img-element */

import { useState } from 'react'
import { cn } from '@/lib/utils'

interface LogoProps {
  className?: string
}

export function CheketWordmark({ className }: LogoProps) {
  const [failed, setFailed] = useState(false)

  if (failed) {
    return <span className={cn('font-bold tracking-tight text-primary', className)}>cheket</span>
  }

  return (
    <img
      src="/logos/cheket-wordmark.webp"
      alt="CHEKET"
      className={cn('object-contain', className)}
      onError={() => setFailed(true)}
    />
  )
}

export function CheketTicketLogo({ className }: LogoProps) {
  const [failed, setFailed] = useState(false)

  if (failed) {
    return (
      <div className={cn('rounded-2xl bg-primary/10 border border-primary/30 flex items-center justify-center', className)}>
        <span className="text-primary font-bold text-2xl">C</span>
      </div>
    )
  }

  return (
    <img
      src="/logos/cheket-ticket.webp"
      alt="CHEKET Logo Ticket"
      className={cn('object-contain', className)}
      onError={() => setFailed(true)}
    />
  )
}
