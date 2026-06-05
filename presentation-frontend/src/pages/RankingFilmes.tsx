import { useEffect, useRef, useState } from 'react'

interface FilmeRanking {
  titulo: string
  ingressos: number
  receita: number
}

interface Props {
  dados: FilmeRanking[]
}

type Metrica = 'ingressos' | 'receita'

const VINHO = '#8B0000'
const CINZA = '#B4B2A9'
const CINZA_TRACK = '#f0eeea'
const MEDALS = ['🥇', '🥈', '🥉']

function fmtBRL(v: number) {
  return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

declare global {
  interface Window { Chart: any }
}

export default function RankingFilmes({ dados }: Props) {
  const [metrica, setMetrica] = useState<Metrica>('ingressos')
  const [animado, setAnimado] = useState(false)
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const chartRef = useRef<any>(null)

  const sorted = [...dados].sort((a, b) =>
    metrica === 'ingressos' ? b.ingressos - a.ingressos : b.receita - a.receita
  )
  const max = sorted.length > 0
    ? (metrica === 'ingressos' ? sorted[0].ingressos : sorted[0].receita)
    : 1

  // animar barras ao montar ou trocar métrica
  useEffect(() => {
    setAnimado(false)
    const t = setTimeout(() => setAnimado(true), 40)
    return () => clearTimeout(t)
  }, [metrica, dados])

  // gráfico Chart.js
  useEffect(() => {
    if (!canvasRef.current || !window.Chart) return
    const labels = sorted.map(d => d.titulo.length > 14 ? d.titulo.slice(0, 13) + '…' : d.titulo)
    const vals = sorted.map(d => metrica === 'ingressos' ? d.ingressos : d.receita)
    const bgColors = vals.map((_, i) => i === 0 ? VINHO : CINZA)
    const bdColors = vals.map((_, i) => i === 0 ? VINHO : '#888780')

    if (chartRef.current) chartRef.current.destroy()
    chartRef.current = new window.Chart(canvasRef.current, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: metrica === 'ingressos' ? 'Ingressos' : 'Receita (R$)',
          data: vals,
          backgroundColor: bgColors,
          borderColor: bdColors,
          borderWidth: 1,
          borderRadius: 4,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (ctx: any) => metrica === 'receita'
                ? ' R$ ' + ctx.parsed.y.toLocaleString('pt-BR')
                : ' ' + ctx.parsed.y + ' ingressos',
            },
          },
        },
        scales: {
          x: {
            ticks: { font: { size: 11 }, color: '#888780', autoSkip: false, maxRotation: 0 },
            grid: { display: false },
            border: { display: false },
          },
          y: {
            beginAtZero: true,
            ticks: {
              font: { size: 11 }, color: '#888780',
              callback: (v: number) => metrica === 'receita' ? 'R$' + v : v,
            },
            grid: { color: 'rgba(0,0,0,0.05)' },
            border: { display: false },
          },
        },
        animation: { duration: 600, easing: 'easeOutQuart' },
      },
    })
    return () => { if (chartRef.current) chartRef.current.destroy() }
  }, [metrica, dados])

  if (dados.length === 0) {
    return (
      <div style={{ padding: '24px', textAlign: 'center', color: '#bbb', fontSize: '13px' }}>
        Nenhum ingresso encontrado nos últimos 30 dias.
      </div>
    )
  }

  const tabStyle = (ativo: boolean): React.CSSProperties => ({
    fontSize: '12px',
    padding: '5px 12px',
    borderRadius: '6px',
    border: `0.5px solid ${ativo ? VINHO : '#E0E0E0'}`,
    background: ativo ? VINHO : 'transparent',
    color: ativo ? 'white' : '#888',
    cursor: 'pointer',
    fontWeight: ativo ? 600 : 400,
    transition: 'all 0.15s',
  })

  return (
    <div>
      {/* header com tabs */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
        <div style={{ display: 'flex', gap: 4 }}>
          <button style={tabStyle(metrica === 'ingressos')} onClick={() => setMetrica('ingressos')}>
            Ingressos
          </button>
          <button style={tabStyle(metrica === 'receita')} onClick={() => setMetrica('receita')}>
            Receita
          </button>
        </div>
        <span style={{ fontSize: '11px', color: '#bbb' }}>últimos 30 dias</span>
      </div>

      {/* lista com barras animadas */}
      <div style={{ display: 'flex', flexDirection: 'column' }}>
        {sorted.map((d, i) => {
          const val = metrica === 'ingressos' ? d.ingressos : d.receita
          const pct = max > 0 ? Math.round((val / max) * 100) : 0
          const sub = metrica === 'receita'
            ? `${d.ingressos} ingressos`
            : fmtBRL(d.receita)

          return (
            <div
              key={d.titulo}
              style={{
                display: 'flex', alignItems: 'center', gap: 10,
                padding: '9px 6px',
                borderBottom: i < sorted.length - 1 ? '0.5px solid #f0eeea' : 'none',
                borderRadius: '4px',
                transition: 'background 0.1s',
              }}
            >
              {/* medalha / número */}
              <div style={{
                fontSize: i < 3 ? '15px' : '13px',
                fontWeight: 500,
                color: i < 3 ? VINHO : '#ccc',
                width: 22,
                textAlign: 'center',
                flexShrink: 0,
              }}>
                {i < 3 ? MEDALS[i] : i + 1}
              </div>

              {/* info */}
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{
                  fontSize: 13, fontWeight: 500, color: '#1a1a1a',
                  whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
                }}>
                  {d.titulo}
                </div>
                <div style={{ fontSize: 11, color: '#999', marginTop: 1 }}>{sub}</div>
              </div>

              {/* barra */}
              <div style={{ width: 100, flexShrink: 0 }}>
                <div style={{ height: 6, background: CINZA_TRACK, borderRadius: 3, overflow: 'hidden' }}>
                  <div style={{
                    height: '100%',
                    borderRadius: 3,
                    background: i === 0 ? VINHO : CINZA,
                    width: animado ? `${pct}%` : '0%',
                    transition: `width 0.6s cubic-bezier(0.22,1,0.36,1) ${i * 60}ms`,
                  }} />
                </div>
              </div>

              {/* valor */}
              <div style={{
                fontSize: 13, fontWeight: 600, color: '#1a1a1a',
                minWidth: 42, textAlign: 'right', flexShrink: 0,
              }}>
                {metrica === 'ingressos' ? val : fmtBRL(val)}
              </div>
            </div>
          )
        })}
      </div>

      {/* gráfico de barras */}
      <div style={{ marginTop: 20, position: 'relative', width: '100%', height: `${Math.max(sorted.length * 40 + 60, 180)}px` }}>
        <canvas
          ref={canvasRef}
          role="img"
          aria-label={`Gráfico de barras: ${metrica === 'ingressos' ? 'ingressos vendidos' : 'receita'} por filme nos últimos 30 dias`}
        >
          {sorted.map(d => `${d.titulo}: ${metrica === 'ingressos' ? d.ingressos : fmtBRL(d.receita)}`).join(', ')}
        </canvas>
      </div>
    </div>
  )
}
