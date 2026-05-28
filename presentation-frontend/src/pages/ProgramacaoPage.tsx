import { useEffect, useState } from 'react'
import type { ClienteLogado } from '../components/AuthModal'
import { useMoviePoster } from '../hooks/useMoviePoster'

const COR = '#1565C0'
const AMBER = '#f57c00'

type Sessao = { id: string; inicio: string; sala: number; tipoSala: string }

const TIPO_SALA_LABEL: Record<string, string> = {
  PADRAO: 'Comum', TRES_D: '3D', IMAX: 'IMAX', VIP: 'VIP',
}
const TIPO_SALA_COR: Record<string, string> = {
  PADRAO: '#546e7a', TRES_D: '#1565c0', IMAX: '#6a1b9a', VIP: '#b8860b',
}
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

const GENEROS = ['', 'ACAO', 'COMEDIA', 'DRAMA', 'TERROR', 'ANIMACAO', 'DOCUMENTARIO', 'ROMANCE', 'FICCAO_CIENTIFICA']
const CLASSIFICACOES = ['', 'LIVRE', 'DEZ', 'DOZE', 'QUATORZE', 'DEZESSEIS', 'DEZOITO']

const CLASSIFICACAO_LABEL: Record<string, string> = {
  LIVRE: 'Livre', DEZ: '10 anos', DOZE: '12 anos',
  QUATORZE: '14 anos', DEZESSEIS: '16 anos', DEZOITO: '18 anos',
}

const BADGE_COR: Record<string, string> = {
  LIVRE: '#2e7d32', DEZ: '#1565c0', DOZE: '#6a1b9a',
  QUATORZE: '#e65100', DEZESSEIS: '#b71c1c', DEZOITO: '#212121',
}

function getDias(): { label: string; value: string }[] {
  const dias = []
  const hoje = new Date()
  const diasSemana = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']
  const meses = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez']
  for (let i = 0; i < 5; i++) {
    const d = new Date(hoje)
    d.setDate(hoje.getDate() + i)
    const iso = d.toISOString().slice(0, 10)
    const label = i === 0 ? 'Hoje' : `${diasSemana[d.getDay()]} ${d.getDate()} ${meses[d.getMonth()]}`
    dias.push({ label, value: iso })
  }
  return dias
}

const DIAS = getDias()

