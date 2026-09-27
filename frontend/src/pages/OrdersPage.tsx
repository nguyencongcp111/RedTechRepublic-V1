import { useEffect, useState } from 'react';
import api from '../api/axiosClient';
import { getOrder, getOrdersByUser, createOrder } from '../api/orderApi';
import { errorText, money } from '../api/common';
import { requestKey, finishRequest } from '../api/idempotency';
import type { Order } from '../types/order';
import type { ProductResponse } from '../types/product';
import type { PaymentMethod } from '../types/payment';
export default function OrdersPage(){
 const [orders,setOrders]=useState<Order[]>([]),[order,setOrder]=useState<Order|null>(null),[lookup,setLookup]=useState(''),[byUser,setByUser]=useState(false);
 const [page,setPage]=useState(0),[pages,setPages]=useState(0),[revision,setRevision]=useState(0),[error,setError]=useState(''),[busy,setBusy]=useState(false);
 const [productId,setProductId]=useState(''),[quantity,setQuantity]=useState(1);
 const [userId,setUserId]=useState(''),[currency,setCurrency]=useState('VND'),[method,setMethod]=useState<PaymentMethod>('CASH_ON_DELIVERY');
 const [createItems,setCreateItems]=useState([{productId:'',quantity:1}]);
 useEffect(()=>{let live=true;api.get<ProductResponse<Order>>('/api/orders',{params:{page,size:15}}).then(r=>{if(live){setOrders(r.data.items);setPages(r.data.totalPages);}}).catch(e=>{if(live)setError(errorText(e));});return()=>{live=false;};},[page,revision]);
 async function run(action:()=>Promise<void>){setBusy(true);setError('');try{await action();}catch(e){setError(errorText(e));}finally{setBusy(false);}}
 const refresh=()=>setRevision(r=>r+1);
 return <div><div className="page-heading"><div><p className="eyebrow">VẬN HÀNH / ĐƠN HÀNG</p><h1 className="page-title">Quản lý đơn hàng</h1></div><button className="btn btn-ghost" onClick={refresh}>Làm mới</button></div>
 {error&&<div className="alert alert-error" role="alert">{error}</div>}
 <div className="card"><form className="toolbar" onSubmit={e=>{e.preventDefault();void run(async()=>{if(byUser){setOrders((await getOrdersByUser(lookup.trim())).data);setPages(1);}else setOrder((await getOrder(lookup.trim())).data);});}}>
  <input className="form-control" required aria-label="Mã tra cứu" placeholder={byUser?'User ID':'Order ID'} value={lookup} onChange={e=>setLookup(e.target.value)}/>
  <label><input type="checkbox" checked={byUser} onChange={e=>setByUser(e.target.checked)}/> Theo người dùng</label><button className="btn btn-primary" disabled={busy}>Tra cứu</button></form>
 <div className="table-wrap"><table className="table"><thead><tr><th>Đơn hàng</th><th>Người dùng</th><th>Trạng thái</th><th>Tổng tiền</th><th/></tr></thead><tbody>{orders.map(o=><tr key={o.id}><td className="mono">{o.id}</td><td className="mono">{o.userId}</td><td><span className="badge badge-neutral">{o.status}</span></td><td>{money(o.totalAmount,o.currency)}</td><td><button className="btn btn-ghost btn-sm" onClick={()=>void run(async()=>setOrder((await getOrder(o.id)).data))}>Chi tiết</button></td></tr>)}</tbody></table></div>
 <div className="pagination"><button className="btn btn-ghost" disabled={!page} onClick={()=>setPage(p=>p-1)}>Trước</button><span>{page+1}/{Math.max(1,pages)}</span><button className="btn btn-ghost" disabled={page+1>=pages} onClick={()=>setPage(p=>p+1)}>Sau</button></div></div>
 {order&&<section className="card section-space"><div className="card-header"><h2 className="card-title">Chi tiết đơn</h2><span className="badge badge-neutral">{order.status}</span></div><p className="mono">{order.id}</p><p>Tổng số lượng: {order.totalQuantity} · {money(order.totalAmount,order.currency)}</p>
 <div className="table-wrap"><table className="table"><thead><tr><th>Sản phẩm</th><th>SKU</th><th>Số lượng</th><th>Đơn giá</th><th/></tr></thead><tbody>{order.items.map(i=><tr key={i.id}><td>{i.productName}</td><td>{i.sku}</td><td>{i.quantity}</td><td>{money(i.unitPrice,order.currency)}</td><td><button className="btn btn-ghost btn-sm" disabled={busy||order.status!=='PENDING'} onClick={()=>{if(window.confirm('Xóa sản phẩm này khỏi đơn?'))void run(async()=>{setOrder((await api.delete<Order>(`/api/orders/${order.id}/items/${i.id}`)).data);refresh();});}}>Xóa item</button></td></tr>)}</tbody></table></div>
 <p className="form-hint">Chỉ sửa item khi đơn PENDING. Đơn đã giữ hàng hoặc đang thanh toán sẽ khóa thao tác này.</p>
 <form className="toolbar" onSubmit={e=>{e.preventDefault();void run(async()=>{setOrder((await api.post<Order>(`/api/orders/${order.id}/items`,{productId,quantity})).data);setProductId('');refresh();});}}>
 <input className="form-control" required placeholder="Product ID cần thêm" aria-label="Product ID cần thêm" value={productId} onChange={e=>setProductId(e.target.value)}/>
 <input className="form-control" type="number" required min={1} step={1} aria-label="Số lượng cần thêm" value={quantity} onChange={e=>setQuantity(Number(e.target.value))}/>
 <button className="btn btn-primary" disabled={busy||order.status!=='PENDING'}>Thêm item</button></form></section>}
 <details className="card section-space"><summary>Tạo đơn hàng cho khách</summary><form onSubmit={e=>{e.preventDefault();void run(async()=>{const payload={userId,currency,paymentMethod:method,items:createItems};setOrder((await createOrder(requestKey('admin-order',payload),payload)).data);finishRequest('admin-order');refresh();});}}>
 <div className="form-grid"><label className="form-group">User ID<input className="form-control" required value={userId} onChange={e=>setUserId(e.target.value)}/></label><label className="form-group">Tiền tệ<input className="form-control" required pattern="[A-Za-z]{3}" value={currency} onChange={e=>setCurrency(e.target.value.toUpperCase())}/></label></div>
 {createItems.map((item,i)=><div className="toolbar" key={i}><input className="form-control" required placeholder="Product ID" aria-label={'Sản phẩm '+(i+1)} value={item.productId} onChange={e=>setCreateItems(items=>items.map((v,n)=>n===i?{...v,productId:e.target.value}:v))}/>
 <input className="form-control" required type="number" min={1} step={1} aria-label={'Số lượng '+(i+1)} value={item.quantity} onChange={e=>setCreateItems(items=>items.map((v,n)=>n===i?{...v,quantity:Number(e.target.value)}:v))}/><button type="button" className="btn btn-ghost" disabled={createItems.length===1} onClick={()=>setCreateItems(items=>items.filter((_,n)=>n!==i))}>Xóa</button></div>)}
 <div className="toolbar"><button type="button" className="btn btn-ghost" onClick={()=>setCreateItems(items=>[...items,{productId:'',quantity:1}])}>+ Dòng sản phẩm</button><select className="form-control" aria-label="Phương thức thanh toán" value={method} onChange={e=>setMethod(e.target.value as PaymentMethod)}>{['CARD','BANK_TRANSFER','E_WALLET','CASH_ON_DELIVERY'].map(v=><option key={v}>{v}</option>)}</select><button className="btn btn-primary" disabled={busy}>Tạo đơn</button></div></form></details>
 </div>;
}
