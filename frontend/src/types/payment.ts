export type PaymentMethod = 'CARD' | 'BANK_TRANSFER' | 'E_WALLET' | 'CASH_ON_DELIVERY';
export type PaymentStatus = 'PENDING' | 'SUCCEEDED' | 'FAILED' | 'REFUNDED' | 'CANCELLED';

export interface CreatePaymentRequest {
  orderId: string;
  amount: number;
  currency: string;
  paymentMethod: PaymentMethod;
}

export interface ConfirmPaymentRequest {
  providerReference: string;
}

export interface CancelPaymentRequest {
  reasonCode?: string;
  reason: string;
}

export interface Payment {
  id: string;
  orderId: string;
  amount: number;
  currency: string;
  paymentMethod: PaymentMethod;
  status: PaymentStatus;
  providerReference?: string;
  failureCode?: string;
  failureMessage?: string;
  createdAt?: string;
  updatedAt?: string;
  paidAt?: string;
}
