"use client"

import * as React from "react"
import { cn } from "@/lib/utils"

export interface LoginButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  /** Button variant: "primary" for main actions, "secondary" for alternatives */
  variant?: "primary" | "secondary"
  /** Button label text (can also use children) */
  label?: string
  /** Click event handler */
  onClick?: (e: React.MouseEvent<HTMLButtonElement>) => void
  /** Button type attribute */
  type?: "button" | "submit" | "reset"
  /** Whether the button is disabled */
  disabled?: boolean
  /** Additional CSS class names */
  className?: string
  /** Button content */
  children?: React.ReactNode
}

const LoginButton = React.forwardRef<HTMLButtonElement, LoginButtonProps>(
  ({ className, variant = "primary", label, children, ...props }, ref) => {
    const variantStyles = {
      primary:
        "bg-primary text-primary-foreground shadow hover:bg-primary/90",
      secondary:
        "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
    }

    return (
      <button
        ref={ref}
        className={cn(
          "h-12 w-full rounded-sm text-base font-semibold",
          "outline-none transition-colors",
          "focus-visible:ring-2 focus-visible:ring-ring",
          "disabled:cursor-not-allowed disabled:opacity-50",
          variantStyles[variant],
          className
        )}
        {...props}
      >
        {children ?? label}
      </button>
    )
  }
)

LoginButton.displayName = "LoginButton"

export { LoginButton }
