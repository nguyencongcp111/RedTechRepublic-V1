export type Role = 'ADMIN' | 'SELLER' | 'USER';

export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    token: string;
    username: string;
    role: Role;
}
