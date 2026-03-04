import { mockEvents } from "@/lib/mock-data"
import { notFound } from "next/navigation"
import { ShowForm } from "@/components/shows/ShowForm"

interface ShowEditPageProps {
    params: Promise<{ showId: string }>
}

export default async function ShowEditPage({ params }: ShowEditPageProps) {
    const { showId } = await params
    const event = mockEvents.find((e) => e.showId.toString() === showId)

    if (!event) {
        notFound()
    }

    return <ShowForm mode="edit" initialData={event} />
}
