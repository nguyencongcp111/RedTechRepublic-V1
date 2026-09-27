import axiosClient from './axiosClient';
import type { CreateOrderRequest, Order } from '../types/order';

export const createOrder = (idempotencyKey: string, data: CreateOrderRequest) =>
  axiosClient.post<Order>('/api/orders', data, {
    headers: { 'Idempotency-Key': idempotencyKey },
  });

export const getOrder = (id: string) =>
  axiosClient.get<Order>(`/api/orders/${id}`);

export const getOrdersByUser = (userId: string) =>
  axiosClient.get<Order[]>(`/api/orders/user/${userId}`);
