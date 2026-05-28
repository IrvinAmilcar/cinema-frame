import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { ClienteLogado } from '../components/AuthModal'
import { useMoviePoster } from '../hooks/useMoviePoster'
import { fetchMovieImages } from '../lib/tmdb'

const COR = '#1565C0'

type Sessao = { id: string; inicio: string; sala: number }
type Filme = {
  filmeId: string
  titulo: string
  genero: string
  classificacao: string
  duracaoMinutos: number
  trailerUrl: string | null
  sinopse: string | null
  nota: number
  recomendado: boolean
  sessoes: Sessao[]
}

const CLASSIFICACAO_LABEL: Record<string, string> = {
  LIVRE: 'Livre', DEZ: '10+', DOZE: '12+',
  QUATORZE: '14+', DEZESSEIS: '16+', DEZOITO: '18+',
}

const GENERO_ICON: Record<string, string> = {
  ACAO: '💥', COMEDIA: '😄', DRAMA: '🎭', TERROR: '👻',
  ANIMACAO: '🎨', DOCUMENTARIO: '📽️', ROMANCE: '💕',
  FICCAO_CIENTIFICA: '🚀',
}

const BANNER_GRADIENTS = [
  'linear-gradient(135deg, #1a237e 0%, #283593 40%, #3949ab 100%)',
  'linear-gradient(135deg, #4a148c 0%, #6a1b9a 40%, #7b1fa2 100%)',
  'linear-gradient(135deg, #1b5e20 0%, #2e7d32 40%, #388e3c 100%)',
  'linear-gradient(135deg, #bf360c 0%, #d84315 40%, #e64a19 100%)',
  'linear-gradient(135deg, #0d47a1 0%, #1565c0 40%, #1976d2 100%)',
]

const hoje = new Date().toISOString().slice(0, 10)

