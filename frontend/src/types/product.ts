export type ProductStatus =
    | "ACTIVE"
    | "INACTIVE"
    | "ARCHIVED";


export interface Product {
    id: string;
    sku: string;
    name: string;
    description: string;
    price: number;
    currency: string;
    status: ProductStatus;
    createdAt: string;
    updatedAt: string;
    version: number;
}


export interface ProductResponse<T> {
    items: T[];
    page: number;
    size: number;
    totalItems: number;
    totalPages: number;
    isLast: boolean;
}


export interface ProductFormValues {
    sku: string;
    name: string;
    description: string;
    price: number;
    currency: string;
    status: ProductStatus;
}


export const emptyProductForm: ProductFormValues = {
    sku: '',
    name: '',
    description: '',
    price: 0,
    currency: 'VND',
    status: 'ACTIVE',
};


export interface CreateProductRequest {
    sku: string;
    name: string;
    description: string;
    price: number;
    currency: string;
}


export interface UpdateProductRequest {
    name: string;
    description: string;
    price: number;
    status: ProductStatus;
}
