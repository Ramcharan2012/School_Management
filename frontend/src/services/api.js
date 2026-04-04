import axios from 'axios';

// Use relative URL so Vite dev proxy forwards to Spring Boot (avoids CORS)
// Vite proxy: localhost:5173/api → localhost:8080/api
const BASE_URL = '/api';

const api = axios.create({ baseURL: BASE_URL });

// Attach JWT to every request automatically
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Auto-logout on 401
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default api;

// ── Auth ────────────────────────────────────────────────────────────────────
export const authAPI = {
  login: (identifier, password) =>
    api.post('/auth/login', { identifier, password }),
  me: () => api.get('/auth/me'),
  logout: () => api.post('/auth/logout'),
};

// ── Transport ────────────────────────────────────────────────────────────────
export const transportAPI = {
  getRoutes: () => api.get('/admin/transport/routes'),
  getStops: (routeId) => api.get(`/admin/transport/routes/${routeId}/stops`),
  getVehicles: () => api.get('/admin/transport/vehicles'),
  getBusLocation: (vehicleId) => api.get(`/transport/bus/${vehicleId}/location`),
  sendLocation: (data) => api.post('/transport/location', data),
  createRoute: (data) => api.post('/admin/transport/routes', data),
  createVehicle: (data) => api.post('/admin/transport/vehicles', data),
  addStop: (routeId, data) => api.post(`/admin/transport/routes/${routeId}/stops`, data),
};

// ── Dashboard ────────────────────────────────────────────────────────────────
export const dashboardAPI = {
  getStats: () => api.get('/admin/dashboard/stats'),
};
