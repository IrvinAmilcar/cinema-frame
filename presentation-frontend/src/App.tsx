import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom'
import CatalogoPage from './pages/CatalogoPage'
import GradePage from './pages/GradePage'
import SalasPage from './pages/SalasPage'
import BombonierePage from './pages/BombonierePage'


function Home() {
  return <h2>Bem-vindo ao F.R.A.M.E — Backoffice</h2>
}

const navStyle = (isActive: boolean): React.CSSProperties => ({
  display: 'flex', alignItems: 'center', gap: '10px',
  padding: '9px 12px',
  borderRadius: '8px',
  fontSize: '14px',
  textDecoration: 'none',
  color: isActive ? 'white' : '#555',
  background: isActive ? '#8B0000' : 'transparent',
  fontWeight: isActive ? 500 : 400,
})

export default function App() {
  return (
    <BrowserRouter>
      <div style={{ display: 'flex', minHeight: '100vh', fontFamily: 'sans-serif' }}>

        {/* SIDEBAR */}
        <aside style={{ width: '220px', borderRight: '0.5px solid #e0e0e0', display: 'flex', flexDirection: 'column', backgroundColor: 'white', flexShrink: 0 }}>
          
          {/* Logo */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '20px 16px 16px', borderBottom: '0.5px solid #e0e0e0' }}>
            <div style={{ width: 36, height: 36, background: '#8B0000', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontSize: '18px' }}>▶</div>
            <div>
              <div style={{ fontSize: '15px', fontWeight: 500 }}>F.R.A.M.E</div>
              <div style={{ fontSize: '12px', color: '#888' }}>Cinema Backoffice</div>
            </div>
          </div>

          {/* Links */}
          <nav style={{ flex: 1, padding: '12px 8px', display: 'flex', flexDirection: 'column', gap: '2px' }}>
            <NavLink to="/" end style={({ isActive }) => navStyle(isActive)}>Dashboard</NavLink>
            <NavLink to="/filmes" style={({ isActive }) => navStyle(isActive)}>Catálogo de Filmes</NavLink>
            <NavLink to="/grade" style={({ isActive }) => navStyle(isActive)}>Grade de Exibição</NavLink>
            <NavLink to="/bomboniere" style={({ isActive }) => navStyle(isActive)}>Bomboniere</NavLink>
            <NavLink to="/checkin" style={({ isActive }) => navStyle(isActive)}>Check-in</NavLink>
            <NavLink to="/caixa" style={({ isActive }) => navStyle(isActive)}>Fechamento de Caixa</NavLink>
            <NavLink to="/usuarios" style={({ isActive }) => navStyle(isActive)}>Usuários</NavLink>
          </nav>

          {/* Rodapé */}
          <div style={{ padding: '12px 8px', borderTop: '0.5px solid #e0e0e0' }}>
            <a href="#" style={navStyle(false)}>Sair</a>
          </div>
        </aside>
      <main style={{ flex: 1, padding: '1.5rem', backgroundColor: '#FAFAFA' }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/filmes" element={<CatalogoPage />} />
          <Route path="/salas" element={<SalasPage />} />
          <Route path="/grade" element={<GradePage />} />
          <Route path="/bomboniere" element={<BombonierePage />} />
          {/* Fabiana: <Route path="/checkin" element={<CheckinPage />} /> */}
          {/* Amanda:  <Route path="/fidelidade" element={<FidelidadePage />} /> */}
          {/* Amanda:  <Route path="/caixa" element={<CaixaPage />} /> */}
          {/* Julia:   <Route path="/programacao" element={<ProgramacaoPage />} /> */}
          {/* Julia:   <Route path="/compra" element={<CompraPage />} /> */}
        </Routes>
      </main>
       </div>  
    </BrowserRouter>
  )
}
