'use client'

import { ReactNode } from 'react'
import { Ticket as TicketIcon } from 'lucide-react'

interface EmptyStateProps {
  title: string
  description?: string
  icon?: ReactNode
}

export function EmptyState({ title, description, icon }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center px-6 py-14 text-center">
      <div className="mb-5 flex h-20 w-20 items-center justify-center rounded-full bg-[#f3f4f6]">
        {icon ?? <TicketIcon className="h-8 w-8 text-[#7a8594]" />}
      </div>
      <p className="mb-2 text-xl font-bold tracking-[-0.02em] text-[#111111]">{title}</p>
      {description && (
        <p className="max-w-[240px] text-sm leading-7 text-[#6f7b88]">{description}</p>
      )}
    </div>
  )
}
