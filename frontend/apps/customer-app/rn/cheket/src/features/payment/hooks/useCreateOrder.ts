import {useMutation} from '@tanstack/react-query';
import {createOrderUseCase} from '../../../domains/payment/useCases/createOrder';
import {paymentRepositoryImpl} from '../services/paymentRepositoryImpl';

type CreateOrderVariables = {
  ticketIds: string[];
};

export function useCreateOrder() {
  return useMutation({
    mutationFn: ({ticketIds}: CreateOrderVariables) => createOrderUseCase(paymentRepositoryImpl, ticketIds),
  });
}
