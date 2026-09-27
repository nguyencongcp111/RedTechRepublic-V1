import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import api from '../../api/axiosClient';
import { getProducts, getProduct } from '../../api/productApi';
import { allCategories } from '../../api/categoryApi';
import { errorText, money } from '../../api/common';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import type { Product } from '../../types/product';
import type { Category } from '../../types/category';
import type { InventoryItem } from '../../types/inventory';
import Modal from '../../components/Modal';
export default function ShopPage(){
  const {addItem,items}=useCart(),{isAuthenticated,user}=useAuth(),navigate=useNavigate();
  const [products,setProducts]=useState<Product[]>([]),[categories,setCategories]=useState<Category[]>([]);
  const [keyword,setKeyword]=useState(''),[category,setCategory]=useState(''),[sort,setSort]=useState('new');
  const [page,setPage]=useState(0),[pages,setPages]=useState(0),[total,setTotal]=useState(0),[revision,setRevision]=useState(0);
  const [loading,setLoading]=useState(true),[error,setError]=useState(''),[notice,setNotice]=useState('');
  const [stock,setStock]=useState<Record<string,number|null>>({});
  const [detail,setDetail]=useState<Product|null>(null),[qty,setQty]=useState(1);
  useEffect(()=>{let live=true;allCategories().then(c=>{if(live)setCategories(c);}).catch(e=>{if(live)setError(errorText(e));});return()=>{live=false;};},[revision]);
  useEffect(()=>{
    let live=true;
    const timer=setTimeout(()=>{setLoading(true);setError('');
      getProducts(keyword,page,12,category,'ACTIVE',sort==='new'?'createdAt':'price',sort==='price-asc'?'ASC':'DESC')
        .then(r=>{if(live){setProducts(r.data.items);setPages(r.data.totalPages);setTotal(r.data.totalItems);}})
        .catch(e=>{if(live)setError(errorText(e));}).finally(()=>{if(live)setLoading(false);});
    },250);
    return()=>{live=false;clearTimeout(timer);};
  },[keyword,category,page,sort,revision]);
  useEffect(()=>{
    let live=true;
    Promise.all(products.map(async p=>{
      try{const item=(await api.get<InventoryItem>(`/api/inventory/${encodeURIComponent(p.sku)}`)).data;return [p.sku,item.onHand-item.reserved] as const;}
      catch(e){return [p.sku,axios.isAxiosError(e)&&e.response?.status===404?0:null] as const;}
    })).then(entries=>{if(live)setStock(Object.fromEntries(entries));});
    return()=>{live=false;};
  },[products]);
  const add=useCallback((p:Product,quantity=1)=>{
    if(!isAuthenticated){navigate('/login',{state:{from:'/shop'}});return;}
    if(user?.role==='ADMIN'){setNotice('Tài khoản quản trị dùng để quản lý cửa hàng. Hãy dùng tài khoản khách hàng để đặt đơn.');return;}
    const available=stock[p.sku],inCart=items.find(i=>i.product.id===p.id)?.quantity??0;
    if(available==null){setNotice('Chưa xác nhận được tồn kho. Hãy làm mới và thử lại.');return;}
    if(quantity+inCart>available){setNotice(`Chỉ còn ${available} sản phẩm, giỏ hàng đã có ${inCart}.`);return;}
    if(items.length&&items[0].product.currency!==p.currency){setNotice('Mỗi đơn hàng chỉ dùng một loại tiền tệ.');return;}
    addItem(p,quantity);setNotice('Đã thêm '+p.name+' vào giỏ hàng.');setDetail(null);
  },[isAuthenticated,navigate,user,stock,items,addItem]);
  async function show(p:Product){try{setDetail((await getProduct(p.id)).data);setQty(1);}catch(e){setError(errorText(e));}}
  const stockLabel=(sku:string)=>stock[sku]==null?'Chưa xác nhận tồn kho':stock[sku]===0?'Hết hàng':`Còn ${stock[sku]}`;
  return <div>
    <section className="shop-hero"><div><p className="eyebrow">CHÀO MỪNG ĐẾN TECHSHOP</p><h1>Công nghệ mới.<br/><span>Cảm hứng mỗi ngày.</span></h1><p>Khám phá sản phẩm, tìm đúng thứ bạn cần và theo dõi đơn hàng trong một nơi.</p><a className="btn btn-primary" href="#catalog">Khám phá sản phẩm <span>↗</span></a></div>
      <div className="hero-art" aria-hidden="true"><div className="art-ring"/><div className="art-device"><span>T.</span><div/></div><div className="art-chip">TECH / EVERYDAY</div><span className="art-spark">✦</span></div>
    </section>
    <div className="shop-perks"><span><strong>01</strong> Tìm theo danh mục</span><span><strong>02</strong> Xem số lượng khả dụng</span><span><strong>03</strong> Theo dõi đơn dễ dàng</span></div>
    <section id="catalog"><div className="page-heading"><div><p className="eyebrow">KHÁM PHÁ BỘ SƯU TẬP</p><h2 className="page-title">{categories.find(c=>c.id===category)?.name??'Dành cho nhịp sống của bạn'}</h2></div><span className="muted">{total} sản phẩm</span></div>
      <div className="category-chips"><button className={`chip ${!category?'selected':''}`} onClick={()=>{setCategory('');setPage(0);}}>Tất cả</button>{categories.map(c=><button className={`chip ${category===c.id?'selected':''}`} key={c.id} onClick={()=>{setCategory(c.id);setPage(0);}}>{c.name}</button>)}</div>
      <div className="catalog-toolbar"><input className="form-control" aria-label="Tìm sản phẩm" placeholder="Tìm tên sản phẩm hoặc SKU…" value={keyword} onChange={e=>{setKeyword(e.target.value);setPage(0);}}/>
        <select className="form-control" aria-label="Sắp xếp sản phẩm" value={sort} onChange={e=>{setSort(e.target.value);setPage(0);}}><option value="new">Mới nhất</option><option value="price-asc">Giá tăng dần</option><option value="price-desc">Giá giảm dần</option></select>
        <button className="btn btn-ghost" onClick={()=>setRevision(r=>r+1)}>Làm mới</button></div>
      {notice&&<div role="status" className="alert alert-success">{notice} <Link to="/shop/cart">Xem giỏ hàng →</Link><button className="modal-close" aria-label="Đóng thông báo" onClick={()=>setNotice('')}>×</button></div>}
      {error&&<div className="alert alert-error" role="alert">{error}</div>}
      {loading?<div className="state-center">Đang tải sản phẩm…</div>:!products.length?<div className="empty-catalog"><h3>Chưa tìm thấy sản phẩm</h3><p>Hãy thử từ khóa khác hoặc chọn tất cả danh mục.</p><button className="btn btn-ghost" onClick={()=>{setCategory('');setKeyword('');setPage(0);}}>Xóa bộ lọc</button></div>:
        <div className="product-grid">{products.map(p=><article className="store-product" key={p.id}>
          <button className="product-image-button" onClick={()=>show(p)} aria-label={'Xem '+p.name}>{p.imageUrl?<img src={p.imageUrl} alt={p.name} loading="lazy"/>:<div className="image-placeholder"><span>T.</span><small>TECHSHOP</small></div>}<span className="product-category-label">{p.category?.name??'Sản phẩm'}</span></button>
          <div className="store-product-body"><div className="product-title-row"><button className="text-button" onClick={()=>show(p)}><h3>{p.name}</h3></button><span className={`stock-pill ${stock[p.sku]===0?'sold-out':''}`}>{stockLabel(p.sku)}</span></div><p className="muted">{p.sku}</p>
            <div className="product-buy-row"><strong>{money(p.price,p.currency)}</strong><button className="add-cart-button" aria-label={'Thêm '+p.name+' vào giỏ'} disabled={stock[p.sku]==null||stock[p.sku]===0} onClick={()=>add(p)}>+</button></div></div>
        </article>)}</div>}
      <div className="pagination"><button className="btn btn-ghost" disabled={page===0} onClick={()=>setPage(p=>p-1)}>← Trước</button><span>{page+1} / {Math.max(1,pages)}</span><button className="btn btn-ghost" disabled={page+1>=pages} onClick={()=>setPage(p=>p+1)}>Sau →</button></div>
    </section>
    <section className="shop-bottom-banner"><div><p className="eyebrow">MỖI ĐƠN HÀNG, MỘT TRẢI NGHIỆM</p><h2>Giỏ hàng hôm nay.<br/>Cảm hứng cho ngày mai.</h2></div><Link className="btn btn-primary" to={isAuthenticated?'/shop/orders':'/login'}>{isAuthenticated?'Theo dõi đơn hàng':'Bắt đầu cùng TechShop'} →</Link></section>
    {detail&&<Modal title={detail.name} onClose={()=>setDetail(null)}><div className="detail-grid">{detail.imageUrl?<img className="detail-image" src={detail.imageUrl} alt={detail.name}/>:<div className="image-placeholder">T.</div>}
      <div><p className="eyebrow">{detail.category?.name}</p><h3>{money(detail.price,detail.currency)}</h3><p className="product-description">{detail.description||'Chưa có mô tả.'}</p><p className="muted">{detail.sku} · {stockLabel(detail.sku)}</p>
        <label className="form-group">Số lượng<input className="form-control" type="number" min={1} max={stock[detail.sku]??1} value={qty} onChange={e=>setQty(Math.max(1,Math.floor(Number(e.target.value)||1)))}/></label>
        <button className="btn btn-primary" disabled={!stock[detail.sku]} onClick={()=>add(detail,qty)}>Thêm vào giỏ →</button></div></div></Modal>}
  </div>;
}
