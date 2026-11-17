import axiosClient from './axiosClient';
import type { CreateOrderRequest, Order } from '../types/order';
import type { PaymentMethod } from '../types/payment';

export const createOrder = (idempotencyKey: string, data: CreateOrderRequest) =>
  axiosClient.post<Order>('/api/orders', data, {
    headers: { 'Idempotency-Key': idempotencyKey },
  });

export const getOrder = (id: string) =>
  axiosClient.get<Order>(`/api/orders/${id}`);

/** Sửa nhanh (ADMIN): phương thức thanh toán; số lượng item khi đơn còn PENDING. */
export const quickEditOrder = (id: string, data: { paymentMethod?: PaymentMethod; items?: { itemId: string; quantity: number }[] }) =>
  axiosClient.patch<Order>(`/api/orders/${id}`, data);

export const getOrdersByUser = (userId: string) =>
  axiosClient.get<Order[]>(`/api/orders/user/${userId}`);
