import type { Category } from './category';

export type ProductStatus = 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';

export interface Product {
  id: string;
  sku: string;
  name: string;
  description: string;
  price: number;
  currency: string;
  status: ProductStatus;
  category?: Category;
  createdAt: string;
  updatedAt: string;
  version: number;
  imageUrl?: string | null;
}

export interface ProductResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
  isLast: boolean;
}

export interface ProductFormValues {
  sku: string;
  name: string;
  description: string;
  price: number;
  currency: string;
  status: ProductStatus;
  categoryId: string;
}

export const emptyProductForm: ProductFormValues = {
  sku: '',
  name: '',
  description: '',
  price: 0,
  currency: 'VND',
  status: 'ACTIVE',
  categoryId: '',
};

export interface CreateProductRequest {
  sku: string;
  name: string;
  description: string;
  price: number;
  currency: string;
  categoryId: string;
}

export interface UpdateProductRequest {
  categoryId: string;
  name: string;
  description: string;
  price: number;
  status: ProductStatus;
}
