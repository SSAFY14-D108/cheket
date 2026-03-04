import {create} from 'zustand';
import {Ticket} from '../core/types/common';
import {MOCK_TICKETS} from '../core/data/mockData';

type TicketState = {
  tickets: Ticket[];
  addTicket: (ticket: Ticket) => void;
  updateTicketStatus: (id: string, updates: Partial<Ticket>) => void;
};

export const useTicketStore = create<TicketState>(set => ({
  tickets: [...MOCK_TICKETS],
  addTicket: (ticket: Ticket) =>
    set(state => ({
      tickets: [...state.tickets, ticket],
    })),
  updateTicketStatus: (id: string, updates: Partial<Ticket>) =>
    set(state => ({
      tickets: state.tickets.map(t => (t.id === id ? {...t, ...updates} : t)),
    })),
}));
