import { BrowserRouter, Routes, Route, NavLink, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import CatalogoPage from './pages/CatalogoPage'
import GradePage from './pages/GradePage'
import SalasPage from './pages/SalasPage'
import BombonierePage from './pages/BombonierePage'
import CompraPage from './pages/CompraPage'

type Modo = 'backoffice' | 'portal'

function Home({ modo }: { modo: Modo }) {
  return <h2>Bem-vindo ao F.R.A.M.E — {modo === 'backoffice' ? 'Backoffice' : 'Portal do Cliente'}</h2>
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
  padding: '9px 12px',
  borderRadius: '8px',
  fontSize: '14px',
  textDecoration: 'none',
  color: isActive ? 'white' : '#555',
  background: isActive ? cor : 'transparent',
  fontWeight: isActive ? 500 : 400,
})

function Sidebar({ modo, onToggle }: { modo: Modo; onToggle: () => void }) {
  const cor = modo === 'backoffice' ? '#8B0000' : '#1565C0'
  const navigate = useNavigate()

  const handleToggle = () => {
    onToggle()
    navigate('/')
  }

  return (
    <aside style={{ width: '220px', borderRight: '0.5px solid #e0e0e0', display: 'flex', flexDirection: 'column', backgroundColor: 'white', flexShrink: 0 }}>

      {/* Logo */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '20px 16px 16px', borderBottom: '0.5px solid #e0e0e0' }}>
        <div style={{ width: 36, height: 36, background: cor, borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontSize: '18px' }}>▶</div>
        <div>
          <div style={{ fontSize: '15px', fontWeight: 500 }}>F.R.A.M.E</div>
          <div style={{ fontSize: '12px', color: '#888' }}>{modo === 'backoffice' ? 'Backoffice' : 'Portal do Cliente'}</div>
        </div>
      </div>

      {/* Toggle */}
      <div style={{ padding: '10px 8px', borderBottom: '0.5px solid #e0e0e0' }}>
        <button
          onClick={handleToggle}
          style={{ width: '100%', padding: '8px', borderRadius: '8px', border: `1px solid ${cor}`, background: 'white', color: cor, fontSize: '12px', fontWeight: 600, cursor: 'pointer' }}>
          {modo === 'backoffice' ? '→ Portal do Cliente' : '→ Backoffice'}
        </button>
      </div>

      {/* Links */}
      <nav style={{ flex: 1, padding: '12px 8px', display: 'flex', flexDirection: 'column', gap: '2px' }}>
        {modo === 'backoffice' ? (
          <>
            <NavLink to="/" end style={({ isActive }) => navStyle(isActive, cor)}>Dashboard</NavLink>
            <NavLink to="/filmes" style={({ isActive }) => navStyle(isActive, cor)}>Catálogo de Filmes</NavLink>
            <NavLink to="/salas" style={({ isActive }) => navStyle(isActive, cor)}>Salas</NavLink>
            <NavLink to="/grade" style={({ isActive }) => navStyle(isActive, cor)}>Grade de Exibição</NavLink>
            <NavLink to="/bomboniere" style={({ isActive }) => navStyle(isActive, cor)}>Bomboniere</NavLink>
            <NavLink to="/checkin" style={({ isActive }) => navStyle(isActive, cor)}>Check-in</NavLink>
            <NavLink to="/caixa" style={({ isActive }) => navStyle(isActive, cor)}>Fechamento de Caixa</NavLink>
          </>
        ) : (
          <>
            <NavLink to="/" end style={({ isActive }) => navStyle(isActive, cor)}>Início</NavLink>
            <NavLink to="/programacao" style={({ isActive }) => navStyle(isActive, cor)}>Programação</NavLink>
            <NavLink to="/compra" style={({ isActive }) => navStyle(isActive, cor)}>Comprar Ingresso</NavLink>
            <NavLink to="/fidelidade" style={({ isActive }) => navStyle(isActive, cor)}>Fidelidade</NavLink>
          </>
        )}
      </nav>

      {/* Rodapé */}
      <div style={{ padding: '12px 8px', borderTop: '0.5px solid #e0e0e0' }}>
        <a href="#" style={navStyle(false, cor)}>Sair</a>
      </div>
    </aside>
  )
}

export default function App() {
  const [modo, setModo] = useState<Modo>('backoffice')

  return (
    <BrowserRouter>
      <div style={{ display: 'flex', minHeight: '100vh', fontFamily: 'sans-serif' }}>
        <Sidebar modo={modo} onToggle={() => setModo(m => m === 'backoffice' ? 'portal' : 'backoffice')} />
        <main style={{ flex: 1, padding: '1.5rem', backgroundColor: '#FAFAFA' }}>
          <Routes>
            <Route path="/" element={<Home modo={modo} />} />
            {/* Backoffice */}
            <Route path="/filmes" element={<CatalogoPage />} />
            <Route path="/salas" element={<SalasPage />} />
            <Route path="/grade" element={<GradePage />} />
            <Route path="/bomboniere" element={<BombonierePage />} />
            <Route path="/checkin" element={<EmConstrucao nome="Check-in" />} />
            <Route path="/caixa" element={<EmConstrucao nome="Fechamento de Caixa" />} />
            {/* Portal */}
            <Route path="/programacao" element={<EmConstrucao nome="Programação" />} />
            <Route path="/compra" element={<CompraPage />} />
            <Route path="/fidelidade" element={<EmConstrucao nome="Fidelidade" />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
