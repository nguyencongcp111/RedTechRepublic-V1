import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getOrdersByUser } from '../../api/orderApi';
import { getPaymentsByOrders, getPaymentByOrderId } from '../../api/paymentApi';
import type { Order } from '../../types/order';
import type { Payment } from '../../types/payment';

const METHOD_LABEL: Record<string, string> = {
  CARD: '💳 Thẻ tín dụng',
  BANK_TRANSFER: '🏦 Chuyển khoản',
  E_WALLET: '📲 Ví điện tử',
  CASH_ON_DELIVERY: '💵 COD',
};

const STATUS_MAP: Record<string, { label: string; color: string; bg: string }> = {
  PENDING:   { label: 'Chờ thanh toán', color: '#92400e', bg: '#fef3c7' },
  SUCCEEDED: { label: 'Thành công',     color: '#065f46', bg: '#d1fae5' },
  FAILED:    { label: 'Thất bại',       color: '#7f1d1d', bg: '#fee2e2' },
  REFUNDED:  { label: 'Đã hoàn tiền',  color: '#1e40af', bg: '#dbeafe' },
  CANCELLED: { label: 'Đã huỷ',        color: '#374151', bg: '#f3f4f6' },
};

function StatusBadge({ status }: { status: string }) {
  const cfg = STATUS_MAP[status] ?? { label: status, color: '#374151', bg: '#f3f4f6' };
  return (
    <span style={{
      padding: '2px 10px', borderRadius: 99, fontSize: 12, fontWeight: 600,
      color: cfg.color, background: cfg.bg, display: 'inline-block',
    }}>
      {cfg.label}
    </span>
  );
}

function formatPrice(amount: number, currency: string) {
  return new Intl.NumberFormat('vi-VN').format(amount) + ' ' + currency;
}

function formatDate(iso?: string | null) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

