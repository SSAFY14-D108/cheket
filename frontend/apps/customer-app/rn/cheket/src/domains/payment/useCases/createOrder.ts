import {IPaymentRepository} from '../repository/IPaymentRepository';

export async function createOrderUseCase(repository: IPaymentRepository, ticketIds: string[]) {
  return repository.createOrder(ticketIds);
}
