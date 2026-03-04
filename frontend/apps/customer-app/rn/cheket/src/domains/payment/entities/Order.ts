export type Order = {
  id: string;
  ticketIds: string[];
  amount: number;
  status: 'created' | 'paid' | 'failed';
};