export default function PaymentHistoryPage() {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const filterOrderId = searchParams.get('orderId');

  const [payments, setPayments] = useState<Payment[]>([]);
  const [orderMap, setOrderMap] = useState<Record<string, Order>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user) return;

    const load = async () => {
      if (filterOrderId) {
        // Single order filter (came from order history page)
        const [pyRes, ordRes] = await Promise.all([
          getPaymentByOrderId(filterOrderId),
          getOrdersByUser(user.userId),
        ]);
        setPayments(pyRes.data);
        const map: Record<string, Order> = {};
        ordRes.data.forEach(o => { map[o.id] = o; });
        setOrderMap(map);
      } else {
        // All payments for user's orders
        const ordRes = await getOrdersByUser(user.userId);
        const orders = ordRes.data;
        const map: Record<string, Order> = {};
        orders.forEach(o => { map[o.id] = o; });
        setOrderMap(map);

        if (orders.length === 0) {
          setPayments([]);
          return;
        }
        const pyRes = await getPaymentsByOrders(orders.map(o => o.id));
        // Sort newest first
        setPayments(pyRes.data.sort((a, b) =>
          new Date(b.createdAt ?? 0).getTime() - new Date(a.createdAt ?? 0).getTime()
        ));
      }
    };

    load().catch(() => setError('Không thể tải lịch sử thanh toán.')).finally(() => setLoading(false));
  }, [user, filterOrderId]);

  if (!user) {
    return (
      <div style={{ textAlign: 'center', padding: 64 }}>
        <p>Bạn cần <Link to="/login">đăng nhập</Link> để xem lịch sử thanh toán.</p>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 860, margin: '0 auto', padding: '32px 16px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700, margin: 0 }}>🧾 Lịch sử thanh toán</h1>
          {filterOrderId && (
            <p style={{ fontSize: 12, color: 'var(--color-text-muted)', marginTop: 4 }}>
              Đơn hàng: <code style={{ fontFamily: 'monospace' }}>{filterOrderId}</code>
            </p>
          )}
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          {filterOrderId && (
            <Link to="/shop/payments" style={{ fontSize: 13, color: 'var(--color-primary)' }}>
              Xem tất cả
            </Link>
          )}
          <Link to="/shop/orders" style={{ fontSize: 13, color: 'var(--color-text-muted)' }}>← Đơn hàng</Link>
        </div>
      </div>

      {loading && (
        <div style={{ textAlign: 'center', padding: 64, color: 'var(--color-text-muted)' }}>
          Đang tải lịch sử...
        </div>
      )}

      {error && (
        <div style={{ background: '#fee2e2', color: '#7f1d1d', borderRadius: 10, padding: '12px 18px', marginBottom: 16 }}>
          {error}
        </div>
      )}

      {!loading && !error && payments.length === 0 && (
        <div style={{ textAlign: 'center', padding: 64, color: 'var(--color-text-muted)' }}>
          <div style={{ fontSize: 48, marginBottom: 16 }}>🧾</div>
          <p style={{ fontSize: 16 }}>Chưa có giao dịch thanh toán nào.</p>
          <Link to="/shop" className="btn btn-primary btn-sm" style={{ marginTop: 12 }}>Mua sắm ngay</Link>
        </div>
      )}

      {!loading && payments.length > 0 && (
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13, background: '#fff', borderRadius: 12, overflow: 'hidden', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
            <thead>
              <tr style={{ background: 'var(--color-bg-secondary)', borderBottom: '1px solid var(--color-border)' }}>
                <th style={{ padding: '12px 14px', textAlign: 'left', fontWeight: 600, color: '#374151' }}>Mã giao dịch</th>
                <th style={{ padding: '12px 14px', textAlign: 'left', fontWeight: 600, color: '#374151' }}>Đơn hàng</th>
                <th style={{ padding: '12px 14px', textAlign: 'right', fontWeight: 600, color: '#374151' }}>Số tiền</th>
                <th style={{ padding: '12px 14px', textAlign: 'center', fontWeight: 600, color: '#374151' }}>Phương thức</th>
                <th style={{ padding: '12px 14px', textAlign: 'center', fontWeight: 600, color: '#374151' }}>Trạng thái</th>
                <th style={{ padding: '12px 14px', textAlign: 'center', fontWeight: 600, color: '#374151' }}>Ngày thanh toán</th>
                <th style={{ padding: '12px 14px', textAlign: 'center', fontWeight: 600, color: '#374151' }}>Ngày tạo</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((pay, idx) => {
                const order = orderMap[pay.orderId] ?? null;
                return (
                  <tr key={pay.id} style={{ borderBottom: '1px solid #f3f4f6', background: idx % 2 === 0 ? '#fff' : '#fafafa' }}>
                    <td style={{ padding: '10px 14px', fontFamily: 'monospace', fontSize: 11, color: '#374151' }}>
                      {pay.id.slice(0, 14)}…
                    </td>
                    <td style={{ padding: '10px 14px', fontSize: 11 }}>
                      {order ? (
                        <Link
                          to={`/shop/orders`}
                          style={{ color: 'var(--color-primary)', textDecoration: 'none', fontFamily: 'monospace' }}
                          title={pay.orderId}
                        >
                          {pay.orderId.slice(0, 14)}…
                        </Link>
                      ) : (
                        <span style={{ fontFamily: 'monospace' }}>{pay.orderId.slice(0, 14)}…</span>
                      )}
                    </td>
                    <td style={{ padding: '10px 14px', textAlign: 'right', fontWeight: 700, color: pay.status === 'SUCCEEDED' ? '#065f46' : '#374151' }}>
                      {formatPrice(pay.amount, pay.currency)}
                    </td>
                    <td style={{ padding: '10px 14px', textAlign: 'center' }}>
                      {METHOD_LABEL[pay.paymentMethod] ?? pay.paymentMethod}
                    </td>
                    <td style={{ padding: '10px 14px', textAlign: 'center' }}>
                      <StatusBadge status={pay.status} />
                    </td>
                    <td style={{ padding: '10px 14px', textAlign: 'center', color: 'var(--color-text-muted)' }}>
                      {formatDate(pay.paidAt)}
                    </td>
                    <td style={{ padding: '10px 14px', textAlign: 'center', color: 'var(--color-text-muted)' }}>
                      {formatDate(pay.createdAt)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
