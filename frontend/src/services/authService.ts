import { api } from './api';
import { AuthResponse, Usuario } from '../types';

export interface LoginPayload {
  email: string;
  senha: string;
}

export interface CadastroPayload {
  nome: string;
  email: string;
  senha: string;
}

export const authService = {
  async login(dados: LoginPayload): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', dados);
    return response.data;
  },

  async cadastrar(dados: CadastroPayload): Promise<Usuario> {
    const response = await api.post<Usuario>('/auth/register', dados);
    return response.data;
  },

  async me(): Promise<Usuario> {
    const response = await api.get<Usuario>('/usuarios/me');
    return response.data;
  },
};
