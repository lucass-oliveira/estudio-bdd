# Registro de interações com IA

Ferramenta usada nas interações abaixo — OpenAI Codex. Os pedidos foram resumidos a partir do trabalho realizado nesta sessão. As decisões registradas são as adotadas na implementação para revisão do grupo.

## API

### 1. Mapear o domínio e delimitar a identidade de demonstração

- **Problema** — transformar as regras do enunciado em operações HTTP e acomodar a tela de login solicitada em aula.
- **Prompt** — implemente o backend com Spring Boot, JPA e PostgreSQL. Use `GET /api/usuarios` para a seleção e `X-Usuario-Id` para a identidade simulada. Envie antes o contrato para a SPA.
- **Proposta** — rascunhar as rotas e os DTOs, manter as coleções no domínio com associações JPA e centralizar a verificação de autor no serviço.
- **Decisão e mudança** — usar o contrato com recursos de projeto, história, cenário, passo e sugestão. Carregar os usuários e projetos fictícios na primeira execução.
- **Verificação** — roteiro da API para identidade ausente ou desconhecida, autoria e chamadas pelo navegador.

### 2. Preservar a autoria na exclusão do projeto

- **Problema** — remover um projeto em cascata poderia apagar histórias de outro autor.
- **Prompt** — revise a autoria em histórias e cenários e confirme quem pode excluir um projeto.
- **Proposta** — permitir a exclusão se o projeto estiver vazio ou se todas as histórias forem do usuário atual. Serializar as alterações do projeto no banco.
- **Decisão e mudança** — o serviço de projeto verifica todos os autores dentro da transação, antes de remover o projeto. O roteiro inclui tentativas de alteração e exclusão por um segundo usuário.
- **Verificação** — executar a exclusão com ambas as identidades e conferir o HTTP 403 para quem não tem autoria.

### 3. Cobrir regras de negócio e dependências entre camadas

- **Problema** — validar as regras obrigatórias e manter o acesso a dados nos repositórios.
- **Prompt** — escreva os testes obrigatórios da API e a regra ArchUnit, usando as regras reais de cenário, sugestão e exportação.
- **Proposta** — testar ordem e tipos dos passos, decisões sem motivo, sugestões já decididas e exportação com pendências. Inspecionar as dependências dos controllers com ArchUnit.
- **Decisão e mudança** — manter a suíte executável por Maven, com casos válidos e inválidos, incluindo cenário sem Dado e Gherkin localizado.
- **Verificação** — executar `mvn test` e o roteiro HTTP contra PostgreSQL.

## SPA

### 4. Implementar o login da demonstração

- **Problema** — o professor exigiu uma tela de login e o enunciado já previa selecionar um usuário fictício.
- **Prompt** — crie a SPA React com seleção carregada da API, botão Entrar, nome ativo, ação Sair e envio de `X-Usuario-Id`.
- **Proposta** — iniciar pelo formulário de seleção do usuário, manter a escolha durante a sessão da aba e carregar os projetos após a entrada.
- **Decisão e mudança** — usar a tela de login como porta de entrada. O login repassa a identidade às chamadas, e o logout limpa a escolha.
- **Verificação** — entrar com uma conta, recarregar a aba, sair e entrar com a outra conta.

### 5. Aproximar história e sugestão na tela

- **Problema** — para decidir uma sugestão, o analista precisa comparar seu conteúdo com a narrativa e os cenários.
- **Prompt** — revise se narrativa e sugestões podem ser lidas lado a lado no desktop e se continuam editáveis em uma tela de 390 px.
- **Proposta** — colocar narrativa e cenários em uma coluna, sugestões na coluna ao lado e reorganizar os painéis em uma coluna em telas estreitas.
- **Decisão e mudança** — adotar a composição em colunas com formulários em caixas de diálogo. A revisão em 390 px levou a ampliar o seletor de tipo do passo para que o nome Quando ficasse legível.
- **Verificação** — abrir a história e o editor de cenário no desktop e em 390 px. Durante um teste no Chrome, os painéis e o editor couberam sem rolagem horizontal na página.

### 6. Usar os erros da API na revisão e na exportação

- **Problema** — respeitar a autoria nos controles de edição e comunicar por que o backend bloqueia uma operação.
- **Prompt** — confira as ações por autoria, a apresentação de `ProblemDetail` nos formulários e o bloqueio de exportação com pendências.
- **Proposta** — mostrar ações de alteração para o autor, liberar registro e decisão de sugestões para os usuários de demonstração e exibir o detalhe vindo da API.
- **Decisão e mudança** — a SPA usa a identidade para decidir quais controles aparecem. O botão Exportar envia a requisição à API, que revalida as pendências e devolve a mensagem ou o arquivo.
- **Verificação** — com a API real, a exportação bloqueou com HTTP 409 antes da decisão e baixou o arquivo após o aceite. Com outro usuário, os botões de edição ficaram indisponíveis e o registro de sugestões permaneceu acessível. Um teste com resposta simulada HTTP 400 confirmou que a mensagem do campo aparece no formulário.
