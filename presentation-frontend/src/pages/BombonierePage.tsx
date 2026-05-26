import { useState, useEffect } from 'react';
import { apiBomboniere, InsumoResponse, ProdutoBomboniereResponse } from '../api/client';

export default function BombonierePage() {
  const [insumos, setInsumos] = useState<InsumoResponse[]>([]);
  const [produtos, setProdutos] = useState<ProdutoBomboniereResponse[]>([]);
  const [alertas, setAlertas] = useState<InsumoResponse[]>([]);

  // Carrega os dados quando a página abrir
  useEffect(() => {
    carregarDados();
  }, []);

  const carregarDados = async () => {
    try {
      const insumosData = await apiBomboniere.listarInsumos();
      const produtosData = await apiBomboniere.listarProdutos();
      const alertasData = await apiBomboniere.listarAlertas();
      
      setInsumos(insumosData);
      setProdutos(produtosData);
      setAlertas(alertasData);
    } catch (error) {
      console.error("Erro ao carregar os dados da bomboniere", error);
    }
  };

  // A função deve ficar SEMPRE antes do return!
  const handleVenda = async (produtoId: string) => {
    try {
      await apiBomboniere.venderProduto(produtoId);
      
      alert('Venda realizada com sucesso!');
      
      // recarrega os dados para a tela atualizar o estoque e os alertas em tempo real!
      carregarDados(); 
    } catch (error) {
      console.error("Erro ao vender", error);
      alert('Erro ao realizar a venda. Verifique o console.');
    }
  };

  // O return é a última coisa do seu componente
  return (
    <div>
      <h1>Bomboniere Backoffice</h1>
      <p>Gerenciamento de estoque, produtos e vendas.</p>
      
      <div style={{ display: 'flex', gap: '2rem', marginTop: '2rem' }}>
        
        {/* Painel de Alertas de Estoque */}
        <div style={{ flex: 1, border: '1px solid red', padding: '1rem', borderRadius: '8px' }}>
          <h3>⚠️ Alertas de Estoque Crítico</h3>
          {alertas.length === 0 ? (
            <p>Nenhum alerta no momento.</p>
          ) : (
            <ul>
              {alertas.map(alerta => (
                <li key={alerta.id} style={{ color: 'red', fontWeight: 'bold' }}>
                  {alerta.nome} (Restam: {alerta.quantidade})
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Listagem de Produtos e Vendas */}
        <div style={{ flex: 2, border: '1px solid #ccc', padding: '1rem', borderRadius: '8px' }}>
          <h3>Catálogo de Produtos (Combos)</h3>
          {produtos.length === 0 ? (
            <p>Nenhum produto cadastrado.</p>
          ) : (
            <ul>
              {produtos.map(produto => (
                <li key={produto.id} style={{ marginBottom: '1rem', listStyleType: 'none', padding: '10px', borderBottom: '1px solid #eee' }}>
                  <strong>{produto.nome}</strong> - R$ {produto.preco}
                  <br />
                  <button 
                    onClick={() => handleVenda(produto.id)} 
                    style={{ marginTop: '10px', padding: '5px 15px', cursor: 'pointer', backgroundColor: '#4CAF50', color: 'white', border: 'none', borderRadius: '4px' }}
                  >
                    Vender Produto
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
        
      </div>
    </div>
  );
}