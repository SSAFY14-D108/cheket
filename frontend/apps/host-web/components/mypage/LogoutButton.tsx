"use client"

import { useRouter } from "next/navigation"
import { useToast } from "@/hooks/use-toast"
import { LogOut } from "lucide-react"
import { logoutHost } from "@/lib/auth-api"
import { clearAuthTokens } from "@/lib/auth-storage"

export function LogoutButton() {
  const router = useRouter()
  const { toast } = useToast()

  const handleLogout = async () => {
    try {
      await logoutHost()
    } catch {
      // Local auth state must always be cleared even if logout API fails.
    } finally {
      clearAuthTokens()
      toast({
        title: "로그아웃 완료",
        description: "로그인 페이지로 이동합니다.",
      })
      router.push("/")
      router.refresh()
    }
  }

  return (
    <button
      type="button"
      onClick={handleLogout}
      className="flex items-center gap-2 rounded-md bg-secondary px-4 py-2 text-sm font-semibold text-secondary-foreground shadow-sm transition-all hover:bg-secondary/80"
    >
      <LogOut className="size-4" />
      <span>로그아웃</span>
    </button>
  )
}
