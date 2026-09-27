import axiosClient from './axiosClient';
import type {
  CreateInventoryItemRequest,
  AdjustInventoryRequest,
  CreateReservationRequest,
  Reservation,
  TransitionStatus,
} from '../types/inventory';

export const createInventoryItem = (data: CreateInventoryItemRequest) =>
  axiosClient.post('/api/inventory/stock', data);

export const adjustInventory = (sku: string, data: AdjustInventoryRequest) =>
  axiosClient.patch(`/api/inventory/stock/${sku}`, data);

export const createReservation = (
  idempotencyKey: string,
  data: CreateReservationRequest
) =>
  axiosClient.post<Reservation>('/api/inventory/reservation', data, {
    headers: { 'Idempotency-Key': idempotencyKey },
  });

export const transitionReservation = (
  id: string,
  status: TransitionStatus
) =>
  axiosClient.patch(`/api/inventory/reservation/${id}/status`, { status });
