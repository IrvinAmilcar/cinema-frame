import { useEffect, useState, useCallback, useRef } from 'react'
import RankingFilmes from './RankingFilmes'
import BombonieireChart from './BombonieireChart'

const cores = {
  vinho: '#8B0000',
  fundo: '#FAFAFA',
  borda: '#E0E0E0',
  verdeClaro: '#E8F5E9',
  verdeTexto: '#2E7D32',
  vermelhoClaro: '#FFEBEE',
  vermelhoTexto: '#C62828',
}

const API_URL = 'http://localhost:8080'

// ─── Tipos ────────────────────────────────────────────────────────────────────

interface InsumoResponse {
  id: string
  nome: string
  unidade: string
  quantidadeEmEstoque: number
  nivelCritico: number
}

interface SalaResponse {
  id: string
  numero: number
  capacidade: number
  tipo: string
}

interface SessaoResponse {
  id: string
  filmeId: string
  filmeTitulo: string
  salaId: string
  salaNumero: number
  inicio: string
  fim: string
}

interface GradeResponse {
  id: string
  inicio: string
  fim: string
  sessoes: SessaoResponse[]
}

interface OcupacaoSessao {
  sessaoId: string
  filme: string
  horario: string
  salaNumero: number
  salaTipo: string
  capacidade: number
  ingressosVendidos: number
  diasVigencia: number
}

interface IngressosSessao {
  sessaoId: string
  filme: string
  horario: string
  salaNumero: number
  salaTipo: string
  totalIngressos: number
  totalInteira: number
  valorTotal: number
}

interface BomboniereSessao {
  sessaoId: string
  filme: string
  horario: string
  salaNumero: number
  salaTipo: string
  produto: string
  quantidade: number
  valorTotal: number
}

