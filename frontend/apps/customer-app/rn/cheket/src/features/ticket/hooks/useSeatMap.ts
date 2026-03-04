import {useMemo, useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {fetchShowSeatsUseCase} from '../../../domains/ticket/useCases/fetchShowSeats';
import {ticketRepositoryImpl} from '../services/ticketRepositoryImpl';

export function useSeatMap(showId: string) {
  const [selectedSeatIds, setSelectedSeatIds] = useState<string[]>([]);
  const seatsQuery = useQuery({
    queryKey: ['seatMap', showId],
    queryFn: () => fetchShowSeatsUseCase(ticketRepositoryImpl, showId),
    enabled: !!showId,
  });

  const toggleSeat = (seatId: string) => {
    setSelectedSeatIds(prev =>
      prev.includes(seatId) ? prev.filter(id => id !== seatId) : [...prev, seatId],
    );
  };

  return {
    seats: seatsQuery.data ?? [],
    isLoading: seatsQuery.isLoading,
    isError: seatsQuery.isError,
    selectedSeatIds,
    toggleSeat,
    selectedSeats: useMemo(
      () => (seatsQuery.data ?? []).filter(seat => selectedSeatIds.includes(seat.id)),
      [seatsQuery.data, selectedSeatIds],
    ),
  };
}
