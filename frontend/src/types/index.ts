export interface Usuario {
  id: number;
  nome: string;
  email: string;
}

export interface Fiado {
  id: number;
  nomeCliente: string; // Exatamente como o atributo Java em Fiado.java
  valor: number;
  data: string;        // LocalDate serializado pelo Spring vem como "YYYY-MM-DD"
  descricao?: string;
  pago: boolean;
}

export interface AuthResponse {
  token: string;
  tipo: 'Bearer';
}
