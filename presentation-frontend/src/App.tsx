import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom'
import CatalogoPage from './pages/CatalogoPage'
import GradePage from './pages/GradePage'
import SalasPage from './pages/SalasPage'

function Home() {
  return <h2>Bem-vindo ao F.R.A.M.E — Backoffice</h2>
}

export default function App() {
  return (
    <BrowserRouter>
      <nav style={{ padding: '1rem', borderBottom: '1px solid #ccc', display: 'flex', gap: '1rem' }}>
        <NavLink to="/">Início</NavLink>
        <NavLink to="/filmes">Catálogo</NavLink>
        <NavLink to="/salas">Salas</NavLink>
        <NavLink to="/grade">Grade</NavLink>
        <NavLink to="/bomboniere">Bomboniere</NavLink>
        <NavLink to="/fidelidade">Fidelidade</NavLink>
        <NavLink to="/caixa">Caixa</NavLink>
      </nav>

      <main style={{ padding: '1.5rem' }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/filmes" element={<CatalogoPage />} />
          <Route path="/salas" element={<SalasPage />} />
          <Route path="/grade" element={<GradePage />} />
          {/* Fabiana: <Route path="/bomboniere" element={<BombonieirePage />} /> */}
          {/* Fabiana: <Route path="/checkin" element={<CheckinPage />} /> */}
          {/* Amanda:  <Route path="/fidelidade" element={<FidelidadePage />} /> */}
          {/* Amanda:  <Route path="/caixa" element={<CaixaPage />} /> */}
          {/* Julia:   <Route path="/programacao" element={<ProgramacaoPage />} /> */}
          {/* Julia:   <Route path="/compra" element={<CompraPage />} /> */}
        </Routes>
      </main>
    </BrowserRouter>
  )
}
