import axiosClient from './axiosClient';
import type {
    Product,
    ProductResponse,
    CreateProductRequest,
    UpdateProductRequest,
} from '../types/product';


export const getProducts = (
    keyword?: string,
    page: number = 0,
    size: number = 10
) => {
    return axiosClient.get<ProductResponse<Product>>('/products', {
        params: {
            keyword,
            page,
            size,
        },
    });
};


export const createProduct = (
    values: CreateProductRequest
) => {
    return axiosClient.post<Product>('/products', values);
};


export const updateProduct = (
    id: string,
    values: UpdateProductRequest
) => {
    return axiosClient.put<Product>(
        `/products/${id}`,
        values
    );
};