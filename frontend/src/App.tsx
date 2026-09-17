import { useState, useCallback } from "react";
import { useProducts } from "./api/useProducts";
import { createProduct, updateProduct } from "./api/productApi";
import SearchBox from './components/SearchBox';
import Pagination from './components/Pagination';
import ProductForm from './components/ProductForm';
import type { Product, CreateProductRequest, UpdateProductRequest } from './types/product';
import type { ApiErrorResponse } from './types/apiError';
import ProductList from "./components/ProductList";

function App() {
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const { products, totalPages, state, errorMessage, refetch } = useProducts(keyword, page);

  const handleSearch = useCallback((newKeyword: string) => {
    console.log('SEARCH CALLED:', newKeyword);

    setKeyword(newKeyword);
    setPage(0);
  }, []);

  const extractErrorMessage = (error: unknown): string => {
    let message = 'Da xay ra loi khong xac dinh, vui long thu lai.';

    if (error instanceof Error) {
      message = error.message;
    } else if (typeof error === 'string') {
      message = error;
    } else if (typeof error === 'object' && error !== null) {
      const errObj = error as ApiErrorResponse;
      if (errObj.message) {
        message = errObj.message;
      }
    }
    return message;
  }

  const handleFormSubmit = async (
    values: CreateProductRequest | UpdateProductRequest
) => {
    setSubmitting(true);
    setFormError(null);

    try {
        if (editingProduct) {
            if (!('status' in values)) {
                throw new Error('Dữ liệu cập nhật sản phẩm không hợp lệ.');
            }

            await updateProduct(editingProduct.id, values);
        } else {
            if (!('sku' in values)) {
                throw new Error('Dữ liệu tạo sản phẩm không hợp lệ.');
            }

            await createProduct(values);
        }

        setEditingProduct(null);
        await refetch();
    } catch (err) {
        setFormError(extractErrorMessage(err));
    } finally {
        setSubmitting(false);
    }
};
  return (
    <div style={{ padding: 24, fontFamily: 'sans-serif', maxWidth:
    800, margin: '0 auto' }}>
      <h1>Quản lý sản phẩm (ADMIN)</h1>
      <ProductForm
        editingProduct={editingProduct}
        onSubmit={handleFormSubmit}
        submitting={submitting}
        serverError={formError}
        onCancel={() => setEditingProduct(null)}
      />
      <SearchBox onSearch={handleSearch} />
      <div style={{ marginTop: 16 }}>
        <ProductList
          products={products}
          state={state}
          errorMessage={errorMessage}
          onRetry={refetch}
          onEdit={setEditingProduct}
        />
      </div>
      <Pagination currentPage={page} totalPages={totalPages}
      onPageChange={setPage} />
    </div>
  );
}

export default App;