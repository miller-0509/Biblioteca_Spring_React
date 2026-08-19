import { useEffect, useState, useMemo } from 'react'
import {
  listarPrestamos,
  listarPrestamosPorUsuario,
  crearPrestamo,
  aceptarPrestamoLibro,
  rechazarPrestamoLibro,
  devolverPrestamoLibro,
  solicitarRenovacionLibro,
  procesarRenovacionLibro,
  listarLibros,
  listarUsuarios,
} from '../api/index.js'
import { useAuth } from '../auth/AuthContext.jsx'
import {
  BookmarkCheck,
  Plus,
  Search,
  Filter,
  CheckCircle2,
  AlertCircle,
  Clock,
  RotateCcw,
  Check,
  X,
  RefreshCw,
  User,
  BookOpen,
  Calendar,
  AlertTriangle
} from 'lucide-react'

const ESTADOS_FISICOS = ['excelente', 'bueno', 'regular', 'deteriorado', 'dañado', 'incompleto']
const ESTADOS_FINALES = ['disponible', 'mantenimiento', 'dañado', 'perdido', 'eliminado']
const ES_STAFF = (rol) => rol === 'administrador' || rol === 'bibliotecario'

function PrestamosLibros() {
  const { user } = useAuth()
  const [prestamos, setPrestamos] = useState([])
  const [libros, setLibros] = useState([])
  const [usuarios, setUsuarios] = useState([])
  const [tabEstado, setTabEstado] = useState('todos')
  const [busqueda, setBusqueda] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState({ usuarioId: '', libroId: '', observaciones: '' })
  
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
        ? await listarPrestamos()
        : await listarPrestamosPorUsuario(user.id)
      setPrestamos(lista || [])
      const [l, u] = await Promise.all([
        listarLibros(),
        staff ? listarUsuarios() : Promise.resolve([]),
      ])
      setLibros(l?.content || l || [])
      setUsuarios(u || [])
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
        p.libroTitulo?.toLowerCase().includes(busqueda.toLowerCase()) ||
        p.usuarioNombre?.toLowerCase().includes(busqueda.toLowerCase()) ||
        String(p.id).includes(busqueda)
      return matchTab && matchBusqueda
    })
  }, [prestamos, tabEstado, busqueda])

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.libroId) {
      setMessage({ type: 'error', text: 'Por favor selecciona un libro del catálogo' })
      return
    }
    try {
      await crearPrestamo({
        usuarioId: Number(form.usuarioId || user.id),
        libroId: Number(form.libroId),
        observaciones: form.observaciones,
      })
      setMessage({ type: 'success', text: 'Solicitud de préstamo enviada exitosamente' })
      setForm({ usuarioId: '', libroId: '', observaciones: '' })
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
      await devolverPrestamoLibro(devolviendo.id, formDevolucion)
      setMessage({ type: 'success', text: 'Devolución registrada correctamente (las multas se aplican automáticamente si hubo retraso)' })
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
          <span style={{ flex: 1 }}>{message.text}</span>
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
            placeholder="Buscar por libro, usuario o ID..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
        </div>

        <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
          {['todos', 'pendiente', 'aceptado', 'devolver', 'rechazado'].map((st) => (
            <button
              key={st}
              className={`small ${tabEstado === st ? '' : 'secondary'}`}
              onClick={() => setTabEstado(st)}
            >
              {st.charAt(0).toUpperCase() + st.slice(1)}
            </button>
          ))}

          <button onClick={() => setShowModal(true)}>
            <Plus size={16} />
            <span>Solicitar Préstamo</span>
          </button>

          <button className="secondary" onClick={cargar} title="Recargar">
            <RefreshCw size={15} className={cargando ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Table of Loans */}
      <div className="table-container">
        <div className="table-responsive">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Usuario</th>
                <th>Libro Solicitado</th>
                <th>Fecha Solicitud</th>
                <th>Devolución Esperada</th>
                <th>Estado</th>
                <th>Renovaciones</th>
                <th style={{ textAlign: 'right' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {cargando && prestamosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={8}>
                    <div style={{ padding: '36px 16px', textAlign: 'center', color: 'var(--text-muted)' }}>
                      <RefreshCw size={24} className="animate-spin" style={{ margin: '0 auto 10px', color: 'var(--primary)' }} />
                      <div style={{ fontSize: 13, fontWeight: 500 }}>Cargando registros de préstamos...</div>
                    </div>
                  </td>
                </tr>
              ) : prestamosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={8}>
                    <div className="empty-state">
                      <div className="empty-state-icon">
                        <BookmarkCheck size={24} />
                      </div>
                      <h4>No hay préstamos para mostrar</h4>
                      <p>No se encontraron registros bajo el filtro o búsqueda seleccionados.</p>
                    </div>
                  </td>
                </tr>
              ) : (
                prestamosFiltrados.map((p) => (
                  <tr key={p.id}>
                    <td style={{ fontWeight: 600, color: 'var(--text-muted)' }}>#{p.id}</td>
                    <td>
                      <div style={{ fontWeight: 700, color: 'var(--text-main)' }}>{p.usuarioNombre}</div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>ID: {p.usuarioId || '—'}</div>
                    </td>
                    <td>
                      <div style={{ fontWeight: 600, color: 'var(--primary-dark)' }}>{p.libroTitulo}</div>
                    </td>
                    <td style={{ fontSize: 13 }}>
                      {p.fechaSolicitud ? new Date(p.fechaSolicitud).toLocaleDateString() : '—'}
                    </td>
                    <td style={{ fontSize: 13 }}>
                      {p.fechaDevolucionEsperada ? (
                        <span style={{ fontWeight: 600 }}>
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
                      <span style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-secondary)' }}>
                        {p.renovacionesAplicadas ?? 0}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: 6, alignItems: 'center' }}>
                        {staff && p.estado === 'pendiente' && (
                          <>
                            <button
                              className="success small"
                              onClick={() => accion(() => aceptarPrestamoLibro(p.id), 'Préstamo aprobado')}
                              title="Aprobar préstamo"
                            >
                              <Check size={13} />
                              <span>Aprobar</span>
                            </button>
                            <button
                              className="danger small"
                              onClick={() => setRechazando(p)}
                              title="Rechazar préstamo"
                            >
                              <X size={13} />
                              <span>Rechazar</span>
                            </button>
                          </>
                        )}
                        {p.estado === 'aceptado' && (
                          <>
                            <button
                              className="small"
                              onClick={() => {
                                setDevolviendo(p)
                                setFormDevolucion({ estadoFisico: 'bueno', estadoFinal: 'disponible', observacionDevolucion: '' })
                              }}
                            >
                              <RotateCcw size={13} />
                              <span>Devolver</span>
                            </button>
                            <button
                              className="secondary small"
                              onClick={() => setRenovando(p)}
                            >
                              <span>Renovar</span>
                            </button>
                          </>
                        )}
                        {p.estado === 'aceptado' && p.estadoRenovacion === 'pendiente' && staff && (
                          <button
                            className="small"
                            style={{ background: 'var(--purple)' }}
                            onClick={() => setProcesando(p)}
                          >
                            <span>Procesar Renov.</span>
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

      {/* Modal: New Loan Request */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <div className="sidebar-brand-icon" style={{ width: 32, height: 32 }}>
                  <BookmarkCheck size={16} />
                </div>
                <h3>Solicitar Préstamo de Libro</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setShowModal(false)}>
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                {staff && (
                  <label>
                    <span>Usuario Solicitante</span>
                    <select
                      name="usuarioId"
                      value={form.usuarioId}
                      onChange={(e) => setForm({ ...form, usuarioId: e.target.value })}
                    >
                      <option value="">— Seleccionar Usuario —</option>
                      {usuarios.map((u) => (
                        <option key={u.id} value={u.id}>
                          {u.nombres} {u.apellidos} ({u.rol})
                        </option>
                      ))}
                    </select>
                  </label>
                )}

                <label>
                  <span>Seleccionar Libro *</span>
                  <select
                    name="libroId"
                    value={form.libroId}
                    onChange={(e) => setForm({ ...form, libroId: e.target.value })}
                    required
                  >
                    <option value="">— Seleccionar del catálogo —</option>
                    {libros.map((l) => (
                      <option key={l.id} value={l.id}>
                        {l.titulo} — {l.autor} ({l.estado})
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Observaciones o Justificación</span>
                  <textarea
                    name="observaciones"
                    value={form.observaciones}
                    onChange={(e) => setForm({ ...form, observaciones: e.target.value })}
                    placeholder="Motivo del préstamo, proyecto o asignatura..."
                  />
                </label>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 10 }}>
                  <button type="button" className="secondary" onClick={() => setShowModal(false)}>
                    Cancelar
                  </button>
                  <button type="submit">
                    <CheckCircle2 size={16} />
                    <span>Confirmar Solicitud</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Devolución */}
      {devolviendo && (
        <div className="modal-overlay" onClick={() => setDevolviendo(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 500 }}>
            <div className="modal-header">
              <h3>Registrar Devolución — {devolviendo.libroTitulo}</h3>
              <button className="modal-close-btn" onClick={() => setDevolviendo(null)}>
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <form onSubmit={handleDevolucionSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                <label>
                  <span>Estado Físico en la Entrega</span>
                  <select
                    value={formDevolucion.estadoFisico}
                    onChange={(e) => setFormDevolucion({ ...formDevolucion, estadoFisico: e.target.value })}
                  >
                    {ESTADOS_FISICOS.map((s) => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Estado Final del Libro</span>
                  <select
                    value={formDevolucion.estadoFinal}
                    onChange={(e) => setFormDevolucion({ ...formDevolucion, estadoFinal: e.target.value })}
                  >
                    {ESTADOS_FINALES.map((s) => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Observación de Devolución *</span>
                  <textarea
                    value={formDevolucion.observacionDevolucion}
                    onChange={(e) => setFormDevolucion({ ...formDevolucion, observacionDevolucion: e.target.value })}
                    placeholder="Condiciones del libro al momento de la entrega..."
                    required
                  />
                </label>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 6 }}>
                  <button type="button" className="secondary" onClick={() => setDevolviendo(null)}>
                    Cancelar
                  </button>
                  <button type="submit">
                    <CheckCircle2 size={16} />
                    <span>Confirmar Devolución</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Renovación */}
      {renovando && (
        <div className="modal-overlay" onClick={() => setRenovando(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 460 }}>
            <div className="modal-header">
              <h3>Solicitar Renovación — #{renovando.id}</h3>
              <button className="modal-close-btn" onClick={() => setRenovando(null)}>
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  const motivo = e.target.motivo.value
                  if (!motivo.trim()) return
                  accion(() => solicitarRenovacionLibro(renovando.id, motivo), 'Solicitud de renovación enviada')
                }}
                style={{ display: 'flex', flexDirection: 'column', gap: 14 }}
              >
                <label>
                  <span>Motivo de la Extensión de Tiempo *</span>
                  <textarea
                    name="motivo"
                    placeholder="Explica por qué necesitas más días con el libro..."
                    required
                  />
                </label>
                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
                  <button type="button" className="secondary" onClick={() => setRenovando(null)}>
                    Cancelar
                  </button>
                  <button type="submit">
                    <CheckCircle2 size={15} />
                    <span>Enviar Renovación</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Rechazar */}
      {rechazando && (
        <div className="modal-overlay" onClick={() => setRechazando(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 460 }}>
            <div className="modal-header">
              <h3>Rechazar Solicitud — #{rechazando.id}</h3>
              <button className="modal-close-btn" onClick={() => setRechazando(null)}>
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  const razon = e.target.razon.value
                  if (!razon.trim()) return
                  accion(() => rechazarPrestamoLibro(rechazando.id, razon), 'Solicitud de préstamo rechazada')
                }}
                style={{ display: 'flex', flexDirection: 'column', gap: 14 }}
              >
                <label>
                  <span>Motivo de Rechazo *</span>
                  <textarea
                    name="razon"
                    placeholder="Indica el motivo del rechazo para informar al usuario..."
                    required
                  />
                </label>
                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
                  <button type="button" className="secondary" onClick={() => setRechazando(null)}>
                    Cancelar
                  </button>
                  <button type="submit" className="danger">
                    <X size={15} />
                    <span>Confirmar Rechazo</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Procesar Renovación */}
      {procesando && (
        <div className="modal-overlay" onClick={() => setProcesando(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 460 }}>
            <div className="modal-header">
              <h3>Procesar Renovación — #{procesando.id}</h3>
              <button className="modal-close-btn" onClick={() => setProcesando(null)}>
                <X size={18} />
              </button>
            </div>
            <div className="modal-body">
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  const accionTipo = e.target.accionTipo.value
                  const motivo = e.target.motivo.value
                  accion(() => procesarRenovacionLibro(procesando.id, accionTipo, motivo), 'Renovación procesada con éxito')
                }}
                style={{ display: 'flex', flexDirection: 'column', gap: 14 }}
              >
                <label>
                  <span>Decisión</span>
                  <select name="accionTipo">
                    <option value="aprobar">Aprobar Extensión</option>
                    <option value="rechazar">Rechazar Extensión</option>
                  </select>
                </label>

                <label>
                  <span>Motivo o Justificación (si se rechaza)</span>
                  <input name="motivo" placeholder="Opcional si se aprueba" />
                </label>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 6 }}>
                  <button type="button" className="secondary" onClick={() => setProcesando(null)}>
                    Cancelar
                  </button>
                  <button type="submit">
                    <CheckCircle2 size={15} />
                    <span>Guardar Decisión</span>
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

export default PrestamosLibros
