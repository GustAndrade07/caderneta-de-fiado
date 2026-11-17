import React, { createContext, useContext, useEffect, useState } from 'react';
import { isAxiosError } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { setUnauthorizedHandler } from '../services/api';
import { authService } from '../services/authService';
import { tokenStorage } from '../services/tokenStorage';
import { Usuario } from '../types';

const USER_STORAGE_KEY = '@caderneta_fiado:usuario';

interface AuthContextData {
  token: string | null;
  usuario: Usuario | null;
  loading: boolean;
  signIn: (email: string, senha: string) => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextData>({} as AuthContextData);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(null);
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    setUnauthorizedHandler(() => {
      if (active) {
        void AsyncStorage.removeItem(USER_STORAGE_KEY);
        setToken(null);
        setUsuario(null);
      }
    });

    async function loadStoredSession() {
      try {
        const [storedToken, storedUser] = await Promise.all([
          tokenStorage.get(),
          AsyncStorage.getItem(USER_STORAGE_KEY),
        ]);

        if (storedUser && active) {
          setUsuario(JSON.parse(storedUser));
        }

        if (storedToken) {
          try {
            const meData = await authService.me();
            if (active) {
              setToken(storedToken);
              // Atualiza os dados do usuário com o retorno mais recente do /me, se vier
              if (meData) {
                setUsuario(meData);
                await AsyncStorage.setItem(USER_STORAGE_KEY, JSON.stringify(meData));
              }
            }
          } catch (error) {
            // Uma falha de rede não invalida a sessão; um 401 invalida.
            if ((!isAxiosError(error) || error.response?.status !== 401) && active) {
              setToken(storedToken);
            } else if (active) {
              await AsyncStorage.removeItem(USER_STORAGE_KEY);
              setUsuario(null);
            }
          }
        }
      } catch {
        // Se o armazenamento local falhar, o usuário pode entrar novamente.
      } finally {
        if (active) setLoading(false);
      }
    }

    void loadStoredSession();
    return () => {
      active = false;
      setUnauthorizedHandler(null);
    };
  }, []);

  async function signIn(email: string, senha: string) {
    const data = await authService.login({ email, senha });
    await tokenStorage.set(data.token);

    let dadosUsuario: Usuario;
    try {
      dadosUsuario = await authService.me();
    } catch {
      dadosUsuario = { id: 0, nome: email.split('@')[0], email };
    }

    await AsyncStorage.setItem(USER_STORAGE_KEY, JSON.stringify(dadosUsuario));
    setUsuario(dadosUsuario);
    setToken(data.token);
  }

  async function signOut() {
    await Promise.all([
      tokenStorage.clear(),
      AsyncStorage.removeItem(USER_STORAGE_KEY),
    ]);
    setToken(null);
    setUsuario(null);
  }

  return (
    <AuthContext.Provider value={{ token, usuario, loading, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  );
};

export function useAuth() {
  return useContext(AuthContext);
}