export default function PortalHomePage({ cliente }: { cliente: ClienteLogado | null }) {
  const navigate = useNavigate()
  const [filmes, setFilmes] = useState<Filme[]>([])
  const [destaque, setDestaque] = useState(0)
  const [loading, setLoading] = useState(true)
  // pré-carrega imagens dos 3 primeiros filmes para evitar flash ao trocar slide
  const [backdrops, setBackdrops] = useState<(string | null)[]>([])

  useEffect(() => {
    fetch(`/api/programacao/filmes?data=${hoje}`)
      .then(r => r.json())
      .then((data: Filme[]) => { setFilmes(data); setLoading(false) })
      .catch(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (filmes.length === 0) return
    const top3 = filmes.slice(0, 3)
    Promise.all(top3.map(f => fetchMovieImages(f.titulo))).then(results => {
      setBackdrops(results.map(r => r.backdropUrl))
    })
  }, [filmes])

  useEffect(() => {
    if (filmes.length < 2) return
    const t = setInterval(() => setDestaque(d => (d + 1) % Math.min(filmes.length, 3)), 5000)
    return () => clearInterval(t)
  }, [filmes.length])

  const filmeDestaque = filmes[destaque] ?? null
  const backdropAtual = backdrops[destaque] ?? null

  const proximasSessoes = filmes
    .flatMap(f => f.sessoes.map(s => ({ ...s, titulo: f.titulo, filmeId: f.filmeId })))
    .sort((a, b) => a.inicio.localeCompare(b.inicio))
    .slice(0, 5)

  const primeiroNome = cliente?.nome.split(' ')[0] ?? null

  return (
    <div style={{ maxWidth: 1200, fontFamily: 'sans-serif' }}>

      {/* Header */}
      <div style={{ marginBottom: 28 }}>
        <h1 style={{ margin: 0, fontSize: 28, fontWeight: 700, color: '#1a1a2e' }}>
          {primeiroNome ? `Olá, ${primeiroNome}! 👋` : 'Bem-vindo ao F.R.A.M.E! 🎬'}
        </h1>
        <p style={{ margin: '6px 0 0', color: '#888', fontSize: 15 }}>
          {primeiroNome
            ? 'Que bom ter você aqui! O que vamos assistir hoje?'
            : 'Explore a programação e compre seus ingressos.'}
        </p>
      </div>

      {/* Cards de acesso rápido */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16, marginBottom: 32 }}>
        <QuickCard icon='📅' iconBg='#e3f2fd' title='Programação' desc='Veja os filmes em cartaz hoje' onClick={() => navigate('/programacao')} />
        <QuickCard icon='🎟️' iconBg='#f3e5f5' title='Comprar Ingresso' desc='Reserve seu assento e finalize a compra' onClick={() => navigate('/compra')} />
        <QuickCard icon='⭐' iconBg='#fce4ec' title='Fidelidade' desc='Consulte seus pontos e benefícios' onClick={() => navigate('/fidelidade')} />
      </div>

      {/* Banner + próximas sessões */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: 20, marginBottom: 32 }}>

        {/* Banner em destaque */}
        <div style={{
          borderRadius: 16, overflow: 'hidden', minHeight: 300, position: 'relative',
          background: backdropAtual
            ? `linear-gradient(to right, rgba(0,0,0,0.82) 45%, rgba(0,0,0,0.3) 100%), url(${backdropAtual}) center/cover no-repeat`
            : BANNER_GRADIENTS[destaque % BANNER_GRADIENTS.length],
          boxShadow: '0 4px 20px rgba(0,0,0,0.18)',
          transition: 'background 0.5s ease',
        }}>
          {loading ? (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 300, color: 'rgba(255,255,255,0.5)', fontSize: 14 }}>
              Carregando...
            </div>
          ) : filmeDestaque ? (
            <div style={{ padding: 36, display: 'flex', flexDirection: 'column', minHeight: 300, boxSizing: 'border-box' }}>
              <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.7)', marginBottom: 12, display: 'flex', alignItems: 'center', gap: 6 }}>
                ☆ Em destaque
              </div>
              <div style={{ fontSize: 28, fontWeight: 800, color: 'white', marginBottom: 12, lineHeight: 1.2, maxWidth: 480 }}>
                {filmeDestaque.titulo}
              </div>
              {filmeDestaque.sinopse && (
                <p style={{ margin: '0 0 20px', color: 'rgba(255,255,255,0.85)', fontSize: 14, lineHeight: 1.6, maxWidth: 420 }}>
                  {filmeDestaque.sinopse.length > 140 ? filmeDestaque.sinopse.slice(0, 140) + '…' : filmeDestaque.sinopse}
                </p>
              )}
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 24 }}>
                <span style={badgeStyle('rgba(255,255,255,0.2)')}>
                  {GENERO_ICON[filmeDestaque.genero] ?? '🎬'} {filmeDestaque.genero.replace('_', ' ')}
                </span>
                <span style={badgeStyle('rgba(255,255,255,0.2)')}>{filmeDestaque.duracaoMinutos} min</span>
                {filmeDestaque.nota > 0 && <span style={badgeStyle('rgba(255,200,0,0.3)')}>★ {filmeDestaque.nota.toFixed(1)}</span>}
                <span style={badgeStyle('rgba(255,255,255,0.2)')}>{CLASSIFICACAO_LABEL[filmeDestaque.classificacao] ?? filmeDestaque.classificacao}</span>
              </div>
              <button onClick={() => navigate('/programacao')} style={{
                display: 'inline-flex', alignItems: 'center', gap: 8,
                background: 'white', color: COR, border: 'none', borderRadius: 10,
                padding: '10px 20px', fontSize: 14, fontWeight: 700, cursor: 'pointer',
                alignSelf: 'flex-start', boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
              }}>
                Ver sessões ▶
              </button>
              {filmes.length > 1 && (
                <div style={{ display: 'flex', gap: 6, marginTop: 'auto', paddingTop: 20 }}>
                  {filmes.slice(0, 3).map((_, i) => (
                    <button key={i} onClick={() => setDestaque(i)} style={{
                      width: i === destaque ? 20 : 8, height: 8, borderRadius: 4,
                      border: 'none', cursor: 'pointer', padding: 0,
                      background: i === destaque ? 'white' : 'rgba(255,255,255,0.4)',
                      transition: 'all 0.3s',
                    }} />
                  ))}
                </div>
              )}
            </div>
          ) : (
            <div style={{ padding: 36, display: 'flex', flexDirection: 'column', height: 300, justifyContent: 'center' }}>
              <div style={{ fontSize: 40, marginBottom: 12 }}>🎬</div>
              <div style={{ color: 'rgba(255,255,255,0.8)', fontSize: 16, fontWeight: 600 }}>Nenhum filme em cartaz hoje</div>
              <div style={{ color: 'rgba(255,255,255,0.5)', fontSize: 14, marginTop: 6 }}>Confira a programação dos próximos dias</div>
            </div>
          )}
        </div>

        {/* Próximas sessões */}
        <div style={{
          background: 'white', borderRadius: 16, padding: 24,
          boxShadow: '0 2px 12px rgba(0,0,0,0.06)', border: '1px solid #f0f0f0',
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
            <div style={{ fontWeight: 700, fontSize: 15, color: '#1a1a2e' }}>📅 Próximas sessões</div>
            <button onClick={() => navigate('/programacao')} style={{ background: 'none', border: 'none', color: COR, fontSize: 13, cursor: 'pointer', fontWeight: 500 }}>
              Ver todas
            </button>
          </div>
          {loading ? (
            <div style={{ color: '#bbb', fontSize: 14, textAlign: 'center', padding: '20px 0' }}>Carregando...</div>
          ) : proximasSessoes.length === 0 ? (
            <div style={{ color: '#bbb', fontSize: 14, textAlign: 'center', padding: '20px 0' }}>Nenhuma sessão hoje</div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {proximasSessoes.map((s, i) => (
                <SessaoItem key={s.id} titulo={s.titulo} inicio={s.inicio} sala={s.sala} index={i} total={proximasSessoes.length} />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Features */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 16 }}>
        <FeatureCard icon='🎟️' iconBg='#e3f2fd' title='Ingresso rápido' desc='Compre seu ingresso de forma rápida e segura.' />
        <FeatureCard icon='🪑' iconBg='#f3e5f5' title='Escolha seu lugar' desc='Selecione seus assentos favoritos na sala.' />
        <FeatureCard icon='🍿' iconBg='#fce4ec' title='Combos especiais' desc='Aproveite nossos combos de pipoca e bebidas.' />
        <FeatureCard icon='🎁' iconBg='#fff8e1' title='Clube de vantagens' desc='Acumule pontos e troque por benefícios exclusivos.' />
      </div>
    </div>
  )
}

/* Sessão com poster automático do TMDB */
function SessaoItem({ titulo, inicio, sala, index, total }: {
  titulo: string; inicio: string; sala: number; index: number; total: number
}) {
  const { posterUrl } = useMoviePoster(titulo)

  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12,
      paddingBottom: index < total - 1 ? 12 : 0,
      borderBottom: index < total - 1 ? '1px solid #f5f5f5' : 'none',
    }}>
      <div style={{
        width: 44, height: 44, borderRadius: 10, flexShrink: 0, overflow: 'hidden',
        background: BANNER_GRADIENTS[index % BANNER_GRADIENTS.length],
      }}>
        {posterUrl
          ? <img src={posterUrl} alt={titulo} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          : <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 18 }}>🎬</div>
        }
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontWeight: 600, fontSize: 13, color: '#1a1a2e', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {titulo}
        </div>
        <div style={{ fontSize: 12, color: '#999', marginTop: 2 }}>
          Hoje • {inicio.slice(0, 5)} • Sala {String(sala).padStart(2, '0')}
        </div>
      </div>
      <div style={{ color: COR, fontWeight: 700, fontSize: 14, flexShrink: 0 }}>
        {inicio.slice(0, 5)}
      </div>
    </div>
  )
}

