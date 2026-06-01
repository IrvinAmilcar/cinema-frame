import { useState, useEffect } from 'react';
import { apiUsuarios, FuncionarioResponse } from '../api/client';

// Mapeamento de Roles (Backend <-> Frontend)
const ROLES = [
  { value: 'GERENTE', label: 'Gerente', desc: 'Gerente possui acesso total ao sistema.' },
  { value: 'OPERADOR_DE_CAIXA', label: 'Operador de Caixa', desc: 'Operador possui acesso limitado.' }
];

// Mapeamento de Permissões
const PERMISSOES_UI = [
  { id: 'ESTORNAR_VENDA', label: 'Permitir Estorno de Venda', shortLabel: 'Estorno de Venda', desc: 'Autoriza cancelamento de ingressos e vendas' },
  { id: 'ALTERAR_PRECO', label: 'Permitir Alteração Manual de Preços', shortLabel: 'Alterar Preços', desc: 'Modificar valores de ingressos e produtos' },
  { id: 'GERENCIAR_USUARIOS', label: 'Gerenciar Usuários', shortLabel: 'Gerenciar Usuários', desc: 'Criar, editar e remover funcionários' },
  { id: 'GERENCIAR_CATALOGO', label: 'Gerenciar Catálogo de Filmes', shortLabel: 'Gerenciar Catálogo', desc: 'Adicionar, editar e remover filmes' },
  { id: 'GERENCIAR_GRADE', label: 'Gerenciar Grade de Exibição', shortLabel: 'Gerenciar Grade', desc: 'Criar e modificar sessões' },
  { id: 'ACESSAR_RELATORIOS', label: 'Acessar Relatórios Financeiros', shortLabel: 'Acessar Relatórios', desc: 'Visualizar fechamento de caixa e relatórios' }
];

// Paleta de Cores do F.R.A.M.E
const cores = {
  vinho: '#8B0000', fundo: '#F8F9FA', borda: '#EBEBEB', texto: '#1a1a2e', subtexto: '#6c757d',
  verdeClaro: '#E8F5E9', verdeTexto: '#2E7D32', vermelhoClaro: '#FFEBEE', vermelhoTexto: '#C62828'
};

// Componente visual do Switch (Chavinha Liga/Desliga)
const Switch = ({ checked, onChange }: { checked: boolean; onChange: () => void }) => (
  <label style={{ position: 'relative', display: 'inline-block', width: '44px', height: '24px' }}>
    <input type="checkbox" checked={checked} onChange={onChange} style={{ opacity: 0, width: 0, height: 0 }} />
    <span style={{
      position: 'absolute', cursor: 'pointer', top: 0, left: 0, right: 0, bottom: 0,
      backgroundColor: checked ? '#00A859' : '#D1D5DB', borderRadius: '24px', transition: '.3s'
    }}>
      <span style={{
        position: 'absolute', height: '18px', width: '18px', left: checked ? '22px' : '3px',
        bottom: '3px', backgroundColor: 'white', borderRadius: '50%', transition: '.3s', boxShadow: '0 1px 3px rgba(0,0,0,0.3)'
      }} />
    </span>
  </label>
);

