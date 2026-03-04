import { mockEvents, mockDailyBookings, mockCompany } from "@/lib/mock-data"
import { notFound } from "next/navigation"
import { DashboardContent } from "@/components/dashboard/DashboardContent"

interface DashboardPageProps {
    params: Promise<{ showId: string }>
}

export default async function DashboardPage({ params }: DashboardPageProps) {
    const { showId } = await params
    const event = mockEvents.find((e) => e.showId.toString() === showId)

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
