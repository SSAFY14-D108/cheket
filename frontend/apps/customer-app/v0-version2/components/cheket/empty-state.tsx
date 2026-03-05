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
    <div className="flex flex-col items-center justify-center py-16 px-6 text-center">
      <div className="w-16 h-16 rounded-full bg-secondary flex items-center justify-center mb-4">
        {icon ?? <TicketIcon className="w-7 h-7 text-muted-foreground" />}
      </div>
      <p className="text-foreground font-medium text-base mb-1">{title}</p>
      {description && (
        <p className="text-muted-foreground text-sm leading-relaxed">{description}</p>
      )}
    </div>
  )
}
