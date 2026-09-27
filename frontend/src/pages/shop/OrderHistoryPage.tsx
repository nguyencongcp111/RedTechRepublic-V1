import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getOrdersByUser } from '../../api/orderApi';
import type { Order } from '../../types/order';

// Status badge config
const STATUS_MAP: Record<string, { label: string; color: string; bg: string }> = {
  PENDING:   { label: 'Chờ xử lý',    color: '#92400e', bg: '#fef3c7' },
  CONFIRMED: { label: 'Đã xác nhận',  color: '#1e40af', bg: '#dbeafe' },
  INVENTORY_RESERVED:{ label: 'Đã giữ hàng', color: '#92400e', bg: '#fef3c7' },
  PAYMENT_PROCESSING:{ label: 'Chờ xác nhận thanh toán', color: '#92400e', bg: '#fef3c7' },
  CANCELLED: { label: 'Đã huỷ',      color: '#7f1d1d', bg: '#fee2e2' },
  FAILED:    { label: 'Thất bại',     color: '#7f1d1d', bg: '#fee2e2' },
};

function StatusBadge({ status }: { status: string }) {
  const cfg = STATUS_MAP[status] ?? { label: status, color: '#374151', bg: '#f3f4f6' };
  return (
    <span style={{
      padding: '2px 10px',
      borderRadius: 99,
      fontSize: 12,
      fontWeight: 600,
      color: cfg.color,
      background: cfg.bg,
      display: 'inline-block',
    }}>
      {cfg.label}
    </span>
  );
}

function formatPrice(amount: number, currency: string) {
  return new Intl.NumberFormat('vi-VN').format(amount) + ' ' + currency;
}

function formatDate(iso?: string) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

