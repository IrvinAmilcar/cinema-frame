import { useEffect, useState } from 'react'
import api from '../api/client'

interface Sala {
  id: string
  numero: number
  capacidade: number
  tipo: string
}

const TIPOS = ['COMUM', 'IMAX', 'TRES_D', 'VIP']

const formInicial = { numero: '', capacidade: '', tipo: 'COMUM' }

export default function SalasPage() {
  const [salas, setSalas] = useState<Sala[]>([])
  const [form, setForm] = useState(formInicial)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')

  async function carregar() {
    const res = await api.get<Sala[]>('/salas')
    setSalas(res.data)
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
      await api.post('/salas', {
        numero: Number(form.numero),
        capacidade: Number(form.capacidade),
        tipo: form.tipo,
      })
      setForm(formInicial)
      await carregar()
      msg('Sala cadastrada!')
    } catch (e) { err(e) }
  }

  async function remover(sala: Sala) {
    if (!confirm(`Remover Sala ${sala.numero}?`)) return
    try {
      await api.delete(`/salas/${sala.id}`)
      await carregar()
      msg('Sala removida!')
    } catch (e) { err(e) }
  }

  return (
    <div>
      <h2>Salas</h2>

      {erro    && <p style={{ color: 'red',   border: '1px solid red',   padding: '0.5rem' }}>{erro}</p>}
      {sucesso && <p style={{ color: 'green', border: '1px solid green', padding: '0.5rem' }}>{sucesso}</p>}

      <form onSubmit={cadastrar} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', maxWidth: 320, marginBottom: '2rem' }}>
        <h3>Nova Sala</h3>
        <input
          type="number" placeholder="Número da sala" required min={1}
          value={form.numero} onChange={e => setForm({ ...form, numero: e.target.value })}
        />
        <input
          type="number" placeholder="Capacidade (lugares)" required min={1}
          value={form.capacidade} onChange={e => setForm({ ...form, capacidade: e.target.value })}
        />
        <select value={form.tipo} onChange={e => setForm({ ...form, tipo: e.target.value })}>
          {TIPOS.map(t => <option key={t} value={t}>{t}</option>)}
        </select>
        <button type="submit">Cadastrar</button>
      </form>

      <table border={1} cellPadding={6} style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th>Número</th>
            <th>Capacidade</th>
            <th>Tipo</th>
            <th>Ação</th>
          </tr>
        </thead>
        <tbody>
          {salas.length === 0 && (
            <tr><td colSpan={4} style={{ textAlign: 'center' }}>Nenhuma sala cadastrada.</td></tr>
          )}
          {salas.map(s => (
            <tr key={s.id}>
              <td>Sala {s.numero}</td>
              <td>{s.capacidade} lugares</td>
              <td>{s.tipo}</td>
              <td>
                <button onClick={() => remover(s)} style={{ color: 'red' }}>Remover</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
