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
  Filter,
  CheckCircle2,
  AlertCircle,
  Trash2,
  RefreshCw,
  X,
  History,
  Shield,
  Layers
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
          <span style={{ flex: 1 }}>{message.text}</span>
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
            placeholder="Buscar por nombre, serie, marca..."
            value={filtro.busqueda}
            onChange={(e) => setFiltro({ ...filtro, busqueda: e.target.value })}
          />
        </div>

        <div style={{ display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap' }}>
          <select
            value={filtro.estado}
            onChange={(e) => setFiltro({ ...filtro, estado: e.target.value })}
            style={{ minWidth: 140 }}
          >
            <option value="">Todos los estados</option>
            {ESTADOS.map((s) => (
              <option key={s} value={s}>
                {s.charAt(0).toUpperCase() + s.slice(1)}
              </option>
            ))}
          </select>

          <select
            value={filtro.tipo}
            onChange={(e) => setFiltro({ ...filtro, tipo: e.target.value })}
            style={{ minWidth: 140 }}
          >
            <option value="">Todos los tipos</option>
            {tiposDisponibles.map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>

          {puedeGestionar && (
            <button onClick={() => setShowModal(true)}>
              <Plus size={16} />
              <span>Nuevo Equipo</span>
            </button>
          )}

          <button className="secondary" onClick={cargar} title="Recargar lista">
            <RefreshCw size={15} className={cargando ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Table Container */}
      <div className="table-container">
        <div className="table-responsive">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Equipo / Modelo</th>
                <th>Tipo</th>
                <th>Marca</th>
                <th>N° Serie</th>
                <th>Estado</th>
                <th>Disponible</th>
                <th>Ubicación</th>
                <th style={{ textAlign: 'right' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {cargando && equipos.length === 0 ? (
                <tr>
                  <td colSpan={9}>
                    <div style={{ padding: '36px 16px', textAlign: 'center', color: 'var(--text-muted)' }}>
                      <RefreshCw size={24} className="animate-spin" style={{ margin: '0 auto 10px', color: 'var(--primary)' }} />
                      <div style={{ fontSize: 13, fontWeight: 500 }}>Cargando inventario de equipos...</div>
                    </div>
                  </td>
                </tr>
              ) : equipos.length === 0 ? (
                <tr>
                  <td colSpan={9}>
                    <div className="empty-state">
                      <div className="empty-state-icon">
                        <Laptop size={24} />
                      </div>
                      <h4>No se encontraron equipos</h4>
                      <p>No hay equipos registrados o los filtros no arrojaron resultados.</p>
                      {puedeGestionar && (
                        <button onClick={() => setShowModal(true)} style={{ marginTop: 14 }}>
                          <Plus size={15} />
                          <span>Registrar Primer Equipo</span>
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ) : (
                equipos.map((eq) => (
                  <tr key={eq.id}>
                    <td style={{ fontWeight: 600, color: 'var(--text-muted)' }}>#{eq.id}</td>
                    <td>
                      <div style={{ fontWeight: 700, color: 'var(--text-main)' }}>{eq.nombre}</div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{eq.modelo || 'Sin modelo'}</div>
                    </td>
                    <td>
                      <span style={{ fontSize: 12, background: 'var(--bg-subtle)', padding: '2px 8px', borderRadius: 4 }}>
                        {eq.tipoEquipo}
                      </span>
                    </td>
                    <td>{eq.marca || '—'}</td>
                    <td>
                      <code style={{ background: '#f1f5f9', padding: '2px 6px', borderRadius: 4, fontSize: 12 }}>
                        {eq.numeroSerie}
                      </code>
                    </td>
                    <td>
                      <span className={`badge ${eq.estado}`}>{eq.estado}</span>
                    </td>
                    <td>
                      {eq.disponiblePrestamo ? (
                        <span style={{ color: 'var(--emerald)', fontWeight: 600, fontSize: 13 }}>● Sí</span>
                      ) : (
                        <span style={{ color: 'var(--rose)', fontWeight: 600, fontSize: 13 }}>● No</span>
                      )}
                    </td>
                    <td style={{ fontSize: 13, color: 'var(--text-secondary)' }}>{eq.ubicacion || '—'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: 6, alignItems: 'center' }}>
                        {puedeGestionar && (
                          <select
                            className="form-select"
                            style={{ padding: '4px 8px', fontSize: 12, width: 'auto' }}
                            value={eq.estado}
                            onChange={(e) => openEstadoModal(eq, e.target.value)}
                          >
                            {ESTADOS.map((s) => (
                              <option key={s} value={s}>
                                → {s}
                              </option>
                            ))}
                          </select>
                        )}
                        <button
                          className="secondary small"
                          onClick={() => handleVerHistorial(eq)}
                          title="Historial de cambios"
                        >
                          <History size={13} />
                        </button>
                        {puedeGestionar && (
                          <button
                            className="danger small"
                            onClick={() => handleEliminar(eq)}
                            title="Eliminar equipo"
                          >
                            <Trash2 size={13} />
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

      {/* Modal for Creating Equipment */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <div className="sidebar-brand-icon" style={{ width: 32, height: 32 }}>
                  <Laptop size={16} />
                </div>
                <h3>Registrar Nuevo Equipo</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setShowModal(false)}>
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              <form className="grid" onSubmit={handleSubmit}>
                <label style={{ gridColumn: '1 / -1' }}>
                  <span>Nombre del Equipo *</span>
                  <input
                    name="nombre"
                    value={form.nombre}
                    onChange={handleChange}
                    placeholder="Ej: Portátil Dell Latitude 3420"
                    required
                  />
                </label>

                <label>
                  <span>Tipo de Equipo *</span>
                  <input
                    name="tipoEquipo"
                    value={form.tipoEquipo}
                    onChange={handleChange}
                    placeholder="Ej: Portátil, Videoproyector, Tablet"
                    required
                  />
                </label>

                <label>
                  <span>Número de Serie (Único) *</span>
                  <input
                    name="numeroSerie"
                    value={form.numeroSerie}
                    onChange={handleChange}
                    placeholder="Ej: SN-987456321"
                    required
                  />
                </label>

                <label>
                  <span>Marca</span>
                  <input
                    name="marca"
                    value={form.marca}
                    onChange={handleChange}
                    placeholder="Ej: Lenovo, Dell, Epson"
                  />
                </label>

                <label>
                  <span>Modelo</span>
                  <input
                    name="modelo"
                    value={form.modelo}
                    onChange={handleChange}
                    placeholder="Ej: ThinkPad E14"
                  />
                </label>

                <label>
                  <span>Estado Inicial</span>
                  <select name="estado" value={form.estado} onChange={handleChange}>
                    {ESTADOS.map((s) => (
                      <option key={s} value={s}>
                        {s.charAt(0).toUpperCase() + s.slice(1)}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Ubicación / Almacén</span>
                  <input
                    name="ubicacion"
                    value={form.ubicacion}
                    onChange={handleChange}
                    placeholder="Ej: Almacén 2 - Estante B"
                  />
                </label>

                <label>
                  <span>Tiempo Máx. Préstamo (Días)</span>
                  <input
                    type="number"
                    name="tiempoMaxPrestamo"
                    value={form.tiempoMaxPrestamo}
                    onChange={handleChange}
                    placeholder="Ej: 3"
                  />
                </label>

                <label>
                  <span>Proveedor</span>
                  <input
                    name="proveedor"
                    value={form.proveedor}
                    onChange={handleChange}
                    placeholder="Distribuidor o Marca"
                  />
                </label>

                <label style={{ gridColumn: '1 / -1' }}>
                  <span>Descripción o Especificaciones</span>
                  <textarea
                    name="descripcion"
                    value={form.descripcion}
                    onChange={handleChange}
                    placeholder="Características técnicas, accesorios incluidos..."
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

      {/* Modal for State Change with Observation */}
      {estadoModal.open && (
        <div className="modal-overlay" onClick={() => setEstadoModal({ ...estadoModal, open: false })}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 480 }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <Shield size={18} color="var(--primary)" />
                <h3>Cambiar Estado: {estadoModal.equipo?.nombre}</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setEstadoModal({ ...estadoModal, open: false })}>
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              <form onSubmit={submitEstadoChange} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                <label>
                  <span>Nuevo Estado</span>
                  <select
                    value={estadoModal.nuevoEstado}
                    onChange={(e) => setEstadoModal({ ...estadoModal, nuevoEstado: e.target.value })}
                  >
                    {ESTADOS.map((s) => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                </label>

                <label>
                  <span>Administrador Responsable</span>
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

                <label>
                  <span>Observación del Cambio (Obligatorio) *</span>
                  <textarea
                    value={estadoModal.observacion}
                    onChange={(e) => setEstadoModal({ ...estadoModal, observacion: e.target.value })}
                    placeholder="Motivo del cambio de estado, diagnóstico técnico o mantenimiento..."
                    required
                  />
                </label>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 6 }}>
                  <button type="button" className="secondary" onClick={() => setEstadoModal({ ...estadoModal, open: false })}>
                    Cancelar
                  </button>
                  <button type="submit">
                    <CheckCircle2 size={15} />
                    <span>Confirmar Cambio</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Modal for History */}
      {historialModal.open && (
        <div className="modal-overlay" onClick={() => setHistorialModal({ ...historialModal, open: false })}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 680 }}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <History size={18} color="var(--primary)" />
                <h3>Historial de Estados — {historialModal.equipo?.nombre}</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setHistorialModal({ ...historialModal, open: false })}>
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              {historialModal.filas.length === 0 ? (
                <div className="empty-state" style={{ padding: '24px 0' }}>
                  <h4>Sin cambios de estado</h4>
                  <p>Este equipo no tiene transiciones de estado registradas en el historial.</p>
                </div>
              ) : (
                <div className="table-responsive">
                  <table>
                    <thead>
                      <tr>
                        <th>Fecha</th>
                        <th>Estado Anterior</th>
                        <th>Estado Nuevo</th>
                        <th>Responsable</th>
                        <th>Observación</th>
                      </tr>
                    </thead>
                    <tbody>
                      {historialModal.filas.map((h, i) => (
                        <tr key={i}>
                          <td style={{ fontSize: 12 }}>
                            {h.fechaCambio ? new Date(h.fechaCambio).toLocaleString() : '—'}
                          </td>
                          <td><span className={`badge ${h.estadoAnterior}`}>{h.estadoAnterior}</span></td>
                          <td><span className={`badge ${h.estadoNuevo}`}>{h.estadoNuevo}</span></td>
                          <td style={{ fontWeight: 600 }}>{h.administradorNombre || '—'}</td>
                          <td style={{ fontSize: 12.5 }}>{h.observacion}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
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
