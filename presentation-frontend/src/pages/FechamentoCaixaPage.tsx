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
  const [carregandoResumo, setCarregandoResumo] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [sucesso, setSucesso] = useState<string | null>(null);

  // Busca o resumo do período sempre que as datas mudam
  const buscarResumo = useCallback(async (inicio: string, fim: string) => {
    if (!inicio || !fim || inicio > fim) return;
    setCarregandoResumo(true);
    try {
      const res = await apiCaixa.resumoPorPeriodo(inicio, fim);
      setResumo(res);
    } catch {
      setResumo(null);
    } finally {
      setCarregandoResumo(false);
    }
  }, []);

  useEffect(() => {
    buscarResumo(dataInicio, dataFim);
  }, [dataInicio, dataFim, buscarResumo]);

  function handleDataFimChange(valor: string) {
    if (valor < dataInicio) {
      setErroData('A data final não pode ser anterior à data inicial.');
      return;
    }
    setErroData(null);
    setDataFim(valor);
  }

  function handleDataInicioChange(valor: string) {
    setDataInicio(valor);
    if (dataFim < valor) {
      setDataFim(valor);
    }
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

  function removerLinha(id: number) {
    setLinhas(prev => prev.filter(l => l.id !== id));
  }

  function adicionarLinha() {
    setLinhas(prev => [...prev, novaLinha()]);
  }

  function getVendas(): VendaDiaRequest[] {
    return linhas.map(l => ({
      sessaoId: l.sessaoId || `sessao-${Math.random().toString(36).slice(2, 7)}`,
      capacidadeSala: parseInt(l.capacidadeSala) || 0,
      ingressosVendidos: parseInt(l.ingressosVendidos) || 0,
      valorArrecadado: parseFloat(l.valorArrecadado) || 0,
    }));
  }

  // Valores dos cards: usa resumo do back se disponível, senão calcula localmente da tabela
  const valorIngressos = resumo?.totalIngressosValor ?? 0;
  const valorBomboniere = resumo?.totalBomboniere ?? 0;
  const totalDescontoPontos = resumo?.totalDescontoPontos ?? 0;
  // Receita Total = ingressos + bomboniere - descontos de fidelidade
  const receitaTotal = valorIngressos + valorBomboniere - totalDescontoPontos;

  function mostrarSucesso(msg: string) {
    setSucesso(msg); setErro(null);
    setTimeout(() => setSucesso(null), 4000);
  }

  function mostrarErro(msg: string) {
    setErro(msg); setSucesso(null);
    setTimeout(() => setErro(null), 5000);
  }

  async function handleFecharCaixa() {
    const vendas = getVendas();
    if (!vendas.length) { mostrarErro('Adicione ao menos uma sessão.'); return; }
    setCarregando(true);
    try {
      const res = await apiCaixa.fecharCaixa({
        data: dataInicio,
        momentoFechamento: getMomentoFechamento(),
        vendas,
      });
      setResultado(res);
      mostrarSucesso('Caixa fechado com sucesso!');
      buscarResumo(dataInicio, dataFim);
    } catch (e: any) {
      mostrarErro(e.message || 'Erro ao fechar o caixa.');
    } finally {
      setCarregando(false);
    }
  }

  async function handleConsultarRelatorio() {
    setCarregando(true);
    try {
      const res = await apiCaixa.consultarRelatorio(dataInicio);
      setResultado(res);
      mostrarSucesso('Relatório carregado.');
    } catch (e: any) {
      mostrarErro(e.message || 'Nenhum fechamento encontrado para essa data.');
    } finally {
      setCarregando(false);
    }
  }

  const inputStyle: React.CSSProperties = {
    width: '100%', padding: '8px 10px', border: `1px solid ${cores.borda}`,
    borderRadius: '6px', fontSize: '13px', backgroundColor: 'white',
  };

  const btnPrimario: React.CSSProperties = {
    backgroundColor: cores.vinho, color: 'white', border: 'none',
    padding: '10px 20px', borderRadius: '8px', fontSize: '14px',
    fontWeight: 600, cursor: carregando ? 'not-allowed' : 'pointer',
    opacity: carregando ? 0.7 : 1, display: 'flex', alignItems: 'center', gap: '8px',
  };

  const btnSecundario: React.CSSProperties = {
    backgroundColor: 'white', color: '#555', border: `1px solid ${cores.borda}`,
    padding: '10px 20px', borderRadius: '8px', fontSize: '14px',
    fontWeight: 500, cursor: carregando ? 'not-allowed' : 'pointer',
    display: 'flex', alignItems: 'center', gap: '8px',
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif', backgroundColor: cores.fundo, minHeight: '100vh' }}>

      {/* Cabeçalho */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
        <div>
          <h1 style={{ color: '#1a1a1a', margin: 0, fontSize: '22px', fontWeight: 600 }}>Fechamento de Caixa</h1>
          <p style={{ color: '#888', margin: '4px 0 0', fontSize: '13px' }}>Consolidação financeira e relatórios</p>
        </div>
        <button
          onClick={() => {
            if (!resultado) { mostrarErro('Nenhum relatório para exportar.'); return; }
            const csv = [
              'Campo,Valor',
              `Data,${resultado.data}`,
              `Total de Vendas,${resultado.totalVendas}`,
              `Total de Ingressos,${resultado.totalIngressos}`,
              `Total de Sessões,${resultado.totalSessoes}`,
              `Taxa de Ocupação Média,${resultado.taxaOcupacaoMedia.toFixed(1)}%`,
              `Momento do Fechamento,${resultado.momentoFechamento}`,
            ].join('\n');
            const a = document.createElement('a');
            a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
            a.download = `fechamento-${resultado.data}.csv`;
            a.click();
          }}
          style={{ backgroundColor: cores.verdeTexto, color: 'white', border: 'none', padding: '9px 16px', borderRadius: '8px', fontSize: '13px', fontWeight: 600, cursor: 'pointer' }}
        >
          ↓ Exportar CSV
        </button>
      </div>

      {/* Alertas */}
      {erro && (
        <div style={{ backgroundColor: cores.vermelhoClaro, color: cores.vermelhoTexto, border: `1px solid #FFCDD2`, borderRadius: '8px', padding: '12px 16px', marginBottom: '16px', fontSize: '14px', fontWeight: 500 }}>
          ⚠ {erro}
        </div>
      )}
      {sucesso && (
        <div style={{ backgroundColor: cores.verdeClaro, color: cores.verdeTexto, border: `1px solid #C8E6C9`, borderRadius: '8px', padding: '12px 16px', marginBottom: '16px', fontSize: '14px', fontWeight: 500 }}>
          ✓ {sucesso}
        </div>
      )}

      {/* Filtro de datas */}
      <div style={{ display: 'flex', gap: '16px', marginBottom: '20px' }}>
        <div style={{ flex: 1 }}>
          <label style={{ fontSize: '12px', color: '#888', fontWeight: 600, display: 'block', marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Data Inicial
          </label>
          <input
            type="date"
            value={dataInicio}
            onChange={e => handleDataInicioChange(e.target.value)}
            style={inputStyle}
          />
        </div>
        <div style={{ flex: 1 }}>
          <label style={{ fontSize: '12px', color: '#888', fontWeight: 600, display: 'block', marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Data Final
          </label>
          <input
            type="date"
            value={dataFim}
            onChange={e => handleDataFimChange(e.target.value)}
            style={inputStyle}
          />
        </div>
      </div>

      {erroData && (
        <div style={{ backgroundColor: cores.vermelhoClaro, color: cores.vermelhoTexto, border: `1px solid #FFCDD2`, borderRadius: '8px', padding: '10px 14px', marginBottom: '16px', fontSize: '13px', fontWeight: 500 }}>
          ⚠ {erroData}
        </div>
      )}

      {/* Cards de métricas — alimentados pelo resumo do período */}
      <div style={{ display: 'flex', gap: '14px', marginBottom: '20px' }}>
        <div style={{ flex: 1, backgroundColor: 'white', border: `1px solid ${cores.borda}`, borderRadius: '10px', padding: '16px 18px' }}>
          <div style={{ fontSize: '11px', color: '#999', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            🎟 Ingressos
            {carregandoResumo && <span style={{ fontSize: '10px', color: '#bbb' }}>atualizando...</span>}
          </div>
          <div style={{ fontSize: '22px', fontWeight: 600, color: '#1a1a1a' }}>
            {fmtBRL(valorIngressos)}
          </div>
          {resumo && <div style={{ fontSize: '12px', color: '#999', marginTop: '2px' }}>{resumo.totalIngressos} ingressos vendidos</div>}
        </div>
        <div style={{ flex: 1, backgroundColor: 'white', border: `1px solid ${cores.borda}`, borderRadius: '10px', padding: '16px 18px' }}>
          <div style={{ fontSize: '11px', color: '#999', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '6px' }}>
            🍿 Bomboniere
          </div>
          <div style={{ fontSize: '22px', fontWeight: 600, color: '#1a1a1a' }}>{fmtBRL(valorBomboniere)}</div>
        </div>
        <div style={{ flex: 1, backgroundColor: 'white', border: `1px solid ${cores.borda}`, borderRadius: '10px', padding: '16px 18px' }}>
          <div style={{ fontSize: '11px', color: '#999', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '6px' }}>
            🎫 Descontos Fidelidade
          </div>
          <div style={{ fontSize: '22px', fontWeight: 600, color: cores.vermelhoTexto }}>- {fmtBRL(totalDescontoPontos)}</div>
        </div>
        <div style={{ flex: 1, backgroundColor: cores.vinho, border: `1px solid ${cores.vinho}`, borderRadius: '10px', padding: '16px 18px' }}>
          <div style={{ fontSize: '11px', color: 'rgba(255,255,255,0.7)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '6px' }}>
            Receita Total
          </div>
          <div style={{ fontSize: '22px', fontWeight: 600, color: 'white' }}>{fmtBRL(receitaTotal)}</div>
        </div>
      </div>
    </div>
  )
}
