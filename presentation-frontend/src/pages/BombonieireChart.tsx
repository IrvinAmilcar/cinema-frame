import { useEffect, useRef, useState } from 'react'

interface BombonieireItem {
  produto: string
  quantidade: number
  valorTotal: number
}

interface Props {
  dados: BombonieireItem[]
}

type Metrica = 'quantidade' | 'receita'

const VINHO = '#8B0000'
const PALETA = ['#8B0000', '#C62828', '#B4B2A9', '#888780', '#5F5E5A', '#D3D1C7', '#444441']

function fmtBRL(v: number) {
  return v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

declare global {
  interface Window { Chart: any }
}

export default function BombonieireChart({ dados }: Props) {
  const [metrica, setMetrica] = useState<Metrica>('quantidade')
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const chartRef = useRef<any>(null)
  const [selecionado, setSelecionado] = useState<string | null>(null)

  // agrega por produto
  const porProduto: Record<string, { quantidade: number; receita: number }> = {}
  dados.forEach(d => {
    if (!porProduto[d.produto]) porProduto[d.produto] = { quantidade: 0, receita: 0 }
    porProduto[d.produto].quantidade += d.quantidade
    porProduto[d.produto].receita += d.valorTotal
  })

  const sorted = Object.entries(porProduto)
    .map(([produto, v]) => ({ produto, ...v }))
    .sort((a, b) => metrica === 'quantidade' ? b.quantidade - a.quantidade : b.receita - a.receita)
    .slice(0, 7)

  const totalQtd = sorted.reduce((s, d) => s + d.quantidade, 0)
  const totalRec = sorted.reduce((s, d) => s + d.receita, 0)

  const getVal = (d: typeof sorted[0]) => metrica === 'quantidade' ? d.quantidade : d.receita

  useEffect(() => {
    if (!canvasRef.current || !window.Chart || sorted.length === 0) return

    const vals = sorted.map(d => getVal(d))
    const labels = sorted.map(d => d.produto.length > 16 ? d.produto.slice(0, 15) + '…' : d.produto)
    const cores = sorted.map((_, i) => PALETA[i % PALETA.length])

    if (chartRef.current) chartRef.current.destroy()
    chartRef.current = new window.Chart(canvasRef.current, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [{
          data: vals,
          backgroundColor: cores,
          borderColor: '#fff',
          borderWidth: 2,
          hoverOffset: 6,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '62%',
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (ctx: any) => {
                const pct = Math.round((ctx.parsed / vals.reduce((a: number, b: number) => a + b, 0)) * 100)
                return metrica === 'receita'
                  ? ` ${fmtBRL(ctx.parsed)} (${pct}%)`
                  : ` ${ctx.parsed} unid. (${pct}%)`
              },
            },
          },
        },
        onClick: (_: any, els: any[]) => {
          if (els.length > 0) {
            const idx = els[0].index
            setSelecionado(prev => prev === sorted[idx].produto ? null : sorted[idx].produto)
          } else {
            setSelecionado(null)
          }
        },
        animation: { duration: 600, easing: 'easeOutQuart' },
      },
    })
    return () => { if (chartRef.current) chartRef.current.destroy() }
  }, [metrica, dados])

  if (sorted.length === 0) {
    return (
      <div style={{ padding: '24px', textAlign: 'center', color: '#bbb', fontSize: '13px' }}>
        Nenhuma venda de bomboniere nos últimos 30 dias.
      </div>
    )
  }

  const tabStyle = (ativo: boolean): React.CSSProperties => ({
    fontSize: '12px', padding: '5px 12px', borderRadius: '6px',
    border: `0.5px solid ${ativo ? VINHO : '#E0E0E0'}`,
    background: ativo ? VINHO : 'transparent',
    color: ativo ? 'white' : '#888',
    cursor: 'pointer', fontWeight: ativo ? 600 : 400,
    transition: 'all 0.15s',
  })

  const sel = selecionado ? sorted.find(d => d.produto === selecionado) : null

  return (
    <div>
      {/* tabs */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
        <div style={{ display: 'flex', gap: 4 }}>
          <button style={tabStyle(metrica === 'quantidade')} onClick={() => setMetrica('quantidade')}>Unidades</button>
          <button style={tabStyle(metrica === 'receita')} onClick={() => setMetrica('receita')}>Receita</button>
        </div>
        <span style={{ fontSize: '11px', color: '#bbb' }}>últimos 30 dias</span>
      </div>

      {/* rosca + legenda */}
      <div style={{ display: 'flex', gap: 16, alignItems: 'center' }}>

        {/* rosca */}
        <div style={{ position: 'relative', width: 160, height: 160, flexShrink: 0 }}>
          <canvas
            ref={canvasRef}
            role="img"
            aria-label={`Gráfico de rosca: ${metrica === 'quantidade' ? 'unidades vendidas' : 'receita'} por produto da bomboniere`}
          >
            {sorted.map(d => `${d.produto}: ${metrica === 'quantidade' ? d.quantidade : fmtBRL(d.receita)}`).join(', ')}
          </canvas>
          {/* centro da rosca */}
          <div style={{
            position: 'absolute', inset: 0, display: 'flex',
            flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
            pointerEvents: 'none',
          }}>
            {sel ? (
              <>
                <div style={{ fontSize: 11, color: '#888', textAlign: 'center', maxWidth: 70, lineHeight: 1.2, marginBottom: 3 }}>
                  {sel.produto.length > 12 ? sel.produto.slice(0, 11) + '…' : sel.produto}
                </div>
                <div style={{ fontSize: 15, fontWeight: 600, color: VINHO }}>
                  {metrica === 'quantidade' ? sel.quantidade : fmtBRL(sel.receita)}
                </div>
              </>
            ) : (
              <>
                <div style={{ fontSize: 11, color: '#aaa', marginBottom: 2 }}>total</div>
                <div style={{ fontSize: 15, fontWeight: 600, color: '#1a1a1a' }}>
                  {metrica === 'quantidade' ? totalQtd : fmtBRL(totalRec)}
                </div>
              </>
            )}
          </div>
        </div>

        {/* lista de produtos */}
        <div style={{ flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column', gap: 6 }}>
          {sorted.map((d, i) => {
            const val = getVal(d)
            const total = metrica === 'quantidade' ? totalQtd : totalRec
            const pct = total > 0 ? Math.round((val / total) * 100) : 0
            const cor = PALETA[i % PALETA.length]
            const ativo = selecionado === d.produto
            return (
              <div
                key={d.produto}
                onClick={() => setSelecionado(prev => prev === d.produto ? null : d.produto)}
                style={{
                  display: 'flex', alignItems: 'center', gap: 8,
                  padding: '5px 6px', borderRadius: '6px', cursor: 'pointer',
                  background: ativo ? '#FFF3F3' : 'transparent',
                  border: `0.5px solid ${ativo ? '#FFCDD2' : 'transparent'}`,
                  transition: 'background 0.1s',
                }}
              >
                <span style={{
                  width: 10, height: 10, borderRadius: '50%',
                  background: cor, flexShrink: 0, display: 'inline-block',
                }} />
                <div style={{
                  fontSize: 12, color: '#1a1a1a', flex: 1, minWidth: 0,
                  whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
                  fontWeight: ativo ? 500 : 400,
                }}>
                  {d.produto}
                </div>
                <div style={{ fontSize: 11, color: '#888', flexShrink: 0 }}>{pct}%</div>
                <div style={{ fontSize: 12, fontWeight: 500, color: '#1a1a1a', flexShrink: 0, minWidth: 44, textAlign: 'right' }}>
                  {metrica === 'quantidade' ? d.quantidade : fmtBRL(d.receita)}
                </div>
              </div>
            )
          })}
        </div>
      </div>

      {/* totais */}
      <div style={{
        marginTop: 14, paddingTop: 12,
        borderTop: '0.5px solid #f0eeea',
        display: 'flex', justifyContent: 'space-between',
        fontSize: 12, color: '#888',
      }}>
        <span>{totalQtd} unidades vendidas</span>
        <span style={{ fontWeight: 500, color: VINHO }}>{fmtBRL(totalRec)}</span>
      </div>
    </div>
  )
}
