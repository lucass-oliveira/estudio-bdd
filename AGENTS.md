# Estúdio BDD

- Use Java 21, Spring Boot, Spring Data JPA e PostgreSQL em `api/`. Use React e Vite em `frontend/`.
- Mantenha as camadas SPA → controller → serviço → repositório, com entidades e enums no domínio. Injete dependências pelo construtor.
- Controllers recebem e devolvem DTOs e delegam as regras de negócio. Serviços controlam transações, autoria, ordenação, decisões e exportação. Repositórios concentram o acesso JPA.
- Preserve as restrições de unicidade e as chaves estrangeiras. Use associações lazy e carregue os dados necessários dentro da transação.
- Valide entradas dos DTOs com Bean Validation e use ProblemDetail para erros HTTP. Mantenha mensagens voltadas ao usuário em português.
- A identidade da atividade é simulada pelo cabeçalho `X-Usuario-Id`. Mantenha a tela de login de demonstração e verifique autoria no backend.
- Após alterar uma regra, execute `mvn test` dentro de `api/` ou `./mvnw test` com o Maven Wrapper. No Windows, use `mvnw.cmd test`.
- Em `frontend/`, use `npm ci`, `npm run dev` e `npm run build`. Após alterações no contrato, atualize a SPA, a documentação e o roteiro de verificação correspondente.
- Em exemplos, testes e evidências, use dados fictícios. Mantenha senhas, `node_modules/`, `target/` e `dist/` fora do repositório e do ZIP.
