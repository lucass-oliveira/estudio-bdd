import { useState } from 'react';
import { ArrowRight, BookOpen, CircleCheck, Layers3, ListChecks } from 'lucide-react';
import { useResource } from './api.js';
import { Brand, ErrorMessage } from './components.jsx';
import Workspace from './Workspace.jsx';

const sessionKey = 'estudio-bdd.usuario';

function Login({ onEnter }) {
  const [selection, setSelection] = useState('');
  const [revision, setRevision] = useState(0);
  const accounts = useResource('/usuarios', undefined, revision);
  const options = accounts.data || [];

  function submit(event) {
    event.preventDefault();
    const user = options.find(account => String(account.id) === selection);
    if (user) onEnter(user);
  }

  return <main className="login-page">
    <aside className="login-cover" aria-label="Sobre o Estúdio BDD">
      <Brand />
      <div className="cover-content">
        <h2>Requisitos em exemplos.</h2>
        <p>Escreva histórias de usuário, organize cenários BDD e revise sugestões de IA em um só lugar.</p>
        <div className="cover-scenario" aria-label="Exemplo de cenário BDD">
          <div className="cover-scenario-heading"><BookOpen size={17} /><span>Cenário · Exportar uma história</span><CircleCheck size={17} /></div>
          <div><b>Dado</b><span>uma história com ao menos um cenário e sem sugestões pendentes</span></div>
          <div><b>Quando</b><span>o analista exporta a história</span></div>
          <div><b>Então</b><span>o sistema gera o arquivo .feature</span></div>
        </div>
      </div>
      <div className="cover-footer"><Layers3 size={16} /> Programação de Sistemas II</div>
    </aside>
    <section className="login-side">
      <form className="login-card" onSubmit={submit}>
        <div className="login-mobile-brand"><Brand /></div>
        <span className="login-icon"><ListChecks size={26} /></span>
        <h1>Entrar no Estúdio</h1>
        <p>Selecione um usuário de demonstração para acessar os projetos.</p>
        <ErrorMessage message={accounts.loading ? '' : accounts.error} onRetry={() => setRevision(value => value + 1)} />
        <label>Usuário de demonstração
          <select value={selection} onChange={event => setSelection(event.target.value)} required disabled={accounts.loading || !options.length}>
            <option value="">{accounts.loading ? 'Carregando usuários…' : options.length ? 'Selecione um usuário' : 'Nenhum usuário disponível'}</option>
            {options.map(user => <option key={user.id} value={user.id}>{user.nome} · {user.email}</option>)}
          </select>
        </label>
        <button className="button primary login-submit" type="submit" disabled={!selection || accounts.loading}>
          Entrar <ArrowRight size={18} />
        </button>
        <small>Acesso de demonstração. Nenhuma senha é solicitada.</small>
      </form>
    </section>
  </main>;
}

export default function App() {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(sessionStorage.getItem(sessionKey)); }
    catch { return null; }
  });

  function updateSession(account) {
    if (account) sessionStorage.setItem(sessionKey, JSON.stringify(account));
    else sessionStorage.removeItem(sessionKey);
    setUser(account);
  }

  return user ? <Workspace user={user} onSignOut={() => updateSession(null)} /> : <Login onEnter={updateSession} />;
}
