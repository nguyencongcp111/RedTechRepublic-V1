import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axiosClient';
import { money } from '../api/common';
type Stats={total:number;active?:number;categories?:number;available?:number;onHand?:number;reserved?:number;lowStock?:number;byStatus?:Record<string,number>;revenue?:{currency:string;amount:number}[]};
const services=[['products','Sản phẩm','/products'],['inventory','Kho hàng','/inventory'],['orders','Đơn hàng','/orders'],['payments','Thanh toán','/payments']];
export default function DashboardPage(){
  const [stats,setStats]=useState<Record<string,Stats>>({}),[errors,setErrors]=useState<string[]>([]),[loading,setLoading]=useState(true),[updated,setUpdated]=useState('');
  const load=useCallback(async()=>{
    setLoading(true);
    const results=await Promise.allSettled(services.map(([s])=>api.get<Stats>(`/api/${s}/stats`)));
    const next:Record<string,Stats>={}, failed:string[]=[];
    results.forEach((r,i)=>{if(r.status==='fulfilled')next[services[i][0]]=r.value.data;else failed.push(services[i][1]);});
    setStats(next);setErrors(failed);setUpdated(new Date().toLocaleTimeString('vi-VN'));setLoading(false);
  },[]);
  useEffect(()=>{const timer=setTimeout(()=>void load(),0);return()=>clearTimeout(timer);},[load]);
  const orderStates=stats.orders?.byStatus??{};
  return <div><div className="page-heading"><div><p className="eyebrow">TECHSHOP / TỔNG QUAN</p><h1 className="page-title">Một góc nhìn. Toàn bộ cửa hàng.</h1><p className="page-subtitle">Số liệu thực tế từ các dịch vụ · {updated?'Cập nhật lúc '+updated:'Đang kết nối'}</p></div><button className="btn btn-primary" disabled={loading} onClick={load}>{loading?'Đang tải…':'↻ Làm mới'}</button></div>
    {errors.length>0&&<div className="alert alert-error" role="alert">Chưa tải được: {errors.join(', ')}. Các số liệu này chưa khả dụng; hãy thử lại.</div>}
    <div className="metrics-grid">{services.map(([key,label,url],i)=><Link className="metric-card" to={url} key={key}><span className="metric-label">{label}<span>↗</span></span><strong>{stats[key]?.total?.toLocaleString('vi-VN')??'—'}</strong>
      <small>{i===0?`${stats.products?.active??'—'} đang bán · ${stats.products?.categories??'—'} danh mục`:i===1?`${stats.inventory?.available??'—'} sản phẩm khả dụng`:i===2?'Tất cả trạng thái':'Tất cả giao dịch'}</small></Link>)}</div>
    <div className="dashboard-grid"><section className="card"><div className="card-header"><h2 className="card-title">Trạng thái đơn hàng</h2><Link to="/orders">Xem đơn →</Link></div>
      {!stats.orders?<p className="muted">Chưa có số liệu.</p>:Object.entries(orderStates).map(([status,count])=><div className="status-meter" key={status}><div><span>{status}</span><strong>{count}</strong></div><meter min={0} max={Math.max(1,stats.orders.total)} value={count}>{count}</meter></div>)}
    </section><section className="card"><h2 className="card-title">Doanh thu đã thanh toán</h2><p className="muted">Chỉ tính giao dịch SUCCEEDED, tách riêng từng tiền tệ; không tính giao dịch đã hoàn tiền.</p>
      {!stats.payments?<p>Chưa có số liệu.</p>:stats.payments.revenue?.length?stats.payments.revenue.map(r=><div className="revenue-value" key={r.currency}>{money(r.amount,r.currency)}</div>):<p className="empty-note">Chưa có thanh toán thành công.</p>}
      <div className="stock-summary"><span>Sắp hết hàng (khả dụng ≤ 5)</span><strong>{stats.inventory?.lowStock??'—'} SKU</strong></div><Link className="btn btn-ghost" to="/inventory">Kiểm tra tồn kho →</Link>
    </section></div>
    <div className="quick-start"><div><p className="eyebrow">SẴN SÀNG CHO ĐƠN HÀNG TIẾP THEO</p><h2>Cập nhật sản phẩm. Giữ cửa hàng luôn mới.</h2></div><Link className="btn btn-primary" to="/products">Quản lý danh mục →</Link></div>
  </div>;
}
