import React, { useEffect, useState } from 'react';
import { ActivityIndicator, Button, StyleSheet, Text, View } from 'react-native';
import { useAuth } from '../contexts/AuthContext';
import { authService } from '../services/authService';
import { colors } from '../theme/colors';
import { Usuario } from '../types';

export function PerfilScreen() {
  const { signOut } = useAuth();
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [erro, setErro] = useState('');

  useEffect(() => {
    let active = true;
    authService.me()
      .then((dados) => { if (active) setUsuario(dados); })
      .catch(() => { if (active) setErro('Não foi possível carregar o perfil.'); });
    return () => { active = false; };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Perfil do Comerciante</Text>
      {usuario ? (
        <View style={styles.dados}>
          <Text style={styles.nome}>{usuario.nome}</Text>
          <Text style={styles.subtitle}>{usuario.email}</Text>
        </View>
      ) : erro ? <Text style={styles.subtitle}>{erro}</Text> : <ActivityIndicator color={colors.primary} />}
      <Button title="Sair da conta" color={colors.danger} onPress={() => void signOut()} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24, backgroundColor: colors.background },
  title: { fontSize: 20, fontWeight: 'bold', color: colors.text, marginBottom: 8 },
  subtitle: { fontSize: 14, color: colors.textSecondary, marginBottom: 20, textAlign: 'center' },
  dados: { alignItems: 'center', marginBottom: 16 },
  nome: { fontSize: 18, fontWeight: 'bold', color: colors.text, marginBottom: 4 },
});
