import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import type { MyCompanyInfo } from "@/lib/mypage-api"

interface CompanyInfoCardProps {
  company: MyCompanyInfo
}

export function CompanyInfoCard({ company }: CompanyInfoCardProps) {
  const fields = [
    { label: "회사명", value: company.companyName },
    { label: "사업자등록번호", value: company.businessNo },
    { label: "이메일", value: company.email },
    { label: "지갑 주소", value: company.walletAddress ?? "지갑 정보 없음" },
    {
      label: "잔액",
      value: company.balance !== null && company.balance !== undefined ? `${company.balance} CTK` : "잔액 정보 없음",
    },
  ]

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">회사 정보</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="flex flex-col gap-3">
          {fields.map((field, index) => (
            <div key={field.label}>
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">{field.label}</span>
                <span className="text-sm font-medium text-foreground">{field.value}</span>
              </div>
              {index < fields.length - 1 && <Separator className="mt-3" />}
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
