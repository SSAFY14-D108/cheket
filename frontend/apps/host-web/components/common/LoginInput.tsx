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
          "h-15 w-full rounded-[1.35rem] border border-black/10 bg-[#f7f7f8] px-5 text-base text-black placeholder:text-black/30",
          "outline-none transition-all duration-200",
          "focus:border-black/25 focus:bg-white focus:ring-4 focus:ring-black/5",
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
