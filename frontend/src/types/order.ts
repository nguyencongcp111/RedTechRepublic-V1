import type { PaymentMethod } from './payment';

export interface OrderItem {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  quantity: number;
  unitPrice: number;
  currency: string;
}

export interface Order {
  id: string;
  userId: string;
  status: string;
  totalQuantity: number;
  totalAmount: number;
  currency: string;
  items: OrderItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateOrderRequest {
  userId: string;
  currency: string;
  /** Backend requires paymentMethod field */
  paymentMethod: PaymentMethod;
  items: { productId: string; quantity: number }[];
}
