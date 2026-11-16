export const priorities = { ALTA: 'Alta', MEDIA: 'Média', BAIXA: 'Baixa' };
export const origins = { HUMANO: 'Humano', SUGESTAO_IA: 'Sugestão de IA' };
export const stepTypes = { DADO: 'Dado', QUANDO: 'Quando', ENTAO: 'Então', E: 'E', MAS: 'Mas' };
export const decisions = { PENDENTE: 'Pendente', ACEITA: 'Aceita', REJEITADA: 'Rejeitada' };
export const reasons = {
  INCORRETA: 'Incorreta', GENERICA: 'Genérica', DUPLICADA: 'Duplicada',
  FORA_DO_ESCOPO: 'Fora do escopo', OUTRO: 'Outro',
};

export function initials(name) {
  return name.trim().split(/\s+/).slice(0, 2).map(word => word[0].toUpperCase()).join('');
}

export function formatDate(value) {
  return value ? new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' }).format(new Date(value)) : '';
}
