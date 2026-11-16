import assert from 'node:assert/strict'
import { mkdir, readFile, writeFile } from 'node:fs/promises'
import { join } from 'node:path'
import { parseArgs } from 'node:util'

const { values: opcoes } = parseArgs({
  options: {
    evidencias: { type: 'string' },
    'preparar-reinicio': { type: 'string' },
    'confirmar-reinicio': { type: 'string' },
  },
})
const base = process.env.API_URL ?? 'http://localhost:8080/api'
const origem = process.env.SPA_ORIGIN ?? 'http://localhost:5173'
const resultados = []

async function chamar(metodo, rota, status, usuario, corpo) {
  const headers = {}
  if (usuario) headers['X-Usuario-Id'] = String(usuario)
  if (corpo) headers['Content-Type'] = 'application/json'
  const resposta = await fetch(base + rota, {
    method: metodo,
    headers,
    body: corpo ? JSON.stringify(corpo) : undefined,
  })
  const texto = await resposta.text()
  assert.equal(resposta.status, status, `${metodo} ${rota}: ${texto}`)
  const dados = resposta.headers.get('content-type')?.includes('json') ? JSON.parse(texto) : texto
  if (status >= 400) {
    assert.match(resposta.headers.get('content-type'), /application\/problem\+json/)
    assert.equal(dados.status, status)
    assert.ok(dados.detail)
  }
  return { dados, headers: resposta.headers }
}

function resultado(entrada, esperado, observado) {
  resultados.push(`| ${entrada} | ${esperado} | ${observado} |`)
}

const { dados: usuarios } = await chamar('GET', '/usuarios', 200)
assert.ok(usuarios.length >= 2, 'Inicie com dados de demonstração e dois usuários.')
const [autora, outro] = usuarios.map(usuario => usuario.id)
const rotaCenarios = '/cenarios'
const historia = {
  titulo: 'Consultar reservas',
  papel: 'leitora',
  objetivo: 'consultar as reservas em andamento',
  beneficio: 'acompanhar os prazos de retirada',
  prioridade: 'MEDIA',
}
const passos = [
  { tipo: 'DADO', texto: 'que a leitora tem uma reserva ativa' },
  { tipo: 'QUANDO', texto: 'ela consulta suas reservas em andamento' },
  { tipo: 'ENTAO', texto: 'a reserva ativa aparece com seu prazo' },
]
const sugestao = {
  ferramenta: 'Assistente fictício',
  prompt: 'Que caso de borda falta na consulta de reservas?',
  conteudo: 'Considere uma leitora sem reservas ativas.',
}

async function prepararReinicio(arquivo) {
  const { dados: projeto } = await chamar('POST', '/projetos', 201, autora, {
    nome: `Persistência ${Date.now()}`,
  })
  const { dados: novaHistoria } = await chamar('POST', `/projetos/${projeto.id}/historias`, 201, autora, historia)
  const { dados: cenario } = await chamar('POST', `/historias/${novaHistoria.id}/cenarios`, 201, autora, {
    titulo: 'Reserva encontrada',
    origem: 'HUMANO',
    passos,
  })
  await writeFile(arquivo, JSON.stringify({ projeto, novaHistoria, cenario, autora }))
  console.log('Dados de persistência criados. Reinicie a API e o PostgreSQL antes da confirmação.')
}

async function confirmarReinicio(arquivo) {
  const { projeto, novaHistoria, cenario, autora: usuario } = JSON.parse(
    await readFile(arquivo, 'utf8'),
  )
  const { dados: persistida } = await chamar('GET', `/historias/${novaHistoria.id}`, 200, usuario)
  assert.equal(persistida.titulo, novaHistoria.titulo)
  assert.equal(persistida.cenarios[0].id, cenario.id)
  assert.deepEqual(persistida.cenarios[0].passos, cenario.passos)
  await chamar('DELETE', `/projetos/${projeto.id}`, 204, usuario)
  console.log('Após o reinício, a API recuperou a história, o cenário e os passos com os mesmos IDs e posições.')
}

