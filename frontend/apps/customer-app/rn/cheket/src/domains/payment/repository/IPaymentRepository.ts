import {Order} from '../entities/Order';

export interface IPaymentRepository {
  createOrder(ticketIds: string[]): Promise<Order>;
}
