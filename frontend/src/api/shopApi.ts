import api from './axiosClient';
import type { ProductResponse } from '../types/product';
import type { Shop, ShopRequest } from '../types/shop';

export const listShops = (keyword = '', page = 0, size = 20) =>
  api.get<ProductResponse<Shop>>('/api/shops', { params: { keyword: keyword || undefined, page, size } });
export const myShops = () => api.get<Shop[]>('/api/shops/mine');
export const createShop = (data: ShopRequest) => api.post<Shop>('/api/shops', data);
export const updateShop = (id: string, data: ShopRequest) => api.put<Shop>(`/api/shops/${id}`, data);

/** Mọi gian hàng (dùng cho ô chọn gian hàng của ADMIN). */
export async function allShops() {
  const first = (await listShops('', 0, 100)).data;
  const rest = await Promise.all(Array.from({ length: Math.max(0, first.totalPages - 1) }, (_, i) => listShops('', i + 1, 100)));
  return [...first.items, ...rest.flatMap(r => r.data.items)];
}
