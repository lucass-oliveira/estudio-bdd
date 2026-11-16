import { useEffect, useRef, useState } from 'react';
import { AlertCircle, Braces, X } from 'lucide-react';

export function Brand() {
  return <div className="brand"><span className="brand-icon"><Braces size={21} /></span><strong>Estúdio BDD</strong></div>;
}

export function ErrorMessage({ message, onRetry }) {
  if (!message) return null;
  return <div className="error-message" role="alert">
    <AlertCircle size={18} />
    <div><span>{message}</span>{onRetry && <button className="text-button" onClick={() => onRetry()}>Tentar novamente</button>}</div>
  </div>;
}

export function Empty({ icon: Icon, title, children }) {
  return <div className="empty"><span className="empty-icon"><Icon size={23} /></span><strong>{title}</strong>{children}</div>;
}

export function Badge({ value, labels }) {
  return <span className={`badge badge-${value?.toLowerCase()}`}>{labels[value] || value}</span>;
}

export function Options({ values }) {
  return Object.entries(values).map(([value, label]) => <option value={value} key={value}>{label}</option>);
}

export function Dialog({ title, children, onCancel, onSave, submitLabel = 'Salvar', danger = false, wide = false }) {
  const dialog = useRef(null);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const element = dialog.current;
    element.showModal();
    return () => element.close();
  }, []);

  async function submit(event) {
    event.preventDefault();
    if (pending) return;
    const data = new FormData(event.currentTarget);
    setPending(true);
    setError('');
    try {
      await onSave(data);
    } catch (failure) {
      setError(failure.message);
    } finally {
      setPending(false);
    }
  }

  return <dialog ref={dialog} className={wide ? 'dialog dialog-wide' : 'dialog'} aria-labelledby="dialog-title"
    onCancel={event => { event.preventDefault(); if (!pending) onCancel(); }}>
    <form onSubmit={submit}>
      <div className="dialog-header"><h2 id="dialog-title">{title}</h2>
        <button type="button" className="icon-button" onClick={onCancel} disabled={pending} aria-label="Fechar"><X size={20} /></button>
      </div>
      <div className="dialog-content"><ErrorMessage message={error} />{children}</div>
      <div className="dialog-footer">
        <button type="button" className="button secondary" onClick={onCancel} disabled={pending}>Cancelar</button>
        <button type="submit" className={`button ${danger ? 'danger' : 'primary'}`} disabled={pending}>{pending ? 'Aguarde…' : submitLabel}</button>
      </div>
    </form>
  </dialog>;
}
