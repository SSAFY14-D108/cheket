import {useMutation} from '@tanstack/react-query';
import {loginUseCase} from '../../../domains/auth/useCases/login';
import {saveTokens} from '../../../core/storage/secureStore';
import {useAuthStore} from '../../../store/authStore';
import {authRepositoryImpl} from '../services/authRepositoryImpl';

type LoginVariables = {
  email: string;
  password: string;
};

export function useLogin() {
  const setAuthSession = useAuthStore(state => state.setAuthSession);

  return useMutation({
    mutationFn: ({email, password}: LoginVariables) => loginUseCase(authRepositoryImpl, email, password),
    onSuccess: async result => {
      await saveTokens(result.accessToken, result.refreshToken);
      setAuthSession(result.accessToken, result.refreshToken);
    },
  });
}
