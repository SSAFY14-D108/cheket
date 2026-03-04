import {ITicketRepository} from '../repository/ITicketRepository';

export async function reserveSeatUseCase(repository: ITicketRepository, showId: string, seatId: string) {
  return repository.reserveSeat(showId, seatId);
}
