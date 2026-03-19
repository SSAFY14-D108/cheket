"use client"

import { type FormEvent, useState } from "react"
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
        title: "로그인 성공",
        description: response.responseMessage,
      })
      router.push("/mypage")
    } catch (error) {
      const message =
        error instanceof ApiError
          ? error.message
          : "로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."

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

  const handleSignup = () => {
    router.push("/signup")
  }

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (isSubmitting) {
      return
    }

    void handleLogin()
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="flex w-full max-w-md flex-col gap-5 rounded-lg border border-dashed border-gray-400 p-8 px-6"
    >
      <h1 className="text-center text-2xl font-bold tracking-tight text-primary">
        CHEKET HOST
      </h1>

      <div className="mb-2 mt-2 flex flex-col space-y-2 text-center">
        <h2 className="text-xl font-bold tracking-tight text-foreground">
          호스트 로그인
        </h2>
        <p className="text-sm text-muted-foreground">
          관리자 시스템에 접근하려면 계정 정보를 입력해주세요
        </p>
      </div>

      <div className="flex flex-col gap-4">
        <LoginInput
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => {
            setEmail(e.target.value)
            if (errorMessage) setErrorMessage("")
          }}
        />
        <LoginInput
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => {
            setPassword(e.target.value)
            if (errorMessage) setErrorMessage("")
          }}
        />
      </div>

      {errorMessage ? (
        <p className="text-sm text-destructive">{errorMessage}</p>
      ) : null}

      <div className="flex flex-col gap-2">
        <LoginButton
          type="submit"
          variant="primary"
          disabled={isSubmitting}
        >
          {isSubmitting ? "로그인 중..." : "로그인"}
        </LoginButton>
        <LoginButton
          type="button"
          variant="secondary"
          onClick={handleSignup}
          disabled={isSubmitting}
        >
          회원가입
        </LoginButton>
      </div>
    </form>
  )
}
