import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AppStackParamList } from '../types';
import { colors } from '../theme/colors';

type Props = NativeStackScreenProps<AppStackParamList, 'FormularioFiado'>;

export function FormularioFiadoScreen({ route }: Props) {
  const fiadoEdicao = route.params?.fiado;

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{fiadoEdicao ? 'Editar Fiado' : 'Novo Fiado'}</Text>
      <Text style={styles.subtitle}>Tela base pronta para implementação (Eduardo)</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24, backgroundColor: colors.background },
  title: { fontSize: 20, fontWeight: 'bold', color: colors.text, marginBottom: 8 },
  subtitle: { fontSize: 14, color: colors.textSecondary, textAlign: 'center' },
});