// ingressos vendidos por sessão (contagem real via assentos-ocupados)
interface OcupacaoReal {
  sessaoId: string
  vendidos: number
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function fmtBRL(v: number) {
  return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

function hoje() {
  return new Date().toISOString().split('T')[0]
}

function ini30() {
  const d = new Date()
  d.setDate(d.getDate() - 30)
  return d.toISOString().split('T')[0]
}

type EstadoSala =
  | { tipo: 'em_andamento'; sessao: SessaoResponse }
  | { tipo: 'proxima'; sessao: SessaoResponse; minutosAte: number }
  | { tipo: 'vazia' }

function estadoDaSala(salaNumero: number, sessoesDoDia: SessaoResponse[]): EstadoSala {
  const agora = new Date()
  const hhmm = `${String(agora.getHours()).padStart(2, '0')}:${String(agora.getMinutes()).padStart(2, '0')}`
  const dasSala = sessoesDoDia
    .filter(s => s.salaNumero === salaNumero)
    .sort((a, b) => a.inicio.localeCompare(b.inicio))

  if (dasSala.length === 0) return { tipo: 'vazia' }

  // sessão em andamento: inicio <= agora < fim (fim = fim real do filme, sem intervalo de limpeza)
  // mas o SessaoResponse.fim já é getFimComIntervalo, então durante limpeza ainda está "em andamento"
  const emAndamento = dasSala.find(s => s.inicio <= hhmm && hhmm < s.fim)
  if (emAndamento) return { tipo: 'em_andamento', sessao: emAndamento }

  // próxima sessão do dia
  const proxima = dasSala.find(s => s.inicio > hhmm)
  if (proxima) {
    // calcular minutos até a próxima
    const [ph, pm] = proxima.inicio.split(':').map(Number)
    const minutos = (ph * 60 + pm) - (agora.getHours() * 60 + agora.getMinutes())
    return { tipo: 'proxima', sessao: proxima, minutosAte: minutos }
  }

  // todas as sessões do dia já terminaram
  return { tipo: 'vazia' }
}

// ─── Gráfico de rosca SVG ─────────────────────────────────────────────────────

function DonutChart({ pct, cor }: { pct: number; cor: string }) {
  const r = 38
  const circ = 2 * Math.PI * r
  const dash = (Math.min(pct, 100) / 100) * circ
  return (
    <svg width="100" height="100" viewBox="0 0 100 100">
      <circle cx="50" cy="50" r={r} fill="none" stroke="#f0eeea" strokeWidth="10" />
      <circle
        cx="50" cy="50" r={r} fill="none"
        stroke={cor} strokeWidth="10"
        strokeDasharray={`${dash} ${circ - dash}`}
        strokeLinecap="round"
        strokeDashoffset={circ * 0.25}
        style={{ transition: 'stroke-dasharray 0.6s ease' }}
      />
    </svg>
  )
}

// ─── Card de sala ─────────────────────────────────────────────────────────────

function SalaCard({
  sala,
  estado,
  ocupacaoReal,
}: {
  sala: SalaResponse
  estado: EstadoSala
  ocupacaoReal: OcupacaoReal[]
}) {
  const cap = sala.capacidade
  const sessaoAtiva = estado.tipo === 'em_andamento' ? estado.sessao : null
  const vendidos = sessaoAtiva
    ? (ocupacaoReal.find(o => o.sessaoId === sessaoAtiva.id)?.vendidos ?? 0)
    : 0
  const pct = cap > 0 && sessaoAtiva ? Math.round((vendidos / cap) * 100) : 0
  const cor = estado.tipo === 'vazia' ? '#ccc'
    : estado.tipo === 'proxima' ? '#888'
    : pct > 80 ? cores.vermelhoTexto
    : pct > 50 ? '#F57C00'
    : cores.vinho

  return (
    <div style={{
      flex: 1, backgroundColor: 'white', border: `1px solid ${cores.borda}`,
      borderRadius: '10px', padding: '16px', display: 'flex',
      flexDirection: 'column', alignItems: 'center', gap: '4px', minWidth: 0,
    }}>
      {/* título da sala */}
      <div style={{ fontSize: '12px', fontWeight: 600, color: '#555', marginBottom: '8px', textAlign: 'center' }}>
        Sala {sala.numero} — {sala.tipo.replace('TRES_D', '3D').replace(/_/g, ' ')}
      </div>

      {/* rosca */}
      <div style={{ position: 'relative', width: 100, height: 100 }}>
        <DonutChart pct={pct} cor={cor} />
        <div style={{
          position: 'absolute', inset: 0, display: 'flex',
          flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
        }}>
          {estado.tipo === 'vazia' ? (
            <span style={{ fontSize: '11px', color: '#bbb', fontWeight: 500 }}>vazia</span>
          ) : estado.tipo === 'proxima' ? (
            <span style={{ fontSize: '11px', color: '#888', fontWeight: 500, textAlign: 'center', lineHeight: 1.3 }}>
              em {estado.minutosAte}min
            </span>
          ) : (
            <span style={{ fontSize: '18px', fontWeight: 700, color: cor }}>{pct}%</span>
          )}
        </div>
      </div>

      {/* assentos */}
      <div style={{ fontSize: '12px', color: '#888', marginTop: '4px' }}>
        {estado.tipo === 'em_andamento'
          ? `${vendidos}/${cap} assentos`
          : estado.tipo === 'proxima'
          ? `${cap} assentos`
          : `0/${cap} assentos`}
      </div>

      {/* info da sessão */}
      {estado.tipo === 'em_andamento' && (
        <div style={{ textAlign: 'center', marginTop: '8px' }}>
          <div style={{
            fontSize: '12px', fontWeight: 500, color: '#1a1a1a',
            whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '140px',
          }}>
            {estado.sessao.filmeTitulo}
          </div>
          <div style={{ fontSize: '11px', color: '#999', marginTop: '2px' }}>
            {estado.sessao.inicio} – {estado.sessao.fim}
          </div>
        </div>
      )}

      {estado.tipo === 'proxima' && (
        <div style={{ textAlign: 'center', marginTop: '8px' }}>
          <div style={{
            fontSize: '10px', fontWeight: 600, color: '#aaa',
            textTransform: 'uppercase', letterSpacing: '0.4px', marginBottom: '3px',
          }}>
            Próxima sessão
          </div>
          <div style={{
            fontSize: '12px', fontWeight: 500, color: '#555',
            whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '140px',
          }}>
            {estado.sessao.filmeTitulo}
          </div>
          <div style={{ fontSize: '11px', color: '#bbb', marginTop: '2px' }}>
            {estado.sessao.inicio} – {estado.sessao.fim}
          </div>
        </div>
      )}

      {estado.tipo === 'vazia' && (
        <div style={{ fontSize: '11px', color: '#bbb', marginTop: '8px', fontStyle: 'italic' }}>
          Sem sessão hoje
        </div>
      )}
    </div>
  )
}

// ─── Componente principal ─────────────────────────────────────────────────────

function useChartJs() {
  useEffect(() => {
    if (document.getElementById('chartjs-script')) return
    const s = document.createElement('script')
    s.id = 'chartjs-script'
    s.src = 'https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.js'
    document.head.appendChild(s)
  }, [])
}

export default function DashboardPage() {
  useChartJs()
  const [alertas, setAlertas] = useState<InsumoResponse[]>([])
  const [alertasLoading, setAlertasLoading] = useState(true)
  const [salas, setSalas] = useState<SalaResponse[]>([])
  const [sessoesDoDia, setSessoesDoDia] = useState<SessaoResponse[]>([])
  // ocupação real: contagem de assentos reservados por sessão (via /api/sessao/{id}/assentos-ocupados)
  const [ocupacaoReal, setOcupacaoReal] = useState<OcupacaoReal[]>([])
  const [ingressos, setIngressos] = useState<IngressosSessao[]>([])
  const [ocupacaoPeriodo, setOcupacaoPeriodo] = useState<OcupacaoSessao[]>([])
  const [bomboniere, setBomboniere] = useState<BomboniereSessao[]>([])
  const [atualizadoEm, setAtualizadoEm] = useState('')
  const [carregando, setCarregando] = useState(false)
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const tickRef = useRef<ReturnType<typeof setInterval> | null>(null)
  // tick de 1 minuto para recalcular estados das salas sem novo fetch
  const [tick, setTick] = useState(0)

  // Para cada sessão da grade de hoje, busca quantos assentos estão ocupados
  const buscarOcupacaoReal = useCallback(async (sessoes: SessaoResponse[]) => {
    if (sessoes.length === 0) { setOcupacaoReal([]); return }
    const resultados = await Promise.all(
      sessoes.map(async s => {
        try {
          const res = await fetch(`${API_URL}/api/sessao/${s.id}/assentos-ocupados?data=${hoje()}`)
          const assentos: number[] = res.ok ? await res.json() : []
          return { sessaoId: s.id, vendidos: assentos.length }
        } catch {
          return { sessaoId: s.id, vendidos: 0 }
        }
      })
    )
    setOcupacaoReal(resultados)
  }, [])

  // ranking de filmes para o componente RankingFilmes
  const rankingDados: { titulo: string; ingressos: number; receita: number }[] = []
  const _porFilme: Record<string, { ingressos: number; receita: number }> = {}
  ingressos.forEach(s => {
    if (!_porFilme[s.filme]) _porFilme[s.filme] = { ingressos: 0, receita: 0 }
    _porFilme[s.filme].ingressos += s.totalIngressos
    _porFilme[s.filme].receita += s.valorTotal
  })
  Object.entries(_porFilme)
    .sort((a, b) => b[1].ingressos - a[1].ingressos)
    .slice(0, 7)
    .forEach(([titulo, v]) => rankingDados.push({ titulo, ...v }))

  const carregarDados = useCallback(async () => {
    const dataHoje = hoje()
    setCarregando(true)
    setAtualizadoEm(new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }))

    setAlertasLoading(true)
    fetch(`${API_URL}/bomboniere/alertas`)
      .then(r => r.json()).then(setAlertas).catch(() => setAlertas([]))
      .finally(() => setAlertasLoading(false))

    try {
      const [salasRes, gradesRes, ingRes, ocuPeriodoRes, bombRes] = await Promise.all([
        fetch(`${API_URL}/api/salas`).then(r => r.ok ? r.json() : []),
        fetch(`${API_URL}/api/grades`).then(r => r.ok ? r.json() : []),
        fetch(`${API_URL}/caixa/ingressos-por-sessao?dataInicio=${ini30()}&dataFim=${dataHoje}`)
          .then(r => r.ok ? r.json() : []),
        fetch(`${API_URL}/caixa/ocupacao-por-sessao?dataInicio=${ini30()}&dataFim=${dataHoje}`)
          .then(r => r.ok ? r.json() : []),
        fetch(`${API_URL}/caixa/bomboniere-por-sessao?dataInicio=${ini30()}&dataFim=${dataHoje}`)
          .then(r => r.ok ? r.json() : []),
      ])

      setSalas(salasRes)
      const sessoesHoje: SessaoResponse[] = (gradesRes as GradeResponse[])
        .filter(g => g.inicio <= dataHoje && g.fim >= dataHoje)
        .flatMap(g => g.sessoes)
      setSessoesDoDia(sessoesHoje)
      setIngressos(ingRes)
      setOcupacaoPeriodo(ocuPeriodoRes)
      setBomboniere(bombRes)

      // busca ocupação real (assentos reservados) para cada sessão de hoje
      await buscarOcupacaoReal(sessoesHoje)
    } catch {
      setSalas([]); setSessoesDoDia([]); setIngressos([]); setOcupacaoPeriodo([])
    } finally {
      setCarregando(false)
    }
  }, [buscarOcupacaoReal])

