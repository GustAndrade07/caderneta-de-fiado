export const TOKEN_KEY = 'caderneta_fiado_jwt';

export const tokenStorage = {
  get: async () => typeof localStorage === 'undefined' ? null : localStorage.getItem(TOKEN_KEY),
  set: async (token: string) => { localStorage.setItem(TOKEN_KEY, token); },
  clear: async () => { localStorage.removeItem(TOKEN_KEY); },
};
