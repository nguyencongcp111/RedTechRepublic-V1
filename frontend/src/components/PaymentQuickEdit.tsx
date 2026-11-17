import { useState } from 'react';
import { quickEditPayment } from '../api/paymentApi';
import { errorText, PAYMENT_METHODS } from '../api/common';
import type { Payment, PaymentMethod } from '../types/payment';

/**
 * Sửa nhanh thanh toán (ADMIN, chỉ khi PENDING): phương thức và mã đối soát.
 * Trạng thái vẫn đổi qua các nút Xác nhận / Hủy / Hoàn tiền.
 */
export default function PaymentQuickEdit({ payment, onSaved }: { payment: Payment; onSaved: (payment: Payment) => void }) {
  const [method, setMethod] = useState<PaymentMethod>(payment.paymentMethod);
  const [reference, setReference] = useState(payment.providerReference ?? '');
  const [busy, setBusy] = useState(false), [error, setError] = useState('');
  const editable = payment.status === 'PENDING';
  const changed = method !== payment.paymentMethod || reference.trim() !== (payment.providerReference ?? '');

  async function save(e: React.FormEvent) {
    e.preventDefault(); setBusy(true); setError('');
    try {
      onSaved((await quickEditPayment(payment.id, {
        paymentMethod: method !== payment.paymentMethod ? method : undefined,
        providerReference: reference.trim() !== (payment.providerReference ?? '') ? reference.trim() : undefined,
      })).data);
    } catch (err) { setError(errorText(err)); } finally { setBusy(false); }
  }

  return <form className="card section-space" onSubmit={save}>
    <h3 className="card-title">Sửa nhanh</h3>
    {error && <div className="alert alert-error" role="alert">{error}</div>}
    <div className="form-grid">
      <label className="form-group">Phương thức<select className="form-control" disabled={!editable} value={method} onChange={e => setMethod(e.target.value as PaymentMethod)}>
        {PAYMENT_METHODS.map(m => <option key={m}>{m}</option>)}</select></label>
      <label className="form-group">Mã đối soát<input className="form-control" disabled={!editable} maxLength={255} placeholder="Để trống để xóa" value={reference} onChange={e => setReference(e.target.value)} /></label>
    </div>
    {!editable && <p className="form-hint">Chỉ sửa nhanh được thanh toán đang PENDING.</p>}
    <button className="btn btn-primary" disabled={!editable || busy || !changed}>{busy ? 'Đang lưu…' : 'Lưu thay đổi'}</button>
  </form>;
}
