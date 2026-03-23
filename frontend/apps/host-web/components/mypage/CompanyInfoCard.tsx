import { useState } from "react"
import { Building2, Check, Copy } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
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
    if (!company.walletAddress) {
      return
    }

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
          ? `${Number(company.balance).toLocaleString()} CTK`
          : "잔액 정보 없음",
    },
  ]

  return (
    <Card className="flex h-full flex-col">
      <CardHeader className="border-b bg-slate-50/50 pb-4">
        <CardTitle className="flex items-center gap-2 text-lg font-bold text-slate-800">
          <Building2 className="h-5 w-5 text-primary" />
          <span>회사 정보</span>
        </CardTitle>
      </CardHeader>
      <CardContent className="flex flex-1 flex-col justify-center pt-6">
        <div className="flex flex-col gap-4">
          {fields.map((field, index) => (
            <div key={field.label} className="group transition-colors">
              <div className="flex flex-col gap-1.5 sm:grid sm:grid-cols-3 sm:gap-6 sm:items-start">
                <span className="text-sm font-medium text-slate-400">{field.label}</span>
                {field.label === "지갑 주소" ? (
                  <div className="sm:col-span-2 flex items-center gap-2">
                    <span className="text-sm font-bold text-slate-700">{field.value}</span>
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      className="h-7 px-2 text-xs"
                      onClick={handleCopyWalletAddress}
                      disabled={!company.walletAddress}
                    >
                      {copied ? <Check className="mr-1 h-3.5 w-3.5" /> : <Copy className="mr-1 h-3.5 w-3.5" />}
                      {copied ? "복사됨" : "복사"}
                    </Button>
                  </div>
                ) : (
                  <span className="break-all text-sm font-bold text-slate-700 sm:col-span-2">{field.value}</span>
                )}
              </div>
              {index < fields.length - 1 && <Separator className="mt-4" />}
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
