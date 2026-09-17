import { useState, useEffect, useCallback } from 'react';
import { getProducts } from './productApi';
import type { Product } from '../types/product';
import axios from 'axios';
import type { ApiErrorResponse } from '../types/apiError';

export type LoadState = 'loading' | 'success' | 'empty' | 'error';

export function useProducts(keyword: string, page: number, size = 10) {
    const [products, setProducts] = useState<Product[]>([]);
    const [totalPages, setTotalPages] = useState(0)
    const [state, setState] = useState<LoadState>('loading');
    const [errorMessage, setErrorMessage] = useState<string>('');
    
    const fetchProducts = useCallback(() => {
        console.log('FETCH PRODUCTS:', {
                keyword,
                page,
                size
        });

        setState('loading');
        getProducts(keyword, page, size)
            .then((res) => {
                const data = res.data;
                setProducts(data.items);
                setTotalPages(data.totalPages);
                setState(data.items.length === 0 ? 'empty' : 'success');
            })
            .catch((err) => {
                let message = 'Da xay ra loi khong xac dinh, vui long thu lai.';

                if (axios.isAxiosError<ApiErrorResponse>(err)) {
                    if (err.response?.data?.message) {
                        message = err.response.data.message;
                    } else if (!err.response) {
            // Khong nhan duoc response nao ca - Gateway hoa course-service dang tat

                        message = 'Khong ket noi duoc toi he thong. Vui long thu lai sau.';
                    }
                }
                setErrorMessage(message);
                setState('error');
            });
    }, [keyword, page, size]);
    useEffect(() => { fetchProducts(); }, [fetchProducts]);
    return { products, totalPages, state, errorMessage, refetch: fetchProducts };
}