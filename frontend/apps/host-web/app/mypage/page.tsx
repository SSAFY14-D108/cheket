import { CompanyInfoCard } from "@/components/mypage/CompanyInfoCard"
import { EventCard } from "@/components/mypage/EventCard"
import { mockCompany, mockEvents } from "@/lib/mock-data"
import Link from "next/link"

export default function MyPage() {
  return (
    <main className="mx-auto max-w-6xl px-6 py-10">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-foreground">마이페이지</h1>
        <div className="flex gap-3">
          <Link
            href="/shows/create"
            className="rounded-sm bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
          >
            공연 등록
          </Link>
          <Link
            href="/"
            className="rounded-sm bg-secondary px-4 py-2 text-sm font-medium text-secondary-foreground transition-colors hover:bg-secondary/80"
          >
            로그아웃
          </Link>
        </div>
      </div>

      {/* 회사 정보 */}
      <section className="mt-8">
        <CompanyInfoCard company={mockCompany} />
      </section>

      {/* 공연 목록 */}
      <section className="mt-10">
        <h2 className="text-lg font-semibold text-foreground">등록한 공연</h2>
        <div className="mt-4 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {mockEvents.map((event) => (
            <EventCard key={event.showId} event={event} />
          ))}
        </div>
      </section>
    </main>
  )
}
