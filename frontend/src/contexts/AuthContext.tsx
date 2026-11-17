import React, { createContext, useContext, useEffect, useState } from 'react';
import { isAxiosError } from 'axios';
import { setUnauthorizedHandler } from '../services/api';
import { authService } from '../services/authService';
import { tokenStorage } from '../services/tokenStorage';

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
    let active = true;

    setUnauthorizedHandler(() => {
      if (active) setToken(null);
    });

    async function loadStoredToken() {
      try {
        const storedToken = await tokenStorage.get();
        if (storedToken) {
          try {
            await authService.me();
            if (active) setToken(storedToken);
          } catch (error) {
            // Uma falha de rede não invalida a sessão; um 401 invalida.
            if ((!isAxiosError(error) || error.response?.status !== 401) && active) {
              setToken(storedToken);
            }
          }
        }
      } catch {
        // Se o armazenamento local falhar, o usuário pode entrar novamente.
      } finally {
        if (active) setLoading(false);
      }
    }

    void loadStoredToken();
    return () => {
      active = false;
      setUnauthorizedHandler(null);
    };
  }, []);

  async function signIn(email: string, senha: string) {
    const data = await authService.login({ email, senha });
    await tokenStorage.set(data.token);
    setToken(data.token);
  }

  async function signOut() {
    await tokenStorage.clear();
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
