import { useEffect, useState } from 'react'
import api from '../api/client'
import { fetchMovieImages } from '../lib/tmdb'

interface Filme {
  id: string; titulo: string; duracaoMinutos: number
  classificacaoIndicativa: string; genero: string
  trailerURL: string; ativo: boolean; sinopse?: string; nota?: number
}

const CLASSIFICACOES = ['LIVRE','DEZ','DOZE','QUATORZE','DEZESSEIS','DEZOITO']
const GENEROS = ['ACAO','AVENTURA','COMEDIA','DRAMA','FICCAO_CIENTIFICA','TERROR','ROMANCE','ANIMACAO','DOCUMENTARIO']
const GENERO_LABEL: Record<string,string> = { ACAO:'Ação', AVENTURA:'Aventura', COMEDIA:'Comédia', DRAMA:'Drama', FICCAO_CIENTIFICA:'Ficção Científica', TERROR:'Terror', ROMANCE:'Romance', ANIMACAO:'Animação', DOCUMENTARIO:'Documentário' }
const CLASS_COR: Record<string,{bg:string,tx:string}> = { LIVRE:{bg:'#E8F5E9',tx:'#2E7D32'}, DEZ:{bg:'#E3F2FD',tx:'#1565C0'}, DOZE:{bg:'#E0F7FA',tx:'#00838F'}, QUATORZE:{bg:'#FFF3E0',tx:'#E65100'}, DEZESSEIS:{bg:'#FBE9E7',tx:'#D84315'}, DEZOITO:{bg:'#FFEBEE',tx:'#C62828'} }
const GENERO_COR: Record<string,string> = { ACAO:'#C62828', AVENTURA:'#E65100', COMEDIA:'#F9A825', DRAMA:'#6A1B9A', FICCAO_CIENTIFICA:'#1565C0', TERROR:'#212121', ROMANCE:'#AD1457', ANIMACAO:'#00838F', DOCUMENTARIO:'#4E342E' }

const COR = '#8B0000'
const formVazio = { titulo:'', duracaoMinutos:90, classificacaoIndicativa:'LIVRE', genero:'ACAO', trailerURL:'', sinopse:'', nota:'' }

type Tab = 'TODOS'|'ATIVOS'|'INATIVOS'
type Poster = { posterUrl: string|null }

const inp: React.CSSProperties = { width:'100%', padding:'10px 12px', borderRadius:8, border:'1px solid #DDD', fontSize:14, boxSizing:'border-box' }
const lbl: React.CSSProperties = { display:'block', marginBottom:8, fontSize:13, fontWeight:500, color:'#1a1a2e' }

