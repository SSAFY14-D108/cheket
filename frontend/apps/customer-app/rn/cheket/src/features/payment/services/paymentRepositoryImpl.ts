import {IPaymentRepository} from '../../../domains/payment/repository/IPaymentRepository';
import {createOrderApi} from './paymentApi';

export const paymentRepositoryImpl: IPaymentRepository = {
  createOrder: (ticketIds: string[]) => createOrderApi(ticketIds),
};
