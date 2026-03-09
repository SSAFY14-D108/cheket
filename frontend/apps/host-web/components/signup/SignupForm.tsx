"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { LoginInput } from "@/components/common/LoginInput"
import { LoginButton } from "@/components/common/LoginButton"
import { useToast } from "@/hooks/use-toast"
import { signupHost } from "@/lib/auth-api"
import { ApiError } from "@/lib/api"

interface SignupState {
  companyName: string
  businessNo: string
  email: string
  password: string
  passwordConfirm: string
}

const initialState: SignupState = {
  companyName: "",
  businessNo: "",
  email: "",
  password: "",
  passwordConfirm: "",
}

export function SignupForm() {
  const router = useRouter()
  const { toast } = useToast()
  const [form, setForm] = useState<SignupState>(initialState)
  const [errors, setErrors] = useState<Partial<Record<keyof SignupState, string>>>({})
  const [isSubmitting, setIsSubmitting] = useState(false)

  const updateField = (key: keyof SignupState, value: string) => {
    setForm((prev) => ({ ...prev, [key]: value }))
    if (errors[key]) {
      setErrors((prev) => ({ ...prev, [key]: undefined }))
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

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof SignupState, string>> = {}

    if (!form.companyName.trim()) newErrors.companyName = "회사 이름을 입력해주세요."
    if (!form.businessNo.trim()) newErrors.businessNo = "사업자등록번호를 입력해주세요."
    if (!form.email.trim()) newErrors.email = "이메일을 입력해주세요."
    if (!form.password.trim()) newErrors.password = "비밀번호를 입력해주세요."
    if (!form.passwordConfirm.trim()) {
      newErrors.passwordConfirm = "비밀번호 확인을 입력해주세요."
    } else if (form.password !== form.passwordConfirm) {
      newErrors.passwordConfirm = "비밀번호가 일치하지 않습니다."
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async () => {
    if (!validate()) return

    setIsSubmitting(true)

    try {
      const response = await signupHost({
        companyName: form.companyName.trim(),
        businessNo: form.businessNo.trim(),
        email: form.email.trim(),
        password: form.password,
      })

      toast({
        title: "회원가입 완료",
        description: response.responseMessage,
      })
      router.push("/")
    } catch (error) {
      const message =
        error instanceof ApiError
          ? error.message
          : "회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."

      toast({
        title: "회원가입 실패",
        description: message,
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const isFormValid =
    form.companyName.trim().length > 0 &&
    form.businessNo.trim().length > 0 &&
    form.email.trim().length > 0 &&
    form.password.length > 0 &&
    form.passwordConfirm.length > 0 &&
    form.password === form.passwordConfirm

  return (
    <div className="flex w-full max-w-md flex-col gap-5 px-6">
      <h1 className="text-center text-2xl font-bold tracking-tight text-foreground">
        회원가입
      </h1>

      <div className="flex flex-col gap-3">
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

        <div className="flex flex-col gap-1">
          <LoginInput
            type="text"
            placeholder="사업자등록번호"
            value={form.businessNo}
            onChange={(e) => updateField("businessNo", e.target.value)}
          />
          {errors.businessNo && (
            <p className="text-sm text-destructive">{errors.businessNo}</p>
          )}
        </div>

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
          disabled={!isFormValid || isSubmitting}
        >
          {isSubmitting ? "가입 처리 중..." : "회원가입"}
        </LoginButton>
        <LoginButton
          type="button"
          variant="secondary"
          onClick={() => router.push("/")}
          disabled={isSubmitting}
        >
          로그인으로 돌아가기
        </LoginButton>
      </div>
    </div>
  )
}
