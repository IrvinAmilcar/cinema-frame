import { useState, useEffect, useRef } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import { useMoviePoster } from '../hooks/useMoviePoster'
import type { ClienteLogado } from '../components/AuthModal'

const API = 'http://localhost:8080'

function getDias() {
  const dias = []
  const hoje = new Date()
  const semana = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']
  const meses = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez']
  for (let i = 0; i < 5; i++) {
    const d = new Date(hoje)
    d.setDate(hoje.getDate() + i)
    const iso = d.toISOString().slice(0, 10)
    const label = i === 0 ? 'Hoje' : `${semana[d.getDay()]} ${d.getDate()} ${meses[d.getMonth()]}`
    dias.push({ label, value: iso })
  }
  return dias
}
const DIAS = getDias()
const COR = '#8B0000'
const COR_PORTAL = '#1565C0'
const COLS = 12

const CLASSIFICACAO_IDADE: Record<string, number> = {
  LIVRE: 0, DEZ: 10, DOZE: 12, QUATORZE: 14, DEZESSEIS: 16, DEZOITO: 18,
}

function calcularIdade(dataNascimento: string): number {
  if (!dataNascimento) return -1
  const nasc = new Date(dataNascimento)
  const hoje = new Date()
  let idade = hoje.getFullYear() - nasc.getFullYear()
  const m = hoje.getMonth() - nasc.getMonth()
  if (m < 0 || (m === 0 && hoje.getDate() < nasc.getDate())) idade--
  return idade
}

async function extrairMensagemErro(res: Response): Promise<string> {
  try {
    const json = await res.json()
    return json.mensagem || json.message || JSON.stringify(json)
  } catch {
    return await res.text()
  }
}

function calcularPontosAGanhar(valor: number): number {
  const base = Math.floor(valor)
  if (valor >= 500) return Math.floor(base * 3.0)
  if (valor >= 200) return Math.floor(base * 2.0)
  if (valor >= 100) return Math.floor(base * 1.5)
  return base
}

interface SessaoDisponivel {
  id: string
  filme: string
  genero: string
  classificacao: string
  inicio: string
  capacidade: number
  precoInteira: number
  precoMeia: number
  tipoSala: string
}

interface DescontoResponse {
  valorOriginal: number
  valorDesconto: number
  valorFinal: number
  cumulativo: boolean
}

type TipoElegibilidade = 'ESTUDANTE' | 'IDOSO' | 'PCD'

interface DadosPessoa {
  tipo: 'INTEIRA' | 'MEIA'
  dataNascimento: string
  tipoElegibilidade: TipoElegibilidade | ''
  comprovante: string
  arquivoSelecionado: string
}

function pessoaVazia(tipo: 'INTEIRA' | 'MEIA'): DadosPessoa {
  return { tipo, dataNascimento: '', tipoElegibilidade: tipo === 'MEIA' ? 'ESTUDANTE' : '', comprovante: '', arquivoSelecionado: '' }
}

function reconstruirPessoas(prev: DadosPessoa[], novaInteira: number, novaMeia: number): DadosPessoa[] {
  const inteiras = Array.from({ length: novaInteira }, (_, i) =>
    prev.filter(p => p.tipo === 'INTEIRA')[i] ?? pessoaVazia('INTEIRA'))
  const meias = Array.from({ length: novaMeia }, (_, i) =>
    prev.filter(p => p.tipo === 'MEIA')[i] ?? pessoaVazia('MEIA'))
  return [...inteiras, ...meias]
}

type Etapa = 'sessao' | 'ingresso' | 'assento' | 'bomboniere' | 'cupom' | 'confirmacao' | 'sucesso'
const ETAPAS: Etapa[] = ['sessao', 'ingresso', 'assento', 'bomboniere', 'cupom', 'confirmacao', 'sucesso']

interface ProdutoBomboniere {
  id: string
  nome: string
  preco: number
  categoria: string
  ativo: boolean
}

interface ItemCarrinho {
  produto: ProdutoBomboniere
  qtd: number
}

function numToLabel(n: number) {
  const row = String.fromCharCode(65 + Math.floor((n - 1) / COLS))
  const col = ((n - 1) % COLS) + 1
  return `${row}${col}`
}

