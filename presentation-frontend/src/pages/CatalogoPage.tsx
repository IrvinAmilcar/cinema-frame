import { useEffect, useState } from 'react'
import api from '../api/client'

interface Filme {
  id: string
  titulo: string
  duracaoMinutos: number
  classificacaoIndicativa: string
  genero: string
  trailerURL: string
  ativo: boolean
}

const CLASSIFICACOES = ['LIVRE', 'DEZ', 'DOZE', 'QUATORZE', 'DEZESSEIS', 'DEZOITO']
const GENEROS = ['ACAO', 'AVENTURA', 'COMEDIA', 'DRAMA', 'FICCAO_CIENTIFICA', 'TERROR', 'ROMANCE', 'ANIMACAO', 'DOCUMENTARIO']

const formInicial = { titulo: '', duracaoMinutos: 90, classificacaoIndicativa: 'LIVRE', genero: 'ACAO', trailerURL: '' }

export default function CatalogoPage() {
  const [filmes, setFilmes] = useState<Filme[]>([])
  const [form, setForm] = useState(formInicial)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')

  async function carregar() {
    const res = await api.get<Filme[]>('/filmes')
    setFilmes(res.data)
  }

  useEffect(() => { carregar() }, [])

  function msg(ok: string) { setSucesso(ok); setErro(''); setTimeout(() => setSucesso(''), 3000) }
  function err(e: unknown) {
    const ax = e as { response?: { data?: { message?: string }; status?: number } }
    setErro(ax.response?.data?.message ?? `Erro ${ax.response?.status ?? ''}`)
    setSucesso('')
  }

  async function cadastrar(e: React.FormEvent) {
    e.preventDefault()
    try {
      await api.post('/filmes', form)
      setForm(formInicial)
      await carregar()
      msg('Filme cadastrado!')
    } catch (e) { err(e) }
  }

  async function toggleAtivo(filme: Filme) {
    try {
      await api.patch(`/filmes/${filme.id}/${filme.ativo ? 'desativar' : 'ativar'}`)
      await carregar()
      msg(`Filme ${filme.ativo ? 'desativado' : 'ativado'}!`)
    } catch (e) { err(e) }
  }

  async function remover(filme: Filme) {
    if (!confirm(`Remover "${filme.titulo}"?`)) return
    try {
      await api.delete(`/filmes/${filme.id}`)
      await carregar()
      msg('Filme removido!')
    } catch (e) { err(e) }
  }

  return (
    <div>
      <h2>Catálogo de Filmes</h2>

      {erro && <p style={{ color: 'red', border: '1px solid red', padding: '0.5rem' }}>{erro}</p>}
      {sucesso && <p style={{ color: 'green', border: '1px solid green', padding: '0.5rem' }}>{sucesso}</p>}

      <form onSubmit={cadastrar} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', maxWidth: 400, marginBottom: '2rem' }}>
        <h3>Novo Filme</h3>
        <input placeholder="Título" required value={form.titulo} onChange={e => setForm({ ...form, titulo: e.target.value })} />
        <input type="number" placeholder="Duração (min)" required min={1} value={form.duracaoMinutos} onChange={e => setForm({ ...form, duracaoMinutos: Number(e.target.value) })} />
        <select value={form.classificacaoIndicativa} onChange={e => setForm({ ...form, classificacaoIndicativa: e.target.value })}>
          {CLASSIFICACOES.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
        <select value={form.genero} onChange={e => setForm({ ...form, genero: e.target.value })}>
          {GENEROS.map(g => <option key={g} value={g}>{g}</option>)}
        </select>
        <input placeholder="URL do trailer" value={form.trailerURL} onChange={e => setForm({ ...form, trailerURL: e.target.value })} />
        <button type="submit">Cadastrar</button>
      </form>

      <table border={1} cellPadding={6} style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th>Título</th>
            <th>Duração</th>
            <th>Classificação</th>
            <th>Gênero</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {filmes.length === 0 && (
            <tr><td colSpan={6} style={{ textAlign: 'center' }}>Nenhum filme cadastrado.</td></tr>
          )}
          {filmes.map(f => (
            <tr key={f.id} style={{ opacity: f.ativo ? 1 : 0.5 }}>
              <td>{f.titulo}</td>
              <td>{f.duracaoMinutos} min</td>
              <td>{f.classificacaoIndicativa}</td>
              <td>{f.genero}</td>
              <td>{f.ativo ? 'Ativo' : 'Inativo'}</td>
              <td style={{ display: 'flex', gap: '0.5rem' }}>
                <button onClick={() => toggleAtivo(f)}>{f.ativo ? 'Desativar' : 'Ativar'}</button>
                <button onClick={() => remover(f)} style={{ color: 'red' }}>Remover</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
