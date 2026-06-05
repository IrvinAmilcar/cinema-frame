import { useState, useEffect, useCallback } from 'react';
import { apiCaixa, type VendaDiaRequest, type FechamentoCaixaResponse } from '../api/client';

const cores = {
  vinho: '#8B0000',
  fundo: '#FAFAFA',
  borda: '#E0E0E0',
  verdeClaro: '#E8F5E9',
  verdeTexto: '#2E7D32',
  vermelhoClaro: '#FFEBEE',
  vermelhoTexto: '#C62828',
};

function hoje(): string {
  return new Date().toISOString().split('T')[0];
}

function fmtBRL(valor: number): string {
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

interface LinhaVenda {
  id: number;
  sessaoId: string;
  capacidadeSala: string;
  ingressosVendidos: string;
  valorArrecadado: string;
}

let proximoId = 1;
function novaLinha(sessaoId = '', cap = '', ing = '', val = ''): LinhaVenda {
  return { id: proximoId++, sessaoId, capacidadeSala: cap, ingressosVendidos: ing, valorArrecadado: val };
}

interface ResumoPeriodo {
  totalIngressos: number;
  totalIngressosValor: number;
  totalBomboniere: number;
  totalDescontoPontos: number;
  receitaTotal: number;
}

interface FechamentoCaixaResumo {
  quantidadeIngressos: number;
  totalIngressos: number;
  valorBomboniere: number;
  receitaTotal: number;
  dataHora: string;
}

interface Caixa {
  id: number;
  aberto: boolean;
  dataAbertura: string | null;
  fechamento: FechamentoCaixaResumo | null;
  operadorId: string | null;
}

interface Operador {
  id: string;
  nome: string;
}

function caixasIniciais(): Caixa[] {
  try {
    const salvo = localStorage.getItem('frame_caixas');
    if (salvo) {
      const parsed: Caixa[] = JSON.parse(salvo);
      const valido = parsed.every(c =>
        !c.fechamento || ('quantidadeIngressos' in c.fechamento && 'totalIngressos' in c.fechamento)
      ) && parsed.every(c => 'operadorId' in c);
      if (valido) return parsed;
      localStorage.removeItem('frame_caixas');
    }
  } catch {}
  return [
    { id: 1, aberto: true, dataAbertura: new Date().toLocaleString('pt-BR'), fechamento: null, operadorId: null },
    { id: 2, aberto: false, dataAbertura: null, fechamento: null, operadorId: null },
  ];
}

function gerarResumoAleatorio(): FechamentoCaixaResumo {
  const quantidadeIngressos = Math.floor(Math.random() * 80) + 40;
  const precoMedio = Math.random() * 20 + 20;
  const totalIngressos = parseFloat((quantidadeIngressos * precoMedio).toFixed(2));
  const valorBomboniere = parseFloat((Math.random() * 500 + 100).toFixed(2));
  const receitaTotal = parseFloat((totalIngressos + valorBomboniere).toFixed(2));
  return { quantidadeIngressos, totalIngressos, valorBomboniere, receitaTotal, dataHora: new Date().toLocaleString('pt-BR') };
}

export default function FechamentoCaixaPage() {
  const [dataInicio, setDataInicio] = useState(hoje());
  const [dataFim, setDataFim] = useState(hoje());
  const [erroData, setErroData] = useState<string | null>(null);
  const [linhas, setLinhas] = useState<LinhaVenda[]>([
    novaLinha('sessao-001', '100', '80', '1600.00'),
    novaLinha('sessao-002', '100', '60', '1200.00'),
  ]);
  const [abaAtiva, setAbaAtiva] = useState<'ingressos' | 'bomboniere' | 'ocupacao'>('ingressos');
  const [resultado, setResultado] = useState<FechamentoCaixaResponse | null>(null);
  const [resumo, setResumo] = useState<ResumoPeriodo | null>(null);
  const [carregando, setCarregando] = useState(false);

  interface IngressoSessao {
    sessaoId: string; filme: string; horario: string;
    salaNumero: number; salaTipo: string;
    totalIngressos: number; totalInteira: number; valorTotal: number;
  }
  interface BomboniereSessao {
    sessaoId: string; filme: string; horario: string;
    salaNumero: number; salaTipo: string;
    produto: string; quantidade: number; valorTotal: number;
  }
  interface OcupacaoSessao {
    sessaoId: string; filme: string; horario: string;
    salaNumero: number; salaTipo: string;
    capacidade: number; ingressosVendidos: number; diasVigencia: number;
  }

  const [ingressosSessao, setIngressosSessao] = useState<IngressoSessao[]>([]);
  const [bomboniereSessao, setBomboniereSessao] = useState<BomboniereSessao[]>([]);
  const [ocupacaoSessao, setOcupacaoSessao] = useState<OcupacaoSessao[]>([]);
  const [carregandoIngressos, setCarregandoIngressos] = useState(false);
  const [carregandoBomboniere, setCarregandoBomboniere] = useState(false);
  const [carregandoOcupacao, setCarregandoOcupacao] = useState(false);
  const [carregandoResumo, setCarregandoResumo] = useState(false);

  const [operadores, setOperadores] = useState<Operador[]>([]);
  const [caixas, setCaixas] = useState<Caixa[]>(caixasIniciais);
  const [confirmando, setConfirmando] = useState<number | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [sucesso, setSucesso] = useState<string | null>(null);

  useEffect(() => {
    fetch('http://localhost:8080/funcionarios')
      .then(r => r.json())
      .then((lista: { id: string; nome: string; role: string; ativo: boolean }[]) =>
        setOperadores(lista.filter(f => f.role === 'OPERADOR_DE_CAIXA' && f.ativo).map(f => ({ id: f.id, nome: f.nome })))
      )
      .catch(() => setOperadores([]));
  }, []);

  function salvarCaixas(lista: Caixa[]) {
    try { localStorage.setItem('frame_caixas', JSON.stringify(lista)); } catch {}
    setCaixas(lista);
  }

  function trocarOperador(caixaId: number, operadorId: string) {
    salvarCaixas(caixas.map(c => c.id === caixaId ? { ...c, operadorId: operadorId || null } : c));
  }

  function podeFechartLivremente(): boolean {
    return new Date().getHours() >= 21;
  }

  function jaFechouHoje(caixa: Caixa): boolean {
    if (!caixa.fechamento) return false;
    const h = new Date().toLocaleDateString('pt-BR');
    return caixa.fechamento.dataHora.startsWith(h);
  }

  function executarToggle(id: number) {
    setConfirmando(null);
    salvarCaixas(caixas.map(c => {
      if (c.id !== id) return c;
      if (c.aberto) return { ...c, aberto: false, dataAbertura: null, fechamento: gerarResumoAleatorio() };
      if (jaFechouHoje(c)) return c;
      return { ...c, aberto: true, dataAbertura: new Date().toLocaleString('pt-BR'), fechamento: null, operadorId: null };
    }));
  }

  function handleCliqueFechar(id: number) {
    const caixa = caixas.find(c => c.id === id);
    if (!caixa) return;
    if (!caixa.aberto) {
      if (jaFechouHoje(caixa)) { mostrarErro('Este caixa já foi fechado hoje e não pode ser reaberto.'); return; }
      executarToggle(id); return;
    }
    if (jaFechouHoje(caixa)) { mostrarErro('Este caixa já foi fechado hoje.'); return; }
    if (podeFechartLivremente()) { executarToggle(id); } else { setConfirmando(id); }
  }

  const buscarResumo = useCallback(async (inicio: string, fim: string) => {
    if (!inicio || !fim || inicio > fim) return;
    setCarregandoResumo(true); setCarregandoIngressos(true);
    setCarregandoBomboniere(true); setCarregandoOcupacao(true);
    try {
      const [resumoRes, ingressosRes, bombonieresRes, ocupacaoRes] = await Promise.all([
        apiCaixa.resumoPorPeriodo(inicio, fim),
        apiCaixa.ingressosPorSessao(inicio, fim),
        apiCaixa.bombonierePorSessao(inicio, fim),
        apiCaixa.ocupacaoPorSessao(inicio, fim),
      ]);
      setResumo(resumoRes); setIngressosSessao(ingressosRes);
      setBomboniereSessao(bombonieresRes); setOcupacaoSessao(ocupacaoRes);
    } catch {
      setResumo(null); setIngressosSessao([]); setBomboniereSessao([]); setOcupacaoSessao([]);
    } finally {
      setCarregandoResumo(false); setCarregandoIngressos(false);
      setCarregandoBomboniere(false); setCarregandoOcupacao(false);
    }
  }, []);

  useEffect(() => { buscarResumo(dataInicio, dataFim); }, [dataInicio, dataFim, buscarResumo]);

  function handleDataFimChange(valor: string) {
    if (valor < dataInicio) { setErroData('A data final não pode ser anterior à data inicial.'); return; }
    setErroData(null); setDataFim(valor);
  }

  function handleDataInicioChange(valor: string) {
    setDataInicio(valor);
    if (dataFim < valor) setDataFim(valor);
    setErroData(null);
  }

  function getMomentoFechamento(): string {
    const d = new Date();
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
  }

  function atualizarLinha(id: number, campo: keyof LinhaVenda, valor: string) {
    setLinhas(prev => prev.map(l => l.id === id ? { ...l, [campo]: valor } : l));
  }
  function removerLinha(id: number) { setLinhas(prev => prev.filter(l => l.id !== id)); }
  function adicionarLinha() { setLinhas(prev => [...prev, novaLinha()]); }

  function getVendas(): VendaDiaRequest[] {
    return linhas.map(l => ({
      sessaoId: l.sessaoId || `sessao-${Math.random().toString(36).slice(2, 7)}`,
      capacidadeSala: parseInt(l.capacidadeSala) || 0,
      ingressosVendidos: parseInt(l.ingressosVendidos) || 0,
      valorArrecadado: parseFloat(l.valorArrecadado) || 0,
    }));
  }

  const valorIngressos = resumo?.totalIngressosValor ?? 0;
  const valorBomboniere = resumo?.totalBomboniere ?? 0;
  const totalDescontoPontos = resumo?.totalDescontoPontos ?? 0;
  const receitaTotal = valorIngressos + valorBomboniere - totalDescontoPontos;

  function mostrarSucesso(msg: string) { setSucesso(msg); setErro(null); setTimeout(() => setSucesso(null), 4000); }
  function mostrarErro(msg: string) { setErro(msg); setSucesso(null); setTimeout(() => setErro(null), 5000); }

  async function handleFecharCaixa() {
    const vendas = getVendas();
    if (!vendas.length) { mostrarErro('Adicione ao menos uma sessão.'); return; }
    setCarregando(true);
    try {
      const res = await apiCaixa.fecharCaixa({ data: dataInicio, momentoFechamento: getMomentoFechamento(), vendas });
      setResultado(res); mostrarSucesso('Caixa fechado com sucesso!'); buscarResumo(dataInicio, dataFim);
    } catch (e: any) { mostrarErro(e.message || 'Erro ao fechar o caixa.');
    } finally { setCarregando(false); }
  }

  async function handleConsultarRelatorio() {
    setCarregando(true);
    try {
      const res = await apiCaixa.consultarRelatorio(dataInicio);
      setResultado(res); mostrarSucesso('Relatório carregado.');
    } catch (e: any) { mostrarErro(e.message || 'Nenhum fechamento encontrado para essa data.');
    } finally { setCarregando(false); }
  }

  const inputStyle: React.CSSProperties = {
    width: '100%', padding: '8px 10px', border: `1px solid ${cores.borda}`,
    borderRadius: '6px', fontSize: '13px', backgroundColor: 'white',
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif', backgroundColor: cores.fundo, minHeight: '100vh' }}>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
        <div>
          <h1 style={{ color: '#1a1a1a', margin: 0, fontSize: '22px', fontWeight: 600 }}>Fechamento de Caixa</h1>
          <p style={{ color: '#888', margin: '4px 0 0', fontSize: '13px' }}>Consolidação financeira e relatórios</p>
        </div>
        <button
          onClick={() => {
            const linhasCSV: string[] = [];

            linhasCSV.push(`RELATÓRIO DE FECHAMENTO DE CAIXA`);
            linhasCSV.push(`Período,${dataInicio} a ${dataFim}`);
            linhasCSV.push(`Gerado em,${new Date().toLocaleString('pt-BR')}`);
            linhasCSV.push('');

            linhasCSV.push('RESUMO FINANCEIRO');
            linhasCSV.push(`Ingressos (R$),${valorIngressos.toFixed(2)}`);
            linhasCSV.push(`Bomboniere (R$),${valorBomboniere.toFixed(2)}`);
            linhasCSV.push(`Descontos Fidelidade (R$),-${totalDescontoPontos.toFixed(2)}`);
            linhasCSV.push(`Receita Total (R$),${receitaTotal.toFixed(2)}`);
            linhasCSV.push('');

            linhasCSV.push('CONTROLE DE CAIXAS PRESENCIAIS');
            linhasCSV.push('Caixa,Status,Operador,Aberto em,Fechado em,Qtd. Ingressos,Total Ingressos (R$),Bomboniere (R$),Receita Total (R$)');
            caixas.forEach(c => {
              const operadorNome = operadores.find(o => o.id === c.operadorId)?.nome ?? '-';
              const status = c.aberto ? 'Aberto' : 'Fechado';
              const abertaEm = c.dataAbertura ?? '-';
              const fechadoEm = c.fechamento?.dataHora ?? '-';
              const qtd = c.fechamento?.quantidadeIngressos ?? '-';
              const totalIng = c.fechamento ? c.fechamento.totalIngressos.toFixed(2) : '-';
              const bomb = c.fechamento ? c.fechamento.valorBomboniere.toFixed(2) : '-';
              const rec = c.fechamento ? c.fechamento.receitaTotal.toFixed(2) : '-';
              linhasCSV.push(`Caixa ${c.id},${status},${operadorNome},${abertaEm},${fechadoEm},${qtd},${totalIng},${bomb},${rec}`);
            });
            linhasCSV.push('');

            if (ingressosSessao.length > 0) {
              linhasCSV.push('INGRESSOS POR SESSÃO');
              linhasCSV.push('Horário,Filme,Sala,Tipo,% Inteira,Qtd. Vendidos,Valor Total (R$)');
              ingressosSessao.forEach(i => {
                const pctInteira = i.totalIngressos > 0 ? Math.round((i.totalInteira / i.totalIngressos) * 100) + '%' : '-';
                linhasCSV.push(`${i.horario},"${i.filme}","Sala ${i.salaNumero} ${i.salaTipo.replace('TRES_D','3D')}",Misto,${pctInteira},${i.totalIngressos},${i.valorTotal.toFixed(2)}`);
              });
              linhasCSV.push('');
            }

            if (bomboniereSessao.length > 0) {
              linhasCSV.push('BOMBONIERE POR SESSÃO');
              linhasCSV.push('Horário,Filme,Sala,Produto,Qtd.,Valor Total (R$)');
              bomboniereSessao.forEach(b => {
                linhasCSV.push(`${b.horario},"${b.filme}","Sala ${b.salaNumero} ${b.salaTipo.replace('TRES_D','3D')}","${b.produto}",${b.quantidade},${b.valorTotal.toFixed(2)}`);
              });
              linhasCSV.push('');
            }

            if (ocupacaoSessao.length > 0) {
              linhasCSV.push('OCUPAÇÃO POR SESSÃO');
              linhasCSV.push('Horário,Filme,Sala,Vendidos,Capacidade Total,Taxa Ocupação (%)');
              ocupacaoSessao.forEach(s => {
                const taxa = s.capacidade > 0 && s.diasVigencia > 0 ? ((s.ingressosVendidos / (s.capacidade * s.diasVigencia)) * 100).toFixed(1) : '0.0';
                linhasCSV.push(`${s.horario},"${s.filme}","Sala ${s.salaNumero} ${s.salaTipo.replace('TRES_D','3D')}",${s.ingressosVendidos},${s.capacidade * s.diasVigencia},${taxa}`);
              });
              const taxaMedia = ocupacaoSessao.length > 0
                ? (ocupacaoSessao.reduce((acc, s) => acc + (s.capacidade > 0 && s.diasVigencia > 0 ? (s.ingressosVendidos / (s.capacidade * s.diasVigencia)) * 100 : 0), 0) / ocupacaoSessao.length).toFixed(1)
                : '0.0';
              linhasCSV.push(`,,,,Taxa Média,${taxaMedia}`);
            }

            const conteudoCSV = '\uFEFF' + linhasCSV.join('\r\n');
            const bom = new Blob([conteudoCSV], { type: 'text/csv;charset=utf-8;' });
            
            const a = document.createElement('a');
            a.href = URL.createObjectURL(bom);
            a.download = `fechamento-${dataInicio}-a-${dataFim}.csv`;
            a.click();
          }}
          style={{ backgroundColor: cores.verdeTexto, color: 'white', border: 'none', padding: '9px 16px', borderRadius: '8px', fontSize: '13px', fontWeight: 600, cursor: 'pointer' }}
        >↓ Exportar CSV</button>
      </div>

      {erro && <div style={{ backgroundColor: cores.vermelhoClaro, color: cores.vermelhoTexto, border: `1px solid #FFCDD2`, borderRadius: '8px', padding: '12px 16px', marginBottom: '16px', fontSize: '14px', fontWeight: 500 }}>⚠ {erro}</div>}
      {sucesso && <div style={{ backgroundColor: cores.verdeClaro, color: cores.verdeTexto, border: `1px solid #C8E6C9`, borderRadius: '8px', padding: '12px 16px', marginBottom: '16px', fontSize: '14px', fontWeight: 500 }}>✓ {sucesso}</div>}

      <div style={{ display: 'flex', gap: '16px', marginBottom: '20px' }}>
        <div style={{ flex: 1 }}>
          <label style={{ fontSize: '12px', color: '#888', fontWeight: 600, display: 'block', marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Data Inicial</label>
          <input type="date" value={dataInicio} onChange={e => handleDataInicioChange(e.target.value)} style={inputStyle} />
        </div>
        <div style={{ flex: 1 }}>
          <label style={{ fontSize: '12px', color: '#888', fontWeight: 600, display: 'block', marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Data Final</label>
          <input type="date" value={dataFim} onChange={e => handleDataFimChange(e.target.value)} style={inputStyle} />
        </div>
      </div>

      {erroData && <div style={{ backgroundColor: cores.vermelhoClaro, color: cores.vermelhoTexto, border: `1px solid #FFCDD2`, borderRadius: '8px', padding: '10px 14px', marginBottom: '16px', fontSize: '13px', fontWeight: 500 }}>⚠ {erroData}</div>}

      <div style={{ display: 'flex', gap: '14px', marginBottom: '20px' }}>
        <div style={{ flex: 1, backgroundColor: 'white', border: `1px solid ${cores.borda}`, borderRadius: '10px', padding: '16px 18px' }}>
          <div style={{ fontSize: '11px', color: '#999', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            🎟 Ingressos {carregandoResumo && <span style={{ fontSize: '10px', color: '#bbb' }}>atualizando...</span>}
          </div>
          <div style={{ fontSize: '22px', fontWeight: 600, color: '#1a1a1a' }}>{fmtBRL(valorIngressos)}</div>
          {resumo && <div style={{ fontSize: '12px', color: '#999', marginTop: '2px' }}>{resumo.totalIngressos} ingressos vendidos</div>}
        </div>
        <div style={{ flex: 1, backgroundColor: 'white', border: `1px solid ${cores.borda}`, borderRadius: '10px', padding: '16px 18px' }}>
          <div style={{ fontSize: '11px', color: '#999', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '6px' }}>🍿 Bomboniere</div>
          <div style={{ fontSize: '22px', fontWeight: 600, color: '#1a1a1a' }}>{fmtBRL(valorBomboniere)}</div>
        </div>
        <div style={{ flex: 1, backgroundColor: 'white', border: `1px solid ${cores.borda}`, borderRadius: '10px', padding: '16px 18px' }}>
          <div style={{ fontSize: '11px', color: '#999', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '6px' }}>🎫 Descontos Fidelidade</div>
          <div style={{ fontSize: '22px', fontWeight: 600, color: cores.vermelhoTexto }}>- {fmtBRL(totalDescontoPontos)}</div>
        </div>
        <div style={{ flex: 1, backgroundColor: cores.vinho, border: `1px solid ${cores.vinho}`, borderRadius: '10px', padding: '16px 18px' }}>
          <div style={{ fontSize: '11px', color: 'rgba(255,255,255,0.7)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '6px' }}>Receita Total</div>
          <div style={{ fontSize: '22px', fontWeight: 600, color: 'white' }}>{fmtBRL(receitaTotal)}</div>
        </div>
      </div>

      {/* Controle de Caixas */}
      <div style={{ backgroundColor: 'white', border: `1px solid ${cores.borda}`, borderRadius: '10px', padding: '18px 20px', marginBottom: '20px' }}>
        <div style={{ fontSize: '13px', fontWeight: 600, color: '#555', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span style={{ color: cores.vinho }}>💲</span> Controle de Caixas Presenciais
        </div>
        <div style={{ display: 'flex', gap: '14px' }}>
          {caixas.map(caixa => (
            <div key={caixa.id} style={{ flex: 1, borderRadius: '10px', padding: '16px 18px', backgroundColor: caixa.aberto ? '#F0FFF4' : 'white', border: `1px solid ${caixa.aberto ? '#A8D5B5' : cores.borda}` }}>
              <div style={{ fontWeight: 600, fontSize: '14px', color: '#1a1a1a', marginBottom: '6px' }}>Caixa {caixa.id}</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '5px', marginBottom: '10px' }}>
                <span style={{ fontSize: '12px' }}>{caixa.aberto ? '🔓' : '🔒'}</span>
                <span style={{ fontSize: '12px', fontWeight: 600, color: caixa.aberto ? '#2E7D32' : '#888' }}>{caixa.aberto ? 'Aberto' : 'Fechado'}</span>
              </div>

              {caixa.aberto && caixa.dataAbertura && (
                <div style={{ fontSize: '11px', color: '#777', marginBottom: '12px', padding: '6px 10px', backgroundColor: '#E8F5E9', borderRadius: '6px' }}>
                  📅 Aberto em {caixa.dataAbertura}
                </div>
              )}

              {/* Operador */}
              <div style={{ marginBottom: '12px' }}>
                <label style={{ fontSize: '11px', color: '#888', fontWeight: 600, display: 'block', marginBottom: '5px', textTransform: 'uppercase', letterSpacing: '0.4px' }}>Operador</label>
                {caixa.operadorId && operadores.find(o => o.id === caixa.operadorId) ? (
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '7px 10px', border: `1px solid ${cores.borda}`, borderRadius: '6px', backgroundColor: '#f9f8f5' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <div style={{ width: '28px', height: '28px', borderRadius: '50%', backgroundColor: cores.vinho, color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '12px', fontWeight: 700, flexShrink: 0 }}>
                        {operadores.find(o => o.id === caixa.operadorId)!.nome.charAt(0).toUpperCase()}
                      </div>
                      <span style={{ fontSize: '13px', fontWeight: 500, color: '#1a1a1a' }}>
                        {operadores.find(o => o.id === caixa.operadorId)!.nome}
                      </span>
                    </div>
                    {caixa.aberto && (
                      <button onClick={() => trocarOperador(caixa.id, '')} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#bbb', fontSize: '16px', lineHeight: 1, padding: '0 2px' }} onMouseEnter={e => (e.currentTarget.style.color = cores.vermelhoTexto)} onMouseLeave={e => (e.currentTarget.style.color = '#bbb')} title="Trocar operador">×</button>
                    )}
                  </div>
                ) : caixa.aberto ? (
                  <select value="" onChange={e => trocarOperador(caixa.id, e.target.value)} style={{ width: '100%', padding: '7px 10px', border: `1px solid ${cores.borda}`, borderRadius: '6px', fontSize: '13px', backgroundColor: 'white', color: '#aaa', cursor: 'pointer' }}>
                    <option value="">Selecionar operador...</option>
                    {operadores.map(op => <option key={op.id} value={op.id}>{op.nome}</option>)}
                  </select>
                ) : (
                  <div style={{ padding: '7px 10px', border: `1px solid ${cores.borda}`, borderRadius: '6px', backgroundColor: '#f9f8f5', fontSize: '13px', color: '#bbb', fontStyle: 'italic' }}>Nenhum operador</div>
                )}
              </div>

              {/* Resumo do fechamento */}
              {!caixa.aberto && caixa.fechamento && (
                <div style={{ fontSize: '11px', color: '#555', marginBottom: '12px', padding: '10px', backgroundColor: '#f9f8f5', borderRadius: '6px', lineHeight: 1.8 }}>
                  <div style={{ fontWeight: 600, color: '#888', marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '0.4px', fontSize: '10px' }}>Fechado em {caixa.fechamento.dataHora}</div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px 10px' }}>
                    <div><div style={{ color: '#999', fontSize: '10px', marginBottom: '1px' }}>Qtd. Ingressos</div><strong style={{ fontSize: '13px' }}>{caixa.fechamento.quantidadeIngressos}</strong></div>
                    <div><div style={{ color: '#999', fontSize: '10px', marginBottom: '1px' }}>Total Ingressos</div><strong style={{ fontSize: '13px' }}>{fmtBRL(caixa.fechamento.totalIngressos)}</strong></div>
                    <div><div style={{ color: '#999', fontSize: '10px', marginBottom: '1px' }}>Bomboniere</div><strong style={{ fontSize: '13px' }}>{fmtBRL(caixa.fechamento.valorBomboniere)}</strong></div>
                  </div>
                  <div style={{ marginTop: '10px', paddingTop: '8px', borderTop: `1px solid ${cores.borda}`, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: '#999', fontSize: '10px', textTransform: 'uppercase', letterSpacing: '0.4px' }}>Receita Total</span>
                    <strong style={{ fontSize: '14px', color: cores.vinho }}>{fmtBRL(caixa.fechamento.receitaTotal)}</strong>
                  </div>
                </div>
              )}

              {confirmando === caixa.id && (
                <div style={{ marginBottom: '10px', padding: '12px', backgroundColor: '#FFF8E1', border: '1px solid #FFE082', borderRadius: '8px', fontSize: '12px', color: '#5D4037' }}>
                  <div style={{ fontWeight: 600, marginBottom: '6px' }}>⚠ Fechar fora do horário?</div>
                  <div style={{ marginBottom: '10px', lineHeight: 1.5 }}>São {new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}h. O fechamento normalmente ocorre após as 21h. Deseja fechar mesmo assim?</div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button onClick={() => executarToggle(caixa.id)} style={{ flex: 1, padding: '7px', backgroundColor: cores.vinho, color: 'white', border: 'none', borderRadius: '6px', fontWeight: 600, fontSize: '12px', cursor: 'pointer' }}>Sim, fechar agora</button>
                    <button onClick={() => setConfirmando(null)} style={{ flex: 1, padding: '7px', backgroundColor: 'white', color: '#555', border: `1px solid ${cores.borda}`, borderRadius: '6px', fontWeight: 600, fontSize: '12px', cursor: 'pointer' }}>Cancelar</button>
                  </div>
                </div>
              )}

              <button
                onClick={() => handleCliqueFechar(caixa.id)}
                disabled={jaFechouHoje(caixa)}
                style={{ width: '100%', padding: '9px', border: 'none', borderRadius: '7px', fontWeight: 600, fontSize: '13px', cursor: jaFechouHoje(caixa) ? 'not-allowed' : 'pointer', backgroundColor: jaFechouHoje(caixa) ? '#ccc' : caixa.aberto ? cores.vinho : '#2E7D32', color: 'white', opacity: jaFechouHoje(caixa) ? 0.6 : 1 }}
              >
                {caixa.aberto ? '🔒 Fechar Caixa' : jaFechouHoje(caixa) ? '🔒 Encerrado hoje' : '🔓 Abrir Caixa'}
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* Abas */}
      <div style={{ backgroundColor: 'white', border: `1px solid ${cores.borda}`, borderRadius: '10px', overflow: 'hidden', marginBottom: '20px' }}>
        <div style={{ display: 'flex', borderBottom: `1px solid ${cores.borda}` }}>
          {(['ingressos', 'bomboniere', 'ocupacao'] as const).map(aba => (
            <button key={aba} onClick={() => setAbaAtiva(aba)} style={{ padding: '12px 22px', border: 'none', cursor: 'pointer', fontSize: '13px', fontWeight: 600, backgroundColor: abaAtiva === aba ? cores.vinho : 'white', color: abaAtiva === aba ? 'white' : '#555', borderBottom: abaAtiva === aba ? `2px solid ${cores.vinho}` : '2px solid transparent' }}>
              {aba === 'ingressos' ? 'Ingressos' : aba === 'bomboniere' ? 'Bomboniere' : 'Ocupação'}
            </button>
          ))}
        </div>

        {abaAtiva === 'ingressos' && (
          <div>
            {carregandoIngressos ? <div style={{ padding: '24px', textAlign: 'center', color: '#888', fontSize: '13px' }}>Carregando...</div>
            : ingressosSessao.length === 0 ? <div style={{ padding: '24px', textAlign: 'center', color: '#bbb', fontSize: '13px' }}>Nenhum ingresso encontrado para o período selecionado.</div>
            : (
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                <thead>
                  <tr style={{ backgroundColor: '#f9f8f5', borderBottom: `1px solid ${cores.borda}` }}>
                    {['Horário', 'Filme', 'Sala', '% Inteira', 'Qtd.', 'Valor Total'].map(h => (
                      <th key={h} style={{ padding: '10px 16px', textAlign: 'left', fontSize: '11px', fontWeight: 600, color: '#888', textTransform: 'uppercase', letterSpacing: '0.5px' }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {ingressosSessao.map((item, i) => (
                    <tr key={i} style={{ borderBottom: `1px solid #f0eeea` }}>
                      <td style={{ padding: '10px 16px', color: '#555' }}>{item.horario}</td>
                      <td style={{ padding: '10px 16px', fontWeight: 500, color: '#1a1a1a' }}>{item.filme}</td>
                      <td style={{ padding: '10px 16px', color: '#555' }}>Sala {item.salaNumero}<span style={{ marginLeft: '6px', fontSize: '10px', backgroundColor: '#f0eeea', padding: '2px 6px', borderRadius: '4px', color: '#888', fontWeight: 600 }}>{item.salaTipo.replace('TRES_D', '3D')}</span></td>
                      <td style={{ padding: '10px 16px' }}>
                        {item.totalIngressos === 0 ? <span style={{ fontSize: '11px', color: '#bbb', fontStyle: 'italic' }}>—</span>
                        : <span style={{ fontSize: '12px', fontWeight: 600, color: '#1565C0' }}>{Math.round((item.totalInteira / item.totalIngressos) * 100)}% inteira</span>}
                      </td>
                      <td style={{ padding: '10px 16px', fontWeight: 600, color: item.totalIngressos === 0 ? '#bbb' : '#1a1a1a' }}>{item.totalIngressos}</td>
                      <td style={{ padding: '10px 16px', fontWeight: 600, color: item.totalIngressos === 0 ? '#bbb' : cores.vinho }}>{fmtBRL(item.valorTotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {abaAtiva === 'bomboniere' && (
          <div>
            {carregandoBomboniere ? <div style={{ padding: '24px', textAlign: 'center', color: '#888', fontSize: '13px' }}>Carregando...</div>
            : bomboniereSessao.length === 0 ? <div style={{ padding: '24px', textAlign: 'center', color: '#bbb', fontSize: '13px' }}>Nenhuma venda de bomboniere encontrada para o período selecionado.</div>
            : (() => {
              const sessoes = Array.from(new Map(bomboniereSessao.map(i => [i.sessaoId, { sessaoId: i.sessaoId, filme: i.filme, horario: i.horario, salaNumero: i.salaNumero, salaTipo: i.salaTipo }])).values());
              return (
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                  <thead>
                    <tr style={{ backgroundColor: '#f9f8f5', borderBottom: `1px solid ${cores.borda}` }}>
                      {['Data/Hora', 'Filme', 'Sala', 'Produtos', 'Total'].map(h => (
                        <th key={h} style={{ padding: '10px 16px', textAlign: 'left', fontSize: '11px', fontWeight: 600, color: '#888', textTransform: 'uppercase', letterSpacing: '0.5px' }}>{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {sessoes.map(sessao => {
                      const itens = bomboniereSessao.filter(i => i.sessaoId === sessao.sessaoId);
                      const totalSessao = itens.reduce((s, i) => s + i.valorTotal, 0);
                      return (
                        <tr key={sessao.sessaoId} style={{ borderBottom: `1px solid #f0eeea` }}>
                          <td style={{ padding: '12px 16px', color: '#555', whiteSpace: 'nowrap', verticalAlign: 'top' }}>{itens[0].horario}</td>
                          <td style={{ padding: '12px 16px', fontWeight: 500, color: '#1a1a1a', verticalAlign: 'top' }}>{itens[0].filme}</td>
                          <td style={{ padding: '12px 16px', color: '#555', whiteSpace: 'nowrap', verticalAlign: 'top' }}>Sala {itens[0].salaNumero}<span style={{ marginLeft: '6px', fontSize: '10px', backgroundColor: '#f0eeea', padding: '2px 6px', borderRadius: '4px', color: '#888', fontWeight: 600 }}>{itens[0].salaTipo.replace('TRES_D', '3D')}</span></td>
                          <td style={{ padding: '12px 16px', verticalAlign: 'top' }}>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '5px' }}>
                              {itens.map((item, ii) => (
                                <span key={ii} style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', backgroundColor: '#f0f4ff', border: '1px solid #d0d9f5', borderRadius: '6px', padding: '3px 9px', fontSize: '12px' }}>
                                  <strong style={{ color: '#1a1a1a' }}>{item.quantidade}×</strong>
                                  <span style={{ color: '#333' }}>{item.produto}</span>
                                  <span style={{ color: '#999', fontSize: '11px' }}>· {fmtBRL(item.valorTotal)}</span>
                                </span>
                              ))}
                            </div>
                          </td>
                          <td style={{ padding: '12px 16px', fontWeight: 700, color: cores.vinho, whiteSpace: 'nowrap', verticalAlign: 'top' }}>{fmtBRL(totalSessao)}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              );
            })()}
          </div>
        )}

        {abaAtiva === 'ocupacao' && (
          <div>
            {carregandoOcupacao ? <div style={{ padding: '24px', textAlign: 'center', color: '#888', fontSize: '13px' }}>Carregando...</div>
            : ocupacaoSessao.length === 0 ? <div style={{ padding: '24px', textAlign: 'center', color: '#bbb', fontSize: '13px' }}>Nenhuma sessão encontrada para o período selecionado.</div>
            : (() => {
              return (
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                  <thead>
                    <tr style={{ backgroundColor: '#f9f8f5', borderBottom: `1px solid ${cores.borda}` }}>
                      {['Horário', 'Filme', 'Sala', 'Vendidos', 'Capacidade', 'Ocupação'].map(h => (
                        <th key={h} style={{ padding: '10px 16px', textAlign: 'left', fontSize: '11px', fontWeight: 600, color: '#888', textTransform: 'uppercase', letterSpacing: '0.5px' }}>{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {ocupacaoSessao.map((s, i) => {
                      const taxa = (s.capacidade > 0 && s.diasVigencia > 0) ? (s.ingressosVendidos / (s.capacidade * s.diasVigencia)) * 100 : 0;
                      const cor = taxa >= 75 ? '#2E7D32' : taxa >= 40 ? '#F57C00' : '#C62828';
                      const bg = taxa >= 75 ? '#E8F5E9' : taxa >= 40 ? '#FFF3E0' : '#FFEBEE';
                      return (
                        <tr key={i} style={{ borderBottom: `1px solid #f0eeea` }}>
                          <td style={{ padding: '10px 16px', color: '#555' }}>{s.horario}</td>
                          <td style={{ padding: '10px 16px', fontWeight: 500, color: '#1a1a1a' }}>{s.filme}</td>
                          <td style={{ padding: '10px 16px', color: '#555' }}>Sala {s.salaNumero}<span style={{ marginLeft: '6px', fontSize: '10px', backgroundColor: '#f0eeea', padding: '2px 6px', borderRadius: '4px', color: '#888', fontWeight: 600 }}>{s.salaTipo.replace('TRES_D', '3D')}</span></td>
                          <td style={{ padding: '10px 16px', fontWeight: 600, color: '#1a1a1a' }}>{s.ingressosVendidos}</td>
                          <td style={{ padding: '10px 16px', color: '#555' }}>{s.capacidade * s.diasVigencia}</td>
                          <td style={{ padding: '10px 16px' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                              <div style={{ flex: 1, maxWidth: '120px', height: '6px', backgroundColor: '#f0eeea', borderRadius: '3px', overflow: 'hidden' }}>
                                <div style={{ width: `${Math.min(taxa, 100)}%`, height: '100%', backgroundColor: cor, borderRadius: '3px' }} />
                              </div>
                              <span style={{ fontSize: '12px', fontWeight: 700, color: cor, backgroundColor: bg, padding: '2px 8px', borderRadius: '4px', minWidth: '48px', textAlign: 'center' }}>{taxa.toFixed(1)}%</span>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              );
            })()}
          </div>
        )}
      </div>

    </div>
  );
}

