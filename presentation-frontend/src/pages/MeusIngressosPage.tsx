import { useEffect, useState } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import type { ClienteLogado } from '../components/AuthModal'
import { useMoviePoster } from '../hooks/useMoviePoster'

const COR = '#1565C0'

const TIPO_SALA_LABEL: Record<string, string> = {
  PADRAO: 'Comum', TRES_D: '3D', IMAX: 'IMAX', VIP: 'VIP',
}
const TIPO_SALA_COR: Record<string, string> = {
  PADRAO: '#546e7a', TRES_D: '#1565c0', IMAX: '#6a1b9a', VIP: '#b8860b',
}

interface IngressoResponse {
  ingressoId: string
  qrCode: string
  filmeTitulo: string
  dataSessao: string
  horarioInicio: string
  horarioFim: string
  salaNumero: number
  tipoSala: string
  tipoIngresso: string
}

function formatarData(iso: string) {
  const [ano, mes, dia] = iso.split('-')
  return `${dia}/${mes}/${ano}`
}

function IngressoCard({ ingresso }: { ingresso: IngressoResponse }) {
  const { posterUrl } = useMoviePoster(ingresso.filmeTitulo)
  const [verQr, setVerQr] = useState(false)
  const corSala = TIPO_SALA_COR[ingresso.tipoSala] ?? COR
  const labelSala = TIPO_SALA_LABEL[ingresso.tipoSala] ?? ingresso.tipoSala

  return (
    <div style={{
      background: 'white', borderRadius: 16, overflow: 'hidden',
      border: '1px solid #f0f0f0', boxShadow: '0 2px 12px rgba(0,0,0,0.07)',
      display: 'flex', flexDirection: 'column',
    }}>
      {/* Poster */}
      <div style={{ height: 160, background: '#1a1a2e', position: 'relative', overflow: 'hidden' }}>
        {posterUrl
          ? <img src={posterUrl} alt={ingresso.filmeTitulo}
              style={{ width: '100%', height: '100%', objectFit: 'cover', opacity: 0.85 }} />
          : <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 48 }}>🎬</div>
        }
        {/* Badge tipo sala */}
        <div style={{
          position: 'absolute', top: 10, right: 10,
          background: corSala, color: 'white',
          fontSize: 11, fontWeight: 700, padding: '3px 8px', borderRadius: 6,
        }}>
          {labelSala}
        </div>
        {/* Badge tipo ingresso */}
        <div style={{
          position: 'absolute', top: 10, left: 10,
          background: ingresso.tipoIngresso === 'MEIA' ? '#e65100' : '#2e7d32',
          color: 'white', fontSize: 11, fontWeight: 700,
          padding: '3px 8px', borderRadius: 6,
        }}>
          {ingresso.tipoIngresso === 'MEIA' ? 'Meia' : 'Inteira'}
        </div>
      </div>

      {/* Info */}
      <div style={{ padding: '16px 18px', flex: 1, display: 'flex', flexDirection: 'column', gap: 8 }}>
        <div style={{ fontWeight: 700, fontSize: 15, color: '#1a1a2e', lineHeight: 1.3 }}>
          {ingresso.filmeTitulo}
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          <InfoRow icon='📅' text={formatarData(ingresso.dataSessao)} />
          <InfoRow icon='🕐' text={`${ingresso.horarioInicio} – ${ingresso.horarioFim}`} />
          <InfoRow icon='🎭' text={`Sala ${ingresso.salaNumero} · ${labelSala}`} />
        </div>

        {/* Divider pontilhado */}
        <div style={{ borderTop: '2px dashed #f0f0f0', margin: '4px 0' }} />

        <button onClick={() => setVerQr(v => !v)}
          style={{
            background: verQr ? '#f5f5f5' : COR, color: verQr ? '#555' : 'white',
            border: 'none', borderRadius: 10, padding: '10px 0',
            fontSize: 13, fontWeight: 600, cursor: 'pointer', width: '100%',
          }}>
          {verQr ? 'Ocultar QR Code' : '📲 Ver QR Code'}
        </button>

        {verQr && (
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8, padding: '8px 0' }}>
            <QRCodeSVG value={ingresso.qrCode} size={150} level="H" />
            <span style={{ fontFamily: 'monospace', fontSize: 10, color: '#aaa', wordBreak: 'break-all', textAlign: 'center' }}>
              {ingresso.qrCode}
            </span>
          </div>
        )}
      </div>
    </div>
  )
}

function InfoRow({ icon, text }: { icon: string; text: string }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, color: '#555' }}>
      <span style={{ fontSize: 14 }}>{icon}</span>
      {text}
    </div>
  )
}

export default function MeusIngressosPage({ cliente }: { cliente: ClienteLogado | null }) {
  const [ingressos, setIngressos] = useState<IngressoResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [erroDebug, setErroDebug] = useState('')

  useEffect(() => {
    if (!cliente) return
    const url = `/api/cliente/${cliente.clienteId}/ingressos`
    fetch(url)
      .then(r => {
        if (!r.ok) { setErroDebug(`HTTP ${r.status}`); setLoading(false); return [] }
        return r.json()
      })
      .then(data => { if (Array.isArray(data)) { setIngressos(data); setLoading(false) } })
      .catch(e => { setErroDebug(String(e)); setLoading(false) })
  }, [cliente?.clienteId])

  if (!cliente) {
    return (
      <div style={{ textAlign: 'center', padding: '60px 0', color: '#888' }}>
        <div style={{ fontSize: 48, marginBottom: 12 }}>🔒</div>
        <p style={{ fontSize: 15 }}>Faça login para ver seus ingressos.</p>
      </div>
    )
  }

  return (
    <div style={{ maxWidth: 900 }}>
      <div style={{ marginBottom: 28 }}>
        <h1 style={{ margin: 0, fontSize: 26, fontWeight: 700, color: '#1a1a2e' }}>🎟️ Meus Ingressos</h1>
        <p style={{ margin: '6px 0 0', color: '#888', fontSize: 14 }}>
          Ingressos válidos até o fim do dia.
        </p>
      </div>

      {erroDebug && (
        <div style={{ background: '#fff3cd', border: '1px solid #ffc107', borderRadius: 8, padding: '10px 14px', marginBottom: 16, fontSize: 13, color: '#856404' }}>
          Erro ao buscar ingressos: <strong>{erroDebug}</strong><br/>
          URL: <code>/api/cliente/{cliente.clienteId}/ingressos</code>
        </div>
      )}

      {loading && (
        <div style={{ color: '#aaa', fontSize: 14, padding: '40px 0', textAlign: 'center' }}>
          Carregando seus ingressos...
        </div>
      )}

      {!loading && ingressos.length === 0 && !erroDebug && (
        <div style={{ textAlign: 'center', padding: '60px 0', color: '#aaa' }}>
          <div style={{ fontSize: 48, marginBottom: 12 }}>🎬</div>
          <p style={{ fontSize: 15 }}>Você ainda não tem ingressos ativos.</p>
        </div>
      )}

      {!loading && ingressos.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: 20 }}>
          {ingressos.map(i => <IngressoCard key={i.ingressoId} ingresso={i} />)}
        </div>
      )}
    </div>
  )
}
