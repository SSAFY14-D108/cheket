"use client"

import { type FormEvent, useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { LoginInput } from "@/components/common/LoginInput"
import { LoginButton } from "@/components/common/LoginButton"
import { useToast } from "@/hooks/use-toast"
import { loginHost } from "@/lib/auth-api"
import { ApiError } from "@/lib/api"
import { setAccessToken, setRefreshToken } from "@/lib/auth-storage"

export function LoginForm() {
  const router = useRouter()
  const { toast } = useToast()
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState("")

  const handleLogin = async () => {
    if (!email.trim() || !password.trim()) {
      setErrorMessage("이메일과 비밀번호를 입력해주세요.")
      return
    }

    setIsSubmitting(true)
    setErrorMessage("")

    try {
      const response = await loginHost({
        email: email.trim(),
        password,
      })

      setAccessToken(response.data.accessToken)
      setRefreshToken(response.data.refreshToken)

      toast({
        title: "로그인 완료",
        description: response.responseMessage,
      })
      router.push("/mypage")
    } catch (error) {
      const message =
        error instanceof ApiError
          ? error.message
          : "로그인 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요."

      setErrorMessage(message)
      toast({
        title: "로그인 실패",
        description: message,
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (isSubmitting) {
      return
    }

    void handleLogin()
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-8">
      <div className="space-y-4">
        <p className="text-sm font-medium uppercase tracking-[0.28em] text-black/36">
          Login
        </p>
        <div className="space-y-2">
          <h1 className="text-5xl font-semibold tracking-[-0.06em] text-black">로그인</h1>
          <p className="text-sm leading-6 text-black/50">호스트 계정으로 접속하세요.</p>
        </div>
      </div>

      <div className="relative overflow-hidden rounded-[2rem] border border-black/8 bg-white/92 p-6 shadow-[0_24px_60px_rgba(15,23,42,0.08)] sm:p-8">
        <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(180deg,_rgba(255,255,255,0.8),_rgba(255,255,255,0.95))]" />
        <div className="relative space-y-8">
          <div className="space-y-7">
            <label className="block">
              <LoginInput
                type="email"
                placeholder="이메일"
                value={email}
                onChange={(event) => {
                  setEmail(event.target.value)
                  if (errorMessage) setErrorMessage("")
                }}
              />
            </label>
            <label className="block">
              <LoginInput
                type="password"
                placeholder="비밀번호"
                value={password}
                onChange={(event) => {
                  setPassword(event.target.value)
                  if (errorMessage) setErrorMessage("")
                }}
              />
            </label>
          </div>

          {errorMessage ? (
            <p className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
              {errorMessage}
            </p>
          ) : null}

          <div className="space-y-4 pt-6">
            <LoginButton type="submit" variant="primary" disabled={isSubmitting}>
              {isSubmitting ? "로그인 중..." : "로그인"}
            </LoginButton>
            <Link
              href="/signup"
              className="flex h-14 items-center justify-center rounded-2xl border border-black/10 px-4 text-sm font-semibold text-black/72 transition-colors hover:bg-black/[0.03]"
            >
              회원가입
            </Link>
          </div>
        </div>
      </div>
    </form>
  )
}
