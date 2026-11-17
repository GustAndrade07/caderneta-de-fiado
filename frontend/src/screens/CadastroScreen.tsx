import React, { useState } from 'react';
import { useRouter } from 'expo-router';
import { ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { useAuth } from '../contexts/AuthContext';
import { authErrorMessage } from '../services/authErrors';
import { authService } from '../services/authService';
import { EMAIL_FORMAT_MESSAGE, isValidEmail } from '../services/emailValidation';
import { colors } from '../theme/colors';

export function CadastroScreen() {
  const router = useRouter();
  const { signIn } = useAuth();
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [erro, setErro] = useState('');
  const [enviando, setEnviando] = useState(false);

  async function cadastrar() {
    if (enviando) return;
    const nomeNormalizado = nome.trim();
    const emailNormalizado = email.trim().toLowerCase();
    if (nomeNormalizado.length < 2) {
      setErro('Informe um nome com pelo menos 2 caracteres.');
      return;
    }
    if (!emailNormalizado) {
      setErro('Informe o e-mail.');
      return;
    }
    if (!isValidEmail(emailNormalizado)) {
      setErro(EMAIL_FORMAT_MESSAGE);
      return;
    }
    if (senha.length < 8) {
      setErro('A senha deve ter pelo menos 8 caracteres.');
      return;
    }
    if (senha !== confirmacao) {
      setErro('As senhas não conferem.');
      return;
    }

    setErro('');
    setEnviando(true);
    let contaCriada = false;
    try {
      await authService.cadastrar({ nome: nomeNormalizado, email: emailNormalizado, senha });
      contaCriada = true;
      await signIn(emailNormalizado, senha);
    } catch (error) {
      if (contaCriada) {
        setErro('Conta criada, mas não foi possível entrar automaticamente. Volte ao login.');
      } else {
        setErro(authErrorMessage(error));
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <KeyboardAvoidingView style={styles.container} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <ScrollView contentContainerStyle={styles.conteudo} keyboardShouldPersistTaps="handled">
        <View style={styles.formulario}>
          <Text style={styles.titulo}>Criar conta</Text>
          <Text style={styles.subtitulo}>Comece a organizar os fiados do seu comércio.</Text>

          <Text style={styles.label}>Nome</Text>
          <TextInput style={styles.campo} value={nome} onChangeText={setNome} placeholder="Seu nome" autoComplete="name" textContentType="name" editable={!enviando} accessibilityLabel="Nome" />

          <Text style={styles.label}>E-mail</Text>
          <TextInput style={styles.campo} value={email} onChangeText={setEmail} placeholder="nome@dominio.com" keyboardType="email-address" autoCapitalize="none" autoComplete="email" textContentType="emailAddress" editable={!enviando} accessibilityLabel="E-mail" />

          <Text style={styles.label}>Senha</Text>
          <TextInput style={styles.campo} value={senha} onChangeText={setSenha} placeholder="Pelo menos 8 caracteres" secureTextEntry autoComplete="new-password" textContentType="newPassword" editable={!enviando} accessibilityLabel="Senha" />

          <Text style={styles.label}>Confirmar senha</Text>
          <TextInput style={styles.campo} value={confirmacao} onChangeText={setConfirmacao} placeholder="Digite a senha novamente" secureTextEntry autoComplete="new-password" textContentType="newPassword" editable={!enviando} accessibilityLabel="Confirmar senha" onSubmitEditing={() => void cadastrar()} />

          {erro ? <Text style={styles.erro} accessibilityRole="alert">{erro}</Text> : null}

          <Pressable style={[styles.botao, enviando && styles.botaoDesabilitado]} onPress={() => void cadastrar()} disabled={enviando} accessibilityRole="button">
            {enviando ? <ActivityIndicator color={colors.surface} /> : <Text style={styles.botaoTexto}>Criar conta</Text>}
          </Pressable>

          <Pressable onPress={() => router.replace('/login')} disabled={enviando} accessibilityRole="button">
            <Text style={styles.link}>Já tem conta? Entrar</Text>
          </Pressable>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  conteudo: { flexGrow: 1, justifyContent: 'center', padding: 24 },
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
