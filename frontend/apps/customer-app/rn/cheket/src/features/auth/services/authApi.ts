import {httpClient} from '../../../core/api/httpClient';
import {User} from '../../../domains/auth/entities/User';

type LoginResponse = {
  user: User;
  accessToken: string;
  refreshToken: string;
};

export function loginApi(email: string, password: string): Promise<LoginResponse> {
  return httpClient.post<LoginResponse>('/auth/login', {email, password});
}

export function logoutApi(): Promise<void> {
  return httpClient.post<void>('/auth/logout');
}
