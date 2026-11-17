export const EMAIL_FORMAT_MESSAGE = 'Use o formato nome@dominio.com (ex.: maria@exemplo.com).';

export function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@.]+(\.[^\s@.]+)+$/.test(email);
}
