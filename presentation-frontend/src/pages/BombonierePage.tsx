import { useState, useEffect } from 'react';
import { apiBomboniere, InsumoResponse, ProdutoBomboniereResponse } from '../api/client';

export default function BombonierePage() {
  const [abaAtiva, setAbaAtiva] = useState<'insumos' | 'produtos' | 'alertas'>('insumos');
  
  // Dados
  const [insumos, setInsumos] = useState<InsumoResponse[]>([]);
  const [produtos, setProdutos] = useState<any[]>([]); 
  const [alertas, setAlertas] = useState<InsumoResponse[]>([]);

  // Estados dos Modais
  const [modalInsumoAberto, setModalInsumoAberto] = useState(false);
  const [modalProdutoAberto, setModalProdutoAberto] = useState(false);
  const [modalReceitaAberto, setModalReceitaAberto] = useState<{aberto: boolean, produtoId: string | null}>({aberto: false, produtoId: null});
  const [modalReporAberto, setModalReporAberto] = useState<{aberto: boolean, insumo: InsumoResponse | null}>({aberto: false, insumo: null});
  // Formulários
  const [formInsumo, setFormInsumo] = useState({ nome: '', unidade: 'g (gramas)', quantidade: 0, nivelCritico: 0 });
  const [formProduto, setFormProduto] = useState({ nome: '', preco: 0, categoria: 'SALGADO' });
  const [formReceita, setFormReceita] = useState({ insumoId: '', quantidade: 0 });
  const [formRepor, setFormRepor] = useState({ quantidade: 0 });

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
      console.error("Erro ao carregar dados", error);
    }
  };

  // --- Funções de Submissão ---

  const handleCadastrarInsumo = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // O seu backend atualmente não recebe a "unidade" no Request, se precisar, atualize o InsumoRequest no Java!
      await apiBomboniere.cadastrarInsumo({
        nome: formInsumo.nome,
        quantidade: Number(formInsumo.quantidade),
        nivelCritico: Number(formInsumo.nivelCritico)
      });
      setModalInsumoAberto(false);
      setFormInsumo({ nome: '', unidade: 'g (gramas)', quantidade: 0, nivelCritico: 0 });
      carregarDados();
    } catch (error) {
      alert("Erro ao cadastrar insumo.");
    }
  };

  const handleCadastrarProduto = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await apiBomboniere.cadastrarProduto({
        nome: formProduto.nome,
        preco: Number(formProduto.preco),
        categoria: formProduto.categoria
      });
      setModalProdutoAberto(false);
      setFormProduto({ nome: '', preco: 0, categoria: 'SALGADO' });
      carregarDados();
    } catch (error) {
      alert("Erro ao cadastrar produto.");
    }
  };

  const handleAdicionarReceita = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!modalReceitaAberto.produtoId) return;
    try {
      await apiBomboniere.adicionarReceita(modalReceitaAberto.produtoId, {
        insumoId: formReceita.insumoId,
        quantidade: Number(formReceita.quantidade)
      });
      setModalReceitaAberto({ aberto: false, produtoId: null });
      setFormReceita({ insumoId: '', quantidade: 0 });
      carregarDados();
    } catch (error) {
      alert("Erro ao adicionar receita.");
    }
  };

  const handleReporEstoque = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!modalReporAberto.insumo) return;
    try {
      await apiBomboniere.reporEstoque(modalReporAberto.insumo.id, Number(formRepor.quantidade));
      setModalReporAberto({ aberto: false, insumo: null });
      setFormRepor({ quantidade: 0 });
      carregarDados(); // Recarrega para mostrar o estoque novo!
      alert("Estoque atualizado com sucesso!");
    } catch (error) {
      alert("Erro ao repor estoque.");
    }
  };

  // --- Estilos Baseados no Protótipo ---
  const cores = {
    vinho: '#8B0000',
    fundo: '#FAFAFA',
    borda: '#E0E0E0',
    verdeClaro: '#E8F5E9',
    verdeTexto: '#2E7D32',
    vermelhoClaro: '#FFEBEE',
    vermelhoTexto: '#C62828'
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif', backgroundColor: cores.fundo, minHeight: '100vh' }}>
      <h1 style={{ color: '#333' }}>Bomboniere</h1>
      <p style={{ color: '#666', marginBottom: '20px' }}>Gerenciar insumos e produtos</p>

      {/* TABS */}
      <div style={{ display: 'flex', borderBottom: `2px solid ${cores.borda}`, marginBottom: '20px' }}>
        <button 
          onClick={() => setAbaAtiva('insumos')}
          style={{ padding: '10px 20px', cursor: 'pointer', border: 'none', backgroundColor: abaAtiva === 'insumos' ? cores.vinho : 'transparent', color: abaAtiva === 'insumos' ? 'white' : '#333', fontWeight: 'bold', borderRadius: '8px 8px 0 0' }}>
          Insumos
        </button>
        <button 
          onClick={() => setAbaAtiva('produtos')}
          style={{ padding: '10px 20px', cursor: 'pointer', border: 'none', backgroundColor: abaAtiva === 'produtos' ? cores.vinho : 'transparent', color: abaAtiva === 'produtos' ? 'white' : '#333', fontWeight: 'bold', borderRadius: '8px 8px 0 0' }}>
          Produtos
        </button>
        <button 
          onClick={() => setAbaAtiva('alertas')}
          style={{ padding: '10px 20px', cursor: 'pointer', border: 'none', backgroundColor: abaAtiva === 'alertas' ? cores.vinho : 'transparent', color: abaAtiva === 'alertas' ? 'white' : '#333', fontWeight: 'bold', borderRadius: '8px 8px 0 0', position: 'relative' }}>
          Alertas de Estoque
          {alertas.length > 0 && (
            <span style={{ position: 'absolute', top: -5, right: -5, backgroundColor: 'red', color: 'white', borderRadius: '50%', padding: '2px 6px', fontSize: '12px' }}>{alertas.length}</span>
          )}
        </button>
      </div>

      {/* CONTEÚDO INSUMOS */}
      {abaAtiva === 'insumos' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '15px' }}>
            <button onClick={() => setModalInsumoAberto(true)} style={{ backgroundColor: cores.vinho, color: 'white', padding: '10px 15px', borderRadius: '5px', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>+ Cadastrar Insumo</button>
          </div>
          <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
            <thead style={{ borderBottom: `2px solid ${cores.borda}`, textAlign: 'left' }}>
              <tr>
                <th style={{ padding: '15px' }}>Nome</th>
                <th style={{ padding: '15px' }}>Unidade</th>
                <th style={{ padding: '15px' }}>Estoque Atual</th>
                <th style={{ padding: '15px' }}>Nível Crítico</th>
                <th style={{ padding: '15px' }}>Status</th>
                <th style={{ padding: '15px' }}>Ações</th>
              </tr>
            </thead>
            <tbody>
              {insumos.map(insumo => {
                const critico = insumo.quantidade <= insumo.nivelCritico;
                return (
                  <tr key={insumo.id} style={{ borderBottom: `1px solid ${cores.borda}` }}>
                    <td style={{ padding: '15px' }}>{insumo.nome}</td>
                    <td style={{ padding: '15px' }}>{insumo.unidade || 'UN'}</td>
                    <td style={{ padding: '15px', fontWeight: 'bold' }}>{insumo.quantidade}</td>
                    <td style={{ padding: '15px' }}>{insumo.nivelCritico}</td>
                    <td style={{ padding: '15px' }}>
                      <span style={{ padding: '5px 10px', borderRadius: '20px', fontSize: '12px', fontWeight: 'bold', backgroundColor: critico ? cores.vermelhoClaro : cores.verdeClaro, color: critico ? cores.vermelhoTexto : cores.verdeTexto }}>
                        {critico ? '⚠️ Crítico' : 'Normal'}
                      </span>
                    </td>
                    <td style={{ padding: '15px' }}>
                        <button onClick={() => setModalReporAberto({ aberto: true, insumo: insumo })} style={{ color: '#1976D2', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>Registrar Entrada</button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* CONTEÚDO PRODUTOS */}
      {abaAtiva === 'produtos' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '15px' }}>
            <button onClick={() => setModalProdutoAberto(true)} style={{ backgroundColor: cores.vinho, color: 'white', padding: '10px 15px', borderRadius: '5px', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>+ Cadastrar Produto</button>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
            {produtos.map(produto => (
              <div key={produto.id} style={{ backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)', border: `1px solid ${cores.borda}` }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <h3 style={{ margin: 0 }}>{produto.nome}</h3>
                  <span style={{ backgroundColor: cores.verdeClaro, color: cores.verdeTexto, padding: '3px 8px', borderRadius: '4px', fontSize: '12px', fontWeight: 'bold' }}>Ativo</span>
                </div>
                {produto.categoria && <span style={{ backgroundColor: '#E3F2FD', color: '#1565C0', padding: '3px 8px', borderRadius: '4px', fontSize: '10px', fontWeight: 'bold', display: 'inline-block', marginTop: '5px' }}>{produto.categoria}</span>}
                <h2 style={{ margin: '15px 0' }}>R$ {produto.preco.toFixed(2)}</h2>
                
                <div style={{ borderTop: `1px solid ${cores.borda}`, paddingTop: '10px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                    <span style={{ fontSize: '12px', color: '#666', fontWeight: 'bold' }}>Receita:</span>
                    <button onClick={() => setModalReceitaAberto({aberto: true, produtoId: produto.id})} style={{ color: '#1976D2', background: 'none', border: 'none', cursor: 'pointer', fontSize: '12px' }}>+ Adicionar Insumo</button>
                  </div>
                  <ul style={{ listStyle: 'none', padding: 0, margin: 0, fontSize: '14px', color: '#555' }}>
                    {/* Renderiza a receita caso venha no Response */}
                    {produto.receita && produto.receita.map((item: any, index: number) => (
                      <li key={index} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                        <span>{item.insumo.nome}: {item.quantidade}</span>
                        <span style={{cursor:'pointer', color: 'red'}}>🗑️</span>
                      </li>
                    ))}
                    {(!produto.receita || produto.receita.length === 0) && <li>Nenhuma receita cadastrada.</li>}
                  </ul>
                </div>
                <button style={{ width: '100%', marginTop: '15px', padding: '10px', backgroundColor: '#F5F5F5', border: `1px solid ${cores.borda}`, borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>Editar Produto</button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* CONTEÚDO ALERTAS */}
      {abaAtiva === 'alertas' && (
        <div style={{ backgroundColor: cores.vermelhoClaro, padding: '20px', borderRadius: '8px', border: '1px solid #FFCDD2' }}>
           {alertas.length === 0 ? <p style={{color: cores.vermelhoTexto}}>Nenhum alerta crítico no momento.</p> : (
             alertas.map(alerta => (
               <div key={alerta.id} style={{ marginBottom: '15px', paddingBottom: '15px', borderBottom: '1px solid #FFCDD2' }}>
                  <h3 style={{ color: cores.vermelhoTexto, margin: '0 0 10px 0' }}>⚠️ {alerta.nome}</h3>
                  <div style={{ display: 'flex', gap: '50px', marginBottom: '10px' }}>
                    <div><span style={{fontSize: '12px', color: '#666'}}>Quantidade Atual:</span><br/><strong>{alerta.quantidade}</strong></div>
                    <div><span style={{fontSize: '12px', color: '#666'}}>Nível Crítico:</span><br/><strong>{alerta.nivelCritico}</strong></div>
                  </div>
                  <button 
  onClick={() => setModalReporAberto({ aberto: true, insumo: alerta })} 
  style={{ backgroundColor: cores.vinho, color: 'white', padding: '8px 15px', borderRadius: '5px', border: 'none', cursor: 'pointer' }}
>
  Registrar Entrada de Estoque
</button>
               </div>
             ))
           )}
        </div>
      )}

      {/* --- MODAIS --- */}
      
      {/* Modal Insumo */}
      {modalInsumoAberto && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '10px', width: '400px' }}>
            <h2>Cadastrar Insumo</h2>
            <form onSubmit={handleCadastrarInsumo}>
              <label style={{ display: 'block', marginBottom: '10px' }}>Nome *
                <input required type="text" value={formInsumo.nome} onChange={e => setFormInsumo({...formInsumo, nome: e.target.value})} style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: `1px solid ${cores.borda}` }}/>
              </label>
              <div style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
                <label style={{ flex: 1 }}>Unidade *
                  <select value={formInsumo.unidade} onChange={e => setFormInsumo({...formInsumo, unidade: e.target.value})} style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: `1px solid ${cores.borda}` }}>
                    <option>g (gramas)</option><option>ml</option><option>unidades</option>
                  </select>
                </label>
                <label style={{ flex: 1 }}>Quantidade Inicial *
                  <input required type="number" step="0.01" value={formInsumo.quantidade} onChange={e => setFormInsumo({...formInsumo, quantidade: Number(e.target.value)})} style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: `1px solid ${cores.borda}` }}/>
                </label>
              </div>
              <label style={{ display: 'block', marginBottom: '20px' }}>Nível Crítico *
                <input required type="number" step="0.01" value={formInsumo.nivelCritico} onChange={e => setFormInsumo({...formInsumo, nivelCritico: Number(e.target.value)})} style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: `1px solid ${cores.borda}` }}/>
                <small style={{ fontSize: '10px', color: '#888' }}>Quando o estoque atingir este valor, um alerta será exibido.</small>
              </label>
              <div style={{ display: 'flex', gap: '10px' }}>
                <button type="button" onClick={() => setModalInsumoAberto(false)} style={{ flex: 1, padding: '10px', backgroundColor: 'transparent', border: `1px solid ${cores.borda}`, borderRadius: '5px', cursor: 'pointer' }}>Cancelar</button>
                <button type="submit" style={{ flex: 1, padding: '10px', backgroundColor: cores.vinho, color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>Cadastrar</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Produto */}
      {modalProdutoAberto && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '10px', width: '400px' }}>
            <h2>Cadastrar Produto</h2>
            <form onSubmit={handleCadastrarProduto}>
              <label style={{ display: 'block', marginBottom: '10px' }}>Nome *
                <input required type="text" value={formProduto.nome} onChange={e => setFormProduto({...formProduto, nome: e.target.value})} style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: `1px solid ${cores.borda}` }}/>
              </label>
              <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
                <label style={{ flex: 1 }}>Preço *
                  <input required type="number" step="0.01" value={formProduto.preco} onChange={e => setFormProduto({...formProduto, preco: Number(e.target.value)})} style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: `1px solid ${cores.borda}` }}/>
                </label>
                <label style={{ flex: 1 }}>Categoria *
                  <select value={formProduto.categoria} onChange={e => setFormProduto({...formProduto, categoria: e.target.value})} style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: `1px solid ${cores.borda}` }}>
                    <option>SALGADO</option><option>DOCE</option><option>BEBIDA</option><option>COMBO</option>
                  </select>
                </label>
              </div>
              <div style={{ display: 'flex', gap: '10px' }}>
                <button type="button" onClick={() => setModalProdutoAberto(false)} style={{ flex: 1, padding: '10px', backgroundColor: 'transparent', border: `1px solid ${cores.borda}`, borderRadius: '5px', cursor: 'pointer' }}>Cancelar</button>
                <button type="submit" style={{ flex: 1, padding: '10px', backgroundColor: cores.vinho, color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>Cadastrar</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Adicionar Receita */}
      {modalReceitaAberto.aberto && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '10px', width: '400px' }}>
            <h2>Adicionar Insumo à Receita</h2>
            <form onSubmit={handleAdicionarReceita}>
              <label style={{ display: 'block', marginBottom: '10px' }}>Insumo *
                <select required value={formReceita.insumoId} onChange={e => setFormReceita({...formReceita, insumoId: e.target.value})} style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: `1px solid ${cores.borda}` }}>
                  <option value="">Selecione um insumo</option>
                  {insumos.map(ins => <option key={ins.id} value={ins.id}>{ins.nome}</option>)}
                </select>
              </label>
              <label style={{ display: 'block', marginBottom: '20px' }}>Quantidade por Unidade Vendida *
                <input required type="number" step="0.01" value={formReceita.quantidade} onChange={e => setFormReceita({...formReceita, quantidade: Number(e.target.value)})} style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: `1px solid ${cores.borda}` }}/>
              </label>
              <div style={{ display: 'flex', gap: '10px' }}>
                <button type="button" onClick={() => setModalReceitaAberto({aberto: false, produtoId: null})} style={{ flex: 1, padding: '10px', backgroundColor: 'transparent', border: `1px solid ${cores.borda}`, borderRadius: '5px', cursor: 'pointer' }}>Cancelar</button>
                <button type="submit" style={{ flex: 1, padding: '10px', backgroundColor: cores.vinho, color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>Adicionar</button>
              </div>
            </form>
          </div>
        </div>
      )}
    {/* Modal Registrar Entrada de Estoque */}
      {modalReporAberto.aberto && modalReporAberto.insumo && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '10px', width: '400px' }}>
            <h2>Registrar Entrada de Estoque</h2>
            <div style={{ backgroundColor: cores.fundo, padding: '10px', borderRadius: '5px', marginBottom: '20px', border: `1px solid ${cores.borda}` }}>
              <strong>{modalReporAberto.insumo.nome}</strong>
              <p style={{ margin: '5px 0 0 0', fontSize: '14px', color: '#666' }}>Estoque atual: {modalReporAberto.insumo.quantidade} {modalReporAberto.insumo.unidade}</p>
            </div>
            
            <form onSubmit={handleReporEstoque}>
              <label style={{ display: 'block', marginBottom: '20px' }}>Quantidade a Adicionar *
                <input required type="number" step="0.01" value={formRepor.quantidade} onChange={e => setFormRepor({ quantidade: Number(e.target.value) })} style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: `1px solid ${cores.borda}` }}/>
              </label>
              <div style={{ display: 'flex', gap: '10px' }}>
                <button type="button" onClick={() => setModalReporAberto({ aberto: false, insumo: null })} style={{ flex: 1, padding: '10px', backgroundColor: 'transparent', border: `1px solid ${cores.borda}`, borderRadius: '5px', cursor: 'pointer' }}>Cancelar</button>
                <button type="submit" style={{ flex: 1, padding: '10px', backgroundColor: cores.vinho, color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>Registrar</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}