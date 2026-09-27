import Sidebar from './Sidebar';
import './MainLayout.css';

export default function MainLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="main-layout">
      <Sidebar />
      <main className="main-content">
        {children}
      </main>
    </div>
  );
}
