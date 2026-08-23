import { useEffect, useState, useMemo } from 'react'
import { listarUsuarios, crearUsuario } from '../api/index.js'
import {
  Users,
  Plus,
  Search,
  CheckCircle2,
  AlertCircle,
  X,
  RefreshCw,
  Mail,
  UserCheck
} from 'lucide-react'

const emptyForm = { nombres: '', apellidos: '', correo: '', password: '', rol: '' }

const ROL_CONFIG = {
  administrador: { bg: '#eef2ff', text: '#3730a3', border: '#c7d2fe', avatarBg: '#4f46e5' },
  bibliotecario: { bg: '#ecfeff', text: '#155e75', border: '#a5f3fc', avatarBg: '#06b6d4' },
  almacenista: { bg: '#faf5ff', text: '#6b21a8', border: '#e9d5ff', avatarBg: '#8b5cf6' },
  aprendiz: { bg: '#ecfdf5', text: '#065f46', border: '#a7f3d0', avatarBg: '#10b981' },
  instructor: { bg: '#fffbeb', text: '#92400e', border: '#fde68a', avatarBg: '#f59e0b' },
}

function Usuarios() {
  const [usuarios, setUsuarios] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [showModal, setShowModal] = useState(false)
  const [busqueda, setBusqueda] = useState('')
  const [filtroRol, setFiltroRol] = useState('')
  const [message, setMessage] = useState({ type: '', text: '' })
  const [cargando, setCargando] = useState(true)

  const cargar = async () => {
    setCargando(true)
    try {
      const res = await listarUsuarios()
      setUsuarios(res || [])
    } catch (e) {
      setMessage({ type: 'error', text: e.message })
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const usuariosFiltrados = useMemo(() => {
    return usuarios.filter((u) => {
      const matchBusqueda =
        !busqueda ||
        u.nombres?.toLowerCase().includes(busqueda.toLowerCase()) ||
        u.apellidos?.toLowerCase().includes(busqueda.toLowerCase()) ||
        u.correo?.toLowerCase().includes(busqueda.toLowerCase()) ||
        String(u.id).includes(busqueda)
      const matchRol = !filtroRol || u.rol === filtroRol
      return matchBusqueda && matchRol
    })
  }, [usuarios, busqueda, filtroRol])

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await crearUsuario({ ...form })
      setMessage({ type: 'success', text: `Usuario "${form.nombres} ${form.apellidos}" creado exitosamente` })
      setForm(emptyForm)
      setShowModal(false)
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
            placeholder="Buscar por nombre, correo institucional o ID..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
        </div>

        <div style={{ display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap' }}>
          <select
            value={filtroRol}
            onChange={(e) => setFiltroRol(e.target.value)}
            style={{ minWidth: 160 }}
          >
            <option value="">Todos los roles</option>
            <option value="aprendiz">Aprendiz</option>
            <option value="instructor">Instructor</option>
            <option value="bibliotecario">Bibliotecario</option>
            <option value="almacenista">Almacenista</option>
            <option value="administrador">Administrador</option>
          </select>

          <button onClick={() => setShowModal(true)}>
            <Plus size={16} />
            <span>Nuevo Usuario</span>
          </button>

          <button className="secondary" onClick={cargar} title="Recargar">
            <RefreshCw size={15} className={cargando ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Users Table */}
      <div className="table-container">
        <div className="table-responsive">
          <table>
            <thead>
              <tr>
                <th style={{ width: 70 }}>ID</th>
                <th>Usuario</th>
                <th>Correo Institucional</th>
                <th>Rol Asignado</th>
                <th>Estado de Cuenta</th>
              </tr>
            </thead>
            <tbody>
              {cargando && usuariosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={5}>
                    <div style={{ padding: '40px 16px', textAlign: 'center', color: 'var(--text-muted)' }}>
                      <RefreshCw size={26} className="animate-spin" style={{ margin: '0 auto 10px', color: 'var(--primary)' }} />
                      <div style={{ fontSize: 13.5, fontWeight: 600 }}>Cargando usuarios...</div>
                    </div>
                  </td>
                </tr>
              ) : usuariosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={5}>
                    <div className="empty-state">
                      <div className="empty-state-icon">
                        <Users size={24} />
                      </div>
                      <h4>No se encontraron usuarios</h4>
                      <p>No hay registros que coincidan con la búsqueda o filtros seleccionados.</p>
                    </div>
                  </td>
                </tr>
              ) : (
                usuariosFiltrados.map((u) => {
                  const initials = ((u.nombres?.[0] || '') + (u.apellidos?.[0] || '')).toUpperCase() || 'U'
                  const cfg = ROL_CONFIG[u.rol] || { bg: '#f1f5f9', text: '#334155', border: '#e2e8f0', avatarBg: '#64748b' }
                  return (
                    <tr key={u.id}>
                      <td style={{ fontWeight: 700, color: 'var(--text-muted)' }}>#{u.id}</td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                          <div
                            style={{
                              width: 36,
                              height: 36,
                              borderRadius: '50%',
                              background: cfg.avatarBg,
                              color: '#ffffff',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              fontWeight: 700,
                              fontSize: 13,
                              flexShrink: 0,
                              boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                            }}
                          >
                            {initials}
                          </div>
                          <div>
                            <div style={{ fontWeight: 700, color: 'var(--text-main)', fontSize: 14 }}>
                              {u.nombres} {u.apellidos}
                            </div>
                            <div style={{ fontSize: 11.5, color: 'var(--text-muted)' }}>
                              Registrado en plataforma
                            </div>
                          </div>
                        </div>
                      </td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                          <Mail size={14} color="var(--text-muted)" />
                          <span style={{ fontWeight: 500 }}>{u.correo}</span>
                        </div>
                      </td>
                      <td>
                        <span
                          className="badge"
                          style={{
                            background: cfg.bg,
                            color: cfg.text,
                            borderColor: cfg.border,
                            fontWeight: 700
                          }}
                        >
                          {u.rol || 'Sin rol'}
                        </span>
                      </td>
                      <td>
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, fontSize: 12.5, color: 'var(--emerald-800)', background: 'var(--emerald-50)', border: '1px solid var(--emerald-200)', padding: '2px 8px', borderRadius: '999px', fontWeight: 700 }}>
                          <UserCheck size={13} color="var(--emerald-600)" /> Activo
                        </span>
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal: New User */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <div className="stat-icon-wrapper indigo" style={{ width: 34, height: 34 }}>
                  <Users size={18} />
                </div>
                <h3>Registrar Nuevo Usuario</h3>
              </div>
              <button className="modal-close-btn" onClick={() => setShowModal(false)}>
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              <form className="grid" onSubmit={handleSubmit}>
                <label>
                  <span>Nombres *</span>
                  <input
                    name="nombres"
                    value={form.nombres}
                    onChange={handleChange}
                    placeholder="Ej: Carlos Andrés"
                    required
                  />
                </label>

                <label>
                  <span>Apellidos *</span>
                  <input
                    name="apellidos"
                    value={form.apellidos}
                    onChange={handleChange}
                    placeholder="Ej: Gómez Pérez"
                    required
                  />
                </label>

                <label style={{ gridColumn: '1 / -1' }}>
                  <span>Correo Institucional *</span>
                  <input
                    type="email"
                    name="correo"
                    value={form.correo}
                    onChange={handleChange}
                    placeholder="ejemplo@email.com"
                    required
                  />
                </label>

                <label>
                  <span>Contraseña *</span>
                  <input
                    type="password"
                    name="password"
                    value={form.password}
                    onChange={handleChange}
                    placeholder="••••••••"
                    required
                  />
                </label>

                <label>
                  <span>Rol de Usuario *</span>
                  <select name="rol" value={form.rol} onChange={handleChange} required>
                    <option value="">— Seleccionar Rol —</option>
                    <option value="aprendiz">Aprendiz</option>
                    <option value="instructor">Instructor</option>
                    <option value="bibliotecario">Bibliotecario</option>
                    <option value="almacenista">Almacenista</option>
                    <option value="administrador">Administrador</option>
                  </select>
                </label>

                <div style={{ gridColumn: '1 / -1', display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 10 }}>
                  <button type="button" className="secondary" onClick={() => setShowModal(false)}>
                    Cancelar
                  </button>
                  <button type="submit">
                    <CheckCircle2 size={16} />
                    <span>Crear Usuario</span>
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

export default Usuarios
