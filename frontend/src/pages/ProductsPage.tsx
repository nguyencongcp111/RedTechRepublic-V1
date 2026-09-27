import { useEffect, useState } from 'react';
import { getProducts, createProduct, updateProduct, uploadProductImage, removeProductImage, getProductBySku } from '../api/productApi';
import { allCategories, createCategory } from '../api/categoryApi';
import { errorText, money } from '../api/common';
import type { Product, CreateProductRequest, UpdateProductRequest } from '../types/product';
import type { Category } from '../types/category';
import ProductForm from '../components/ProductForm';
import Modal from '../components/Modal';
import StockBadge from '../components/StockBadge';
export default function ProductsPage() {
  const [products,setProducts] = useState<Product[]>([]), [categories,setCategories] = useState<Category[]>([]);
  const [keyword,setKeyword] = useState(''), [category,setCategory] = useState(''), [page,setPage] = useState(0);
  const [pages,setPages] = useState(0), [revision,setRevision] = useState(0);
  const [loading,setLoading] = useState(true), [error,setError] = useState(''), [message,setMessage] = useState('');
  const [opened,setOpened] = useState(false), [editing,setEditing] = useState<Product|null>(null);
  const [saving,setSaving] = useState(false), [formError,setFormError] = useState('');
  const [categoryModal,setCategoryModal] = useState(false), [newCategory,setNewCategory] = useState('');
  const [categoryDescription,setCategoryDescription] = useState('');
  useEffect(() => { let live = true; allCategories().then(c => { if(live) setCategories(c); }).catch(e => { if(live) setError(errorText(e)); }); return () => { live=false; }; },[revision]);
  useEffect(() => {
    let live=true;
    const timer=setTimeout(() => { setLoading(true); setError('');
      getProducts(keyword,page,12,category).then(r => { if(live) { setProducts(r.data.items); setPages(r.data.totalPages); } })
        .catch(e => { if(live) setError(errorText(e)); }).finally(() => { if(live) setLoading(false); });
    },250);
    return () => { live=false; clearTimeout(timer); };
  },[keyword,page,category,revision]);
  const refresh=() => setRevision(r=>r+1);
  function open(product:Product|null) { setEditing(product); setFormError(''); setOpened(true); }
  async function save(values:CreateProductRequest|UpdateProductRequest,file?:File) {
    setSaving(true); setFormError('');
    try {
      const product = editing ? (await updateProduct(editing.id, values as UpdateProductRequest)).data : (await createProduct(values as CreateProductRequest)).data;
      setEditing(product);
      if(file) {
        try { await uploadProductImage(product.id,file); }
        catch(e) { setFormError('Sản phẩm đã lưu. Ảnh chưa tải được: '+errorText(e)); refresh(); return; }
      }
      setOpened(false); refresh(); setMessage('Đã lưu sản phẩm.');
    } catch(e) { setFormError(errorText(e)); } finally { setSaving(false); }
  }
  async function removeImage(product:Product) {
    if(!window.confirm('Gỡ ảnh đang hiển thị của sản phẩm này?')) return;
    try { await removeProductImage(product.id); refresh(); } catch(e) { setError(errorText(e)); }
  }
  async function lookupSku() {
    try { const product=(await getProductBySku(keyword.trim())).data; open(product); } catch(e) { setError(errorText(e)); }
  }
  return <div>
    <div className="page-heading"><div><p className="eyebrow">CATALOG / QUẢN TRỊ</p><h1 className="page-title">Sản phẩm</h1><p className="page-subtitle">Danh mục, hình ảnh và số lượng sẵn sàng bán.</p></div>
      <div className="inline-actions"><button className="btn btn-ghost" onClick={()=>setCategoryModal(true)}>Danh mục</button><button className="btn btn-primary" onClick={()=>open(null)}>+ Thêm sản phẩm</button></div></div>
    {message && <div className="alert alert-success" role="status">{message}</div>}
    {error && <div className="alert alert-error" role="alert">{error}<button className="btn btn-ghost btn-sm" onClick={refresh}>Thử lại</button></div>}
    <div className="card"><div className="toolbar">
      <input className="form-control" aria-label="Tìm sản phẩm" placeholder="Tìm theo tên hoặc SKU…" value={keyword} onChange={e=>{setKeyword(e.target.value);setPage(0);}}/>
      <button className="btn btn-ghost" disabled={!keyword.trim()} onClick={lookupSku}>Tra SKU chính xác</button>
      <select className="form-control" aria-label="Lọc danh mục" value={category} onChange={e=>{setCategory(e.target.value);setPage(0);}}>
        <option value="">Tất cả danh mục</option>{categories.map(c=><option key={c.id} value={c.id}>{c.name}</option>)}</select>
    </div>
    {loading ? <div className="state-center">Đang tải sản phẩm…</div> : <div className="table-wrap"><table className="table"><thead><tr><th>Sản phẩm / tồn khả dụng</th><th>Danh mục</th><th>Giá</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
      {products.map(p=><tr key={p.id}><td><div className="product-cell">{p.imageUrl ? <img src={p.imageUrl} alt={p.name}/> : <div className="image-placeholder small">T</div>}
        <div><strong>{p.name}</strong> <StockBadge sku={p.sku} refresh={revision}/><div className="muted">{p.sku}</div><small className="mono">{p.id}</small></div></div></td>
        <td>{p.category?.name ?? 'Chưa phân loại'}</td><td>{money(p.price,p.currency)}</td><td><span className="badge badge-neutral">{p.status}</span></td>
        <td><div className="inline-actions"><button className="btn btn-ghost btn-sm" onClick={()=>open(p)} disabled={p.status==='ARCHIVED'}>Chỉnh sửa</button>{p.imageUrl&&<button className="btn btn-ghost btn-sm" onClick={()=>removeImage(p)}>Gỡ ảnh</button>}</div></td></tr>)}
      {!products.length&&<tr><td colSpan={5}>Chưa có sản phẩm phù hợp.</td></tr>}
    </tbody></table></div>}
    <div className="pagination"><button className="btn btn-ghost" disabled={page===0} onClick={()=>setPage(p=>p-1)}>← Trước</button><span>{page+1} / {Math.max(1,pages)}</span><button className="btn btn-ghost" disabled={page+1>=pages} onClick={()=>setPage(p=>p+1)}>Sau →</button></div></div>
    {opened&&<Modal title={editing?'Chỉnh sửa sản phẩm':'Thêm sản phẩm'} onClose={()=>{if(!saving)setOpened(false);}}>
      <ProductForm editingProduct={editing} onSubmit={save} onCancel={()=>setOpened(false)} submitting={saving} serverError={formError}/>
    </Modal>}
    {categoryModal&&<Modal title="Danh mục sản phẩm" onClose={()=>setCategoryModal(false)}>
      <form onSubmit={async e=>{e.preventDefault();setSaving(true);setFormError('');try{await createCategory({name:newCategory.trim(),description:categoryDescription.trim()});setNewCategory('');setCategoryDescription('');refresh();}catch(err){setFormError(errorText(err));}finally{setSaving(false);}}}>
        {formError&&<div className="alert alert-error">{formError}</div>}
        <label className="form-group">Tên danh mục<input className="form-control" required maxLength={255} value={newCategory} onChange={e=>setNewCategory(e.target.value)}/></label>
        <label className="form-group">Mô tả<textarea className="form-control" maxLength={1500} value={categoryDescription} onChange={e=>setCategoryDescription(e.target.value)}/></label>
        <button className="btn btn-primary" disabled={saving}>Tạo danh mục</button>
      </form><div className="category-chips">{categories.map(c=><span className="chip" key={c.id}>{c.name}</span>)}</div>
    </Modal>}
  </div>;
}
