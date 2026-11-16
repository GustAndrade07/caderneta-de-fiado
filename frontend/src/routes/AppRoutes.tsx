import React from 'react';
import { Pressable, Text } from 'react-native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { AppStackParamList } from '../types';
import { ListaFiadosScreen } from '../screens/ListaFiadosScreen';
import { DetalheFiadoScreen } from '../screens/DetalheFiadoScreen';
import { FormularioFiadoScreen } from '../screens/FormularioFiadoScreen';
import { PerfilScreen } from '../screens/PerfilScreen';
import { colors } from '../theme/colors';

const Stack = createNativeStackNavigator<AppStackParamList>();

export function AppRoutes() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: colors.primary },
        headerTintColor: colors.surface,
        headerTitleStyle: { fontWeight: 'bold' },
      }}
    >
      <Stack.Screen
        name="ListaFiados"
        component={ListaFiadosScreen}
        options={({ navigation }) => ({
          title: 'Caderneta de Fiado',
          headerRight: () => (
            <Pressable onPress={() => navigation.navigate('Perfil')} hitSlop={8}>
              <Text style={{ color: colors.surface, fontSize: 16, fontWeight: 'bold' }}>Perfil</Text>
            </Pressable>
          ),
        })}
      />
      <Stack.Screen name="DetalheFiado" component={DetalheFiadoScreen} options={{ title: 'Detalhes do Fiado' }} />
      <Stack.Screen name="FormularioFiado" component={FormularioFiadoScreen} options={{ title: 'Registrar Fiado' }} />
      <Stack.Screen name="Perfil" component={PerfilScreen} options={{ title: 'Meu Perfil' }} />
    </Stack.Navigator>
  );
}