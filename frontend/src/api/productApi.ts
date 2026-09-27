import api from './axiosClient';
import type { Product, ProductResponse, CreateProductRequest, UpdateProductRequest, ProductStatus } from '../types/product';
export const getProducts = (keyword = '', page = 0, size = 10, categoryId?: string, status?: ProductStatus, sortBy = 'createdAt', sortDirection = 'DESC') =>
  api.get<ProductResponse<Product>>('/api/products', { params: { keyword, page, size, categoryId: categoryId || undefined, status, sortBy, sortDirection } });
export const getProduct = (id: string) => api.get<Product>(`/api/products/${id}`);
export const getProductBySku = (sku: string) => api.get<Product>(`/api/products/by-sku/${encodeURIComponent(sku)}`);
export const createProduct = (data: CreateProductRequest) => api.post<Product>('/api/products', data);
export const updateProduct = (id: string, data: UpdateProductRequest) => api.put<Product>(`/api/products/${id}`, data);
export const uploadProductImage = (id: string, file: File) => {
  const body = new FormData(); body.append('file', file);
  return api.post<Product>(`/api/products/${id}/image`, body, { headers: { 'Content-Type': undefined } });
};
export const removeProductImage = (id: string) => api.delete<Product>(`/api/products/${id}/image`);
