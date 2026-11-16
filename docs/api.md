# API REST

URL padrão `http://localhost:8080/api`. Entradas e saídas usam JSON, com exceção do download de Gherkin. Envie `Content-Type: application/json` nos pedidos com corpo. As rotas de projetos, histórias, cenários, passos e sugestões exigem um usuário existente em `X-Usuario-Id`. Consulte `GET /usuarios` para identificar os usuários de demonstração.

## Rotas

| Método | Rota relativa a `/api` | Resultado |
| --- | --- | --- |
| GET | `/usuarios` | Lista `id`, `nome` e `email` |
| POST | `/usuarios` | Cadastra com `nome` e `email` |
| GET | `/projetos` | Lista os projetos |
| POST | `/projetos` | Cadastra um projeto |
| GET | `/projetos/{projetoId}` | Detalha o projeto e suas histórias com contagens de cenários e sugestões pendentes. Aceita `?prioridade=ALTA` |
| PUT | `/projetos/{projetoId}` | Atualiza o projeto |
| DELETE | `/projetos/{projetoId}` | Exclui um projeto vazio ou cujas histórias são todas do usuário atual |
| GET | `/projetos/{projetoId}/resumo` | Contagem de histórias, cenários por origem, sugestões por decisão e motivos de rejeição por frequência |
| POST | `/projetos/{projetoId}/historias` | Cadastra uma história com o usuário atual como autor |
| GET | `/historias/{historiaId}` | Detalha a história e seus cenários |
| PUT | `/historias/{historiaId}` | Atualiza a história, pelo autor |
| DELETE | `/historias/{historiaId}` | Exclui a história, pelo autor |
| GET | `/historias/{historiaId}/cenarios` | Lista os cenários e seus passos em ordem |
| POST | `/historias/{historiaId}/cenarios` | Cadastra um cenário com seus passos, pelo autor |
| GET | `/cenarios/{cenarioId}` | Detalha o cenário e seus passos |
| PUT | `/cenarios/{cenarioId}` | Atualiza o cenário com seus passos, pelo autor |
| DELETE | `/cenarios/{cenarioId}` | Exclui o cenário e seus passos, pelo autor |
| GET | `/cenarios/{cenarioId}/passos` | Lista os passos em ordem |
| POST | `/cenarios/{cenarioId}/passos` | Insere um passo, pelo autor |
| GET | `/cenarios/{cenarioId}/passos/{passoId}` | Detalha um passo |
| PUT | `/cenarios/{cenarioId}/passos/{passoId}` | Altera um passo, pelo autor |
| DELETE | `/cenarios/{cenarioId}/passos/{passoId}` | Remove um passo, pelo autor |
| GET | `/historias/{historiaId}/sugestoes` | Lista as sugestões. Aceita `?decisao=PENDENTE` |
| POST | `/historias/{historiaId}/sugestoes` | Registra uma sugestão pendente com o usuário atual |
| PUT | `/sugestoes/{sugestaoId}/decisao` | Aceita ou rejeita uma sugestão pendente, com justificativa |
| GET | `/historias/{historiaId}/feature` | Baixa um `.feature` em português se a história estiver apta |

## Corpos dos pedidos

**Projeto**

```json
{
  "nome": "Portal da Biblioteca",
  "descricao": "Sistema de empréstimos e reservas",
  "contextoDominio": "Um exemplar pode estar disponível ou emprestado."
}
```

**História**

```json
{
  "titulo": "Reservar um livro disponível",
  "papel": "leitora",
  "objetivo": "reservar um exemplar disponível",
  "beneficio": "retirar o livro mais tarde",
  "prioridade": "ALTA"
}
```

`prioridade` aceita `ALTA`, `MEDIA` e `BAIXA`.

**Cenário**

```json
{
  "titulo": "Reserva confirmada",
  "origem": "HUMANO",
  "passos": [
    {"tipo": "DADO", "texto": "um exemplar disponível"},
    {"tipo": "QUANDO", "texto": "a leitora solicita uma reserva"},
    {"tipo": "ENTAO", "texto": "o exemplar é reservado para a leitora"}
  ]
}
```

`origem` aceita `HUMANO` e `SUGESTAO_IA`. A lista define a ordem dos passos. Ao editar, envie o `id` dos passos que devem manter sua identidade. Use `posicao` no corpo do cenário para alterar sua posição na história. Para cadastrar ou editar um passo diretamente, envie `{"tipo":"E","texto":"a reserva aparece no histórico","posicao":4}`. As posições começam em 1 e cada alteração passa pela validação do cenário completo.

**Sugestão**

```json
{
  "ferramenta": "Assistente de demonstração",
  "prompt": "Sugira um cenário de falha para a reserva de um exemplar.",
  "conteudo": "Considerar a reserva de um exemplar emprestado."
}
```

**Decisão**

```json
{
  "decisao": "REJEITADA",
  "motivoRejeicao": "DUPLICADA",
  "justificativa": "O empréstimo de exemplar já está coberto por um cenário."
}
```

`decisao` aceita `ACEITA` e `REJEITADA`. Na rejeição, `motivoRejeicao` aceita `INCORRETA`, `GENERICA`, `DUPLICADA`, `FORA_DO_ESCOPO` e `OUTRO`. Na aceitação, omita `motivoRejeicao`. Os cenários são ajustados individualmente pelo autor.

## Respostas

Cadastros retornam `201`. Consultas e atualizações retornam `200`. Exclusões retornam `204`. Erros usam `application/problem+json` e incluem `status`, `title` e `detail`. A propriedade `erros` identifica campos inválidos quando aplicável.

| Status | Significado |
| --- | --- |
| `400` | Corpo, campo ou parâmetro inválido |
| `401` | Identidade de demonstração ausente ou desconhecida |
| `403` | Usuário atual sem permissão de autoria |
| `404` | Registro não encontrado |
| `409` | Duplicidade, sugestão já decidida ou exportação bloqueada |
| `422` | Regra de cenário ou decisão violada |

O arquivo de exportação inclui `# language: pt` e o nome no cabeçalho `Content-Disposition`.
