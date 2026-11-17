import React, { useCallback, useState } from 'react';
import { ActivityIndicator, Alert, Pressable, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from 'expo-router';
import { useAuth } from '../contexts/AuthContext';
import { authService } from '../services/authService';
import { Usuario } from '../types';
import { colors } from '../theme/colors';

export function PerfilScreen() {
  const { signOut } = useAuth();
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(false);

  const carregarPerfil = useCallback(async () => {
    setCarregando(true);
    setErro(false);
    try {
      setUsuario(await authService.me());
    } catch {
      // 401 já é tratado pelo interceptador do axios (apaga o token e volta ao login).
      setErro(true);
    } finally {
      setCarregando(false);
    }
  }, []);

  useFocusEffect(useCallback(() => { void carregarPerfil(); }, [carregarPerfil]));

  // O logout apaga o token do SecureStore e zera o estado do login: o app volta para a tela de Login.
  const confirmarSaida = () => Alert.alert(
    'Sair da conta',
    'Deseja realmente sair?',
    [{ text: 'Cancelar', style: 'cancel' }, { text: 'Sair', style: 'destructive', onPress: () => void signOut() }],
  );

  const iniciais = usuario?.nome
    ? usuario.nome.trim().split(/\s+/).slice(0, 2).map((parte) => parte[0]?.toUpperCase() ?? '').join('')
    : '';

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Perfil do Comerciante</Text>

      {carregando ? <ActivityIndicator style={styles.carregando} size="large" color={colors.primary} /> : null}

      {!carregando && erro ? (
        <View style={styles.cartao}>
          <Text style={styles.erro}>Não foi possível carregar os dados do perfil.</Text>
          <Pressable style={styles.botaoSecundario} onPress={() => void carregarPerfil()}>
            <Text style={styles.textoBotaoSecundario}>Tentar novamente</Text>
          </Pressable>
        </View>
      ) : null}

      {!carregando && usuario ? (
        <View style={styles.cartao}>
          <View style={styles.avatar}><Text style={styles.avatarTexto}>{iniciais}</Text></View>
          <Text style={styles.rotulo}>Nome</Text>
          <Text style={styles.valor}>{usuario.nome}</Text>
          <Text style={styles.rotulo}>E-mail</Text>
          <Text style={styles.valor}>{usuario.email}</Text>
        </View>
      ) : null}

      <Pressable style={styles.botaoSair} onPress={confirmarSaida}>
        <Text style={styles.textoBotaoSair}>Sair da conta</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: colors.background },
  title: { fontSize: 22, fontWeight: 'bold', color: colors.text, marginBottom: 16 },
  carregando: { marginVertical: 32 },
  cartao: { backgroundColor: colors.surface, borderColor: colors.border, borderRadius: 12, borderWidth: 1, padding: 20 },
  avatar: { alignItems: 'center', alignSelf: 'center', backgroundColor: colors.primary, borderRadius: 36, height: 72, justifyContent: 'center', marginBottom: 16, width: 72 },
  avatarTexto: { color: colors.surface, fontSize: 26, fontWeight: 'bold' },
  rotulo: { color: colors.textSecondary, fontSize: 13, marginTop: 10 },
  valor: { color: colors.text, fontSize: 17, fontWeight: '600', marginTop: 2 },
  erro: { color: colors.danger, fontSize: 15, textAlign: 'center' },
  botaoSecundario: { alignItems: 'center', borderColor: colors.primary, borderRadius: 8, borderWidth: 1, marginTop: 14, padding: 10 },
  textoBotaoSecundario: { color: colors.primary, fontWeight: 'bold' },
  botaoSair: { alignItems: 'center', backgroundColor: colors.danger, borderRadius: 8, marginTop: 24, padding: 14 },
  textoBotaoSair: { color: colors.surface, fontSize: 16, fontWeight: 'bold' },
});
