import { useState, useEffect } from 'react';
import { allCategories, createCategory } from '../api/categoryApi';
import { errorText } from '../api/common';
import { emptyProductForm } from '../types/product';
import type { Product, CreateProductRequest, UpdateProductRequest, ProductStatus } from '../types/product';
import type { Category } from '../types/category';

export default function ProductForm({ editingProduct, onSubmit, onCancel, submitting, serverError }: {
  editingProduct: Product | null;
  onSubmit: (values: CreateProductRequest | UpdateProductRequest, file?: File) => Promise<void>;
  onCancel: () => void; submitting: boolean; serverError: string | null;
}) {
  const [values, setValues] = useState(() => editingProduct ? {
    sku: editingProduct.sku, name: editingProduct.name, description: editingProduct.description ?? '',
    price: editingProduct.price, currency: editingProduct.currency, status: editingProduct.status, categoryId: editingProduct.category?.id ?? ''
  } : emptyProductForm);
  const [categories, setCategories] = useState<Category[]>([]);
  const [categoryName, setCategoryName] = useState('');
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [addingCategory, setAddingCategory] = useState(false);
  const [error, setError] = useState('');
  const [file, setFile] = useState<File>();
  const [preview, setPreview] = useState(editingProduct?.imageUrl ?? '');
  useEffect(() => {
    let live = true;
    allCategories().then(c => { if (live) setCategories(c); }).catch(e => { if (live) setError(errorText(e)); })
      .finally(() => { if (live) setLoadingCategories(false); });
    return () => { live = false; };
  }, []);
  useEffect(() => () => { if (preview.startsWith('blob:')) URL.revokeObjectURL(preview); }, [preview]);
  async function addCategory() {
    if (!categoryName.trim()) return;
    setAddingCategory(true); setError('');
    try {
      const category = (await createCategory({ name: categoryName.trim() })).data;
      setCategories(previous => [...previous, category]); setValues(v => ({ ...v, categoryId: category.id })); setCategoryName('');
    } catch (e) { setError(errorText(e)); } finally { setAddingCategory(false); }
  }
  async function submit(e: React.FormEvent) {
    e.preventDefault(); setError('');
    if (!values.categoryId) { setError('Vui lòng chọn danh mục.'); return; }
    if (!Number.isSafeInteger(values.price) || values.price < 0) { setError('Giá phải là số nguyên không âm.'); return; }
    const common = { name: values.name.trim(), description: values.description.trim(), price: values.price, categoryId: values.categoryId };
    await onSubmit(editingProduct ? { ...common, status: values.status } : {
      ...common, sku: values.sku.trim(), currency: values.currency.toUpperCase()
    }, file);
  }
  return <form onSubmit={submit}>
    {(error || serverError) && <div className="alert alert-error" role="alert">{error || serverError}</div>}
    <div className="form-grid">
      <label className="form-group">Mã SKU<input className="form-control" value={values.sku} required disabled={!!editingProduct}
        pattern="[A-Za-z0-9][A-Za-z0-9_-]*" maxLength={64} onChange={e => setValues({ ...values, sku: e.target.value })} /></label>
      <label className="form-group">Tên sản phẩm<input className="form-control" value={values.name} required maxLength={255}
        onChange={e => setValues({ ...values, name: e.target.value })} /></label>
      <label className="form-group">Giá<input className="form-control" type="number" min="0" step="1" required value={values.price}
        onChange={e => setValues({ ...values, price: Number(e.target.value) })} /></label>
      <label className="form-group">Tiền tệ<input className="form-control" value={values.currency} disabled={!!editingProduct} required pattern="[A-Za-z]{3}" maxLength={3}
        onChange={e => setValues({ ...values, currency: e.target.value })} /></label>
    </div>
    <label className="form-group">Danh mục<select className="form-control" required value={values.categoryId} disabled={loadingCategories}
      onChange={e => setValues({ ...values, categoryId: e.target.value })}>
      <option value="">{loadingCategories ? 'Đang tải danh mục…' : 'Chọn danh mục'}</option>
      {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
    </select></label>
    <div className="inline-actions"><input className="form-control" placeholder="Tên danh mục mới" aria-label="Tên danh mục mới" value={categoryName} maxLength={255}
      onChange={e => setCategoryName(e.target.value)} /><button type="button" className="btn btn-ghost" disabled={addingCategory || !categoryName.trim()} onClick={addCategory}>+ Tạo danh mục</button></div>
    <label className="form-group">Mô tả<textarea className="form-control" rows={3} value={values.description} maxLength={editingProduct ? 2000 : 1500}
      onChange={e => setValues({ ...values, description: e.target.value })} /></label>
    {editingProduct && <label className="form-group">Trạng thái<select className="form-control" value={values.status}
      onChange={e => setValues({ ...values, status: e.target.value as ProductStatus })}>
      <option value="ACTIVE">Đang bán</option><option value="INACTIVE">Tạm ngừng</option><option value="ARCHIVED">Lưu trữ</option>
    </select></label>}
    <label className="form-group">Ảnh sản phẩm<input className="form-control" type="file" accept="image/jpeg,image/png,image/webp"
      onChange={e => { const selected = e.target.files?.[0]; if (selected && selected.size > 5 * 1024 * 1024) { setError('Ảnh tối đa 5 MB.'); e.target.value = ''; return; } setFile(selected); setPreview(selected ? URL.createObjectURL(selected) : editingProduct?.imageUrl ?? ''); }} /></label>
    <p className="form-hint">JPEG, PNG hoặc WebP, tối đa 5 MB. Ảnh sẽ được tải lên khi lưu sản phẩm.</p>
    {preview && <img className="image-preview" src={preview} alt="Xem trước ảnh sản phẩm" />}
    <div className="modal-footer"><button type="button" className="btn btn-ghost" onClick={onCancel} disabled={submitting}>Hủy</button>
      <button className="btn btn-primary" disabled={submitting || loadingCategories}>{submitting ? 'Đang lưu…' : 'Lưu sản phẩm'}</button></div>
  </form>;
}
