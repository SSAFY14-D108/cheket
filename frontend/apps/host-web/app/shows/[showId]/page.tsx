import { mockEvents } from "@/lib/mock-data"
import { notFound } from "next/navigation"
import { ShowDetailView } from "@/components/shows/ShowDetailView"

interface ShowDetailPageProps {
    params: Promise<{ showId: string }>
}

export default async function ShowDetailPage({ params }: ShowDetailPageProps) {
    const { showId } = await params
    const event = mockEvents.find((e) => e.id === showId)

    if (!event) {
        notFound()
    }

    return <ShowDetailView event={event} />
}
