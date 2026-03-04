"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { LoginInput } from "@/components/common/LoginInput"
import { LoginButton } from "@/components/common/LoginButton"

export function LoginForm() {
  const router = useRouter()
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")

  const handleLogin = () => {
    router.push("/mypage")
  }

  const handleSignup = () => {
    router.push("/signup")
  }

  return (
    <div className="flex w-full max-w-md flex-col gap-5 px-6 border border-dashed border-gray-400 p-8 rounded-lg">
      <h1 className="text-center text-2xl font-bold tracking-tight text-foreground text-primary">
        CHEKET HOST
      </h1>

      <div className="flex flex-col space-y-2 text-center mb-2 mt-2">
        <h2 className="text-xl font-bold tracking-tight text-foreground">
          호스트 로그인
        </h2>
        <p className="text-sm text-muted-foreground">
          관리자 시스템에 접근하려면 계정 정보를 입력해주세요
        </p>
      </div>

      <div className="flex flex-col gap-4">
        <LoginInput
          type="text"
          placeholder="아이디"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <LoginInput
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </div>

      <div className="flex flex-col gap-2">
        <LoginButton type="button" variant="primary" onClick={handleLogin}>
          로그인
        </LoginButton>
        <LoginButton type="button" variant="secondary" onClick={handleSignup}>
          회원가입
        </LoginButton>
      </div>
    </div>
  )
}