export default function ProgramacaoPage({ cliente }: { cliente: ClienteLogado | null }) {
  const [filmes, setFilmes] = useState<Filme[]>([])
  const [loading, setLoading] = useState(true)
  const [genero, setGenero] = useState('')
  const [classificacao, setClassificacao] = useState('')
  const [dataSelecionada, setDataSelecionada] = useState(DIAS[0].value)
  const [erro, setErro] = useState('')
  const [filmeDetalhes, setFilmeDetalhes] = useState<Filme | null>(null)
  const [trailerUrl, setTrailerUrl] = useState<string | null>(null)
  const [favoritados, setFavoritados] = useState<Set<string>>(new Set())
  const [ordenar, setOrdenar] = useState('')
  const [novasRecomendacoes, setNovasRecomendacoes] = useState(false)

  function buildParams() {
    const params = new URLSearchParams()
    params.set('data', dataSelecionada)
    if (genero) params.set('genero', genero)
    if (classificacao) params.set('classificacao', classificacao)
    if (ordenar) params.set('ordenar', ordenar)
    if (cliente) params.set('clienteId', cliente.clienteId)
    return params
  }

  useEffect(() => {
    setLoading(true)
    setErro('')
    fetch(`/api/programacao/filmes?${buildParams()}`)
      .then(r => r.json())
      .then(data => { setFilmes(data); setLoading(false) })
      .catch(() => { setErro('Erro ao carregar programação.'); setLoading(false) })
  }, [genero, classificacao, dataSelecionada, ordenar, cliente?.clienteId])

  // Polling silencioso a cada 30s para atualizar recomendações sem travar a tela
  useEffect(() => {
    if (!cliente) return
    const intervalo = setInterval(() => {
      fetch(`/api/programacao/filmes?${buildParams()}`)
        .then(r => r.json())
        .then((novos: Filme[]) => {
          setFilmes(prev => {
            const idsRecAntes = new Set(prev.filter(f => f.recomendado).map(f => f.filmeId))
            const idsRecDepois = new Set(novos.filter(f => f.recomendado).map(f => f.filmeId))
            const mudou = [...idsRecDepois].some(id => !idsRecAntes.has(id)) ||
                          [...idsRecAntes].some(id => !idsRecDepois.has(id))
            if (mudou) setNovasRecomendacoes(true)
            return novos
          })
        })
        .catch(() => {})
    }, 10_000)
    return () => clearInterval(intervalo)
  }, [cliente?.clienteId, genero, classificacao, dataSelecionada, ordenar])

  useEffect(() => {
    if (!cliente) { setFavoritados(new Set()); return }
    fetch(`/api/notificacao/favoritos/${cliente.clienteId}`)
      .then(r => r.json())
      .then((ids: string[]) => setFavoritados(new Set(ids)))
      .catch(() => {})
  }, [cliente])

  const toggleFavoritar = (filmeId: string) => {
    if (!cliente) return
    if (favoritados.has(filmeId)) {
      fetch(`/api/notificacao/favoritos/${cliente.clienteId}/${filmeId}`, { method: 'DELETE' })
        .then(() => setFavoritados(prev => { const s = new Set(prev); s.delete(filmeId); return s }))
        .catch(() => {})
    } else {
      fetch('/api/notificacao/favoritar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usuarioId: cliente.clienteId, filmeId }),
      }).then(() => setFavoritados(prev => new Set([...prev, filmeId])))
        .catch(() => {})
    }
  }

  const recomendados = filmes.filter(f => f.recomendado)
  const demais = filmes.filter(f => !f.recomendado)

  return (
    <div style={{ maxWidth: 1100 }}>
      <h2 style={{ marginBottom: 4, color: '#1a1a2e' }}>Programação</h2>

      {/* Filtro de data */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 20, flexWrap: 'wrap' }}>
        {DIAS.map(d => (
          <button key={d.value} onClick={() => setDataSelecionada(d.value)}
            style={{
              padding: '7px 14px', borderRadius: 20, fontSize: 13, cursor: 'pointer',
              border: dataSelecionada === d.value ? 'none' : '1px solid #ddd',
              background: dataSelecionada === d.value ? COR : 'white',
              color: dataSelecionada === d.value ? 'white' : '#555',
              fontWeight: dataSelecionada === d.value ? 600 : 400,
            }}>
            {d.label}
          </button>
        ))}
      </div>

      {/* Filtros */}
      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 28, alignItems: 'flex-end' }}>
        <div>
          <label style={labelStyle}>Gênero</label>
          <select value={genero} onChange={e => setGenero(e.target.value)} style={selectStyle}>
            <option value=''>Todos</option>
            {GENEROS.filter(Boolean).map(g => <option key={g} value={g}>{g.replace('_', ' ')}</option>)}
          </select>
        </div>
        <div>
          <label style={labelStyle}>Classificação máxima</label>
          <select value={classificacao} onChange={e => setClassificacao(e.target.value)} style={selectStyle}>
            <option value=''>Todas</option>
            {CLASSIFICACOES.filter(Boolean).map(c => <option key={c} value={c}>{CLASSIFICACAO_LABEL[c]}</option>)}
          </select>
        </div>
        <div>
          <label style={labelStyle}>Ordenar por</label>
          <select value={ordenar} onChange={e => setOrdenar(e.target.value)} style={selectStyle}>
            <option value=''>Padrão</option>
            <option value='popularidade'>Popularidade</option>
          </select>
        </div>
      </div>

      {erro && <p style={{ color: 'red' }}>{erro}</p>}
      {loading && <p style={{ color: '#888' }}>Carregando...</p>}

      {!loading && (
        <>
          {/* Seção "Para você" */}
          {cliente && (
            <>
              {novasRecomendacoes && (
                <div style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  background: '#e8f5e9', border: '1px solid #a5d6a7', borderRadius: 10,
                  padding: '10px 16px', marginBottom: 14,
                }}>
                  <span style={{ fontSize: 13, color: '#2e7d32', fontWeight: 500 }}>
                    ✨ Suas recomendações foram atualizadas!
                  </span>
                  <button onClick={() => setNovasRecomendacoes(false)}
                    style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#2e7d32', fontSize: 16 }}>
                    ✕
                  </button>
                </div>
              )}
              <ParaVoceSection
                recomendados={recomendados}
                clienteNome={cliente.nome.split(' ')[0]}
                favoritados={favoritados}
                onVerDetalhes={setFilmeDetalhes}
                onVerTrailer={setTrailerUrl}
                onFavoritar={toggleFavoritar}
              />
            </>
          )}

          {/* Seção "Em cartaz" */}
          {filmes.length > 0 ? (
            <>
              {cliente && recomendados.length > 0 && (
                <h3 style={{ color: '#444', fontWeight: 600, fontSize: 15, marginBottom: 16, marginTop: 0 }}>
                  Em cartaz
                </h3>
              )}
              <div style={gridStyle}>
                {filmes.map(f => (
                  <FilmeCard key={f.filmeId} filme={f} onVerDetalhes={() => setFilmeDetalhes(f)}
                    onVerTrailer={url => setTrailerUrl(url)}
                    favoritado={favoritados.has(f.filmeId)} onFavoritar={toggleFavoritar} logado={!!cliente} />
                ))}
              </div>
            </>
          ) : (
            <div style={{ textAlign: 'center', color: '#aaa', padding: 48 }}>
              Nenhum filme disponível com os filtros selecionados.
            </div>
          )}
        </>
      )}

      {filmeDetalhes && (
        <FilmeDetalhesModal filme={filmeDetalhes} onFechar={() => setFilmeDetalhes(null)}
          onVerTrailer={url => setTrailerUrl(url)}
          favoritado={favoritados.has(filmeDetalhes.filmeId)} onFavoritar={toggleFavoritar} logado={!!cliente} />
      )}

      {trailerUrl && (
        <TrailerModal url={trailerUrl} onFechar={() => setTrailerUrl(null)} />
      )}
    </div>
  )
}

