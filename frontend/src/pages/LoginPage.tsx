import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosClient';
import { errorText } from '../api/common';
import './LoginPage.css';
export default function LoginPage() {
  const { login }=useAuth(); const navigate=useNavigate(); const location=useLocation();
  const [register,setRegister]=useState(false), [username,setUsername]=useState(''), [password,setPassword]=useState('');
  const [confirmation,setConfirmation]=useState(''), [visible,setVisible]=useState(false), [busy,setBusy]=useState(false), [error,setError]=useState('');
  async function submit(e:React.FormEvent) {
    e.preventDefault(); setError('');
    if(register&&password!==confirmation){setError('Mật khẩu xác nhận chưa khớp.');return;}
    setBusy(true);
    try {
      if(register) await api.post('/api/auth/register',{username:username.trim(),password});
      const user=await login({username:username.trim(),password});
      const from=(location.state as {from?:string}|null)?.from;
      navigate(user.role==='ADMIN'?'/dashboard':from?.startsWith('/shop')?from:'/shop',{replace:true});
    }catch(e){setError(errorText(e));}finally{setBusy(false);}
  }
  return <main className="login-page">
    <div className="login-triangles" aria-hidden="true"><i/><i/><i/><i/></div>
    <Link className="login-wordmark" to="/shop"><span className="brand-mark">T</span> TechShop<span className="wordmark-dot">.</span></Link>
    <section className="login-story"><p className="eyebrow">CÔNG NGHỆ CHO MỖI NGÀY</p><h1>Kết nối<br/>điều bạn <em>yêu.</em></h1>
      <p>Một nơi để khám phá, lựa chọn và theo dõi những sản phẩm dành cho bạn.</p>
      <Link to="/shop" className="login-back">Khám phá cửa hàng <span>↗</span></Link>
    </section>
    <section className="login-form-wrap">
      <div className="login-tabs"><button className={!register?'selected':''} onClick={()=>{setRegister(false);setError('');}}>Đăng nhập</button>
        <button className={register?'selected':''} onClick={()=>{setRegister(true);setError('');}}>Tạo tài khoản</button></div>
      <h2>{register?'Bắt đầu cùng TechShop':'Chào mừng trở lại.'}</h2><p>{register?'Tạo tài khoản để mua sắm và theo dõi đơn hàng.':'Đăng nhập để tiếp tục trải nghiệm của bạn.'}</p>
      {error&&<div role="alert" className="alert alert-error">{error}</div>}
      <form onSubmit={submit}>
        <label className="form-group">Tên đăng nhập<input className="form-control" autoComplete="username" required
          pattern={register?'[A-Za-z0-9_.-]{3,64}':undefined} maxLength={64} value={username} onChange={e=>setUsername(e.target.value)} placeholder="Tên tài khoản của bạn"/></label>
        <label className="form-group">Mật khẩu<div className="password-wrap"><input className="form-control" required type={visible?'text':'password'}
          autoComplete={register?'new-password':'current-password'} minLength={register?8:undefined} maxLength={72} value={password} onChange={e=>setPassword(e.target.value)} placeholder={register?'Tối thiểu 8 ký tự':'Nhập mật khẩu'}/>
          <button type="button" className="password-toggle" aria-label={visible?'Ẩn mật khẩu':'Hiện mật khẩu'} onClick={()=>setVisible(v=>!v)}>{visible?'Ẩn':'Hiện'}</button></div></label>
        {register&&<label className="form-group">Xác nhận mật khẩu<input className="form-control" type="password" required autoComplete="new-password" value={confirmation} onChange={e=>setConfirmation(e.target.value)}/></label>}
        <button className="btn login-submit" disabled={busy}>{busy?'Đang xử lý…':register?'Tạo tài khoản':'Đăng nhập'} <span>→</span></button>
      </form>
      <p className="login-footnote">Mua sắm theo cách của bạn. Quản lý trong một nơi.</p>
    </section><small className="login-copyright">© 2026 TechShop</small>
  </main>;
}
