import {IAuthRepository} from '../repository/IAuthRepository';

export async function loginUseCase(repository: IAuthRepository, email: string, password: string) {
  return repository.login(email, password);
}
