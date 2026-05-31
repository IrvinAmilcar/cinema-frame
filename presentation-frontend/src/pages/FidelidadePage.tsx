import { useEffect, useState } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import type { ClienteLogado } from '../components/AuthModal'

const COR = '#1565C0'
const AMARELO = '#f59e0b'

interface Lancamento {
  pontosOriginais: number
  saldoAtual: number
  validade: string
  dataCriacao: string
  status: 'ATIVO' | 'EXPIRADO'
  descricao?: string
}

interface Recompensa {
  id: string
  nome: string
  tipo: string
  pontosNecessarios: number
  categoria?: string
  preco?: number
  disponivel: boolean
}

interface Resgate {
  beneficioId: string
  pontosDebitados: number
  data: string
  horario?: string
  descricao?: string
  nomeBeneficio?: string
}

interface VoucherSalvo {
  nome: string
  codigo: string
  data: string
  horario: string
}

interface Props {
  cliente: ClienteLogado | null
}

const TIPO_EMOJI: Record<string, string> = {
  INGRESSO_GRATIS: '🎟️',
  DESCONTO_PERCENTUAL: '💰',
  UPGRADE_ASSENTO: '⬆️',
  PIPOCA_GRATIS: '🍿',
  PRODUTO_BOMBONIERE: '🛍️',
  COMBO: '🍿',
  BEBIDA: '🥤',
  DOCES: '🍬',
  SALGADOS: '🥨',
}

const CATEGORIA_EMOJI: Record<string, string> = {
  COMBO: '🍿',
  BEBIDA: '🥤',
  DOCES: '🍬',
  SALGADOS: '🥨',
}

function formatarData(iso: string): string {
  try {
    const [ano, mes, dia] = iso.split('-')
    return `${dia}/${mes}/${ano}`
  } catch { return iso }
}

function parsearDescricao(descricao: string): { titulo: string; detalhes: string } {
  if (!descricao) return { titulo: 'Compra de ingresso', detalhes: '' }
  const sepIdx = descricao.indexOf(' — ')
  if (sepIdx === -1) return { titulo: descricao, detalhes: '' }
  const resto = descricao.slice(sepIdx + 3)
  const partes = resto.split(' · ')
  const filme = partes[0] ?? ''
  const extras = partes.slice(1).join(' · ')
  return { titulo: 'Compra de ingresso — ' + filme, detalhes: extras }
}

const STORAGE_KEY = (clienteId: string) => `vouchers_${clienteId}`

function carregarVouchers(clienteId: string): VoucherSalvo[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY(clienteId))
    return raw ? JSON.parse(raw) : []
  } catch { return [] }
}

function salvarVoucher(clienteId: string, voucher: VoucherSalvo) {
  const lista = carregarVouchers(clienteId)
  lista.unshift(voucher)
  localStorage.setItem(STORAGE_KEY(clienteId), JSON.stringify(lista))
}

