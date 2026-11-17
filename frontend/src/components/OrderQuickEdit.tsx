import { useState } from 'react';
import { quickEditOrder } from '../api/orderApi';
import { getPaymentByOrderId, quickEditPayment } from '../api/paymentApi';
import { errorText, money, PAYMENT_METHODS } from '../api/common';
import type { Order } from '../types/order';
import type { PaymentMethod } from '../types/payment';

const CLOSED = ['CONFIRMED', 'CANCELLED', 'FAILED'];

/**
 * Sửa nhanh đơn hàng (ADMIN): phương thức thanh toán (khi đơn chưa kết thúc) và
 * số lượng từng dòng (chỉ khi PENDING). Đổi phương thức cũng cập nhật các thanh toán
 * PENDING của đơn để hai bên không lệch nhau.
 */
export default function OrderQuickEdit({ order, onSaved }: { order: Order; onSaved: (order: Order, note: string) => void }) {
  const [method, setMethod] = useState<PaymentMethod>(order.paymentMethod ?? 'CASH_ON_DELIVERY');
  const [quantities, setQuantities] = useState<Record<string, number>>(() => Object.fromEntries(order.items.map(i => [i.id, i.quantity])));
  const [busy, setBusy] = useState(false), [error, setError] = useState('');
  const methodEditable = !CLOSED.includes(order.status);
  const quantityEditable = order.status === 'PENDING';
  const changedItems = order.items.filter(i => quantities[i.id] !== i.quantity).map(i => ({ itemId: i.id, quantity: quantities[i.id] }));
  const methodChanged = methodEditable && method !== order.paymentMethod;
  const newTotal = order.items.reduce((sum, i) => sum + i.unitPrice * (quantities[i.id] ?? i.quantity), 0);

  async function save(e: React.FormEvent) {
    e.preventDefault(); setBusy(true); setError('');
    try {
      const updated = (await quickEditOrder(order.id, {
        paymentMethod: methodChanged ? method : undefined,
        items: quantityEditable && changedItems.length ? changedItems : undefined,
      })).data;
      let note = 'Đã lưu đơn hàng.';
      if (methodChanged) {
        const pending = (await getPaymentByOrderId(order.id)).data.filter(p => p.status === 'PENDING' && p.paymentMethod !== method);
        await Promise.all(pending.map(p => quickEditPayment(p.id, { paymentMethod: method })));
        if (pending.length) note += ` Đã đồng bộ phương thức cho ${pending.length} thanh toán đang chờ.`;
      }
      onSaved(updated, note);
    } catch (err) { setError(errorText(err)); } finally { setBusy(false); }
  }

  if (!methodEditable && !quantityEditable) return <p className="form-hint">Đơn đã {order.status}: không còn trường nào sửa nhanh được.</p>;
  return <form className="card section-space" onSubmit={save}>
    <h3 className="card-title">Sửa nhanh</h3>
    {error && <div className="alert alert-error" role="alert">{error}</div>}
    <label className="form-group">Phương thức thanh toán
      <select className="form-control" value={method} disabled={!methodEditable} onChange={e => setMethod(e.target.value as PaymentMethod)}>
        {PAYMENT_METHODS.map(m => <option key={m}>{m}</option>)}</select></label>
    {quantityEditable ? <div className="table-wrap"><table className="table"><thead><tr><th>Sản phẩm</th><th>Số lượng</th></tr></thead><tbody>
      {order.items.map(i => <tr key={i.id}><td>{i.productName} <span className="muted">{i.sku}</span></td>
        <td><input className="form-control" style={{ width: 100 }} type="number" min={1} step={1} required aria-label={'Số lượng ' + i.sku}
          value={quantities[i.id]} onChange={e => setQuantities(q => ({ ...q, [i.id]: Math.max(1, Math.trunc(Number(e.target.value)) || 1) }))} /></td></tr>)}
    </tbody></table><p className="muted">Tổng mới: {money(newTotal, order.currency)}</p></div>
      : <p className="form-hint">Số lượng chỉ sửa được khi đơn còn PENDING (chưa giữ hàng trong kho).</p>}
    <button className="btn btn-primary" disabled={busy || (!methodChanged && !changedItems.length)}>{busy ? 'Đang lưu…' : 'Lưu thay đổi'}</button>
  </form>;
}
