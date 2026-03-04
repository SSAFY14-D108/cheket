"use client"

import * as React from "react"
import { cn } from "@/lib/utils"

export interface LoginInputProps
  extends React.InputHTMLAttributes<HTMLInputElement> {
  /** Input field type (e.g. "text", "password", "email") */
  type?: string
  /** Placeholder text displayed when input is empty */
  placeholder?: string
  /** Current value of the input */
  value?: string
  /** Callback fired when the input value changes */
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void
  /** Whether the input is disabled */
  disabled?: boolean
  /** Additional CSS class names */
  className?: string
}

const LoginInput = React.forwardRef<HTMLInputElement, LoginInputProps>(
  ({ className, type = "text", ...props }, ref) => {
    return (
      <input
        type={type}
        ref={ref}
        className={cn(
          "h-14 w-full rounded-sm border-0 bg-muted px-4 text-base text-foreground placeholder:text-muted-foreground",
          "outline-none transition-colors",
          "focus:ring-2 focus:ring-ring",
          "disabled:cursor-not-allowed disabled:opacity-50",
          className
        )}
        {...props}
      />
    )
  }
)

LoginInput.displayName = "LoginInput"

export { LoginInput }