export default function CatalogoPage() {
  const [filmes, setFilmes] = useState<Filme[]>([])
  const [posters, setPosters] = useState<Record<string,Poster>>({})
  const [tab, setTab] = useState<Tab>('TODOS')
  const [form, setForm] = useState(formVazio)
  const [editandoId, setEditandoId] = useState<string|null>(null)
  const [modal, setModal] = useState(false)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')

  async function carregar() {
    const res = await api.get<Filme[]>('/filmes')
    setFilmes(res.data)
    res.data.forEach(f => {
      fetchMovieImages(f.titulo).then(r => setPosters(p => ({ ...p, [f.id]: r })))
    })
  }

  useEffect(() => { carregar() }, [])

  function msg(ok: string) { setSucesso(ok); setErro(''); setTimeout(() => setSucesso(''), 3000) }
  function err(e: unknown) {
    const ax = e as { response?: { data?: { mensagem?: string; message?: string }; status?: number } }
    setErro(ax.response?.data?.mensagem ?? ax.response?.data?.message ?? `Erro ${ax.response?.status ?? ''}`)
  }

  function abrirCadastro() { setForm(formVazio); setEditandoId(null); setErro(''); setModal(true) }
  function abrirEdicao(f: Filme) {
    setForm({ titulo:f.titulo, duracaoMinutos:f.duracaoMinutos, classificacaoIndicativa:f.classificacaoIndicativa, genero:f.genero, trailerURL:f.trailerURL??'', sinopse:f.sinopse??'', nota:f.nota!=null?String(f.nota):'' })
    setEditandoId(f.id); setErro(''); setModal(true)
  }

  async function salvar(e: React.FormEvent) {
    e.preventDefault()
    const payload = { ...form, nota: form.nota !== '' ? Number(form.nota) : null, sinopse: form.sinopse || null }
    try {
      if (editandoId) { await api.put(`/filmes/${editandoId}`, payload) } else { await api.post('/filmes', payload) }
      setModal(false); await carregar(); msg(editandoId ? 'Filme atualizado!' : 'Filme cadastrado!')
    } catch(e) { err(e) }
  }

  async function toggleAtivo(f: Filme) {
    try { await api.patch(`/filmes/${f.id}/${f.ativo?'desativar':'ativar'}`); await carregar(); msg(`Filme ${f.ativo?'desativado':'ativado'}!`) } catch(e) { err(e) }
  }

  async function remover(f: Filme) {
    if (!confirm(`Remover "${f.titulo}"? Esta ação não pode ser desfeita.`)) return
    try { await api.delete(`/filmes/${f.id}`); await carregar(); msg('Filme removido!') } catch(e) { err(e) }
  }

  const filtrados = filmes.filter(f => tab==='TODOS' ? true : tab==='ATIVOS' ? f.ativo : !f.ativo)
  const ativos = filmes.filter(f => f.ativo).length

  return (
    <div style={{ fontFamily:'sans-serif', color:'#1a1a2e', padding:'0 0 40px' }}>

      {/* Header */}
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:24 }}>
        <div>
          <h1 style={{ margin:'0 0 6px', fontSize:26, fontWeight:700 }}>Catálogo de Filmes</h1>
          <p style={{ margin:0, color:'#6c757d', fontSize:14 }}>Gerencie os filmes exibidos no cinema</p>
        </div>
        <button onClick={abrirCadastro} style={{ backgroundColor:COR, color:'white', padding:'12px 20px', borderRadius:8, border:'none', cursor:'pointer', fontWeight:600, fontSize:14, display:'flex', alignItems:'center', gap:8 }}>
          <span style={{ fontSize:18 }}>+</span> Novo Filme
        </button>
      </div>

      {/* Notificações */}
      {erro    && <div style={{ background:'#FFEBEE', border:'1px solid #FFCDD2', borderRadius:8, padding:'12px 16px', marginBottom:16, color:'#C62828', fontSize:14 }}>{erro}</div>}
      {sucesso && <div style={{ background:'#E8F5E9', border:'1px solid #C8E6C9', borderRadius:8, padding:'12px 16px', marginBottom:16, color:'#2E7D32', fontSize:14 }}>{sucesso}</div>}

      {/* Stats */}
      <div style={{ display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:16, marginBottom:24 }}>
        {[{ label:'Total de Filmes', val:filmes.length, cor:'#1a1a2e' },
          { label:'Filmes Ativos', val:ativos, cor:'#2E7D32' },
          { label:'Filmes Inativos', val:filmes.length-ativos, cor:'#C62828' }].map(s => (
          <div key={s.label} style={{ background:'white', borderRadius:12, border:'1px solid #EBEBEB', padding:'20px 24px', boxShadow:'0 1px 3px rgba(0,0,0,0.06)' }}>
            <div style={{ fontSize:13, color:'#6c757d', marginBottom:6 }}>{s.label}</div>
            <div style={{ fontSize:28, fontWeight:700, color:s.cor }}>{s.val}</div>
          </div>
        ))}
      </div>

      {/* Tabs */}
      <div style={{ display:'flex', borderBottom:'2px solid #EBEBEB', marginBottom:24, gap:4 }}>
        {(['TODOS','ATIVOS','INATIVOS'] as Tab[]).map(t => (
          <button key={t} onClick={() => setTab(t)} style={{ padding:'10px 20px', border:'none', cursor:'pointer', background:'transparent', fontWeight:600, fontSize:13, borderBottom:tab===t?`2px solid ${COR}`:'2px solid transparent', color:tab===t?COR:'#6c757d', marginBottom:-2 }}>
            {t==='TODOS'?'Todos':t==='ATIVOS'?'Ativos':'Inativos'}
          </button>
        ))}
      </div>

      {/* Grid de filmes */}
      {filtrados.length === 0 && (
        <div style={{ textAlign:'center', padding:'60px 20px', color:'#6c757d' }}>
          <div style={{ fontSize:48, marginBottom:12, opacity:.3 }}>&#127916;</div>
          <p style={{ margin:0, fontSize:16 }}>Nenhum filme encontrado.</p>
        </div>
      )}
      <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(240px,1fr))', gap:20 }}>
        {filtrados.map(f => {
          const poster = posters[f.id]?.posterUrl
          const cls = CLASS_COR[f.classificacaoIndicativa] ?? {bg:'#F5F5F5',tx:'#333'}
          const genCor = GENERO_COR[f.genero] ?? '#555'
          return (
            <div key={f.id} style={{ background:'white', borderRadius:12, border:'1px solid #EBEBEB', boxShadow:'0 2px 8px rgba(0,0,0,0.08)', overflow:'hidden', display:'flex', flexDirection:'column', opacity:f.ativo?1:.6 }}>
              {/* Poster */}
              <div style={{ height:200, background: poster ? `url(${poster}) center/cover no-repeat` : `linear-gradient(135deg, ${genCor}22, ${genCor}44)`, position:'relative', display:'flex', alignItems:'center', justifyContent:'center' }}>
                {!poster && <span style={{ fontSize:48, fontWeight:700, color:genCor, opacity:.4 }}>{f.titulo.charAt(0)}</span>}
                <div style={{ position:'absolute', top:10, right:10, background:cls.bg, color:cls.tx, padding:'3px 8px', borderRadius:6, fontSize:11, fontWeight:700 }}>
                  {f.classificacaoIndicativa}
                </div>
                {!f.ativo && <div style={{ position:'absolute', inset:0, background:'rgba(0,0,0,0.4)', display:'flex', alignItems:'center', justifyContent:'center' }}><span style={{ background:'rgba(0,0,0,0.7)', color:'white', padding:'4px 12px', borderRadius:20, fontSize:12, fontWeight:600 }}>INATIVO</span></div>}
              </div>

              {/* Info */}
              <div style={{ padding:'16px', flex:1, display:'flex', flexDirection:'column', gap:8 }}>
                <div style={{ fontWeight:700, fontSize:15, lineHeight:1.3, color:'#1a1a2e' }}>{f.titulo}</div>
                <div style={{ display:'flex', gap:6, flexWrap:'wrap' }}>
                  <span style={{ background:genCor+'18', color:genCor, padding:'2px 8px', borderRadius:20, fontSize:11, fontWeight:600 }}>{GENERO_LABEL[f.genero]??f.genero}</span>
                </div>
                <div style={{ display:'flex', gap:12, fontSize:13, color:'#6c757d' }}>
                  <span>&#9200; {f.duracaoMinutos} min</span>
                  {f.nota != null && f.nota > 0 && <span style={{ color:'#F9A825' }}>&#9733; {f.nota.toFixed(1)}</span>}
                </div>
                {f.sinopse && <p style={{ margin:0, fontSize:12, color:'#888', lineHeight:1.5, display:'-webkit-box', WebkitLineClamp:2, WebkitBoxOrient:'vertical', overflow:'hidden' }}>{f.sinopse}</p>}
              </div>

              {/* Ações */}
              <div style={{ padding:'12px 16px', borderTop:'1px solid #F0F0F0', display:'flex', gap:6 }}>
                <button onClick={() => abrirEdicao(f)} style={{ flex:1, padding:'7px', borderRadius:6, border:'1px solid #DDD', background:'white', cursor:'pointer', fontSize:12, fontWeight:600, color:'#1565C0' }}>Editar</button>
                <button onClick={() => toggleAtivo(f)} style={{ flex:1, padding:'7px', borderRadius:6, border:'none', background:f.ativo?'#FFF3E0':'#E8F5E9', cursor:'pointer', fontSize:12, fontWeight:600, color:f.ativo?'#E65100':'#2E7D32' }}>{f.ativo?'Desativar':'Ativar'}</button>
                <button onClick={() => remover(f)} style={{ padding:'7px 10px', borderRadius:6, border:'none', background:'#FFEBEE', cursor:'pointer', fontSize:12, fontWeight:600, color:'#C62828' }}>&#128465;</button>
              </div>
            </div>
          )
        })}
      </div>

      {/* Modal */}
      {modal && (
        <div style={{ position:'fixed', inset:0, background:'rgba(0,0,0,0.5)', display:'flex', justifyContent:'center', alignItems:'center', zIndex:1000, padding:20 }}>
          <div style={{ background:'white', borderRadius:12, width:'100%', maxWidth:560, maxHeight:'90vh', display:'flex', flexDirection:'column' }}>
            <div style={{ padding:'24px 28px', borderBottom:'1px solid #EBEBEB', display:'flex', justifyContent:'space-between', alignItems:'center' }}>
              <h2 style={{ margin:0, fontSize:20, color:'#1a1a2e' }}>{editandoId?'Editar Filme':'Novo Filme'}</h2>
              <button onClick={() => setModal(false)} style={{ background:'none', border:'none', fontSize:22, color:'#888', cursor:'pointer' }}>&#215;</button>
            </div>

            <div style={{ padding:'24px 28px', overflowY:'auto', flex:1 }}>
              {erro && <div style={{ background:'#FFEBEE', borderRadius:8, padding:'10px 14px', marginBottom:16, color:'#C62828', fontSize:13 }}>{erro}</div>}
              <form id="formFilme" onSubmit={salvar}>
                <div style={{ marginBottom:16 }}>
                  <label style={lbl}>Título *</label>
                  <input style={inp} required value={form.titulo} onChange={e => setForm({...form,titulo:e.target.value})} placeholder="Ex: Oppenheimer" />
                </div>
                <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:16, marginBottom:16 }}>
                  <div>
                    <label style={lbl}>Duração (min) *</label>
                    <input style={inp} type="number" required min={1} value={form.duracaoMinutos} onChange={e => setForm({...form,duracaoMinutos:Number(e.target.value)})} />
                  </div>
                  <div>
                    <label style={lbl}>Nota (0-5)</label>
                    <input style={inp} type="number" min={0} max={5} step={0.1} value={form.nota} onChange={e => setForm({...form,nota:e.target.value})} placeholder="Ex: 4.5" />
                  </div>
                </div>
                <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:16, marginBottom:16 }}>
                  <div>
                    <label style={lbl}>Classificação *</label>
                    <select style={inp} value={form.classificacaoIndicativa} onChange={e => setForm({...form,classificacaoIndicativa:e.target.value})}>
                      {CLASSIFICACOES.map(c => <option key={c} value={c}>{c}</option>)}
                    </select>
                  </div>
                  <div>
                    <label style={lbl}>Gênero *</label>
                    <select style={inp} value={form.genero} onChange={e => setForm({...form,genero:e.target.value})}>
                      {GENEROS.map(g => <option key={g} value={g}>{GENERO_LABEL[g]??g}</option>)}
                    </select>
                  </div>
                </div>
                <div style={{ marginBottom:16 }}>
                  <label style={lbl}>URL do Trailer</label>
                  <input style={inp} value={form.trailerURL} onChange={e => setForm({...form,trailerURL:e.target.value})} placeholder="https://youtube.com/..." />
                </div>
                <div>
                  <label style={lbl}>Sinopse</label>
                  <textarea style={{ ...inp, minHeight:80, resize:'vertical' }} value={form.sinopse} onChange={e => setForm({...form,sinopse:e.target.value})} placeholder="Breve descrição do filme..." />
                </div>
              </form>
            </div>

            <div style={{ padding:'20px 28px', borderTop:'1px solid #EBEBEB', display:'flex', gap:12 }}>
              <button onClick={() => setModal(false)} style={{ flex:1, padding:12, background:'white', border:'1px solid #DDD', borderRadius:8, cursor:'pointer', fontWeight:600, color:'#1a1a2e' }}>Cancelar</button>
              <button form="formFilme" type="submit" style={{ flex:1, padding:12, background:COR, color:'white', border:'none', borderRadius:8, cursor:'pointer', fontWeight:600 }}>{editandoId?'Salvar alterações':'Cadastrar'}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
