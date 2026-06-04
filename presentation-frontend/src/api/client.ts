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
  unidade: string;
  quantidadeEmEstoque: number; 
  nivelCritico: number;
}

export interface ProdutoBomboniereResponse {
  id: string;
  nome: string;
  preco: number;
  categoria?: string;
  ativo?: boolean;
  receita?: { insumoId: string; insumoNome: string; quantidade: number }[];
}
export interface InsumoRequest {
  nome: string;
  unidade: string;
  quantidade: number;
  nivelCritico: number;
}

export interface ProdutoRequest {
  nome: string;
  preco: number;
  categoria: string;
}

export interface ReceitaRequest {
  insumoId: string;
  quantidade: number;
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

  cadastrarInsumo: async (dados: InsumoRequest): Promise<void> => {
    const res = await fetch(`${API_URL}/bomboniere/insumos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dados)
    });
    if (!res.ok) throw new Error('Erro ao cadastrar insumo');
  },

  // ===== AQUI ESTÁ A FUNÇÃO QUE ESTAVA FALTANDO =====
  reporEstoque: async (id: string, quantidade: number): Promise<void> => {
    const res = await fetch(`${API_URL}/bomboniere/insumos/${id}/repor`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ quantidade })
    });
    if (!res.ok) throw new Error('Erro ao repor estoque');
  },

  // ==========================
  // PRODUTOS E RECEITAS
  // ==========================
  listarProdutos: async (): Promise<ProdutoBomboniereResponse[]> => {
    const res = await fetch(`${API_URL}/bomboniere/produtos`);
    if (!res.ok) throw new Error('Erro ao listar produtos');
    return res.json();
  },

  cadastrarProduto: async (dados: ProdutoRequest): Promise<void> => {
    const res = await fetch(`${API_URL}/bomboniere/produtos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dados)
    });
    if (!res.ok) throw new Error('Erro ao cadastrar produto');
  },

  adicionarReceita: async (produtoId: string, dados: ReceitaRequest): Promise<void> => {
    const res = await fetch(`${API_URL}/bomboniere/produtos/${produtoId}/receita`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dados)
    });
    if (!res.ok) throw new Error('Erro ao adicionar receita');
  },


  editarProduto: async (id: string, dados: ProdutoRequest): Promise<void> => {
    const res = await fetch(`${API_URL}/bomboniere/produtos/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dados)
    });
    if (!res.ok) throw new Error('Erro ao editar produto');
},

removerItemReceita: async (produtoId: string, insumoId: string): Promise<void> => {
    const res = await fetch(`${API_URL}/bomboniere/produtos/${produtoId}/receita/${insumoId}`, {
        method: 'DELETE'
    });
    if (!res.ok) throw new Error('Erro ao remover item da receita');
},

  toggleAtivo: async (id: string): Promise<ProdutoBomboniereResponse> => {
    const res = await fetch(`${API_URL}/bomboniere/produtos/${id}/toggle-ativo`, {
      method: 'PATCH',
    });
    if (!res.ok) throw new Error('Erro ao alterar status do produto');
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

// ==========================
// USUÁRIOS E PERMISSÕES (RBAC)
// ==========================

export interface FuncionarioResponse {
  id: string;
  nome: string;
  email: string;
  role: string;
  ativo: boolean;
  permissoes: string[];
}

export interface FuncionarioRequest {
  nome: string;
  email: string;
  role: string;
  ativo: boolean;
  permissoes: string[];
}

export const apiUsuarios = {
  listarTodos: async (): Promise<FuncionarioResponse[]> => {
    const res = await fetch(`${API_URL}/funcionarios`);
    if (!res.ok) throw new Error('Erro ao buscar funcionários');
    return res.json();
  },
  
  buscarPorId: async (id: string): Promise<FuncionarioResponse> => {
    const res = await fetch(`${API_URL}/funcionarios/${id}`);
    if (!res.ok) throw new Error('Erro ao buscar funcionário');
    return res.json();
  },
  
  cadastrar: async (dados: FuncionarioRequest): Promise<FuncionarioResponse> => {
    const res = await fetch(`${API_URL}/funcionarios`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dados)
    });
    if (!res.ok) throw new Error('Erro ao cadastrar funcionário');
    return res.json();
  },
  
  atualizar: async (id: string, dados: FuncionarioRequest): Promise<FuncionarioResponse> => {
    const res = await fetch(`${API_URL}/funcionarios/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dados)
    });
    if (!res.ok) throw new Error('Erro ao atualizar funcionário');
    return res.json();
  },
  
  excluir: async (id: string): Promise<void> => {
    const res = await fetch(`${API_URL}/funcionarios/${id}`, { 
      method: 'DELETE' 
    });
    if (!res.ok) throw new Error('Erro ao excluir funcionário');
  }
};
// ==========================
// FECHAMENTO DE CAIXA
// ==========================