/* ---------- Para você ---------- */

function ParaVoceSection({ recomendados, clienteNome, favoritados, onVerDetalhes, onVerTrailer, onFavoritar }: {
  recomendados: Filme[]
  clienteNome: string
  favoritados: Set<string>
  onVerDetalhes: (f: Filme) => void
  onVerTrailer: (url: string) => void
  onFavoritar: (id: string) => void
}) {
  return (
    <div style={{ marginBottom: 36 }}>
      {/* Header */}
      <div style={{ marginBottom: 16 }}>
        <div style={{ fontWeight: 700, color: AMBER, fontSize: 16 }}>Para {clienteNome}</div>
        <div style={{ fontSize: 12, color: '#999', marginTop: 2 }}>baseado no seu histórico e favoritos</div>
      </div>

      {recomendados.length > 0 ? (
        <div style={{
          display: 'flex', gap: 14, overflowX: 'auto',
          paddingBottom: 10, scrollbarWidth: 'thin', scrollbarColor: '#e0e0e0 transparent',
        }}>
          {recomendados.map(f => (
            <FilmeCardDestaque key={f.filmeId} filme={f}
              onVerDetalhes={() => onVerDetalhes(f)}
              onVerTrailer={onVerTrailer}
              favoritado={favoritados.has(f.filmeId)}
              onFavoritar={onFavoritar}
            />
          ))}
        </div>
      ) : (
        <div style={{
          display: 'flex', alignItems: 'center', gap: 16,
          background: '#fffde7', border: '1px dashed #fbc02d',
          borderRadius: 12, padding: '18px 22px',
        }}>
          <span style={{ fontSize: 30, flexShrink: 0 }}>🎬</span>
          <div>
            <div style={{ fontWeight: 600, color: '#5d4037', fontSize: 14 }}>Ainda sem sugestões</div>
            <div style={{ color: '#8d6e63', fontSize: 13, marginTop: 3 }}>
              Favorite filmes e compre ingressos para receber recomendações personalizadas.
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

/* ---------- Card destaque (horizontal strip) ---------- */

function FilmeCardDestaque({ filme, onVerDetalhes, onVerTrailer, favoritado, onFavoritar }: {
  filme: Filme
  onVerDetalhes: () => void
  onVerTrailer: (url: string) => void
  favoritado: boolean
  onFavoritar: (id: string) => void
}) {
  const { posterUrl } = useMoviePoster(filme.titulo)

  return (
    <div style={{
      width: 220, flexShrink: 0,
      background: 'white',
      border: '1px solid #ffe0b2',
      borderTop: `3px solid ${AMBER}`,
      borderRadius: 10,
      overflow: 'hidden',
      display: 'flex', flexDirection: 'column', gap: 0,
    }}>
      {posterUrl && (
        <div style={{ height: 130, overflow: 'hidden', cursor: 'pointer' }} onClick={onVerDetalhes}>
          <img src={posterUrl} alt={filme.titulo} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        </div>
      )}
      <div style={{ padding: 14, display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
      {/* Title + favorite */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 6 }}>
        <div onClick={onVerDetalhes}
          style={{ fontWeight: 700, fontSize: 13, color: '#1a1a2e', lineHeight: 1.35, cursor: 'pointer', flex: 1 }}>
          {filme.titulo}
        </div>
        <button onClick={() => onFavoritar(filme.filmeId)}
          title={favoritado ? 'Remover dos favoritos' : 'Favoritar'}
          style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 17, padding: 0, lineHeight: 1, flexShrink: 0 }}>
          {favoritado ? '❤️' : '🤍'}
        </button>
      </div>

      {/* Badges */}
      <div style={{ display: 'flex', gap: 5, flexWrap: 'wrap' }}>
        <span style={tagStyle('#546e7a')}>{filme.genero.replace('_', ' ')}</span>
        <span style={tagStyle(BADGE_COR[filme.classificacao] ?? '#555')}>
          {CLASSIFICACAO_LABEL[filme.classificacao] ?? filme.classificacao}
        </span>
      </div>

      {/* Meta */}
      <div style={{ fontSize: 12, color: '#999' }}>
        {filme.duracaoMinutos} min
        {filme.nota > 0 && (
          <span style={{ marginLeft: 8, color: AMBER, fontWeight: 700 }}>★ {filme.nota.toFixed(1)}</span>
        )}
      </div>

      {/* Sessões */}
      <div style={{ display: 'flex', gap: 5, flexWrap: 'wrap' }}>
        {filme.sessoes.slice(0, 3).map(s => (
          <span key={s.id} style={{ ...sessaoTag, fontSize: 11 }}>
            {s.inicio.slice(0, 5)} · {TIPO_SALA_LABEL[s.tipoSala] ?? s.tipoSala}
          </span>
        ))}
        {filme.sessoes.length > 3 && (
          <span style={{ fontSize: 11, color: '#aaa', alignSelf: 'center' }}>+{filme.sessoes.length - 3}</span>
        )}
      </div>

      {/* Actions */}
      <div style={{ display: 'flex', gap: 7, marginTop: 'auto' }}>
        <button onClick={onVerDetalhes}
          style={{ flex: 1, padding: '6px 0', fontSize: 12, background: '#f5f5f5', border: 'none', borderRadius: 6, cursor: 'pointer', color: '#444', fontWeight: 500 }}>
          Detalhes
        </button>
        {filme.trailerUrl && (
          <button onClick={() => onVerTrailer(filme.trailerUrl!)}
            style={{ flex: 1, padding: '6px 0', fontSize: 12, background: COR, color: 'white', border: 'none', borderRadius: 6, cursor: 'pointer', fontWeight: 500 }}>
            ▶ Trailer
          </button>
        )}
      </div>
      </div>
    </div>
  )
}

/* ---------- Card regular ---------- */

function FilmeCard({ filme, onVerDetalhes, onVerTrailer, favoritado, onFavoritar, logado }: {
  filme: Filme; onVerDetalhes: () => void; onVerTrailer: (url: string) => void
  favoritado: boolean; onFavoritar: (id: string) => void; logado: boolean
}) {
  const { posterUrl } = useMoviePoster(filme.titulo)

  return (
    <div style={cardStyle}>
      {posterUrl && (
        <div style={{ margin: '-16px -16px 12px', height: 180, overflow: 'hidden', borderRadius: '10px 10px 0 0', cursor: 'pointer' }} onClick={onVerDetalhes}>
          <img src={posterUrl} alt={filme.titulo} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        </div>
      )}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8, gap: 8 }}>
        <div onClick={onVerDetalhes} style={{ cursor: 'pointer', flex: 1 }}>
          <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 4, color: '#1a1a2e' }}>{filme.titulo}</div>
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
            <span style={tagStyle('#546e7a')}>{filme.genero.replace('_', ' ')}</span>
            <span style={tagStyle(BADGE_COR[filme.classificacao] ?? '#555')}>
              {CLASSIFICACAO_LABEL[filme.classificacao] ?? filme.classificacao}
            </span>
          </div>
          <div style={{ fontSize: 12, color: '#999', marginTop: 4 }}>
            {filme.duracaoMinutos} min
            {filme.nota > 0 && <span style={{ marginLeft: 8 }}>⭐ {filme.nota.toFixed(1)}/5</span>}
            <span style={{ marginLeft: 8, color: COR, fontWeight: 500 }}>Ver detalhes →</span>
          </div>
        </div>
        {logado && (
          <button onClick={() => onFavoritar(filme.filmeId)}
            title={favoritado ? 'Remover dos favoritos' : 'Favoritar'}
            style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 20, padding: 0, lineHeight: 1, flexShrink: 0 }}>
            {favoritado ? '❤️' : '🤍'}
          </button>
        )}
      </div>

      <div style={{ marginTop: 8 }}>
        <div style={{ fontSize: 12, color: '#888', marginBottom: 6 }}>Sessões:</div>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          {filme.sessoes.map(s => (
            <span key={s.id} style={{ ...sessaoTag, background: TIPO_SALA_COR[s.tipoSala] ? `${TIPO_SALA_COR[s.tipoSala]}18` : '#e3f2fd' }}>
              {s.inicio.slice(0, 5)} · Sala {s.sala} · <strong style={{ color: TIPO_SALA_COR[s.tipoSala] ?? COR }}>{TIPO_SALA_LABEL[s.tipoSala] ?? s.tipoSala}</strong>
            </span>
          ))}
        </div>
      </div>

      {filme.trailerUrl && (
        <div style={{ marginTop: 10 }}>
          <button onClick={() => onVerTrailer(filme.trailerUrl!)} style={trailerBtn}>▶ Ver trailer</button>
        </div>
      )}
    </div>
  )
}

