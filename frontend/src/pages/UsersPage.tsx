import { useEffect, useState } from 'react';
import { approveSeller, listSellerApplications, listUsers, rejectSeller } from '../api/sellerApi';
import { errorText, ROLE_LABEL } from '../api/common';
import type { Role } from '../types/auth';
import type { SellerInfo, SellerStatus, UserSummary } from '../types/seller';

type Tab = '' | Role | 'REQUESTS';
const STATUS_LABEL: Record<SellerStatus, string> = { PENDING: 'Chờ duyệt', APPROVED: 'Đã duyệt', REJECTED: 'Từ chối' };
const badge = (role: string) => role === 'ADMIN' ? 'badge-danger' : role === 'SELLER' ? 'badge-success' : 'badge-neutral';

/** Quản lý người dùng: lọc theo vai trò (gồm "Tài khoản Seller") và duyệt yêu cầu trở thành Seller. */
export default function UsersPage() {
  const [tab, setTab] = useState<Tab>('');
  const [users, setUsers] = useState<UserSummary[]>([]), [counts, setCounts] = useState<Partial<Record<Role, number>>>({});
  const [requests, setRequests] = useState<SellerInfo[]>([]), [requestStatus, setRequestStatus] = useState<SellerStatus | ''>('PENDING');
  const [pending, setPending] = useState(0);
  const [keyword, setKeyword] = useState(''), [page, setPage] = useState(0), [pages, setPages] = useState(0);
  const [revision, setRevision] = useState(0), [loading, setLoading] = useState(true), [busy, setBusy] = useState('');
  const [error, setError] = useState(''), [message, setMessage] = useState('');
  const refresh = () => setRevision(r => r + 1);

  useEffect(() => {
    let live = true;
    const t = setTimeout(() => {
      setLoading(true); setError('');
      const request = tab === 'REQUESTS'
        ? listSellerApplications(requestStatus, page, 20).then(r => { if (live) { setRequests(r.data.items); setPages(r.data.totalPages); setPending(r.data.pending); } })
        : listUsers(tab, keyword.trim(), page, 20).then(r => { if (live) { setUsers(r.data.items); setPages(r.data.totalPages); setCounts(r.data.roleCounts); } });
      request.catch(e => { if (live) setError(errorText(e)); }).finally(() => { if (live) setLoading(false); });
    }, 250);
    return () => { live = false; clearTimeout(t); };
  }, [tab, requestStatus, keyword, page, revision]);

  // Số yêu cầu chờ duyệt cho nhãn tab, kể cả khi đang xem danh sách user.
  useEffect(() => {
    let live = true;
    listSellerApplications('PENDING', 0, 1).then(r => { if (live) setPending(r.data.pending); }).catch(() => {});
    return () => { live = false; };
  }, [revision]);

  async function review(app: SellerInfo, approve: boolean) {
    let reason = '';
    if (approve) { if (!window.confirm(`Duyệt "${app.username}" thành Seller?`)) return; }
    else { const input = window.prompt(`Lý do từ chối "${app.username}" (có thể để trống):`, ''); if (input === null) return; reason = input; }
    setBusy(app.id); setError(''); setMessage('');
    try {
      if (approve) await approveSeller(app.id); else await rejectSeller(app.id, reason.trim());
      setMessage(approve ? `Đã duyệt ${app.username}. Tài khoản cần làm mới phiên (hoặc đăng nhập lại) để nhận quyền Seller.` : `Đã từ chối ${app.username}.`);
      refresh();
    } catch (e) { setError(errorText(e)); } finally { setBusy(''); }
  }

  const tabs: [Tab, string][] = [['', 'Tất cả'], ['USER', 'Khách hàng'], ['SELLER', 'Tài khoản Seller'], ['ADMIN', 'Quản trị'], ['REQUESTS', `Yêu cầu Seller${pending ? ` (${pending})` : ''}`]];
  const total = (counts.USER ?? 0) + (counts.SELLER ?? 0) + (counts.ADMIN ?? 0);
  const count = (t: Tab) => t === '' ? total : t === 'REQUESTS' ? null : counts[t] ?? 0;

  return <div>
    <div className="page-heading"><div><p className="eyebrow">QUẢN TRỊ / NGƯỜI DÙNG</p><h1 className="page-title">Người dùng</h1>
      <p className="page-subtitle">Tài khoản, vai trò và yêu cầu trở thành người bán.</p></div><button className="btn btn-ghost" onClick={refresh}>Làm mới</button></div>
    {message && <div className="alert alert-success" role="status">{message}</div>}
    {error && <div className="alert alert-error" role="alert">{error}</div>}
    <div className="category-chips" role="tablist" aria-label="Bộ lọc người dùng">{tabs.map(([key, label]) =>
      <button key={key || 'ALL'} role="tab" aria-selected={tab === key} className={`chip ${tab === key ? 'selected' : ''}`} onClick={() => { setTab(key); setPage(0); }}>
        {label}{count(key) != null && ` · ${count(key)}`}</button>)}</div>

    <section className="card">
      {tab === 'REQUESTS' ? <>
        <div className="toolbar"><select className="form-control" aria-label="Trạng thái yêu cầu" value={requestStatus} onChange={e => { setRequestStatus(e.target.value as SellerStatus | ''); setPage(0); }}>
          <option value="PENDING">Chờ duyệt</option><option value="APPROVED">Đã duyệt</option><option value="REJECTED">Từ chối</option><option value="">Tất cả</option></select></div>
        {loading ? <div className="state-center">Đang tải yêu cầu…</div> : <div className="table-wrap"><table className="table">
          <thead><tr><th>Tài khoản</th><th>Thông tin người bán</th><th>Gửi lúc</th><th>Trạng thái</th><th /></tr></thead><tbody>
          {requests.map(a => <tr key={a.id}><td><strong>{a.username}</strong></td>
            <td><strong>{a.name}</strong><div className="muted">{a.phone} · {a.email}</div><div className="muted">{a.address}</div></td>
            <td>{new Date(a.updatedAt).toLocaleString('vi-VN')}</td>
            <td><span className={`badge ${a.status === 'APPROVED' ? 'badge-success' : a.status === 'REJECTED' ? 'badge-danger' : 'badge-warning'}`}>{STATUS_LABEL[a.status]}</span>
              {a.reviewedBy && <div className="muted">bởi {a.reviewedBy}</div>}{a.rejectReason && <div className="muted">{a.rejectReason}</div>}</td>
            <td>{a.status === 'PENDING' && <div className="inline-actions">
              <button className="btn btn-primary btn-sm" disabled={!!busy} onClick={() => void review(a, true)}>Duyệt</button>
              <button className="btn btn-ghost btn-sm" disabled={!!busy} onClick={() => void review(a, false)}>Từ chối</button></div>}</td></tr>)}
          {!requests.length && <tr><td colSpan={5}>Không có yêu cầu nào.</td></tr>}
        </tbody></table></div>}
      </> : <>
        <div className="toolbar"><input className="form-control" aria-label="Tìm người dùng" placeholder="Tìm theo tên đăng nhập…" value={keyword} onChange={e => { setKeyword(e.target.value); setPage(0); }} /></div>
        {loading ? <div className="state-center">Đang tải người dùng…</div> : <div className="table-wrap"><table className="table">
          <thead><tr><th>Tên đăng nhập</th><th>Vai trò</th><th>Thông tin người bán</th></tr></thead><tbody>
          {users.map(u => <tr key={u.id}><td><strong>{u.username}</strong><div className="mono muted">{u.id}</div></td>
            <td><span className={`badge ${badge(u.role)}`}>{ROLE_LABEL[u.role] ?? u.role}</span></td>
            <td>{u.seller ? <><strong>{u.seller.name}</strong> <span className="muted">({STATUS_LABEL[u.seller.status]})</span>
              <div className="muted">{u.seller.phone} · {u.seller.email}</div><div className="muted">{u.seller.address}</div></> : <span className="muted">—</span>}</td></tr>)}
          {!users.length && <tr><td colSpan={3}>Không có người dùng phù hợp.</td></tr>}
        </tbody></table></div>}
      </>}
      <div className="pagination"><button className="btn btn-ghost" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Trước</button><span>{page + 1} / {Math.max(1, pages)}</span>
        <button className="btn btn-ghost" disabled={page + 1 >= pages} onClick={() => setPage(p => p + 1)}>Sau →</button></div>
    </section>
  </div>;
}
