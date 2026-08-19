import { useEffect, useState, useMemo } from 'react'
import {
  listarLibros,
  crearLibro,
  cambiarEstadoLibro,
  eliminarLibro,
} from '../api/index.js'
import {
  BookOpen,
  Plus,
  Search,
  Filter,
  CheckCircle2,
  AlertCircle,
  Trash2,
  RefreshCw,
  X,
  Layers,
  ChevronLeft,
  ChevronRight
} from 'lucide-react'

import { useAuth } from '../auth/AuthContext.jsx'

const ESTADOS = ['disponible', 'prestado', 'mantenimiento', 'dañado']

const emptyForm = {
  titulo: '',
  autor: '',
  genero: '',
  codigoUnico: '',
  estado: 'disponible',
  ubicacion: '',
  tiempoMaxPrestamo: '',
  descripcion: '',
  proveedor: '',
  responsable: '',
}

function Libros() {
  const { user } = useAuth()
  const puedeGestionar = user?.rol === 'administrador' || user?.rol === 'bibliotecario'
  const [libros, setLibros] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [showModal, setShowModal] = useState(false)
  const [busqueda, setBusqueda] = useState('')
  const [filtroEstado, setFiltroEstado] = useState('')
  const [message, setMessage] = useState({ type: '', text: '' })
  const [cargando, setCargando] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const cargar = async () => {
    setCargando(true)
    try {
      const res = await listarLibros(page, 10)
      if (res && res.content) {
        setLibros(res.content)
        setTotalPages(res.totalPages || 1)
        setTotalElements(res.totalElements || res.content.length)
      } else if (Array.isArray(res)) {
        setLibros(res)
        setTotalPages(1)
        setTotalElements(res.length)
      } else {
        setLibros([])
      }
    } catch (e) {
      setMessage({ type: 'error', text: e.message })
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
  }, [page])

  const librosFiltrados = useMemo(() => {
    return libros.filter((l) => {
      const matchBusqueda =
        !busqueda ||
        l.titulo?.toLowerCase().includes(busqueda.toLowerCase()) ||
        l.autor?.toLowerCase().includes(busqueda.toLowerCase()) ||
        l.codigoUnico?.toLowerCase().includes(busqueda.toLowerCase())
      const matchEstado = !filtroEstado || l.estado === filtroEstado
      return matchBusqueda && matchEstado
    })
  }, [libros, busqueda, filtroEstado])

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await crearLibro({
        ...form,
        tiempoMaxPrestamo: form.tiempoMaxPrestamo
          ? Number(form.tiempoMaxPrestamo)
          : null,
      })
      setMessage({ type: 'success', text: `Libro "${form.titulo}" registrado exitosamente` })
      setForm(emptyForm)
      setShowModal(false)
      cargar()
    } catch (e) {
      setMessage({ type: 'error', text: e.message })
    }
  }

  const handleEstado = async (libro, nuevoEstado) => {
    try {
      await cambiarEstadoLibro(libro.id, nuevoEstado)
      setMessage({ type: 'success', text: `Estado de "${libro.titulo}" actualizado a ${nuevoEstado}` })
      cargar()
    } catch (e) {
      setMessage({ type: 'error', text: e.message })
    }
  }

  const handleEliminar = async (libro) => {
    if (!confirm(`¿Estás seguro de eliminar el libro "${libro.titulo}" (borrado lógico)?`)) return
    try {
      await eliminarLibro(libro.id)
      setMessage({ type: 'success', text: `Libro "${libro.titulo}" eliminado con éxito` })
      cargar()
    } catch (e) {
      setMessage({ type: 'error', text: e.message })
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
            placeholder="Buscar por título, autor o código..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
        </div>

        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <select
            value={filtroEstado}
            onChange={(e) => setFiltroEstado(e.target.value)}
            style={{ minWidth: 150 }}
          >
            <option value="">Todos los estados</option>
            {ESTADOS.map((s) => (
              <option key={s} value={s}>
                {s.charAt(0).toUpperCase() + s.slice(1)}
              </option>
            ))}
          </select>

          {puedeGestionar && (
            <button onClick={() => setShowModal(true)}>
              <Plus size={16} />
              <span>Nuevo Libro</span>
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
                <th>Título</th>
                <th>Autor</th>
                <th>Género</th>
                <th>Código Único</th>
                <th>Estado</th>
                <th>Disponible</th>
                <th>Ubicación</th>
                {puedeGestionar && <th style={{ textAlign: 'right' }}>Acciones</th>}
              </tr>
            </thead>
            <tbody>
              {cargando && librosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={puedeGestionar ? 9 : 8}>
                    <div style={{ padding: '36px 16px', textAlign: 'center', color: 'var(--text-muted)' }}>
                      <RefreshCw size={24} className="animate-spin" style={{ margin: '0 auto 10px', color: 'var(--primary)' }} />
                      <div style={{ fontSize: 13, fontWeight: 500 }}>Cargando catálogo de libros...</div>
                    </div>
                  </td>
                </tr>
              ) : librosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={puedeGestionar ? 9 : 8}>
                    <div className="empty-state">
                      <div className="empty-state-icon">
                        <BookOpen size={24} />
                      </div>
                      <h4>No se encontraron libros</h4>
                      <p>
                        {busqueda || filtroEstado
                          ? 'No hay registros que coincidan con los filtros aplicados.'
                          : 'Aún no hay libros registrados en el catálogo.'}
                      </p>
                      {!busqueda && !filtroEstado && puedeGestionar && (
                        <button onClick={() => setShowModal(true)} style={{ marginTop: 14 }}>
                          <Plus size={15} />
                          <span>Registrar Primer Libro</span>
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ) : (
                librosFiltrados.map((libro) => (
                  <tr key={libro.id}>
                    <td style={{ fontWeight: 600, color: 'var(--text-muted)' }}>#{libro.id}</td>
                    <td>
                      <div style={{ fontWeight: 700, color: 'var(--text-main)' }}>{libro.titulo}</div>
                      {libro.descripcion && (
                        <div style={{ fontSize: 12, color: 'var(--text-muted)', maxWidth: 260, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {libro.descripcion}
                        </div>
                      )}
                    </td>
                    <td>{libro.autor}</td>
                    <td>
                      <span style={{ fontSize: 12.5, background: 'var(--bg-subtle)', padding: '2px 8px', borderRadius: 4 }}>
                        {libro.genero}
                      </span>
                    </td>
                    <td>
                      <code style={{ background: '#f1f5f9', padding: '2px 6px', borderRadius: 4, fontSize: 12 }}>
                        {libro.codigoUnico}
                      </code>
                    </td>
                    <td>
                      <span className={`badge ${libro.estado}`}>{libro.estado}</span>
                    </td>
                    <td>
                      {libro.disponiblePrestamo ? (
                        <span style={{ color: 'var(--emerald)', fontWeight: 600, fontSize: 13 }}>● Sí</span>
                      ) : (
                        <span style={{ color: 'var(--rose)', fontWeight: 600, fontSize: 13 }}>● No</span>
                      )}
                    </td>
                    <td style={{ fontSize: 13, color: 'var(--text-secondary)' }}>{libro.ubicacion || '—'}</td>
                    {puedeGestionar && (
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: 6, alignItems: 'center' }}>
                          <select
                            className="form-select"
                            style={{ padding: '4px 8px', fontSize: 12, width: 'auto' }}
                            value={libro.estado}
                            onChange={(e) => handleEstado(libro, e.target.value)}
                          >
                            {ESTADOS.map((s) => (
                              <option key={s} value={s}>
                                → {s}
                              </option>
                            ))}
                          </select>
                          <button
                            className="danger small"
                            onClick={() => handleEliminar(libro)}
                            title="Eliminar libro"
                          >
                            <Trash2 size={13} />
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Wrapper */}
        {totalPages > 1 && (
          <div className="pagination-wrapper">
            <span>
              Mostrando página <strong>{page + 1}</strong> de <strong>{totalPages}</strong> ({totalElements} libros en total)
            </span>
            <div className="pagination-controls">
              <button
                className="secondary small"
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
              >
                <ChevronLeft size={14} />
                <span>Anterior</span>
              </button>
              <button
                className="secondary small"
                disabled={page >= totalPages - 1}
                onClick={() => setPage(page + 1)}
              >
                <span>Siguiente</span>
                <ChevronRight size={14} />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Modal for Creating Book */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <div className="sidebar-brand-icon" style={{ width: 32, height: 32 }}>
                  <BookOpen size={16} />
                </div>
                <h3>Registrar Nuevo Libro</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setShowModal(false)}>
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              <form className="grid" onSubmit={handleSubmit}>
                <label style={{ gridColumn: '1 / -1' }}>
                  <span>Título del Libro *</span>
                  <input
                    name="titulo"
                    value={form.titulo}
                    onChange={handleChange}
                    placeholder="Ej: Cien Años de Soledad"
                    required
                  />
                </label>

                <label>
                  <span>Autor *</span>
                  <input
                    name="autor"
                    value={form.autor}
                    onChange={handleChange}
                    placeholder="Ej: Gabriel García Márquez"
                    required
                  />
                </label>

                <label>
                  <span>Género Literario / Categoría *</span>
                  <input
                    name="genero"
                    value={form.genero}
                    onChange={handleChange}
                    placeholder="Ej: Novela, Ciencia, Tecnología"
                    required
                  />
                </label>

                <label>
                  <span>Código Único / ISBN *</span>
                  <input
                    name="codigoUnico"
                    value={form.codigoUnico}
                    onChange={handleChange}
                    placeholder="Ej: LIB-2026-001"
                    required
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
                  <span>Ubicación en Estante</span>
                  <input
                    name="ubicacion"
                    value={form.ubicacion}
                    onChange={handleChange}
                    placeholder="Ej: Pasillo A - Estante 3"
                  />
                </label>

                <label>
                  <span>Tiempo Máx. Préstamo (Días)</span>
                  <input
                    type="number"
                    name="tiempoMaxPrestamo"
                    value={form.tiempoMaxPrestamo}
                    onChange={handleChange}
                    placeholder="Ej: 7"
                  />
                </label>

                <label>
                  <span>Proveedor</span>
                  <input
                    name="proveedor"
                    value={form.proveedor}
                    onChange={handleChange}
                    placeholder="Editorial o Donante"
                  />
                </label>

                <label>
                  <span>Responsable</span>
                  <input
                    name="responsable"
                    value={form.responsable}
                    onChange={handleChange}
                    placeholder="Nombre del encargado"
                  />
                </label>

                <label style={{ gridColumn: '1 / -1' }}>
                  <span>Descripción o Reseña</span>
                  <textarea
                    name="descripcion"
                    value={form.descripcion}
                    onChange={handleChange}
                    placeholder="Breve descripción del estado o contenido del libro..."
                  />
                </label>

                <div style={{ gridColumn: '1 / -1', display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 10 }}>
                  <button type="button" className="secondary" onClick={() => setShowModal(false)}>
                    Cancelar
                  </button>
                  <button type="submit">
                    <CheckCircle2 size={16} />
                    <span>Guardar Libro</span>
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

export default Libros