  useEffect(() => {
    carregarDados()
    // atualiza salas + ocupação a cada 30s
    intervalRef.current = setInterval(() => {
      const dataHoje = hoje()
      setAtualizadoEm(new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }))
      fetch(`${API_URL}/bomboniere/alertas`).then(r => r.json()).then(setAlertas).catch(() => {})
      fetch(`${API_URL}/api/grades`)
        .then(r => r.ok ? r.json() : [])
        .then(async (grades: GradeResponse[]) => {
          const sessoesHoje = grades
            .filter(g => g.inicio <= dataHoje && g.fim >= dataHoje)
            .flatMap(g => g.sessoes)
          setSessoesDoDia(sessoesHoje)
          await buscarOcupacaoReal(sessoesHoje)
        })
        .catch(() => {})
    }, 30000)
    tickRef.current = setInterval(() => setTick(t => t + 1), 60000)
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current)
      if (tickRef.current) clearInterval(tickRef.current)
    }
  }, [carregarDados, buscarOcupacaoReal])

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif', backgroundColor: cores.fundo, minHeight: '100vh' }}>

      {/* ─── Header ─────────────────────────────────────────────────────────── */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
        <div>
          <h1 style={{ color: '#1a1a1a', margin: 0, fontSize: '22px', fontWeight: 600 }}>Dashboard</h1>
          <p style={{ color: '#888', margin: '4px 0 0', fontSize: '13px' }}>Visão em Tempo Real das Operações</p>
        </div>
        <button onClick={carregarDados} disabled={carregando} style={{
          backgroundColor: cores.vinho, color: 'white', border: 'none',
          padding: '9px 16px', borderRadius: '8px', fontSize: '13px',
          fontWeight: 600, cursor: carregando ? 'not-allowed' : 'pointer', opacity: carregando ? 0.7 : 1,
        }}>
          {carregando ? 'Atualizando...' : '↻ Atualizar'}
        </button>
      </div>

      {/* ─── Alertas de estoque ───────────────────────────────────────────────── */}
      {!alertasLoading && alertas.length > 0 && (
        <div style={{ backgroundColor: cores.vermelhoClaro, border: `1px solid #FFCDD2`, borderRadius: '10px', padding: '14px 18px', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '10px' }}>
            <span style={{ fontSize: '16px' }}>⚠</span>
            <span style={{ fontSize: '14px', fontWeight: 600, color: cores.vermelhoTexto }}>
              {alertas.length} insumo{alertas.length > 1 ? 's' : ''} em nível crítico de estoque
            </span>
            <span style={{ marginLeft: 'auto', backgroundColor: '#FFCDD2', color: cores.vermelhoTexto, fontSize: '11px', fontWeight: 600, padding: '2px 8px', borderRadius: '4px', border: `1px solid #EF9A9A` }}>
              Ação necessária
            </span>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
            {alertas.map(a => (
              <div key={a.id} style={{ display: 'flex', alignItems: 'center', gap: '6px', backgroundColor: 'white', border: `1px solid #FFCDD2`, borderRadius: '6px', padding: '6px 12px', fontSize: '12px' }}>
                <span style={{ fontWeight: 600, color: '#1a1a1a' }}>{a.nome}</span>
                <span style={{ color: cores.vermelhoTexto, fontWeight: 600 }}>{a.quantidadeEmEstoque} {a.unidade}</span>
                <span style={{ color: '#bbb' }}>/ mín {a.nivelCritico}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {!alertasLoading && alertas.length === 0 && (
        <div style={{ backgroundColor: cores.verdeClaro, border: `1px solid #C8E6C9`, borderRadius: '10px', padding: '12px 18px', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', fontWeight: 500, color: cores.verdeTexto }}>
          ✓ Estoque da bomboniere normalizado — todos os insumos acima do nível crítico.
        </div>
      )}

      {/* ─── Ocupação em Tempo Real ───────────────────────────────────────────── */}
      <div style={{ backgroundColor: 'white', border: `1px solid ${cores.borda}`, borderRadius: '10px', padding: '18px 20px', marginBottom: '20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '18px' }}>
          <span style={{ fontSize: '13px', fontWeight: 600, color: '#555' }}>Ocupação em Tempo Real</span>
          <span style={{ fontSize: '11px', fontWeight: 600, color: cores.verdeTexto, backgroundColor: cores.verdeClaro, border: `1px solid #A5D6A7`, borderRadius: '4px', padding: '1px 7px', letterSpacing: '0.3px' }}>
            LIVE
          </span>
          <span style={{ marginLeft: 'auto', fontSize: '11px', color: '#bbb' }}>
            {atualizadoEm ? `atualizado às ${atualizadoEm}` : ''}{tick > 0 ? '' : ''}
          </span>
        </div>

        {salas.length === 0 ? (
          <div style={{ padding: '24px', textAlign: 'center', color: '#bbb', fontSize: '13px' }}>Nenhuma sala cadastrada.</div>
        ) : (
          <div style={{ display: 'flex', gap: '14px' }}>
            {salas.sort((a, b) => a.numero - b.numero).map(sala => (
              <SalaCard
                key={sala.id}
                sala={sala}
                estado={estadoDaSala(sala.numero, sessoesDoDia)}
                ocupacaoReal={ocupacaoReal}
              />
            ))}
          </div>
        )}
      </div>

      {/* ─── Filmes + Ingressos por sessão ───────────────────────────────────── */}
      <div style={{ display: 'flex', gap: '16px', marginBottom: '20px' }}>

        <div style={{ flex: 1, backgroundColor: 'white', border: `1px solid ${cores.borda}`, borderRadius: '10px', padding: '18px 20px' }}>
          <div style={{ fontSize: '13px', fontWeight: 600, color: '#555', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ color: cores.vinho }}>🏆</span> Filmes Mais Assistidos
          </div>
          <RankingFilmes dados={rankingDados} />
        </div>

        <div style={{ flex: 1, backgroundColor: 'white', border: `1px solid ${cores.borda}`, borderRadius: '10px', padding: '18px 20px' }}>
          <div style={{ fontSize: '13px', fontWeight: 600, color: '#555', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ color: cores.vinho }}>🍿</span> Bomboniere
          </div>
          <BombonieireChart dados={bomboniere} />
        </div>
      </div>

    </div>
  )
}








