import { useState } from 'react'

export type ClienteLogado = { clienteId: string; nome: string; email: string }

interface Props {
  onLogin: (c: ClienteLogado) => void
  onClose: () => void
}

export default function AuthModal({ onLogin, onClose }: Props) {
  const [aba, setAba] = useState<'login' | 'cadastro'>('login')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [nome, setNome] = useState('')
  const [dataNascimento, setDataNascimento] = useState('')
  const [erro, setErro] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async () => {
    setErro('')
    setLoading(true)
    try {
      const url = aba === 'login' ? '/api/auth/login' : '/api/auth/cadastro'
      const body = aba === 'login'
        ? { email, senha }
        : { nome, email, senha, dataNascimento }

      const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })

      if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        setErro(data.message || data.mensagem || (aba === 'login' ? 'Email ou senha inválidos.' : 'Erro ao cadastrar.'))
        return
      }

      const data: ClienteLogado = await res.json()
      localStorage.setItem('cliente', JSON.stringify(data))
      onLogin(data)
    } catch {
      setErro('Erro de conexão com o servidor.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={overlay} onClick={onClose}>
      <div style={modal} onClick={e => e.stopPropagation()}>
        {/* Abas */}
        <div style={{ display: 'flex', borderBottom: '1px solid #eee', marginBottom: 20 }}>
          {(['login', 'cadastro'] as const).map(a => (
            <button key={a} onClick={() => { setAba(a); setErro('') }}
              style={{ flex: 1, padding: '10px 0', border: 'none', background: 'none', cursor: 'pointer',
                fontWeight: aba === a ? 600 : 400, color: aba === a ? '#1565C0' : '#888',
                borderBottom: aba === a ? '2px solid #1565C0' : '2px solid transparent', fontSize: 14 }}>
              {a === 'login' ? 'Entrar' : 'Cadastrar'}
            </button>
          ))}
        </div>

        {/* Campos */}
        {aba === 'cadastro' && (
          <>
            <label style={label}>Nome completo</label>
            <input style={input} value={nome} onChange={e => setNome(e.target.value)}
              placeholder='Seu nome' autoFocus />
          </>
        )}

        <label style={label}>Email</label>
        <input style={input} type='email' value={email} onChange={e => setEmail(e.target.value)}
          placeholder='seu@email.com' autoFocus={aba === 'login'} />

        <label style={label}>Senha</label>
        <input style={input} type='password' value={senha} onChange={e => setSenha(e.target.value)}
          placeholder='••••••••' onKeyDown={e => e.key === 'Enter' && submit()} />

        {aba === 'cadastro' && (
          <>
            <label style={label}>Data de nascimento</label>
            <input style={input} type='date' value={dataNascimento}
              onChange={e => setDataNascimento(e.target.value)} />
          </>
        )}

        {erro && <p style={{ color: '#c62828', fontSize: 13, marginBottom: 12 }}>{erro}</p>}

        <button onClick={submit} disabled={loading}
          style={{ width: '100%', padding: '10px 0', background: '#1565C0', color: 'white',
            border: 'none', borderRadius: 8, fontWeight: 600, fontSize: 14, cursor: 'pointer',
            opacity: loading ? 0.7 : 1 }}>
          {loading ? 'Aguarde...' : (aba === 'login' ? 'Entrar' : 'Criar conta')}
        </button>
      </div>
    </div>
  )
}

const overlay: React.CSSProperties = {
  position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)',
  display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000,
}
const modal: React.CSSProperties = {
  background: 'white', borderRadius: 12, padding: 28, width: 360,
  boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
}
const label: React.CSSProperties = { display: 'block', fontSize: 12, color: '#555', marginBottom: 4 }
const input: React.CSSProperties = {
  width: '100%', padding: '9px 12px', borderRadius: 8, border: '1px solid #ddd',
  fontSize: 14, marginBottom: 14, boxSizing: 'border-box',
}
