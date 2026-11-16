# Estúdio BDD

Projeto de Programação de Sistemas II, 2026/2. Permite organizar requisitos como histórias de usuário, escrever critérios de aceite em Gherkin, registrar e avaliar sugestões de IA e exportar histórias em arquivos `.feature`.

**Integrante** — Lucas Bittencourt de Oliveira (RA 10409476).

## Executar no Windows

É necessário ter JDK 21, Node.js 22.12 ou superior e PostgreSQL instalado. O Maven Wrapper baixa a versão do Maven usada no projeto na primeira execução.

**Terminal 1 — banco e API.** Abra um PowerShell na raiz. O script usa o PostgreSQL 17 instalado no Windows, prepara um banco local na porta 55432 e define as variáveis da API no terminal. Se o PostgreSQL estiver instalado em outro local, passe `-PgBin` com o caminho da pasta `bin`.

```powershell
.\scripts\banco-local.ps1
Set-Location api
.\mvnw.cmd spring-boot:run
```

**Terminal 2 — SPA.** Abra outro terminal na raiz.

```powershell
Set-Location frontend
npm ci
npm run dev
```

Abra [a tela de login](http://localhost:5173), escolha um usuário de demonstração e clique em **Entrar**. Abra **Portal da Biblioteca**. Com **Reservar um livro disponível**, é possível baixar um `.feature` imediatamente. **Consultar empréstimos ativos** tem uma sugestão pendente para experimentar a curadoria e o bloqueio de exportação.

Para encerrar a SPA e a API, use Ctrl+C nos respectivos terminais. A partir da raiz, rode `.\scripts\banco-local.ps1 -Acao Stop` para parar o banco de demonstração. Os dados ficam em `%LOCALAPPDATA%\EstudioBDD\postgresql` e são reutilizados ao iniciar novamente.

### Usar um PostgreSQL existente

Crie um banco `estudio_bdd` e um usuário dono do banco. No `psql`, um administrador pode usar os comandos abaixo. `\password` solicita a senha de forma interativa.

```sql
CREATE ROLE estudio_bdd LOGIN;
\password estudio_bdd
CREATE DATABASE estudio_bdd OWNER estudio_bdd;
```

Defina `DB_URL` (padrão `jdbc:postgresql://localhost:5432/estudio_bdd`), `DB_USER` (padrão `estudio_bdd`) e `DB_PASSWORD` no ambiente do processo e inicie a API. No Linux ou macOS, a partir de `api/`, use `./mvnw spring-boot:run`. O Hibernate atualiza o esquema na primeira execução. Desative a carga fictícia com `APP_DEMO_DATA=false`.

A API usa a porta 8080 por padrão. A SPA aceita `VITE_API_URL` e o backend aceita `SPA_ORIGIN` se for necessário alterar os endereços. O CORS permite apenas a origem configurada.

### Eclipse

Use **File → Import… → Maven → Existing Maven Projects** e selecione a pasta `api/`. Configure o projeto para JDK 21. A classe principal é `br.edu.mackenzie.estudiobdd.EstudioBddApplication`. Ao executá-la pela IDE, configure as três variáveis `DB_*` na aba **Environment** do perfil de execução. Também é possível editar o código no Eclipse e iniciar os processos pelos terminais acima.

## Login e permissões

O login usa os usuários fictícios do banco. A SPA conserva a escolha no navegador até clicar em **Sair** e envia `X-Usuario-Id` à API. O backend verifica se esse usuário existe e aplica a regra de autoria na história, no cenário e nos passos. A identidade simulada é adequada à demonstração acadêmica e o cabeçalho pode ser trocado por qualquer cliente HTTP.

## Estrutura

| Parte | Função |
| --- | --- |
| `frontend/` | SPA React para login, projetos, histórias, cenários, sugestões, exportação e resumo |
| `api/…/controller` e `dto` | HTTP, validação das entradas, contratos e erros |
| `api/…/service` | Regras de negócio e transações |
| `api/…/repository` | Consultas e persistência Spring Data JPA |
| `api/…/model` | Entidades e enumerações do domínio |

## Testes e materiais

- Na pasta `api/`, rode `mvn test` ou `.\mvnw.cmd test` (Windows). A suíte inclui regras de negócio e uma regra de dependência com ArchUnit.
- Na pasta `frontend/`, rode `npm ci` e `npm run build`.
- [Contrato HTTP](docs/api.md)
- [Diagrama de classes](docs/modelo.md)
- [Prática de BDD](docs/pratica-bdd.md)
- [Registro de interações com IA](docs/registro-ia.md)
- [Roteiro de verificação e resultados](docs/verificacao.md)
- [Exemplo exportado pela API](docs/evidencias/reservar-um-livro-disponivel.feature)

O ZIP é gerado da tag local `entrega-final`. Para entregar no Moodle, envie o ZIP e o link do repositório publicado pelo grupo.
