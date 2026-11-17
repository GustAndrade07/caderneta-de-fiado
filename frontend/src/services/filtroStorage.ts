import AsyncStorage from '@react-native-async-storage/async-storage';

// O filtro não é dado sensível, por isso fica no AsyncStorage (o token fica no SecureStore).
export type FiltroFiados = 'todos' | 'pendentes' | 'pagos';

const FILTRO_KEY = 'caderneta_fiado_filtro';
const FILTROS_VALIDOS: FiltroFiados[] = ['todos', 'pendentes', 'pagos'];

export const filtroStorage = {
  async get(): Promise<FiltroFiados> {
    try {
      const salvo = await AsyncStorage.getItem(FILTRO_KEY);
      return FILTROS_VALIDOS.includes(salvo as FiltroFiados) ? (salvo as FiltroFiados) : 'todos';
    } catch {
      return 'todos';
    }
  },
  async set(filtro: FiltroFiados) {
    try {
      await AsyncStorage.setItem(FILTRO_KEY, filtro);
    } catch {
      // Se não conseguir salvar, o filtro continua valendo só nesta sessão.
    }
  },
};
