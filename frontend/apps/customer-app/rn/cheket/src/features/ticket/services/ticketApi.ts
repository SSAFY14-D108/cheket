import {httpClient} from '../../../core/api/httpClient';
import {Seat} from '../../../domains/ticket/entities/Seat';
import {Ticket} from '../../../domains/ticket/entities/Ticket';

export function fetchShowSeatsApi(showId: string): Promise<Seat[]> {
  return httpClient.get<Seat[]>(`/shows/${showId}/seats`);
}

export function reserveSeatApi(showId: string, seatId: string): Promise<Ticket> {
  return httpClient.post<Ticket>(`/shows/${showId}/seats/${seatId}/reserve`);
}
