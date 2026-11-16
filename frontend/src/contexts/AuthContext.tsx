import React, { createContext, useContext, useEffect, useState } from 'react';
import * as SecureStore from 'expo-secure-store';
import { setUnauthorizedHandler, TOKEN_KEY } from '../services/api';
import { authService } from '../services/authService';

interface AuthContextData {
  token: string | null;
  loading: boolean;
  signIn: (email: string, senha: string) => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextData>({} as AuthContextData);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadStoredToken() {
      try {
        const storedToken = await SecureStore.getItemAsync(TOKEN_KEY);
        if (storedToken) {
          setToken(storedToken);
        }
      } finally {
        setLoading(false);
      }
    }

    loadStoredToken();

    // Registra o callback para o erro 401 do Axios voltar o app para o Login
    setUnauthorizedHandler(() => {
      setToken(null);
    });
  }, []);

  async function signIn(email: string, senha: string) {
    const data = await authService.login({ email, senha });
    await SecureStore.setItemAsync(TOKEN_KEY, data.token);
    setToken(data.token);
  }

  async function signOut() {
    await SecureStore.deleteItemAsync(TOKEN_KEY);
    setToken(null);
  }

  return (
    <AuthContext.Provider value={{ token, loading, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  );
};

export function useAuth() {
  return useContext(AuthContext);
}