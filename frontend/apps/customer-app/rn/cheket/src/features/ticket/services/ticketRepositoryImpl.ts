import {ITicketRepository} from '../../../domains/ticket/repository/ITicketRepository';
import {fetchShowSeatsApi, reserveSeatApi} from './ticketApi';

export const ticketRepositoryImpl: ITicketRepository = {
  fetchShowSeats: (showId: string) => fetchShowSeatsApi(showId),
  reserveSeat: (showId: string, seatId: string) => reserveSeatApi(showId, seatId),
};
