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
  descricao?: string
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

export default function FidelidadePage({ cliente }: Props) {
  const [saldo, setSaldo] = useState<number | null>(null)
  const [lancamentos, setLancamentos] = useState<Lancamento[]>([])
  const [recompensas, setRecompensas] = useState<Recompensa[]>([])
  const [historico, setHistorico] = useState<Resgate[]>([])
  const [aba, setAba] = useState<'extrato' | 'recompensas'>('extrato')
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState<{ texto: string; tipo: 'ok' | 'erro' } | null>(null)
  const [voucher, setVoucher] = useState<{ nome: string; codigo: string } | null>(null)

  const mostrarMsg = (texto: string, tipo: 'ok' | 'erro') => {
    setMsg({ texto, tipo })
    setTimeout(() => setMsg(null), 5000)
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

  useEffect(() => { carregar() }, [cliente?.clienteId])

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
        setVoucher({ nome: recompensa.nome, codigo: data.voucher })
        mostrarMsg(`${recompensa.nome} resgatado! Apresente o voucher na bomboniere.`, 'ok')
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
    | { tipo: 'saida'; data: string; pts: number; descricao: string }

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
    })),
  ].sort((a, b) => {
    const dataDiff = b.data.localeCompare(a.data)
    if (dataDiff !== 0) return dataDiff
    return a.descricao.localeCompare(b.descricao)
  })

  const produtosBomboniere = recompensas.filter(r => r.tipo === 'PRODUTO_BOMBONIERE')
  const beneficiosCadastrados = recompensas.filter(r => r.tipo !== 'PRODUTO_BOMBONIERE')

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
          fontWeight: 500, fontSize: 14,
        }}>
          {msg.tipo === 'ok' ? '✅ ' : '❌ '}{msg.texto}
        </div>
      )}

      {/* Voucher QR Code */}
      {voucher && (
        <div style={{
          background: 'white', border: `2px solid ${COR}`, borderRadius: 14,
          padding: '20px 24px', marginBottom: 20, textAlign: 'center',
        }}>
          <div style={{ fontWeight: 700, fontSize: 16, color: COR, marginBottom: 4 }}>
            🎁 Voucher — {voucher.nome}
          </div>
          <p style={{ color: '#888', fontSize: 13, marginBottom: 16 }}>
            Apresente este QR Code na bomboniere para retirar seu produto.
          </p>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 12 }}>
            <QRCodeSVG value={voucher.codigo} size={160} level="H" includeMargin />
          </div>
          <div style={{ fontFamily: 'monospace', fontSize: 13, color: '#555', marginBottom: 12 }}>
            {voucher.codigo}
          </div>
          <button onClick={() => setVoucher(null)}
            style={{ padding: '6px 16px', borderRadius: 6, border: '1px solid #ccc',
              background: 'white', cursor: 'pointer', fontSize: 13, color: '#666' }}>
            Fechar
          </button>
        </div>
      )}

      {/* Abas */}
      <div style={{ display: 'flex', borderBottom: '2px solid #e5e7eb', marginBottom: 20 }}>
        {(['extrato', 'recompensas'] as const).map(a => (
          <button key={a} onClick={() => setAba(a)}
            style={{
              padding: '10px 20px', border: 'none', background: 'none',
              cursor: 'pointer', fontSize: 14, fontWeight: 600,
              color: aba === a ? COR : '#888',
              borderBottom: aba === a ? `3px solid ${COR}` : '3px solid transparent',
              marginBottom: -2,
            }}>
            {a === 'extrato' ? 'Extrato de Pontos' : 'Resgatar Recompensas'}
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
                        {detalhes && <span style={{ marginLeft: 6, color: '#6b7280' }}>· {detalhes}</span>}
                        {l.tipo === 'entrada' && (l as any).status === 'EXPIRADO' && (
                          <span style={{ marginLeft: 8, color: '#ef4444', fontWeight: 600 }}>· Expirado</span>
                        )}
                      </div>
                    </div>
                    <span style={{ fontWeight: 700, fontSize: 15, color: l.tipo === 'entrada' ? '#16a34a' : '#ef4444' }}>
                      {l.tipo === 'entrada' ? '+' : '-'}{l.pts} pts
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

          {/* Produtos da Bomboniere */}
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

          {/* Benefícios Cadastrados */}
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
    </div>
  )
}
