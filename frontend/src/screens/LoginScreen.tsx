import React, { useState } from 'react';
import { useRouter } from 'expo-router';
import { ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { useAuth } from '../contexts/AuthContext';
import { authErrorMessage } from '../services/authErrors';
import { EMAIL_FORMAT_MESSAGE, isValidEmail } from '../services/emailValidation';
import { colors } from '../theme/colors';

export function LoginScreen() {
  const router = useRouter();
  const { signIn } = useAuth();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState('');
  const [enviando, setEnviando] = useState(false);

  async function entrar() {
    if (enviando) return;
    const emailNormalizado = email.trim().toLowerCase();
    if (!emailNormalizado) {
      setErro('Informe o e-mail.');
      return;
    }
    if (!isValidEmail(emailNormalizado)) {
      setErro(EMAIL_FORMAT_MESSAGE);
      return;
    }
    if (!senha) {
      setErro('Informe a senha.');
      return;
    }

    setErro('');
    setEnviando(true);
    try {
      await signIn(emailNormalizado, senha);
    } catch (error) {
      setErro(authErrorMessage(error));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <KeyboardAvoidingView style={styles.container} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <View style={styles.formulario}>
        <Text style={styles.titulo}>Caderneta de Fiado</Text>
        <Text style={styles.subtitulo}>Entre para acessar seus fiados.</Text>

        <Text style={styles.label}>E-mail</Text>
        <TextInput style={styles.campo} value={email} onChangeText={setEmail} placeholder="nome@dominio.com" keyboardType="email-address" autoCapitalize="none" autoComplete="email" textContentType="username" editable={!enviando} accessibilityLabel="E-mail" />

        <Text style={styles.label}>Senha</Text>
        <TextInput style={styles.campo} value={senha} onChangeText={setSenha} placeholder="Sua senha" secureTextEntry autoComplete="current-password" textContentType="password" editable={!enviando} accessibilityLabel="Senha" onSubmitEditing={() => void entrar()} />

        {erro ? <Text style={styles.erro} accessibilityRole="alert">{erro}</Text> : null}

        <Pressable style={[styles.botao, enviando && styles.botaoDesabilitado]} onPress={() => void entrar()} disabled={enviando} accessibilityRole="button">
          {enviando ? <ActivityIndicator color={colors.surface} /> : <Text style={styles.botaoTexto}>Entrar</Text>}
        </Pressable>

        <Pressable onPress={() => router.push('/cadastro')} disabled={enviando} accessibilityRole="button">
          <Text style={styles.link}>Não tem conta? Cadastre-se</Text>
        </Pressable>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', padding: 24, backgroundColor: colors.background },
  formulario: { width: '100%', maxWidth: 420, alignSelf: 'center' },
  titulo: { fontSize: 28, fontWeight: 'bold', color: colors.text, marginBottom: 8 },
  subtitulo: { fontSize: 16, color: colors.textSecondary, marginBottom: 28 },
  label: { fontSize: 15, fontWeight: '600', color: colors.text, marginBottom: 8 },
  campo: { backgroundColor: colors.surface, borderColor: colors.border, borderWidth: 1, borderRadius: 8, paddingHorizontal: 14, minHeight: 48, fontSize: 16, color: colors.text, marginBottom: 18 },
  erro: { color: colors.danger, marginBottom: 16, fontSize: 14 },
  botao: { minHeight: 48, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.primary, borderRadius: 8, marginTop: 4 },
  botaoDesabilitado: { opacity: 0.7 },
  botaoTexto: { color: colors.surface, fontSize: 16, fontWeight: 'bold' },
  link: { color: colors.primaryDark, fontSize: 15, textAlign: 'center', marginTop: 24 },
});
