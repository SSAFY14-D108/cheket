import { ShowEditContent } from "@/components/shows/ShowEditContent"

interface ShowEditPageProps {
    params: Promise<{ showId: string }>
}

export default async function ShowEditPage({ params }: ShowEditPageProps) {
    const { showId } = await params
    return <ShowEditContent showId={showId} />
}
