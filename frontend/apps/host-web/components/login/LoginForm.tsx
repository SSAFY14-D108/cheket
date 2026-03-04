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
      <h1 className="text-center text-2xl font-bold tracking-tight text-foreground">
        NFT Ticket Admin
      </h1>

      <div className="flex flex-col gap-3">
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
