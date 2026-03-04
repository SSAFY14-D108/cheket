import { mockEvents, mockDailyBookings, mockCompany } from "@/lib/mock-data"
import { notFound } from "next/navigation"
import { DashboardContent } from "@/components/dashboard/DashboardContent"

interface DashboardPageProps {
    params: Promise<{ id: string }>
}

export default async function DashboardPage({ params }: DashboardPageProps) {
    const { id } = await params
    const event = mockEvents.find((e) => e.id === id)

    if (!event) {
        notFound()
    }

    return (
        <DashboardContent
            event={event}
            dailyBookings={mockDailyBookings}
            company={mockCompany}
        />
    )
}
