import { ShowDetailContent } from "@/components/shows/ShowDetailContent"

interface ShowDetailPageProps {
    params: Promise<{ showId: string }>
}

export default async function ShowDetailPage({ params }: ShowDetailPageProps) {
    const { showId } = await params
    return <ShowDetailContent showId={showId} />
}
