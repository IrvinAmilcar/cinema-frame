import React, { useState, useEffect, FormEvent } from 'react';

interface Sessao {
  id: string;
  filmeTitulo: string;
  salaNumero: number;
  inicio: string;
  fim: string;
}

interface Historico {
  total: number;
  aprovados: number;
  negados: number;
}

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=DM+Mono:wght@400;500&display=swap');

.checkin-root * { box-sizing: border-box; margin: 0; padding: 0; }

  .checkin-root {
    font-family: 'DM Sans', sans-serif;
    background: #f4f4f5;
    min-height: 100%;
     margin: -1.5rem;
  }

  /* ── Seleção de Sessão ── */
  .sessao-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 2rem;
    background: #f4f4f5;
  }

  .sessao-header {
    text-align: center;
    margin-bottom: 2rem;
  }

  .sessao-header .qr-icon {
    width: 64px;
    height: 64px;
    background: #8b1a1a;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 1rem;
    font-size: 32px;
  }

  .sessao-header h1 {
    font-size: 1.6rem;
    font-weight: 700;
    color: #18181b;
  }

  .sessao-header p {
    color: #71717a;
    margin-top: 0.3rem;
    font-size: 0.95rem;
  }

  .sessao-lista {
    display: flex;
    flex-direction: column;
    gap: 10px;
    width: 100%;
    max-width: 560px;
  }

  .sessao-card {
    background: white;
    border: 1.5px solid #e4e4e7;
    border-radius: 12px;
    padding: 1.1rem 1.4rem;
    cursor: pointer;
    text-align: left;
    transition: border-color 0.15s, box-shadow 0.15s, transform 0.1s;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  }

  .sessao-card:hover {
    border-color: #8b1a1a;
    box-shadow: 0 4px 16px rgba(139,26,26,0.1);
    transform: translateY(-1px);
  }

  .sessao-card-titulo {
    font-size: 1.05rem;
    font-weight: 600;
    color: #18181b;
    margin-bottom: 6px;
  }

  .sessao-card-info {
    font-size: 0.875rem;
    color: #71717a;
    display: flex;
    gap: 16px;
  }

  .sessao-vazia {
    color: #a1a1aa;
    text-align: center;
    padding: 2rem;
    font-size: 0.95rem;
  }

  /* ── Layout principal do Check-in ── */
  .checkin-layout {
    display: flex;
    min-height: 100%;
  }

  /* Coluna esquerda */
  .checkin-main {
    flex: 1 1 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding: 2rem 2rem 2rem;
    background: #f4f4f5;
    min-width: 0;
  }

  .checkin-topbar {
    width: 100%;
    max-width: 520px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1.5rem;
  }

  .checkin-sessao-info {
    font-size: 0.875rem;
    color: #52525b;
    font-weight: 500;
  }

  .btn-trocar {
    background: white;
    border: 1.5px solid #e4e4e7;
    border-radius: 8px;
    padding: 6px 14px;
    font-size: 0.8rem;
    color: #52525b;
    cursor: pointer;
    font-family: 'DM Sans', sans-serif;
    font-weight: 500;
    transition: border-color 0.15s;
  }

  .btn-trocar:hover { border-color: #a1a1aa; }

  /* Cartão central */
  .checkin-card {
    background: white;
    border-radius: 20px;
    box-shadow: 0 4px 24px rgba(0,0,0,0.07);
    width: 100%;
    max-width: 520px;
    padding: 2.5rem 2rem 2rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0;
    transition: box-shadow 0.3s;
  }

  .checkin-card-icon {
    width: 60px;
    height: 60px;
    background: #8b1a1a;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-bottom: 0.75rem;
  }

  .checkin-card-title {
    font-size: 1.1rem;
    font-weight: 700;
    color: #18181b;
    margin-bottom: 0.25rem;
  }

  .checkin-card-subtitle {
    font-size: 0.85rem;
    color: #a1a1aa;
    margin-bottom: 1.5rem;
  }

  /* Área de feedback */
  .feedback-area {
    width: 100%;
    min-height: 140px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    margin-bottom: 1.5rem;
    border-radius: 12px;
    background: #fafafa;
    border: 1.5px dashed #e4e4e7;
    padding: 1.5rem;
    transition: all 0.3s;
  }

  .feedback-area.aprovado {
    background: #f0fdf4;
    border: 1.5px solid #bbf7d0;
  }

  .feedback-area.negado {
    background: #fff1f2;
    border: 1.5px solid #fecdd3;
  }

  .feedback-icon-circle {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
  }

  .feedback-icon-circle.aprovado { background: #dcfce7; }
  .feedback-icon-circle.negado   { background: #fee2e2; }
  .feedback-icon-circle.aguardando { background: #f4f4f5; }

  .feedback-label {
    font-size: 1rem;
    font-weight: 600;
    color: #18181b;
  }

  .feedback-label.aprovado { color: #166534; }
  .feedback-label.negado   { color: #9f1239; }
  .feedback-label.aguardando { color: #71717a; }

  .feedback-sub {
    font-size: 0.8rem;
    color: #71717a;
    text-align: center;
  }

  .feedback-motivo {
    background: #fff1f2;
    border: 1px solid #fecdd3;
    border-radius: 8px;
    padding: 0.6rem 1rem;
    font-size: 0.85rem;
    color: #9f1239;
    font-weight: 500;
    text-align: center;
    width: 100%;
    margin-top: 4px;
  }

  /* Input + botão */
  .checkin-form {
    display: flex;
    gap: 8px;
    width: 100%;
  }

  .checkin-input {
    flex: 1;
    padding: 12px 16px;
    font-size: 1rem;
    font-family: 'DM Mono', monospace;
    border: 1.5px solid #e4e4e7;
    border-radius: 10px;
    outline: none;
    color: #18181b;
    background: #fafafa;
    transition: border-color 0.15s;
  }

  .checkin-input:focus { border-color: #8b1a1a; background: white; }
  .checkin-input::placeholder { color: #a1a1aa; font-family: 'DM Sans', sans-serif; font-size: 0.9rem; }

  .checkin-btn {
    padding: 12px 20px;
    background: #8b1a1a;
    color: white;
    border: none;
    border-radius: 10px;
    font-size: 0.95rem;
    font-weight: 600;
    cursor: pointer;
    font-family: 'DM Sans', sans-serif;
    transition: background 0.15s;
    white-space: nowrap;
  }

  .checkin-btn:hover { background: #701414; }

  /* Instruções */
  .instrucoes {
    width: 100%;
    max-width: 520px;
    margin-top: 1.5rem;
    background: white;
    border-radius: 14px;
    padding: 1.2rem 1.5rem;
    border: 1.5px solid #e4e4e7;
  }

  .instrucoes-titulo {
    font-size: 0.8rem;
    font-weight: 600;
    color: #52525b;
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 0.75rem;
  }

  .instrucoes ol {
    padding-left: 1.2rem;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .instrucoes ol li {
    font-size: 0.82rem;
    color: #71717a;
    line-height: 1.5;
  }

  .valida-lista {
    margin-top: 0.75rem;
    padding-top: 0.75rem;
    border-top: 1px solid #f4f4f5;
  }

  .valida-titulo {
    font-size: 0.8rem;
    font-weight: 600;
    color: #52525b;
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 0.5rem;
  }

  .valida-lista ul {
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  .valida-lista ul li {
    font-size: 0.8rem;
    color: #71717a;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .valida-lista ul li::before {
    content: '●';
    color: #8b1a1a;
    font-size: 0.5rem;
  }

  /* Coluna direita — histórico */
  .historico-sidebar {
    width: 260px;
    flex-shrink: 0;
    background: white;
    border-left: 1.5px solid #e4e4e7;
    padding: 2rem 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .historico-titulo {
    font-size: 0.95rem;
    font-weight: 700;
    color: #18181b;
    margin-bottom: 0.25rem;
  }

  .historico-sub {
    font-size: 0.78rem;
    color: #a1a1aa;
    margin-bottom: 0.5rem;
  }

  .hist-card {
    border-radius: 10px;
    padding: 1rem;
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .hist-card.verde  { background: #f0fdf4; }
  .hist-card.vermelho { background: #fff1f2; }
  .hist-card.azul   { background: #eff6ff; }

  .hist-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .hist-dot.verde    { background: #22c55e; }
  .hist-dot.vermelho { background: #ef4444; }
  .hist-dot.azul     { background: #3b82f6; }

  .hist-info { flex: 1; }

  .hist-label {
    font-size: 0.75rem;
    color: #71717a;
    font-weight: 500;
  }

  .hist-num {
    font-size: 1.5rem;
    font-weight: 700;
    color: #18181b;
    line-height: 1.2;
  }

  .tabs {
    display: flex;
    gap: 6px;
    margin-bottom: 0.5rem;
  }

  .tab {
    padding: 6px 14px;
    border-radius: 8px;
    border: 1.5px solid #e4e4e7;
    background: white;
    font-size: 0.8rem;
    font-weight: 600;
    cursor: pointer;
    font-family: 'DM Sans', sans-serif;
    color: #52525b;
    transition: all 0.15s;
  }

  .tab.ativo {
    background: #8b1a1a;
    border-color: #8b1a1a;
    color: white;
  }
`;

export function CheckInPage() {
  const [sessoes, setSessoes] = useState<Sessao[]>([]);
  const [sessaoSelecionada, setSessaoSelecionada] = useState<Sessao | null>(null);
  const [codigoIngresso, setCodigoIngresso] = useState('');
  const [status, setStatus] = useState<'aguardando' | 'aprovado' | 'negado'>('aguardando');
  const [mensagemNegado, setMensagemNegado] = useState('');
  const [historico, setHistorico] = useState<Historico>({ total: 0, aprovados: 0, negados: 0 });

  const funcionarioId = "987e6543-e21b-12d3-a456-426614174000";

  useEffect(() => {
    const carregarSessoes = async () => {
      try {
        const resposta = await fetch('/api/grades');
        const grades = await resposta.json();
        const todasSessoes: Sessao[] = grades.flatMap((g: any) => g.sessoes ?? []);
        setSessoes(todasSessoes);
      } catch (error) {
        console.error("Erro ao carregar sessões:", error);
      }
    };
    carregarSessoes();
  }, []);

  const carregarHistorico = async (sessaoId: string) => {
    try {
      const resposta = await fetch(`/api/checkin/historico-hoje?sessaoId=${sessaoId}`);
      if (resposta.ok) {
        const dados = await resposta.json();
        setHistorico({ total: dados.total ?? 0, aprovados: dados.aprovados ?? 0, negados: dados.negados ?? 0 });
      }
    } catch (error) {
      console.error("Erro ao carregar histórico:", error);
    }
  };

  const selecionarSessao = (sessao: Sessao) => {
    setSessaoSelecionada(sessao);
    setStatus('aguardando');
    setMensagemNegado('');
    carregarHistorico(sessao.id);
  };

const handleValidar = async (e: FormEvent) => {
  e.preventDefault();
  if (!codigoIngresso.trim() || !sessaoSelecionada) return;

  setStatus('aguardando');
  setMensagemNegado('');

  try {
    const resposta = await fetch('/api/checkin/validar', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ingressoId: codigoIngresso,
        sessaoId: sessaoSelecionada.id,
        funcionarioId
      })
    });

    const dados = await resposta.json();

    if (dados.sucesso) {
      setStatus('aprovado');
      setMensagemNegado('');
    } else {
      setStatus('negado');
      setMensagemNegado(dados.mensagem ?? 'Ingresso inválido');
    }
  } catch {
    setStatus('negado');
    setMensagemNegado('Erro de comunicação com o servidor.');
  } finally {
    carregarHistorico(sessaoSelecionada.id); // sempre executa
    setCodigoIngresso('');
  }
};

  // ── Tela de seleção de sessão ──
  if (!sessaoSelecionada) {
    return (
      <>
        <style>{styles}</style>
        <div className="checkin-root">
          <div className="sessao-page">
            <div className="sessao-header">
              <div className="qr-icon">🎟️</div>
              <h1>Check-in de Ingressos</h1>
              <p>Selecione a sessão para iniciar a validação</p>
            </div>

            <div className="sessao-lista">
              {sessoes.length === 0 ? (
                <div className="sessao-vazia">Nenhuma sessão disponível no momento.</div>
              ) : sessoes.map(sessao => (
                <button key={sessao.id} className="sessao-card" onClick={() => selecionarSessao(sessao)}>
                  <div className="sessao-card-titulo">{sessao.filmeTitulo}</div>
                  <div className="sessao-card-info">
                    <span>🕐 {sessao.inicio} – {sessao.fim}</span>
                    <span>🎬 Sala {sessao.salaNumero}</span>
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>
      </>
    );
  }

  // ── Tela de check-in ──
  const feedbackIcon = status === 'aprovado' ? '✅' : status === 'negado' ? '❌' : '📷';
  const feedbackLabel = status === 'aprovado' ? 'Check-in Aprovado!' : status === 'negado' ? 'Check-in Negado' : 'Aguardando QR Code';
  const feedbackSub = status === 'aprovado' ? 'Ingresso válido. Entrada autorizada.' : status === 'negado' ? 'Não foi possível validar este ingresso.' : 'Aponte a câmera ou insira o código do ingresso';

  return (
    <>
      <style>{styles}</style>
      <div className="checkin-root">
        <div className="checkin-layout">

          {/* Coluna principal */}
          <div className="checkin-main">
            <div className="checkin-topbar">
              <div className="checkin-sessao-info">
                <strong>{sessaoSelecionada.filmeTitulo}</strong>
                &nbsp;·&nbsp;{sessaoSelecionada.inicio}–{sessaoSelecionada.fim}
                &nbsp;·&nbsp;Sala {sessaoSelecionada.salaNumero}
              </div>
              <button className="btn-trocar" onClick={() => setSessaoSelecionada(null)}>
                ← Trocar sessão
              </button>
            </div>

            <div className="checkin-card">
              <div className="checkin-card-icon">📲</div>
              <div className="checkin-card-title">Validação de Ingresso</div>
              <div className="checkin-card-subtitle">Aponte a câmera para o QR Code do ingresso</div>

              {/* Feedback */}
              <div className={`feedback-area ${status}`}>
                <div className={`feedback-icon-circle ${status}`} style={{ fontSize: 36 }}>
                  {feedbackIcon}
                </div>
                <div className={`feedback-label ${status}`}>{feedbackLabel}</div>
                <div className="feedback-sub">{feedbackSub}</div>
                {status === 'negado' && mensagemNegado && (
                  <div className="feedback-motivo">
                    <strong>Motivo da recusa:</strong><br />{mensagemNegado}
                  </div>
                )}
              </div>

              {/* Input */}
              <form className="checkin-form" onSubmit={handleValidar}>
                <input
                  className="checkin-input"
                  type="text"
                  value={codigoIngresso}
                  onChange={e => setCodigoIngresso(e.target.value)}
                  placeholder="Cole ou escaneie o código do ingresso..."
                  autoFocus
                />
                <button className="checkin-btn" type="submit">Validar</button>
              </form>
            </div>

            {/* Instruções */}
            <div className="instrucoes">
              <div className="instrucoes-titulo">📋 Instruções para o operador:</div>
              <ol>
                <li>Solicite o QR Code do ingresso ao cliente</li>
                <li>Aguarde a validação automática do sistema</li>
                <li>Em caso de recusa, verifique o motivo e oriente o cliente</li>
                <li>Para dúvidas, consulte o gerente de plantão</li>
              </ol>
              <div className="valida-lista">
                <div className="valida-titulo">✅ O sistema valida automaticamente:</div>
                <ul>
                  <li>Se o ingresso pertence à sessão correta</li>
                  <li>Se o ingresso já foi utilizado anteriormente</li>
                  <li>Se está dentro do horário permitido de entrada</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Sidebar histórico */}
          <div className="historico-sidebar">
            <div>
              <div className="historico-titulo">Histórico de Hoje</div>
              <div className="historico-sub">Acompanhamento da sessão atual</div>
            </div>

            <div className="hist-card verde">
              <div className="hist-dot verde" />
              <div className="hist-info">
                <div className="hist-label">Aprovadas</div>
                <div className="hist-num">{historico.aprovados}</div>
              </div>
            </div>

            <div className="hist-card vermelho">
              <div className="hist-dot vermelho" />
              <div className="hist-info">
                <div className="hist-label">Recusadas</div>
                <div className="hist-num">{historico.negados}</div>
              </div>
            </div>

            <div className="hist-card azul">
              <div className="hist-dot azul" />
              <div className="hist-info">
                <div className="hist-label">Total de Leituras</div>
                <div className="hist-num">{historico.total}</div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </>
  );
}