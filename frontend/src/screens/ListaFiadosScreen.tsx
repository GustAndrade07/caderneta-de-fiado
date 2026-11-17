import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, FlatList, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect, useRouter } from 'expo-router';
import { Fiado } from '../types';
import { api } from '../services/api';
import { FiltroFiados, filtroStorage } from '../services/filtroStorage';
import { colors } from '../theme/colors';
import { formatarData, formatarMoeda } from '../utils/formatadores';
import { avisar, confirmar } from '../utils/confirmar';

const OPCOES_FILTRO: { valor: FiltroFiados; rotulo: string }[] = [
  { valor: 'todos', rotulo: 'Todos' },
  { valor: 'pendentes', rotulo: 'Pendentes' },
  { valor: 'pagos', rotulo: 'Pagos' },
];

const MENSAGEM_VAZIA: Record<FiltroFiados, string> = {
  todos: 'Nenhum fiado registrado.',
  pendentes: 'Nenhum fiado pendente.',
  pagos: 'Nenhum fiado pago.',
};

export function ListaFiadosScreen() {
  const router = useRouter();
  const [fiados, setFiados] = useState<Fiado[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [atualizando, setAtualizando] = useState(false);
  const [filtro, setFiltro] = useState<FiltroFiados>('todos');

  // Recupera o último filtro escolhido (salvo no AsyncStorage).
  useEffect(() => {
    void filtroStorage.get().then(setFiltro);
  }, []);

  const escolherFiltro = (novoFiltro: FiltroFiados) => {
    setFiltro(novoFiltro);
    void filtroStorage.set(novoFiltro);
  };

  const carregarFiados = useCallback(async (atualizacao = false) => {
    if (atualizacao) {
      setAtualizando(true);
    } else {
      setCarregando(true);
    }
    try {
      const resposta = await api.get<Fiado[]>('/fiados');
      setFiados(resposta.data);
    } catch (erro: any) {
      // 401 já é tratado pelo interceptador do axios (volta para o login).
      if (erro?.response?.status !== 401) {
        avisar('Não foi possível carregar', 'Tente novamente em alguns instantes.');
      }
    } finally {
      setCarregando(false);
      setAtualizando(false);
    }
  }, []);

  useFocusEffect(useCallback(() => { void carregarFiados(); }, [carregarFiados]));

  const totalDevido = useMemo(
    () => fiados.filter((fiado) => !fiado.pago).reduce((total, fiado) => total + fiado.valor, 0),
    [fiados],
  );

  const fiadosFiltrados = useMemo(() => {
    if (filtro === 'pendentes') return fiados.filter((fiado) => !fiado.pago);
    if (filtro === 'pagos') return fiados.filter((fiado) => fiado.pago);
    return fiados;
  }, [fiados, filtro]);

  const marcarComoPago = async (fiado: Fiado) => {
    try {
      const resposta = await api.patch<Fiado>(`/fiados/${fiado.id}/pagar`);
      setFiados((itens) => itens.map((item) => (item.id === fiado.id ? resposta.data : item)));
    } catch (erro: any) {
      if (erro?.response?.status !== 401) {
        avisar('Não foi possível atualizar', 'Tente marcar o fiado como pago novamente.');
      }
    }
  };

  const excluirFiado = async (id: number) => {
    try {
      await api.delete(`/fiados/${id}`);
      setFiados((itens) => itens.filter((item) => item.id !== id));
    } catch (erro: any) {
      if (erro?.response?.status !== 401) {
        avisar('Não foi possível excluir', 'Tente excluir o fiado novamente.');
      }
    }
  };

  const confirmarExclusao = (fiado: Fiado) => confirmar(
    'Excluir fiado',
    `Deseja excluir o fiado de ${fiado.nomeCliente}?`,
    'Excluir',
    () => void excluirFiado(fiado.id),
  );

  const abrirDetalhe = (fiado: Fiado) =>
    router.push({ pathname: '/detalhe/[id]', params: { id: String(fiado.id) } });

  const abrirEdicao = (fiado: Fiado) =>
    router.push({ pathname: '/formulario', params: { id: String(fiado.id) } });

  const renderItem = ({ item }: { item: Fiado }) => (
    <Pressable style={styles.card} onPress={() => abrirDetalhe(item)} accessibilityRole="button" accessibilityHint="Abre os detalhes do fiado">
      <View style={styles.cardHeader}>
        <Text style={styles.nomeCliente}>{item.nomeCliente}</Text>
        <Text style={styles.valor}>{formatarMoeda(item.valor)}</Text>
      </View>
      <View style={styles.cardInfo}>
        <Text style={styles.data}>{formatarData(item.data)}</Text>
        <View style={[styles.status, item.pago ? styles.statusPago : styles.statusAberto]}>
          <Text style={[styles.statusTexto, item.pago ? styles.statusTextoPago : styles.statusTextoAberto]}>
            {item.pago ? 'Pago' : 'Em aberto'}
          </Text>
        </View>
      </View>
      {item.descricao ? <Text style={styles.descricao}>{item.descricao}</Text> : null}
      <View style={styles.acoesCard}>
        {!item.pago ? <Pressable style={styles.botaoPago} onPress={() => void marcarComoPago(item)}><Text style={styles.textoBotaoPago}>Marcar como pago</Text></Pressable> : null}
        <Pressable style={styles.botaoEditar} onPress={() => abrirEdicao(item)}><Text style={styles.textoBotaoEditar}>Editar</Text></Pressable>
        <Pressable style={styles.botaoExcluir} onPress={() => confirmarExclusao(item)}><Text style={styles.textoBotaoExcluir}>Excluir</Text></Pressable>
      </View>
    </Pressable>
  );

  return (
    <View style={styles.container}>
      <View style={styles.resumo}><Text style={styles.resumoLabel}>Total a receber</Text><Text style={styles.resumoValor}>{formatarMoeda(totalDevido)}</Text></View>
      <View style={styles.acoesTopo}>
        <Pressable style={styles.botaoNovo} onPress={() => router.push('/formulario')}><Text style={styles.textoBotaoNovo}>Adicionar novo fiado</Text></Pressable>
        <Pressable style={styles.botaoPerfil} onPress={() => router.push('/perfil')}><Text style={styles.textoBotaoPerfil}>Perfil / Sair</Text></Pressable>
      </View>
      <View style={styles.filtros} accessibilityRole="tablist">
        {OPCOES_FILTRO.map((opcao) => {
          const ativo = filtro === opcao.valor;
          return (
            <Pressable
              key={opcao.valor}
              style={[styles.filtro, ativo ? styles.filtroAtivo : null]}
              onPress={() => escolherFiltro(opcao.valor)}
              accessibilityRole="tab"
              accessibilityState={{ selected: ativo }}
            >
              <Text style={[styles.textoFiltro, ativo ? styles.textoFiltroAtivo : null]}>{opcao.rotulo}</Text>
            </Pressable>
          );
        })}
      </View>
      {carregando ? <ActivityIndicator style={styles.carregando} size="large" color={colors.primary} /> : (
        <FlatList
          data={fiadosFiltrados}
          keyExtractor={(item) => item.id.toString()}
          renderItem={renderItem}
          contentContainerStyle={fiadosFiltrados.length ? styles.lista : styles.listaVazia}
          refreshControl={<RefreshControl refreshing={atualizando} onRefresh={() => void carregarFiados(true)} colors={[colors.primary]} />}
          ListEmptyComponent={<Text style={styles.vazio}>{MENSAGEM_VAZIA[filtro]}</Text>}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, backgroundColor: colors.background },
  resumo: { padding: 20, borderRadius: 12, backgroundColor: colors.primary, marginBottom: 12 },
  resumoLabel: { color: colors.surface, fontSize: 15 },
  resumoValor: { color: colors.surface, fontSize: 28, fontWeight: 'bold', marginTop: 4 },
  acoesTopo: { flexDirection: 'row', gap: 8, marginBottom: 16 },
  botaoNovo: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.primaryDark, borderRadius: 8, padding: 14 },
  textoBotaoNovo: { color: colors.surface, fontSize: 16, fontWeight: 'bold' },
  botaoPerfil: { alignItems: 'center', justifyContent: 'center', borderColor: colors.primaryDark, borderWidth: 1, borderRadius: 8, paddingHorizontal: 12 },
  textoBotaoPerfil: { color: colors.primaryDark, fontSize: 14, fontWeight: 'bold' },
  filtros: { flexDirection: 'row', gap: 8, marginBottom: 16 },
  filtro: { flex: 1, alignItems: 'center', borderColor: colors.border, borderRadius: 20, borderWidth: 1, backgroundColor: colors.surface, paddingVertical: 8 },
  filtroAtivo: { backgroundColor: colors.primary, borderColor: colors.primary },
  textoFiltro: { color: colors.textSecondary, fontSize: 14, fontWeight: '600' },
  textoFiltroAtivo: { color: colors.surface },
  carregando: { flex: 1 },
  lista: { gap: 12, paddingBottom: 16 },
  listaVazia: { flexGrow: 1, justifyContent: 'center', alignItems: 'center' },
  vazio: { color: colors.textSecondary, fontSize: 16 },
  card: { backgroundColor: colors.surface, borderColor: colors.border, borderRadius: 12, borderWidth: 1, padding: 16 },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', gap: 12 },
  nomeCliente: { color: colors.text, flex: 1, fontSize: 18, fontWeight: 'bold' },
  valor: { color: colors.primaryDark, fontSize: 17, fontWeight: 'bold' },
  cardInfo: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between', marginTop: 10 },
  data: { color: colors.textSecondary, fontSize: 14 },
  status: { borderRadius: 12, paddingHorizontal: 10, paddingVertical: 4 },
  statusAberto: { backgroundColor: colors.background, borderColor: colors.danger, borderWidth: 1 },
  statusPago: { backgroundColor: colors.background, borderColor: colors.success, borderWidth: 1 },
  statusTexto: { fontSize: 12, fontWeight: 'bold' },
  statusTextoAberto: { color: colors.danger },
  statusTextoPago: { color: colors.success },
  descricao: { color: colors.textSecondary, fontSize: 14, marginTop: 12 },
  acoesCard: { flexDirection: 'row', gap: 12, marginTop: 16 },
  botaoPago: { alignItems: 'center', backgroundColor: colors.success, borderRadius: 6, flex: 1, padding: 10 },
  textoBotaoPago: { color: colors.surface, fontWeight: 'bold' },
  botaoEditar: { alignItems: 'center', borderColor: colors.primaryDark, borderRadius: 6, borderWidth: 1, padding: 10 },
  textoBotaoEditar: { color: colors.primaryDark, fontWeight: 'bold' },
  botaoExcluir: { alignItems: 'center', borderColor: colors.danger, borderRadius: 6, borderWidth: 1, padding: 10 },
  textoBotaoExcluir: { color: colors.danger, fontWeight: 'bold' },
});
