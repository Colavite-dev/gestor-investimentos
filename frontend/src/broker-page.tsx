import { useEffect, useMemo, useState } from 'react';
import { Building2, LoaderCircle, Plus, Search } from 'lucide-react';
import { corretorasApi } from './api/modules';
import { EmptyState, ErrorAlert, Modal, StatusBadge, Toast } from './components/ui';
import type { Corretora } from './types/api';
import { formatDate } from './utils/format';
import './broker-page.css';

const formatCnpj = (cnpj: string) => {
  const digits = cnpj.replace(/\D/g, '');
  return digits.length === 14 ? digits.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, '$1.$2.$3/$4-$5') : cnpj;
};

const brokerLocation = (broker: Corretora) => [broker.cidade, broker.uf].filter(Boolean).join(' · ') || 'Não informado';
const brokerStatus = (broker: Corretora) => broker.validadaNaCvm ? 'Validada' : broker.situacaoCadastral || 'Não informado';

function BrokerStatusBadge({ broker }: { broker: Corretora }) {
  return <StatusBadge label={brokerStatus(broker)} tone={broker.validadaNaCvm ? 'success' : 'default'}/>;
}

function BrokerTable({ brokers, recentId }: { brokers: Corretora[]; recentId?: number }) {
  return <>
    <div className="broker-table-wrap"><table className="broker-table"><thead><tr><th>Corretora</th><th>CNPJ</th><th>Localização</th><th>Validação</th><th>Cadastrada em</th></tr></thead><tbody>{brokers.map(broker => <tr key={broker.id} className={broker.id === recentId ? 'is-recent' : undefined}><td><div className="broker-name"><strong>{broker.razaoSocial}</strong>{broker.nomeFantasia && <small>{broker.nomeFantasia}</small>}</div></td><td className="broker-cnpj">{formatCnpj(broker.cnpj)}</td><td>{brokerLocation(broker)}</td><td><BrokerStatusBadge broker={broker}/></td><td className="broker-date">{broker.dataCadastro ? formatDate(broker.dataCadastro) : 'Não informado'}</td></tr>)}</tbody></table></div>
    <div className="broker-card-list">{brokers.map(broker => <article className={`broker-list-card ${broker.id === recentId ? 'is-recent' : ''}`} key={broker.id}><div className="broker-card-heading"><Building2 aria-hidden="true"/><div><strong>{broker.razaoSocial}</strong>{broker.nomeFantasia && <small>{broker.nomeFantasia}</small>}</div><BrokerStatusBadge broker={broker}/></div><dl><div><dt>CNPJ</dt><dd className="broker-cnpj">{formatCnpj(broker.cnpj)}</dd></div><div><dt>Localização</dt><dd>{brokerLocation(broker)}</dd></div><div><dt>Cadastrada em</dt><dd>{broker.dataCadastro ? formatDate(broker.dataCadastro) : 'Não informado'}</dd></div></dl></article>)}</div>
  </>;
}

function BrokerLoading() {
  return <div className="broker-skeletons" aria-label="Carregando corretoras" aria-busy="true">{Array.from({ length: 3 }, (_, index) => <div className="broker-skeleton" key={index}><span/><span/><span/></div>)}</div>;
}