export default function FidelidadePage({ cliente }: Props) {
  const [saldo, setSaldo] = useState<number | null>(null)
  const [lancamentos, setLancamentos] = useState<Lancamento[]>([])
  const [recompensas, setRecompensas] = useState<Recompensa[]>([])
  const [historico, setHistorico] = useState<Resgate[]>([])
  const [vouchers, setVouchers] = useState<VoucherSalvo[]>([])
  const [aba, setAba] = useState<'extrato' | 'recompensas' | 'resgatados'>('extrato')
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState<{ texto: string; tipo: 'ok' | 'erro' } | null>(null)

  const mostrarMsg = (texto: string, tipo: 'ok' | 'erro') => {
    setMsg({ texto, tipo })
    setTimeout(() => setMsg(null), 8000)
  }

  const carregar = async () => {
    if (!cliente) return
    setLoading(true)
    const hoje = new Date().toISOString().split('T')[0]
    try {
      const [rSaldo, rExt, rRec, rHist] = await Promise.all([
        fetch(`/api/fidelidade/${cliente.clienteId}/saldo`),
        fetch(`/api/fidelidade/${cliente.clienteId}/extrato`),
        fetch(`/api/fidelidade/${cliente.clienteId}/recompensas?data=${hoje}`),
        fetch(`/api/fidelidade/${cliente.clienteId}/historico`),
      ])
      if (rSaldo.ok) { const d = await rSaldo.json(); setSaldo(d.saldoAtivo) }
      if (rExt.ok) setLancamentos(await rExt.json())
      if (rRec.ok) setRecompensas(await rRec.json())
      if (rHist.ok) setHistorico(await rHist.json())
    } catch {
      mostrarMsg('Erro ao carregar dados de fidelidade.', 'erro')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (cliente) {
      carregar()
      setVouchers(carregarVouchers(cliente.clienteId))
    }
  }, [cliente?.clienteId])

  const resgatarBeneficio = async (id: string) => {
    if (!cliente) return
    const hoje = new Date().toISOString().split('T')[0]
    try {
      const res = await fetch(
        `/api/fidelidade/${cliente.clienteId}/resgatar/${id}?data=${hoje}`,
        { method: 'POST' }
      )
      if (res.ok) {
        mostrarMsg('Recompensa resgatada com sucesso!', 'ok')
        carregar()
      } else {
        const err = await res.json().catch(() => ({}))
        mostrarMsg(err.message || 'Não foi possível resgatar.', 'erro')
      }
    } catch { mostrarMsg('Erro de conexão.', 'erro') }
  }

  const resgatarProduto = async (recompensa: Recompensa) => {
    if (!cliente) return
    const hoje = new Date().toISOString().split('T')[0]
    try {
      const res = await fetch(
        `/api/fidelidade/${cliente.clienteId}/resgatar-produto/${recompensa.id}?data=${hoje}`,
        { method: 'POST' }
      )
      if (res.ok) {
        const data = await res.json()
        const agora = new Date()
        const novoVoucher: VoucherSalvo = {
          nome: recompensa.nome,
          codigo: data.voucher,
          data: agora.toISOString().split('T')[0],
          horario: agora.toTimeString().slice(0, 5),
        }
        salvarVoucher(cliente.clienteId, novoVoucher)
        setVouchers(carregarVouchers(cliente.clienteId))
        mostrarMsg(
          `Recompensa resgatada! Vá para a aba de Produtos Resgatados para acessar o QR Code.`,
          'ok'
        )
        carregar()
      } else {
        const err = await res.json().catch(() => ({}))
        mostrarMsg(err.message || 'Não foi possível resgatar.', 'erro')
      }
    } catch { mostrarMsg('Erro de conexão.', 'erro') }
  }

  const resgatar = (r: Recompensa) => {
    if (r.tipo === 'PRODUTO_BOMBONIERE') resgatarProduto(r)
    else resgatarBeneficio(r.id)
  }

  if (!cliente) {
    return (
      <div style={{ textAlign: 'center', padding: '60px 0', color: '#888' }}>
        <div style={{ fontSize: 48, marginBottom: 12 }}>🔒</div>
        <p style={{ fontSize: 15 }}>Faça login para acessar o programa de fidelidade.</p>
      </div>
    )
  }

  type LinhaExtrato =
    | { tipo: 'entrada'; data: string; pts: number; descricao: string; status: string }
    | { tipo: 'saida'; data: string; pts: number; descricao: string; horario?: string; nomeBeneficio?: string }

  const linhasExtrato: LinhaExtrato[] = [
    ...lancamentos.map(l => ({
      tipo: 'entrada' as const,
      data: l.dataCriacao,
      pts: l.pontosOriginais,
      descricao: l.descricao ?? 'Compra de ingresso',
      status: l.status,
    })),
    ...historico.map(r => ({
      tipo: 'saida' as const,
      data: r.data,
      pts: r.pontosDebitados,
      descricao: r.descricao ?? 'Pontos usados em resgate',
      horario: r.horario,
      nomeBeneficio: r.nomeBeneficio,
    })),
  ].sort((a, b) => {
    const dataDiff = b.data.localeCompare(a.data)
    if (dataDiff !== 0) return dataDiff
    return a.descricao.localeCompare(b.descricao)
  })

  const produtosBomboniere = recompensas.filter(r => r.tipo === 'PRODUTO_BOMBONIERE')
  const beneficiosCadastrados = recompensas.filter(r => r.tipo !== 'PRODUTO_BOMBONIERE')

  const ABAS = [
    { key: 'extrato', label: 'Extrato de Pontos' },
    { key: 'recompensas', label: 'Resgatar Recompensas' },
    { key: 'resgatados', label: `Produtos Resgatados${vouchers.length > 0 ? ` (${vouchers.length})` : ''}` },
  ] as const

  return (
    <div style={{ maxWidth: 860 }}>
      {/* Header com saldo */}
      <div style={{
        background: COR, borderRadius: 14, padding: '28px 32px',
        color: 'white', marginBottom: 24, position: 'relative', overflow: 'hidden',
      }}>
        <div style={{ position: 'absolute', right: -20, top: -20, fontSize: 120, opacity: 0.07 }}>⭐</div>
        <div style={{ fontSize: 13, opacity: 0.8, marginBottom: 4 }}>{cliente.nome}</div>
        <div style={{ fontSize: 12, opacity: 0.65, marginBottom: 16 }}>{(cliente as any).email ?? ''}</div>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
          <span style={{ fontSize: 20, color: AMARELO }}>⭐</span>
          <span style={{ fontSize: 40, fontWeight: 800 }}>{loading ? '...' : (saldo ?? 0)}</span>
          <span style={{ fontSize: 16, opacity: 0.8 }}>pontos disponíveis</span>
        </div>
        <div style={{ fontSize: 12, opacity: 0.6, marginTop: 6 }}>
          Equivalente a R$ {((saldo ?? 0) / 100).toFixed(2)} em descontos
        </div>
      </div>

      {/* Feedback */}
      {msg && (
        <div style={{
          padding: '12px 16px', borderRadius: 8, marginBottom: 16,
          background: msg.tipo === 'ok' ? '#f0fdf4' : '#fff1f2',
          color: msg.tipo === 'ok' ? '#16a34a' : '#dc2626',
          border: `1px solid ${msg.tipo === 'ok' ? '#bbf7d0' : '#fecaca'}`,
          fontWeight: 500, fontSize: 14, display: 'flex', alignItems: 'center', gap: 8,
        }}>
          <span>{msg.tipo === 'ok' ? '✅' : '❌'}</span>
          <span>{msg.texto}</span>
          {msg.tipo === 'ok' && msg.texto.includes('Produtos Resgatados') && (
            <button onClick={() => setAba('resgatados')}
              style={{
                marginLeft: 'auto', padding: '4px 12px', borderRadius: 6,
                background: '#16a34a', color: 'white', border: 'none',
                cursor: 'pointer', fontSize: 12, fontWeight: 600,
              }}>
              Ver QR Code →
            </button>
          )}
        </div>
      )}

      {/* Abas */}
      <div style={{ display: 'flex', borderBottom: '2px solid #e5e7eb', marginBottom: 20 }}>
        {ABAS.map(a => (
          <button key={a.key} onClick={() => setAba(a.key)}
            style={{
              padding: '10px 16px', border: 'none', background: 'none',
              cursor: 'pointer', fontSize: 13, fontWeight: 600,
              color: aba === a.key ? COR : '#888',
              borderBottom: aba === a.key ? `3px solid ${COR}` : '3px solid transparent',
              marginBottom: -2, whiteSpace: 'nowrap',
            }}>
            {a.label}
          </button>
        ))}
        <button onClick={carregar} style={{
          marginLeft: 'auto', padding: '6px 12px', borderRadius: 6,
          border: '1px solid #e5e7eb', background: 'white', color: '#666',
          fontSize: 12, cursor: 'pointer', alignSelf: 'center',
        }}>🔄</button>
      </div>

      {/* Aba: Extrato */}
      {aba === 'extrato' && (
        <div>
          <div style={{
            background: '#1a1a2e', borderRadius: 12, padding: '20px 24px',
            marginBottom: 20, color: 'white',
          }}>
            <div style={{ fontSize: 12, opacity: 0.6, marginBottom: 4 }}>Saldo Atual</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <span style={{ color: AMARELO, fontSize: 20 }}>⭐</span>
              <span style={{ fontSize: 32, fontWeight: 800 }}>{saldo ?? 0} pontos</span>
            </div>
            <div style={{ fontSize: 12, opacity: 0.5, marginTop: 4 }}>
              = R$ {((saldo ?? 0) / 100).toFixed(2)} em compras
            </div>
          </div>

          {linhasExtrato.length === 0 ? (
            <div style={{ textAlign: 'center', color: '#aaa', padding: 40 }}>
              Nenhum lançamento ainda. Faça uma compra para começar a acumular!
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              {linhasExtrato.map((l, i) => {
                const { titulo, detalhes } = parsearDescricao(l.descricao)
                const isFirst = i === 0
                const isLast = i === linhasExtrato.length - 1
                const ehSaida = l.tipo === 'saida'
                const saida = ehSaida ? (l as Extract<LinhaExtrato, { tipo: 'saida' }>) : null

                return (
                  <div key={i} style={{
                    background: 'white', padding: '14px 16px',
                    borderRadius: isFirst && isLast ? 10 : isFirst ? '10px 10px 0 0' : isLast ? '0 0 10px 10px' : 0,
                    borderBottom: !isLast ? '1px solid #f3f4f6' : 'none',
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    boxShadow: isFirst ? '0 1px 3px rgba(0,0,0,0.05)' : 'none',
                  }}>
                    <div>
                      <div style={{ fontWeight: 500, fontSize: 14, color: '#1a1a2e' }}>{titulo}</div>
                      <div style={{ fontSize: 12, color: '#9ca3af', marginTop: 3 }}>
                        {formatarData(l.data)}
                        {saida?.horario && saida.horario !== '00:00' && (
                          <span style={{ marginLeft: 6 }}>· {saida.horario}</span>
                        )}
                        {saida?.nomeBeneficio && (
                          <span style={{ marginLeft: 6 }}>· {saida.nomeBeneficio}</span>
                        )}
                        {!ehSaida && detalhes && (
                          <span style={{ marginLeft: 6, color: '#6b7280' }}>· {detalhes}</span>
                        )}
                        {!ehSaida && (l as any).status === 'EXPIRADO' && (
                          <span style={{ marginLeft: 8, color: '#ef4444', fontWeight: 600 }}>· Expirado</span>
                        )}
                      </div>
                    </div>
                    <span style={{ fontWeight: 700, fontSize: 15, color: ehSaida ? '#ef4444' : '#16a34a' }}>
                      {ehSaida ? '-' : '+'}{l.pts} pts
                    </span>
                  </div>
                )
              })}
            </div>
          )}
        </div>
      )}

      {/* Aba: Recompensas */}
      {aba === 'recompensas' && (
        <div>
          <div style={{
            background: '#fffbeb', border: '1px solid #fde68a', borderRadius: 10,
            padding: '12px 16px', marginBottom: 24, fontSize: 13, color: '#92400e',
          }}>
            <strong>Seus pontos: {saldo ?? 0} pts</strong>
            {' · '}100 pontos = R$ 1,00 · Produtos da bomboniere custam metade do preço em pontos.
          </div>

          {produtosBomboniere.length > 0 && (
            <div style={{ marginBottom: 28 }}>
              <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1a1a2e', marginBottom: 14 }}>
                🍿 Bomboniere
              </h3>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                {produtosBomboniere.map(r => {
                  const emoji = CATEGORIA_EMOJI[r.categoria ?? ''] ?? '🛍️'
                  return (
                    <div key={r.id} style={{
                      background: 'white', borderRadius: 12, padding: '16px',
                      border: `1px solid ${r.disponivel ? '#e5e7eb' : '#f3f4f6'}`,
                      boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
                      opacity: r.disponivel ? 1 : 0.6,
                    }}>
                      <div style={{ fontSize: 32, marginBottom: 8 }}>{emoji}</div>
                      <div style={{ fontWeight: 700, fontSize: 14, color: '#1a1a2e', marginBottom: 2 }}>
                        {r.nome}
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: AMARELO }}>
                          <span>⭐</span>
                          <span style={{ fontWeight: 700, fontSize: 14 }}>{r.pontosNecessarios} pts</span>
                        </div>
                        <button onClick={() => resgatar(r)} disabled={!r.disponivel}
                          style={{
                            padding: '7px 14px', borderRadius: 8, border: 'none',
                            background: r.disponivel ? COR : '#e5e7eb',
                            color: r.disponivel ? 'white' : '#9ca3af',
                            fontWeight: 600, fontSize: 12,
                            cursor: r.disponivel ? 'pointer' : 'default',
                          }}>
                          {r.disponivel ? 'Resgatar' : 'Pts insuficientes'}
                        </button>
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>
          )}

          {beneficiosCadastrados.length > 0 && (
            <div>
              <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1a1a2e', marginBottom: 14 }}>
                🎁 Benefícios Exclusivos
              </h3>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                {beneficiosCadastrados.map(r => {
                  const emoji = TIPO_EMOJI[r.tipo] ?? '🎁'
                  return (
                    <div key={r.id} style={{
                      background: 'white', borderRadius: 12, padding: '16px',
                      border: `1px solid ${r.disponivel ? '#e5e7eb' : '#f3f4f6'}`,
                      boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
                      opacity: r.disponivel ? 1 : 0.6,
                    }}>
                      <div style={{ fontSize: 32, marginBottom: 8 }}>{emoji}</div>
                      <div style={{ fontWeight: 700, fontSize: 14, color: '#1a1a2e', marginBottom: 12 }}>
                        {r.nome}
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: AMARELO }}>
                          <span>⭐</span>
                          <span style={{ fontWeight: 700, fontSize: 14 }}>{r.pontosNecessarios} pts</span>
                        </div>
                        <button onClick={() => resgatar(r)} disabled={!r.disponivel}
                          style={{
                            padding: '7px 14px', borderRadius: 8, border: 'none',
                            background: r.disponivel ? COR : '#e5e7eb',
                            color: r.disponivel ? 'white' : '#9ca3af',
                            fontWeight: 600, fontSize: 12,
                            cursor: r.disponivel ? 'pointer' : 'default',
                          }}>
                          {r.disponivel ? 'Resgatar' : 'Pts insuficientes'}
                        </button>
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>
          )}

          {produtosBomboniere.length === 0 && beneficiosCadastrados.length === 0 && (
            <div style={{ textAlign: 'center', color: '#aaa', padding: 48 }}>
              <div style={{ fontSize: 40, marginBottom: 12 }}>🎁</div>
              <p style={{ fontSize: 14 }}>Nenhuma recompensa disponível no momento.</p>
            </div>
          )}
        </div>
      )}

      {/* Aba: Produtos Resgatados */}
      {aba === 'resgatados' && (
        <div>
          {vouchers.length === 0 ? (
            <div style={{ textAlign: 'center', color: '#aaa', padding: 48 }}>
              <div style={{ fontSize: 40, marginBottom: 12 }}>🎁</div>
              <p style={{ fontSize: 14 }}>Nenhum produto resgatado ainda.</p>
              <p style={{ fontSize: 13, color: '#bbb' }}>
                Resgate produtos da bomboniere com seus pontos para ver os QR Codes aqui.
              </p>
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
              {vouchers.map((v, i) => (
                <div key={i} style={{
                  background: 'white', borderRadius: 14, overflow: 'hidden',
                  border: '1px solid #e5e7eb', boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
                  textAlign: 'center',
                }}>
                  {/* Ilustração */}
                  <svg width="100%" viewBox="0 0 680 200" style={{ display: 'block' }}>
                    <rect width="680" height="200" fill="#0f1729"/>
                    <circle cx="60" cy="40" r="2" fill="#f59e0b" opacity="0.7"/>
                    <circle cx="120" cy="25" r="1.5" fill="#f59e0b" opacity="0.5"/>
                    <circle cx="560" cy="35" r="2" fill="#f59e0b" opacity="0.6"/>
                    <circle cx="620" cy="55" r="1.5" fill="white" opacity="0.5"/>
                    <circle cx="350" cy="15" r="2" fill="#f59e0b" opacity="0.5"/>
                    <circle cx="480" cy="45" r="1.5" fill="white" opacity="0.3"/>
                    {/* balde */}
                    <path d="M270 115 L292 175 L388 175 L410 115 Z" fill="#e53e3e"/>
                    <path d="M283 115 L302 175 L312 175 L292 115 Z" fill="white" opacity="0.2"/>
                    <path d="M345 115 L364 175 L374 175 L355 115 Z" fill="white" opacity="0.2"/>
                    <ellipse cx="340" cy="115" rx="70" ry="10" fill="#c53030"/>
                    <rect x="270" y="110" width="140" height="16" rx="3" fill="#fc8181"/>
                    <ellipse cx="340" cy="110" rx="70" ry="9" fill="#fc8181"/>
                    {/* pipocas */}
                    <ellipse cx="300" cy="100" rx="13" ry="11" fill="#fefcbf"/>
                    <ellipse cx="290" cy="93" rx="10" ry="8" fill="#fffde7"/>
                    <ellipse cx="338" cy="92" rx="15" ry="12" fill="#fffde7"/>
                    <ellipse cx="348" cy="83" rx="12" ry="10" fill="#fefcbf"/>
                    <ellipse cx="325" cy="85" rx="10" ry="8" fill="#fffde7"/>
                    <ellipse cx="372" cy="98" rx="13" ry="11" fill="#fefcbf"/>
                    <ellipse cx="382" cy="90" rx="10" ry="8" fill="#fffde7"/>
                    <ellipse cx="340" cy="76" rx="12" ry="10" fill="#fffde7"/>
                    {/* copo */}
                    <rect x="428" y="88" width="5" height="60" rx="2.5" fill="#e53e3e"/>
                    <path d="M400 118 L413 175 L463 175 L474 118 Z" fill="#ebf8ff"/>
                    <path d="M400 118 L413 175 L418 175 L406 118 Z" fill="white" opacity="0.5"/>
                    <rect x="408" y="130" width="10" height="8" rx="2" fill="white" opacity="0.55"/>
                    <rect x="424" y="142" width="9" height="7" rx="2" fill="white" opacity="0.5"/>
                    <rect x="443" y="133" width="10" height="8" rx="2" fill="white" opacity="0.5"/>
                    <path d="M402 152 L413 175 L463 175 L472 152 Z" fill="#90cdf4" opacity="0.7"/>
                    <ellipse cx="437" cy="118" rx="37" ry="7" fill="#bee3f8"/>
                    <path d="M400 118 Q437 104 474 118" fill="#e0f2fe"/>
                    {/* ingresso */}
                    <rect x="172" y="120" width="86" height="50" rx="7" fill="#553c9a"/>
                    <circle cx="172" cy="141" r="5" fill="#0f1729"/>
                    <circle cx="172" cy="151" r="5" fill="#0f1729"/>
                    <circle cx="258" cy="141" r="5" fill="#0f1729"/>
                    <circle cx="258" cy="151" r="5" fill="#0f1729"/>
                    <line x1="177" y1="145.5" x2="253" y2="145.5" stroke="#b794f4" strokeWidth="1.5" strokeDasharray="4 3"/>
                    <text x="215" y="138" textAnchor="middle" fontSize="8" fontWeight="500" fill="#e9d8fd" fontFamily="sans-serif">VOUCHER</text>
                    <text x="215" y="159" textAnchor="middle" fontSize="7" fill="#b794f4" fontFamily="sans-serif">BOMBONIERE</text>
                    {/* estrela */}
                    <polygon points="130,70 135,85 151,85 138,94 143,109 130,100 117,109 122,94 109,85 125,85" fill="#f59e0b" opacity="0.85"/>
                    <polygon points="550,60 554,72 567,72 557,80 561,92 550,84 539,92 543,80 533,72 546,72" fill="#f59e0b" opacity="0.7"/>
                    <path d="M500 105 L502 112 L509 114 L502 116 L500 123 L498 116 L491 114 L498 112 Z" fill="#fbd38d" opacity="0.8"/>
                  </svg>

                  {/* Info do voucher */}
                  <div style={{ padding: '16px 16px 20px' }}>
                    <div style={{ fontWeight: 700, fontSize: 15, color: '#1a1a2e', marginBottom: 2 }}>
                      {v.nome}
                    </div>
                    <div style={{ fontSize: 12, color: '#9ca3af', marginBottom: 14 }}>
                      {formatarData(v.data)} · {v.horario}
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 10 }}>
                      <QRCodeSVG value={v.codigo} size={130} level="H" includeMargin />
                    </div>
                    <div style={{
                      fontFamily: 'monospace', fontSize: 10, color: '#aaa',
                      wordBreak: 'break-all', padding: '0 4px',
                    }}>
                      {v.codigo}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}


