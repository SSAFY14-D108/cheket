import {httpClient} from '../../../core/api/httpClient';
import {Order} from '../../../domains/payment/entities/Order';

export function createOrderApi(ticketIds: string[]): Promise<Order> {
  return httpClient.post<Order>('/orders', {ticketIds});
}
