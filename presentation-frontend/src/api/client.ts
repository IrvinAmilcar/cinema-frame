import axios from 'axios'

// Durante desenvolvimento o Vite proxy redireciona /api → http://localhost:8080
// Em produção apontar para o endereço real do backend
const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

export default api