export function BrokerPage() {
  const [brokers, setBrokers] = useState<Corretora[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [open, setOpen] = useState(false);
  const [cnpj, setCnpj] = useState('');
  const [query, setQuery] = useState('');
  const [error, setError] = useState('');
  const [formError, setFormError] = useState('');
  const [toast, setToast] = useState('');
  const [recentId, setRecentId] = useState<number>();

  const load = async () => {
    setLoading(true);
    setError('');
    try { setBrokers(await corretorasApi.list()); }
    catch (reason) { setError((reason as Error).message || 'Não foi possível carregar as corretoras.'); }
    finally { setLoading(false); }
  };
  useEffect(() => {
    let active = true;
    corretorasApi.list().then(items => { if (active) setBrokers(items); }).catch(reason => { if (active) setError(reason.message || 'Não foi possível carregar as corretoras.'); }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  const filteredBrokers = useMemo(() => {
    const normalized = query.trim().toLocaleLowerCase('pt-BR');
    if (!normalized) return brokers;
    const queryDigits = normalized.replace(/\D/g, '');
    return brokers.filter(broker => [broker.razaoSocial, broker.nomeFantasia, broker.cnpj].some(value => value?.toLocaleLowerCase('pt-BR').includes(normalized)) || (queryDigits && broker.cnpj.replace(/\D/g, '').includes(queryDigits)));
  }, [brokers, query]);
  const validatedCount = brokers.filter(broker => broker.validadaNaCvm).length;
  const latestBroker = brokers.reduce<Corretora | undefined>((latest, broker) => !latest || new Date(broker.dataCadastro).getTime() > new Date(latest.dataCadastro).getTime() ? broker : latest, undefined);
  const closeModal = () => { if (!submitting) { setOpen(false); setFormError(''); } };
  const openModal = () => { setCnpj(''); setFormError(''); setOpen(true); };

  const submit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setFormError('');
    try {
      const created = await corretorasApi.create({ cnpj });
      setRecentId(created.id);
      setOpen(false);
      setToast('Corretora cadastrada com sucesso.');
      await load();
    } catch (reason) {
      setFormError((reason as Error).message || 'Não foi possível validar os dados da corretora.');
    } finally { setSubmitting(false); }
  };

  return <div className="page broker-page">
    {!loading && brokers.length > 0 && <header className="broker-page-header"><div><p className="broker-eyebrow">Gestão de corretoras</p><h2>Corretoras</h2><p className="broker-page-description">Cadastre e consulte corretoras validadas por dados oficiais.</p></div><button type="button" className="button primary broker-primary-action" onClick={openModal}><Plus size={17} aria-hidden="true"/>Nova corretora</button></header>}
    {!loading && !error && brokers.length > 0 && <section className="broker-summary" aria-label="Resumo das corretoras"><article><span>Corretoras cadastradas</span><strong>{brokers.length}</strong></article><article><span>Validadas</span><strong>{validatedCount}</strong></article>{latestBroker && <article><span>Último cadastro</span><strong>{latestBroker.dataCadastro ? formatDate(latestBroker.dataCadastro) : 'Não informado'}</strong></article>}</section>}
    {error && <ErrorAlert message={error}/>}
    {loading ? <BrokerLoading/> : !error && (brokers.length === 0 ? <EmptyState title="Nenhuma corretora cadastrada" description="Cadastre sua primeira corretora para começar a organizar seus investimentos." action={<button type="button" className="button primary broker-primary-action" onClick={openModal}><Plus size={16} aria-hidden="true"/>Cadastrar corretora</button>}/> : <section className="broker-list-panel"><div className="broker-list-heading"><div><h3>Corretoras cadastradas</h3><p>{brokers.length === 1 ? '1 registro disponível' : `${brokers.length} registros disponíveis`}</p></div><label className="broker-search"><Search size={16} aria-hidden="true"/><span className="sr-only">Buscar corretora por nome ou CNPJ</span><input value={query} onChange={event => setQuery(event.target.value)} placeholder="Buscar por nome ou CNPJ"/></label></div>{filteredBrokers.length ? <BrokerTable brokers={filteredBrokers} recentId={recentId}/> : <EmptyState title="Nenhuma corretora encontrada" description="Ajuste a busca para consultar os registros já carregados."/>}</section>)}
    <Modal open={open} onClose={closeModal} title="Nova corretora"><form className="broker-form" onSubmit={submit} aria-busy={submitting}><div className="broker-form-intro"><Building2 aria-hidden="true"/><p>Informe o CNPJ para validar e preencher os dados da corretora.</p></div><label htmlFor="broker-cnpj">CNPJ<span>Usaremos o número informado para consultar os dados oficiais.</span></label><input id="broker-cnpj" required value={cnpj} onChange={event => setCnpj(event.target.value)} placeholder="00.000.000/0000-00" inputMode="numeric" autoComplete="off" disabled={submitting}/>{submitting && <div className="broker-validation" role="status" aria-live="polite"><LoaderCircle className="spin" size={18} aria-hidden="true"/><div><strong>Validando dados da corretora…</strong><span>Isso pode levar alguns instantes.</span></div></div>}{formError && <ErrorAlert message={formError}/>}<div className="form-actions"><button type="button" className="button" onClick={closeModal} disabled={submitting}>Cancelar</button><button className="button primary broker-primary-action" disabled={submitting}>{submitting ? 'Validando…' : 'Cadastrar corretora'}</button></div></form></Modal>
    <Toast message={toast} onClose={() => setToast('')}/>
  </div>;
}
