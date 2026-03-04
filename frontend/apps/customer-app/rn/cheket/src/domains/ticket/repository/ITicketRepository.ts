import {Seat} from '../entities/Seat';
import {Ticket} from '../entities/Ticket';

export interface ITicketRepository {
  fetchShowSeats(showId: string): Promise<Seat[]>;
  reserveSeat(showId: string, seatId: string): Promise<Ticket>;
}
