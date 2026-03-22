"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { useToast } from "@/hooks/use-toast"
import { ApiError } from "@/lib/api"
import { clearAuthTokens } from "@/lib/auth-storage"
import {
  deleteMyAccount,
  fetchMyCompanyInfo,
  updateMyCompanyInfo,
  type MyCompanyInfo,
} from "@/lib/mypage-api"

interface SettingsFormState {
  companyName: string
  email: string
}

const initialFormState: SettingsFormState = {
  companyName: "",
  email: "",
}

export function MyPageSettingsContent() {
  const router = useRouter()
  const { toast } = useToast()
  const [company, setCompany] = useState<MyCompanyInfo | null>(null)
  const [form, setForm] = useState<SettingsFormState>(initialFormState)
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [deletePassword, setDeletePassword] = useState("")
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [hasLoadError, setHasLoadError] = useState(false)

  useEffect(() => {
    let isCancelled = false

    async function loadCompanyInfo() {
      try {
        const companyInfo = await fetchMyCompanyInfo()

        if (isCancelled) {
          return
        }

        setCompany(companyInfo)
        setForm({
          companyName: companyInfo.companyName,
          email: companyInfo.email,
        })
      } catch (error) {
        if (isCancelled || (error instanceof ApiError && error.status === 401)) {
          return
        }

        setHasLoadError(true)

        toast({
          title: "계정 정보 조회 실패",
          description:
            error instanceof ApiError
              ? error.message
              : "계정 설정 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.",
          variant: "destructive",
        })
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadCompanyInfo()

    return () => {
      isCancelled = true
    }
  }, [toast])

  const isFormDirty =
    company !== null &&
    (form.companyName.trim() !== company.companyName || form.email.trim() !== company.email)

  const handleSave = async () => {
    const companyName = form.companyName.trim()
    const email = form.email.trim()
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

    if (!companyName && !email) {
      toast({
        title: "입력값 확인",
        description: "회사명 또는 이메일을 입력해주세요.",
        variant: "destructive",
      })
      return
    }

    if (email && !emailPattern.test(email)) {
      toast({
        title: "이메일 형식 확인",
        description: "올바른 이메일 주소를 입력해주세요.",
        variant: "destructive",
      })
      return
    }

    setIsSaving(true)

    try {
      const response = await updateMyCompanyInfo({
        companyName,
        email,
      })

      setCompany((previous) =>
        previous
          ? {
              ...previous,
              companyName,
              email,
            }
          : previous
      )
      setForm({
        companyName,
        email,
      })

      toast({
        title: "저장 완료",
        description: response.responseMessage,
      })
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        return
      }

      toast({
        title: "회사 정보 수정 실패",
        description:
          error instanceof ApiError
            ? error.message
            : "회사 정보를 수정하지 못했습니다. 잠시 후 다시 시도해주세요.",
        variant: "destructive",
      })
    } finally {
      setIsSaving(false)
    }
  }

  const handleDeleteAccount = async () => {
    if (!deletePassword.trim()) {
      toast({
        title: "비밀번호 확인 필요",
        description: "회원 탈퇴를 위해 현재 비밀번호를 입력해주세요.",
        variant: "destructive",
      })
      return
    }

    setIsDeleting(true)

    try {
      const response = await deleteMyAccount(deletePassword)

      clearAuthTokens()
      setDeleteDialogOpen(false)

      toast({
        title: "회원 탈퇴 완료",
        description: response.responseMessage,
      })

      router.push("/")
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        return
      }

      toast({
        title: "회원 탈퇴 실패",
        description:
          error instanceof ApiError
            ? error.message
            : "회원 탈퇴를 처리하지 못했습니다. 잠시 후 다시 시도해주세요.",
        variant: "destructive",
      })
    } finally {
      setIsDeleting(false)
      setDeletePassword("")
    }
  }

  return (
    <main className="mx-auto max-w-6xl px-6 py-10">
      <div className="flex items-center justify-between">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl font-bold text-foreground">계정 설정</h1>
          <p className="text-sm text-muted-foreground">
            회사 정보 수정과 회원 탈퇴를 관리할 수 있습니다.
          </p>
        </div>
        <Link
          href="/mypage"
          className="rounded-sm border border-input bg-background px-4 py-2 text-sm font-medium transition-colors hover:bg-accent hover:text-accent-foreground"
        >
                운영 홈으로 돌아가기
        </Link>
      </div>

      <div className="mt-8 grid gap-6">
        <Card>
          <CardHeader>
            <CardTitle>회사 정보 수정</CardTitle>
            <CardDescription>
              현재 등록된 회사명과 이메일을 수정할 수 있습니다.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-5">
            {hasLoadError ? (
              <div className="rounded-md border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-destructive">
                계정 정보를 불러오지 못해 현재 값을 표시하지 못했습니다. 다시 시도해주세요.
              </div>
            ) : null}
            <div className="flex flex-col gap-2">
              <Label htmlFor="company-name">회사명</Label>
              <Input
                id="company-name"
                value={form.companyName}
                onChange={(event) =>
                  setForm((previous) => ({ ...previous, companyName: event.target.value }))
                }
                disabled={isLoading || isSaving}
                placeholder="회사명을 입력해주세요."
              />
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="company-email">이메일</Label>
              <Input
                id="company-email"
                type="email"
                value={form.email}
                onChange={(event) =>
                  setForm((previous) => ({ ...previous, email: event.target.value }))
                }
                disabled={isLoading || isSaving}
                placeholder="이메일을 입력해주세요."
              />
            </div>

            <div className="flex items-center justify-between rounded-md border bg-muted/30 px-4 py-3">
              <div className="text-sm text-muted-foreground">
                사업자등록번호: <span className="font-medium text-foreground">{company?.businessNo ?? "-"}</span>
              </div>
              <Button onClick={handleSave} disabled={isLoading || isSaving || !isFormDirty}>
                {isSaving ? "저장 중..." : "저장"}
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card className="border-destructive/30">
          <CardHeader>
            <CardTitle className="text-destructive">회원 탈퇴</CardTitle>
            <CardDescription>
              탈퇴 시 계정 상태만 비활성화되며, 진행 중인 공연이 있으면 탈퇴할 수 없습니다.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex items-center justify-between gap-4">
            <p className="text-sm text-muted-foreground">
              탈퇴 후에는 동일한 계정으로 다시 접근할 수 없습니다. 진행 전에 꼭 확인해주세요.
            </p>
            <Button
              variant="destructive"
              onClick={() => setDeleteDialogOpen(true)}
              disabled={isDeleting}
            >
              회원 탈퇴
            </Button>
          </CardContent>
        </Card>
      </div>

      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>회원 탈퇴 확인</DialogTitle>
            <DialogDescription>
              현재 비밀번호를 입력하면 회원 탈퇴가 진행됩니다.
            </DialogDescription>
          </DialogHeader>

          <div className="flex flex-col gap-2">
            <Label htmlFor="delete-password">현재 비밀번호</Label>
            <Input
              id="delete-password"
              type="password"
              value={deletePassword}
              onChange={(event) => setDeletePassword(event.target.value)}
              placeholder="현재 비밀번호를 입력해주세요."
              disabled={isDeleting}
            />
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setDeleteDialogOpen(false)
                setDeletePassword("")
              }}
              disabled={isDeleting}
            >
              취소
            </Button>
            <Button variant="destructive" onClick={handleDeleteAccount} disabled={isDeleting}>
              {isDeleting ? "탈퇴 처리 중..." : "탈퇴 확인"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </main>
  )
}
