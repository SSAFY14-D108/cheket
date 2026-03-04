import {useMutation, useQueryClient} from '@tanstack/react-query';
import {reserveSeatUseCase} from '../../../domains/ticket/useCases/reserveSeat';
import {ticketRepositoryImpl} from '../services/ticketRepositoryImpl';

type ReserveSeatVariables = {
  showId: string;
  seatId: string;
};

export function useReserveSeat() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({showId, seatId}: ReserveSeatVariables) =>
      reserveSeatUseCase(ticketRepositoryImpl, showId, seatId),
    onSuccess: (_ticket, variables) => {
      queryClient.invalidateQueries({queryKey: ['seatMap', variables.showId]});
    },
  });
}
