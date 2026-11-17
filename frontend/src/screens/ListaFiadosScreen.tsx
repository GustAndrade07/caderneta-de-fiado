import React, { useCallback, useMemo, useState } from 'react';
import { ActivityIndicator, Alert, FlatList, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect, useRouter } from 'expo-router';
import { Fiado } from '../types';
import { api } from '../services/api';
import { colors } from '../theme/colors';

const formatarMoeda = (valor: number) =>
  new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);

const formatarData = (data: string) => {
  const [ano, mes, dia] = data.split('-');
  return ano && mes && dia ? `${dia}/${mes}/${ano}` : data;
};

export function ListaFiadosScreen() {
  const router = useRouter();
  const [fiados, setFiados] = useState<Fiado[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [atualizando, setAtualizando] = useState(false);

  const carregarFiados = useCallback(async (atualizacao = false) => {
    if (atualizacao) {
      setAtualizando(true);
    } else {
      setCarregando(true);
    }
    try {
      const resposta = await api.get<Fiado[]>('/fiados');
      setFiados(resposta.data);
    } catch {
      Alert.alert('Não foi possível carregar', 'Tente novamente em alguns instantes.');
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

  const marcarComoPago = async (fiado: Fiado) => {
    try {
      const resposta = await api.patch<Fiado>(`/fiados/${fiado.id}/pagar`);
      setFiados((itens) => itens.map((item) => (item.id === fiado.id ? resposta.data : item)));
    } catch {
      Alert.alert('Não foi possível atualizar', 'Tente marcar o fiado como pago novamente.');
    }
  };

  const excluirFiado = async (id: number) => {
    try {
      await api.delete(`/fiados/${id}`);
      setFiados((itens) => itens.filter((item) => item.id !== id));
    } catch {
      Alert.alert('Não foi possível excluir', 'Tente excluir o fiado novamente.');
    }
  };

  const confirmarExclusao = (fiado: Fiado) => Alert.alert(
    'Excluir fiado',
    `Deseja excluir o fiado de ${fiado.nomeCliente}?`,
    [{ text: 'Cancelar', style: 'cancel' }, { text: 'Excluir', style: 'destructive', onPress: () => void excluirFiado(fiado.id) }],
  );

  const renderItem = ({ item }: { item: Fiado }) => (
    <View style={styles.card}>
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
        <Pressable style={styles.botaoExcluir} onPress={() => confirmarExclusao(item)}><Text style={styles.textoBotaoExcluir}>Excluir</Text></Pressable>
      </View>
    </View>
  );

  return (
    <View style={styles.container}>
      <View style={styles.resumo}><Text style={styles.resumoLabel}>Total a receber</Text><Text style={styles.resumoValor}>{formatarMoeda(totalDevido)}</Text></View>
      <View style={styles.acoesTopo}>
        <Pressable style={styles.botaoNovo} onPress={() => router.push('/formulario')}><Text style={styles.textoBotaoNovo}>Adicionar novo fiado</Text></Pressable>
        <Pressable style={styles.botaoPerfil} onPress={() => router.push('/perfil')}><Text style={styles.textoBotaoPerfil}>Perfil / Sair</Text></Pressable>
      </View>
      {carregando ? <ActivityIndicator style={styles.carregando} size="large" color={colors.primary} /> : (
        <FlatList
          data={fiados}
          keyExtractor={(item) => item.id.toString()}
          renderItem={renderItem}
          contentContainerStyle={fiados.length ? styles.lista : styles.listaVazia}
          refreshControl={<RefreshControl refreshing={atualizando} onRefresh={() => void carregarFiados(true)} colors={[colors.primary]} />}
          ListEmptyComponent={<Text style={styles.vazio}>Nenhum fiado registrado.</Text>}
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
  botaoExcluir: { alignItems: 'center', borderColor: colors.danger, borderRadius: 6, borderWidth: 1, padding: 10 },
  textoBotaoExcluir: { color: colors.danger, fontWeight: 'bold' },
});
