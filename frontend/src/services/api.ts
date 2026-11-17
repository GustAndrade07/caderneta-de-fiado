import { create } from 'axios';
import Constants from 'expo-constants';
import { Platform } from 'react-native';
import { tokenStorage } from './tokenStorage';

const PORTA_API = 8080;

// Descobre o endereço da API sem precisar de .env:
// 1. EXPO_PUBLIC_API_URL, se alguém configurou;
// 2. na web, o mesmo computador que serviu a página;
// 3. no Expo Go, o IP do PC que está rodando o `npx expo start` (o celular já fala com ele);
// 4. senão, o emulador Android (10.0.2.2) ou o próprio computador (localhost).
function descobrirUrlDaApi(): string {
  if (process.env.EXPO_PUBLIC_API_URL) return process.env.EXPO_PUBLIC_API_URL;

  if (Platform.OS === 'web' && typeof window !== 'undefined') {
    return `http://${window.location.hostname}:${PORTA_API}`;
  }

  const hostDoExpo = Constants.expoConfig?.hostUri?.split(':')[0];
  if (hostDoExpo && /^\d{1,3}(\.\d{1,3}){3}$/.test(hostDoExpo)) {
    return `http://${hostDoExpo}:${PORTA_API}`;
  }

  return Platform.OS === 'android' ? `http://10.0.2.2:${PORTA_API}` : `http://localhost:${PORTA_API}`;
}

const BASE_URL = descobrirUrlDaApi();

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
