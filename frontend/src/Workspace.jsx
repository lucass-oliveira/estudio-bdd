import { useEffect, useState } from 'react';
import { BarChart3, BookOpen, ChevronRight, FileText, Folder, FolderOpen, Layers3, LogOut, Pencil, Plus, Trash2 } from 'lucide-react';
import { request, useResource } from './api.js';
import { Badge, Brand, Dialog, Empty, ErrorMessage, Options } from './components.jsx';
import { StoryForm } from './forms.jsx';
import { decisions, formatDate, initials, origins, priorities, reasons } from './labels.js';
import StoryDetail from './StoryDetail.jsx';

function ProjectForm({ project }) {
  return <>
    <label>Nome do projeto<input name="nome" defaultValue={project?.nome} maxLength={160} required /></label>
    <label>Descrição<textarea name="descricao" defaultValue={project?.descricao} rows={3} /></label>
    <label>Contexto do domínio<textarea name="contextoDominio" defaultValue={project?.contextoDominio} rows={4} /></label>
  </>;
}

function ProjectList({ projects, onSelect, onCreate }) {
  return <>
    <div className="page-heading">
      <div><h1>Projetos</h1><p>Organize suas histórias de usuário e cenários por projeto.</p></div>
      <button className="button primary" onClick={onCreate}><Plus size={18} /> Novo projeto</button>
    </div>
    {projects.length ? <div className="project-grid">
      {projects.map(project => <button className="project-card" key={project.id} onClick={() => onSelect(project.id)}>
        <span className="project-icon"><FolderOpen size={23} /></span>
        <strong>{project.nome}</strong>
        {project.descricao && <span className="project-description">{project.descricao}</span>}
        <span className="project-card-footer"><span>{project.criadoEm ? `Criado em ${formatDate(project.criadoEm)}` : ''}</span><ChevronRight size={17} /></span>
      </button>)}
    </div> : <Empty icon={Folder} title="Nenhum projeto cadastrado"><p>Crie um projeto para começar.</p></Empty>}
  </>;
}

function StoryTable({ stories, onSelect, filtered }) {
  if (!stories.length) return <Empty icon={BookOpen} title={filtered ? 'Nenhuma história nesta prioridade' : 'Nenhuma história cadastrada'} />;
  return <div className="table-scroll">
    <table>
      <thead><tr><th>História</th><th>Prioridade</th><th>Cenários</th><th>Sugestões pendentes</th><th><span className="visually-hidden">Abrir</span></th></tr></thead>
      <tbody>{stories.map(story => <tr key={story.id}>
        <td><button className="story-link" onClick={() => onSelect(story.id)}><FileText size={18} /><span><strong>{story.titulo}</strong><small>{story.autor.nome}</small></span></button></td>
        <td><Badge value={story.prioridade} labels={priorities} /></td>
        <td>{story.quantidadeCenarios}</td>
        <td>{story.quantidadeSugestoesPendentes}</td>
        <td><button className="icon-button" aria-label={`Abrir ${story.titulo}`} onClick={() => onSelect(story.id)}><ChevronRight size={18} /></button></td>
      </tr>)}</tbody>
    </table>
  </div>;
}

function CountList({ values, labels }) {
  return <dl className="count-list">{Object.entries(labels).map(([key, label]) => <div key={key}><dt>{label}</dt><dd>{values[key] ?? 0}</dd></div>)}</dl>;
}

