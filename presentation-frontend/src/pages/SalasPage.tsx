import { useEffect, useState } from 'react'
import api from '../api/client'

interface Sala { id: string; numero: number; capacidade: number; tipo: string }

const TIPOS = ['PADRAO','IMAX','TRES_D','VIP']
const TIPO_INFO: Record<string,{label:string,cor:string,bg:string,desc:string}> = {
  PADRAO: { label:'Padrão', cor:'#424242', bg:'#F5F5F5', desc:'Sala padrão' },
  IMAX:   { label:'IMAX',   cor:'#6A1B9A', bg:'#F3E5F5', desc:'Tela gigante e som envolvente' },
  TRES_D: { label:'3D',     cor:'#00838F', bg:'#E0F7FA', desc:'Experiência tridimensional' },
  VIP:    { label:'VIP',    cor:'#E65100', bg:'#FBE9E7', desc:'Poltronas premium' },
}

const COR = '#8B0000'
const formVazio = { numero:'', capacidade:'', tipo:'PADRAO' }
const inp: React.CSSProperties = { width:'100%', padding:'10px 12px', borderRadius:8, border:'1px solid #DDD', fontSize:14, boxSizing:'border-box' }
const lbl: React.CSSProperties = { display:'block', marginBottom:8, fontSize:13, fontWeight:500, color:'#1a1a2e' }

