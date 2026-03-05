"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { LoginInput } from "@/components/common/LoginInput"
import { LoginButton } from "@/components/common/LoginButton"

interface SignupState {
  companyName: string
  businessNumber: string
  email: string
  username: string
  password: string
  passwordConfirm: string
}

const initialState: SignupState = {
  companyName: "",
  businessNumber: "",
  email: "",
  username: "",
  password: "",
  passwordConfirm: "",
}

export function SignupForm() {
  const router = useRouter()
  const [form, setForm] = useState<SignupState>(initialState)
  const [errors, setErrors] = useState<Partial<Record<keyof SignupState, string>>>({})
  const [isIdChecked, setIsIdChecked] = useState(false)

  const updateField = (key: keyof SignupState, value: string) => {
    setForm((prev) => ({ ...prev, [key]: value }))
    if (errors[key]) {
      setErrors((prev) => ({ ...prev, [key]: undefined }))
    }
    if (key === "username") {
      setIsIdChecked(false)
    }
  }

  const passwordMismatch =
    form.password.length > 0 &&
    form.passwordConfirm.length > 0 &&
    form.password !== form.passwordConfirm

  const passwordMatch =
    form.password.length > 0 &&
    form.passwordConfirm.length > 0 &&
    form.password === form.passwordConfirm

  const handleDuplicateCheck = () => {
    if (!form.username.trim()) {
      setErrors((prev) => ({ ...prev, username: "아이디를 먼저 입력해주세요." }))
      return
    }
    setIsIdChecked(true)
    alert("사용 가능한 아이디입니다.")
  }

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof SignupState, string>> = {}
    if (!form.companyName.trim()) newErrors.companyName = "회사 이름을 입력해주세요."
    if (!form.businessNumber.trim()) newErrors.businessNumber = "사업자등록번호를 입력해주세요."
    if (!form.email.trim()) newErrors.email = "이메일을 입력해주세요."
    if (!form.username.trim()) newErrors.username = "아이디를 입력해주세요."
    else if (!isIdChecked) newErrors.username = "아이디 중복확인을 해주세요."
    if (!form.password.trim()) newErrors.password = "비밀번호를 입력해주세요."
    if (!form.passwordConfirm.trim()) newErrors.passwordConfirm = "비밀번호 확인을 입력해주세요."
    else if (form.password !== form.passwordConfirm) newErrors.passwordConfirm = "비밀번호가 일치하지 않습니다."
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = () => {
    if (!validate()) return
    // TODO: 회원가입 API 연동
    alert("회원가입이 완료되었습니다.")
    router.push("/")
  }

  const isFormValid =
    form.companyName.trim().length > 0 &&
    form.businessNumber.trim().length > 0 &&
    form.email.trim().length > 0 &&
    form.username.trim().length > 0 &&
    isIdChecked &&
    form.password.length > 0 &&
    form.passwordConfirm.length > 0 &&
    form.password === form.passwordConfirm

  return (
    <div className="flex w-full max-w-md flex-col gap-5 px-6">
      <h1 className="text-center text-2xl font-bold tracking-tight text-foreground">
        회원가입
      </h1>

      <div className="flex flex-col gap-3">
        {/* 회사 이름 */}
        <div className="flex flex-col gap-1">
          <LoginInput
            type="text"
            placeholder="회사 이름"
            value={form.companyName}
            onChange={(e) => updateField("companyName", e.target.value)}
          />
          {errors.companyName && (
            <p className="text-sm text-destructive">{errors.companyName}</p>
          )}
        </div>

        {/* 사업자등록번호 */}
        <div className="flex flex-col gap-1">
          <LoginInput
            type="text"
            placeholder="사업자등록번호"
            value={form.businessNumber}
            onChange={(e) => updateField("businessNumber", e.target.value)}
          />
          {errors.businessNumber && (
            <p className="text-sm text-destructive">{errors.businessNumber}</p>
          )}
        </div>

        {/* 이메일 */}
        <div className="flex flex-col gap-1">
          <LoginInput
            type="email"
            placeholder="이메일"
            value={form.email}
            onChange={(e) => updateField("email", e.target.value)}
          />
          {errors.email && (
            <p className="text-sm text-destructive">{errors.email}</p>
          )}
        </div>

        {/* 아이디 + 중복확인 */}
        <div className="flex flex-col gap-1">
          <div className="flex gap-2">
            <LoginInput
              type="text"
              placeholder="아이디"
              value={form.username}
              onChange={(e) => updateField("username", e.target.value)}
              className="flex-1"
            />
            <button
              type="button"
              onClick={handleDuplicateCheck}
              className="shrink-0 rounded-sm bg-primary px-4 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
            >
              중복확인
            </button>
          </div>
          {isIdChecked && !errors.username && (
            <p className="text-sm text-chart-5">사용 가능한 아이디입니다.</p>
          )}
          {errors.username && (
            <p className="text-sm text-destructive">{errors.username}</p>
          )}
        </div>

        {/* 비밀번호 */}
        <div className="flex flex-col gap-1">
          <LoginInput
            type="password"
            placeholder="비밀번호"
            value={form.password}
            onChange={(e) => updateField("password", e.target.value)}
          />
          {errors.password && (
            <p className="text-sm text-destructive">{errors.password}</p>
          )}
        </div>

        {/* 비밀번호 확인 */}
        <div className="flex flex-col gap-1">
          <LoginInput
            type="password"
            placeholder="비밀번호 확인"
            value={form.passwordConfirm}
            onChange={(e) => updateField("passwordConfirm", e.target.value)}
          />
          {passwordMismatch && (
            <p className="text-sm text-destructive">비밀번호가 일치하지 않습니다.</p>
          )}
          {passwordMatch && (
            <p className="text-sm text-chart-5">비밀번호가 일치합니다.</p>
          )}
          {errors.passwordConfirm && !passwordMismatch && (
            <p className="text-sm text-destructive">{errors.passwordConfirm}</p>
          )}
        </div>
      </div>

      <div className="flex flex-col gap-2">
        <LoginButton
          type="button"
          variant="primary"
          onClick={handleSubmit}
          disabled={!isFormValid}
        >
          회원가입
        </LoginButton>
        <LoginButton type="button" variant="secondary" onClick={() => router.push("/")}>
          로그인으로 돌아가기
        </LoginButton>
      </div>
    </div>
  )
}
