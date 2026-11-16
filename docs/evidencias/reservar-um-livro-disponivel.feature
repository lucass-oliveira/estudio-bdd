# language: pt
Funcionalidade: Reservar um livro disponível
  Como leitora
  Quero reservar um exemplar disponível
  Para retirar o livro na biblioteca dentro do prazo

  Cenário: Reserva confirmada para exemplar disponível
    Dado que Ana possui cadastro ativo
    E um exemplar de "Dom Casmurro" está disponível
    Quando Ana solicita a reserva do exemplar
    Então a reserva de Ana é confirmada
    E o prazo de retirada é de 48 horas

  Cenário: Exemplar já reservado por outro leitor
    Dado que Bruno tem uma reserva ativa do único exemplar de "Dom Casmurro"
    Quando Ana solicita a reserva do mesmo exemplar
    Então o pedido de Ana é recusado
