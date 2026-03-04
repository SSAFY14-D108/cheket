import {IAuthRepository} from '../../../domains/auth/repository/IAuthRepository';
import {loginApi, logoutApi} from './authApi';

export const authRepositoryImpl: IAuthRepository = {
  login: (email, password) => loginApi(email, password),
  logout: () => logoutApi(),
};
