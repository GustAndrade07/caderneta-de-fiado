import React, { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AppStackParamList, Fiado } from '../types';
import { api } from '../services/api';
import { colors } from '../theme/colors';

type Props = NativeStackScreenProps<AppStackParamList, 'FormularioFiado'>;

type Erros = { nomeCliente?: string; valor?: string; data?: string };

// A API usa "YYYY-MM-DD"; na tela o usuário digita "DD/MM/AAAA".
const apiParaTela = (data: string) => {
  const [ano, mes, dia] = data.split('-');
  return ano && mes && dia ? `${dia}/${mes}/${ano}` : data;
};

const hojeNaTela = () => {
  const hoje = new Date();
  const dia = String(hoje.getDate()).padStart(2, '0');
  const mes = String(hoje.getMonth() + 1).padStart(2, '0');
  return `${dia}/${mes}/${hoje.getFullYear()}`;
};

// Insere as barras enquanto a pessoa digita: 16112026 -> 16/11/2026
const mascararData = (texto: string) => {
  const numeros = texto.replace(/\D/g, '').slice(0, 8);
  if (numeros.length <= 2) return numeros;
  if (numeros.length <= 4) return `${numeros.slice(0, 2)}/${numeros.slice(2)}`;
  return `${numeros.slice(0, 2)}/${numeros.slice(2, 4)}/${numeros.slice(4)}`;
};

