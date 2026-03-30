"use client"

import { usePathname } from "next/navigation"
import { AppFooter } from "@/components/layout/AppFooter"

export function ConditionalFooter() {
  const pathname = usePathname()

  if (pathname === "/" || pathname === "/signup") {
    return null
  }

  return <AppFooter />
}
