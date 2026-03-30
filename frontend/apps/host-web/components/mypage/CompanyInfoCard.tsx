import { useState } from "react"
import { Building2, Check, Copy } from "lucide-react"
import { Button } from "@/components/ui/button"
import type { MyCompanyInfo } from "@/lib/mypage-api"

interface CompanyInfoCardProps {
  company: MyCompanyInfo
}

export function CompanyInfoCard({ company }: CompanyInfoCardProps) {
  const [copied, setCopied] = useState(false)
  const walletAddress = company.walletAddress ?? "지갑 정보 없음"
  const shortWalletAddress =
    company.walletAddress && company.walletAddress.length > 12
      ? `${company.walletAddress.slice(0, 6)}...${company.walletAddress.slice(-4)}`
      : walletAddress

  const handleCopyWalletAddress = async () => {
    if (!company.walletAddress) return

    try {
      await navigator.clipboard.writeText(company.walletAddress)
      setCopied(true)
      setTimeout(() => setCopied(false), 1500)
    } catch {
      // ignore
    }
  }

  const fields = [
    { label: "회사명", value: company.companyName },
    { label: "사업자등록번호", value: company.businessNo },
    { label: "이메일", value: company.email },
    { label: "지갑 주소", value: shortWalletAddress },
    {
      label: "잔액",
      value:
        company.balance !== null && company.balance !== undefined
          ? `${Number(company.balance).toLocaleString()} SSF`
          : "잔액 정보 없음",
    },
  ]

  return (
    <section className="rounded-[2rem] border border-black/8 bg-white p-6 shadow-sm sm:p-8">
      <div className="flex items-center gap-3 border-b border-black/8 pb-5">
        <div className="flex size-11 items-center justify-center rounded-2xl bg-black/[0.04]">
          <Building2 className="h-5 w-5 text-black" />
        </div>
        <div>
          <h2 className="text-xl font-semibold tracking-[-0.03em] text-black">
            회사 정보
          </h2>
          <p className="text-sm text-black/45">운영자 계정 기본 정보</p>
        </div>
      </div>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        {fields.map((field) => (
          <div
            key={field.label}
            className="rounded-[1.5rem] border border-black/8 bg-white p-5"
          >
            <p className="text-sm text-black/42">{field.label}</p>
            {field.label === "지갑 주소" ? (
              <div className="mt-2 flex items-center gap-2">
                <span className="break-all text-sm font-semibold text-black/80">
                  {field.value}
                </span>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  className="h-8 rounded-full border-black/10 px-3 text-xs"
                  onClick={handleCopyWalletAddress}
                  disabled={!company.walletAddress}
                >
                  {copied ? (
                    <Check className="mr-1 h-3.5 w-3.5" />
                  ) : (
                    <Copy className="mr-1 h-3.5 w-3.5" />
                  )}
                  {copied ? "복사됨" : "복사"}
                </Button>
              </div>
            ) : (
              <p className="mt-2 break-all text-base font-semibold text-black">
                {field.value}
              </p>
            )}
          </div>
        ))}
      </div>
    </section>
  )
}
