import { useEffect, useState } from 'react';
import { createShop, listShops, updateShop } from '../api/shopApi';
import { errorText } from '../api/common';
import type { Shop, ShopRequest } from '../types/shop';
import ShopForm from '../components/ShopForm';
import Modal from '../components/Modal';

/** Quản trị gian hàng: ADMIN xem mọi gian hàng, tạo gian hàng của mình và sửa bất kỳ gian hàng nào. */
export default function ShopsPage() {
  const [shops, setShops] = useState<Shop[]>([]);
  const [keyword, setKeyword] = useState(''), [page, setPage] = useState(0), [pages, setPages] = useState(0);
  const [revision, setRevision] = useState(0), [loading, setLoading] = useState(true);
  const [error, setError] = useState(''), [message, setMessage] = useState('');
  const [editing, setEditing] = useState<Shop | null | undefined>(undefined);
  const [saving, setSaving] = useState(false), [formError, setFormError] = useState('');

  useEffect(() => {
    let live = true;
    const t = setTimeout(() => {
      setLoading(true); setError('');
      listShops(keyword.trim(), page, 20).then(r => { if (live) { setShops(r.data.items); setPages(r.data.totalPages); } })
        .catch(e => { if (live) setError(errorText(e)); }).finally(() => { if (live) setLoading(false); });
    }, 250);
    return () => { live = false; clearTimeout(t); };
  }, [keyword, page, revision]);

  async function save(values: ShopRequest) {
    setSaving(true); setFormError('');
    try {
      if (editing) await updateShop(editing.id, values); else await createShop(values);
      setEditing(undefined); setMessage('Đã lưu gian hàng.'); setRevision(r => r + 1);
    } catch (e) { setFormError(errorText(e)); } finally { setSaving(false); }
  }

  return <div>
    <div className="page-heading"><div><p className="eyebrow">CATALOG / GIAN HÀNG</p><h1 className="page-title">Gian hàng</h1>
      <p className="page-subtitle">Chỉ ADMIN và SELLER được tạo gian hàng. Người tạo lấy từ tài khoản đang đăng nhập.</p></div>
      <button className="btn btn-primary" onClick={() => { setFormError(''); setEditing(null); }}>+ Tạo gian hàng</button></div>
    {message && <div className="alert alert-success" role="status">{message}</div>}
    {error && <div className="alert alert-error" role="alert">{error}</div>}
    <section className="card">
      <div className="toolbar"><input className="form-control" aria-label="Tìm gian hàng" placeholder="Tìm theo tên gian hàng hoặc người tạo…" value={keyword} onChange={e => { setKeyword(e.target.value); setPage(0); }} /></div>
      {loading ? <div className="state-center">Đang tải gian hàng…</div> : <div className="table-wrap"><table className="table">
        <thead><tr><th>Gian hàng</th><th>Người tạo</th><th>Liên hệ</th><th>Địa chỉ</th><th /></tr></thead><tbody>
        {shops.map(s => <tr key={s.id}><td><strong>{s.name}</strong> {s.system && <span className="badge badge-info">Hệ thống</span>}<div className="mono muted">{s.id}</div></td>
          <td>@{s.ownerUsername}</td><td>{s.phone}<div className="muted">{s.email}</div></td><td>{s.address}</td>
          <td><button className="btn btn-ghost btn-sm" onClick={() => { setFormError(''); setEditing(s); }}>Sửa</button></td></tr>)}
        {!shops.length && <tr><td colSpan={5}>Chưa có gian hàng phù hợp.</td></tr>}
      </tbody></table></div>}
      <div className="pagination"><button className="btn btn-ghost" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Trước</button><span>{page + 1} / {Math.max(1, pages)}</span>
        <button className="btn btn-ghost" disabled={page + 1 >= pages} onClick={() => setPage(p => p + 1)}>Sau →</button></div>
    </section>
    {editing !== undefined && <Modal title={editing ? 'Sửa gian hàng' : 'Tạo gian hàng'} onClose={() => { if (!saving) setEditing(undefined); }}>
      <ShopForm shop={editing} onSubmit={save} onCancel={() => setEditing(undefined)} submitting={saving} error={formError} /></Modal>}
  </div>;
}
