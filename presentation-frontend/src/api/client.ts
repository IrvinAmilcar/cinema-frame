import axios from 'axios'

// Durante desenvolvimento o Vite proxy redireciona /api → http://localhost:8080
// Em produção apontar para o endereço real do backend
const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

export default api

export interface InsumoResponse {
  id: string;
  nome: string;
  quantidade: number; 
  nivelCritico: number;
}

export interface ProdutoBomboniereResponse {
  id: string;
  nome: string;
  preco: number;
}

const API_URL = 'http://localhost:8080'; 

export const apiBomboniere = {
  // ==========================
  // INSUMOS E ALERTAS
  // ==========================
  listarInsumos: async (): Promise<InsumoResponse[]> => {
    const res = await fetch(`${API_URL}/bomboniere/insumos`);
    if (!res.ok) throw new Error('Erro ao listar insumos');
    return res.json();
  },

  listarAlertas: async (): Promise<InsumoResponse[]> => {
    const res = await fetch(`${API_URL}/bomboniere/alertas`);
    if (!res.ok) throw new Error('Erro ao buscar alertas');
    return res.json();
  },

  // ==========================
  // PRODUTOS
  // ==========================
  listarProdutos: async (): Promise<ProdutoBomboniereResponse[]> => {
    const res = await fetch(`${API_URL}/bomboniere/produtos`);
    if (!res.ok) throw new Error('Erro ao listar produtos');
    return res.json();
  },

  // ==========================
  // VENDAS
  // ==========================
  venderProduto: async (produtoId: string): Promise<string> => {
    const res = await fetch(`${API_URL}/bomboniere/produtos/${produtoId}/vender`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Erro ao realizar a venda');
    return res.text(); 
  }
};
