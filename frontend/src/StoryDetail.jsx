import { useState } from 'react';
import { BookOpen, ChevronRight, Download, Pencil, Plus, Sparkles, Trash2, UserRound } from 'lucide-react';
import { request, useResource } from './api.js';
import { Badge, Dialog, Empty, ErrorMessage, Options } from './components.jsx';
import { ScenarioDialog, StoryForm } from './forms.jsx';
import { decisions, formatDate, origins, priorities, reasons, stepTypes } from './labels.js';

function ScenarioList({ scenarios, canEdit, onEdit, onCreate, onDelete }) {
  return <section className="panel">
    <div className="panel-header"><h2>Cenários <span className="count">{scenarios.length}</span></h2>
      {canEdit && <button className="button secondary" onClick={onCreate}><Plus size={16} /> Novo cenário</button>}
    </div>
    {!scenarios.length ? <Empty icon={BookOpen} title="Nenhum cenário cadastrado" /> : <div className="scenario-list">
      {scenarios.map(scenario => <article className="scenario-card" key={scenario.id}>
        <div className="scenario-header">
          <div><Badge value={scenario.origem} labels={origins} /><h3>{scenario.titulo}</h3></div>
          {canEdit && <div className="button-row">
            <button className="icon-button" aria-label={`Editar cenário ${scenario.titulo}`} onClick={() => onEdit(scenario)}><Pencil size={16} /></button>
            <button className="icon-button" aria-label={`Excluir cenário ${scenario.titulo}`} onClick={() => onDelete(scenario)}><Trash2 size={16} /></button>
          </div>}
        </div>
        <ol className="scenario-steps">
          {scenario.passos.map(step => <li key={step.id}><b className={`step-type ${step.tipo.toLowerCase()}`}>{stepTypes[step.tipo]}</b><span>{step.texto}</span></li>)}
        </ol>
      </article>)}
    </div>}
  </section>;
}

function Suggestions({ storyId, userId, revision, onRetry, onCreate, onDecision }) {
  const [filter, setFilter] = useState('');
  const response = useResource(`/historias/${storyId}/sugestoes${filter ? `?decisao=${filter}` : ''}`, userId, revision);

  return <section className="panel suggestions-panel">
    <div className="panel-header"><h2><Sparkles size={17} /> Sugestões de IA</h2></div>
    <div className="suggestions-toolbar">
      <select aria-label="Filtrar por decisão" value={filter} onChange={event => setFilter(event.target.value)}>
        <option value="">Todas as decisões</option><Options values={decisions} />
      </select>
      <button className="button secondary" onClick={onCreate}><Plus size={16} /> Registrar sugestão</button>
    </div>
    {response.error && <div className="suggestions-inset"><ErrorMessage message={response.error} onRetry={onRetry} /></div>}
    {!response.data && response.loading && <p role="status" className="loading suggestions-inset">Carregando sugestões…</p>}
    {response.data?.length === 0 && <Empty icon={Sparkles} title={filter ? 'Nenhuma sugestão com esta decisão' : 'Nenhuma sugestão registrada'} />}
    {!!response.data?.length && <div className="suggestion-list">
      {response.data.map(suggestion => <article className="suggestion-card" key={suggestion.id}>
        <div className="suggestion-header"><strong>{suggestion.ferramenta}</strong><Badge value={suggestion.decisao} labels={decisions} /></div>
        <p className="suggestion-content">{suggestion.conteudo}</p>
        <details className="suggestion-prompt"><summary>Ver prompt</summary><p>{suggestion.prompt}</p></details>
        <p className="suggestion-meta">{suggestion.registradaPor?.nome} · {formatDate(suggestion.registradaEm)}</p>
        {suggestion.decisao === 'PENDENTE' ? <div className="suggestion-actions">
          <button className="button secondary" onClick={() => onDecision(suggestion, 'REJEITADA')}>Rejeitar</button>
          <button className="button primary" onClick={() => onDecision(suggestion, 'ACEITA')}>Aceitar</button>
        </div> : <div className="suggestion-verdict">
          {suggestion.motivoRejeicao && <strong>{reasons[suggestion.motivoRejeicao]}</strong>}
          <p>{suggestion.justificativa}</p>
          {suggestion.decididaPor && <small>{suggestion.decididaPor.nome} · {formatDate(suggestion.decididaEm)}</small>}
        </div>}
      </article>)}
    </div>}
  </section>;
}

function Narrative({ story }) {
  return <div className="panel narrative">
    <h2>História de usuário</h2>
    <dl>
      <div><dt>Como</dt><dd>{story.papel}</dd></div>
      <div><dt>Quero</dt><dd>{story.objetivo}</dd></div>
      <div><dt>Para</dt><dd>{story.beneficio}</dd></div>
    </dl>
  </div>;
}

