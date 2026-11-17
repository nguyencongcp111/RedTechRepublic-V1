import axios from 'axios';
export function errorText(error: unknown) {
  if (axios.isAxiosError(error)) return error.response?.data?.detail ?? error.response?.data?.message ?? (error.response ? 'Yêu cầu không thực hiện được.' : 'Không kết nối được đến hệ thống.');
  return error instanceof Error ? error.message : 'Đã xảy ra lỗi.';
}
export const PAYMENT_METHODS = ['CARD', 'BANK_TRANSFER', 'E_WALLET', 'CASH_ON_DELIVERY'] as const;
export const ROLE_LABEL: Record<string, string> = { ADMIN: 'Quản trị viên', SELLER: 'Người bán', USER: 'Khách hàng' };
export const money = (amount: number, currency: string) => new Intl.NumberFormat('vi-VN').format(amount) + ' ' + currency;
