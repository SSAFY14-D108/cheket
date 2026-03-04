import { mockEvents } from "@/lib/mock-data"
import { notFound } from "next/navigation"
import { EventForm } from "@/components/shows/EventForm"

interface ShowEditPageProps {
    params: Promise<{ showId: string }>
}

export default async function ShowEditPage({ params }: ShowEditPageProps) {
    const { showId } = await params
    const event = mockEvents.find((e) => e.id === showId)

    if (!event) {
        notFound()
    }

    return <EventForm mode="edit" initialData={event} />
}
