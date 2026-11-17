import { Alert, Platform } from 'react-native';

// No navegador o Alert.alert do react-native-web não mostra botões, então usamos window.confirm.
export function confirmar(titulo: string, mensagem: string, textoConfirmar: string, aoConfirmar: () => void) {
  if (Platform.OS === 'web') {
    if (window.confirm(`${titulo}\n\n${mensagem}`)) aoConfirmar();
    return;
  }
  Alert.alert(titulo, mensagem, [
    { text: 'Cancelar', style: 'cancel' },
    { text: textoConfirmar, style: 'destructive', onPress: aoConfirmar },
  ]);
}

export function avisar(titulo: string, mensagem: string) {
  if (Platform.OS === 'web') {
    window.alert(`${titulo}\n\n${mensagem}`);
    return;
  }
  Alert.alert(titulo, mensagem);
}
