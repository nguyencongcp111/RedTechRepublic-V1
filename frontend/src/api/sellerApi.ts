import api from './axiosClient';
import type { ProductResponse } from '../types/product';
import type { SellerApplicationRequest, SellerInfo, SellerStatus, UserSummary } from '../types/seller';
import type { Role } from '../types/auth';

/** Hồ sơ ứng tuyển của chính mình; null nếu chưa từng gửi (API trả 204). */
export const mySellerApplication = async () => (await api.get<SellerInfo | ''>('/api/auth/seller-applications/me')).data || null;
export const applySeller = (data: SellerApplicationRequest) => api.post<SellerInfo>('/api/auth/seller-applications', data);

export const listSellerApplications = (status: SellerStatus | '' = 'PENDING', page = 0, size = 20) =>
  api.get<ProductResponse<SellerInfo> & { pending: number }>('/api/auth/seller-applications', { params: { status: status || undefined, page, size } });
export const approveSeller = (id: string) => api.post<SellerInfo>(`/api/auth/seller-applications/${id}/approve`);
export const rejectSeller = (id: string, reason: string) => api.post<SellerInfo>(`/api/auth/seller-applications/${id}/reject`, { reason });

export const listUsers = (role: Role | '' = '', keyword = '', page = 0, size = 20) =>
  api.get<ProductResponse<UserSummary> & { roleCounts: Record<Role, number> }>('/api/auth/users', { params: { role: role || undefined, keyword: keyword || undefined, page, size } });
