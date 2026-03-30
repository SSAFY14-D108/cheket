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
        "bg-[#171717] text-white hover:bg-[#2a2a2a] shadow-[0_12px_30px_rgba(23,23,23,0.16)]",
      secondary:
        "border border-black/10 bg-white text-black hover:bg-black/[0.03]",
    }

    return (
      <button
        ref={ref}
        className={cn(
          "inline-flex h-15 w-full items-center justify-center gap-2 rounded-[1.35rem] px-4 text-base font-semibold",
          "outline-none transition-all duration-200",
          "focus-visible:ring-2 focus-visible:ring-black/15 focus-visible:ring-offset-2 focus-visible:ring-offset-transparent",
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
