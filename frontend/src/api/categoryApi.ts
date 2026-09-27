import api from './axiosClient';
import type { Category } from '../types/category';
import type { ProductResponse } from '../types/product';
export const getCategories = (keyword = '', page = 0, size = 100) =>
  api.get<ProductResponse<Category>>('/api/category', { params: { keyword, page, size } });
export const createCategory = (data: { name: string; description?: string }) => api.post<Category>('/api/category', data);
export async function allCategories() {
  const first = (await getCategories()).data;
  const rest = await Promise.all(Array.from({ length: Math.max(0, first.totalPages - 1) }, (_, i) => getCategories('', i + 1)));
  return [...first.items, ...rest.flatMap(r => r.data.items)];
}
