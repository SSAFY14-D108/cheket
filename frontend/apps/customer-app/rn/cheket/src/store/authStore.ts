import {create} from 'zustand';
import {User} from '../core/types/common';
import {MOCK_USER} from '../core/data/mockData';

type AuthState = {
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null;
  isLoggedIn: boolean;
  setAuthSession: (accessToken: string, refreshToken: string) => void;
  clearAuthSession: () => void;
  login: (id: string, password: string) => boolean;
  logout: () => void;
  topUpCTK: (amount: number) => void;
};

export const useAuthStore = create<AuthState>((set, get) => ({
  accessToken: null,
  refreshToken: null,
  user: null,
  isLoggedIn: false,
  setAuthSession: (accessToken, refreshToken) =>
    set({
      accessToken,
      refreshToken,
    }),
  clearAuthSession: () =>
    set({
      accessToken: null,
      refreshToken: null,
      user: null,
      isLoggedIn: false,
    }),
  login: (id: string, _password: string) => {
    if (!id.trim()) {
      return false;
    }
    set({
      user: {...MOCK_USER},
      accessToken: 'mock_access_token',
      refreshToken: 'mock_refresh_token',
      isLoggedIn: true,
    });
    return true;
  },
  logout: () => {
    set({
      user: null,
      accessToken: null,
      refreshToken: null,
      isLoggedIn: false,
    });
  },
  topUpCTK: (amount: number) => {
    const {user} = get();
    if (!user) {
      return;
    }
    set({
      user: {
        ...user,
        ctkBalance: user.ctkBalance + amount,
      },
    });
  },
}));
