import { useEffect, useState, useMemo } from 'react'
import {
  listarEquipos,
  crearEquipo,
  cambiarEstadoEquipo,
  eliminarEquipo,
  listarHistorialEquipos,
  listarUsuarios,
} from '../api/index.js'
import {
  Laptop,
  Plus,
  Search,
  CheckCircle2,
  AlertCircle,
  Trash2,
  RefreshCw,
  X,
  History,
  Shield,
  Check
} from 'lucide-react'

import { useAuth } from '../auth/AuthContext.jsx'

const ESTADOS = ['disponible', 'prestado', 'mantenimiento', 'dañado']

const emptyForm = {
  nombre: '',
  tipoEquipo: '',
  marca: '',
  modelo: '',
  numeroSerie: '',
  estado: 'disponible',
  ubicacion: '',
  tiempoMaxPrestamo: '',
  proveedor: '',
  responsable: '',
  disponiblePrestamo: true,
  descripcion: '',
}

function Equipos() {
  const { user } = useAuth()
  const puedeGestionar = user?.rol === 'administrador' || user?.rol === 'almacenista'
  const [equipos, setEquipos] = useState([])
  const [usuarios, setUsuarios] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [showModal, setShowModal] = useState(false)
  const [filtro, setFiltro] = useState({ busqueda: '', estado: '', tipo: '' })
  const [historialModal, setHistorialModal] = useState({ open: false, equipo: null, filas: [] })
  const [estadoModal, setEstadoModal] = useState({ open: false, equipo: null, nuevoEstado: 'disponible', administradorId: '', observacion: '' })
  const [message, setMessage] = useState({ type: '', text: '' })
  const [cargando, setCargando] = useState(true)

  const cargar = async () => {
    setCargando(true)
    try {
      const [eq, us] = await Promise.all([
        listarEquipos(filtro),
        puedeGestionar ? listarUsuarios() : Promise.resolve([])
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
  }, [filtro.busqueda, filtro.estado, filtro.tipo])

  const tiposDisponibles = useMemo(() => {
    const set = new Set(equipos.map((e) => e.tipoEquipo).filter(Boolean))
    return Array.from(set)
  }, [equipos])

  const handleChange = (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    setForm({ ...form, [e.target.name]: value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await crearEquipo({
        ...form,
        tiempoMaxPrestamo: form.tiempoMaxPrestamo ? Number(form.tiempoMaxPrestamo) : null,
      })
      setMessage({ type: 'success', text: `Equipo "${form.nombre}" registrado exitosamente` })
      setForm(emptyForm)
      setShowModal(false)
      cargar()
    } catch (err) {
      setMessage({ type: 'error', text: err.message })
    }
  }

  const openEstadoModal = (equipo, nuevoEstado) => {
    setEstadoModal({
      open: true,
      equipo,
      nuevoEstado,
      administradorId: usuarios[0]?.id || '',
      observacion: ''
    })
  }

  const submitEstadoChange = async (e) => {
    e.preventDefault()
    if (!estadoModal.observacion.trim()) {
      setMessage({ type: 'error', text: 'La observación es obligatoria para cambiar el estado del equipo.' })
      return
    }
    try {
      await cambiarEstadoEquipo(estadoModal.equipo.id, {
        estado: estadoModal.nuevoEstado,
        observacion: estadoModal.observacion,
        administradorId: Number(estadoModal.administradorId),
      })
      setMessage({ type: 'success', text: `Estado de "${estadoModal.equipo.nombre}" actualizado a ${estadoModal.nuevoEstado}` })
      setEstadoModal({ open: false, equipo: null, nuevoEstado: 'disponible', administradorId: '', observacion: '' })
      cargar()
    } catch (err) {
      setMessage({ type: 'error', text: err.message })
    }
  }

  const handleVerHistorial = async (equipo) => {
    try {
      const filas = await listarHistorialEquipos(equipo.id)
      setHistorialModal({ open: true, equipo, filas: filas || [] })
    } catch (err) {
      setMessage({ type: 'error', text: err.message })
    }
  }

  const handleEliminar = async (equipo) => {
    if (!confirm(`¿Eliminar equipo "${equipo.nombre}" (borrado lógico)?`)) return
    try {
      await eliminarEquipo(equipo.id)
      setMessage({ type: 'success', text: `Equipo "${equipo.nombre}" eliminado exitosamente` })
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

      {/* Filter and Search Bar */}
      <div className="filter-bar">
        <div className="search-box">
          <Search size={16} className="search-icon" />
          <input
            type="text"
            placeholder="Buscar por nombre, marca, serie o ubicación..."
            value={filtro.busqueda}
            onChange={(e) => setFiltro({ ...filtro, busqueda: e.target.value })}
          />
        </div>

        <div style={{ display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap' }}>
          <select
            value={filtro.estado}
            onChange={(e) => setFiltro({ ...filtro, estado: e.target.value })}
            style={{ minWidth: 150 }}
          >
            <option value="">Todos los estados</option>
            {ESTADOS.map((s) => (
              <option key={s} value={s}>
                {s.charAt(0).toUpperCase() + s.slice(1)}
              </option>
            ))}
          </select>

          {tiposDisponibles.length > 0 && (
            <select
              value={filtro.tipo}
              onChange={(e) => setFiltro({ ...filtro, tipo: e.target.value })}
              style={{ minWidth: 150 }}
            >
              <option value="">Todos los tipos</option>
              {tiposDisponibles.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          )}

          {puedeGestionar && (
            <button onClick={() => setShowModal(true)}>
              <Plus size={16} />
              <span>Nuevo Equipo</span>
            </button>
          )}

          <button className="secondary" onClick={cargar} title="Recargar">
            <RefreshCw size={15} className={cargando ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Equipment Table */}
      <div className="table-container">
        <div className="table-responsive">
          <table>
            <thead>
              <tr>
                <th style={{ width: 70 }}>ID</th>
                <th>Equipo</th>
                <th>Tipo / Marca</th>
                <th>Número de Serie</th>
                <th>Estado</th>
                <th>Disponibilidad</th>
                <th>Ubicación</th>
                <th style={{ textAlign: 'right' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {cargando && equipos.length === 0 ? (
                <tr>
                  <td colSpan={8}>
                    <div style={{ padding: '40px 16px', textAlign: 'center', color: 'var(--text-muted)' }}>
                      <RefreshCw size={26} className="animate-spin" style={{ margin: '0 auto 10px', color: 'var(--primary)' }} />
                      <div style={{ fontSize: 13.5, fontWeight: 600 }}>Cargando inventario de equipos...</div>
                    </div>
                  </td>
                </tr>
              ) : equipos.length === 0 ? (
                <tr>
                  <td colSpan={8}>
                    <div className="empty-state">
                      <div className="empty-state-icon">
                        <Laptop size={24} />
                      </div>
                      <h4>No se encontraron equipos</h4>
                      <p>
                        {filtro.busqueda || filtro.estado || filtro.tipo
                          ? 'No hay equipos que coincidan con los filtros seleccionados.'
                          : 'Aún no hay equipos registrados en el inventario.'}
                      </p>
                      {!filtro.busqueda && !filtro.estado && !filtro.tipo && puedeGestionar && (
                        <button onClick={() => setShowModal(true)} style={{ marginTop: 16 }}>
                          <Plus size={15} />
                          <span>Registrar Primer Equipo</span>
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ) : (
                equipos.map((equipo) => (
                  <tr key={equipo.id}>
                    <td style={{ fontWeight: 700, color: 'var(--text-muted)' }}>#{equipo.id}</td>
                    <td>
                      <div style={{ fontWeight: 700, color: 'var(--text-main)', fontSize: 14 }}>{equipo.nombre}</div>
                      {equipo.descripcion && (
                        <div style={{ fontSize: 12, color: 'var(--text-muted)', maxWidth: 260, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {equipo.descripcion}
                        </div>
                      )}
                    </td>
                    <td>
                      <div style={{ fontWeight: 600, color: 'var(--text-main)', fontSize: 13 }}>{equipo.tipoEquipo || 'Equipo'}</div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{equipo.marca} {equipo.modelo}</div>
                    </td>
                    <td>
                      <code style={{ background: '#f1f5f9', color: '#334155', padding: '2px 7px', borderRadius: 4, fontSize: 12, fontWeight: 700 }}>
                        {equipo.numeroSerie || 'N/A'}
                      </code>
                    </td>
                    <td>
                      <span className={`badge ${equipo.estado}`}>{equipo.estado}</span>
                    </td>
                    <td>
                      {equipo.disponiblePrestamo ? (
                        <span style={{ color: 'var(--emerald-800)', background: 'var(--emerald-50)', border: '1px solid var(--emerald-200)', padding: '2px 8px', borderRadius: '999px', fontWeight: 700, fontSize: 11.5 }}>
                          ● Disponible
                        </span>
                      ) : (
                        <span style={{ color: 'var(--rose-800)', background: 'var(--rose-50)', border: '1px solid var(--rose-200)', padding: '2px 8px', borderRadius: '999px', fontWeight: 700, fontSize: 11.5 }}>
                          ● No disponible
                        </span>
                      )}
                    </td>
                    <td style={{ fontSize: 13, color: 'var(--text-secondary)' }}>{equipo.ubicacion || '—'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: 6, alignItems: 'center' }}>
                        <button
                          className="secondary small"
                          onClick={() => handleVerHistorial(equipo)}
                          title="Ver Historial de Estados"
                          style={{ padding: '5px 8px' }}
                        >
                          <History size={13} />
                        </button>

                        {puedeGestionar && (
                          <>
                            <select
                              style={{ padding: '4px 8px', fontSize: 12, width: 'auto', borderRadius: 6 }}
                              value={equipo.estado}
                              onChange={(e) => openEstadoModal(equipo, e.target.value)}
                            >
                              {ESTADOS.map((s) => (
                                <option key={s} value={s}>
                                  → {s}
                                </option>
                              ))}
                            </select>

                            <button
                              className="danger small"
                              onClick={() => handleEliminar(equipo)}
                              title="Eliminar Equipo"
                              style={{ padding: '5px 8px' }}
                            >
                              <Trash2 size={13} />
                            </button>
                          </>
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

      {/* Modal: New Equipment */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <div className="stat-icon-wrapper indigo" style={{ width: 34, height: 34 }}>
                  <Laptop size={18} />
                </div>
                <h3>Registrar Nuevo Equipo en Almacén</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setShowModal(false)}>
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              <form className="grid" onSubmit={handleSubmit}>
                <label style={{ gridColumn: '1 / -1' }}>
                  <span>Nombre / Identificador del Equipo *</span>
                  <input
                    name="nombre"
                    value={form.nombre}
                    onChange={handleChange}
                    placeholder="Ej. Portátil Dell Latitude 5420 #04"
                    required
                  />
                </label>

                <label>
                  <span>Tipo de Equipo *</span>
                  <input
                    name="tipoEquipo"
                    value={form.tipoEquipo}
                    onChange={handleChange}
                    placeholder="Ej. Portátil, Proyector, Tablet"
                    required
                  />
                </label>

                <label>
                  <span>Marca *</span>
                  <input
                    name="marca"
                    value={form.marca}
                    onChange={handleChange}
                    placeholder="Ej. Dell, Epson, HP"
                    required
                  />
                </label>

                <label>
                  <span>Modelo</span>
                  <input
                    name="modelo"
                    value={form.modelo}
                    onChange={handleChange}
                    placeholder="Ej. Latitude 5420"
                  />
                </label>

                <label>
                  <span>Número de Serie / Placa SENA</span>
                  <input
                    name="numeroSerie"
                    value={form.numeroSerie}
                    onChange={handleChange}
                    placeholder="Ej. SN-998234-ADSO"
                  />
                </label>

                <label>
                  <span>Estado Inicial *</span>
                  <select name="estado" value={form.estado} onChange={handleChange}>
                    {ESTADOS.map((s) => (
                      <option key={s} value={s}>
                        {s.charAt(0).toUpperCase() + s.slice(1)}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Ubicación en Almacén</span>
                  <input
                    name="ubicacion"
                    value={form.ubicacion}
                    onChange={handleChange}
                    placeholder="Ej. Almacén 1, Estante C"
                  />
                </label>

                <label>
                  <span>Tiempo Máximo Préstamo (días)</span>
                  <input
                    type="number"
                    name="tiempoMaxPrestamo"
                    value={form.tiempoMaxPrestamo}
                    onChange={handleChange}
                    placeholder="Ej. 7"
                    min="1"
                  />
                </label>

                <label>
                  <span>Proveedor</span>
                  <input
                    name="proveedor"
                    value={form.proveedor}
                    onChange={handleChange}
                    placeholder="Ej. Proveeduría SENA"
                  />
                </label>

                <label>
                  <span>Responsable</span>
                  <input
                    name="responsable"
                    value={form.responsable}
                    onChange={handleChange}
                    placeholder="Ej. Almacenista ADSO"
                  />
                </label>

                <label style={{ gridColumn: '1 / -1' }}>
                  <span>Especificaciones / Descripción</span>
                  <textarea
                    name="descripcion"
                    value={form.descripcion}
                    onChange={handleChange}
                    placeholder="Detalles de hardware, periféricos incluidos, cargador..."
                  />
                </label>

                <div style={{ gridColumn: '1 / -1', display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 10 }}>
                  <button type="button" className="secondary" onClick={() => setShowModal(false)}>
                    Cancelar
                  </button>
                  <button type="submit">
                    <CheckCircle2 size={16} />
                    <span>Guardar Equipo</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Change Status */}
      {estadoModal.open && (
        <div className="modal-overlay" onClick={() => setEstadoModal({ open: false, equipo: null, nuevoEstado: 'disponible', administradorId: '', observacion: '' })}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 500 }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <Shield size={18} color="var(--primary)" />
                <h3>Cambiar Estado — {estadoModal.equipo?.nombre}</h3>
              </div>
              <button
                className="modal-close-btn"
                onClick={() => setEstadoModal({ open: false, equipo: null, nuevoEstado: 'disponible', administradorId: '', observacion: '' })}
              >
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              <form onSubmit={submitEstadoChange} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                <div style={{ background: '#f8fafc', padding: 12, borderRadius: 8, border: '1px solid #e2e8f0', fontSize: 13 }}>
                  <div>Estado actual: <strong style={{ textTransform: 'capitalize' }}>{estadoModal.equipo?.estado}</strong></div>
                  <div style={{ marginTop: 4 }}>Nuevo estado seleccionado: <strong style={{ color: 'var(--primary)', textTransform: 'capitalize' }}>{estadoModal.nuevoEstado}</strong></div>
                </div>

                {usuarios.length > 0 && (
                  <label>
                    <span>Responsable del Cambio *</span>
                    <select
                      value={estadoModal.administradorId}
                      onChange={(e) => setEstadoModal({ ...estadoModal, administradorId: e.target.value })}
                      required
                    >
                      {usuarios.map((u) => (
                        <option key={u.id} value={u.id}>
                          {u.nombres} {u.apellidos} ({u.rol})
                        </option>
                      ))}
                    </select>
                  </label>
                )}

                <label>
                  <span>Observación o Justificación del Cambio *</span>
                  <textarea
                    value={estadoModal.observacion}
                    onChange={(e) => setEstadoModal({ ...estadoModal, observacion: e.target.value })}
                    placeholder="Describe el motivo técnico, mantenimiento realizado, entrega o condición del equipo..."
                    required
                  />
                </label>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 8 }}>
                  <button
                    type="button"
                    className="secondary"
                    onClick={() => setEstadoModal({ open: false, equipo: null, nuevoEstado: 'disponible', administradorId: '', observacion: '' })}
                  >
                    Cancelar
                  </button>
                  <button type="submit">
                    <Check size={15} />
                    <span>Guardar Cambio</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Timeline History */}
      {historialModal.open && (
        <div className="modal-overlay" onClick={() => setHistorialModal({ open: false, equipo: null, filas: [] })}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 640 }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <History size={18} color="var(--primary)" />
                <h3>Historial de Estados — {historialModal.equipo?.nombre}</h3>
              </div>
              <button
                className="modal-close-btn"
                onClick={() => setHistorialModal({ open: false, equipo: null, filas: [] })}
              >
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              {historialModal.filas.length === 0 ? (
                <div className="empty-state" style={{ padding: '24px 0' }}>
                  <p>No hay registros históricos de cambio de estado para este equipo.</p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                  {historialModal.filas.map((h, i) => (
                    <div
                      key={h.id || i}
                      style={{
                        padding: '12px 16px',
                        background: '#f8fafc',
                        borderRadius: 10,
                        border: '1px solid #e2e8f0',
                        fontSize: 13
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                        <span className={`badge ${h.estadoNuevo || h.estado}`}>{h.estadoNuevo || h.estado}</span>
                        <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                          {h.fechaCambio ? new Date(h.fechaCambio).toLocaleString() : 'Fecha no registrada'}
                        </span>
                      </div>
                      <p style={{ margin: '6px 0 2px', color: 'var(--text-main)', fontWeight: 500 }}>
                        {h.observacion || 'Sin observaciones'}
                      </p>
                      {h.administradorNombre && (
                        <div style={{ fontSize: 11.5, color: 'var(--text-muted)' }}>
                          Por: <strong>{h.administradorNombre}</strong>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default Equipos
