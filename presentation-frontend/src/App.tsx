import { BrowserRouter, Routes, Route, NavLink, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import CatalogoPage from './pages/CatalogoPage'
import GradePage from './pages/GradePage'
import SalasPage from './pages/SalasPage'
import BombonierePage from './pages/BombonierePage'
import CompraPage from './pages/CompraPage'
import ProgramacaoPage from './pages/ProgramacaoPage'
import AuthModal, { type ClienteLogado } from './components/AuthModal'

type Modo = 'backoffice' | 'portal'

function Home({ modo, cliente }: { modo: Modo; cliente: ClienteLogado | null }) {
  if (modo === 'backoffice') {
    return <h2>Bem-vindo ao F.R.A.M.E — Backoffice</h2>
  }
  return (
    <div style={{ padding: '24px 0' }}>
      <h2 style={{ marginBottom: 8, color: '#1a1a2e' }}>
        {cliente ? `Olá, ${cliente.nome.split(' ')[0]}!` : 'Bem-vindo ao Portal do Cliente'}
      </h2>
      <p style={{ color: '#888', marginBottom: 24 }}>
        {cliente
          ? 'Explore a programação e compre seus ingressos.'
          : 'Faça login para ver recomendações personalizadas e comprar ingressos.'}
      </p>
      <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
        <QuickCard title='Programação' desc='Veja os filmes em cartaz hoje' to='/programacao' />
        <QuickCard title='Comprar Ingresso' desc='Reserve seu assento e finalize a compra' to='/compra' />
        {cliente && <QuickCard title='Fidelidade' desc='Consulte seus pontos e benefícios' to='/fidelidade' />}
      </div>
    </div>
  )
}

function QuickCard({ title, desc, to }: { title: string; desc: string; to: string }) {
  return (
    <NavLink to={to} style={{ textDecoration: 'none' }}>
      <div style={{ background: 'white', border: '1px solid #e0e0e0', borderRadius: 10,
        padding: '16px 20px', minWidth: 200, cursor: 'pointer' }}>
        <div style={{ fontWeight: 600, marginBottom: 4, color: '#1a1a2e' }}>{title}</div>
        <div style={{ fontSize: 13, color: '#888' }}>{desc}</div>
      </div>
    </NavLink>
  )
}

function EmConstrucao({ nome }: { nome: string }) {
  return (
    <div style={{ padding: '40px', textAlign: 'center', color: '#888' }}>
      <div style={{ fontSize: '48px', marginBottom: '16px' }}>🚧</div>
      <h2 style={{ color: '#555' }}>{nome}</h2>
      <p>Esta página ainda está sendo implementada.</p>
    </div>
  )
}

const navStyle = (isActive: boolean, cor: string): React.CSSProperties => ({
  display: 'flex', alignItems: 'center', gap: '10px',
  padding: '9px 12px', borderRadius: '8px', fontSize: '14px',
  textDecoration: 'none', color: isActive ? 'white' : '#555',
  background: isActive ? cor : 'transparent', fontWeight: isActive ? 500 : 400,
})

function Sidebar({ modo, onToggle, cliente, onOpenAuth, onLogout }:
  { modo: Modo; onToggle: () => void; cliente: ClienteLogado | null;
    onOpenAuth: () => void; onLogout: () => void }) {
  const cor = modo === 'backoffice' ? '#8B0000' : '#1565C0'
  const navigate = useNavigate()

  return (
    <aside style={{ width: '220px', borderRight: '0.5px solid #e0e0e0', display: 'flex',
      flexDirection: 'column', backgroundColor: 'white', flexShrink: 0 }}>

      {/* Logo */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px',
        padding: '20px 16px 16px', borderBottom: '0.5px solid #e0e0e0' }}>
        <div style={{ width: 36, height: 36, background: cor, borderRadius: '8px',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: 'white', fontSize: '18px' }}>▶</div>
        <div>
          <div style={{ fontSize: '15px', fontWeight: 500 }}>F.R.A.M.E</div>
          <div style={{ fontSize: '12px', color: '#888' }}>
            {modo === 'backoffice' ? 'Backoffice' : 'Portal do Cliente'}
          </div>
        </div>
      </div>

      {/* Toggle */}
      <div style={{ padding: '10px 8px', borderBottom: '0.5px solid #e0e0e0' }}>
        <button onClick={() => { onToggle(); navigate('/') }}
          style={{ width: '100%', padding: '8px', borderRadius: '8px',
            border: `1px solid ${cor}`, background: 'white', color: cor,
            fontSize: '12px', fontWeight: 600, cursor: 'pointer' }}>
          {modo === 'backoffice' ? '→ Portal do Cliente' : '→ Backoffice'}
        </button>
      </div>

      {/* Links */}
      <nav style={{ flex: 1, padding: '12px 8px', display: 'flex', flexDirection: 'column', gap: '2px' }}>
        {modo === 'backoffice' ? (
          <>
            <NavLink to='/' end style={({ isActive }) => navStyle(isActive, cor)}>Dashboard</NavLink>
            <NavLink to='/filmes' style={({ isActive }) => navStyle(isActive, cor)}>Catálogo de Filmes</NavLink>
            <NavLink to='/salas' style={({ isActive }) => navStyle(isActive, cor)}>Salas</NavLink>
            <NavLink to='/grade' style={({ isActive }) => navStyle(isActive, cor)}>Grade de Exibição</NavLink>
            <NavLink to='/bomboniere' style={({ isActive }) => navStyle(isActive, cor)}>Bomboniere</NavLink>
            <NavLink to='/checkin' style={({ isActive }) => navStyle(isActive, cor)}>Check-in</NavLink>
            <NavLink to='/caixa' style={({ isActive }) => navStyle(isActive, cor)}>Fechamento de Caixa</NavLink>
          </>
        ) : (
          <>
            <NavLink to='/' end style={({ isActive }) => navStyle(isActive, cor)}>Início</NavLink>
            <NavLink to='/programacao' style={({ isActive }) => navStyle(isActive, cor)}>Programação</NavLink>
            <NavLink to='/compra' style={({ isActive }) => navStyle(isActive, cor)}>Comprar Ingresso</NavLink>
            <NavLink to='/fidelidade' style={({ isActive }) => navStyle(isActive, cor)}>Fidelidade</NavLink>
          </>
        )}
      </nav>

      {/* Área do cliente (só no portal) */}
      {modo === 'portal' && (
        <div style={{ padding: '12px 8px', borderTop: '0.5px solid #e0e0e0' }}>
          {cliente ? (
            <div>
              <div style={{ fontSize: 12, color: '#888', marginBottom: 4, paddingLeft: 4 }}>
                Logado como
              </div>
              <div style={{ fontSize: 13, fontWeight: 500, color: '#1a1a2e',
                paddingLeft: 4, marginBottom: 8, wordBreak: 'break-word' }}>
                {cliente.nome}
              </div>
              <button onClick={onLogout}
                style={{ width: '100%', padding: '7px', borderRadius: '8px',
                  border: '1px solid #ddd', background: 'white', color: '#888',
                  fontSize: 12, cursor: 'pointer' }}>
                Sair
              </button>
            </div>
          ) : (
            <button onClick={onOpenAuth}
              style={{ width: '100%', padding: '9px', borderRadius: '8px',
                border: `1px solid ${cor}`, background: cor, color: 'white',
                fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
              Entrar / Cadastrar
            </button>
          )}
        </div>
      )}

      {modo === 'backoffice' && (
        <div style={{ padding: '12px 8px', borderTop: '0.5px solid #e0e0e0' }}>
          <a href='#' style={navStyle(false, '#8B0000')}>Sair</a>
        </div>
      )}
    </aside>
  )
}

export default function App() {
  const [modo, setModo] = useState<Modo>('backoffice')
  const [showAuth, setShowAuth] = useState(false)
  const [cliente, setCliente] = useState<ClienteLogado | null>(() => {
    try {
      const s = localStorage.getItem('cliente')
      return s ? JSON.parse(s) : null
    } catch { return null }
  })

  const handleLogin = (c: ClienteLogado) => {
    setCliente(c)
    setShowAuth(false)
  }

  const handleLogout = () => {
    localStorage.removeItem('cliente')
    setCliente(null)
  }

  return (
    <BrowserRouter>
      <div style={{ display: 'flex', minHeight: '100vh', fontFamily: 'sans-serif' }}>
        <Sidebar
          modo={modo}
          onToggle={() => setModo(m => m === 'backoffice' ? 'portal' : 'backoffice')}
          cliente={cliente}
          onOpenAuth={() => setShowAuth(true)}
          onLogout={handleLogout}
        />
        <main style={{ flex: 1, padding: '1.5rem', backgroundColor: '#FAFAFA' }}>
          <Routes>
            <Route path='/' element={<Home modo={modo} cliente={cliente} />} />
            {/* Backoffice */}
            <Route path='/filmes' element={<CatalogoPage />} />
            <Route path='/salas' element={<SalasPage />} />
            <Route path='/grade' element={<GradePage />} />
            <Route path='/bomboniere' element={<BombonierePage />} />
            <Route path='/checkin' element={<EmConstrucao nome='Check-in' />} />
            <Route path='/caixa' element={<EmConstrucao nome='Fechamento de Caixa' />} />
            {/* Portal */}
            <Route path='/programacao' element={<ProgramacaoPage cliente={cliente} />} />
            <Route path='/compra' element={<CompraPage />} />
            <Route path='/fidelidade' element={<EmConstrucao nome='Fidelidade' />} />
          </Routes>
        </main>
      </div>
      {showAuth && <AuthModal onLogin={handleLogin} onClose={() => setShowAuth(false)} />}
    </BrowserRouter>
  )
}
