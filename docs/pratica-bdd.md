# Prática inicial de BDD

A atividade inicial da seção 5 do enunciado pede uma história e três cenários escritos pelo grupo antes de consultar IA. A apresentação de aula sugere um exemplo de sucesso e dois de falha. A parte a seguir é reservada para esse trabalho do grupo.

## Registro do grupo

### Trabalho individual

- **Integrante:** Lucas Bittencourt de Oliveira — RA 10409476.
- **Regra escolhida:** validação da sequência de passos de um cenário. O cenário precisa ter pelo menos um `Quando` e um `Então`, respeitar a ordem `Dado`, `Quando`, `Então`, e não pode começar com `E` ou `Mas`.

### História de usuário

> Como analista de requisitos, quero cadastrar cenários com passos válidos, para documentar critérios de aceite que possam ser compreendidos e testados.

### Cenários definidos manualmente

#### Sucesso: cenário completo

```gherkin
Cenário: Cadastro de cenário válido
  Dado que estou cadastrando a história "Reservar um livro disponível"
  Quando informo os passos Dado, Quando e Então nessa ordem
  Então o cenário é aceito e preserva a ordem dos passos
```

#### Falha: ausência de resultado esperado

```gherkin
Cenário: Cadastro sem Então
  Dado que estou cadastrando a história "Reservar um livro disponível"
  Quando informo somente os passos Dado e Quando
  Então o cadastro é rejeitado porque falta o resultado esperado
```

#### Falha: continuação sem passo anterior

```gherkin
Cenário: Cadastro iniciado com Mas
  Dado que estou cadastrando a história "Reservar um livro disponível"
  Quando informo os passos Mas, Quando e Então
  Então o cadastro é rejeitado porque Mas precisa continuar um passo anterior
```

### Decisões sobre as sugestões de IA

- **Cenário sem Dado:** aceitar. A regra permite omitir `Dado` quando o cenário começa com `Quando` e contém `Então`.
- **Cenário iniciado com Mas:** rejeitar. `Mas` não pode ser o primeiro passo, pois depende de uma etapa anterior.
- **Cenário com Dado e Quando, sem Então:** rejeitar como `DUPLICADA`, pois a mesma regra já foi analisada no cenário manual de ausência de resultado esperado.

## Sugestões geradas por IA para revisão

O Codex preparou as sugestões abaixo para a regra 2, que trata da validação da sequência de passos. O exemplo de referência foi o cenário sem `Então` da apresentação de aula. O grupo pode comparar as propostas com seus próprios cenários depois de concluir a atividade manual.

Prompt para reproduzir a consulta

> Para o Estúdio BDD, proponha exemplos adicionais para validar cenários. A regra exige pelo menos um Quando e um Então, permite omitir Dado e exige a ordem Dado, Quando, Então. E e Mas continuam o tipo anterior. Escreva exemplos em Gherkin em português, com dados concretos, incluindo o limite de omitir Dado e o de iniciar com Mas. Considere também uma reformulação do caso sem Então que já existe nos slides e indique o que a tornaria redundante.

### Proposta de aceitação · cenário sem Dado

```gherkin
Cenário: Cenário com Quando e Então é aceito sem Dado
  Dado que Ana é autora da história "Exportar requisitos"
  Quando Ana cadastra o cenário "Baixar arquivo" somente com os passos Quando e Então
  Então o cenário é registrado com esses dois passos na ordem informada
```

A proposta verifica a menor sequência permitida pela regra.

### Proposta de aceitação · cenário iniciado por Mas

```gherkin
Cenário: Cenário iniciado por Mas é rejeitado
  Dado que Ana é autora da história "Exportar requisitos"
  Quando Ana tenta cadastrar o cenário "Baixar arquivo" com os passos Mas, Quando e Então
  Então o cadastro é rejeitado porque Mas precisa continuar um passo anterior
```

A proposta verifica a regra para `Mas` mesmo quando os dois tipos obrigatórios aparecem depois dele.

### Proposta de rejeição · cenário sem resultado esperado

```gherkin
Cenário: Cadastro com Dado e Quando é recusado
  Dado que Ana é autora da história "Exportar requisitos"
  Quando Ana tenta cadastrar o cenário "Baixar arquivo" com os passos Dado e Quando
  Então o cadastro é rejeitado porque falta um passo Então
```

O motivo sugerido é `DUPLICADA`, pois a apresentação de aula já demonstra essa mesma entrada inválida e o resultado esperado. A decisão final de cada proposta fica a cargo do grupo.