export default function OrderHistoryPage() {
  const { user } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expanded, setExpanded] = useState<string | null>(null);

  useEffect(() => {
    if (!user) return;
    getOrdersByUser(user.userId)
      .then(res => setOrders(res.data))
      .catch(() => setError('Không thể tải danh sách đơn hàng.'))
      .finally(() => setLoading(false));
  }, [user]);

  if (!user) {
    return (
      <div style={{ textAlign: 'center', padding: 64 }}>
        <p>Bạn cần <Link to="/login">đăng nhập</Link> để xem đơn hàng.</p>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 860, margin: '0 auto', padding: '32px 16px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <h1 style={{ fontSize: 22, fontWeight: 700, margin: 0 }}>📦 Đơn hàng của tôi</h1>
        <Link to="/shop" style={{ fontSize: 13, color: 'var(--color-primary)' }}>← Tiếp tục mua sắm</Link>
      </div>

      {loading && (
        <div style={{ textAlign: 'center', padding: 64, color: 'var(--color-text-muted)' }}>
          Đang tải đơn hàng...
        </div>
      )}

      {error && (
        <div style={{ background: '#fee2e2', color: '#7f1d1d', borderRadius: 10, padding: '12px 18px', marginBottom: 16 }}>
          {error}
        </div>
      )}

      {!loading && !error && orders.length === 0 && (
        <div style={{ textAlign: 'center', padding: 64, color: 'var(--color-text-muted)' }}>
          <div style={{ fontSize: 48, marginBottom: 16 }}>📭</div>
          <p style={{ fontSize: 16 }}>Bạn chưa có đơn hàng nào.</p>
          <Link to="/shop" className="btn btn-primary btn-sm" style={{ marginTop: 12 }}>Mua sắm ngay</Link>
        </div>
      )}

      {!loading && orders.map(order => (
        <div
          key={order.id}
          style={{
            background: '#fff',
            border: '1px solid var(--color-border)',
            borderRadius: 12,
            marginBottom: 16,
            overflow: 'hidden',
            boxShadow: '0 1px 4px rgba(0,0,0,0.05)',
          }}
        >
          {/* Header row */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 12,
              padding: '14px 20px',
              cursor: 'pointer',
              background: 'var(--color-bg-secondary)',
              flexWrap: 'wrap',
            }}
            onClick={() => setExpanded(expanded === order.id ? null : order.id)}
          >
            <div style={{ flex: 1, minWidth: 200 }}>
              <div style={{ fontSize: 12, color: 'var(--color-text-muted)', marginBottom: 2 }}>Mã đơn hàng</div>
              <div style={{ fontFamily: 'monospace', fontSize: 13, fontWeight: 600, color: '#374151' }}>
                {order.id.slice(0, 18)}…
              </div>
            </div>
            <div style={{ textAlign: 'center', minWidth: 120 }}>
              <div style={{ fontSize: 12, color: 'var(--color-text-muted)', marginBottom: 2 }}>Ngày tạo</div>
              <div style={{ fontSize: 13 }}>{formatDate(order.createdAt)}</div>
            </div>
            <div style={{ textAlign: 'center', minWidth: 100 }}>
              <div style={{ fontSize: 12, color: 'var(--color-text-muted)', marginBottom: 2 }}>Tổng tiền</div>
              <div style={{ fontSize: 14, fontWeight: 700, color: 'var(--color-primary)' }}>
                {formatPrice(order.totalAmount, order.currency)}
              </div>
            </div>
            <div style={{ textAlign: 'center', minWidth: 110 }}>
              <StatusBadge status={order.status} />
            </div>
            <div style={{ color: '#9ca3af', fontSize: 18 }}>
              {expanded === order.id ? '▲' : '▼'}
            </div>
          </div>

          {/* Expanded items */}
          {expanded === order.id && (
            <div style={{ padding: '16px 20px' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--color-border)' }}>
                    <th style={{ textAlign: 'left', padding: '6px 8px', color: 'var(--color-text-muted)', fontWeight: 500 }}>Sản phẩm</th>
                    <th style={{ textAlign: 'right', padding: '6px 8px', color: 'var(--color-text-muted)', fontWeight: 500 }}>SKU</th>
                    <th style={{ textAlign: 'right', padding: '6px 8px', color: 'var(--color-text-muted)', fontWeight: 500 }}>SL</th>
                    <th style={{ textAlign: 'right', padding: '6px 8px', color: 'var(--color-text-muted)', fontWeight: 500 }}>Đơn giá</th>
                    <th style={{ textAlign: 'right', padding: '6px 8px', color: 'var(--color-text-muted)', fontWeight: 500 }}>Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                  {(order.items ?? []).map((item, idx) => (
                    <tr key={idx} style={{ borderBottom: '1px solid #f3f4f6' }}>
                      <td style={{ padding: '8px 8px', fontWeight: 500 }}>{item.productName}</td>
                      <td style={{ textAlign: 'right', padding: '8px 8px', color: 'var(--color-text-muted)', fontFamily: 'monospace', fontSize: 11 }}>{item.sku}</td>
                      <td style={{ textAlign: 'right', padding: '8px 8px' }}>{item.quantity}</td>
                      <td style={{ textAlign: 'right', padding: '8px 8px' }}>{formatPrice(item.unitPrice, item.currency)}</td>
                      <td style={{ textAlign: 'right', padding: '8px 8px', fontWeight: 600 }}>{formatPrice(item.unitPrice * item.quantity, item.currency)}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr>
                    <td colSpan={4} style={{ textAlign: 'right', padding: '10px 8px', fontWeight: 600 }}>Tổng cộng:</td>
                    <td style={{ textAlign: 'right', padding: '10px 8px', fontWeight: 700, color: 'var(--color-primary)' }}>
                      {formatPrice(order.totalAmount, order.currency)}
                    </td>
                  </tr>
                </tfoot>
              </table>

              <div style={{ display: 'flex', gap: 8, marginTop: 10, justifyContent: 'flex-end' }}>
                <Link
                  to={`/shop/payments?orderId=${order.id}`}
                  className="btn btn-ghost btn-sm"
                  style={{ fontSize: 12 }}
                >
                  🧾 Xem lịch sử thanh toán
                </Link>
              </div>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
