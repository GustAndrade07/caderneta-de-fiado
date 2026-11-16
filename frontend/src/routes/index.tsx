import React from 'react';
import { ActivityIndicator, View } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { useAuth } from '../contexts/AuthContext';
import { AuthRoutes } from './AuthRoutes';
import { AppRoutes } from './AppRoutes';
import { colors } from '../theme/colors';

export function Routes() {
  const { token, loading } = useAuth();

  if (loading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: colors.background }}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  return (
    <NavigationContainer>
      {token ? <AppRoutes /> : <AuthRoutes />}
    </NavigationContainer>
  );
}