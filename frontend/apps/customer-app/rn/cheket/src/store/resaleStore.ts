import {create} from 'zustand';
import {ResaleItem} from '../core/types/common';
import {MOCK_RESALE_ITEMS} from '../core/data/mockData';
import {useAuthStore} from './authStore';
import {useTicketStore} from './ticketStore';

type ResaleState = {
  resaleItems: ResaleItem[];
  addResaleItem: (item: ResaleItem) => void;
  removeResaleItem: (id: string) => void;
  buyResaleTicket: (resaleItemId: string) => string | false;
};

export const useResaleStore = create<ResaleState>((set, get) => ({
  resaleItems: [...MOCK_RESALE_ITEMS],
  addResaleItem: (item: ResaleItem) =>
    set(state => ({
      resaleItems: [...state.resaleItems, item],
    })),
  removeResaleItem: (id: string) =>
    set(state => ({
      resaleItems: state.resaleItems.filter(item => item.id !== id),
    })),
  buyResaleTicket: (resaleItemId: string) => {
    const resaleItem = get().resaleItems.find(item => item.id === resaleItemId);
    if (!resaleItem) {
      return false;
    }

    const authState = useAuthStore.getState();
    const user = authState.user;
    if (!user) {
      return false;
    }

    if (user.ctkBalance < resaleItem.resalePrice) {
      return false;
    }

    const newTicketId = `tkt_${Date.now()}`;
    const newTicket = {
      id: newTicketId,
      eventId: resaleItem.ticketId.split('_')[0] || resaleItem.ticketId,
      eventName: resaleItem.eventName,
      eventDate: resaleItem.eventDate,
      venue: resaleItem.venue,
      poster: resaleItem.poster,
      seatId: resaleItem.ticketId,
      seatLabel: resaleItem.seatLabel,
      grade: resaleItem.grade,
      originalPrice: resaleItem.originalPrice,
      status: 'SOLD' as const,
    };

    useTicketStore.getState().addTicket(newTicket);

    set(state => ({
      resaleItems: state.resaleItems.filter(item => item.id !== resaleItemId),
    }));

    useAuthStore.setState({
      user: {
        ...user,
        ctkBalance: user.ctkBalance - resaleItem.resalePrice,
      },
    });

    return newTicketId;
  },
}));
