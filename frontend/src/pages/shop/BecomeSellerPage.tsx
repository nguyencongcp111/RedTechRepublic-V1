import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { applySeller, mySellerApplication } from '../../api/sellerApi';
import { errorText } from '../../api/common';
import { useAuth } from '../../context/AuthContext';
import type { SellerApplicationRequest, SellerInfo } from '../../types/seller';

const empty: SellerApplicationRequest = { name: '', address: '', phone: '', email: '' };
const STATUS_LABEL = { PENDING: 'Đang chờ duyệt', APPROVED: 'Đã được duyệt', REJECTED: 'Bị từ chối' } as const;

/** Khu vực của User: gửi hồ sơ ứng tuyển trở thành Seller và theo dõi trạng thái duyệt. */
export default function BecomeSellerPage() {
  const { user, refreshSession } = useAuth();
  const navigate = useNavigate();
  const [application, setApplication] = useState<SellerInfo | null>(null);
  const [form, setForm] = useState<SellerApplicationRequest>(empty);
  const [loading, setLoading] = useState(true), [busy, setBusy] = useState(false);
  const [error, setError] = useState(''), [message, setMessage] = useState('');

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const mine = await mySellerApplication();
      setApplication(mine);
      if (mine) setForm({ name: mine.name, address: mine.address, phone: mine.phone, email: mine.email });
    } catch (e) { setError(errorText(e)); } finally { setLoading(false); }
  }, []);
  useEffect(() => { const t = setTimeout(() => void load(), 0); return () => clearTimeout(t); }, [load]);

  /** Sau khi admin duyệt, token cũ vẫn mang role USER: làm mới phiên để nhận role SELLER. */
  async function activateSeller() {
    setBusy(true); setError('');
    try {
      const next = await refreshSession();
      if (next?.role === 'SELLER') navigate('/seller');
      else setError('Phiên đăng nhập chưa nhận quyền Seller. Hãy đăng xuất và đăng nhập lại.');
    } catch (e) { setError(errorText(e)); } finally { setBusy(false); }
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault(); setBusy(true); setError(''); setMessage('');
    try {
      const result = (await applySeller({ name: form.name.trim(), address: form.address.trim(), phone: form.phone.trim(), email: form.email.trim() })).data;
      setApplication(result); setMessage('Đã gửi yêu cầu. Quản trị viên sẽ xem xét sớm.');
    } catch (err) { setError(errorText(err)); } finally { setBusy(false); }
  }

  const set = (key: keyof SellerApplicationRequest) => (e: React.ChangeEvent<HTMLInputElement>) => setForm(v => ({ ...v, [key]: e.target.value }));
  const canSubmit = !application || application.status === 'REJECTED';

  return <div>
    <div className="page-heading"><div><p className="eyebrow">TÀI KHOẢN / NGƯỜI BÁN</p><h1 className="page-title">Ứng tuyển trở thành Seller</h1>
      <p className="page-subtitle">Mở gian hàng, đăng sản phẩm và quản lý kho của riêng bạn.</p></div></div>
    {error && <div className="alert alert-error" role="alert">{error}</div>}
    {message && <div className="alert alert-success" role="status">{message}</div>}

    {user?.role === 'SELLER' ? <section className="card"><h2 className="card-title">Bạn đã là Seller</h2>
      <p className="muted">Quản lý gian hàng và sản phẩm trong Kênh người bán.</p><Link className="btn btn-primary" to="/seller">Mở Kênh người bán →</Link></section>
    : loading ? <div className="state-center">Đang tải hồ sơ…</div> : <>
      {application && <section className="card section-space">
        <div className="card-header"><h2 className="card-title">Hồ sơ của bạn</h2>
          <span className={`badge ${application.status === 'APPROVED' ? 'badge-success' : application.status === 'REJECTED' ? 'badge-danger' : 'badge-warning'}`}>{STATUS_LABEL[application.status]}</span></div>
        <p><strong>{application.name}</strong> · {application.phone} · {application.email}</p><p className="muted">{application.address}</p>
        <p className="form-hint">Gửi lúc {new Date(application.updatedAt).toLocaleString('vi-VN')}{application.reviewedAt ? ` · xử lý lúc ${new Date(application.reviewedAt).toLocaleString('vi-VN')}` : ''}</p>
        {application.status === 'REJECTED' && <div className="alert alert-error">Lý do: {application.rejectReason || 'Không ghi lý do.'} Bạn có thể chỉnh hồ sơ và gửi lại bên dưới.</div>}
        {application.status === 'PENDING' && <button className="btn btn-ghost" disabled={busy} onClick={() => void load()}>Kiểm tra lại trạng thái</button>}
        {application.status === 'APPROVED' && <button className="btn btn-primary" disabled={busy} onClick={() => void activateSeller()}>{busy ? 'Đang kích hoạt…' : 'Kích hoạt quyền Seller →'}</button>}
      </section>}

      {canSubmit && <section className="card section-space"><h2 className="card-title">{application ? 'Gửi lại hồ sơ' : 'Thông tin người bán'}</h2>
        <form onSubmit={submit}>
          <label className="form-group">Tên người bán / doanh nghiệp<input className="form-control" required maxLength={255} value={form.name} onChange={set('name')} /></label>
          <label className="form-group">Địa chỉ<input className="form-control" required maxLength={500} value={form.address} onChange={set('address')} /></label>
          <div className="form-grid">
            <label className="form-group">Số điện thoại<input className="form-control" required type="tel" pattern="\+?[0-9 .\-]{8,20}" title="8–20 chữ số" value={form.phone} onChange={set('phone')} /></label>
            <label className="form-group">Email<input className="form-control" required type="email" maxLength={255} value={form.email} onChange={set('email')} /></label>
          </div>
          <button className="btn btn-primary" disabled={busy}>{busy ? 'Đang gửi…' : 'Gửi yêu cầu duyệt'}</button>
        </form></section>}
    </>}
  </div>;
}
