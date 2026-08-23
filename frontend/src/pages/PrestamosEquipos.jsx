import { useEffect, useState, useMemo } from 'react'
import {
  listarPrestamosEquipos,
  listarPrestamosEquiposPorUsuario,
  solicitarPrestamoEquipo,
  aceptarPrestamoEquipo,
  rechazarPrestamoEquipo,
  devolverPrestamoEquipo,
  solicitarRenovacionEquipo,
  procesarRenovacionEquipo,
  listarEquipos,
  listarUsuarios,
} from '../api/index.js'
import { useAuth } from '../auth/AuthContext.jsx'
import {
  Plus,
  Search,
  CheckCircle2,
  AlertCircle,
  RotateCcw,
  Check,
  X,
  RefreshCw,
  Laptop,
  Clock
} from 'lucide-react'

const ESTADOS_FISICOS = ['excelente', 'bueno', 'regular', 'dañado', 'incompleto']
const ESTADOS_FINALES = ['disponible', 'mantenimiento', 'dañado', 'perdido', 'eliminado', 'fuera_de_servicio']
const ES_STAFF = (rol) => rol === 'administrador' || rol === 'almacenista'

function PrestamosEquipos() {
  const { user } = useAuth()
  const [prestamos, setPrestamos] = useState([])
  const [equipos, setEquipos] = useState([])
  const [usuarios, setUsuarios] = useState([])
  const [tabEstado, setTabEstado] = useState('todos')
  const [busqueda, setBusqueda] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState({ usuarioId: '', equipoId: '', observaciones: '' })

  // Action Modals
  const [devolviendo, setDevolviendo] = useState(null)
  const [formDevolucion, setFormDevolucion] = useState({ estadoFisico: 'bueno', estadoFinal: 'disponible', observacionDevolucion: '' })
  const [renovando, setRenovando] = useState(null)
  const [rechazando, setRechazando] = useState(null)
  const [procesando, setProcesando] = useState(null)
  const [message, setMessage] = useState({ type: '', text: '' })
  const [cargando, setCargando] = useState(true)

  const staff = ES_STAFF(user?.rol)

  const cargar = async () => {
    setCargando(true)
    try {
      const lista = staff
        ? await listarPrestamosEquipos()
        : await listarPrestamosEquiposPorUsuario(user.id)
      setPrestamos(lista || [])
      const [eq, us] = await Promise.all([
        listarEquipos(),
        staff ? listarUsuarios() : Promise.resolve([]),
      ])
      setEquipos(eq || [])
      setUsuarios(us || [])
    } catch (e) {
      setMessage({ type: 'error', text: e.message })
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const prestamosFiltrados = useMemo(() => {
    return prestamos.filter((p) => {
      const matchTab =
        tabEstado === 'todos' ? true : p.estado?.toLowerCase() === tabEstado.toLowerCase()
      const matchBusqueda =
        !busqueda ||
        p.equipoNombre?.toLowerCase().includes(busqueda.toLowerCase()) ||
        p.recursoNombre?.toLowerCase().includes(busqueda.toLowerCase()) ||
        p.usuarioNombre?.toLowerCase().includes(busqueda.toLowerCase()) ||
        String(p.id).includes(busqueda)
      return matchTab && matchBusqueda
    })
  }, [prestamos, tabEstado, busqueda])

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.equipoId) {
      setMessage({ type: 'error', text: 'Por favor selecciona un equipo' })
      return
    }
    try {
      await solicitarPrestamoEquipo({
        usuarioId: Number(form.usuarioId || user.id),
        equipoId: Number(form.equipoId),
        observaciones: form.observaciones,
      })
      setMessage({ type: 'success', text: 'Solicitud de préstamo de equipo enviada' })
      setForm({ usuarioId: '', equipoId: '', observaciones: '' })
      setShowModal(false)
      cargar()
    } catch (err) {
      setMessage({ type: 'error', text: err.message })
    }
  }

  const accion = async (fn, ok) => {
    try {
      await fn()
      setMessage({ type: 'success', text: ok })
      setDevolviendo(null)
      setRenovando(null)
      setRechazando(null)
      setProcesando(null)
      cargar()
    } catch (err) {
      setMessage({ type: 'error', text: err.message })
    }
  }

  const handleDevolucionSubmit = async (e) => {
    e.preventDefault()
    if (!formDevolucion.observacionDevolucion.trim()) {
      setMessage({ type: 'error', text: 'La observación es obligatoria al registrar la devolución.' })
      return
    }
    try {
      await devolverPrestamoEquipo(devolviendo.id, formDevolucion)
      setMessage({ type: 'success', text: 'Devolución de equipo registrada correctamente' })
      setDevolviendo(null)
      setFormDevolucion({ estadoFisico: 'bueno', estadoFinal: 'disponible', observacionDevolucion: '' })
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

      {/* Filter and Tab Bar */}
      <div className="filter-bar">
        <div className="search-box">
          <Search size={16} className="search-icon" />
          <input
            type="text"
            placeholder="Buscar por equipo, usuario o ID..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
        </div>

        <div className="tab-pills">
          {[
            { id: 'todos', label: 'Todos' },
            { id: 'pendiente', label: 'Pendientes' },
            { id: 'aprobada', label: 'En Préstamo' },
            { id: 'devolver', label: 'Devueltos' },
            { id: 'rechazado', label: 'Rechazados' },
          ].map((t) => (
            <button
              key={t.id}
              className={`tab-pill-btn ${tabEstado === t.id ? 'active' : ''}`}
              onClick={() => setTabEstado(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>

        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <button onClick={() => setShowModal(true)}>
            <Plus size={16} />
            <span>Solicitar Equipo</span>
          </button>
          <button className="secondary" onClick={cargar} title="Recargar">
            <RefreshCw size={15} className={cargando ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Loans Table */}
      <div className="table-container">
        <div className="table-responsive">
          <table>
            <thead>
              <tr>
                <th style={{ width: 70 }}>ID</th>
                <th>Equipo Solicitado</th>
                {staff && <th>Usuario Solicitante</th>}
                <th>Fecha Préstamo</th>
                <th>Límite Devolución</th>
                <th>Estado</th>
                <th>Renovaciones</th>
                <th style={{ textAlign: 'right' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {cargando && prestamosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={staff ? 8 : 7}>
                    <div style={{ padding: '40px 16px', textAlign: 'center', color: 'var(--text-muted)' }}>
                      <RefreshCw size={26} className="animate-spin" style={{ margin: '0 auto 10px', color: 'var(--primary)' }} />
                      <div style={{ fontSize: 13.5, fontWeight: 600 }}>Cargando préstamos de equipos...</div>
                    </div>
                  </td>
                </tr>
              ) : prestamosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={staff ? 8 : 7}>
                    <div className="empty-state">
                      <div className="empty-state-icon">
                        <Laptop size={24} />
                      </div>
                      <h4>No se encontraron préstamos de equipos</h4>
                      <p>
                        {busqueda || tabEstado !== 'todos'
                          ? 'No hay registros que coincidan con los filtros aplicados.'
                          : 'No se registran solicitudes de préstamo de equipos actualmente.'}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                prestamosFiltrados.map((p) => (
                  <tr key={p.id}>
                    <td style={{ fontWeight: 700, color: 'var(--text-muted)' }}>#{p.id}</td>
                    <td>
                      <div style={{ fontWeight: 700, color: 'var(--text-main)', fontSize: 14 }}>
                        {p.equipoNombre || p.recursoNombre || `Equipo #${p.equipoId}`}
                      </div>
                      {p.observaciones && (
                        <div style={{ fontSize: 12, color: 'var(--text-muted)', maxWidth: 260, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {p.observaciones}
                        </div>
                      )}
                    </td>
                    {staff && (
                      <td>
                        <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>{p.usuarioNombre}</div>
                        <div style={{ fontSize: 11.5, color: 'var(--text-muted)' }}>{p.usuarioRol || 'Usuario'}</div>
                      </td>
                    )}
                    <td style={{ fontSize: 13 }}>
                      {p.fechaPrestamo ? new Date(p.fechaPrestamo).toLocaleDateString() : '—'}
                    </td>
                    <td style={{ fontSize: 13 }}>
                      {p.fechaDevolucionEsperada ? (
                        <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                          {new Date(p.fechaDevolucionEsperada).toLocaleDateString()}
                        </span>
                      ) : (
                        '—'
                      )}
                    </td>
                    <td>
                      <span className={`badge ${p.estado}`}>{p.estado}</span>
                    </td>
                    <td>
                      <span style={{ fontSize: 12, background: 'var(--bg-subtle)', padding: '2px 8px', borderRadius: 4, fontWeight: 700 }}>
                        {p.vecesRenovado || 0} / 2
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: 6, alignItems: 'center' }}>
                        {/* Staff Approval / Rejection */}
                        {staff && p.estado === 'pendiente' && (
                          <>
                            <button
                              className="success small"
                              onClick={() => accion(() => aceptarPrestamoEquipo(p.id), 'Préstamo de equipo aprobado')}
                              title="Aprobar Solicitud"
                            >
                              <Check size={13} />
                              <span>Aprobar</span>
                            </button>
                            <button
                              className="danger small"
                              onClick={() => setRechazando(p)}
                              title="Rechazar Solicitud"
                            >
                              <X size={13} />
                              <span>Rechazar</span>
                            </button>
                          </>
                        )}

                        {/* Staff Return Inspection */}
                        {staff && p.estado === 'aprobada' && (
                          <button
                            className="secondary small"
                            onClick={() => setDevolviendo(p)}
                            title="Recibir Devolución de Equipo"
                          >
                            <Laptop size={13} />
                            <span>Recibir</span>
                          </button>
                        )}

                        {/* User or Staff Renewal Request */}
                        {p.estado === 'aprobada' && (p.vecesRenovado || 0) < 2 && (
                          <button
                            className="secondary small"
                            onClick={() => setRenovando(p)}
                            title="Solicitar Renovación de días"
                          >
                            <RotateCcw size={13} />
                            <span>Renovar</span>
                          </button>
                        )}

                        {/* Staff Process Renewal */}
                        {staff && p.estado === 'solicitada' && (
                          <button
                            className="small"
                            onClick={() => setProcesando(p)}
                            title="Procesar Renovación"
                          >
                            <Clock size={13} />
                            <span>Procesar</span>
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal: New Equipment Loan Request */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <div className="stat-icon-wrapper indigo" style={{ width: 34, height: 34 }}>
                  <Laptop size={18} />
                </div>
                <h3>Solicitar Préstamo de Equipo</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setShowModal(false)}>
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                {staff && (
                  <label>
                    <span>Usuario Solicitante *</span>
                    <select
                      value={form.usuarioId}
                      onChange={(e) => setForm({ ...form, usuarioId: e.target.value })}
                      required
                    >
                      <option value="">— Seleccionar Aprendiz o Instructor —</option>
                      {usuarios.map((u) => (
                        <option key={u.id} value={u.id}>
                          {u.nombres} {u.apellidos} ({u.rol})
                        </option>
                      ))}
                    </select>
                  </label>
                )}

                <label>
                  <span>Equipo de Almacén *</span>
                  <select
                    value={form.equipoId}
                    onChange={(e) => setForm({ ...form, equipoId: e.target.value })}
                    required
                  >
                    <option value="">— Seleccionar Equipo Disponible —</option>
                    {equipos.map((eq) => (
                      <option key={eq.id} value={eq.id} disabled={!eq.disponiblePrestamo && eq.estado !== 'disponible'}>
                        {eq.nombre} — {eq.marca} ({eq.estado})
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Observaciones / Uso Destinado</span>
                  <textarea
                    value={form.observaciones}
                    onChange={(e) => setForm({ ...form, observaciones: e.target.value })}
                    placeholder="Ambiente formativo, clase, práctica técnica..."
                  />
                </label>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 8 }}>
                  <button type="button" className="secondary" onClick={() => setShowModal(false)}>
                    Cancelar
                  </button>
                  <button type="submit">
                    <CheckCircle2 size={16} />
                    <span>Enviar Solicitud</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Return Equipment (Inspection) */}
      {devolviendo && (
        <div className="modal-overlay" onClick={() => setDevolviendo(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 500 }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <Laptop size={18} color="var(--primary)" />
                <h3>Registrar Devolución de Equipo — #{devolviendo.id}</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setDevolviendo(null)}>
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              <form onSubmit={handleDevolucionSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                <div style={{ background: '#f8fafc', padding: 12, borderRadius: 8, border: '1px solid #e2e8f0', fontSize: 13 }}>
                  <div>Equipo: <strong>{devolviendo.equipoNombre || devolviendo.recursoNombre}</strong></div>
                  <div>Usuario: <strong>{devolviendo.usuarioNombre}</strong></div>
                </div>

                <label>
                  <span>Estado Físico en la Entrega *</span>
                  <select
                    value={formDevolucion.estadoFisico}
                    onChange={(e) => setFormDevolucion({ ...formDevolucion, estadoFisico: e.target.value })}
                    required
                  >
                    {ESTADOS_FISICOS.map((ef) => (
                      <option key={ef} value={ef}>
                        {ef.charAt(0).toUpperCase() + ef.slice(1)}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Estado Final en Inventario *</span>
                  <select
                    value={formDevolucion.estadoFinal}
                    onChange={(e) => setFormDevolucion({ ...formDevolucion, estadoFinal: e.target.value })}
                    required
                  >
                    {ESTADOS_FINALES.map((ef) => (
                      <option key={ef} value={ef}>
                        {ef.charAt(0).toUpperCase() + ef.slice(1)}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Observación de la Devolución *</span>
                  <textarea
                    value={formDevolucion.observacionDevolucion}
                    onChange={(e) => setFormDevolucion({ ...formDevolucion, observacionDevolucion: e.target.value })}
                    placeholder="Condición del equipo, periféricos, cable de poder, limpieza..."
                    required
                  />
                </label>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 8 }}>
                  <button type="button" className="secondary" onClick={() => setDevolviendo(null)}>
                    Cancelar
                  </button>
                  <button type="submit" className="success">
                    <Check size={15} />
                    <span>Confirmar Devolución</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Request Renewal */}
      {renovando && (
        <div className="modal-overlay" onClick={() => setRenovando(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 460 }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <RotateCcw size={18} color="var(--primary)" />
                <h3>Solicitar Renovación — #{renovando.id}</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setRenovando(null)}>
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <p style={{ fontSize: 13.5, color: 'var(--text-secondary)', marginBottom: 14 }}>
                ¿Deseas solicitar una extensión de tiempo para el préstamo del equipo <strong>{renovando.equipoNombre || renovando.recursoNombre}</strong>?
              </p>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
                <button className="secondary" onClick={() => setRenovando(null)}>
                  Cancelar
                </button>
                <button
                  onClick={() => accion(() => solicitarRenovacionEquipo(renovando.id), 'Solicitud de renovación enviada')}
                >
                  <Check size={15} />
                  <span>Confirmar Solicitud</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Reject Loan */}
      {rechazando && (
        <div className="modal-overlay" onClick={() => setRechazando(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 460 }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <AlertCircle size={18} color="var(--rose)" />
                <h3>Rechazar Solicitud — #{rechazando.id}</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setRechazando(null)}>
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <p style={{ fontSize: 13.5, color: 'var(--text-secondary)', marginBottom: 14 }}>
                ¿Estás seguro de rechazar la solicitud de equipo del usuario <strong>{rechazando.usuarioNombre}</strong>?
              </p>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
                <button className="secondary" onClick={() => setRechazando(null)}>
                  Cancelar
                </button>
                <button
                  className="danger"
                  onClick={() => accion(() => rechazarPrestamoEquipo(rechazando.id), 'Solicitud de préstamo rechazada')}
                >
                  <X size={15} />
                  <span>Confirmar Rechazo</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Process Renewal (Staff) */}
      {procesando && (
        <div className="modal-overlay" onClick={() => setProcesando(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 480 }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <Clock size={18} color="var(--primary)" />
                <h3>Procesar Renovación de Equipo — #{procesando.id}</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setProcesando(null)}>
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <p style={{ fontSize: 13.5, color: 'var(--text-secondary)', marginBottom: 16 }}>
                Solicitud de renovación para el equipo <strong>{procesando.equipoNombre || procesando.recursoNombre}</strong> solicitada por <strong>{procesando.usuarioNombre}</strong>.
              </p>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
                <button
                  className="danger"
                  onClick={() => accion(() => procesarRenovacionEquipo(procesando.id, false), 'Renovación rechazada')}
                >
                  <X size={15} />
                  <span>Rechazar</span>
                </button>
                <button
                  className="success"
                  onClick={() => accion(() => procesarRenovacionEquipo(procesando.id, true), 'Renovación aprobada')}
                >
                  <Check size={15} />
                  <span>Aprobar Renovación</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default PrestamosEquipos
