import React, { useEffect, useState } from 'react'
import api from '../api/client'

interface Sessao { id:string; filmeId:string; filmeTitulo:string; salaId:string; salaNumero:number; inicio:string; fim:string }
interface Grade  { id:string; inicio:string; fim:string; sessoes:Sessao[] }
interface Filme  { id:string; titulo:string; ativo:boolean }
interface Sala   { id:string; numero:number; capacidade:number; tipo:string }

type ModalTipo = 'GRADE'|'SESSAO'|null
type EditandoSessao = { gradeId:string; sessaoId:string; filmeId:string; salaId:string; inicio:string }
type EditandoGrade  = { id:string; inicio:string; fim:string }

const COR = '#8B0000'
const inp: React.CSSProperties = { width:'100%', padding:'10px 12px', borderRadius:8, border:'1px solid #DDD', fontSize:14, boxSizing:'border-box' }
const lbl: React.CSSProperties = { display:'block', marginBottom:6, fontSize:13, fontWeight:500, color:'#1a1a2e' }

function diasRestantes(fim: string) {
  const dias = Math.ceil((new Date(fim).getTime() - Date.now()) / 86400000)
  if (dias < 0)  return { txt:'Encerrada', cor:'#888', bg:'#F5F5F5' }
  if (dias === 0) return { txt:'Encerra hoje', cor:'#E65100', bg:'#FBE9E7' }
  if (dias <= 7)  return { txt:`${dias} dias`, cor:'#E65100', bg:'#FFF3E0' }
  return { txt:`${dias} dias`, cor:'#2E7D32', bg:'#E8F5E9' }
}

