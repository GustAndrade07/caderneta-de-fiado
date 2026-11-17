import React, { useCallback, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect, useLocalSearchParams, useRouter } from 'expo-router';
import { Fiado } from '../types';
import { api } from '../services/api';
import { colors } from '../theme/colors';
import { formatarData, formatarMoeda } from '../utils/formatadores';
import { avisar, confirmar } from '../utils/confirmar';

export function DetalheFiadoScreen() {
  const router = useRouter();
  const { id } = useLocalSearchParams<{ id: string }>();
  const [fiado, setFiado] = useState<Fiado | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [processando, setProcessando] = useState(false);

  // Recarrega ao voltar para a tela, para mostrar as alterações feitas no formulário.
  useFocusEffect(useCallback(() => {
    let ativo = true;
    api.get<Fiado>(`/fiados/${id}`)
      .then(({ data }) => { if (ativo) setFiado(data); })
      .catch((erro: any) => {
        // 401 já é tratado pelo interceptador do axios (volta para o login).
        if (ativo && erro?.response?.status !== 401) {
          avisar('Não foi possível carregar o fiado', 'Tente novamente em alguns instantes.');
          router.back();
        }
      })
      .finally(() => { if (ativo) setCarregando(false); });
    return () => { ativo = false; };
  }, [id, router]));

  const marcarComoPago = async () => {
    if (!fiado) return;
    setProcessando(true);
    try {
      const resposta = await api.patch<Fiado>(`/fiados/${fiado.id}/pagar`);
      setFiado(resposta.data);
    } catch (erro: any) {
      if (erro?.response?.status !== 401) {
        avisar('Não foi possível atualizar', 'Tente marcar o fiado como pago novamente.');
      }
    } finally {
      setProcessando(false);
    }
  };

  const excluir = async () => {
    if (!fiado) return;
    setProcessando(true);
    try {
      await api.delete(`/fiados/${fiado.id}`);
      // A lista recarrega sozinha ao voltar (useFocusEffect em ListaFiadosScreen).
      router.back();
    } catch (erro: any) {
      if (erro?.response?.status !== 401) {
        avisar('Não foi possível excluir', 'Tente excluir o fiado novamente.');
      }
      setProcessando(false);
    }
  };

  const confirmarExclusao = () => {
    if (!fiado) return;
    confirmar('Excluir fiado', `Deseja excluir o fiado de ${fiado.nomeCliente}?`, 'Excluir', () => void excluir());
  };

  if (carregando || !fiado) {
    return (
      <View style={[styles.container, styles.centro]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.conteudo}>
      <View style={styles.card}>
        <Text style={styles.nomeCliente}>{fiado.nomeCliente}</Text>
        <Text style={styles.valor}>{formatarMoeda(fiado.valor)}</Text>
        <View style={[styles.status, fiado.pago ? styles.statusPago : styles.statusAberto]}>
          <Text style={[styles.statusTexto, fiado.pago ? styles.statusTextoPago : styles.statusTextoAberto]}>
            {fiado.pago ? 'Pago' : 'Em aberto'}
          </Text>
        </View>

        <Text style={styles.rotulo}>Data</Text>
        <Text style={styles.informacao}>{formatarData(fiado.data)}</Text>

        <Text style={styles.rotulo}>Descrição</Text>
        <Text style={[styles.informacao, !fiado.descricao ? styles.semDescricao : null]}>
          {fiado.descricao || 'Sem descrição'}
        </Text>
      </View>

      <Pressable
        style={[styles.botaoEditar, processando ? styles.desabilitado : null]}
        disabled={processando}
        onPress={() => router.push({ pathname: '/formulario', params: { id: String(fiado.id) } })}
      >
        <Text style={styles.textoBotaoEditar}>Editar</Text>
      </Pressable>
      {!fiado.pago ? (
        <Pressable style={[styles.botaoPago, processando ? styles.desabilitado : null]} disabled={processando} onPress={() => void marcarComoPago()}>
          <Text style={styles.textoBotaoPago}>Marcar como pago</Text>
        </Pressable>
      ) : null}
      <Pressable style={[styles.botaoExcluir, processando ? styles.desabilitado : null]} disabled={processando} onPress={confirmarExclusao}>
        <Text style={styles.textoBotaoExcluir}>Excluir</Text>
      </Pressable>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  centro: { alignItems: 'center', justifyContent: 'center' },
  conteudo: { padding: 16, gap: 12 },
  card: { backgroundColor: colors.surface, borderColor: colors.border, borderRadius: 12, borderWidth: 1, padding: 20 },
  nomeCliente: { color: colors.text, fontSize: 22, fontWeight: 'bold' },
  valor: { color: colors.primaryDark, fontSize: 30, fontWeight: 'bold', marginTop: 8 },
  status: { alignSelf: 'flex-start', borderRadius: 12, borderWidth: 1, marginTop: 12, paddingHorizontal: 10, paddingVertical: 4 },
  statusAberto: { backgroundColor: colors.background, borderColor: colors.danger },
  statusPago: { backgroundColor: colors.background, borderColor: colors.success },
  statusTexto: { fontSize: 12, fontWeight: 'bold' },
  statusTextoAberto: { color: colors.danger },
  statusTextoPago: { color: colors.success },
  rotulo: { color: colors.textSecondary, fontSize: 13, fontWeight: '600', marginTop: 20, textTransform: 'uppercase' },
  informacao: { color: colors.text, fontSize: 16, marginTop: 4 },
  semDescricao: { color: colors.textSecondary, fontStyle: 'italic' },
  botaoEditar: { alignItems: 'center', backgroundColor: colors.primary, borderRadius: 8, padding: 14 },
  textoBotaoEditar: { color: colors.surface, fontSize: 16, fontWeight: 'bold' },
  botaoPago: { alignItems: 'center', backgroundColor: colors.success, borderRadius: 8, padding: 14 },
  textoBotaoPago: { color: colors.surface, fontSize: 16, fontWeight: 'bold' },
  botaoExcluir: { alignItems: 'center', borderColor: colors.danger, borderRadius: 8, borderWidth: 1, padding: 14 },
  textoBotaoExcluir: { color: colors.danger, fontSize: 16, fontWeight: 'bold' },
  desabilitado: { opacity: 0.6 },
});