export default function StoryDetail({ storyId, project, user, revision, onChange, onProjects, onBack }) {
  const [dialog, setDialog] = useState(null);
  const [exporting, setExporting] = useState(false);
  const [exportError, setExportError] = useState('');
  const path = `/historias/${storyId}`;
  const response = useResource(path, user.id, revision);
  const story = response.data;
  const canEdit = story?.autor.id === user.id;

  const call = (resource, method, body) => request(resource, { userId: user.id, method, body });
  const close = () => setDialog(null);

  function saved(message) {
    close();
    setExportError('');
    onChange(message);
  }

  async function exportFeature() {
    setExportError('');
    setExporting(true);
    try {
      const { blob, name } = await request(`${path}/feature`, { userId: user.id, download: true });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = name;
      link.click();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (error) {
      setExportError(error.message);
    } finally {
      setExporting(false);
    }
  }

  async function saveStory(data) {
    await call(path, 'PUT', Object.fromEntries(data));
    saved('História atualizada.');
  }

  async function deleteStory() {
    await call(path, 'DELETE');
    saved('História excluída.');
    onBack();
  }

  async function saveScenario(body) {
    const id = dialog.scenario?.id;
    await (id ? call(`/cenarios/${id}`, 'PUT', body) : call(`${path}/cenarios`, 'POST', body));
    saved(id ? 'Cenário atualizado.' : 'Cenário cadastrado.');
  }

  async function deleteScenario() {
    await call(`/cenarios/${dialog.scenario.id}`, 'DELETE');
    saved('Cenário excluído.');
  }

  async function createSuggestion(data) {
    await call(`${path}/sugestoes`, 'POST', Object.fromEntries(data));
    saved('Sugestão registrada.');
  }

  async function decideSuggestion(data) {
    await call(`/sugestoes/${dialog.suggestion.id}/decisao`, 'PUT', { ...Object.fromEntries(data), decisao: dialog.decision });
    saved(dialog.decision === 'ACEITA' ? 'Sugestão aceita.' : 'Sugestão rejeitada.');
  }

  const rejecting = dialog?.decision === 'REJEITADA';
  return <>
    <nav className="breadcrumbs" aria-label="Navegação">
      <button onClick={onProjects}>Projetos</button><ChevronRight size={14} />
      <button onClick={onBack}>{project?.nome || 'Histórias'}</button><ChevronRight size={14} /><span>História</span>
    </nav>
    <ErrorMessage message={response.error} onRetry={() => onChange()} />
    {!story ? (response.loading && <p role="status" className="loading">Carregando história…</p>) : <>
      <div className="page-heading story-heading">
        <div><div className="story-meta"><Badge value={story.prioridade} labels={priorities} /><span><UserRound size={14} /> {story.autor.nome}</span></div><h1>{story.titulo}</h1></div>
        <div className="button-row">
          {canEdit && <>
            <button className="button secondary" onClick={() => setDialog({ type: 'story' })}><Pencil size={16} /> Editar história</button>
            <button className="icon-button bordered" aria-label="Excluir história" onClick={() => setDialog({ type: 'deleteStory' })}><Trash2 size={17} /></button>
          </>}
          <button className="button primary" onClick={exportFeature} disabled={exporting}><Download size={17} /> {exporting ? 'Exportando…' : 'Exportar .feature'}</button>
        </div>
      </div>
      <ErrorMessage message={exportError} />
      <div className="story-layout">
        <div className="story-main">
          <Narrative story={story} />
          <ScenarioList scenarios={story.cenarios} canEdit={canEdit} onCreate={() => setDialog({ type: 'scenario' })}
            onEdit={scenario => setDialog({ type: 'scenario', scenario })}
            onDelete={scenario => setDialog({ type: 'deleteScenario', scenario })} />
        </div>
        <Suggestions storyId={storyId} userId={user.id} revision={revision} onRetry={() => onChange()}
          onCreate={() => setDialog({ type: 'suggestion' })} onDecision={(suggestion, decision) => setDialog({ type: 'decision', suggestion, decision })} />
      </div>
    </>}
    {dialog?.type === 'story' && <Dialog title="Editar história" onCancel={close} onSave={saveStory}><StoryForm story={story} /></Dialog>}
    {dialog?.type === 'deleteStory' && <Dialog title="Excluir história" danger submitLabel="Excluir história" onCancel={close} onSave={deleteStory}>
      <p>Excluir a história <strong>{story.titulo}</strong>, seus cenários e suas sugestões?</p>
    </Dialog>}
    {dialog?.type === 'scenario' && <ScenarioDialog scenario={dialog.scenario} scenarioCount={story.cenarios.length} onCancel={close} onSave={saveScenario} />}
    {dialog?.type === 'deleteScenario' && <Dialog title="Excluir cenário" danger submitLabel="Excluir cenário" onCancel={close} onSave={deleteScenario}>
      <p>Excluir o cenário <strong>{dialog.scenario.titulo}</strong> e seus passos?</p>
    </Dialog>}
    {dialog?.type === 'suggestion' && <Dialog title="Registrar sugestão" submitLabel="Registrar" onCancel={close} onSave={createSuggestion}>
      <label>Ferramenta<input name="ferramenta" placeholder="Ex.: ChatGPT" required /></label>
      <label>Prompt utilizado<textarea name="prompt" rows={3} required /></label>
      <label>Conteúdo da sugestão<textarea name="conteudo" rows={5} required /></label>
    </Dialog>}
    {dialog?.type === 'decision' && <Dialog title={rejecting ? 'Rejeitar sugestão' : 'Aceitar sugestão'}
      submitLabel={rejecting ? 'Rejeitar' : 'Aceitar'} onCancel={close} onSave={decideSuggestion}>
      {rejecting && <label>Motivo da rejeição<select name="motivoRejeicao" defaultValue="" required>
        <option value="">Selecione um motivo</option><Options values={reasons} />
      </select></label>}
      <label>Justificativa<textarea name="justificativa" rows={4} required /></label>
    </Dialog>}
  </>;
}