function QuickCard({ icon, iconBg, title, desc, onClick }: {
  icon: string; iconBg: string; title: string; desc: string; onClick: () => void
}) {
  return (
    <div onClick={onClick} style={{
      background: 'white', borderRadius: 14, padding: '20px 20px 18px',
      border: '1px solid #f0f0f0', cursor: 'pointer',
      boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
      display: 'flex', flexDirection: 'column', gap: 10,
    }}
      onMouseEnter={e => (e.currentTarget.style.boxShadow = '0 4px 16px rgba(21,101,192,0.12)')}
      onMouseLeave={e => (e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.05)')}
    >
      <div style={{ width: 44, height: 44, borderRadius: 12, background: iconBg, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 22 }}>
        {icon}
      </div>
      <div>
        <div style={{ fontWeight: 700, fontSize: 15, color: '#1a1a2e', marginBottom: 4 }}>{title}</div>
        <div style={{ fontSize: 13, color: '#888', lineHeight: 1.4 }}>{desc}</div>
      </div>
      <div style={{ color: COR, fontSize: 16, fontWeight: 700, marginTop: 4 }}>→</div>
    </div>
  )
}

function FeatureCard({ icon, iconBg, title, desc }: {
  icon: string; iconBg: string; title: string; desc: string
}) {
  return (
    <div style={{
      background: 'white', borderRadius: 14, padding: '20px 16px',
      border: '1px solid #f0f0f0', boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
      display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', gap: 10,
    }}>
      <div style={{ width: 52, height: 52, borderRadius: '50%', background: iconBg, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 24 }}>
        {icon}
      </div>
      <div style={{ fontWeight: 700, fontSize: 14, color: '#1a1a2e' }}>{title}</div>
      <div style={{ fontSize: 12, color: '#999', lineHeight: 1.5 }}>{desc}</div>
    </div>
  )
}

function badgeStyle(bg: string): React.CSSProperties {
  return { background: bg, color: 'white', fontSize: 12, padding: '4px 10px', borderRadius: 20 }
}
