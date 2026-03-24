export interface CollectionTicket {
  id: string
  eventId: string
  eventName: string
  eventDate: string
  venue: string
  poster: string
  seatId: string
  seatLabel: string
  grade: string
  numbering: string
  status: 'USED'
}

// Backend API response shape
export interface CollectionTicketDto {
  ticketId: number
  numbering: string
  posterUrl: string
  show: {
    showId: number
    name: string
    date: string
    venue: string
    effect?: string | null
  }
  seatId: number | null
  sectionName: string
  seatNo: string
  grade: string
}

export interface ApiResponse<T> {
  httpStatusCode: number
  responseMessage: string
  data: T
}

export function mapDtoToTicket(dto: CollectionTicketDto): CollectionTicket {
  return {
    id: String(dto.ticketId),
    eventId: String(dto.show.showId),
    eventName: dto.show.name,
    eventDate: dto.show.date,
    venue: dto.show.venue,
    poster: dto.posterUrl,
    seatId: dto.seatId ? String(dto.seatId) : '',
    seatLabel: `${dto.sectionName} ${dto.seatNo}`,
    grade: dto.grade,
    numbering: dto.numbering,
    status: 'USED',
  }
}
