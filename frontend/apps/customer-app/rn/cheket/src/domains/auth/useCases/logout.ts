import {IAuthRepository} from '../repository/IAuthRepository';

export async function logoutUseCase(repository: IAuthRepository): Promise<void> {
  return repository.logout();
}
