import { useEffect, useState } from 'react'
import api from '../api/client'

interface Sessao {
  id: string
  filmeId: string
  filmeTitulo: string
  salaId: string
  salaNumero: number
  inicio: string  // "HH:mm"
  fim: string     // "HH:mm"
}

interface Grade {
  id: string
  inicio: string   // "YYYY-MM-DD"
  fim: string
  sessoes: Sessao[]
}

interface Filme { id: string; titulo: string; ativo: boolean }
interface Sala  { id: string; numero: number; capacidade: number; tipo: string }

export default function GradePage() {
  const [grades, setGrades]   = useState<Grade[]>([])
  const [filmes, setFilmes]   = useState<Filme[]>([])
  const [salas,  setSalas]    = useState<Sala[]>([])
  const [erro,   setErro]     = useState('')
  const [sucesso, setSucesso] = useState('')

  const [novaGrade, setNovaGrade] = useState({ inicio: '', fim: '' })
  const [novaSessao, setNovaSessao] = useState({ gradeId: '', filmeId: '', salaId: '', inicio: '' })

  async function carregar() {
    const [g, f, s] = await Promise.all([
      api.get<Grade[]>('/grades'),
      api.get<Filme[]>('/filmes'),
      api.get<Sala[]>('/salas'),
    ])
    setGrades(g.data)
    setFilmes(f.data.filter(f => f.ativo))
    setSalas(s.data)
  }

  useEffect(() => { carregar() }, [])

  function msg(ok: string) { setSucesso(ok); setErro(''); setTimeout(() => setSucesso(''), 3000) }
  function err(e: unknown) {
    const ax = e as { response?: { data?: { mensagem?: string; message?: string }; status?: number } }
    setErro(ax.response?.data?.mensagem ?? ax.response?.data?.message ?? `Erro ${ax.response?.status ?? ''}`)
    setSucesso('')
  }

  async function criarGrade(e: React.FormEvent) {
    e.preventDefault()
    try {
      await api.post('/grades', novaGrade)
      setNovaGrade({ inicio: '', fim: '' })
      await carregar()
      msg('Grade criada!')
    } catch (e) { err(e) }
  }

  async function adicionarSessao(e: React.FormEvent) {
    e.preventDefault()
    try {
      await api.post(`/grades/${novaSessao.gradeId}/sessoes`, {
        filmeId: novaSessao.filmeId,
        salaId:  novaSessao.salaId,
        inicio:  novaSessao.inicio,   // "HH:mm" — horário diário recorrente
      })
      setNovaSessao({ gradeId: novaSessao.gradeId, filmeId: '', salaId: '', inicio: '' })
      await carregar()
      msg('Sessão adicionada! Ela vai repetir diariamente durante a grade.')
    } catch (e) { err(e) }
  }

  async function removerGrade(gradeId: string) {
    if (!confirm('Remover esta grade e todas as suas sessões?')) return
    try {
      await api.delete(`/grades/${gradeId}`)
      await carregar()
      msg('Grade removida!')
    } catch (e) { err(e) }
  }

  async function removerSessao(gradeId: string, sessaoId: string) {
    if (!confirm('Remover esta sessão de todos os dias da grade?')) return
    try {
      await api.delete(`/grades/${gradeId}/sessoes/${sessaoId}`)
      await carregar()
      msg('Sessão removida!')
    } catch (e) { err(e) }
  }

  function fmtHora(t: string) {
    return t ?? ''
  }

  return (
    <div>
      <h2>Grade de Exibição</h2>
      <p style={{ color: '#555', marginTop: 0 }}>
        Cada sessão cadastrada repete <strong>diariamente</strong> durante o período da grade.
      </p>

      {erro && (
        <div style={{
          display: 'flex', alignItems: 'flex-start', gap: '10px',
          background: '#fffbeb', border: '1px solid #f59e0b', borderRadius: '8px',
          padding: '12px 16px', marginBottom: '1rem', color: '#92400e',
        }}>
          <span style={{ fontSize: '18px', flexShrink: 0 }}>⚠️</span>
          <div>
            <strong style={{ fontSize: '14px' }}>Não foi possível adicionar a sessão</strong>
            <p style={{ margin: '2px 0 0', fontSize: '13px' }}>{erro}</p>
          </div>
          <button onClick={() => setErro('')} style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer', color: '#92400e', fontSize: '16px', flexShrink: 0 }}>✕</button>
        </div>
      )}
      {sucesso && <p style={{ color: 'green', border: '1px solid green', padding: '0.5rem', borderRadius: '6px' }}>{sucesso}</p>}

      <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap', marginBottom: '2rem' }}>

        {/* Criar grade */}
        <form onSubmit={criarGrade} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', minWidth: 220 }}>
          <h3>Nova Grade</h3>
          <label>Início da grade
            <input type="date" required value={novaGrade.inicio}
              min={new Date().toISOString().slice(0, 10)}
              onChange={e => setNovaGrade({ ...novaGrade, inicio: e.target.value })} />
          </label>
          <label>Fim da grade
            <input type="date" required value={novaGrade.fim}
              min={novaGrade.inicio || new Date().toISOString().slice(0, 10)}
              onChange={e => setNovaGrade({ ...novaGrade, fim: e.target.value })} />
          </label>
          <button type="submit">Criar Grade</button>
        </form>

        {/* Adicionar sessão */}
        <form onSubmit={adicionarSessao} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', minWidth: 260 }}>
          <h3>Adicionar Sessão Diária</h3>
          <label>Grade
            <select required value={novaSessao.gradeId}
              onChange={e => setNovaSessao({ ...novaSessao, gradeId: e.target.value })}>
              <option value="">Selecione...</option>
              {grades.map(g => (
                <option key={g.id} value={g.id}>{g.inicio} → {g.fim}</option>
              ))}
            </select>
          </label>
          <label>Filme
            <select required value={novaSessao.filmeId}
              onChange={e => setNovaSessao({ ...novaSessao, filmeId: e.target.value })}>
              <option value="">Selecione...</option>
              {filmes.map(f => <option key={f.id} value={f.id}>{f.titulo}</option>)}
            </select>
          </label>
          <label>Sala
            <select required value={novaSessao.salaId}
              onChange={e => setNovaSessao({ ...novaSessao, salaId: e.target.value })}>
              <option value="">Selecione...</option>
              {salas.map(s => <option key={s.id} value={s.id}>Sala {s.numero} ({s.tipo})</option>)}
            </select>
          </label>
          <label>Horário diário
            <input type="time" required value={novaSessao.inicio}
              onChange={e => setNovaSessao({ ...novaSessao, inicio: e.target.value })} />
          </label>
          <button type="submit">Adicionar Sessão</button>
        </form>

      </div>

      {/* Lista de grades */}
      {grades.length === 0 && <p>Nenhuma grade cadastrada.</p>}
      {grades.map(g => (
        <details key={g.id} style={{ marginBottom: '1rem', border: '1px solid #ccc', padding: '0.75rem' }} open>
          <summary style={{ cursor: 'pointer', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <span>Grade: {g.inicio} → {g.fim} &nbsp;|&nbsp; {g.sessoes.length} sessão(ões) diária(s)</span>
            <button
              onClick={e => { e.preventDefault(); removerGrade(g.id) }}
              style={{ color: 'red', fontWeight: 'normal', fontSize: '0.85rem' }}
            >Remover grade</button>
          </summary>

          {g.sessoes.length === 0
            ? <p style={{ marginTop: '0.5rem' }}>Sem sessões nesta grade.</p>
            : (
              <table border={1} cellPadding={5} style={{ borderCollapse: 'collapse', width: '100%', marginTop: '0.75rem' }}>
                <thead>
                  <tr><th>Filme</th><th>Sala</th><th>Início (diário)</th><th>Sala livre às</th><th>Ação</th></tr>
                </thead>
                <tbody>
                  {g.sessoes.map(s => (
                    <tr key={s.id}>
                      <td>{s.filmeTitulo}</td>
                      <td>Sala {s.salaNumero}</td>
                      <td>{fmtHora(s.inicio)}</td>
                      <td>{fmtHora(s.fim)}</td>
                      <td>
                        <button onClick={() => removerSessao(g.id, s.id)} style={{ color: 'red' }}>Remover</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )
          }
        </details>
      ))}
    </div>
  )
}
