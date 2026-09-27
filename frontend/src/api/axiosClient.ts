import axios from 'axios';
const axiosClient = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL, headers: { 'Content-Type': 'application/json' } });
let refreshing: Promise<string> | null = null;
export function clearSession() {
  localStorage.removeItem('accessToken'); localStorage.removeItem('refreshToken'); localStorage.removeItem('user');
  window.dispatchEvent(new Event('auth-changed'));
}
axiosClient.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token && !/\/api\/auth\/(login|register|refresh-token)$/.test(config.url ?? '')) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
axiosClient.interceptors.response.use(r => r, async error => {
  const request = error.config;
  const authEndpoint = /\/api\/auth\/(login|register|refresh-token)$/.test(request?.url ?? '');
  if (error.response?.status !== 401 || !request || request._retry || authEndpoint) return Promise.reject(error);
  request._retry = true;
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) { clearSession(); return Promise.reject(error); }
  if (!refreshing) {
    const previousToken = localStorage.getItem('accessToken');
    refreshing = axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh-token`, { refreshToken })
      .then(r => {
        if (localStorage.getItem('accessToken') !== previousToken) throw new Error('Phiên đăng nhập đã thay đổi');
        const data = r.data.data;
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        window.dispatchEvent(new Event('auth-changed'));
        return data.accessToken as string;
      }).catch(e => { clearSession(); throw e; }).finally(() => { refreshing = null; });
  }
  try { request.headers.Authorization = `Bearer ${await refreshing}`; return axiosClient(request); }
  catch (e) { return Promise.reject(e); }
});
export default axiosClient;