function ProjectSummary({ projectId, userId, revision, onRetry }) {
  const response = useResource(`/projetos/${projectId}/resumo`, userId, revision);
  if (response.error) return <ErrorMessage message={response.error} onRetry={onRetry} />;
  const summary = response.data;
  if (!summary) return <p role="status" className="loading">Carregando resumo…</p>;
  return <section className="summary-grid" aria-label="Resumo do projeto">
    <article className="summary-card"><h2>Histórias de usuário</h2><strong className="summary-total">{summary.quantidadeHistorias}</strong></article>
    <article className="summary-card"><h2>Cenários por origem</h2><CountList values={summary.cenariosPorOrigem} labels={origins} /></article>
    <article className="summary-card"><h2>Sugestões por decisão</h2><CountList values={summary.sugestoesPorDecisao} labels={decisions} /></article>
    <article className="summary-card"><h2>Motivos de rejeição</h2>
      {summary.motivosRejeicao.length ? <dl className="count-list">
        {summary.motivosRejeicao.map(({ motivo, quantidade }) => <div key={motivo}><dt>{reasons[motivo]}</dt><dd>{quantidade}</dd></div>)}
      </dl> : <p className="muted">Nenhuma sugestão rejeitada.</p>}
    </article>
  </section>;
}

function ProjectDetail({ projectId, userId, revision, onRetry, onBack, onStory, onEdit, onDelete, onCreateStory }) {
  const [priority, setPriority] = useState('');
  const [tab, setTab] = useState('historias');
  const path = `/projetos/${projectId}`;
  const response = useResource(path, userId, revision);
  const filtered = useResource(priority && tab === 'historias' ? `${path}?prioridade=${priority}` : null, userId, revision);
  const project = response.data;
  const stories = priority ? filtered.data?.historias : project?.historias;

  return <>
    <nav className="breadcrumbs" aria-label="Navegação"><button onClick={onBack}>Projetos</button><ChevronRight size={14} /><span>{project?.nome || 'Projeto'}</span></nav>
    {response.error && <ErrorMessage message={response.error} onRetry={onRetry} />}
    {!project ? (response.loading && <p role="status" className="loading">Carregando projeto…</p>) : <>
      <div className="page-heading project-heading">
        <div><div className="heading-with-icon"><span className="project-icon"><FolderOpen size={24} /></span><h1>{project.nome}</h1></div>
          {project.descricao && <p>{project.descricao}</p>}
        </div>
        <div className="button-row">
          <button className="button secondary" onClick={() => onEdit(project)}><Pencil size={16} /> Editar projeto</button>
          <button className="icon-button bordered" aria-label="Excluir projeto" onClick={() => onDelete(project)}><Trash2 size={17} /></button>
        </div>
      </div>
      {project.contextoDominio && <details className="project-context"><summary>Contexto do domínio</summary><p>{project.contextoDominio}</p></details>}
      <div className="view-tabs" aria-label="Seções do projeto">
        <button aria-pressed={tab === 'historias'} onClick={() => setTab('historias')}><BookOpen size={17} /> Histórias</button>
        <button aria-pressed={tab === 'resumo'} onClick={() => setTab('resumo')}><BarChart3 size={17} /> Resumo</button>
      </div>
      {tab === 'historias' ? <section className="panel">
        <div className="panel-header"><h2>Histórias de usuário</h2>
          <div className="button-row">
            <select aria-label="Filtrar por prioridade" value={priority} onChange={event => setPriority(event.target.value)}>
              <option value="">Todas as prioridades</option><Options values={priorities} />
            </select>
            <button className="button primary" onClick={onCreateStory}><Plus size={17} /> Nova história</button>
          </div>
        </div>
        {filtered.error && <div className="panel-inset"><ErrorMessage message={filtered.error} onRetry={onRetry} /></div>}
        {stories ? <StoryTable stories={stories} onSelect={onStory} filtered={Boolean(priority)} />
          : filtered.loading && <p role="status" className="loading panel-inset">Carregando histórias…</p>}
      </section> : <ProjectSummary projectId={projectId} userId={userId} revision={revision} onRetry={onRetry} />}
    </>}
  </>;
}

