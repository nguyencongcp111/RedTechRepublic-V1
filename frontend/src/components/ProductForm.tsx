import { useState, useEffect } from 'react';
import type { CreateProductRequest, Product, ProductFormValues, ProductStatus, UpdateProductRequest } from '../types/product';
import { emptyProductForm } from '../types/product';

interface ProductFormProps {
    editingProduct: Product | null;
    onSubmit: (values: CreateProductRequest | UpdateProductRequest) => Promise<void>;
    onCancel: () => void;
    submitting: boolean;
    serverError: string | null;
}

export default function ProductForm({
editingProduct,
onSubmit,
onCancel,
submitting,
serverError,
}: ProductFormProps) {
const [values, setValues] =
useState<ProductFormValues>(emptyProductForm);
const [clientErrors, setClientErrors] =
    useState<Partial<Record<keyof ProductFormValues, string>>>({});

useEffect(() => {
if (editingProduct) {
setValues({
sku: editingProduct.sku,
name: editingProduct.name,
description: editingProduct.description,
price: editingProduct.price,
currency: editingProduct.currency,
status: editingProduct.status,
});
} else {
setValues(emptyProductForm);
}
setClientErrors({});
}, [editingProduct]);
const validate = (): boolean => {
    const errors: Partial<
        Record<keyof ProductFormValues, string>
    > = {};

    // SKU
    const sku = values.sku.trim();

    if (!sku) {
        errors.sku = 'SKU không được để trống.';
    } else if (sku.length > 64) {
        errors.sku = 'SKU không được quá 64 ký tự.';
    } else if (!/^[A-Za-z0-9][A-Za-z0-9_-]*$/.test(sku)) {
        errors.sku =
            'SKU chỉ được chứa chữ, số, dấu gạch ngang và gạch dưới; ký tự đầu tiên phải là chữ hoặc số.';
    }

    // NAME
    const name = values.name.trim();

    if (!name) {
        errors.name = 'Tên sản phẩm không được để trống.';
    } else if (name.length > 255) {
        errors.name = 'Tên sản phẩm không được quá 255 ký tự.';
    }

    // DESCRIPTION
    const description = values.description ?? '';

    if (description.length > 1500) {
        errors.description =
            'Mô tả không được quá 1500 ký tự.';
    }

    // PRICE
    if (!Number.isFinite(values.price)) {
        errors.price = 'Giá sản phẩm phải là một số hợp lệ.';
    } else if (!Number.isInteger(values.price)) {
        errors.price = 'Giá sản phẩm phải là số nguyên.';
    } else if (values.price < 0) {
        errors.price = 'Giá sản phẩm phải lớn hơn hoặc bằng 0.';
    }

    // CURRENCY
    const currency = values.currency.trim();

    if (!currency) {
        errors.currency =
            'Đơn vị tiền không được để trống.';
    } else if (!/^[A-Za-z]{3}$/.test(currency)) {
        errors.currency =
            'Đơn vị tiền phải gồm đúng 3 chữ cái.';
    }

    setClientErrors(errors);

    return Object.keys(errors).length === 0;
};
const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) return;

    if (editingProduct) {
        const payload: UpdateProductRequest = {
            name: values.name.trim(),
            description: values.description.trim(),
            price: values.price,
            status: values.status,
        };

        await onSubmit(payload);
    } else {
        const payload: CreateProductRequest = {
            sku: values.sku.trim(),
            name: values.name.trim(),
            description: values.description.trim(),
            price: values.price,
            currency: values.currency.trim().toUpperCase(),
        };

        await onSubmit(payload);
    }
};
return (
    <form
        onSubmit={handleSubmit}
        style={{
            border: '1px solid #ddd',
            padding: 16,
            borderRadius: 8,
            marginBottom: 16,
        }}
    >
        <h3>
            {editingProduct
                ? 'Sửa sản phẩm'
                : 'Thêm sản phẩm mới'}
        </h3>

        {/* SKU - chỉ khi thêm */}
        {!editingProduct && (
            <div style={{ marginBottom: 8 }}>
                <label>SKU</label>
                <br />

                <input
                    type="text"
                    value={values.sku}
                    onChange={(e) =>
                        setValues({
                            ...values,
                            sku: e.target.value,
                        })
                    }
                    maxLength={64}
                />

                {clientErrors.sku && (
                    <p
                        style={{
                            color: '#b91c1c',
                            margin: 0,
                        }}
                    >
                        {clientErrors.sku}
                    </p>
                )}
            </div>
        )}

        {/* Tên sản phẩm */}
        <div style={{ marginBottom: 8 }}>
            <label>Tên sản phẩm</label>
            <br />

            <input
                type="text"
                value={values.name}
                onChange={(e) =>
                    setValues({
                        ...values,
                        name: e.target.value,
                    })
                }
                maxLength={255}
            />

            {clientErrors.name && (
                <p
                    style={{
                        color: '#b91c1c',
                        margin: 0,
                    }}
                >
                    {clientErrors.name}
                </p>
            )}
        </div>

        {/* Mô tả */}
        <div style={{ marginBottom: 8 }}>
            <label>Mô tả</label>
            <br />

            <textarea
                value={values.description}
                onChange={(e) =>
                    setValues({
                        ...values,
                        description: e.target.value,
                    })
                }
                maxLength={editingProduct ? 2000 : 1500}
                rows={4}
            />

            {clientErrors.description && (
                <p
                    style={{
                        color: '#b91c1c',
                        margin: 0,
                    }}
                >
                    {clientErrors.description}
                </p>
            )}
        </div>

        {/* Giá */}
        <div style={{ marginBottom: 8 }}>
            <label>Giá</label>
            <br />

            <input
                type="number"
                value={values.price}
                onChange={(e) =>
                    setValues({
                        ...values,
                        price: Number(e.target.value),
                    })
                }
                min={0}
                step={1}
            />

            {clientErrors.price && (
                <p
                    style={{
                        color: '#b91c1c',
                        margin: 0,
                    }}
                >
                    {clientErrors.price}
                </p>
            )}
        </div>

        {/* Đơn vị tiền - chỉ khi thêm */}
        {!editingProduct && (
            <div style={{ marginBottom: 8 }}>
                <label>Đơn vị tiền</label>
                <br />

                <input
                    type="text"
                    value={values.currency}
                    onChange={(e) =>
                        setValues({
                            ...values,
                            currency: e.target.value,
                        })
                    }
                    maxLength={3}
                    placeholder="VND"
                />

                {clientErrors.currency && (
                    <p
                        style={{
                            color: '#b91c1c',
                            margin: 0,
                        }}
                    >
                        {clientErrors.currency}
                    </p>
                )}
            </div>
        )}

        {/* Trạng thái - chỉ khi sửa */}
        {editingProduct && (
            <div style={{ marginBottom: 8 }}>
                <label>Trạng thái</label>
                <br />

                <select
                    value={values.status}
                    onChange={(e) =>
                        setValues({
                            ...values,
                            status: e.target.value as ProductStatus,
                        })
                    }
                >
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="INACTIVE">INACTIVE</option>
                    <option value="ARCHIVED">ARCHIVED</option>
                </select>

                {clientErrors.status && (
                    <p
                        style={{
                            color: '#b91c1c',
                            margin: 0,
                        }}
                    >
                        {clientErrors.status}
                    </p>
                )}
            </div>
        )}

        {/* Server error */}
        {serverError && (
            <p
                style={{
                    color: '#b91c1c',
                }}
            >
                {serverError}
            </p>
        )}

        {/* Buttons */}
        <button
            type="submit"
            disabled={submitting}
        >
            {submitting
                ? 'Đang lưu...'
                : editingProduct
                    ? 'Cập nhật'
                    : 'Thêm mới'}
        </button>

        {editingProduct && (
            <button
                type="button"
                onClick={onCancel}
                style={{ marginLeft: 8 }}
            >
                Hủy
            </button>
        )}
    </form>
);

}