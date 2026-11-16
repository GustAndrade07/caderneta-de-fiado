import { api } from './api';
import { AuthResponse } from '../types';

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

  async cadastrar(dados: CadastroPayload): Promise<void> {
    await api.post('/auth/cadastro', dados);
  },
};