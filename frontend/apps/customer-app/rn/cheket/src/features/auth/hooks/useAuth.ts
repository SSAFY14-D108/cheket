import {useAuthStore} from '../../../store/authStore';

export function useAuth() {
  return useAuthStore(state => ({
    accessToken: state.accessToken,
    refreshToken: state.refreshToken,
    isLoggedIn: state.isLoggedIn,
  }));
}
