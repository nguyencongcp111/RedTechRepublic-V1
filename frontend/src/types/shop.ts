export interface ShopRequest {
  name: string;
  address: string;
  phone: string;
  email: string;
}

export interface Shop extends ShopRequest {
  id: string;
  ownerUsername: string;
  system: boolean;
  createdAt: string;
  updatedAt: string;
}

export const emptyShop: ShopRequest = { name: '', address: '', phone: '', email: '' };
