export const formatarMoeda = (valor: number) =>
  new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);

// A API envia "YYYY-MM-DD"; na tela mostramos "DD/MM/AAAA".
export const formatarData = (data: string) => {
  const [ano, mes, dia] = data.split('-');
  return ano && mes && dia ? `${dia}/${mes}/${ano}` : data;
};
