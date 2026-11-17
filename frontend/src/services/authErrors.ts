import { isAxiosError } from 'axios';

interface ApiError {
  mensagem?: string;
}

export function authErrorMessage(error: unknown): string {
  if (!isAxiosError<ApiError>(error)) {
    return 'Não foi possível concluir a operação. Tente novamente.';
  }

  if (!error.response) {
    return 'Não foi possível conectar à API. Confira a rede e o endereço configurado.';
  }

  switch (error.response.status) {
    case 400:
      return error.response.data?.mensagem || 'Confira os dados informados.';
    case 401:
      return 'E-mail ou senha inválidos.';
    case 409:
      return 'Este e-mail já está cadastrado.';
    default:
      return 'Ocorreu um erro no servidor. Tente novamente.';
  }
}
