import { useEffect, useState, useMemo } from 'react'
import { listarMultas, condonarMulta } from '../api/multas.js'
import { useAuth } from '../auth/AuthContext.jsx'
import {
  AlertTriangle,
  Search,
  CheckCircle2,
  AlertCircle,
  X,
  RefreshCw,
  ShieldAlert,
  Clock,
  Check,
  ShieldCheck
} from 'lucide-react'

const FILTROS = ['', 'acumulando', 'activa', 'cumplida', 'condonada']
const PUEDE_CONDONAR = (rol) =>
  rol === 'administrador' || rol === 'bibliotecario' || rol === 'almacenista'

function Multas() {
  const { user } = useAuth()
  const [multas, setMultas] = useState([])
  const [estado, setEstado] = useState('')
  const [busqueda, setBusqueda] = useState('')
  const [condonando, setCondonando] = useState(null)
  const [message, setMessage] = useState({ type: '', text: '' })
  const [cargando, setCargando] = useState(true)

  const puedeCondonar = PUEDE_CONDONAR(user?.rol)

  const cargar = async () => {
    setCargando(true)
    try {
      const res = await listarMultas(estado || undefined)
      setMultas(res || [])
    } catch (e) {
      setMessage({ type: 'error', text: e.message })
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [estado])

  const multasFiltradas = useMemo(() => {
    return multas.filter((m) => {
      if (!busqueda) return true
      const b = busqueda.toLowerCase()
      return (
        m.usuarioNombre?.toLowerCase().includes(b) ||
        m.recursoNombre?.toLowerCase().includes(b) ||
        String(m.id).includes(b)
      )
    })
  }, [multas, busqueda])

  const stats = useMemo(() => {
    return {
      total: multas.length,
      activas: multas.filter((m) => m.estado === 'activa').length,
      acumulando: multas.filter((m) => m.estado === 'acumulando').length,
      cumplidas: multas.filter((m) => m.estado === 'cumplida').length,
    }
  }, [multas])

  const handleCondonar = async (e) => {
    e.preventDefault()
    const observacion = e.target.observacion.value
    if (!observacion.trim()) return
    try {
      await condonarMulta(condonando.id, observacion)
      setMessage({ type: 'success', text: `Sanción #${condonando.id} condonada con éxito` })
      setCondonando(null)
      cargar()
    } catch (err) {
      setMessage({ type: 'error', text: err.message })
    }
  }

  return (
    <div className="animate-fade-in">
      {/* Notifications */}
      {message.text && (
        <div className={`alert ${message.type}`}>
          {message.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
          <span style={{ flex: 1, fontWeight: 600 }}>{message.text}</span>
          <button
            onClick={() => setMessage({ type: '', text: '' })}
            style={{ background: 'transparent', border: 'none', color: 'inherit', padding: 0, boxShadow: 'none' }}
          >
            <X size={16} />
          </button>
        </div>
      )}

      {/* KPI Stats */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', marginBottom: 20 }}>
        <div className="stat-card">
          <div className="stat-icon-wrapper rose">
            <ShieldAlert size={22} />
          </div>
          <div className="stat-info">
            <div className="stat-label">Total Sanciones</div>
            <div className="stat-value">{stats.total}</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper rose">
            <AlertTriangle size={22} />
          </div>
          <div className="stat-info">
            <div className="stat-label">Sanciones Activas</div>
            <div className="stat-value">{stats.activas}</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper amber">
            <Clock size={22} />
          </div>
          <div className="stat-info">
            <div className="stat-label">Acumulando Días</div>
            <div className="stat-value">{stats.acumulando}</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper emerald">
            <CheckCircle2 size={22} />
          </div>
          <div className="stat-info">
            <div className="stat-label">Sanciones Cumplidas</div>
            <div className="stat-value">{stats.cumplidas}</div>
          </div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="filter-bar">
        <div className="search-box">
          <Search size={16} className="search-icon" />
          <input
            type="text"
            placeholder="Buscar por usuario, libro o equipo..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
        </div>

        <div className="tab-pills">
          {FILTROS.map((f) => (
            <button
              key={f}
              className={`tab-pill-btn ${estado === f ? 'active' : ''}`}
              onClick={() => setEstado(f)}
            >
              {f ? f.charAt(0).toUpperCase() + f.slice(1) : 'Todas'}
            </button>
          ))}
        </div>

        <button className="secondary" onClick={cargar} title="Recargar">
          <RefreshCw size={15} className={cargando ? 'animate-spin' : ''} />
        </button>
      </div>

      {/* Table of Fines */}
      <div className="table-container">
        <div className="table-responsive">
          <table>
            <thead>
              <tr>
                <th style={{ width: 70 }}>ID</th>
                <th>Recurso Sancionado</th>
                {puedeCondonar && <th>Usuario Sancionado</th>}
                <th>Días de Retraso</th>
                <th>Días Suspensión</th>
                <th>Estado</th>
                <th>Fin de Suspensión</th>
                {!puedeCondonar && <th>Observación / Motivo</th>}
                {puedeCondonar && <th style={{ textAlign: 'right' }}>Acciones</th>}
              </tr>
            </thead>
            <tbody>
              {cargando && multasFiltradas.length === 0 ? (
                <tr>
                  <td colSpan={puedeCondonar ? 8 : 7}>
                    <div style={{ padding: '40px 16px', textAlign: 'center', color: 'var(--text-muted)' }}>
                      <RefreshCw size={26} className="animate-spin" style={{ margin: '0 auto 10px', color: 'var(--primary)' }} />
                      <div style={{ fontSize: 13.5, fontWeight: 600 }}>Cargando sanciones y multas...</div>
                    </div>
                  </td>
                </tr>
              ) : multasFiltradas.length === 0 ? (
                <tr>
                  <td colSpan={puedeCondonar ? 8 : 7}>
                    <div className="empty-state">
                      <div className="empty-state-icon">
                        <CheckCircle2 size={24} color="var(--emerald-600)" />
                      </div>
                      <h4>No hay sanciones registradas</h4>
                      <p>
                        {puedeCondonar
                          ? 'No se encontraron multas en el sistema con el filtro seleccionado.'
                          : '¡Excelente! No tienes multas ni suspensiones activas en tu cuenta.'}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                multasFiltradas.map((m) => (
                  <tr key={m.id}>
                    <td style={{ fontWeight: 700, color: 'var(--text-muted)' }}>#{m.id}</td>
                    <td>
                      <div style={{ fontWeight: 700, color: 'var(--text-main)', fontSize: 14 }}>
                        {m.recursoNombre || 'Recurso'}
                      </div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                        {m.tipoRecurso || 'Biblioteca SENA'}
                      </div>
                    </td>
                    {puedeCondonar && (
                      <td>
                        <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{m.usuarioNombre}</div>
                      </td>
                    )}
                    <td>
                      <span style={{ color: 'var(--rose-800)', background: 'var(--rose-50)', border: '1px solid var(--rose-200)', padding: '2px 8px', borderRadius: 6, fontWeight: 700, fontSize: 12 }}>
                        {m.diasRetraso} {m.diasRetraso === 1 ? 'día' : 'días'}
                      </span>
                    </td>
                    <td>
                      <span style={{ fontWeight: 700, color: 'var(--text-main)' }}>
                        {m.diasSuspension} {m.diasSuspension === 1 ? 'día' : 'días'}
                      </span>
                    </td>
                    <td>
                      <span className={`badge ${m.estado}`}>{m.estado}</span>
                    </td>
                    <td style={{ fontSize: 13 }}>
                      {m.fechaFinSuspension ? (
                        <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                          {new Date(m.fechaFinSuspension).toLocaleDateString()}
                        </span>
                      ) : (
                        '—'
                      )}
                    </td>
                    {!puedeCondonar && (
                      <td style={{ fontSize: 13, color: 'var(--text-secondary)' }}>
                        {m.observacion || '—'}
                      </td>
                    )}
                    {puedeCondonar && (
                      <td style={{ textAlign: 'right' }}>
                        {(m.estado === 'acumulando' || m.estado === 'activa') && (
                          <button
                            className="small secondary"
                            onClick={() => setCondonando(m)}
                            title="Condonar o Exonerar Sanción"
                          >
                            <ShieldCheck size={13} color="var(--emerald-600)" />
                            <span>Condonar</span>
                          </button>
                        )}
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal: Condonar Sanción */}
      {condonando && (
        <div className="modal-overlay" onClick={() => setCondonando(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 480 }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <CheckCircle2 size={18} color="var(--emerald-600)" />
                <h3>Condonar Sanción — #{condonando.id}</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setCondonando(null)}>
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <p style={{ fontSize: 13.5, color: 'var(--text-secondary)', marginBottom: 14 }}>
                Estás a punto de condonar la sanción del usuario <strong>{condonando.usuarioNombre}</strong> por el recurso <strong>{condonando.recursoNombre}</strong>.
              </p>
              <form onSubmit={handleCondonar} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                <label>
                  <span>Observación o Justificación de la Condonación *</span>
                  <textarea
                    name="observacion"
                    placeholder="Motivo formal de la exoneración o condonación de la sanción..."
                    required
                  />
                </label>
                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 6 }}>
                  <button type="button" className="secondary" onClick={() => setCondonando(null)}>
                    Cancelar
                  </button>
                  <button type="submit" className="success">
                    <Check size={15} />
                    <span>Confirmar Condonación</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default Multas
