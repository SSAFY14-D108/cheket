import {create} from 'zustand';

type AuthState = {
  accessToken: string | null;
  refreshToken: string | null;
  isLoggedIn: boolean;
  setAuthSession: (accessToken: string, refreshToken: string) => void;
  clearAuthSession: () => void;
};

export const useAuthStore = create<AuthState>(set => ({
  accessToken: null,
  refreshToken: null,
  isLoggedIn: false,
  setAuthSession: (accessToken, refreshToken) =>
    set({
      accessToken,
      refreshToken,
      isLoggedIn: true,
    }),
  clearAuthSession: () =>
    set({
      accessToken: null,
      refreshToken: null,
      isLoggedIn: false,
    }),
}));