export default function UsuariosPage() {
  const [funcionarios, setFuncionarios] = useState<FuncionarioResponse[]>([]);
  
  // Controle de Interface
  const [usuarioSelecionado, setUsuarioSelecionado] = useState<FuncionarioResponse | null>(null);
  const [modalAberto, setModalAberto] = useState<'CADASTRAR' | 'EDITAR' | null>(null);

  // Estado do Formulário
  const formInicial = { nome: '', email: '', role: 'OPERADOR', ativo: true, permissoes: [] as string[] };
  const [formFunc, setFormFunc] = useState(formInicial);

  useEffect(() => {
    carregarUsuarios();
  }, []);

  const carregarUsuarios = async () => {
    try {
      const data = await apiUsuarios.listarTodos();
      setFuncionarios(data);
      // Atualiza o painel lateral caso o usuário selecionado tenha sido editado
      if (usuarioSelecionado) {
        const atualizado = data.find(f => f.id === usuarioSelecionado.id);
        setUsuarioSelecionado(atualizado || null);
      }
    } catch (error) {
      console.error(error);
    }
  };

  const togglePermissao = (permissaoId: string) => {
    setFormFunc(prev => {
      const possui = prev.permissoes.includes(permissaoId);
      return {
        ...prev,
        permissoes: possui ? prev.permissoes.filter(p => p !== permissaoId) : [...prev.permissoes, permissaoId]
      };
    });
  };

  const handleChangeRole = (novoRole: string) => {
    setFormFunc(prev => {
      if (novoRole === 'GERENTE') {
        // Se mudou para Gerente, pega todos os IDs de permissões e marca automaticamente
        const todasPermissoes = PERMISSOES_UI.map(p => p.id);
        return { ...prev, role: novoRole, permissoes: todasPermissoes };
      } else {
        // Se mudou para Operador, limpa as permissões por segurança
        return { ...prev, role: novoRole, permissoes: [] };
      }
    });
  };

  const handleSalvar = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (modalAberto === 'CADASTRAR') {
        await apiUsuarios.cadastrar(formFunc);
      } else if (modalAberto === 'EDITAR' && usuarioSelecionado) {
        await apiUsuarios.atualizar(usuarioSelecionado.id, formFunc);
      }
      setModalAberto(null);
      setFormFunc(formInicial);
      carregarUsuarios();
    } catch (error) {
      alert('Erro ao salvar funcionário.');
    }
  };

  const handleExcluir = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation(); // Evita que o clique abra o painel lateral
    if (window.confirm('Tem certeza que deseja desativar este usuário?')) {
      try {
        await apiUsuarios.excluir(id);
        if (usuarioSelecionado?.id === id) setUsuarioSelecionado(null);
        carregarUsuarios();
      } catch (error) {
        alert('Erro ao excluir funcionário.');
      }
    }
  };

  const abrirEdicao = () => {
    if (!usuarioSelecionado) return;
    setFormFunc({ 
      nome: usuarioSelecionado.nome, 
      email: usuarioSelecionado.email, 
      role: usuarioSelecionado.role, 
      ativo: usuarioSelecionado.ativo, 
      permissoes: usuarioSelecionado.permissoes 
    });
    setModalAberto('EDITAR');
  };

  return (
    <div style={{ display: 'flex', height: '100%', backgroundColor: cores.fundo, fontFamily: 'sans-serif', margin: '-1.5rem', padding: '2rem' }}>
      
      {/* Lado Esquerdo: Lista */}
      <div style={{ flex: 1, marginRight: usuarioSelecionado ? '20px' : '0', transition: 'margin 0.3s' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '30px' }}>
          <div>
            <h1 style={{ color: cores.texto, margin: '0 0 8px 0', fontSize: '24px' }}>Gestão de Usuários</h1>
            <p style={{ color: cores.subtexto, margin: 0 }}>Controle de permissões e funções (RBAC)</p>
          </div>
          <button 
            onClick={() => { setFormFunc(formInicial); setModalAberto('CADASTRAR'); }} 
            style={{ backgroundColor: cores.vinho, color: 'white', padding: '12px 20px', borderRadius: '8px', border: 'none', cursor: 'pointer', fontWeight: 600, fontSize: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontSize: '18px', fontWeight: 'normal' }}>+</span> Cadastrar Funcionário
          </button>
        </div>

        <div style={{ backgroundColor: 'white', borderRadius: '12px', border: `1px solid ${cores.borda}`, overflow: 'hidden' }}>
          <div style={{ padding: '20px', borderBottom: `1px solid ${cores.borda}`, display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{ fontSize: '18px' }}>👥</span>
            <h3 style={{ margin: 0, color: cores.texto, fontSize: '16px' }}>Funcionários</h3>
          </div>
          
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            {funcionarios.map(func => {
              const roleInfo = ROLES.find(r => r.value === func.role) || ROLES[1];
              const isGerente = func.role === 'GERENTE';
              const selecionado = usuarioSelecionado?.id === func.id;

              return (
                <div 
                  key={func.id} 
                  onClick={() => setUsuarioSelecionado(func)}
                  style={{ 
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center', 
                    padding: '16px 20px', borderBottom: `1px solid ${cores.borda}`, 
                    cursor: 'pointer', backgroundColor: selecionado ? '#F4F6F8' : 'white',
                    transition: 'background 0.2s'
                  }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
                      <span style={{ fontWeight: 600, color: cores.texto }}>{func.nome}</span>
                      {!func.ativo && (
                        <span style={{ backgroundColor: cores.vermelhoClaro, color: cores.vermelhoTexto, padding: '2px 8px', borderRadius: '4px', fontSize: '10px', fontWeight: 'bold' }}>
                          INATIVO
                        </span>
                      )}
                    </div>
                    <div style={{ color: cores.subtexto, fontSize: '14px' }}>{func.email}</div>
                  </div>
                  
                  <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <div style={{ 
                      display: 'flex', alignItems: 'center', gap: '6px', 
                      backgroundColor: isGerente ? '#F3E5F5' : '#F5F5F5', 
                      color: isGerente ? '#7B1FA2' : '#424242', 
                      padding: '6px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: 600 
                    }}>
                      <svg width="12" height="14" viewBox="0 0 12 14" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M6 0L0 2.66667V6.66667C0 10.3667 2.56667 13.82 6 14C9.43333 13.82 12 10.3667 12 6.66667V2.66667L6 0ZM6 12.5867C3.48667 12.3933 1.33333 9.7 1.33333 6.66667V3.62667L6 1.55333L10.6667 3.62667V6.66667C10.6667 9.7 8.51333 12.3933 6 12.5867Z" fill="currentColor"/>
                      </svg>
                      {roleInfo.label}
                    </div>
                    <button onClick={(e) => handleExcluir(func.id, e)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: cores.vermelhoTexto, padding: '4px' }} title="Excluir">
                      <svg width="16" height="18" viewBox="0 0 14 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M11 5.5V14H3V5.5H11ZM9.5 0H4.5L3.5 1H0V2.5H14V1H10.5L9.5 0ZM12.5 4H1.5V14C1.5 14.825 2.175 15.5 3 15.5H11C11.825 15.5 12.5 14.825 12.5 14V4Z" fill="currentColor"/>
                      </svg>
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      </div>

      {/* Lado Direito: Painel Lateral de Detalhes */}
      {usuarioSelecionado && (
        <div style={{ width: '380px', backgroundColor: 'white', borderRadius: '12px', border: `1px solid ${cores.borda}`, padding: '24px', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
            <h3 style={{ margin: 0, fontSize: '18px', color: cores.texto }}>Visualizar Permissões</h3>
            <div style={{ display: 'flex', gap: '10px' }}>
              <button onClick={abrirEdicao} style={{ backgroundColor: '#E3F2FD', color: '#1976D2', border: 'none', padding: '6px 16px', borderRadius: '6px', fontWeight: 600, cursor: 'pointer' }}>Editar</button>
              <button onClick={() => setUsuarioSelecionado(null)} style={{ background: 'none', border: 'none', fontSize: '20px', color: '#888', cursor: 'pointer' }}>✕</button>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '30px' }}>
            <div style={{ width: '60px', height: '60px', backgroundColor: cores.vinho, color: 'white', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '24px', marginBottom: '8px' }}>
              {usuarioSelecionado.nome.charAt(0).toUpperCase()}
            </div>
            <h2 style={{ margin: 0, fontSize: '18px', color: cores.texto }}>{usuarioSelecionado.nome}</h2>
            <span style={{ color: cores.subtexto, fontSize: '14px' }}>{usuarioSelecionado.email}</span>
            <div style={{ alignSelf: 'flex-start', backgroundColor: usuarioSelecionado.role === 'GERENTE' ? '#F3E5F5' : '#F5F5F5', color: usuarioSelecionado.role === 'GERENTE' ? '#7B1FA2' : '#424242', padding: '4px 10px', borderRadius: '4px', fontSize: '12px', fontWeight: 600, marginTop: '4px' }}>
              {ROLES.find(r => r.value === usuarioSelecionado.role)?.label || usuarioSelecionado.role}
            </div>
          </div>

          <h4 style={{ margin: '0 0 16px 0', fontSize: '14px', color: cores.texto }}>Permissões Ativas</h4>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', overflowY: 'auto' }}>
            {PERMISSOES_UI.map(perm => {
              const temPermissao = usuarioSelecionado.permissoes.includes(perm.id);
              return (
                <div key={perm.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', backgroundColor: cores.fundo, padding: '12px 16px', borderRadius: '8px' }}>
                  <span style={{ fontSize: '13px', color: '#424242' }}>{perm.shortLabel}</span>
                  <span style={{ 
                    backgroundColor: temPermissao ? cores.verdeClaro : cores.vermelhoClaro, 
                    color: temPermissao ? cores.verdeTexto : cores.vermelhoTexto, 
                    padding: '4px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: 500 
                  }}>
                    {temPermissao ? 'Permitido' : 'Negado'}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Modal Adicionar / Editar Funcionario */}
      {modalAberto && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.4)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}>
          <div style={{ backgroundColor: 'white', borderRadius: '12px', width: '600px', display: 'flex', flexDirection: 'column', maxHeight: '90vh' }}>
            
            <div style={{ padding: '24px', borderBottom: `1px solid ${cores.borda}`, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2 style={{ margin: 0, fontSize: '20px', color: cores.texto }}>
                {modalAberto === 'CADASTRAR' ? 'Cadastrar Novo Funcionário' : 'Editar Funcionário'}
              </h2>
              <button onClick={() => setModalAberto(null)} style={{ background: 'none', border: 'none', fontSize: '20px', color: '#888', cursor: 'pointer' }}>✕</button>
            </div>

            <div style={{ padding: '24px', overflowY: 'auto', flex: 1 }}>
              <form id="formUsuario" onSubmit={handleSalvar}>
                
                <div style={{ display: 'flex', gap: '20px', marginBottom: '20px' }}>
                  <div style={{ flex: 1 }}>
                    <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: 500, color: cores.texto }}>Nome Completo *</label>
                    <input required type="text" value={formFunc.nome} onChange={e => setFormFunc({...formFunc, nome: e.target.value})} style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', border: `1px solid #CCC`, fontSize: '14px', boxSizing: 'border-box' }} placeholder="Ex: Carlos Silva" />
                  </div>
                  <div style={{ flex: 1 }}>
                    <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: 500, color: cores.texto }}>E-mail *</label>
                    <input required type="email" value={formFunc.email} onChange={e => setFormFunc({...formFunc, email: e.target.value})} style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', border: `1px solid #CCC`, fontSize: '14px', boxSizing: 'border-box' }} placeholder="carlos.silva@cinema.com" />
                  </div>
                </div>

                <div style={{ marginBottom: '30px' }}>
                  <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: 500, color: cores.texto }}>Função (Role) *</label>
                  
                  <select 
                        value={formFunc.role} 
                        onChange={e => handleChangeRole(e.target.value)}
                        style={{ 
                            width: '100%', 
                            padding: '10px 12px', 
                            borderRadius: '8px', 
                            border: '1px solid #CCC', 
                            fontSize: '14px', 
                            color: cores.texto,
                            backgroundColor: 'white',
                            cursor: 'pointer'
                        }}
                        >
                        {ROLES.map(r => <option key={r.value} value={r.value}>{r.label}</option>)}
                        </select>
                  
                  <p style={{ margin: '8px 0 0 0', fontSize: '13px', color: cores.subtexto }}>
                    {ROLES.find(r => r.value === formFunc.role)?.desc}
                  </p>
                </div>

                <h4 style={{ margin: '0 0 16px 0', fontSize: '15px', color: cores.texto, fontWeight: 600 }}>Permissões Específicas</h4>
                
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '24px' }}>
                  {PERMISSOES_UI.map(perm => (
                    <div key={perm.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', backgroundColor: cores.fundo, padding: '16px', borderRadius: '8px' }}>
                      <div>
                        <div style={{ fontSize: '14px', fontWeight: 500, color: cores.texto, marginBottom: '4px' }}>{perm.label}</div>
                        <div style={{ fontSize: '13px', color: cores.subtexto }}>{perm.desc}</div>
                      </div>
                      <Switch checked={formFunc.permissoes.includes(perm.id)} onChange={() => togglePermissao(perm.id)} />
                    </div>
                  ))}
                </div>

                {modalAberto === 'EDITAR' && (
                  <label style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '20px', cursor: 'pointer' }}>
                    <input type="checkbox" checked={formFunc.ativo} onChange={e => setFormFunc({...formFunc, ativo: e.target.checked})} style={{ width: '18px', height: '18px', accentColor: cores.vinho }} />
                    <span style={{ fontSize: '15px', fontWeight: 500, color: cores.texto }}>Usuário ativo (pode acessar o sistema)</span>
                  </label>
                )}

              </form>
            </div>

            <div style={{ padding: '24px', borderTop: `1px solid ${cores.borda}`, display: 'flex', gap: '16px' }}>
              <button type="button" onClick={() => setModalAberto(null)} style={{ flex: 1, padding: '12px', backgroundColor: 'white', border: `1px solid #CCC`, borderRadius: '8px', cursor: 'pointer', fontWeight: 600, color: cores.texto }}>Cancelar</button>
              <button type="submit" form="formUsuario" style={{ flex: 1, padding: '12px', backgroundColor: cores.vinho, color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>
                {modalAberto === 'CADASTRAR' ? 'Cadastrar' : 'Atualizar'}
              </button>
            </div>

          </div>
        </div>
      )}

    </div>
  );
}