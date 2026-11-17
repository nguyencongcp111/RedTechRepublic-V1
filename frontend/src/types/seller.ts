import type { Role } from './auth';

export type SellerStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface SellerApplicationRequest {
  name: string;
  address: string;
  phone: string;
  email: string;
}

export interface SellerInfo extends SellerApplicationRequest {
  id: string;
  userId: string;
  username: string;
  status: SellerStatus;
  rejectReason?: string | null;
  reviewedBy?: string | null;
  reviewedAt?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UserSummary {
  id: string;
  username: string;
  role: Role;
  seller?: SellerInfo | null;
}
