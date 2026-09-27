import axiosClient from './axiosClient';
import type { LoginRequest, LoginResponse } from '../types/auth';

export const loginApi = (data: LoginRequest) =>
  axiosClient.post<{
    status: number;
    data: {
      username: string;
      type: string;
      accessToken: string;
      refreshToken: string;
    };
    message: string;
  }>('/api/auth/login', data);

export const registerApi = (data: {
  username: string;
  password: string;
  email?: string;
}) =>
  axiosClient.post('/api/auth/register', data);

export const refreshTokenApi = (refreshToken: string) =>
  axiosClient.post<{
    status: number;
    data: {
      accessToken: string;
      refreshToken: string;
    };
    message: string;
  }>('/api/auth/refresh-token', { refreshToken });

export const logoutApi = (token: string) =>
  axiosClient.post('/api/auth/logout', null, {
    headers: { Authorization: `Bearer ${token}` },
  });

// Keep LoginResponse type re-export for consumers
export type { LoginRequest, LoginResponse };
