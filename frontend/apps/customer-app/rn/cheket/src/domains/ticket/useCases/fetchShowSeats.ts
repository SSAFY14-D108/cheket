import {ITicketRepository} from '../repository/ITicketRepository';

export async function fetchShowSeatsUseCase(repository: ITicketRepository, showId: string) {
  return repository.fetchShowSeats(showId);
}
