"use client"

import { useEffect } from "react"
import { useToast } from "@/hooks/use-toast"

export function AlertToastBridge() {
  const { toast } = useToast()

  useEffect(() => {
    const originalAlert = window.alert

    window.alert = (message?: string) => {
      const normalizedMessage =
        typeof message === "string" && message.trim()
          ? message.trim()
          : "요청이 완료되었습니다."

      toast({
        title: "알림",
        description: normalizedMessage,
      })
    }

    return () => {
      window.alert = originalAlert
    }
  }, [toast])

  return null
}
