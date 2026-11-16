import { useState } from 'react';
import { ArrowDown, ArrowUp, Plus, Trash2 } from 'lucide-react';
import { Dialog, Options } from './components.jsx';
import { origins, priorities, stepTypes } from './labels.js';

export function StoryForm({ story }) {
  return <>
    <label>Título<input name="titulo" defaultValue={story?.titulo} maxLength={160} required /></label>
    <label>Como <span className="label-hint">Papel</span><input name="papel" defaultValue={story?.papel} required /></label>
    <label>Quero <span className="label-hint">Objetivo</span><textarea name="objetivo" defaultValue={story?.objetivo} rows={2} required /></label>
    <label>Para <span className="label-hint">Benefício</span><textarea name="beneficio" defaultValue={story?.beneficio} rows={2} required /></label>
    <label>Prioridade<select name="prioridade" defaultValue={story?.prioridade || 'MEDIA'}><Options values={priorities} /></select></label>
  </>;
}

function newStep(tipo) {
  return { key: crypto.randomUUID(), tipo, texto: '' };
}

export function ScenarioDialog({ scenario, scenarioCount, onCancel, onSave }) {
  const [steps, setSteps] = useState(() => scenario?.passos.map(step => ({ ...step, key: step.id })) ||
    [newStep('DADO'), newStep('QUANDO'), newStep('ENTAO')]);

  function updateStep(key, changes) {
    setSteps(current => current.map(step => step.key === key ? { ...step, ...changes } : step));
  }

  function moveStep(from, to) {
    setSteps(current => {
      const next = [...current];
      [next[from], next[to]] = [next[to], next[from]];
      return next;
    });
  }

  function submit(data) {
    const fields = Object.fromEntries(data);
    return onSave({
      ...fields,
      posicao: fields.posicao ? Number(fields.posicao) : undefined,
      passos: steps.map(({ id, tipo, texto }) => ({ id, tipo, texto })),
    });
  }

  return <Dialog wide title={scenario ? 'Editar cenário' : 'Novo cenário'} onSave={submit} onCancel={onCancel}>
    <label>Título do cenário<input name="titulo" defaultValue={scenario?.titulo} maxLength={160} required /></label>
    <div className="form-row">
      <label>Origem<select name="origem" defaultValue={scenario?.origem || 'HUMANO'}><Options values={origins} /></select></label>
      {scenario && <label>Posição<select name="posicao" defaultValue={scenario.posicao}>
        {Array.from({ length: scenarioCount }, (_, index) => <option key={index + 1}>{index + 1}</option>)}
      </select></label>}
    </div>
    <fieldset className="steps-editor">
      <legend>Passos em ordem</legend>
      {steps.map((step, index) => <div className="step-editor" key={step.key}>
        <select aria-label={`Tipo do passo ${index + 1}`} value={step.tipo} onChange={event => updateStep(step.key, { tipo: event.target.value })}>
          <Options values={stepTypes} />
        </select>
        <input aria-label={`Texto do passo ${index + 1}`} placeholder="Descreva o comportamento" value={step.texto}
          onChange={event => updateStep(step.key, { texto: event.target.value })} required />
        <div className="step-controls">
          <button className="icon-button" type="button" disabled={index === 0} onClick={() => moveStep(index, index - 1)} aria-label={`Subir passo ${index + 1}`}><ArrowUp size={16} /></button>
          <button className="icon-button" type="button" disabled={index === steps.length - 1} onClick={() => moveStep(index, index + 1)} aria-label={`Descer passo ${index + 1}`}><ArrowDown size={16} /></button>
          <button className="icon-button" type="button" onClick={() => setSteps(current => current.filter(row => row.key !== step.key))} aria-label={`Excluir passo ${index + 1}`}><Trash2 size={16} /></button>
        </div>
      </div>)}
      <button className="button secondary" type="button" onClick={() => setSteps(current => [...current, newStep(current.length ? 'E' : 'DADO')])}><Plus size={16} /> Adicionar passo</button>
    </fieldset>
  </Dialog>;
}
