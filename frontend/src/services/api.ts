import { create } from 'axios';
import { Platform } from 'react-native';
import { tokenStorage } from './tokenStorage';

// No celular físico, configure EXPO_PUBLIC_API_URL com o IPv4 do computador.
const BASE_URL = process.env.EXPO_PUBLIC_API_URL || (Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080');

export const api = create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptador de requisição: anexa o token JWT salvo
api.interceptors.request.use(async (config) => {
  if (!config.url?.startsWith('/auth/')) {
    const token = await tokenStorage.get();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

// Interceptador de resposta: se o back-end retornar 401, apaga o token e desloga
let onUnauthorizedCallback: (() => void) | null = null;

export function setUnauthorizedHandler(callback: (() => void) | null) {
  onUnauthorizedCallback = callback;
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && !error.config?.url?.startsWith('/auth/')) {
      await tokenStorage.clear();
      if (onUnauthorizedCallback) {
        onUnauthorizedCallback();
      }
    }
    return Promise.reject(error);
  }
);