export default function CompraPage({ cliente }: { cliente: ClienteLogado | null }) {
  const [etapa, setEtapa] = useState<Etapa>('sessao')
  const [sessoes, setSessoes] = useState<SessaoDisponivel[]>([])
  const [sessao, setSessao] = useState<SessaoDisponivel | null>(null)
  const [dataSelecionada, setDataSelecionada] = useState(DIAS[0].value)
  const [qtdInteira, setQtdInteira] = useState(0)
  const [qtdMeia, setQtdMeia] = useState(0)
  const [pessoas, setPessoas] = useState<DadosPessoa[]>([])
  const [assentosSelecionados, setAssentosSelecionados] = useState<number[]>([])
  const [assentosOcupados, setAssentosOcupados] = useState<number[]>([])
  const [pedidoId, setPedidoId] = useState<string | null>(null)
  const [codigoCupom, setCodigoCupom] = useState('')
  const [desconto, setDesconto] = useState<DescontoResponse | null>(null)
  const [erroCupom, setErroCupom] = useState('')
  const [qrCodes, setQrCodes] = useState<{ assento: string; codigo: string }[]>([])
  const [voucherQr, setVoucherQr] = useState<string | null>(null)
  const [produtos, setProdutos] = useState<ProdutoBomboniere[]>([])
  const [carrinho, setCarrinho] = useState<ItemCarrinho[]>([])
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState('')
  const [tempoRestante, setTempoRestante] = useState(600)
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)

  // fidelidade
  const [saldoPontos, setSaldoPontos] = useState<number | null>(null)
  const [usarPontos, setUsarPontos] = useState(false)
  const [pontosGanhos, setPontosGanhos] = useState(0)
  // estados de fidelidade
  const [avisoLimite, setAvisoLimite] = useState(false)
  const [limiteMensalAtingido, setLimiteMensalAtingido] = useState(false)

  const quantidade = qtdInteira + qtdMeia
  const precoInteira = sessao?.precoInteira ?? 0
  const precoMeia = sessao?.precoMeia ?? 0
  const subtotal = qtdInteira * precoInteira + qtdMeia * precoMeia
  const precoFinal = desconto ? desconto.valorFinal : subtotal
  const totalBomboniere = carrinho.reduce((s, i) => s + i.produto.preco * i.qtd, 0)
  const totalSemPontos = precoFinal + totalBomboniere
  const descontoPontosReais = usarPontos && saldoPontos ? parseFloat((saldoPontos / 100).toFixed(2)) : 0
  const totalFinal = Math.max(0, totalSemPontos - descontoPontosReais)
  const pontosQueVaiGanhar = calcularPontosAGanhar(totalFinal)
  const etapaIdx = ETAPAS.indexOf(etapa)

  const ingressoResumo = [
    qtdInteira > 0 ? `${qtdInteira}× Inteira` : '',
    qtdMeia > 0 ? `${qtdMeia}× Meia` : '',
  ].filter(Boolean).join(' + ')

  useEffect(() => {
    setPessoas(prev => reconstruirPessoas(prev, qtdInteira, qtdMeia))
  }, [qtdInteira, qtdMeia])

  const atualizarPessoa = (idx: number, campo: Partial<DadosPessoa>) =>
    setPessoas(prev => prev.map((p, i) => i === idx ? { ...p, ...campo } : p))

  const idadeMinima = sessao ? (CLASSIFICACAO_IDADE[sessao.classificacao] ?? 0) : 0

  function erroIdade(p: DadosPessoa): string | null {
    if (!p.dataNascimento) return null
    const idade = calcularIdade(p.dataNascimento)
    if (idade < 0) return 'Data de nascimento inválida.'
    if (idadeMinima > 0 && idade < idadeMinima)
      return `Idade mínima para este filme é ${idadeMinima} anos. Esta pessoa tem ${idade} anos.`
    return null
  }

  const pessoasValidas = pessoas.every(p => {
    if (!p.dataNascimento) return false
    if (erroIdade(p)) return false
    if (p.tipo === 'MEIA') {
      if (!p.tipoElegibilidade) return false
      if (p.tipoElegibilidade === 'ESTUDANTE' && !p.comprovante.trim()) return false
      if (p.tipoElegibilidade === 'IDOSO' && !p.comprovante.trim()) return false
      if (p.tipoElegibilidade === 'PCD' && !p.arquivoSelecionado) return false
    }
    return true
  })

  useEffect(() => {
    fetch(`${API}/api/programacao/sessoes?data=${dataSelecionada}`)
      .then(r => r.json())
      .then(setSessoes)
      .catch(() => setErro('Não foi possível carregar as sessões disponíveis.'))
  }, [dataSelecionada])

  useEffect(() => {
    if (etapa === 'assento') {
      setTempoRestante(600)
      timerRef.current = setInterval(() => {
        setTempoRestante(t => {
          if (t <= 1) { clearInterval(timerRef.current!); return 0 }
          return t - 1
        })
      }, 1000)
    } else {
      if (timerRef.current) clearInterval(timerRef.current)
    }
    return () => { if (timerRef.current) clearInterval(timerRef.current) }
  }, [etapa])

  const formatTimer = (s: number) =>
    `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`

  const selecionarSessao = async (s: SessaoDisponivel) => {
    setCarregando(true)
    setErro('')
    try {
      const res = await fetch(`${API}/api/pedido`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessaoId: s.id, clienteId: cliente?.clienteId ?? null }),
      })
      if (!res.ok) throw new Error()
      const data = await res.json()
      setPedidoId(data.pedidoId)
      setSessao(s)
      if (cliente?.clienteId) {
        const mesAtual = new Date().toISOString().slice(0, 7) // YYYY-MM
        Promise.all([
          fetch(`${API}/api/fidelidade/${cliente.clienteId}/saldo`).then(r => r.ok ? r.json() : null),
          fetch(`${API}/api/fidelidade/${cliente.clienteId}/extrato`).then(r => r.ok ? r.json() : []),
        ]).then(([saldoData, extrato]) => {
          if (saldoData) setSaldoPontos(saldoData.saldoAtivo)
          // Calcula pontos acumulados no mês atual
          const pontosNoMes = (extrato as any[]).filter((l: any) =>
            l.dataCriacao?.startsWith(mesAtual)
          ).reduce((sum: number, l: any) => sum + (l.pontosOriginais ?? 0), 0)
          setLimiteMensalAtingido(pontosNoMes >= 10000)
        }).catch(() => {})
      }
      setEtapa('ingresso')
    } catch {
      setErro('Erro ao iniciar pedido. Tente novamente.')
    } finally {
      setCarregando(false)
    }
  }

  const irParaAssentos = async () => {
    if (!sessao) return
    setCarregando(true)
    try {
      const res = await fetch(`${API}/api/sessao/${sessao.id}/assentos-ocupados?data=${dataSelecionada}`)
      const ocupados: number[] = res.ok ? await res.json() : []
      setAssentosOcupados(ocupados)
      setAssentosSelecionados([])
      setEtapa('assento')
    } catch {
      setAssentosOcupados([])
      setEtapa('assento')
    } finally {
      setCarregando(false)
    }
  }

  const toggleAssento = (n: number) => {
    if (assentosOcupados.includes(n)) return
    if (assentosSelecionados.includes(n)) {
      setAssentosSelecionados(prev => prev.filter(a => a !== n))
      return
    }
    if (assentosSelecionados.length >= quantidade) return
    setAssentosSelecionados(prev => [...prev, n])
  }

  const confirmarAssentos = async () => {
    if (!pedidoId || !sessao) return
    setCarregando(true)
    setErro('')
    try {
      for (const n of assentosSelecionados) {
        const res = await fetch(`${API}/api/reserva`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ sessaoId: sessao.id, numeroAssento: n, data: dataSelecionada }),
        })
        if (!res.ok) {
          setErro(`Assento ${numToLabel(n)} já foi reservado por outro usuário. Escolha novamente.`)
          const ocupados = await fetch(`${API}/api/sessao/${sessao.id}/assentos-ocupados`).then(r => r.json()).catch(() => [])
          setAssentosOcupados(ocupados)
          setAssentosSelecionados([])
          return
        }
        const reserva = await res.json()
        if (n === assentosSelecionados[0]) {
          await fetch(`${API}/api/pedido/${pedidoId}/reserva`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reservaId: reserva.id }),
          })
        }
      }
      const resp = await fetch(`${API}/bomboniere/produtos`)
      const lista: ProdutoBomboniere[] = resp.ok
        ? (await resp.json()).filter((p: ProdutoBomboniere) => p.ativo)
        : []
      setProdutos(lista)
      setEtapa('bomboniere')
    } catch {
      setErro('Erro ao reservar assentos. Tente novamente.')
    } finally {
      setCarregando(false)
    }
  }

  const alterarCarrinho = (produto: ProdutoBomboniere, delta: number) => {
    setCarrinho(prev => {
      const existe = prev.find(i => i.produto.id === produto.id)
      if (!existe) return delta > 0 ? [...prev, { produto, qtd: 1 }] : prev
      const novaQtd = existe.qtd + delta
      if (novaQtd <= 0) return prev.filter(i => i.produto.id !== produto.id)
      return prev.map(i => i.produto.id === produto.id ? { ...i, qtd: novaQtd } : i)
    })
  }

  const aplicarCupom = async () => {
    if (!codigoCupom.trim() || !pedidoId) return
    setErroCupom('')
    try {
      const res = await fetch(`${API}/api/pedido/${pedidoId}/cupom`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ codigo: codigoCupom, valorTotal: subtotal, quantidadeIngressos: quantidade }),
      })
      if (!res.ok) { setErroCupom('Cupom inválido ou não encontrado.'); setDesconto(null); return }
      const resultado: DescontoResponse = await res.json()
      if (desconto && !desconto.cumulativo) {
        setErroCupom('Cupom não cumulativo já aplicado. Não é possível combinar cupons.')
        return
      }
      setDesconto(resultado)
    } catch { setErroCupom('Erro ao aplicar cupom.') }
  }

  const confirmarCompra = async () => {
    if (!pedidoId) return
    setCarregando(true)
    setErro('')
    try {
      for (const pessoa of pessoas) {
        const possuiElegibilidade = pessoa.tipo === 'INTEIRA' || !!(
          (pessoa.tipoElegibilidade === 'ESTUDANTE' && pessoa.comprovante.trim()) ||
          (pessoa.tipoElegibilidade === 'IDOSO' && pessoa.comprovante.trim()) ||
          (pessoa.tipoElegibilidade === 'PCD' && pessoa.arquivoSelecionado)
        )
        const res = await fetch(`${API}/api/pedido/${pedidoId}/ingresso`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            tipo: pessoa.tipo,
            quantidade: 1,
            possuiElegibilidade,
            dataNascimento: pessoa.dataNascimento || null,
          }),
        })
        if (!res.ok) {
          setErro(await extrairMensagemErro(res) || 'Não foi possível adicionar o ingresso.')
          return
        }
      }
      let voucherCodigo: string | null = null
      for (const item of carrinho) {
        const res = await fetch(`${API}/api/pedido/${pedidoId}/produto`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ produtoId: item.produto.id, quantidade: item.qtd }),
        })
        if (!res.ok) {
          setErro(await extrairMensagemErro(res) || `Estoque insuficiente para "${item.produto.nome}".`)
          return
        }
        const data = await res.json()
        voucherCodigo = data.voucher
      }
      const res = await fetch(`${API}/api/pedido/${pedidoId}/finalizar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          valorTotal: totalFinal,
          usarPontos: usarPontos && saldoPontos !== null && saldoPontos > 0,
          descontoPontos: (usarPontos && saldoPontos !== null && saldoPontos > 0) ? descontoPontosReais : 0,
          clienteId: cliente?.clienteId ?? null,
          valorIngressos: precoFinal,
          valorBomboniere: totalBomboniere,
          descontoCupom: desconto?.valorDesconto ?? 0,
        }),
      })
      if (!res.ok) throw new Error(await extrairMensagemErro(res) || 'Erro ao finalizar a compra.')
      // ── MUDANÇA 2: ler limitePontosAtingido da resposta ──
      const respData = await res.json()
      const codigosDoBackend = respData.qrCodes
      if (respData.limitePontosAtingido) setAvisoLimite(true)
      setQrCodes(assentosSelecionados.map((n, i) => ({
        assento: numToLabel(n),
        codigo: codigosDoBackend[i] ?? `QR-ASSENTO-${numToLabel(n)}`,
      })))
      setVoucherQr(voucherCodigo)
      setPontosGanhos(respData.limitePontosAtingido ? 0 : pontosQueVaiGanhar)
      setEtapa('sucesso')
    } catch (e: unknown) {
      setErro(e instanceof Error ? e.message : 'Erro ao finalizar a compra.')
    } finally {
      setCarregando(false)
    }
  }

  const reiniciar = () => {
    setEtapa('sessao'); setSessao(null); setPedidoId(null)
    setQtdInteira(0); setQtdMeia(0); setPessoas([])
    setAssentosSelecionados([]); setAssentosOcupados([])
    setCarrinho([])
    setCodigoCupom(''); setDesconto(null); setErroCupom('')
    setQrCodes([]); setVoucherQr(null); setErro('')
    setSaldoPontos(null); setUsarPontos(false); setPontosGanhos(0)
    setAvisoLimite(false)
    setLimiteMensalAtingido(false)
  }

  const totalAssentos = Math.min(sessao?.capacidade ?? 60, 96)
  const rows = Math.ceil(totalAssentos / COLS)

  return (
    <div style={{ maxWidth: 780, margin: '0 auto' }}>
      <h1 style={{ fontSize: 26, fontWeight: 700, marginBottom: 4 }}>Comprar Ingresso</h1>
      <p style={{ color: '#666', marginBottom: 20, fontSize: 14 }}>Escolha a sessão e finalize sua compra</p>

      <div style={{ display: 'flex', gap: 6, marginBottom: 28 }}>
        {ETAPAS.map((e, i) => (
          <div key={e} style={{ flex: 1, height: 3, borderRadius: 2, background: etapaIdx >= i ? COR_PORTAL : '#e0e0e0' }} />
        ))}
      </div>

      {erro && <div style={{ background: '#ffebee', border: '1px solid #ef9a9a', borderRadius: 8, padding: '10px 14px', marginBottom: 16, color: '#c62828', fontSize: 14 }}>{erro}</div>}

      {etapa === 'sessao' && (
        <div>
          <div style={{ display: 'flex', gap: 8, marginBottom: 20, flexWrap: 'wrap' }}>
            {DIAS.map(d => (
              <button key={d.value} onClick={() => setDataSelecionada(d.value)}
                style={{
                  padding: '7px 14px', borderRadius: 20, fontSize: 13, cursor: 'pointer',
                  border: dataSelecionada === d.value ? 'none' : '1px solid #ddd',
                  background: dataSelecionada === d.value ? COR_PORTAL : 'white',
                  color: dataSelecionada === d.value ? 'white' : '#555',
                  fontWeight: dataSelecionada === d.value ? 600 : 400,
                }}>
                {d.label}
              </button>
            ))}
          </div>
          {sessoes.length === 0
            ? <p style={{ color: '#888' }}>Nenhuma sessão disponível no momento.</p>
            : agruparPorFilme(sessoes).map(grupo => (
              <FilmeComSessoes key={grupo.filme} grupo={grupo} onSelecionar={selecionarSessao} carregando={carregando} />
            ))}
        </div>
      )}

      {etapa === 'ingresso' && sessao && (
        <div>
          <SessaoCard sessao={sessao} />
          <h2 style={{ fontSize: 17, marginBottom: 16 }}>Quantidade de ingressos</h2>
          <div style={{ border: '1px solid #e0e0e0', borderRadius: 10, overflow: 'hidden', marginBottom: 24 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '16px 20px', background: 'white' }}>
              <div>
                <div style={{ fontWeight: 600, fontSize: 15 }}>Inteira</div>
                <div style={{ color: '#666', fontSize: 13, marginTop: 2 }}>R$ {precoInteira.toFixed(2)}</div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                <BtnQtd onClick={() => setQtdInteira(q => Math.max(0, q - 1))}>−</BtnQtd>
                <span style={{ fontSize: 20, fontWeight: 700, minWidth: 24, textAlign: 'center' }}>{qtdInteira}</span>
                <BtnQtd onClick={() => setQtdInteira(q => Math.min(10 - qtdMeia, q + 1))}>+</BtnQtd>
              </div>
            </div>
            <div style={{ borderTop: '1px solid #f0f0f0' }} />
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '16px 20px', background: 'white' }}>
              <div>
                <div style={{ fontWeight: 600, fontSize: 15 }}>Meia-entrada</div>
                <div style={{ color: '#666', fontSize: 13, marginTop: 2 }}>R$ {precoMeia.toFixed(2)} <span style={{ fontSize: 11, color: '#aaa' }}>(estudante, idoso, PcD)</span></div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                <BtnQtd onClick={() => setQtdMeia(q => Math.max(0, q - 1))}>−</BtnQtd>
                <span style={{ fontSize: 20, fontWeight: 700, minWidth: 24, textAlign: 'center' }}>{qtdMeia}</span>
                <BtnQtd onClick={() => setQtdMeia(q => Math.min(10 - qtdInteira, q + 1))}>+</BtnQtd>
              </div>
            </div>
          </div>
          {pessoas.length > 0 && (
            <div style={{ marginBottom: 24 }}>
              <h2 style={{ fontSize: 17, marginBottom: 14 }}>Dados dos participantes</h2>
              {pessoas.map((p, i) => (
                <div key={i} style={{ border: '1px solid #e0e0e0', borderRadius: 10, padding: '16px 20px', marginBottom: 12, background: 'white' }}>
                  <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 14, color: p.tipo === 'MEIA' ? COR : COR_PORTAL }}>
                    Ingresso {i + 1} — {p.tipo === 'INTEIRA' ? 'Inteira' : 'Meia-entrada'}
                  </div>
                  <label style={{ fontSize: 13, color: '#555', display: 'block', marginBottom: 4 }}>
                    Data de nascimento <span style={{ color: '#c62828' }}>*</span>
                    {idadeMinima > 0 && <span style={{ color: '#888', fontWeight: 400 }}> — mínimo {idadeMinima} anos ({sessao!.classificacao})</span>}
                  </label>
                  <input type="date" value={p.dataNascimento}
                    onChange={e => atualizarPessoa(i, { dataNascimento: e.target.value })}
                    max={new Date().toISOString().split('T')[0]}
                    style={{ width: '100%', padding: '9px 12px', borderRadius: 7, border: `1px solid ${erroIdade(p) ? '#c62828' : '#ccc'}`, fontSize: 14, marginBottom: 4, boxSizing: 'border-box' as const }} />
                  {!p.dataNascimento ? (
                    <p style={{ color: '#888', fontSize: 12, margin: '0 0 12px' }}>Informe a data de nascimento para continuar.</p>
                  ) : erroIdade(p) ? (
                    <p style={{ color: '#c62828', fontSize: 12, margin: '0 0 12px', fontWeight: 500 }}>✕ {erroIdade(p)}</p>
                  ) : (
                    <p style={{ color: '#2e7d32', fontSize: 12, margin: '0 0 12px' }}>
                      ✓ {calcularIdade(p.dataNascimento)} anos — {idadeMinima > 0 ? `permitido (mínimo ${idadeMinima} anos).` : 'livre para todos.'}
                    </p>
                  )}
                  {p.tipo === 'MEIA' && (
                    <>
                      <label style={{ fontSize: 13, color: '#555', display: 'block', marginBottom: 4 }}>Tipo de elegibilidade <span style={{ color: '#c62828' }}>*</span></label>
                      <select value={p.tipoElegibilidade}
                        onChange={e => atualizarPessoa(i, { tipoElegibilidade: e.target.value as TipoElegibilidade, comprovante: '', arquivoSelecionado: '' })}
                        style={{ width: '100%', padding: '9px 12px', borderRadius: 7, border: '1px solid #ccc', fontSize: 14, marginBottom: 10, background: 'white', boxSizing: 'border-box' as const }}>
                        <option value="ESTUDANTE">Estudante</option>
                        <option value="IDOSO">Idoso (60+)</option>
                        <option value="PCD">Pessoa com Deficiência (PcD)</option>
                      </select>
                      {p.tipoElegibilidade === 'ESTUDANTE' && (
                        <>
                          <label style={{ fontSize: 13, color: '#555', display: 'block', marginBottom: 4 }}>Número da carteira estudantil <span style={{ color: '#c62828' }}>*</span></label>
                          <input type="text" value={p.comprovante} placeholder="Ex: 2024-BR-12345678"
                            onChange={e => atualizarPessoa(i, { comprovante: e.target.value })}
                            style={{ width: '100%', padding: '9px 12px', borderRadius: 7, border: '1px solid #ccc', fontSize: 14, boxSizing: 'border-box' as const }} />
                        </>
                      )}
                      {p.tipoElegibilidade === 'IDOSO' && (
                        <>
                          <label style={{ fontSize: 13, color: '#555', display: 'block', marginBottom: 4 }}>CPF do beneficiário <span style={{ color: '#c62828' }}>*</span></label>
                          <input type="text" value={p.comprovante} placeholder="Ex: 000.000.000-00"
                            onChange={e => atualizarPessoa(i, { comprovante: e.target.value })}
                            style={{ width: '100%', padding: '9px 12px', borderRadius: 7, border: '1px solid #ccc', fontSize: 14, boxSizing: 'border-box' as const }} />
                        </>
                      )}
                      {p.tipoElegibilidade === 'PCD' && (
                        <>
                          <label style={{ fontSize: 13, color: '#555', display: 'block', marginBottom: 4 }}>Laudo médico ou carteira PcD <span style={{ color: '#c62828' }}>*</span></label>
                          <input type="file" accept=".pdf,image/*"
                            onChange={e => atualizarPessoa(i, { arquivoSelecionado: e.target.files?.[0]?.name ?? '' })}
                            style={{ fontSize: 13, width: '100%' }} />
                          {p.arquivoSelecionado && <p style={{ fontSize: 12, color: '#2e7d32', marginTop: 4 }}>Arquivo: {p.arquivoSelecionado}</p>}
                        </>
                      )}
                    </>
                  )}
                </div>
              ))}
            </div>
          )}
          <div style={{ background: '#f5f5f5', borderRadius: 8, padding: '12px 16px', marginBottom: 24 }}>
            {quantidade === 0
              ? <p style={{ margin: 0, color: '#888', fontSize: 14 }}>Selecione ao menos 1 ingresso para continuar.</p>
              : <>
                  <p style={{ margin: '0 0 4px', fontSize: 14, color: '#555' }}>{ingressoResumo}</p>
                  <p style={{ margin: 0, fontWeight: 700, fontSize: 16 }}>Total: R$ {subtotal.toFixed(2)}</p>
                </>}
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <BtnVoltar onClick={reiniciar} />
            <BtnPrimario onClick={irParaAssentos} disabled={quantidade === 0 || !pessoasValidas || carregando}>
              Escolher assentos →
            </BtnPrimario>
          </div>
        </div>
      )}

      {etapa === 'assento' && sessao && (
        <div>
          <div style={{ background: COR, color: 'white', borderRadius: 12, padding: '12px 20px', marginBottom: 20, display: 'flex', alignItems: 'center', gap: 12 }}>
            <span style={{ fontSize: 20 }}>⏱</span>
            <div>
              <div style={{ fontSize: 12, opacity: 0.85 }}>Tempo restante para finalizar</div>
              <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: 1 }}>{formatTimer(tempoRestante)}</div>
            </div>
          </div>
          <h2 style={{ fontSize: 17, marginBottom: 4 }}>Escolha seu{quantidade > 1 ? 's' : ''} assento{quantidade > 1 ? 's' : ''}</h2>
          <p style={{ color: '#666', fontSize: 13, marginBottom: 20 }}>
            Selecione {quantidade} assento{quantidade > 1 ? 's' : ''}. ({assentosSelecionados.length}/{quantidade} selecionado{assentosSelecionados.length !== 1 ? 's' : ''})
          </p>
          <div style={{ marginBottom: 6 }}>
            <div style={{ background: COR, height: 5, borderRadius: 3 }} />
            <p style={{ textAlign: 'center', fontSize: 12, color: '#888', margin: '4px 0 18px' }}>Tela</p>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 24 }}>
            {Array.from({ length: rows }, (_, ri) => (
              <div key={ri} style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                <span style={{ width: 20, fontSize: 12, color: '#888', textAlign: 'right', flexShrink: 0 }}>
                  {String.fromCharCode(65 + ri)}
                </span>
                {Array.from({ length: COLS }, (_, ci) => {
                  const n = ri * COLS + ci + 1
                  if (n > totalAssentos) return <div key={ci} style={{ flex: 1 }} />
                  const ocupado = assentosOcupados.includes(n)
                  const selecionado = assentosSelecionados.includes(n)
                  const cheio = !selecionado && assentosSelecionados.length >= quantidade
                  return (
                    <button key={ci} onClick={() => toggleAssento(n)} disabled={ocupado || (cheio && !selecionado)}
                      title={`${String.fromCharCode(65 + ri)}${ci + 1}`}
                      style={{
                        flex: 1, aspectRatio: '1', borderRadius: 6, border: 'none',
                        cursor: ocupado ? 'not-allowed' : cheio ? 'default' : 'pointer',
                        background: ocupado ? '#ccc' : selecionado ? COR : '#f0f0f0',
                        opacity: cheio && !selecionado && !ocupado ? 0.4 : 1,
                        fontSize: 9, color: selecionado ? 'white' : '#666',
                        transition: 'background 0.15s'
                      }}>
                      {ci + 1}
                    </button>
                  )
                })}
              </div>
            ))}
          </div>
          <div style={{ display: 'flex', justifyContent: 'center', gap: 24, marginBottom: 24, fontSize: 12, color: '#555' }}>
            <Legenda cor="#f0f0f0" label="Disponível" />
            <Legenda cor={COR} label="Selecionado" />
            <Legenda cor="#ccc" label="Ocupado" />
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <BtnVoltar onClick={() => setEtapa('ingresso')} />
            <BtnPrimario onClick={confirmarAssentos} disabled={assentosSelecionados.length < quantidade || carregando}>
              {carregando ? 'Reservando...' : `Continuar com ${assentosSelecionados.length} assento${assentosSelecionados.length !== 1 ? 's' : ''}`}
            </BtnPrimario>
          </div>
        </div>
      )}

      {etapa === 'bomboniere' && sessao && (
        <div>
          <SessaoCard sessao={sessao} extra={`${assentosSelecionados.map(numToLabel).join(', ')} · ${ingressoResumo}`} />
          <h2 style={{ fontSize: 17, marginBottom: 4 }}>Bomboniere <span style={{ fontWeight: 400, color: '#888', fontSize: 14 }}>(opcional)</span></h2>
          <p style={{ color: '#666', fontSize: 13, marginBottom: 18 }}>Adicione produtos para retirar na entrada.</p>
          {produtos.length === 0
            ? <p style={{ color: '#aaa', fontSize: 14, marginBottom: 24 }}>Nenhum produto disponível no momento.</p>
            : (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 12, marginBottom: 24 }}>
                {produtos.map(p => {
                  const item = carrinho.find(i => i.produto.id === p.id)
                  const qtd = item?.qtd ?? 0
                  return (
                    <div key={p.id} style={{ border: `2px solid ${qtd > 0 ? COR_PORTAL : '#e0e0e0'}`, borderRadius: 10, padding: '14px 16px', background: 'white', display: 'flex', flexDirection: 'column', gap: 8 }}>
                      <div>
                        <div style={{ fontWeight: 600, fontSize: 14 }}>{p.nome}</div>
                        <div style={{ fontSize: 12, color: '#888', marginTop: 2 }}>{p.categoria}</div>
                        <div style={{ fontWeight: 700, fontSize: 15, color: COR_PORTAL, marginTop: 4 }}>R$ {p.preco.toFixed(2)}</div>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginTop: 4 }}>
                        <BtnQtd onClick={() => alterarCarrinho(p, -1)}>−</BtnQtd>
                        <span style={{ fontWeight: 700, fontSize: 18, minWidth: 20, textAlign: 'center' }}>{qtd}</span>
                        <BtnQtd onClick={() => alterarCarrinho(p, 1)}>+</BtnQtd>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          {carrinho.length > 0 && (
            <div style={{ background: '#e3f2fd', border: '1px solid #90caf9', borderRadius: 8, padding: '10px 16px', marginBottom: 20 }}>
              <p style={{ margin: 0, fontWeight: 600, fontSize: 14, color: COR_PORTAL }}>
                {carrinho.map(i => `${i.qtd}× ${i.produto.nome}`).join(', ')} — R$ {totalBomboniere.toFixed(2)}
              </p>
            </div>
          )}
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <BtnVoltar onClick={() => setEtapa('assento')} />
            <BtnPrimario onClick={() => setEtapa('cupom')}>Continuar</BtnPrimario>
          </div>
        </div>
      )}

      {etapa === 'cupom' && sessao && (
        <div>
          <SessaoCard sessao={sessao} extra={`${assentosSelecionados.map(numToLabel).join(', ')} · ${ingressoResumo}`} />
          <h2 style={{ fontSize: 17, marginBottom: 8 }}>Cupom de desconto <span style={{ fontWeight: 400, color: '#888', fontSize: 14 }}>(opcional)</span></h2>
          <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
            <input value={codigoCupom} onChange={e => { setCodigoCupom(e.target.value); setErroCupom('') }}
              placeholder="Ex: ITAU15" style={{ flex: 1, padding: '10px 14px', borderRadius: 8, border: '1px solid #ccc', fontSize: 14 }} />
            <button onClick={aplicarCupom} style={{ background: '#f5f5f5', border: '1px solid #ccc', borderRadius: 8, padding: '10px 16px', cursor: 'pointer', fontSize: 14 }}>Aplicar</button>
          </div>
          {erroCupom && <p style={{ color: '#c62828', fontSize: 13, margin: '4px 0 8px' }}>{erroCupom}</p>}
          {desconto && (
            <div style={{ background: '#e8f5e9', border: '1px solid #a5d6a7', borderRadius: 8, padding: '10px 14px', marginBottom: 8 }}>
              <p style={{ margin: 0, color: '#2e7d32', fontWeight: 500, fontSize: 14 }}>
                Desconto: R$ {desconto.valorDesconto.toFixed(2)} — Total: R$ {desconto.valorFinal.toFixed(2)}
              </p>
              {!desconto.cumulativo && (
                <p style={{ margin: '4px 0 0', color: '#1b5e20', fontSize: 12 }}>Cupom não cumulativo — não pode ser combinado com outros cupons.</p>
              )}
            </div>
          )}
          {/* Aviso de limite mensal atingido */}
          {cliente && limiteMensalAtingido && (
            <div style={{ background: '#fffbeb', border: '1px solid #fde68a', borderRadius: 10, padding: '12px 16px', marginTop: 20, display: 'flex', alignItems: 'center', gap: 10, color: '#92400e' }}>
              <span style={{ fontSize: 18 }}>⚠️</span>
              <div>
                <div style={{ fontWeight: 600, fontSize: 13 }}>Limite mensal de pontos atingido</div>
                <div style={{ fontSize: 12, marginTop: 2 }}>
                  Você já acumulou 10.000 pontos este mês. Nenhum ponto será acumulado nesta compra.
                </div>
              </div>
            </div>
          )}
          {cliente && saldoPontos !== null && saldoPontos > 0 && (
            <div style={{ background: '#1a1a2e', borderRadius: 12, padding: '18px 20px', marginTop: 20, color: 'white' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 }}>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 14, marginBottom: 6 }}>⭐ Fidelidade FRAME</div>
                  {!limiteMensalAtingido && (
                    <div style={{ fontSize: 13, color: '#f59e0b', fontWeight: 700, marginBottom: 4 }}>
                      Você ganhará nesta compra: {pontosQueVaiGanhar} pontos
                    </div>
                  )}
                  <div style={{ fontSize: 12, opacity: 0.65 }}>
                    Saldo disponível: {saldoPontos} pontos (R$ {(saldoPontos / 100).toFixed(2)})
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexShrink: 0 }}>
                  <div onClick={() => setUsarPontos(v => !v)}
                    style={{ width: 44, height: 24, borderRadius: 12, background: usarPontos ? '#f59e0b' : '#4b5563', position: 'relative', cursor: 'pointer', transition: 'background 0.2s' }}>
                    <div style={{ position: 'absolute', top: 3, left: usarPontos ? 22 : 3, width: 18, height: 18, borderRadius: '50%', background: 'white', transition: 'left 0.2s' }} />
                  </div>
                  <span style={{ fontSize: 12, color: '#d1d5db', whiteSpace: 'nowrap' }}>Usar pontos nesta compra</span>
                </div>
              </div>
              {usarPontos && (
                <div style={{ marginTop: 12, paddingTop: 12, borderTop: '1px solid rgba(255,255,255,0.1)', fontSize: 13, color: '#fbbf24' }}>
                  🎉 Desconto aplicado: - R$ {descontoPontosReais.toFixed(2)} ({saldoPontos} pontos usados)
                </div>
              )}
            </div>
          )}
          {cliente && (saldoPontos === null || saldoPontos === 0) && (
            <div style={{ background: '#1a1a2e', borderRadius: 12, padding: '14px 18px', marginTop: 20, color: 'white', display: 'flex', alignItems: 'center', gap: 12 }}>
              <span style={{ fontSize: 20 }}>⭐</span>
              <div>
                <div style={{ fontWeight: 700, fontSize: 13 }}>Fidelidade FRAME</div>
                <div style={{ fontSize: 12, color: '#f59e0b', marginTop: 2 }}>
                  {limiteMensalAtingido
                    ? 'Limite mensal atingido — nenhum ponto será acumulado.'
                    : `Você ganhará ${pontosQueVaiGanhar} pontos nesta compra!`}
                </div>
              </div>
            </div>
          )}
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 24 }}>
            <BtnVoltar onClick={() => setEtapa('bomboniere')} />
            <BtnPrimario onClick={() => setEtapa('confirmacao')}>Continuar</BtnPrimario>
          </div>
        </div>
      )}

      {etapa === 'confirmacao' && sessao && (
        <div>
          <h2 style={{ fontSize: 17, marginBottom: 18 }}>Confirmar compra</h2>
          <div style={{ background: 'white', border: '1px solid #e0e0e0', borderRadius: 10, padding: '18px 20px', marginBottom: 22 }}>
            <Row label="Filme" value={sessao.filme} />
            <Row label="Sessão" value={sessao.inicio} />
            <Row label="Assentos" value={assentosSelecionados.map(numToLabel).join(', ')} />
            {qtdInteira > 0 && <Row label="Inteira" value={`${qtdInteira}× — R$ ${(qtdInteira * precoInteira).toFixed(2)}`} />}
            {qtdMeia > 0 && <Row label="Meia-entrada" value={`${qtdMeia}× — R$ ${(qtdMeia * precoMeia).toFixed(2)}`} />}
            <Row label="Subtotal ingressos" value={`R$ ${subtotal.toFixed(2)}`} />
            {carrinho.length > 0 && carrinho.map(i => (
              <Row key={i.produto.id} label={`${i.produto.nome} ×${i.qtd}`} value={`R$ ${(i.produto.preco * i.qtd).toFixed(2)}`} />
            ))}
            {carrinho.length > 0 && <Row label="Subtotal bomboniere" value={`R$ ${totalBomboniere.toFixed(2)}`} />}
            {desconto && <Row label="Desconto cupom" value={`- R$ ${desconto.valorDesconto.toFixed(2)}`} green />}
            {usarPontos && descontoPontosReais > 0 && (
              <Row label={`Desconto pontos FRAME (${saldoPontos} pts)`} value={`- R$ ${descontoPontosReais.toFixed(2)}`} green />
            )}
            <div style={{ borderTop: '1px solid #eee', paddingTop: 12, display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ fontWeight: 600 }}>Total</span>
              <span style={{ fontWeight: 700, fontSize: 18, color: COR_PORTAL }}>R$ {totalFinal.toFixed(2)}</span>
            </div>
            {cliente && !limiteMensalAtingido && (
              <div style={{ marginTop: 12, background: '#1a1a2e', borderRadius: 8, padding: '10px 14px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: '#d1d5db', fontSize: 13 }}>⭐ Pontos que você vai ganhar</span>
                <span style={{ color: '#f59e0b', fontWeight: 700, fontSize: 14 }}>+{pontosQueVaiGanhar} pts</span>
              </div>
            )}
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <BtnVoltar onClick={() => setEtapa('cupom')} />
            <BtnPrimario onClick={confirmarCompra} disabled={carregando}>
              {carregando ? 'Processando...' : 'Confirmar e Pagar'}
            </BtnPrimario>
          </div>
        </div>
      )}

      {etapa === 'sucesso' && (
        <div style={{ textAlign: 'center', padding: '32px 0' }}>
          <div style={{ fontSize: 48, marginBottom: 10 }}>✅</div>
          <h2 style={{ fontSize: 22, marginBottom: 4 }}>Compra realizada!</h2>
          <p style={{ color: '#666', marginBottom: 16, fontSize: 14 }}>
            Apresente o QR Code de cada assento na entrada do cinema.
          </p>
          {/* Banner de pontos ganhos */}
          {cliente && pontosGanhos > 0 && (
            <div style={{ background: '#1a1a2e', borderRadius: 10, padding: '12px 20px', display: 'inline-flex', alignItems: 'center', gap: 10, marginBottom: 16, color: 'white' }}>
              <span style={{ fontSize: 22 }}>⭐</span>
              <span style={{ fontSize: 15, fontWeight: 700, color: '#f59e0b' }}>+{pontosGanhos} pontos</span>
              <span style={{ fontSize: 13, opacity: 0.7 }}>adicionados à sua conta</span>
            </div>
          )}

          <h3 style={{ fontSize: 15, color: '#555', marginBottom: 14 }}>Ingressos</h3>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 20, justifyContent: 'center', marginBottom: 32 }}>
            {qrCodes.map(({ assento, codigo }) => (
              <div key={assento} style={{ background: 'white', border: '1px solid #e0e0e0', borderRadius: 12, padding: '20px 24px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12 }}>
                <span style={{ fontWeight: 700, fontSize: 16, color: COR }}>Assento {assento}</span>
                <QRCodeSVG value={codigo} size={160} level="H" includeMargin />
                <span style={{ fontFamily: 'monospace', fontSize: 10, color: '#aaa' }}>{codigo}</span>
              </div>
            ))}
          </div>
          {voucherQr && (
            <>
              <h3 style={{ fontSize: 15, color: '#555', marginBottom: 14 }}>Retirada na Bomboniere</h3>
              <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 32 }}>
                <div style={{ background: 'white', border: '2px solid #e0e0e0', borderRadius: 12, padding: '20px 24px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12 }}>
                  <span style={{ fontWeight: 700, fontSize: 15, color: COR_PORTAL }}>Voucher Bomboniere</span>
                  <p style={{ margin: 0, fontSize: 12, color: '#888' }}>{carrinho.map(i => `${i.qtd}× ${i.produto.nome}`).join(' · ')}</p>
                  <QRCodeSVG value={voucherQr} size={160} level="H" includeMargin />
                  <span style={{ fontFamily: 'monospace', fontSize: 10, color: '#aaa' }}>{voucherQr}</span>
                </div>
              </div>
            </>
          )}
          <button onClick={reiniciar} style={{ background: COR_PORTAL, color: 'white', border: 'none', borderRadius: 8, padding: '12px 28px', cursor: 'pointer', fontWeight: 500, fontSize: 14 }}>
            Nova compra
          </button>
        </div>
      )}
    </div>
  )
}

interface GrupoFilme { filme: string; genero: string; classificacao: string; sessoes: SessaoDisponivel[] }

function agruparPorFilme(sessoes: SessaoDisponivel[]): GrupoFilme[] {
  const map = new Map<string, GrupoFilme>()
  for (const s of sessoes) {
    if (!map.has(s.filme)) map.set(s.filme, { filme: s.filme, genero: s.genero, classificacao: s.classificacao, sessoes: [] })
    map.get(s.filme)!.sessoes.push(s)
  }
  return Array.from(map.values())
}

const CLASSIFICACAO_COR: Record<string, string> = {
  LIVRE: '#2e7d32', DEZ: '#1565c0', DOZE: '#6a1b9a', QUATORZE: '#e65100', DEZESSEIS: '#b71c1c', DEZOITO: '#212121',
}
const CLASSIFICACAO_LABEL2: Record<string, string> = {
  LIVRE: 'L', DEZ: '10', DOZE: '12', QUATORZE: '14', DEZESSEIS: '16', DEZOITO: '18',
}
const TIPO_SALA_LABEL: Record<string, string> = { PADRAO: 'Comum', TRES_D: '3D', IMAX: 'IMAX', VIP: 'VIP' }
const TIPO_SALA_COR: Record<string, string> = { PADRAO: '#546e7a', TRES_D: '#1565c0', IMAX: '#6a1b9a', VIP: '#b8860b' }

function FilmeComSessoes({ grupo, onSelecionar, carregando }: { grupo: GrupoFilme; onSelecionar: (s: SessaoDisponivel) => void; carregando: boolean }) {
  const { posterUrl } = useMoviePoster(grupo.filme)
  return (
    <div style={{ background: 'white', border: '1px solid #e8e8e8', borderRadius: 14, marginBottom: 16, overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.06)', display: 'flex', alignItems: 'stretch' }}>
      <div style={{ width: 110, flexShrink: 0, position: 'relative', background: '#1a1a2e' }}>
        {posterUrl ? <img src={posterUrl} alt={grupo.filme} style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }} /> : <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 36, minHeight: 140 }}>🎬</div>}
        <div style={{ position: 'absolute', top: 8, left: 8, background: CLASSIFICACAO_COR[grupo.classificacao] ?? '#555', color: 'white', fontSize: 11, fontWeight: 700, padding: '2px 6px', borderRadius: 5 }}>
          {CLASSIFICACAO_LABEL2[grupo.classificacao] ?? grupo.classificacao}
        </div>
      </div>
      <div style={{ flex: 1, padding: '18px 22px', borderLeft: '1px solid #f0f0f0' }}>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 12, marginBottom: 14 }}>
          <span style={{ fontWeight: 700, fontSize: 17, color: '#1a1a2e' }}>{grupo.filme}</span>
          <span style={{ fontSize: 13, color: '#999' }}>{grupo.genero.replace('_', ' ')}</span>
        </div>
        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginTop: 4 }}>
          {grupo.sessoes.map(s => {
            const cor = TIPO_SALA_COR[s.tipoSala] ?? COR_PORTAL
            const label = TIPO_SALA_LABEL[s.tipoSala] ?? s.tipoSala
            return (
              <button key={s.id} onClick={() => onSelecionar(s)} disabled={carregando}
                style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '10px 18px', borderRadius: 10, cursor: carregando ? 'default' : 'pointer', border: `2px solid ${cor}`, background: 'white', color: cor, transition: 'all 0.15s', gap: 3 }}
                onMouseEnter={e => { e.currentTarget.style.background = cor; e.currentTarget.style.color = 'white' }}
                onMouseLeave={e => { e.currentTarget.style.background = 'white'; e.currentTarget.style.color = cor }}>
                <span style={{ fontSize: 15, fontWeight: 700 }}>{s.inicio}</span>
                <span style={{ fontSize: 10, fontWeight: 600, letterSpacing: 0.5, opacity: 0.85 }}>{label}</span>
              </button>
            )
          })}
        </div>
      </div>
    </div>
  )
}

function SessaoCard({ sessao, extra }: { sessao: { filme: string; inicio: string; tipoSala?: string }; extra?: string }) {
  const { posterUrl } = useMoviePoster(sessao.filme)
  const cor = TIPO_SALA_COR[sessao.tipoSala ?? ''] ?? COR_PORTAL
  const label = TIPO_SALA_LABEL[sessao.tipoSala ?? '']
  return (
    <div style={{ background: 'white', border: '1px solid #e0e0e0', borderRadius: 10, display: 'flex', alignItems: 'center', marginBottom: 20, overflow: 'hidden' }}>
      <div style={{ width: 56, height: 72, flexShrink: 0, background: '#e0e0e0', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 20 }}>
        {posterUrl ? <img src={posterUrl} alt={sessao.filme} style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : '🎬'}
      </div>
      <div style={{ padding: '12px 16px', flex: 1 }}>
        <div style={{ fontWeight: 600, fontSize: 15 }}>{sessao.filme}</div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 4, flexWrap: 'wrap' }}>
          <span style={{ color: '#666', fontSize: 13 }}>{sessao.inicio}{extra ? ` · ${extra}` : ''}</span>
          {label && <span style={{ background: `${cor}18`, color: cor, fontSize: 11, fontWeight: 700, padding: '2px 8px', borderRadius: 6 }}>{label}</span>}
        </div>
      </div>
    </div>
  )
}

function Legenda({ cor, label }: { cor: string; label: string }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
      <div style={{ width: 18, height: 18, borderRadius: 4, background: cor, border: '1px solid #ddd' }} />
      <span>{label}</span>
    </div>
  )
}

function BtnQtd({ onClick, children }: { onClick: () => void; children: React.ReactNode }) {
  return (
    <button onClick={onClick} style={{ width: 34, height: 34, borderRadius: '50%', border: '1px solid #ccc', background: 'white', fontSize: 18, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      {children}
    </button>
  )
}

function BtnVoltar({ onClick }: { onClick: () => void }) {
  return <button onClick={onClick} style={{ padding: '10px 20px', borderRadius: 8, border: '1px solid #ccc', background: 'white', cursor: 'pointer', fontSize: 14 }}>Voltar</button>
}

function BtnPrimario({ onClick, disabled, children }: { onClick: () => void; disabled?: boolean; children: React.ReactNode }) {
  return (
    <button onClick={onClick} disabled={disabled} style={{ background: disabled ? '#90a4ae' : '#1565C0', color: 'white', border: 'none', borderRadius: 8, padding: '10px 22px', cursor: disabled ? 'default' : 'pointer', fontWeight: 500, fontSize: 14 }}>
      {children}
    </button>
  )
}

function Row({ label, value, green }: { label: string; value: string; green?: boolean }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
      <span style={{ color: '#666', fontSize: 14 }}>{label}</span>
      <span style={{ fontWeight: 500, color: green ? '#2e7d32' : '#333', fontSize: 14 }}>{value}</span>
    </div>
  )
}



