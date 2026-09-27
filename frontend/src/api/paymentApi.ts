import axiosClient from './axiosClient';
import type {
  Payment,
  CreatePaymentRequest,
  ConfirmPaymentRequest,
  CancelPaymentRequest,
} from '../types/payment';

export const createPayment = (
  idempotencyKey: string,
  data: CreatePaymentRequest
) =>
  axiosClient.post<Payment>('/api/payments', data, {
    headers: { 'Idempotency-Key': idempotencyKey },
  });

export const getPaymentById = (id: string) =>
  axiosClient.get<Payment>(`/api/payments/${id}`);

export const getPaymentByOrderId = (orderId: string) =>
  axiosClient.get<Payment[]>(`/api/payments/by-order/${orderId}`);

export const getPaymentsByOrders = (orderIds: string[]) =>
  axiosClient.post<Payment[]>('/api/payments/by-orders', orderIds);

export const confirmPayment = (id: string, data: ConfirmPaymentRequest) =>
  axiosClient.post<Payment>(`/api/payments/${id}/confirm`, data);

export const cancelPayment = (id: string, data: CancelPaymentRequest) =>
  axiosClient.post<Payment>(`/api/payments/${id}/cancel`, data);

export const refundPayment = (id: string) =>
  axiosClient.post<Payment>(`/api/payments/${id}/refund`);
