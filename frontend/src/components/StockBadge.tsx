import { useEffect, useState } from 'react';
import axios from 'axios';
import api from '../api/axiosClient';
import type { InventoryItem } from '../types/inventory';
export default function StockBadge({ sku, refresh = 0 }: { sku: string; refresh?: number }) {
  const [stock, setStock] = useState<number | null>(null);
  const [failed, setFailed] = useState(false);
  useEffect(() => {
    let active = true;
    api.get<InventoryItem>(`/api/inventory/${encodeURIComponent(sku)}`).then(r => {
      if (active) { setStock(r.data.onHand - r.data.reserved); setFailed(false); }
    }).catch(e => {
      if (active) { if (axios.isAxiosError(e) && e.response?.status === 404) setStock(0); else setFailed(true); }
    });
    return () => { active = false; };
  }, [sku, refresh]);
  return <span className={`badge ${failed ? 'badge-neutral' : stock === 0 ? 'badge-danger' : 'badge-success'}`}>
    {failed ? 'Chưa tải được tồn kho' : stock === null ? 'Đang tải kho…' : `Còn ${stock}`}
  </span>;
}
