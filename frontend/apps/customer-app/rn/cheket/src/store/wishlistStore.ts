import {create} from 'zustand';

type WishlistState = {
  wishlist: string[];
  toggleWishlist: (eventId: string) => void;
  isWishlisted: (eventId: string) => boolean;
};

export const useWishlistStore = create<WishlistState>((set, get) => ({
  wishlist: ['evt_001'],
  toggleWishlist: (eventId: string) =>
    set(state => {
      const exists = state.wishlist.includes(eventId);
      return {
        wishlist: exists
          ? state.wishlist.filter(id => id !== eventId)
          : [...state.wishlist, eventId],
      };
    }),
  isWishlisted: (eventId: string) => {
    return get().wishlist.includes(eventId);
  },
}));
