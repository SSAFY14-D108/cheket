export type SeatStatus = 'available' | 'held' | 'reserved';

export type Seat = {
  id: string;
  label: string;
  status: SeatStatus;
};