export default function SalasPage() {
  const [salas, setSalas] = useState<Sala[]>([])
  const [form, setForm] = useState(formVazio)
  const [editandoId, setEditandoId] = useState<string|null>(null)
  const [modal, setModal] = useState(false)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')

  async function carregar() { const r = await api.get<Sala[]>('/salas'); setSalas(r.data) }
  useEffect(() => { carregar() }, [])

  function msg(ok: string) { setSucesso(ok); setErro(''); setTimeout(() => setSucesso(''), 3000) }
  function err(e: unknown) {
    const ax = e as { response?: { data?: { mensagem?: string; message?: string }; status?: number } }
    setErro(ax.response?.data?.mensagem ?? ax.response?.data?.message ?? `Erro ${ax.response?.status ?? ''}`)
  }

  function abrirCadastro() { setForm(formVazio); setEditandoId(null); setErro(''); setModal(true) }
  function abrirEdicao(s: Sala) { setForm({ numero:String(s.numero), capacidade:String(s.capacidade), tipo:s.tipo }); setEditandoId(s.id); setErro(''); setModal(true) }

  async function salvar(e: React.FormEvent) {
    e.preventDefault()
    const payload = { numero:Number(form.numero), capacidade:Number(form.capacidade), tipo:form.tipo }
    try {
      if (editandoId) { await api.put(`/salas/${editandoId}`, payload) } else { await api.post('/salas', payload) }
      setModal(false); await carregar(); msg(editandoId ? 'Sala atualizada!' : 'Sala cadastrada!')
    } catch(e) { err(e) }
  }

  async function remover(s: Sala) {
    if (!confirm(`Remover Sala ${s.numero}?`)) return
    try { await api.delete(`/salas/${s.id}`); await carregar(); msg('Sala removida!') } catch(e) { err(e) }
  }

  const capacidadeTotal = salas.reduce((a,s) => a + s.capacidade, 0)

  return (
    <div style={{ fontFamily:'sans-serif', color:'#1a1a2e', paddingBottom:40 }}>

      {/* Header */}
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:24 }}>
        <div>
          <h1 style={{ margin:'0 0 6px', fontSize:26, fontWeight:700 }}>Salas</h1>
          <p style={{ margin:0, color:'#6c757d', fontSize:14 }}>Gerencie as salas de exibição do cinema</p>
        </div>
        <button onClick={abrirCadastro} style={{ backgroundColor:COR, color:'white', padding:'12px 20px', borderRadius:8, border:'none', cursor:'pointer', fontWeight:600, fontSize:14, display:'flex', alignItems:'center', gap:8 }}>
          <span style={{ fontSize:18 }}>+</span> Nova Sala
        </button>
      </div>

      {erro    && <div style={{ background:'#FFEBEE', border:'1px solid #FFCDD2', borderRadius:8, padding:'12px 16px', marginBottom:16, color:'#C62828', fontSize:14 }}>{erro}</div>}
      {sucesso && <div style={{ background:'#E8F5E9', border:'1px solid #C8E6C9', borderRadius:8, padding:'12px 16px', marginBottom:16, color:'#2E7D32', fontSize:14 }}>{sucesso}</div>}

      {/* Stats */}
      <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fit,minmax(180px,1fr))', gap:16, marginBottom:28 }}>
        {[{ label:'Total de Salas', val:salas.length },
          { label:'Capacidade Total', val:`${capacidadeTotal} lugares` },
          { label:'Salas IMAX', val:salas.filter(s=>s.tipo==='IMAX').length },
          { label:'Salas VIP', val:salas.filter(s=>s.tipo==='VIP').length }].map(s => (
          <div key={s.label} style={{ background:'white', borderRadius:12, border:'1px solid #EBEBEB', padding:'18px 20px', boxShadow:'0 1px 3px rgba(0,0,0,0.06)' }}>
            <div style={{ fontSize:12, color:'#6c757d', marginBottom:6, textTransform:'uppercase', letterSpacing:.5 }}>{s.label}</div>
            <div style={{ fontSize:24, fontWeight:700 }}>{s.val}</div>
          </div>
        ))}
      </div>

      {/* Grid de salas */}
      {salas.length === 0 && (
        <div style={{ textAlign:'center', padding:'60px 20px', color:'#6c757d' }}>
          <div style={{ fontSize:48, marginBottom:12, opacity:.3 }}>&#127909;</div>
          <p style={{ margin:0, fontSize:16 }}>Nenhuma sala cadastrada.</p>
        </div>
      )}
      <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill,minmax(260px,1fr))', gap:20 }}>
        {salas.map(s => {
          const info = TIPO_INFO[s.tipo] ?? { label:s.tipo, cor:'#555', bg:'#F5F5F5', desc:'' }
          return (
            <div key={s.id} style={{ background:'white', borderRadius:12, border:`1px solid #EBEBEB`, boxShadow:'0 2px 8px rgba(0,0,0,0.07)', overflow:'hidden' }}>
              {/* Topo colorido */}
              <div style={{ background:`linear-gradient(135deg, ${info.cor}, ${info.cor}CC)`, padding:'20px 24px', color:'white' }}>
                <div style={{ fontSize:13, fontWeight:600, opacity:.85, marginBottom:4, textTransform:'uppercase', letterSpacing:.5 }}>{info.label}</div>
                <div style={{ fontSize:36, fontWeight:800, letterSpacing:-1 }}>Sala {s.numero}</div>
              </div>
              {/* Info */}
              <div style={{ padding:'16px 20px' }}>
                <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:12 }}>
                  <span style={{ fontSize:13, color:'#6c757d' }}>Capacidade</span>
                  <span style={{ fontWeight:700, fontSize:15 }}>{s.capacidade} lugares</span>
                </div>
                {/* Barra de capacidade */}
                <div style={{ background:'#F0F0F0', borderRadius:20, height:6, marginBottom:16, overflow:'hidden' }}>
                  <div style={{ height:'100%', borderRadius:20, background:info.cor, width:`${Math.min(100,(s.capacidade/300)*100)}%`, transition:'width .5s' }} />
                </div>
                <div style={{ display:'flex', alignItems:'center', gap:8 }}>
                  <span style={{ background:info.bg, color:info.cor, padding:'4px 10px', borderRadius:20, fontSize:11, fontWeight:700 }}>{info.label}</span>
                  <span style={{ fontSize:12, color:'#888' }}>{info.desc}</span>
                </div>
              </div>
              {/* Ações */}
              <div style={{ padding:'12px 20px', borderTop:'1px solid #F0F0F0', display:'flex', gap:8 }}>
                <button onClick={() => abrirEdicao(s)} style={{ flex:1, padding:'8px', borderRadius:6, border:'1px solid #DDD', background:'white', cursor:'pointer', fontSize:13, fontWeight:600, color:'#1565C0' }}>Editar</button>
                <button onClick={() => remover(s)} style={{ flex:1, padding:'8px', borderRadius:6, border:'none', background:'#FFEBEE', cursor:'pointer', fontSize:13, fontWeight:600, color:'#C62828' }}>Remover</button>
              </div>
            </div>
          )
        })}
      </div>

      {/* Modal */}
      {modal && (
        <div style={{ position:'fixed', inset:0, background:'rgba(0,0,0,0.5)', display:'flex', justifyContent:'center', alignItems:'center', zIndex:1000 }}>
          <div style={{ background:'white', borderRadius:12, width:'100%', maxWidth:440 }}>
            <div style={{ padding:'24px 28px', borderBottom:'1px solid #EBEBEB', display:'flex', justifyContent:'space-between', alignItems:'center' }}>
              <h2 style={{ margin:0, fontSize:20, color:'#1a1a2e' }}>{editandoId?'Editar Sala':'Nova Sala'}</h2>
              <button onClick={() => setModal(false)} style={{ background:'none', border:'none', fontSize:22, color:'#888', cursor:'pointer' }}>&#215;</button>
            </div>
            <form id="formSala" onSubmit={salvar} style={{ padding:'24px 28px' }}>
              {erro && <div style={{ background:'#FFEBEE', borderRadius:8, padding:'10px 14px', marginBottom:16, color:'#C62828', fontSize:13 }}>{erro}</div>}
              <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:16, marginBottom:16 }}>
                <div>
                  <label style={lbl}>Número *</label>
                  <input style={inp} type="number" required min={1} value={form.numero} onChange={e => setForm({...form,numero:e.target.value})} placeholder="Ex: 1" />
                </div>
                <div>
                  <label style={lbl}>Capacidade *</label>
                  <input style={inp} type="number" required min={1} value={form.capacidade} onChange={e => setForm({...form,capacidade:e.target.value})} placeholder="Ex: 120" />
                </div>
              </div>
              <div style={{ marginBottom:8 }}>
                <label style={lbl}>Tipo *</label>
                <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:8 }}>
                  {TIPOS.map(t => {
                    const info = TIPO_INFO[t]
                    const sel = form.tipo === t
                    return (
                      <button key={t} type="button" onClick={() => setForm({...form,tipo:t})} style={{ padding:'10px 14px', borderRadius:8, border:`2px solid ${sel?info.cor:'#DDD'}`, background:sel?info.bg:'white', cursor:'pointer', textAlign:'left', transition:'all .15s' }}>
                        <div style={{ fontSize:13, fontWeight:700, color:sel?info.cor:'#333' }}>{info.label}</div>
                        <div style={{ fontSize:11, color:'#888', marginTop:2 }}>{info.desc}</div>
                      </button>
                    )
                  })}
                </div>
              </div>
            </form>
            <div style={{ padding:'0 28px 24px', display:'flex', gap:12 }}>
              <button onClick={() => setModal(false)} style={{ flex:1, padding:12, background:'white', border:'1px solid #DDD', borderRadius:8, cursor:'pointer', fontWeight:600, color:'#1a1a2e' }}>Cancelar</button>
              <button form="formSala" type="submit" style={{ flex:1, padding:12, background:COR, color:'white', border:'none', borderRadius:8, cursor:'pointer', fontWeight:600 }}>{editandoId?'Salvar':'Cadastrar'}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