export default function Workspace({ user, onSignOut }) {
  const [projectId, setProjectId] = useState(null);
  const [storyId, setStoryId] = useState(null);
  const [dialog, setDialog] = useState(null);
  const [revision, setRevision] = useState(0);
  const [notice, setNotice] = useState('');
  const projects = useResource('/projetos', user.id, revision);
  const selectedProject = projects.data?.find(project => project.id === projectId);

  useEffect(() => {
    if (!notice) return;
    const timer = window.setTimeout(() => setNotice(''), 4200);
    return () => window.clearTimeout(timer);
  }, [notice]);

  function refresh(message) {
    setRevision(value => value + 1);
    if (message) setNotice(message);
  }

  function selectProject(id) {
    setStoryId(null);
    setProjectId(id);
  }

  async function saveProject(data) {
    const existing = dialog.project;
    const saved = await request(existing ? `/projetos/${existing.id}` : '/projetos', {
      userId: user.id, method: existing ? 'PUT' : 'POST', body: Object.fromEntries(data),
    });
    selectProject(saved.id);
    setDialog(null);
    refresh(existing ? 'Projeto atualizado.' : 'Projeto cadastrado.');
  }

  async function deleteProject() {
    await request(`/projetos/${dialog.project.id}`, { userId: user.id, method: 'DELETE' });
    selectProject(null);
    setDialog(null);
    refresh('Projeto excluído.');
  }

  async function createStory(data) {
    const story = await request(`/projetos/${projectId}/historias`, { userId: user.id, method: 'POST', body: Object.fromEntries(data) });
    setStoryId(story.id);
    setDialog(null);
    refresh('História cadastrada.');
  }

  return <div className="workspace">
    <aside className="sidebar">
      <div className="sidebar-top"><Brand /></div>
      <nav aria-label="Projetos">
        <button className={`sidebar-link ${!projectId ? 'active' : ''}`} onClick={() => selectProject(null)}><Layers3 size={19} /> Todos os projetos</button>
        {!!projects.data?.length && <div className="sidebar-projects">
          <div className="sidebar-heading">Projetos</div>
          {projects.data.map(project => <button className={`sidebar-link ${projectId === project.id ? 'active' : ''}`} key={project.id} onClick={() => selectProject(project.id)}>
            <Folder size={17} /><span>{project.nome}</span>
          </button>)}
        </div>}
      </nav>
      <div className="sidebar-account">
        <span className="avatar">{initials(user.nome)}</span>
        <span className="account-name">{user.nome}</span>
        <button className="sign-out" onClick={onSignOut}><LogOut size={16} /> Sair</button>
      </div>
    </aside>
    <main className="workspace-main">
      <div className="workspace-constrained">
        {storyId ? <StoryDetail key={storyId} storyId={storyId} project={selectedProject} user={user}
          revision={revision} onChange={refresh} onProjects={() => selectProject(null)} onBack={() => setStoryId(null)} />
        : projectId ? <ProjectDetail key={projectId} projectId={projectId} userId={user.id} revision={revision} onRetry={refresh}
          onBack={() => selectProject(null)} onStory={setStoryId} onEdit={project => setDialog({ type: 'project', project })}
          onDelete={project => setDialog({ type: 'deleteProject', project })} onCreateStory={() => setDialog({ type: 'story' })} />
        : <>
          <ErrorMessage message={projects.error} onRetry={() => refresh()} />
          {projects.data ? <ProjectList projects={projects.data} onSelect={selectProject} onCreate={() => setDialog({ type: 'project' })} />
            : projects.loading && <p role="status" className="loading">Carregando projetos…</p>}
        </>}
      </div>
    </main>
    {notice && <div className="toast" role="status">{notice}</div>}
    {dialog?.type === 'project' && <Dialog title={dialog.project ? 'Editar projeto' : 'Novo projeto'} onCancel={() => setDialog(null)} onSave={saveProject}>
      <ProjectForm project={dialog.project} />
    </Dialog>}
    {dialog?.type === 'deleteProject' && <Dialog title="Excluir projeto" danger submitLabel="Excluir projeto" onCancel={() => setDialog(null)} onSave={deleteProject}>
      <p>Excluir o projeto <strong>{dialog.project.nome}</strong>? As histórias e seus registros também serão excluídos.</p>
    </Dialog>}
    {dialog?.type === 'story' && <Dialog title="Nova história" onCancel={() => setDialog(null)} onSave={createStory}><StoryForm /></Dialog>}
  </div>;
}
