import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '../../context/AuthContext';
import { createShop, myShops, updateShop } from '../../api/shopApi';
import { getProducts, createProduct, updateProduct, uploadProductImage } from '../../api/productApi';
import { adjustInventory, createInventoryItem } from '../../api/inventoryApi';
import { errorText, money } from '../../api/common';
import type { Shop, ShopRequest } from '../../types/shop';
import type { Product, CreateProductRequest, UpdateProductRequest } from '../../types/product';
import ProductForm from '../../components/ProductForm';
import ShopForm from '../../components/ShopForm';
import Modal from '../../components/Modal';
import StockBadge from '../../components/StockBadge';

/** Kênh người bán: SELLER quản lý gian hàng, sản phẩm và kho của chính mình. */
export default function SellerHubPage() {
  const { user } = useAuth();
  const [tab, setTab] = useState<'shops' | 'products'>('shops');
  const [shops, setShops] = useState<Shop[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [keyword, setKeyword] = useState(''), [page, setPage] = useState(0), [pages, setPages] = useState(0);
  const [revision, setRevision] = useState(0), [loading, setLoading] = useState(true);
  const [error, setError] = useState(''), [message, setMessage] = useState('');
  const [shopModal, setShopModal] = useState<Shop | null | undefined>(undefined);
  const [productModal, setProductModal] = useState<Product | null | undefined>(undefined);
  const [saving, setSaving] = useState(false), [formError, setFormError] = useState('');
  const [stockDelta, setStockDelta] = useState<Record<string, number>>({});
  const refresh = () => setRevision(r => r + 1);

  useEffect(() => {
    let live = true;
    myShops().then(r => { if (live) setShops(r.data); }).catch(e => { if (live) setError(errorText(e)); });
    return () => { live = false; };
  }, [revision]);

  const loadProducts = useCallback(() => {
    if (!user) return () => {};
    let live = true;
    const t = setTimeout(() => {
      setLoading(true);
      getProducts(keyword, page, 10, undefined, undefined, 'createdAt', 'DESC', user.username)
        .then(r => { if (live) { setProducts(r.data.items); setPages(r.data.totalPages); } })
        .catch(e => { if (live) setError(errorText(e)); }).finally(() => { if (live) setLoading(false); });
    }, 250);
    return () => { live = false; clearTimeout(t); };
  }, [user, keyword, page]);
  useEffect(() => loadProducts(), [loadProducts, revision]);

  async function saveShop(values: ShopRequest) {
    setSaving(true); setFormError('');
    try {
      if (shopModal) await updateShop(shopModal.id, values); else await createShop(values);
      setShopModal(undefined); setMessage('Đã lưu gian hàng.'); refresh();
    } catch (e) { setFormError(errorText(e)); } finally { setSaving(false); }
  }

  async function saveProduct(values: CreateProductRequest | UpdateProductRequest, file?: File) {
    setSaving(true); setFormError('');
    try {
      const product = productModal ? (await updateProduct(productModal.id, values as UpdateProductRequest)).data
        : (await createProduct(values as CreateProductRequest)).data;
      if (file) {
        try { await uploadProductImage(product.id, file); }
        catch (e) { setProductModal(product); setFormError('Sản phẩm đã lưu. Ảnh chưa tải được: ' + errorText(e)); refresh(); return; }
      }
      setProductModal(undefined); setMessage(productModal ? 'Đã lưu sản phẩm.' : 'Đã đăng sản phẩm. Hãy nhập kho để bắt đầu bán.'); refresh();
    } catch (e) { setFormError(errorText(e)); } finally { setSaving(false); }
  }

  /** Nhập/giảm kho; nếu SKU chưa có bản ghi kho thì tạo mới với số lượng nhập. */
  async function changeStock(p: Product) {
    const delta = stockDelta[p.id] ?? 0;
    if (!delta) return;
    setError(''); setMessage('');
    try {
      try { await adjustInventory(p.sku, { quantityDelta: delta }); }
      catch (e) {
        if (!(axios.isAxiosError(e) && e.response?.status === 404)) throw e;
        if (delta < 0) throw new Error('Sản phẩm chưa có tồn kho để giảm.', { cause: e });
        await createInventoryItem({ productId: p.id, sku: p.sku, onHand: delta });
      }
      setStockDelta(s => ({ ...s, [p.id]: 0 })); setMessage(`Đã cập nhật kho ${p.sku}.`); refresh();
    } catch (e) { setError(errorText(e)); }
  }

  return <div>
    <div className="page-heading"><div><p className="eyebrow">KÊNH NGƯỜI BÁN</p><h1 className="page-title">Xin chào, {user?.username}</h1>
      <p className="page-subtitle">Gian hàng, sản phẩm và tồn kho của riêng bạn.</p></div>
      <div className="inline-actions">
        <button className="btn btn-ghost" onClick={() => { setFormError(''); setShopModal(null); }}>+ Gian hàng</button>
        <button className="btn btn-primary" disabled={!shops.length} title={shops.length ? '' : 'Tạo gian hàng trước'} onClick={() => { setFormError(''); setProductModal(null); }}>+ Đăng sản phẩm</button>
      </div></div>
    {message && <div className="alert alert-success" role="status">{message}</div>}
    {error && <div className="alert alert-error" role="alert">{error}</div>}
    <div className="category-chips">
      <button className={`chip ${tab === 'shops' ? 'selected' : ''}`} onClick={() => setTab('shops')}>Gian hàng ({shops.length})</button>
      <button className={`chip ${tab === 'products' ? 'selected' : ''}`} onClick={() => setTab('products')}>Sản phẩm của tôi</button>
    </div>

    {tab === 'shops' && <section className="card">
      {!shops.length ? <div className="empty-catalog"><h3>Bạn chưa có gian hàng</h3><p>Tạo gian hàng đầu tiên để có thể đăng sản phẩm.</p>
        <button className="btn btn-primary" onClick={() => { setFormError(''); setShopModal(null); }}>Tạo gian hàng</button></div>
      : <div className="table-wrap"><table className="table"><thead><tr><th>Gian hàng</th><th>Liên hệ</th><th>Địa chỉ</th><th /></tr></thead><tbody>
        {shops.map(s => <tr key={s.id}><td><strong>{s.name}</strong><div className="muted">@{s.ownerUsername}</div></td><td>{s.phone}<div className="muted">{s.email}</div></td><td>{s.address}</td>
          <td><button className="btn btn-ghost btn-sm" onClick={() => { setFormError(''); setShopModal(s); }}>Sửa</button></td></tr>)}
      </tbody></table></div>}
    </section>}

    {tab === 'products' && <section className="card">
      <div className="toolbar"><input className="form-control" aria-label="Tìm sản phẩm của tôi" placeholder="Tìm theo tên hoặc SKU…" value={keyword} onChange={e => { setKeyword(e.target.value); setPage(0); }} /></div>
      {loading ? <div className="state-center">Đang tải sản phẩm…</div> : <div className="table-wrap"><table className="table">
        <thead><tr><th>Sản phẩm</th><th>Gian hàng</th><th>Giá</th><th>Trạng thái</th><th>Nhập / giảm kho</th><th /></tr></thead><tbody>
        {products.map(p => <tr key={p.id}>
          <td><div className="product-cell">{p.imageUrl ? <img src={p.imageUrl} alt={p.name} /> : <div className="image-placeholder small">T</div>}
            <div><strong>{p.name}</strong> <StockBadge sku={p.sku} refresh={revision} /><div className="muted">{p.sku}</div></div></div></td>
          <td>{p.shopName}</td><td>{money(p.price, p.currency)}</td><td><span className="badge badge-neutral">{p.status}</span></td>
          <td><div className="inline-actions"><input className="form-control" style={{ width: 90 }} type="number" step={1} aria-label={'Số lượng kho ' + p.sku}
            value={stockDelta[p.id] ?? 0} onChange={e => setStockDelta(s => ({ ...s, [p.id]: Math.trunc(Number(e.target.value)) }))} />
            <button className="btn btn-ghost btn-sm" disabled={!stockDelta[p.id]} onClick={() => void changeStock(p)}>Cập nhật</button></div></td>
          <td><button className="btn btn-ghost btn-sm" disabled={p.status === 'ARCHIVED'} onClick={() => { setFormError(''); setProductModal(p); }}>Sửa</button></td>
        </tr>)}
        {!products.length && <tr><td colSpan={6}>Bạn chưa đăng sản phẩm nào.</td></tr>}
      </tbody></table></div>}
      <p className="form-hint">Số dương để nhập thêm, số âm để giảm. Lần nhập đầu tiên sẽ tạo bản ghi tồn kho cho sản phẩm.</p>
      <div className="pagination"><button className="btn btn-ghost" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Trước</button><span>{page + 1} / {Math.max(1, pages)}</span>
        <button className="btn btn-ghost" disabled={page + 1 >= pages} onClick={() => setPage(p => p + 1)}>Sau →</button></div>
    </section>}

    {shopModal !== undefined && <Modal title={shopModal ? 'Sửa gian hàng' : 'Tạo gian hàng'} onClose={() => { if (!saving) setShopModal(undefined); }}>
      <ShopForm shop={shopModal} onSubmit={saveShop} onCancel={() => setShopModal(undefined)} submitting={saving} error={formError} /></Modal>}
    {productModal !== undefined && <Modal title={productModal ? 'Chỉnh sửa sản phẩm' : 'Đăng sản phẩm'} onClose={() => { if (!saving) setProductModal(undefined); }}>
      <ProductForm editingProduct={productModal} shops={shops} canCreateCategory={false} onSubmit={saveProduct}
        onCancel={() => setProductModal(undefined)} submitting={saving} serverError={formError} /></Modal>}
  </div>;
}
