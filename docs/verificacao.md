# Verificação do Estúdio BDD

Execução local em 16/11/2026 com Java 21, Maven 3.9.12, PostgreSQL 17.10, Node.js 24 e Chrome 157.

## Como reproduzir

1. Siga o [README](../README.md) para iniciar o banco, a API e a SPA. Mantenha a carga fictícia habilitada.
2. Em `api/`, rode `.\mvnw.cmd test`. Em `frontend/`, rode `npm ci` e `npm run build`.
3. Na raiz, rode `node scripts/verificar-api.mjs --evidencias docs/evidencias`. O roteiro usa projetos temporários, confere entradas e respostas HTTP e faz o download da história de demonstração.
4. Para testar persistência, rode `node scripts/verificar-api.mjs --preparar-reinicio "$env:TEMP/estudio-bdd-persistencia.json"`. Pare a API e o banco. Inicie o banco e a API novamente e rode `node scripts/verificar-api.mjs --confirmar-reinicio "$env:TEMP/estudio-bdd-persistencia.json"`. O roteiro confere os IDs, valores e posições antes de apagar os dados do teste.
5. No navegador, entre como Ana Martins e crie um projeto, uma história e um cenário com Dado, Quando e Então. Registre uma sugestão e tente exportar. Aceite a sugestão, tente novamente e abra o arquivo baixado. Consulte o resumo e use os filtros de prioridade. Saia, entre como Bruno Lima e abra a mesma história para comparar as ações disponíveis.

## Testes automatizados

Maven executou **16 testes, 0 falhas**, incluindo a regra ArchUnit. O build da SPA pelo Vite terminou com sucesso.

## API e PostgreSQL

| Entrada | Esperado | Observado |
| --- | --- | --- |
| Acesso sem identidade ou com usuário inexistente | HTTP 401 nos dois pedidos | HTTP 401 nos dois pedidos |
| CORS de localhost e de uma origem diferente | Permitir apenas a SPA | Cabeçalho para a SPA e HTTP 403 para outra origem |
| Cadastrar, editar e repetir o nome de um projeto | HTTP 201, 200 e 409 | HTTP 201, 200 e 409 |
| História válida, título duplicado e benefício vazio | HTTP 201, 409 e 400 | HTTP 201, 409 e 400 |
| Exportação sem cenários | HTTP 409 | HTTP 409 |
| Cenário válido, título duplicado e quatro sequências inválidas | HTTP 201, 409 e quatro HTTP 422 | HTTP 201, 409 e quatro HTTP 422 |
| Inserir um passo E, mover da posição 4 para 3, consultar e excluir | Dado, Quando, E, Então com mesmo ID do passo E | Ordem, ID e texto conferidos, HTTP 404 após a exclusão |
| Mover o segundo cenário para a posição 1 e reordenar seus passos no mesmo PUT | IDs preservados, ordem nova persistida e posição restante renumerada após exclusão | Ordem dos cenários, IDs dos passos e posição restante conferidos |
| Outro usuário tenta editar e excluir história e cenário ou excluir o projeto | Cinco HTTP 403 | Cinco HTTP 403 |
| Alterar a prioridade da história e consultar pelos dois filtros | História apenas em ALTA e um cenário | Uma história em ALTA com um cenário, nenhuma em MEDIA |
| Registrar e filtrar pendente, rejeitar sem motivo e com motivo, tentar rediscutir | Pendente, HTTP 422, REJEITADA e HTTP 409 | Estados e códigos conferidos, com decisora registrada |
| Exportação com sugestão pendente | HTTP 409 | HTTP 409 |
| Aceitar sugestão, consultar cenários e ajustar a origem manualmente | ACEITA e cenário preservado até o ajuste manual | ACEITA, mesmos IDs, origem atualizada no PUT |
| Resumo após cadastrar e decidir as sugestões | 1 história, 1 cenário de IA, 1 aceita, 1 rejeitada e 1 DUPLICADA | Contagens e classificações conferidas |
| Exportação após as decisões | Arquivo .feature localizado com passos ordenados | HTTP 200, anexo e palavras-chave Gherkin em português |
| Excluir cenário e história como autor | Dois HTTP 204 e consultas seguintes HTTP 404 | Dois HTTP 204 e dois HTTP 404 |
| Persistência de história, cenário e passos após parar e iniciar API e PostgreSQL | Mesmos IDs, conteúdo e ordem | A API recuperou a história e os mesmos passos e posições |

## Navegador

| Entrada | Esperado | Observado |
| --- | --- | --- |
| Login como Ana, recarregar a aba e clicar em Sair | Manter a identidade na aba e voltar ao login após sair | Sessão preservada ao recarregar e encerrada ao sair |
| Criar projeto, história e cenário com três passos | Exibir o cenário dentro da história | Projeto, história e cenário exibidos após as chamadas à API |
| Registrar sugestão e tentar exportar | Mostrar o motivo do bloqueio | HTTP 409 com a mensagem da API exibida na tela |
| Aceitar a sugestão, exportar e abrir o arquivo | Baixar Gherkin em português | Download de `consultar-o-prazo-de-retirada.feature` com o cabeçalho `# language: pt` e o cenário criado |
| Abrir resumo e consultar pelas prioridades Média e Alta | Uma sugestão aceita e história apenas em Alta | Resumo e filtro conferidos |
| Acessar a história de Ana como Bruno | Liberar curadoria e omitir edição da história e do cenário | Registro de sugestão disponível e controles de edição ausentes |
| Abrir a história e o editor com janela de 390 px | Painéis e formulário dentro da largura da tela | Sem rolagem horizontal na página |

- [Captura da tela inicial](evidencias/login.png)
- [Captura da história com cenário e sugestão decidida](evidencias/historia-e-sugestoes.png)
- [Arquivo exportado da história fictícia](evidencias/reservar-um-livro-disponivel.feature). O parser do Cucumber reconheceu o dialeto `pt` e compilou dois cenários.
