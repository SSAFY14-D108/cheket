"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { ArrowLeft, Building2, Mail, ShieldAlert } from "lucide-react"
import { useRouter } from "next/navigation"
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
          title: "회사 정보를 불러오지 못했습니다.",
          description:
            error instanceof ApiError
              ? error.message
              : "회사 정보를 가져오는 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
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
        title: "입력값을 확인해주세요.",
        description: "회사명 또는 이메일을 입력해주세요.",
        variant: "destructive",
      })
      return
    }

    if (email && !emailPattern.test(email)) {
      toast({
        title: "이메일 형식이 올바르지 않습니다.",
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
        title: "정보가 저장되었습니다.",
        description: response.responseMessage,
      })
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        return
      }

      toast({
        title: "회사 정보 저장에 실패했습니다.",
        description:
          error instanceof ApiError
            ? error.message
            : "정보를 저장하는 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
        variant: "destructive",
      })
    } finally {
      setIsSaving(false)
    }
  }

  const handleDeleteAccount = async () => {
    if (!deletePassword.trim()) {
      toast({
        title: "비밀번호를 입력해주세요.",
        description: "회원 탈퇴를 진행하려면 현재 비밀번호가 필요합니다.",
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
        title: "회원 탈퇴가 완료되었습니다.",
        description: response.responseMessage,
      })

      router.push("/")
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        return
      }

      toast({
        title: "회원 탈퇴에 실패했습니다.",
        description:
          error instanceof ApiError
            ? error.message
            : "회원 탈퇴를 처리하는 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
        variant: "destructive",
      })
    } finally {
      setIsDeleting(false)
      setDeletePassword("")
    }
  }

  return (
    <main className="min-h-svh bg-white">
      <div className="mx-auto max-w-7xl px-6 py-8 sm:px-8 lg:px-10">
        <section className="overflow-hidden rounded-[2rem] border border-black/8 bg-white px-6 py-7 shadow-sm sm:px-8">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
            <div className="flex items-start gap-4">
              <Link
                href="/mypage"
                className="mt-1 flex size-11 shrink-0 items-center justify-center rounded-full border border-black/10 bg-white text-black transition-colors hover:bg-black/[0.03]"
                aria-label="운영 홈으로 돌아가기"
              >
                <ArrowLeft className="size-4" />
              </Link>

              <div className="space-y-3">
                <div className="space-y-1">
                  <p className="text-xs font-medium uppercase tracking-[0.26em] text-black/42">
                    Account Settings
                  </p>
                  <h1 className="text-3xl font-semibold tracking-[-0.04em] text-black sm:text-4xl">
                    계정 설정
                  </h1>
                </div>
                <p className="max-w-xl text-sm leading-6 text-black/55">
                  회사 정보와 계정 설정을 관리하는 공간입니다. 자주 수정하는 정보와
                  민감한 작업을 분리해 두었습니다.
                </p>
              </div>
            </div>

            <Link
              href="/mypage"
              className="inline-flex items-center justify-center rounded-2xl border border-black/10 bg-white px-4 py-3 text-sm font-semibold text-black/72 transition-colors hover:bg-black/[0.03]"
            >
              운영 홈으로 돌아가기
            </Link>
          </div>
        </section>

        <div className="mt-8 grid gap-6 lg:grid-cols-[minmax(0,1.5fr)_minmax(300px,0.8fr)]">
          <section className="rounded-[1.75rem] border border-black/8 bg-white shadow-sm">
            <div className="border-b border-black/6 px-6 py-5 sm:px-7">
              <div className="flex items-start gap-3">
                <div className="flex size-11 items-center justify-center rounded-2xl bg-black/[0.04]">
                  <Building2 className="size-5 text-black/70" />
                </div>
                <div className="space-y-1">
                  <h2 className="text-xl font-semibold tracking-[-0.03em] text-black">
                    회사 정보 수정
                  </h2>
                  <p className="text-sm text-black/50">
                    회사명과 이메일을 업데이트할 수 있습니다.
                  </p>
                </div>
              </div>
            </div>

            <div className="space-y-6 px-6 py-6 sm:px-7 sm:py-7">
              {hasLoadError ? (
                <div className="rounded-[1rem] border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                  회사 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
                </div>
              ) : null}

              <div className="grid gap-5">
                <div className="space-y-2">
                  <Label htmlFor="company-name" className="text-sm font-medium text-black/70">
                    회사명
                  </Label>
                  <Input
                    id="company-name"
                    value={form.companyName}
                    onChange={(event) =>
                      setForm((previous) => ({ ...previous, companyName: event.target.value }))
                    }
                    disabled={isLoading || isSaving}
                    placeholder="회사명을 입력해주세요."
                    className="h-14 rounded-2xl border-black/10 bg-white px-4"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="company-email" className="text-sm font-medium text-black/70">
                    이메일
                  </Label>
                  <Input
                    id="company-email"
                    type="email"
                    value={form.email}
                    onChange={(event) =>
                      setForm((previous) => ({ ...previous, email: event.target.value }))
                    }
                    disabled={isLoading || isSaving}
                    placeholder="이메일을 입력해주세요."
                    className="h-14 rounded-2xl border-black/10 bg-white px-4"
                  />
                </div>
              </div>

              <div className="rounded-[1.25rem] border border-black/8 bg-[#fafafa] px-4 py-4">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                  <div className="space-y-1">
                    <p className="text-xs font-medium uppercase tracking-[0.24em] text-black/38">
                      Business Number
                    </p>
                    <p className="text-sm text-black/68">
                      사업자등록번호{" "}
                      <span className="font-semibold text-black">
                        {company?.businessNo ?? "-"}
                      </span>
                    </p>
                  </div>

                  <Button
                    onClick={handleSave}
                    disabled={isLoading || isSaving || !isFormDirty}
                    className="h-12 rounded-2xl bg-[#171717] px-5 text-sm font-semibold text-white hover:bg-black/85"
                  >
                    {isSaving ? "저장 중..." : "저장"}
                  </Button>
                </div>
              </div>
            </div>
          </section>

          <aside className="space-y-6">
            <section className="rounded-[1.75rem] border border-black/8 bg-white px-6 py-6 shadow-sm sm:px-7">
              <div className="flex items-start gap-3">
                <div className="flex size-11 items-center justify-center rounded-2xl bg-black/[0.04]">
                  <Mail className="size-5 text-black/70" />
                </div>
                <div className="space-y-1">
                  <h2 className="text-lg font-semibold tracking-[-0.03em] text-black">
                    현재 계정 정보
                  </h2>
                  <p className="text-sm text-black/50">
                    현재 등록된 기본 정보를 빠르게 확인할 수 있습니다.
                  </p>
                </div>
              </div>

              <div className="mt-6 space-y-4 text-sm">
                <div className="rounded-[1rem] bg-[#fafafa] px-4 py-4">
                  <p className="text-xs uppercase tracking-[0.24em] text-black/38">Company</p>
                  <p className="mt-2 font-semibold text-black">
                    {company?.companyName || "-"}
                  </p>
                </div>
                <div className="rounded-[1rem] bg-[#fafafa] px-4 py-4">
                  <p className="text-xs uppercase tracking-[0.24em] text-black/38">Email</p>
                  <p className="mt-2 font-semibold text-black">{company?.email || "-"}</p>
                </div>
                <div className="rounded-[1rem] bg-[#fafafa] px-4 py-4">
                  <p className="text-xs uppercase tracking-[0.24em] text-black/38">
                    Business Number
                  </p>
                  <p className="mt-2 font-semibold text-black">{company?.businessNo || "-"}</p>
                </div>
              </div>
            </section>

            <section className="rounded-[1.75rem] border border-red-200 bg-white px-6 py-6 shadow-sm sm:px-7">
              <div className="flex items-start gap-3">
                <div className="flex size-11 items-center justify-center rounded-2xl bg-red-50">
                  <ShieldAlert className="size-5 text-red-500" />
                </div>
                <div className="space-y-1">
                  <h2 className="text-lg font-semibold tracking-[-0.03em] text-red-600">
                    회원 탈퇴
                  </h2>
                  <p className="text-sm leading-6 text-black/52">
                    계정을 비활성화하면 동일한 계정으로 다시 접근할 수 없습니다. 진행
                    중인 공연이 있다면 먼저 상태를 확인해주세요.
                  </p>
                </div>
              </div>

              <div className="mt-6 rounded-[1rem] bg-red-50/60 px-4 py-4 text-sm text-black/58">
                탈퇴 전에는 공연 진행 여부와 정산 상태를 꼭 확인해주세요.
              </div>

              <Button
                variant="destructive"
                onClick={() => setDeleteDialogOpen(true)}
                disabled={isDeleting}
                className="mt-6 h-12 w-full rounded-2xl bg-red-600 text-sm font-semibold hover:bg-red-700"
              >
                회원 탈퇴
              </Button>
            </section>
          </aside>
        </div>
      </div>

      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>회원 탈퇴 확인</DialogTitle>
            <DialogDescription>
              탈퇴를 진행하려면 현재 비밀번호를 입력해주세요.
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
              {isDeleting ? "탈퇴 처리 중..." : "탈퇴하기"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </main>
  )
}
