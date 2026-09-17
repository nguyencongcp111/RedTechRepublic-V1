import type { Product } from "../types/product";
import type { LoadState } from "../api/useProducts";
interface ProductListProps {
  products: Product[];
  state: LoadState;
  errorMessage: string;
  onRetry: () => void;
  onEdit: (product: Product) => void;
}
export default function ProductList({
  products,
  state,
  errorMessage,
  onRetry,
  onEdit
}: ProductListProps) {
  if (state === "loading") {
    return <p>Dang tai danh sach san pham...</p>;
  }
  if (state === "error") {
    return (
      <div style={{ color: "#b91c1c" }}>
        <p>{errorMessage}</p>
        <button onClick={onRetry}>Thu lai</button>
      </div>
    );
  }
  if (state === "empty") {
    return <p>Khong tim thay san pham nao phu hop.</p>;
  }
  // state === 'success'
  return (
    <table style={{ width: "100%", borderCollapse: "collapse" }}>
      <thead>
        <tr style={{ textAlign: "left", borderBottom: "2px solid #333" }}>
          <th>Mã SKU</th>
          <th>Tên sản phẩm</th>
          <th>Mô tả</th>
          <th>Giá</th>
          <th>Đơn vị tiền tệ</th>
          <th>Trạng thái</th>
          <th>Ngày tạo</th>
          <th>Ngày cập nhật</th>
          <th>Phiên bản</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody>
        {products.map((product) => (
          <tr key={product.id} style={{ borderBottom: "1px solid #eee" }}>
            <td>{product.sku}</td>
            <td>{product.name}</td>
            <td>{product.description}</td>
            <td>{product.price}</td>
            <td>{product.currency}</td>
            <td>{product.status}</td>
            <td>{product.createdAt}</td>
            <td>{product.updatedAt}</td>
            <td>{product.version}</td>
            <td>
              <button
                onClick={() => 
                  onEdit(product)
                }
              >
                Sửa
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
