import { mockEvents } from "@/lib/mock-data"
import { notFound } from "next/navigation"
import { EventDetailView } from "@/components/shows/EventDetailView"

interface ShowDetailPageProps {
    params: Promise<{ id: string }>
}

export default async function ShowDetailPage({ params }: ShowDetailPageProps) {
    const { id } = await params
    const event = mockEvents.find((e) => e.id === id)

    if (!event) {
        notFound()
    }

    return <EventDetailView event={event} />
}