export default function GradePage() {
  const [grades, setGrades]   = useState<Grade[]>([])
  const [filmes, setFilmes]   = useState<Filme[]>([])
  const [salas,  setSalas]    = useState<Sala[]>([])
  const [erro,   setErro]     = useState('')
  const [sucesso,setSucesso]  = useState('')

  const [modal, setModal] = useState<ModalTipo>(null)
  const [formGrade,  setFormGrade]  = useState({ inicio:'', fim:'' })
  const [formSessao, setFormSessao] = useState({ gradeId:'', filmeId:'', salaId:'', inicio:'' })
  const [editandoGrade,  setEditandoGrade]  = useState<EditandoGrade|null>(null)
  const [editandoSessao, setEditandoSessao] = useState<EditandoSessao|null>(null)

  async function carregar() {
    const [g,f,s] = await Promise.all([api.get<Grade[]>('/grades'), api.get<Filme[]>('/filmes'), api.get<Sala[]>('/salas')])
    setGrades(g.data); setFilmes(f.data.filter(f=>f.ativo)); setSalas(s.data)
  }
  useEffect(() => { carregar() }, [])

  function msg(ok: string) { setSucesso(ok); setErro(''); setTimeout(() => setSucesso(''), 3000) }
  function err(e: unknown) {
    const ax = e as { response?: { data?: { mensagem?: string; message?: string }; status?: number } }
    setErro(ax.response?.data?.mensagem ?? ax.response?.data?.message ?? `Erro ${ax.response?.status ?? ''}`)
  }

  async function criarGrade(e: React.FormEvent) {
    e.preventDefault()
    try { await api.post('/grades', formGrade); setModal(null); setFormGrade({inicio:'',fim:''}); await carregar(); msg('Grade criada!') } catch(e) { err(e) }
  }

  async function salvarEdicaoGrade(e: React.FormEvent) {
    e.preventDefault()
    if (!editandoGrade) return
    try { await api.put(`/grades/${editandoGrade.id}`, { inicio:editandoGrade.inicio, fim:editandoGrade.fim }); setEditandoGrade(null); await carregar(); msg('Grade atualizada!') } catch(e) { err(e) }
  }

  async function adicionarSessao(e: React.FormEvent) {
    e.preventDefault()
    try { await api.post(`/grades/${formSessao.gradeId}/sessoes`, { filmeId:formSessao.filmeId, salaId:formSessao.salaId, inicio:formSessao.inicio }); setModal(null); setFormSessao({gradeId:'',filmeId:'',salaId:'',inicio:''}); await carregar(); msg('Sessão adicionada!') } catch(e) { err(e) }
  }

  async function salvarEdicaoSessao(e: React.FormEvent) {
    e.preventDefault()
    if (!editandoSessao) return
    try { await api.put(`/grades/${editandoSessao.gradeId}/sessoes/${editandoSessao.sessaoId}`, { filmeId:editandoSessao.filmeId, salaId:editandoSessao.salaId, inicio:editandoSessao.inicio }); setEditandoSessao(null); await carregar(); msg('Sessão atualizada!') } catch(e) { err(e) }
  }

  async function removerGrade(id: string) {
    if (!confirm('Remover esta grade e todas as suas sessões?')) return
    try { await api.delete(`/grades/${id}`); if(editandoGrade?.id===id) setEditandoGrade(null); if(editandoSessao?.gradeId===id) setEditandoSessao(null); await carregar(); msg('Grade removida!') } catch(e) { err(e) }
  }

  async function removerSessao(gradeId: string, sessaoId: string) {
    if (!confirm('Remover esta sessão de todos os dias da grade?')) return
    try { await api.delete(`/grades/${gradeId}/sessoes/${sessaoId}`); if(editandoSessao?.sessaoId===sessaoId) setEditandoSessao(null); await carregar(); msg('Sessão removida!') } catch(e) { err(e) }
  }

  const hoje = new Date().toISOString().slice(0,10)
  const totalSessoes = grades.reduce((a,g) => a + g.sessoes.length, 0)

  return (
    <div style={{ fontFamily:'sans-serif', color:'#1a1a2e', paddingBottom:40 }}>

      {/* Header */}
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:24 }}>
        <div>
          <h1 style={{ margin:'0 0 6px', fontSize:26, fontWeight:700 }}>Grade de Exibição</h1>
          <p style={{ margin:0, color:'#6c757d', fontSize:14 }}>Sessões recorrentes diárias durante o período da grade</p>
        </div>
        <div style={{ display:'flex', gap:10 }}>
          <button onClick={() => { setFormSessao({gradeId:grades[0]?.id??'',filmeId:'',salaId:'',inicio:''}); setErro(''); setModal('SESSAO') }} disabled={grades.length===0} style={{ padding:'11px 18px', borderRadius:8, border:`1px solid ${COR}`, background:'white', color:COR, cursor:grades.length?'pointer':'not-allowed', fontWeight:600, fontSize:13 }}>+ Sessão</button>
          <button onClick={() => { setFormGrade({inicio:'',fim:''}); setErro(''); setModal('GRADE') }} style={{ backgroundColor:COR, color:'white', padding:'11px 18px', borderRadius:8, border:'none', cursor:'pointer', fontWeight:600, fontSize:13 }}>+ Nova Grade</button>
        </div>
      </div>

      {erro    && <div style={{ background:'#FFEBEE', border:'1px solid #FFCDD2', borderRadius:8, padding:'12px 16px', marginBottom:16, color:'#C62828', fontSize:14 }}>{erro}</div>}
      {sucesso && <div style={{ background:'#E8F5E9', border:'1px solid #C8E6C9', borderRadius:8, padding:'12px 16px', marginBottom:16, color:'#2E7D32', fontSize:14 }}>{sucesso}</div>}

      {/* Stats */}
      <div style={{ display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:16, marginBottom:28 }}>
        {[{ label:'Total de Grades', val:grades.length },
          { label:'Sessões Diárias', val:totalSessoes },
          { label:'Grades Ativas', val:grades.filter(g=>g.inicio<=hoje&&g.fim>=hoje).length }].map(s => (
          <div key={s.label} style={{ background:'white', borderRadius:12, border:'1px solid #EBEBEB', padding:'18px 20px', boxShadow:'0 1px 3px rgba(0,0,0,0.06)' }}>
            <div style={{ fontSize:12, color:'#6c757d', marginBottom:6, textTransform:'uppercase', letterSpacing:.5 }}>{s.label}</div>
            <div style={{ fontSize:26, fontWeight:700 }}>{s.val}</div>
          </div>
        ))}
      </div>

      {/* Grades */}
      {grades.length === 0 && (
        <div style={{ textAlign:'center', padding:'60px 20px', color:'#6c757d', background:'white', borderRadius:12, border:'1px solid #EBEBEB' }}>
          <div style={{ fontSize:48, marginBottom:12, opacity:.3 }}>&#128197;</div>
          <p style={{ margin:0, fontSize:16 }}>Nenhuma grade cadastrada.</p>
        </div>
      )}

      <div style={{ display:'flex', flexDirection:'column', gap:20 }}>
        {grades.map(g => {
          const status = diasRestantes(g.fim)
          const ativa  = g.inicio <= hoje && g.fim >= hoje
          return (
            <div key={g.id} style={{ background:'white', borderRadius:12, border:`1px solid ${editandoGrade?.id===g.id?COR:'#EBEBEB'}`, boxShadow:'0 2px 8px rgba(0,0,0,0.07)', overflow:'hidden' }}>

              {/* Cabeçalho da grade */}
              <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', padding:'18px 24px', borderBottom:'1px solid #F0F0F0', background: ativa ? '#FFF8F8':'white' }}>
                <div style={{ display:'flex', alignItems:'center', gap:16 }}>
                  <div style={{ width:4, height:40, borderRadius:4, background: ativa?COR:'#DDD' }} />
                  <div>
                    <div style={{ display:'flex', alignItems:'center', gap:10, marginBottom:4 }}>
                      <span style={{ fontWeight:700, fontSize:16 }}>{g.inicio} &rarr; {g.fim}</span>
                      {ativa && <span style={{ background:'#FFEBEE', color:COR, padding:'2px 8px', borderRadius:20, fontSize:11, fontWeight:700 }}>EM CARTAZ</span>}
                    </div>
                    <div style={{ fontSize:13, color:'#6c757d' }}>{g.sessoes.length} sessão(ões) diária(s) &bull; <span style={{ color:status.cor, fontWeight:600 }}>Encerra em {status.txt}</span></div>
                  </div>
                </div>
                <div style={{ display:'flex', gap:8 }}>
                  <button onClick={() => { setEditandoGrade({id:g.id,inicio:g.inicio,fim:g.fim}); setEditandoSessao(null); setErro('') }} style={{ padding:'7px 14px', borderRadius:6, border:'1px solid #DDD', background:'white', cursor:'pointer', fontSize:12, fontWeight:600, color:'#1565C0' }}>Editar período</button>
                  <button onClick={() => removerGrade(g.id)} style={{ padding:'7px 14px', borderRadius:6, border:'none', background:'#FFEBEE', cursor:'pointer', fontSize:12, fontWeight:600, color:'#C62828' }}>Remover</button>
                </div>
              </div>

              {/* Form edição de período */}
              {editandoGrade?.id === g.id && (
                <form onSubmit={salvarEdicaoGrade} style={{ display:'flex', gap:16, alignItems:'flex-end', flexWrap:'wrap', padding:'16px 24px', background:'#F8F9FF', borderBottom:'1px solid #E8EAFF' }}>
                  <div>
                    <label style={{ ...lbl, color:'#1565C0' }}>Novo início</label>
                    <input type="date" style={{ ...inp, width:160, borderColor:'#1565C0' }} required value={editandoGrade.inicio} onChange={e => setEditandoGrade({...editandoGrade,inicio:e.target.value})} />
                  </div>
                  <div>
                    <label style={{ ...lbl, color:'#1565C0' }}>Novo fim</label>
                    <input type="date" style={{ ...inp, width:160, borderColor:'#1565C0' }} required value={editandoGrade.fim} min={editandoGrade.inicio} onChange={e => setEditandoGrade({...editandoGrade,fim:e.target.value})} />
                  </div>
                  <button type="submit" style={{ padding:'10px 20px', borderRadius:8, border:'none', background:'#1565C0', color:'white', cursor:'pointer', fontWeight:600 }}>Salvar</button>
                  <button type="button" onClick={() => setEditandoGrade(null)} style={{ padding:'10px 16px', borderRadius:8, border:'1px solid #DDD', background:'white', cursor:'pointer', fontWeight:600 }}>Cancelar</button>
                </form>
              )}

              {/* Tabela de sessões */}
              {g.sessoes.length === 0 ? (
                <div style={{ padding:'24px', textAlign:'center', color:'#aaa', fontSize:14 }}>Nenhuma sessão cadastrada nesta grade.</div>
              ) : (
                <div style={{ overflowX:'auto' }}>
                  <table style={{ width:'100%', borderCollapse:'collapse' }}>
                    <thead>
                      <tr style={{ background:'#FAFAFA' }}>
                        {['Filme','Sala','Início','Encerramento da sala','Ações'].map(h => (
                          <th key={h} style={{ padding:'12px 20px', textAlign:'left', fontSize:12, fontWeight:600, color:'#6c757d', textTransform:'uppercase', letterSpacing:.5, borderBottom:'1px solid #F0F0F0' }}>{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {g.sessoes.map(s => (
                        <React.Fragment key={s.id}>
                          <tr style={{ borderBottom:'1px solid #F5F5F5', background:editandoSessao?.sessaoId===s.id?'#F8F9FF':'white', transition:'background .15s' }}>
                            <td style={{ padding:'14px 20px', fontWeight:600, fontSize:14 }}>{s.filmeTitulo}</td>
                            <td style={{ padding:'14px 20px' }}>
                              <span style={{ background:'#E3F2FD', color:'#1565C0', padding:'3px 10px', borderRadius:20, fontSize:12, fontWeight:600 }}>Sala {s.salaNumero}</span>
                            </td>
                            <td style={{ padding:'14px 20px', fontWeight:700, fontSize:15, color:COR }}>{s.inicio}</td>
                            <td style={{ padding:'14px 20px', color:'#888', fontSize:14 }}>{s.fim}</td>
                            <td style={{ padding:'14px 20px' }}>
                              <div style={{ display:'flex', gap:6 }}>
                                <button onClick={() => { setEditandoSessao({gradeId:g.id,sessaoId:s.id,filmeId:s.filmeId,salaId:s.salaId,inicio:s.inicio}); setEditandoGrade(null) }} style={{ padding:'5px 12px', borderRadius:6, border:'1px solid #DDD', background:'white', cursor:'pointer', fontSize:12, fontWeight:600, color:'#1565C0' }}>Editar</button>
                                <button onClick={() => removerSessao(g.id,s.id)} style={{ padding:'5px 10px', borderRadius:6, border:'none', background:'#FFEBEE', cursor:'pointer', fontSize:12, fontWeight:600, color:'#C62828' }}>&#128465;</button>
                              </div>
                            </td>
                          </tr>
                          {editandoSessao?.sessaoId === s.id && (
                            <tr key={s.id+'-edit'} style={{ background:'#F8F9FF' }}>
                              <td colSpan={5} style={{ padding:'16px 20px', borderBottom:'1px solid #E8EAFF' }}>
                                <form onSubmit={salvarEdicaoSessao} style={{ display:'flex', gap:14, flexWrap:'wrap', alignItems:'flex-end' }}>
                                  <div>
                                    <label style={{ ...lbl, color:'#1565C0' }}>Filme</label>
                                    <select style={{ ...inp, width:220 }} value={editandoSessao.filmeId} onChange={e => setEditandoSessao({...editandoSessao,filmeId:e.target.value})}>
                                      {filmes.map(f => <option key={f.id} value={f.id}>{f.titulo}</option>)}
                                    </select>
                                  </div>
                                  <div>
                                    <label style={{ ...lbl, color:'#1565C0' }}>Sala</label>
                                    <select style={{ ...inp, width:160 }} value={editandoSessao.salaId} onChange={e => setEditandoSessao({...editandoSessao,salaId:e.target.value})}>
                                      {salas.map(sl => <option key={sl.id} value={sl.id}>Sala {sl.numero} ({sl.tipo})</option>)}
                                    </select>
                                  </div>
                                  <div>
                                    <label style={{ ...lbl, color:'#1565C0' }}>Horário</label>
                                    <input type="time" required style={{ ...inp, width:130, borderColor:'#1565C0' }} value={editandoSessao.inicio} onChange={e => setEditandoSessao({...editandoSessao,inicio:e.target.value})} />
                                  </div>
                                  <button type="submit" style={{ padding:'10px 20px', borderRadius:8, border:'none', background:'#1565C0', color:'white', cursor:'pointer', fontWeight:600 }}>Salvar</button>
                                  <button type="button" onClick={() => setEditandoSessao(null)} style={{ padding:'10px 16px', borderRadius:8, border:'1px solid #DDD', background:'white', cursor:'pointer', fontWeight:600 }}>Cancelar</button>
                                </form>
                              </td>
                            </tr>
                          )}
                        </React.Fragment>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )
        })}
      </div>

      {/* Modal Nova Grade */}
      {modal === 'GRADE' && (
        <div style={{ position:'fixed', inset:0, background:'rgba(0,0,0,0.5)', display:'flex', justifyContent:'center', alignItems:'center', zIndex:1000 }}>
          <div style={{ background:'white', borderRadius:12, width:'100%', maxWidth:420 }}>
            <div style={{ padding:'24px 28px', borderBottom:'1px solid #EBEBEB', display:'flex', justifyContent:'space-between', alignItems:'center' }}>
              <h2 style={{ margin:0, fontSize:20 }}>Nova Grade</h2>
              <button onClick={() => setModal(null)} style={{ background:'none', border:'none', fontSize:22, color:'#888', cursor:'pointer' }}>&#215;</button>
            </div>
            <form id="formGrade" onSubmit={criarGrade} style={{ padding:'24px 28px' }}>
              {erro && <div style={{ background:'#FFEBEE', borderRadius:8, padding:'10px 14px', marginBottom:16, color:'#C62828', fontSize:13 }}>{erro}</div>}
              <div style={{ marginBottom:16 }}>
                <label style={lbl}>Data de início *</label>
                <input type="date" style={inp} required value={formGrade.inicio} min={hoje} onChange={e => setFormGrade({...formGrade,inicio:e.target.value})} />
              </div>
              <div>
                <label style={lbl}>Data de fim *</label>
                <input type="date" style={inp} required value={formGrade.fim} min={formGrade.inicio||hoje} onChange={e => setFormGrade({...formGrade,fim:e.target.value})} />
              </div>
              <p style={{ fontSize:12, color:'#888', marginTop:12 }}>As sessões adicionadas nesta grade vão repetir diariamente durante todo o período.</p>
            </form>
            <div style={{ padding:'0 28px 24px', display:'flex', gap:12 }}>
              <button onClick={() => setModal(null)} style={{ flex:1, padding:12, background:'white', border:'1px solid #DDD', borderRadius:8, cursor:'pointer', fontWeight:600 }}>Cancelar</button>
              <button form="formGrade" type="submit" style={{ flex:1, padding:12, background:COR, color:'white', border:'none', borderRadius:8, cursor:'pointer', fontWeight:600 }}>Criar Grade</button>
            </div>
          </div>
        </div>
      )}

      {/* Modal Nova Sessão */}
      {modal === 'SESSAO' && (
        <div style={{ position:'fixed', inset:0, background:'rgba(0,0,0,0.5)', display:'flex', justifyContent:'center', alignItems:'center', zIndex:1000 }}>
          <div style={{ background:'white', borderRadius:12, width:'100%', maxWidth:480 }}>
            <div style={{ padding:'24px 28px', borderBottom:'1px solid #EBEBEB', display:'flex', justifyContent:'space-between', alignItems:'center' }}>
              <h2 style={{ margin:0, fontSize:20 }}>Adicionar Sessão Diária</h2>
              <button onClick={() => setModal(null)} style={{ background:'none', border:'none', fontSize:22, color:'#888', cursor:'pointer' }}>&#215;</button>
            </div>
            <form id="formSessao" onSubmit={adicionarSessao} style={{ padding:'24px 28px' }}>
              {erro && <div style={{ background:'#FFEBEE', borderRadius:8, padding:'10px 14px', marginBottom:16, color:'#C62828', fontSize:13 }}>{erro}</div>}
              <div style={{ marginBottom:16 }}>
                <label style={lbl}>Grade *</label>
                <select style={inp} required value={formSessao.gradeId} onChange={e => setFormSessao({...formSessao,gradeId:e.target.value})}>
                  <option value="">Selecione uma grade...</option>
                  {grades.map(g => <option key={g.id} value={g.id}>{g.inicio} &rarr; {g.fim}</option>)}
                </select>
              </div>
              <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:16, marginBottom:16 }}>
                <div>
                  <label style={lbl}>Filme *</label>
                  <select style={inp} required value={formSessao.filmeId} onChange={e => setFormSessao({...formSessao,filmeId:e.target.value})}>
                    <option value="">Selecione...</option>
                    {filmes.map(f => <option key={f.id} value={f.id}>{f.titulo}</option>)}
                  </select>
                </div>
                <div>
                  <label style={lbl}>Sala *</label>
                  <select style={inp} required value={formSessao.salaId} onChange={e => setFormSessao({...formSessao,salaId:e.target.value})}>
                    <option value="">Selecione...</option>
                    {salas.map(s => <option key={s.id} value={s.id}>Sala {s.numero} ({s.tipo})</option>)}
                  </select>
                </div>
              </div>
              <div>
                <label style={lbl}>Horário diário *</label>
                <input type="time" style={inp} required value={formSessao.inicio} onChange={e => setFormSessao({...formSessao,inicio:e.target.value})} />
                <p style={{ fontSize:12, color:'#888', marginTop:6 }}>Esta sessão vai ocorrer todos os dias da grade neste horário.</p>
              </div>
            </form>
            <div style={{ padding:'0 28px 24px', display:'flex', gap:12 }}>
              <button onClick={() => setModal(null)} style={{ flex:1, padding:12, background:'white', border:'1px solid #DDD', borderRadius:8, cursor:'pointer', fontWeight:600 }}>Cancelar</button>
              <button form="formSessao" type="submit" style={{ flex:1, padding:12, background:COR, color:'white', border:'none', borderRadius:8, cursor:'pointer', fontWeight:600 }}>Adicionar Sessão</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