export interface VendaDiaRequest {
  sessaoId: string;
  capacidadeSala: number;
  ingressosVendidos: number;
  valorArrecadado: number;
}

export interface FecharCaixaRequest {
  data: string;
  momentoFechamento: string;
  vendas: VendaDiaRequest[];
}

export interface FechamentoCaixaResponse {
  id: string;
  data: string;
  totalVendas: number;
  totalIngressos: number;
  totalSessoes: number;
  taxaOcupacaoMedia: number;
  momentoFechamento: string;
}

export const apiCaixa = {
  fecharCaixa: async (dados: FecharCaixaRequest): Promise<FechamentoCaixaResponse> => {
    const res = await fetch(`${API_URL}/caixa/fechar`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dados),
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json.mensagem || 'Erro ao fechar o caixa');
    return json;
  },

  consultarRelatorio: async (data: string): Promise<FechamentoCaixaResponse> => {
    const res = await fetch(`${API_URL}/caixa/relatorio?data=${data}`);
    const json = await res.json();
    if (!res.ok) throw new Error(json.mensagem || 'Nenhum fechamento encontrado para essa data');
    return json;
  },

  ocupacaoPorSessao: async (dataInicio: string, dataFim: string): Promise<{
    sessaoId: string;
    filme: string;
    horario: string;
    salaNumero: number;
    salaTipo: string;
    capacidade: number;
    ingressosVendidos: number;
    diasVigencia: number;
  }[]> => {
    const res = await fetch(`${API_URL}/caixa/ocupacao-por-sessao?dataInicio=${dataInicio}&dataFim=${dataFim}`);
    if (!res.ok) throw new Error('Erro ao buscar ocupação por sessão');
    return res.json();
  },

  bombonierePorSessao: async (dataInicio: string, dataFim: string): Promise<{
    sessaoId: string;
    filme: string;
    horario: string;
    salaNumero: number;
    salaTipo: string;
    produto: string;
    quantidade: number;
    valorTotal: number;
  }[]> => {
    const res = await fetch(`${API_URL}/caixa/bomboniere-por-sessao?dataInicio=${dataInicio}&dataFim=${dataFim}`);
    if (!res.ok) throw new Error('Erro ao buscar bomboniere por sessão');
    return res.json();
  },

  ingressosPorSessao: async (dataInicio: string, dataFim: string): Promise<{
    sessaoId: string;
    filme: string;
    horario: string;
    salaNumero: number;
    salaTipo: string;
    totalIngressos: number;
    totalInteira: number;
    valorTotal: number;
  }[]> => {
    const res = await fetch(`${API_URL}/caixa/ingressos-por-sessao?dataInicio=${dataInicio}&dataFim=${dataFim}`);
    if (!res.ok) throw new Error('Erro ao buscar ingressos por sessão');
    return res.json();
  },

  resumoPorPeriodo: async (dataInicio: string, dataFim: string): Promise<{
    totalIngressos: number;
    totalIngressosValor: number;
    totalBomboniere: number;
    totalDescontoPontos: number;
    receitaTotal: number;
  }> => {
    const res = await fetch(`${API_URL}/caixa/resumo?dataInicio=${dataInicio}&dataFim=${dataFim}`);
    if (!res.ok) throw new Error('Erro ao buscar resumo do período');
    return res.json();
  },
};