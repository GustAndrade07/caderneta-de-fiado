import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useLocalSearchParams } from 'expo-router';
import { colors } from '../theme/colors';

export function DetalheFiadoScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Detalhe do Fiado #{id}</Text>
      <Text style={styles.subtitle}>Tela base pronta para implementação (Nicolas)</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24, backgroundColor: colors.background },
  title: { fontSize: 20, fontWeight: 'bold', color: colors.text, marginBottom: 8 },
  subtitle: { fontSize: 14, color: colors.textSecondary, textAlign: 'center' },
});
