import React from 'react';
import { Button, StyleSheet, Text, View } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AuthStackParamList } from '../types';
import { colors } from '../theme/colors';

type Props = NativeStackScreenProps<AuthStackParamList, 'Cadastro'>;

export function CadastroScreen({ navigation }: Props) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Cadastro de Comerciante</Text>
      <Text style={styles.subtitle}>Tela base pronta para implementação (Vinicius)</Text>
      <Button
        title="Voltar para Login"
        color={colors.primary}
        onPress={() => navigation.goBack()}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24, backgroundColor: colors.background },
  title: { fontSize: 20, fontWeight: 'bold', color: colors.text, marginBottom: 8 },
  subtitle: { fontSize: 14, color: colors.textSecondary, marginBottom: 20, textAlign: 'center' },
});