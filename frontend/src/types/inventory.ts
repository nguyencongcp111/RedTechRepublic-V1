export interface InventoryItem {
  id: string;
  productId: string;
  sku: string;
  onHand: number;
  reserved: number;
  available: number;
}

export interface CreateInventoryItemRequest {
  productId: string;
  sku: string;
  onHand: number;
}

export interface AdjustInventoryRequest {
  quantityDelta: number;
}

export interface CreateReservationRequest {
  orderId: string;
  sku: string;
  quantity: number;
}

export type ReservationStatus = 'ACTIVE' | 'CONFIRMED' | 'RELEASED' | 'EXPIRED';

export interface Reservation {
  id: string;
  orderId: string;
  inventoryItemId: string;
  sku: string;
  quantity: number;
  status: ReservationStatus;
  expiresAt: string;
}

export type TransitionStatus = 'CONFIRMED' | 'RELEASED' | 'EXPIRED';