// Devolve "YYYY-MM-DD" se a data existir de verdade (ex.: recusa 31/02/2026); senão, null.
const telaParaApi = (texto: string): string | null => {
  const partes = texto.split('/');
  if (partes.length !== 3) return null;
  const [dia, mes, ano] = partes.map(Number);
  if (!dia || !mes || String(ano).length !== 4) return null;
  const data = new Date(ano, mes - 1, dia);
  const existe = data.getFullYear() === ano && data.getMonth() === mes - 1 && data.getDate() === dia;
  if (!existe) return null;
  return `${ano}-${String(mes).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;
};

// Aceita "12,50" e "12.50".
const textoParaValor = (texto: string) => Number(texto.trim().replace(',', '.'));

export function FormularioFiadoScreen({ route, navigation }: Props) {
  const fiadoEdicao: Fiado | undefined = route.params?.fiado;

  const [nomeCliente, setNomeCliente] = useState(fiadoEdicao?.nomeCliente ?? '');
  const [valor, setValor] = useState(fiadoEdicao ? String(fiadoEdicao.valor).replace('.', ',') : '');
  const [data, setData] = useState(fiadoEdicao ? apiParaTela(fiadoEdicao.data) : hojeNaTela());
  const [descricao, setDescricao] = useState(fiadoEdicao?.descricao ?? '');
  const [erros, setErros] = useState<Erros>({});
  const [salvando, setSalvando] = useState(false);

  const validar = (): Erros => {
    const novosErros: Erros = {};
    if (!nomeCliente.trim()) {
      novosErros.nomeCliente = 'Informe o nome do cliente.';
    }
    const numero = textoParaValor(valor);
    if (!valor.trim() || Number.isNaN(numero)) {
      novosErros.valor = 'Informe um valor válido.';
    } else if (numero <= 0) {
      novosErros.valor = 'O valor deve ser maior que zero.';
    }
    if (!telaParaApi(data)) {
      novosErros.data = 'Informe uma data válida (DD/MM/AAAA).';
    }
    return novosErros;
  };

  const salvar = async () => {
    const novosErros = validar();
    setErros(novosErros);
    if (Object.keys(novosErros).length > 0) return;

    // O PUT e o POST recebem o mesmo corpo (FiadoRequest no back-end).
    const corpo = {
      nomeCliente: nomeCliente.trim(),
      valor: textoParaValor(valor),
      data: telaParaApi(data),
      descricao: descricao.trim(),
    };

    setSalvando(true);
    try {
      if (fiadoEdicao) {
        await api.put<Fiado>(`/fiados/${fiadoEdicao.id}`, corpo);
      } else {
        await api.post<Fiado>('/fiados', corpo);
      }
      // A lista recarrega sozinha ao voltar (useFocusEffect em ListaFiadosScreen).
      navigation.goBack();
    } catch (erro: any) {
      // 401 já é tratado pelo interceptador do axios (volta para o login).
      if (erro?.response?.status !== 401) {
        const mensagem = erro?.response?.data?.mensagem ?? 'Tente novamente em alguns instantes.';
        Alert.alert('Não foi possível salvar', String(mensagem));
      }
    } finally {
      setSalvando(false);
    }
  };

  return (
    <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">
        <Text style={styles.title}>{fiadoEdicao ? 'Editar Fiado' : 'Novo Fiado'}</Text>

        <Text style={styles.label}>Cliente *</Text>
        <TextInput
          style={[styles.input, erros.nomeCliente ? styles.inputErro : null]}
          value={nomeCliente}
          onChangeText={setNomeCliente}
          placeholder="Nome do cliente"
          placeholderTextColor={colors.textSecondary}
          autoCapitalize="words"
        />
        {erros.nomeCliente ? <Text style={styles.erro}>{erros.nomeCliente}</Text> : null}

        <Text style={styles.label}>Valor (R$) *</Text>
        <TextInput
          style={[styles.input, erros.valor ? styles.inputErro : null]}
          value={valor}
          onChangeText={setValor}
          placeholder="0,00"
          placeholderTextColor={colors.textSecondary}
          keyboardType="decimal-pad"
        />
        {erros.valor ? <Text style={styles.erro}>{erros.valor}</Text> : null}

        <Text style={styles.label}>Data *</Text>
        <TextInput
          style={[styles.input, erros.data ? styles.inputErro : null]}
          value={data}
          onChangeText={(texto) => setData(mascararData(texto))}
          placeholder="DD/MM/AAAA"
          placeholderTextColor={colors.textSecondary}
          keyboardType="number-pad"
          maxLength={10}
        />
        {erros.data ? <Text style={styles.erro}>{erros.data}</Text> : null}

        <Text style={styles.label}>Descrição</Text>
        <TextInput
          style={[styles.input, styles.inputMultilinha]}
          value={descricao}
          onChangeText={setDescricao}
          placeholder="Ex.: 2 kg de arroz e 1 óleo"
          placeholderTextColor={colors.textSecondary}
          multiline
          textAlignVertical="top"
        />

        <Pressable style={[styles.botaoSalvar, salvando ? styles.botaoDesabilitado : null]} onPress={() => void salvar()} disabled={salvando}>
          {salvando ? <ActivityIndicator color={colors.surface} /> : (
            <Text style={styles.textoBotaoSalvar}>{fiadoEdicao ? 'Salvar alterações' : 'Salvar fiado'}</Text>
          )}
        </Pressable>
        <Pressable style={styles.botaoCancelar} onPress={() => navigation.goBack()} disabled={salvando}>
          <Text style={styles.textoBotaoCancelar}>Cancelar</Text>
        </Pressable>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: colors.background },
  container: { padding: 20 },
  title: { fontSize: 22, fontWeight: 'bold', color: colors.text, marginBottom: 12 },
  label: { color: colors.text, fontSize: 15, fontWeight: '600', marginTop: 14, marginBottom: 6 },
  input: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    color: colors.text,
    fontSize: 16,
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  inputErro: { borderColor: colors.danger },
  inputMultilinha: { minHeight: 90 },
  erro: { color: colors.danger, fontSize: 13, marginTop: 4 },
  botaoSalvar: { alignItems: 'center', backgroundColor: colors.primary, borderRadius: 8, marginTop: 24, padding: 14 },
  botaoDesabilitado: { opacity: 0.6 },
  textoBotaoSalvar: { color: colors.surface, fontSize: 16, fontWeight: 'bold' },
  botaoCancelar: { alignItems: 'center', borderColor: colors.border, borderRadius: 8, borderWidth: 1, marginTop: 12, padding: 14 },
  textoBotaoCancelar: { color: colors.textSecondary, fontSize: 16, fontWeight: '600' },
});
