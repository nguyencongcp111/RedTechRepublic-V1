import { useState } from 'react';
import { emptyShop } from '../types/shop';
import type { Shop, ShopRequest } from '../types/shop';

/** Form gian hàng dùng chung cho ADMIN và SELLER. Người tạo lấy từ token. */
export default function ShopForm({ shop, onSubmit, onCancel, submitting, error }: {
  shop: Shop | null; onSubmit: (values: ShopRequest) => Promise<void>; onCancel: () => void; submitting: boolean; error: string;
}) {
  const [values, setValues] = useState<ShopRequest>(shop ? { name: shop.name, address: shop.address, phone: shop.phone, email: shop.email } : emptyShop);
  const set = (key: keyof ShopRequest) => (e: React.ChangeEvent<HTMLInputElement>) => setValues(v => ({ ...v, [key]: e.target.value }));
  return <form onSubmit={e => { e.preventDefault(); void onSubmit({ name: values.name.trim(), address: values.address.trim(), phone: values.phone.trim(), email: values.email.trim() }); }}>
    {error && <div className="alert alert-error" role="alert">{error}</div>}
    <label className="form-group">Tên gian hàng<input className="form-control" required maxLength={255} value={values.name} onChange={set('name')} /></label>
    <label className="form-group">Địa chỉ<input className="form-control" required maxLength={500} value={values.address} onChange={set('address')} /></label>
    <div className="form-grid">
      <label className="form-group">Số điện thoại<input className="form-control" required type="tel" pattern="\+?[0-9 .\-]{8,20}" title="8–20 chữ số" value={values.phone} onChange={set('phone')} /></label>
      <label className="form-group">Email<input className="form-control" required type="email" maxLength={255} value={values.email} onChange={set('email')} /></label>
    </div>
    <div className="modal-footer"><button type="button" className="btn btn-ghost" onClick={onCancel} disabled={submitting}>Hủy</button>
      <button className="btn btn-primary" disabled={submitting}>{submitting ? 'Đang lưu…' : shop ? 'Lưu gian hàng' : 'Tạo gian hàng'}</button></div>
  </form>;
}
