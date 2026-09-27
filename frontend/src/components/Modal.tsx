import { useEffect, useRef } from 'react';
export default function Modal({ title, children, onClose }: { title: string; children: React.ReactNode; onClose: () => void }) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => { const dialog = ref.current; dialog?.showModal(); return () => dialog?.close(); }, []);
  return <dialog ref={ref} className="app-dialog" onCancel={e => { e.preventDefault(); onClose(); }}>
    <div className="modal-header"><h2 className="modal-title">{title}</h2><button className="modal-close" aria-label="Đóng" onClick={onClose}>×</button></div>
    <div className="modal-body">{children}</div>
  </dialog>;
}