/* ---------- Modal de detalhes ---------- */

function FilmeDetalhesModal({ filme, onFechar, onVerTrailer, favoritado, onFavoritar, logado }: {
  filme: Filme; onFechar: () => void; onVerTrailer: (url: string) => void
  favoritado: boolean; onFavoritar: (id: string) => void; logado: boolean
}) {
  return (
    <div onClick={onFechar} style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.55)',
      display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000,
    }}>
      <div onClick={e => e.stopPropagation()} style={{
        background: 'white', borderRadius: 14, padding: 32, maxWidth: 520, width: '90%',
        maxHeight: '85vh', overflowY: 'auto', boxShadow: '0 8px 32px rgba(0,0,0,0.2)',
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
          <h2 style={{ margin: 0, fontSize: 20, color: '#1a1a2e', lineHeight: 1.3 }}>{filme.titulo}</h2>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexShrink: 0, marginLeft: 12 }}>
            {logado && (
              <button onClick={() => onFavoritar(filme.filmeId)}
                title={favoritado ? 'Remover dos favoritos' : 'Favoritar'}
                style={{
                  background: favoritado ? '#fce4ec' : '#f5f5f5', border: 'none', borderRadius: 8,
                  cursor: 'pointer', fontSize: 14, padding: '6px 12px',
                  color: favoritado ? '#c62828' : '#666', fontWeight: 500,
                }}>
                {favoritado ? '❤️ Favoritado' : '🤍 Favoritar'}
              </button>
            )}
            <button onClick={onFechar} style={{ background: 'none', border: 'none', fontSize: 22, cursor: 'pointer', color: '#888' }}>✕</button>
          </div>
        </div>

        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 20 }}>
          <span style={tagStyle('#546e7a')}>{filme.genero.replace('_', ' ')}</span>
          <span style={tagStyle(BADGE_COR[filme.classificacao] ?? '#555')}>
            {CLASSIFICACAO_LABEL[filme.classificacao] ?? filme.classificacao}
          </span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 20 }}>
          <InfoItem label='Duração' value={`${filme.duracaoMinutos} minutos`} />
          <InfoItem label='Classificação' value={CLASSIFICACAO_LABEL[filme.classificacao] ?? filme.classificacao} />
          <InfoItem label='Gênero' value={filme.genero.replace('_', ' ')} />
          {filme.nota > 0 && <InfoItem label='Nota' value={`${filme.nota.toFixed(1)} / 5`} highlight />}
        </div>

        {filme.sinopse && (
          <div style={{ marginBottom: 20 }}>
            <div style={{ fontSize: 12, color: '#888', marginBottom: 6, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 0.5 }}>Sinopse</div>
            <p style={{ margin: 0, fontSize: 14, color: '#333', lineHeight: 1.6 }}>{filme.sinopse}</p>
          </div>
        )}

        <div>
          <div style={{ fontSize: 12, color: '#888', marginBottom: 8, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 0.5 }}>Sessões disponíveis</div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {filme.sessoes.map(s => (
              <span key={s.id} style={{ ...sessaoTag, background: TIPO_SALA_COR[s.tipoSala] ? `${TIPO_SALA_COR[s.tipoSala]}18` : '#e3f2fd' }}>
              {s.inicio.slice(0, 5)} · Sala {s.sala} · <strong style={{ color: TIPO_SALA_COR[s.tipoSala] ?? COR }}>{TIPO_SALA_LABEL[s.tipoSala] ?? s.tipoSala}</strong>
            </span>
            ))}
          </div>
        </div>

        {filme.trailerUrl && (
          <div style={{ marginTop: 20 }}>
            <button onClick={() => onVerTrailer(filme.trailerUrl!)}
              style={{ padding: '10px 20px', background: COR, color: 'white', border: 'none', borderRadius: 8, fontSize: 14, cursor: 'pointer', fontWeight: 600 }}>
              ▶ Assistir trailer
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

function InfoItem({ label, value, highlight }: { label: string; value: string; highlight?: boolean }) {
  return (
    <div style={{ background: '#f8f9fa', borderRadius: 8, padding: '10px 14px' }}>
      <div style={{ fontSize: 11, color: '#999', marginBottom: 2, textTransform: 'uppercase', letterSpacing: 0.5 }}>{label}</div>
      <div style={{ fontSize: 14, color: highlight ? COR : '#333', fontWeight: highlight ? 700 : 500 }}>{value}</div>
    </div>
  )
}

/* ---------- Modal de trailer ---------- */

function TrailerModal({ url, onFechar }: { url: string; onFechar: () => void }) {
  const embedUrl = getYouTubeEmbedUrl(url)
  return (
    <div onClick={onFechar} style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.85)',
      display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1100,
    }}>
      <div onClick={e => e.stopPropagation()} style={{ width: '90%', maxWidth: 800, position: 'relative' }}>
        <button onClick={onFechar} style={{
          position: 'absolute', top: -36, right: 0,
          background: 'none', border: 'none', color: 'white', fontSize: 28, cursor: 'pointer',
        }}>✕</button>
        {embedUrl ? (
          <div style={{ position: 'relative', paddingBottom: '56.25%', height: 0 }}>
            <iframe src={embedUrl} style={{
              position: 'absolute', top: 0, left: 0, width: '100%', height: '100%',
              borderRadius: 10, border: 'none',
            }} allow='autoplay; encrypted-media' allowFullScreen />
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: 32 }}>
            <a href={url} target='_blank' rel='noreferrer' style={{ color: 'white', fontSize: 16 }}>
              Abrir trailer em nova aba →
            </a>
          </div>
        )}
      </div>
    </div>
  )
}