async function verificar() {
  await chamar('GET', '/projetos', 401)
  await chamar('GET', '/projetos', 401, Number.MAX_SAFE_INTEGER)
  resultado('Acesso sem identidade ou com usuário inexistente', 'HTTP 401 nos dois pedidos', 'HTTP 401 nos dois pedidos')
  const headersCors = { 'Access-Control-Request-Method': 'GET', 'Access-Control-Request-Headers': 'X-Usuario-Id' }
  const corsValido = await fetch(base + '/projetos', { method: 'OPTIONS', headers: { ...headersCors, Origin: origem } })
  const corsInvalido = await fetch(base + '/projetos', {
    method: 'OPTIONS', headers: { ...headersCors, Origin: 'https://origem-invalida.example' },
  })
  assert.equal(corsValido.headers.get('access-control-allow-origin'), origem)
  assert.equal(corsInvalido.status, 403)
  assert.equal(corsInvalido.headers.get('access-control-allow-origin'), null)
  resultado('CORS de localhost e de uma origem diferente', 'Permitir apenas a SPA', 'Cabeçalho para a SPA e HTTP 403 para outra origem')

  const { dados: projeto } = await chamar('POST', '/projetos', 201, autora, {
    nome: `Verificação ${Date.now()}`,
    descricao: 'Dados temporários do roteiro',
  })
  const rotaProjeto = `/projetos/${projeto.id}`
  try {
    await chamar('POST', '/projetos', 409, autora, { nome: projeto.nome })
    const { dados: editado } = await chamar('PUT', rotaProjeto, 200, autora, { nome: projeto.nome, descricao: 'Editado' })
    assert.equal(editado.descricao, 'Editado')
    resultado('Cadastrar, editar e repetir o nome de um projeto', 'HTTP 201, 200 e 409', 'HTTP 201, 200 e 409')

    const { dados: cadastrada } = await chamar('POST', rotaProjeto + '/historias', 201, autora, historia)
    const rotaHistoria = `/historias/${cadastrada.id}`
    const cenarios = rotaHistoria + rotaCenarios
    const sugestoes = rotaHistoria + '/sugestoes'
    await chamar('POST', rotaProjeto + '/historias', 409, autora, historia)
    await chamar('POST', rotaProjeto + '/historias', 400, autora, { ...historia, beneficio: '' })
    await chamar('GET', rotaHistoria + '/feature', 409, autora)
    resultado('História válida, título duplicado e benefício vazio', 'HTTP 201, 409 e 400', 'HTTP 201, 409 e 400')
    resultado('Exportação sem cenários', 'HTTP 409', 'HTTP 409')

    const novoCenario = { titulo: 'Reserva encontrada', origem: 'HUMANO', passos }
    const { dados: cenario } = await chamar('POST', cenarios, 201, autora, novoCenario)
    const rotaCenario = rotaCenarios + '/' + cenario.id
    const rotaPassos = rotaCenario + '/passos'
    await chamar('POST', cenarios, 409, autora, novoCenario)
    for (const [titulo, passosInvalidos] of [
      ['Sem Quando', [passos[0], passos[2]]],
      ['Sem Então', [passos[0], passos[1]]],
      ['Fora de ordem', [passos[1], passos[0], passos[2]]],
      ['Começa com E', [{ tipo: 'E', texto: 'a leitora tem uma reserva' }, ...passos]],
    ]) {
      await chamar('POST', cenarios, 422, autora, { ...novoCenario, titulo, passos: passosInvalidos })
    }
    resultado('Cenário válido, título duplicado e quatro sequências inválidas', 'HTTP 201, 409 e quatro HTTP 422', 'HTTP 201, 409 e quatro HTTP 422')

    const adicional = { tipo: 'E', texto: 'a data aparece em horário local', posicao: 4 }
    const { dados: passo } = await chamar('POST', rotaPassos, 201, autora, adicional)
    const { dados: alterado } = await chamar('PUT', rotaPassos + '/' + passo.id, 200, autora, {
      ...adicional, texto: 'ela consulta as datas de retirada', posicao: 3,
    })
    const { dados: ordenados } = await chamar('GET', rotaPassos, 200, autora)
    assert.deepEqual(ordenados.map(item => item.posicao), [1, 2, 3, 4])
    assert.deepEqual(ordenados.map(item => item.tipo), ['DADO', 'QUANDO', 'E', 'ENTAO'])
    assert.equal(ordenados[2].id, passo.id)
    assert.equal(ordenados[2].texto, alterado.texto)
    await chamar('DELETE', rotaPassos + '/' + passo.id, 204, autora)
    await chamar('GET', rotaPassos + '/' + passo.id, 404, autora)
    resultado('Inserir um passo E, mover da posição 4 para 3, consultar e excluir', 'Dado, Quando, E, Então com mesmo ID do passo E', 'Ordem, ID e texto conferidos, HTTP 404 após a exclusão')

    const { dados: segundoCenario } = await chamar('POST', cenarios, 201, autora, {
      ...novoCenario, titulo: 'Consulta com filtro',
      passos: [...passos, { tipo: 'MAS', texto: 'ela não vê reservas antigas' }],
    })
    const [dado, quando, entao, mas] = segundoCenario.passos
    const rotaSegundo = rotaCenarios + '/' + segundoCenario.id
    await chamar('PUT', rotaSegundo, 200, autora, {
      ...segundoCenario, posicao: 1, passos: [dado, quando, mas, entao],
    })
    const { dados: cenariosReordenados } = await chamar('GET', cenarios, 200, autora)
    assert.deepEqual(cenariosReordenados.map(item => item.id), [segundoCenario.id, cenario.id])
    assert.deepEqual(cenariosReordenados[0].passos.map(item => item.id), [dado.id, quando.id, mas.id, entao.id])
    await chamar('DELETE', rotaSegundo, 204, autora)
    const { dados: cenariosRestantes } = await chamar('GET', cenarios, 200, autora)
    assert.deepEqual(cenariosRestantes.map(item => [item.id, item.posicao]), [[cenario.id, 1]])
    resultado('Mover o segundo cenário para a posição 1 e reordenar seus passos no mesmo PUT', 'IDs preservados, ordem nova persistida e posição restante renumerada após exclusão', 'Ordem dos cenários, IDs dos passos e posição restante conferidos')

    await chamar('PUT', rotaHistoria, 403, outro, historia)
    await chamar('DELETE', rotaHistoria, 403, outro)
    await chamar('PUT', rotaCenario, 403, outro, novoCenario)
    await chamar('DELETE', rotaCenario, 403, outro)
    await chamar('DELETE', rotaProjeto, 403, outro)
    resultado('Outro usuário tenta editar e excluir história e cenário ou excluir o projeto', 'Cinco HTTP 403', 'Cinco HTTP 403')

    await chamar('PUT', rotaHistoria, 200, autora, { ...historia, prioridade: 'ALTA' })
    const { dados: filtrado } = await chamar('GET', rotaProjeto + '?prioridade=ALTA', 200, autora)
    const { dados: semMedia } = await chamar('GET', rotaProjeto + '?prioridade=MEDIA', 200, autora)
    assert.deepEqual(filtrado.historias.map(item => item.id), [cadastrada.id])
    assert.equal(filtrado.historias[0].quantidadeCenarios, 1)
    assert.deepEqual(semMedia.historias, [])
    resultado('Alterar a prioridade da história e consultar pelos dois filtros', 'História apenas em ALTA e um cenário', 'Uma história em ALTA com um cenário, nenhuma em MEDIA')

    const { dados: pendente } = await chamar('POST', sugestoes, 201, outro, sugestao)
    const decidir = id => `/sugestoes/${id}/decisao`
    assert.equal(pendente.decisao, 'PENDENTE')
    const { dados: lista } = await chamar('GET', sugestoes + '?decisao=PENDENTE', 200, autora)
    assert.deepEqual(lista.map(item => item.id), [pendente.id])
    const { dados: comPendente } = await chamar('GET', rotaProjeto, 200, autora)
    assert.equal(comPendente.historias[0].quantidadeSugestoesPendentes, 1)
    await chamar('GET', rotaHistoria + '/feature', 409, autora)
    await chamar('PUT', decidir(pendente.id), 422, autora, { decisao: 'REJEITADA', justificativa: 'Já coberto.' })
    const { dados: rejeitada } = await chamar('PUT', decidir(pendente.id), 200, autora, {
      decisao: 'REJEITADA', motivoRejeicao: 'DUPLICADA', justificativa: 'Já coberto.',
    })
    assert.equal(rejeitada.motivoRejeicao, 'DUPLICADA')
    assert.equal(rejeitada.decididaPor.id, autora)
    await chamar('PUT', decidir(pendente.id), 409, autora, { decisao: 'ACEITA', justificativa: 'Útil.' })
    resultado('Registrar e filtrar pendente, rejeitar sem motivo e com motivo, tentar rediscutir', 'Pendente, HTTP 422, REJEITADA e HTTP 409', 'Estados e códigos conferidos, com decisora registrada')
    resultado('Exportação com sugestão pendente', 'HTTP 409', 'HTTP 409')

    const { dados: segunda } = await chamar('POST', sugestoes, 201, outro, sugestao)
    const { dados: antesAceite } = await chamar('GET', cenarios, 200, autora)
    const { dados: aceita } = await chamar('PUT', decidir(segunda.id), 200, autora, {
      decisao: 'ACEITA', justificativa: 'O caso de borda é útil.',
    })
    assert.equal(aceita.decisao, 'ACEITA')
    const { dados: aposAceite } = await chamar('GET', cenarios, 200, autora)
    assert.deepEqual(aposAceite, antesAceite)
    const { dados: ajustado } = await chamar('PUT', rotaCenario, 200, autora, {
      ...novoCenario, origem: 'SUGESTAO_IA', passos: aposAceite[0].passos,
    })
    assert.equal(ajustado.origem, 'SUGESTAO_IA')
    assert.deepEqual(ajustado.passos, aposAceite[0].passos)
    resultado('Aceitar sugestão, consultar cenários e ajustar a origem manualmente', 'ACEITA e cenário preservado até o ajuste manual', 'ACEITA, mesmos IDs, origem atualizada no PUT')

    const { dados: resumo } = await chamar('GET', rotaProjeto + '/resumo', 200, autora)
    assert.equal(resumo.quantidadeHistorias, 1)
    assert.deepEqual(resumo.cenariosPorOrigem, { HUMANO: 0, SUGESTAO_IA: 1 })
    assert.deepEqual(resumo.sugestoesPorDecisao, { PENDENTE: 0, ACEITA: 1, REJEITADA: 1 })
    assert.deepEqual(resumo.motivosRejeicao, [{ motivo: 'DUPLICADA', quantidade: 1 }])
    resultado('Resumo após cadastrar e decidir as sugestões', '1 história, 1 cenário de IA, 1 aceita, 1 rejeitada e 1 DUPLICADA', 'Contagens e classificações conferidas')

    const { dados: feature, headers } = await chamar('GET', rotaHistoria + '/feature', 200, autora)
    assert.match(headers.get('content-disposition'), /attachment;.*\.feature/)
    assert.match(feature, /^# language: pt\r?\n/)
    assert.match(feature, /Funcionalidade: Consultar reservas/)
    assert.match(feature, /Cenário: Reserva encontrada/)
    assert.match(feature, /Dado [^\n]+\s+Quando [^\n]+\s+Então /)
    resultado('Exportação após as decisões', 'Arquivo .feature localizado com passos ordenados', 'HTTP 200, anexo e palavras-chave Gherkin em português')

    await chamar('DELETE', rotaCenario, 204, autora)
    await chamar('GET', rotaPassos, 404, autora)
    await chamar('DELETE', rotaHistoria, 204, autora)
    await chamar('GET', rotaHistoria, 404, autora)
    resultado('Excluir cenário e história como autor', 'Dois HTTP 204 e consultas seguintes HTTP 404', 'Dois HTTP 204 e dois HTTP 404')
  } finally {
    await chamar('DELETE', rotaProjeto, 204, autora)
  }

  const { dados: projetos } = await chamar('GET', '/projetos', 200, autora)
  const biblioteca = projetos.find(item => item.nome === 'Portal da Biblioteca')
  assert.ok(biblioteca, 'Projeto fictício Portal da Biblioteca não encontrado.')
  const { dados: detalhes } = await chamar('GET', '/projetos/' + biblioteca.id, 200, autora)
  const reserva = detalhes.historias.find(item => item.titulo === 'Reservar um livro disponível')
  assert.ok(reserva, 'História fictícia exportável não encontrada.')
  const { dados: exemplo } = await chamar('GET', `/historias/${reserva.id}/feature`, 200, autora)
  if (opcoes.evidencias) {
    await mkdir(opcoes.evidencias, { recursive: true })
    await writeFile(join(opcoes.evidencias, 'reservar-um-livro-disponivel.feature'), exemplo)
  }

  console.log([
    '| Entrada | Esperado | Observado |',
    '| --- | --- | --- |',
    ...resultados,
  ].join('\n'))
}

if (opcoes['preparar-reinicio']) {
  await prepararReinicio(opcoes['preparar-reinicio'])
} else if (opcoes['confirmar-reinicio']) {
  await confirmarReinicio(opcoes['confirmar-reinicio'])
} else {
  await verificar()
}
