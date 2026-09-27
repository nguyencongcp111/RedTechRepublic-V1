import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import { createOrder } from '../../api/orderApi';
import { requestKey, finishRequest } from '../../api/idempotency';
import { errorText, money } from '../../api/common';
import type { PaymentMethod } from '../../types/payment';
import type { Order } from '../../types/order';
const methods:Record<PaymentMethod,string>={CARD:'Thẻ',BANK_TRANSFER:'Chuyển khoản ngân hàng',E_WALLET:'Ví điện tử',CASH_ON_DELIVERY:'Thanh toán khi nhận hàng'};
export default function CartPage(){
  const {items,removeItem,updateQty,clearCart,totalAmount,currency}=useCart(),{user}=useAuth();
  const [method,setMethod]=useState<PaymentMethod>('CASH_ON_DELIVERY'),[busy,setBusy]=useState(false),[error,setError]=useState(''),[created,setCreated]=useState<Order|null>(null);
  async function checkout(){
    if(!user||!items.length)return;
    setBusy(true);setError('');
    const payload={userId:user.userId,currency,paymentMethod:method,items:items.map(i=>({productId:i.product.id,quantity:i.quantity}))};
    try{const order=(await createOrder(requestKey('checkout',payload),payload)).data;setCreated(order);finishRequest('checkout');clearCart();}
    catch(e){setError(errorText(e));}finally{setBusy(false);}
  }
  if(created)return <section className="checkout-success card"><span className="success-orbit">✓</span><p className="eyebrow">ĐÃ TIẾP NHẬN ĐƠN HÀNG</p><h1>Cảm ơn bạn đã chọn TechShop.</h1><p>Đơn đã được tạo. Thanh toán sẽ được hệ thống khởi tạo và chờ xác nhận.</p><p className="mono">{created.id}</p><h2>{money(created.totalAmount,created.currency)}</h2><div className="inline-actions"><Link className="btn btn-primary" to="/shop/orders">Theo dõi đơn hàng →</Link><Link className="btn btn-ghost" to="/shop">Tiếp tục mua sắm</Link></div></section>;
  return <div><p className="eyebrow">GIỎ HÀNG CỦA BẠN</p><h1 className="page-title">Sẵn sàng cho điều mới?</h1><p className="page-subtitle">Kiểm tra sản phẩm và chọn phương thức thanh toán.</p>
    {error&&<div className="alert alert-error" role="alert">{error}</div>}
    {!items.length?<div className="card empty-catalog"><h2>Giỏ hàng đang trống</h2><Link className="btn btn-primary" to="/shop">Khám phá sản phẩm</Link></div>:<div className="checkout-grid"><div className="card">
      {items.map(({product:p,quantity})=><div className="cart-line" key={p.id}>{p.imageUrl?<img src={p.imageUrl} alt={p.name}/>:<div className="image-placeholder small">T.</div>}<div className="cart-line-info"><h3>{p.name}</h3><p className="muted">{p.sku}</p><strong>{money(p.price,p.currency)}</strong></div>
        <label>Số lượng<input className="form-control" aria-label={'Số lượng '+p.name} disabled={busy} type="number" min={1} step={1} value={quantity} onChange={e=>updateQty(p.id,Math.max(1,Math.floor(Number(e.target.value)||1)))}/></label>
        <button className="btn btn-ghost btn-sm" disabled={busy} onClick={()=>removeItem(p.id)}>Xóa</button></div>)}
      <Link className="text-button" to="/shop">← Tiếp tục mua sắm</Link>
    </div><aside className="card checkout-summary"><h2 className="card-title">Thông tin đặt hàng</h2><label className="form-group">Phương thức thanh toán<select className="form-control" value={method} disabled={busy} onChange={e=>setMethod(e.target.value as PaymentMethod)}>{Object.entries(methods).map(([value,label])=><option key={value} value={value}>{label}</option>)}</select></label>
      <div className="stock-summary"><span>Tạm tính</span><strong>{money(totalAmount,currency)}</strong></div><p className="muted">Giá và tồn kho được xác nhận lại khi đặt đơn. Chọn phương thức không đồng nghĩa đã thanh toán.</p>
      <button className="btn btn-primary btn-full" disabled={busy} onClick={checkout}>{busy?'Đang tạo đơn…':'Đặt hàng →'}</button></aside></div>}
  </div>;
}