function getYouTubeEmbedUrl(url: string): string | null {
  const m = url.match(/(?:youtu\.be\/|youtube\.com\/(?:watch\?v=|embed\/))([a-zA-Z0-9_-]{11})/)
  return m ? `https://www.youtube.com/embed/${m[1]}?autoplay=1` : null
}

const gridStyle: React.CSSProperties = { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 16, marginBottom: 8 }
const cardStyle: React.CSSProperties = { background: 'white', border: '1px solid #e0e0e0', borderRadius: 10, padding: 16 }
const labelStyle: React.CSSProperties = { display: 'block', fontSize: 12, color: '#888', marginBottom: 4 }
const selectStyle: React.CSSProperties = { padding: '7px 10px', borderRadius: 8, border: '1px solid #ddd', fontSize: 14, minWidth: 150 }
const sessaoTag: React.CSSProperties = { background: '#e3f2fd', color: COR, fontSize: 12, padding: '3px 8px', borderRadius: 12 }
const trailerBtn: React.CSSProperties = { fontSize: 12, background: COR, color: 'white', border: 'none', borderRadius: 6, padding: '4px 10px', cursor: 'pointer' }
function tagStyle(bg: string): React.CSSProperties {
  return { background: bg, color: 'white', fontSize: 11, padding: '2px 7px', borderRadius: 10 }
}
