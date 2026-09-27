import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import './ShopLayout.css';

export default function ShopLayout({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth();
  const { totalItems } = useCart();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="shop-layout">
      {/* Top navbar */}
      <header className="shop-navbar">
        <div className="shop-navbar-inner">
          {/* Logo */}
          <Link to="/shop" className="shop-logo">
            <span className="brand-mark">T</span>
            <span>TechShop</span>
          </Link>

          {/* Nav links */}
          <nav className="shop-nav">
            <Link to="/shop" className="shop-nav-link">Sản phẩm</Link>
            {user && user.role !== 'ADMIN' && (
              <>
                <Link to="/shop/orders" className="shop-nav-link">📦 Đơn hàng</Link>
                <Link to="/shop/payments" className="shop-nav-link">🧾 Thanh toán</Link>
              </>
            )}
          </nav>

          {/* Right actions */}
          <div className="shop-navbar-right">
            {/* Cart button */}
            <Link to="/shop/cart" className="shop-cart-btn">
              <span className="shop-cart-icon">🛒</span>
              <span>Giỏ hàng</span>
              {totalItems > 0 && (
                <span className="shop-cart-badge">{totalItems}</span>
              )}
            </Link>

            {/* Guest: show login button | Authenticated: show user chip + logout */}
            {user ? (
              <>
                {user.role === 'ADMIN' && (
                  <Link
                    to="/dashboard"
                    className="btn btn-warning btn-sm"
                    style={{ fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: 4 }}
                  >
                    ⚙️ Trang quản trị
                  </Link>
                )}
                <div className="shop-user-chip">
                  <span className="shop-user-avatar">
                    {user.username.charAt(0).toUpperCase()}
                  </span>
                  <span className="shop-user-name">{user.username}</span>
                </div>
                <button className="btn btn-ghost btn-sm" onClick={handleLogout}>
                  Đăng xuất
                </button>
              </>
            ) : (
              <Link to="/login" className="btn btn-primary btn-sm">
                Đăng nhập
              </Link>
            )}
          </div>
        </div>
      </header>

      {/* Main content */}
      <main className="shop-main">
        {children}
      </main>

      {/* Footer */}
      <footer className="shop-footer">
        <span>© 2026 TechShop – E-Commerce Microservices Demo</span>
      </footer>
    </div>
  );
}
