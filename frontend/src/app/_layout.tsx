import React from 'react';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { ActivityIndicator, View } from 'react-native';
import { AuthProvider, useAuth } from '../contexts/AuthContext';
import { colors } from '../theme/colors';

function Navigator() {
  const { token, loading } = useAuth();

  if (loading) {
    return (
      <>
        <StatusBar style="dark" />
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: colors.background }}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      </>
    );
  }

  return (
    <>
      <StatusBar style={token ? 'light' : 'dark'} />
      <Stack screenOptions={{ headerStyle: { backgroundColor: colors.primary }, headerTintColor: colors.surface }}>
        <Stack.Protected guard={!token}>
          <Stack.Screen name="login" options={{ headerShown: false }} />
          <Stack.Screen name="cadastro" options={{ title: 'Criar conta' }} />
        </Stack.Protected>
        <Stack.Protected guard={!!token} redirectTo="/login">
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          <Stack.Screen name="formulario" options={{ title: 'Registrar Fiado' }} />
          <Stack.Screen name="detalhe/[id]" options={{ title: 'Detalhes do Fiado' }} />
        </Stack.Protected>
      </Stack>
    </>
  );
}

export default function RootLayout() {
  return (
    <AuthProvider>
      <Navigator />
    </AuthProvider>
  );
}
