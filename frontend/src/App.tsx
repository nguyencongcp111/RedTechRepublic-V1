import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import MainLayout from './components/layout/MainLayout';
import ShopLayout from './components/layout/ShopLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ProductsPage from './pages/ProductsPage';
import InventoryPage from './pages/InventoryPage';
import OrdersPage from './pages/OrdersPage';
import PaymentsPage from './pages/PaymentsPage';
import ShopPage from './pages/shop/ShopPage';
import CartPage from './pages/shop/CartPage';
import OrderHistoryPage from './pages/shop/OrderHistoryPage';
import PaymentHistoryPage from './pages/shop/PaymentHistoryPage';

/** Admin-only route guard */
function AdminRoute({ children }: { children: React.ReactNode }) {
  const { user, isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role !== 'ADMIN') return <Navigate to="/shop" replace />;
  return <>{children}</>;
}

/** Cart route: must be authenticated to checkout, admin is redirected to dashboard */
function CartRoute({ children }: { children: React.ReactNode }) {
  const { user, isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" state={{ from: '/shop/cart' }} replace />;
  if (user?.role === 'ADMIN') return <Navigate to="/dashboard" replace />;
  return <>{children}</>;
}

/** Smart root route: Admin lands on /dashboard, guests and customers land on /shop */
function RootRoute() {
  const { user, isAuthenticated } = useAuth();
  if (isAuthenticated && user?.role === 'ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }
  return <Navigate to="/shop" replace />;
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <Routes>
            {/* Public – default landing is /shop for guests/users, /dashboard for admin */}
            <Route path="/" element={<RootRoute />} />
            <Route path="/login" element={<LoginPage />} />

            {/* /shop – public: anyone can browse, login required to add to cart */}
            <Route path="/shop" element={
              <ShopLayout><ShopPage /></ShopLayout>
            } />

            {/* /shop/cart – requires login to proceed checkout */}
            <Route path="/shop/cart" element={
              <CartRoute>
                <ShopLayout><CartPage /></ShopLayout>
              </CartRoute>
            } />

            {/* /shop/orders – user order history */}
            <Route path="/shop/orders" element={
              <CartRoute>
                <ShopLayout><OrderHistoryPage /></ShopLayout>
              </CartRoute>
            } />

            {/* /shop/payments – user payment history */}
            <Route path="/shop/payments" element={
              <CartRoute>
                <ShopLayout><PaymentHistoryPage /></ShopLayout>
              </CartRoute>
            } />

            {/* ── ADMIN routes ── */}
            <Route path="/dashboard" element={
              <AdminRoute><MainLayout><DashboardPage /></MainLayout></AdminRoute>
            } />
            <Route path="/products" element={
              <AdminRoute><MainLayout><ProductsPage /></MainLayout></AdminRoute>
            } />
            <Route path="/inventory" element={
              <AdminRoute><MainLayout><InventoryPage /></MainLayout></AdminRoute>
            } />
            <Route path="/orders" element={
              <AdminRoute><MainLayout><OrdersPage /></MainLayout></AdminRoute>
            } />
            <Route path="/payments" element={
              <AdminRoute><MainLayout><PaymentsPage /></MainLayout></AdminRoute>
            } />

            {/* Catch-all */}
            <Route path="*" element={<RootRoute />} />
          </Routes>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;