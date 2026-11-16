import React from 'react';
import { Button, StyleSheet, Text, View } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { AppStackParamList } from '../types';
import { colors } from '../theme/colors';

type Props = NativeStackScreenProps<AppStackParamList, 'ListaFiados'>;

export function ListaFiadosScreen({ navigation }: Props) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Lista de Fiados</Text>
      <Text style={styles.subtitle}>Tela base pronta para implementação (Nicolas)</Text>
      <View style={styles.actions}>
        <Button title="Novo Fiado" color={colors.primary} onPress={() => navigation.navigate('FormularioFiado', {})} />
        <Button title="Ver Detalhe (ID 1)" color={colors.primaryDark} onPress={() => navigation.navigate('DetalheFiado', { fiadoId: 1 })} />
        <Button title="Meu Perfil" color={colors.textSecondary} onPress={() => navigation.navigate('Perfil')} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24, backgroundColor: colors.background },
  title: { fontSize: 20, fontWeight: 'bold', color: colors.text, marginBottom: 8 },
  subtitle: { fontSize: 14, color: colors.textSecondary, marginBottom: 20, textAlign: 'center' },
  actions: { gap: 12, width: '100%' },
});