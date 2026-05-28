import { useEffect, useState } from 'react'

const API = 'http://localhost:8080'
const COR = '#1565C0'

interface Produto {
  id: string
  nome: string
  preco: number
  categoria: string
  ativo: boolean
}

const CATEGORIA_ICON: Record<string, string> = {
  PIPOCA: '🍿', BEBIDA: '🥤', COMBO: '🎁', DOCE: '🍬',
  SALGADO: '🥨', SOBREMESA: '🍦',
}

export default function CardapioPage() {
  const [produtos, setProdutos] = useState<Produto[]>([])
  const [loading, setLoading] = useState(true)
  const [categoriaSelecionada, setCategoriaSelecionada] = useState('')

  useEffect(() => {
    fetch(`${API}/bomboniere/produtos`)
      .then(r => r.json())
      .then((data: Produto[]) => { setProdutos(data.filter(p => p.ativo)); setLoading(false) })
      .catch(() => setLoading(false))
  }, [])

  const categorias = ['', ...Array.from(new Set(produtos.map(p => p.categoria))).sort()]
  const filtrados = categoriaSelecionada
    ? produtos.filter(p => p.categoria === categoriaSelecionada)
    : produtos

  const porCategoria = filtrados.reduce<Record<string, Produto[]>>((acc, p) => {
    if (!acc[p.categoria]) acc[p.categoria] = []
    acc[p.categoria].push(p)
    return acc
  }, {})

  return (
    <div style={{ maxWidth: 900 }}>
      <div style={{ marginBottom: 28 }}>
        <h1 style={{ margin: 0, fontSize: 26, fontWeight: 700, color: '#1a1a2e' }}>🍿 Bomboniere</h1>
        <p style={{ margin: '6px 0 0', color: '#888', fontSize: 14 }}>
          Confira nosso cardápio e adicione itens na hora de comprar seu ingresso.
        </p>
      </div>

      {/* Filtro por categoria */}
      {!loading && categorias.length > 1 && (
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 28 }}>
          {categorias.map(cat => (
            <button key={cat} onClick={() => setCategoriaSelecionada(cat)}
              style={{
                padding: '7px 16px', borderRadius: 20, fontSize: 13, cursor: 'pointer',
                border: categoriaSelecionada === cat ? 'none' : '1px solid #ddd',
                background: categoriaSelecionada === cat ? COR : 'white',
                color: categoriaSelecionada === cat ? 'white' : '#555',
                fontWeight: categoriaSelecionada === cat ? 600 : 400,
              }}>
              {cat === '' ? 'Todos' : `${CATEGORIA_ICON[cat] ?? '🍽️'} ${cat}`}
            </button>
          ))}
        </div>
      )}

      {loading && (
        <div style={{ color: '#aaa', fontSize: 14, padding: '40px 0', textAlign: 'center' }}>
          Carregando cardápio...
        </div>
      )}

      {!loading && filtrados.length === 0 && (
        <div style={{ color: '#aaa', fontSize: 14, padding: '40px 0', textAlign: 'center' }}>
          Nenhum produto disponível no momento.
        </div>
      )}

      {/* Produtos agrupados por categoria */}
      {!loading && Object.entries(porCategoria).map(([cat, itens]) => (
        <div key={cat} style={{ marginBottom: 32 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 14 }}>
            <span style={{ fontSize: 20 }}>{CATEGORIA_ICON[cat] ?? '🍽️'}</span>
            <h2 style={{ margin: 0, fontSize: 16, fontWeight: 700, color: '#1a1a2e' }}>{cat}</h2>
            <div style={{ flex: 1, height: 1, background: '#f0f0f0', marginLeft: 8 }} />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 14 }}>
            {itens.map(p => (
              <div key={p.id} style={{
                background: 'white', borderRadius: 14,
                border: '1px solid #f0f0f0',
                boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
                padding: '20px 18px',
                display: 'flex', flexDirection: 'column', gap: 10,
              }}>
                <div style={{
                  width: 52, height: 52, borderRadius: '50%',
                  background: '#f5f5f5',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 26,
                }}>
                  {CATEGORIA_ICON[p.categoria] ?? '🍽️'}
                </div>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 15, color: '#1a1a2e', marginBottom: 4 }}>{p.nome}</div>
                  <div style={{ fontSize: 12, color: '#aaa' }}>{p.categoria}</div>
                </div>
                <div style={{ fontWeight: 800, fontSize: 18, color: COR, marginTop: 'auto' }}>
                  R$ {p.preco.toFixed(2)}
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}

      {/* Aviso de compra */}
      {!loading && filtrados.length > 0 && (
        <div style={{
          background: '#e3f2fd', border: '1px solid #90caf9', borderRadius: 12,
          padding: '14px 20px', marginTop: 8, display: 'flex', alignItems: 'center', gap: 12,
        }}>
          <span style={{ fontSize: 20 }}>🎟️</span>
          <p style={{ margin: 0, fontSize: 13, color: '#1565c0' }}>
            Para adicionar itens da bomboniere, selecione-os durante a etapa de compra do ingresso.
          </p>
        </div>
      )}
    </div>
  )
}
