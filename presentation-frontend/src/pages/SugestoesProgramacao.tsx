import { useEffect, useRef, useState } from 'react'

interface SugestaoGenero {
  genero: string
  label: string
  assistidos: number
  favoritados: number
  score: number
  naGrade: boolean
}

interface Props {
  dados: SugestaoGenero[]
}

const VINHO = '#8B0000'

declare global {
  interface Window { Chart: any }
}

function fmtScore(s: number) {
  return s.toLocaleString('pt-BR')
}

export default function SugestoesProgramacao({ dados }: Props) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const chartRef = useRef<any>(null)
  const [selecionado, setSelecionado] = useState<SugestaoGenero | null>(null)

  const ausentes = dados.filter(d => !d.naGrade)
  const presentes = dados.filter(d => d.naGrade)
  const maxScore = dados.length > 0 ? dados[0].score : 1

  useEffect(() => {
    if (!canvasRef.current || !window.Chart || dados.length === 0) return

    const labels = dados.map(d => d.label)
    const assistidosData = dados.map(d => d.assistidos * 3)
    const favoritadosData = dados.map(d => d.favoritados)
    const bgAssistidos = dados.map(d => d.naGrade ? '#D3D1C7' : VINHO + 'cc')
    const bgFavoritos = dados.map(d => d.naGrade ? '#E8E6E0' : '#C6282899')

    if (chartRef.current) chartRef.current.destroy()
    chartRef.current = new window.Chart(canvasRef.current, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {
            label: 'Assistidos (×3)',
            data: assistidosData,
            backgroundColor: bgAssistidos,
            borderColor: dados.map(d => d.naGrade ? '#B4B2A9' : VINHO),
            borderWidth: 1,
            borderRadius: 3,
          },
          {
            label: 'Favoritados',
            data: favoritadosData,
            backgroundColor: bgFavoritos,
            borderColor: dados.map(d => d.naGrade ? '#ccc' : '#C62828'),
            borderWidth: 1,
            borderRadius: 3,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              title: (items: any[]) => dados[items[0].dataIndex]?.label ?? '',
              afterBody: (items: any[]) => {
                const d = dados[items[0].dataIndex]
                if (!d) return []
                return [
                  `Score total: ${d.score}`,
                  d.naGrade ? '✓ já na grade' : '⚠ não está na grade',
                ]
              },
            },
          },
        },
        scales: {
          x: {
            stacked: true,
            ticks: { font: { size: 11 }, color: '#888780', maxRotation: 30 },
            grid: { display: false },
            border: { display: false },
          },
          y: {
            stacked: true,
            beginAtZero: true,
            ticks: { font: { size: 11 }, color: '#888780' },
            grid: { color: 'rgba(0,0,0,0.05)' },
            border: { display: false },
          },
        },
        onClick: (_: any, els: any[]) => {
          if (!els.length) { setSelecionado(null); return }
          const d = dados[els[0].index]
          setSelecionado(prev => prev?.genero === d.genero ? null : d)
        },
        animation: { duration: 500, easing: 'easeOutQuart' },
      },
    })
    return () => { if (chartRef.current) chartRef.current.destroy() }
  }, [dados])

  if (dados.length === 0) {
    return (
      <div style={{ padding: '24px', textAlign: 'center', color: '#bbb', fontSize: '13px' }}>
        Nenhum dado de histórico encontrado. Os dados aparecem conforme os clientes compram ingressos.
      </div>
    )
  }

  return (
    <div>
      {/* KPIs rápidos */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 8, marginBottom: 16 }}>
        <div style={{ background: '#FFF3F3', border: '0.5px solid #FFCDD2', borderRadius: 8, padding: '10px 14px' }}>
          <div style={{ fontSize: 11, color: '#aaa', marginBottom: 3 }}>gêneros sem cobertura</div>
          <div style={{ fontSize: 22, fontWeight: 500, color: VINHO }}>{ausentes.length}</div>
          <div style={{ fontSize: 11, color: '#C62828', marginTop: 2 }}>
            {ausentes.slice(0, 2).map(d => d.label).join(', ')}{ausentes.length > 2 ? '…' : ''}
          </div>
        </div>
        <div style={{ background: '#F5F5F5', borderRadius: 8, padding: '10px 14px' }}>
          <div style={{ fontSize: 11, color: '#aaa', marginBottom: 3 }}>gêneros na grade</div>
          <div style={{ fontSize: 22, fontWeight: 500, color: '#555' }}>{presentes.length}</div>
          <div style={{ fontSize: 11, color: '#888', marginTop: 2 }}>
            {presentes.slice(0, 2).map(d => d.label).join(', ')}{presentes.length > 2 ? '…' : ''}
          </div>
        </div>
        <div style={{ background: '#F5F5F5', borderRadius: 8, padding: '10px 14px' }}>
          <div style={{ fontSize: 11, color: '#aaa', marginBottom: 3 }}>top sugestão</div>
          <div style={{ fontSize: 14, fontWeight: 500, color: ausentes[0] ? VINHO : '#555', marginTop: 4 }}>
            {ausentes[0]?.label ?? presentes[0]?.label ?? '—'}
          </div>
          <div style={{ fontSize: 11, color: '#aaa', marginTop: 2 }}>
            score {fmtScore(ausentes[0]?.score ?? presentes[0]?.score ?? 0)}
          </div>
        </div>
      </div>

      {/* legenda */}
      <div style={{ display: 'flex', gap: 16, marginBottom: 10 }}>
        <span style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 11, color: '#888' }}>
          <span style={{ width: 10, height: 10, borderRadius: 2, background: VINHO, display: 'inline-block' }} />
          não está na grade — prioridade
        </span>
        <span style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 11, color: '#888' }}>
          <span style={{ width: 10, height: 10, borderRadius: 2, background: '#D3D1C7', display: 'inline-block' }} />
          já está na grade
        </span>
      </div>

      {/* gráfico */}
      <div style={{ position: 'relative', width: '100%', height: `${Math.max(dados.length * 36 + 60, 200)}px` }}>
        <canvas
          ref={canvasRef}
          role="img"
          aria-label="Gráfico de barras empilhadas com demanda por gênero de filme"
        >
          {dados.map(d => `${d.label}: score ${d.score}`).join(', ')}
        </canvas>
      </div>

      {/* detalhe ao clicar */}
      {selecionado && (
        <div style={{
          marginTop: 12,
          background: selecionado.naGrade ? '#F9F9F9' : '#FFF9F9',
          border: `0.5px solid ${selecionado.naGrade ? '#E0E0E0' : '#FFCDD2'}`,
          borderRadius: 10,
          padding: '14px 18px',
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
            <span style={{ fontSize: 14, fontWeight: 500, color: '#1a1a1a' }}>{selecionado.label}</span>
            <span style={{
              fontSize: 11, fontWeight: 600,
              background: selecionado.naGrade ? '#E8F5E9' : '#FFEBEE',
              color: selecionado.naGrade ? '#2E7D32' : VINHO,
              border: `0.5px solid ${selecionado.naGrade ? '#C8E6C9' : '#FFCDD2'}`,
              padding: '2px 8px', borderRadius: 4,
            }}>
              {selecionado.naGrade ? '✓ na grade atual' : '⚠ ausente da grade'}
            </span>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4,1fr)', gap: 10 }}>
            {[
              { label: 'score total', valor: fmtScore(selecionado.score), destaque: !selecionado.naGrade },
              { label: 'vezes assistido', valor: String(selecionado.assistidos) },
              { label: 'vezes favoritado', valor: String(selecionado.favoritados) },
              { label: 'peso (3×assistido + fav)', valor: `${selecionado.assistidos * 3} + ${selecionado.favoritados}` },
            ].map(({ label, valor, destaque }) => (
              <div key={label}>
                <div style={{ fontSize: 11, color: '#aaa', marginBottom: 2 }}>{label}</div>
                <div style={{ fontSize: 16, fontWeight: 500, color: destaque ? VINHO : '#1a1a1a' }}>{valor}</div>
              </div>
            ))}
          </div>
          {!selecionado.naGrade && (
            <div style={{
              marginTop: 12, paddingTop: 10,
              borderTop: '0.5px solid #FFCDD2',
              fontSize: 12, color: '#C62828',
            }}>
              Considere adicionar um filme de <strong>{selecionado.label}</strong> na próxima grade de exibição.
            </div>
          )}
        </div>
      )}

      {/* barra de score relativo */}
      <div style={{ marginTop: 16, display: 'flex', flexDirection: 'column', gap: 6 }}>
        {dados.slice(0, 5).map((d, i) => {
          const pct = Math.round((d.score / maxScore) * 100)
          return (
            <div
              key={d.genero}
              onClick={() => setSelecionado(prev => prev?.genero === d.genero ? null : d)}
              style={{
                display: 'flex', alignItems: 'center', gap: 10,
                padding: '6px 8px', borderRadius: 6, cursor: 'pointer',
                background: selecionado?.genero === d.genero
                  ? (d.naGrade ? '#F5F5F5' : '#FFF3F3')
                  : 'transparent',
                border: `0.5px solid ${selecionado?.genero === d.genero ? (d.naGrade ? '#E0E0E0' : '#FFCDD2') : 'transparent'}`,
              }}
            >
              <div style={{ fontSize: 13, fontWeight: 500, color: '#888', minWidth: 16 }}>{i + 1}</div>
              <div style={{ fontSize: 12, fontWeight: 500, color: '#1a1a1a', minWidth: 120 }}>{d.label}</div>
              <div style={{ flex: 1 }}>
                <div style={{ height: 6, background: '#f0eeea', borderRadius: 3, overflow: 'hidden' }}>
                  <div style={{
                    height: '100%', width: `${pct}%`, borderRadius: 3,
                    background: d.naGrade ? '#B4B2A9' : VINHO,
                    transition: 'width 0.5s ease',
                  }} />
                </div>
              </div>
              <div style={{ fontSize: 12, fontWeight: 600, minWidth: 50, textAlign: 'right', color: d.naGrade ? '#888' : VINHO }}>
                {fmtScore(d.score)} pts
              </div>
              {!d.naGrade && (
                <span style={{ fontSize: 10, background: '#FFEBEE', color: VINHO, padding: '1px 6px', borderRadius: 4, fontWeight: 600, whiteSpace: 'nowrap' }}>
                  sugerir
                </span>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}
