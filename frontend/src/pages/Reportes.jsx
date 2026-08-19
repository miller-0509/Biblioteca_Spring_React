import { useEffect, useState } from 'react'
import {
  obtenerInventario,
  obtenerReportePrestamos,
  obtenerMisPrestamos,
  obtenerUsuariosActivos,
  exportarExcel,
  exportarPdf,
} from '../api/reportes.js'
import { useAuth } from '../auth/AuthContext.jsx'
import {
  BarChart3,
  FileSpreadsheet,
  FileText,
  Download,
  Laptop,
  BookOpen,
  Users,
  Search,
  CheckCircle2,
  AlertCircle,
  X,
  RefreshCw,
  Layers,
  CalendarCheck
} from 'lucide-react'

const ES_STAFF = (rol) => rol !== 'aprendiz' && rol !== 'instructor'
const ES_ADMIN = (rol) => rol === 'administrador'
const VE_EQUIPOS = (rol) => rol === 'administrador' || rol === 'almacenista'
const VE_LIBROS = (rol) => rol === 'administrador' || rol === 'bibliotecario'

const VISTAS = [
  { id: 'inventario', label: 'Inventario General', icon: Layers, staff: true },
  { id: 'prestamos', label: 'Historial Préstamos', icon: CalendarCheck, staff: true },
  { id: 'mis-prestamos', label: 'Mis Préstamos', icon: BookOpen, staff: false },
  { id: 'usuarios-activos', label: 'Usuarios Activos', icon: Users, admin: true },
]

function Reportes() {
  const { user } = useAuth()
  const [vista, setVista] = useState(() => (ES_STAFF(user?.rol) ? 'inventario' : 'mis-prestamos'))
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [descargando, setDescargando] = useState('')
  const [filtros, setFiltros] = useState({ estado: '', tipoRecurso: '', tipo: '' })
  const [cargando, setCargando] = useState(false)

  const visible = (v) => {
    if (v.admin && !ES_ADMIN(user?.rol)) return false
    if (v.staff && !ES_STAFF(user?.rol)) return false
    return true
  }

  const cargar = async () => {
    setCargando(true)
    setError('')
    try {
      if (vista === 'inventario') {
        setData(await obtenerInventario({ estado: filtros.estado, tipo: filtros.tipo }))
      } else if (vista === 'prestamos') {
        setData(await obtenerReportePrestamos({ estado: filtros.estado, tipoRecurso: filtros.tipoRecurso }))
      } else if (vista === 'mis-prestamos') {
        setData(await obtenerMisPrestamos(filtros.estado))
      } else if (vista === 'usuarios-activos') {
        setData(await obtenerUsuariosActivos())
      }
    } catch (e) {
      setError(e.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [vista, filtros.estado, filtros.tipoRecurso, filtros.tipo])

  const equipos = data?.equipos || data?.prestamosEquipos || []
  const libros = data?.libros || data?.prestamosLibros || []
  const usuariosAct = data?.usuarios || []

  const exportar = async (tipo, formato = 'excel') => {
    const key = `${tipo}_${formato}`
    setDescargando(key)
    try {
      if (formato === 'pdf') {
        await exportarPdf(tipo)
      } else {
        await exportarExcel(tipo)
      }
    } catch (e) {
      setError(e.message)
    } finally {
      setDescargando('')
    }
  }

  return (
    <div className="animate-fade-in">
      {error && (
        <div className="alert error">
          <AlertCircle size={18} />
          <span style={{ flex: 1 }}>{error}</span>
          <button onClick={() => setError('')} style={{ background: 'transparent', border: 'none', color: 'inherit', padding: 0 }}>
            <X size={16} />
          </button>
        </div>
      )}

      {/* Segmented View Navigation */}
      <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 18 }}>
        {VISTAS.filter(visible).map((v) => {
          const Icon = v.icon
          const isActive = vista === v.id
          return (
            <button
              key={v.id}
              className={isActive ? '' : 'secondary'}
              onClick={() => setVista(v.id)}
              style={{ padding: '10px 18px', fontSize: 13.5 }}
            >
              <Icon size={16} />
              <span>{v.label}</span>
            </button>
          )
        })}
      </div>

      {/* Filter and Export Action Bar */}
      {vista !== 'usuarios-activos' && (
        <div className="filter-bar" style={{ justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', alignItems: 'center', flex: 1 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <span style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-muted)' }}>Estado:</span>
              <input
                style={{ width: 140, padding: '6px 10px' }}
                placeholder="Ej: disponible"
                value={filtros.estado}
                onChange={(e) => setFiltros({ ...filtros, estado: e.target.value })}
              />
            </div>

            {vista === 'inventario' && VE_EQUIPOS(user?.rol) && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                <span style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-muted)' }}>Tipo de equipo:</span>
                <input
                  style={{ width: 140, padding: '6px 10px' }}
                  placeholder="Ej: Portátil"
                  value={filtros.tipo}
                  onChange={(e) => setFiltros({ ...filtros, tipo: e.target.value })}
                />
              </div>
            )}

            {vista === 'prestamos' && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                <span style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-muted)' }}>Tipo recurso:</span>
                <select
                  style={{ width: 140, padding: '6px 10px' }}
                  value={filtros.tipoRecurso}
                  onChange={(e) => setFiltros({ ...filtros, tipoRecurso: e.target.value })}
                >
                  <option value="">Todos</option>
                  <option value="equipos">Equipos</option>
                  <option value="libros">Libros</option>
                </select>
              </div>
            )}

            <button className="secondary small" onClick={cargar} title="Actualizar datos">
              <RefreshCw size={14} className={cargando ? 'animate-spin' : ''} />
            </button>
          </div>

          {/* Export Buttons */}
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
            <span style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 4 }}>
              <Download size={14} /> Exportar:
            </span>

            {vista === 'inventario' && VE_EQUIPOS(user?.rol) && (
              <>
                <button
                  className="success small"
                  disabled={descargando === 'inventario_equipos_excel'}
                  onClick={() => exportar('inventario_equipos', 'excel')}
                  title="Exportar a Excel"
                >
                  <FileSpreadsheet size={13} />
                  <span>Equipos XLSX</span>
                </button>
                <button
                  className="small"
                  style={{ background: '#e11d48' }}
                  disabled={descargando === 'inventario_equipos_pdf'}
                  onClick={() => exportar('inventario_equipos', 'pdf')}
                  title="Exportar a PDF"
                >
                  <FileText size={13} />
                  <span>Equipos PDF</span>
                </button>
              </>
            )}

            {vista === 'inventario' && VE_LIBROS(user?.rol) && (
              <>
                <button
                  className="success small"
                  disabled={descargando === 'inventario_libros_excel'}
                  onClick={() => exportar('inventario_libros', 'excel')}
                  title="Exportar a Excel"
                >
                  <FileSpreadsheet size={13} />
                  <span>Libros XLSX</span>
                </button>
                <button
                  className="small"
                  style={{ background: '#e11d48' }}
                  disabled={descargando === 'inventario_libros_pdf'}
                  onClick={() => exportar('inventario_libros', 'pdf')}
                  title="Exportar a PDF"
                >
                  <FileText size={13} />
                  <span>Libros PDF</span>
                </button>
              </>
            )}

            {vista === 'prestamos' && VE_EQUIPOS(user?.rol) && (
              <>
                <button
                  className="success small"
                  disabled={descargando === 'prestamos_equipos_excel'}
                  onClick={() => exportar('prestamos_equipos', 'excel')}
                  title="Exportar a Excel"
                >
                  <FileSpreadsheet size={13} />
                  <span>Préstamos Equipos XLSX</span>
                </button>
                <button
                  className="small"
                  style={{ background: '#e11d48' }}
                  disabled={descargando === 'prestamos_equipos_pdf'}
                  onClick={() => exportar('prestamos_equipos', 'pdf')}
                  title="Exportar a PDF"
                >
                  <FileText size={13} />
                  <span>Préstamos Equipos PDF</span>
                </button>
              </>
            )}

            {vista === 'prestamos' && VE_LIBROS(user?.rol) && (
              <>
                <button
                  className="success small"
                  disabled={descargando === 'prestamos_libros_excel'}
                  onClick={() => exportar('prestamos_libros', 'excel')}
                  title="Exportar a Excel"
                >
                  <FileSpreadsheet size={13} />
                  <span>Préstamos Libros XLSX</span>
                </button>
                <button
                  className="small"
                  style={{ background: '#e11d48' }}
                  disabled={descargando === 'prestamos_libros_pdf'}
                  onClick={() => exportar('prestamos_libros', 'pdf')}
                  title="Exportar a PDF"
                >
                  <FileText size={13} />
                  <span>Préstamos Libros PDF</span>
                </button>
              </>
            )}

            {vista === 'mis-prestamos' && (
              <>
                <button
                  className="success small"
                  disabled={descargando === 'mis_prestamos_excel'}
                  onClick={() => exportar('mis_prestamos', 'excel')}
                  title="Exportar a Excel"
                >
                  <FileSpreadsheet size={13} />
                  <span>Mis Préstamos XLSX</span>
                </button>
                <button
                  className="small"
                  style={{ background: '#e11d48' }}
                  disabled={descargando === 'mis_prestamos_pdf'}
                  onClick={() => exportar('mis_prestamos', 'pdf')}
                  title="Exportar a PDF"
                >
                  <FileText size={13} />
                  <span>Mis Préstamos PDF</span>
                </button>
              </>
            )}
          </div>
        </div>
      )}

      {/* Reports Table Data */}
      {vista === 'inventario' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
          {VE_EQUIPOS(user?.rol) && (
            <div className="table-container">
              <div className="card-header" style={{ padding: '16px 20px', margin: 0, background: '#f8fafc' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <Laptop size={18} color="var(--primary)" />
                  <h3>Inventario de Equipos ({equipos.length})</h3>
                </div>
              </div>
              <div className="table-responsive">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Nombre</th>
                      <th>Tipo</th>
                      <th>N° Serie</th>
                      <th>Estado</th>
                      <th>Ubicación</th>
                    </tr>
                  </thead>
                  <tbody>
                    {equipos.length === 0 ? (
                      <tr><td colSpan={6} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Sin equipos</td></tr>
                    ) : (
                      equipos.map((e) => (
                        <tr key={e.id}>
                          <td style={{ fontWeight: 600, color: 'var(--text-muted)' }}>#{e.id}</td>
                          <td style={{ fontWeight: 700 }}>{e.nombre}</td>
                          <td><span style={{ fontSize: 12, background: 'var(--bg-subtle)', padding: '2px 8px', borderRadius: 4 }}>{e.tipoEquipo}</span></td>
                          <td><code>{e.numeroSerie}</code></td>
                          <td><span className={`badge ${e.estado}`}>{e.estado}</span></td>
                          <td>{e.ubicacion || '—'}</td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {VE_LIBROS(user?.rol) && (
            <div className="table-container">
              <div className="card-header" style={{ padding: '16px 20px', margin: 0, background: '#f8fafc' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <BookOpen size={18} color="var(--emerald)" />
                  <h3>Inventario de Libros ({libros.length})</h3>
                </div>
              </div>
              <div className="table-responsive">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Título</th>
                      <th>Autor</th>
                      <th>Código Único</th>
                      <th>Estado</th>
                      <th>Ubicación</th>
                    </tr>
                  </thead>
                  <tbody>
                    {libros.length === 0 ? (
                      <tr><td colSpan={6} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Sin libros</td></tr>
                    ) : (
                      libros.map((l) => (
                        <tr key={l.id}>
                          <td style={{ fontWeight: 600, color: 'var(--text-muted)' }}>#{l.id}</td>
                          <td style={{ fontWeight: 700 }}>{l.titulo}</td>
                          <td>{l.autor}</td>
                          <td><code>{l.codigoUnico}</code></td>
                          <td><span className={`badge ${l.estado}`}>{l.estado}</span></td>
                          <td>{l.ubicacion || '—'}</td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}

      {vista === 'prestamos' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
          {VE_EQUIPOS(user?.rol) && (
            <div className="table-container">
              <div className="card-header" style={{ padding: '16px 20px', margin: 0, background: '#f8fafc' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <Laptop size={18} color="var(--primary)" />
                  <h3>Reporte de Préstamos de Equipos ({equipos.length})</h3>
                </div>
              </div>
              <div className="table-responsive">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Usuario</th>
                      <th>Equipo</th>
                      <th>Estado</th>
                      <th>Fecha Solicitud</th>
                    </tr>
                  </thead>
                  <tbody>
                    {equipos.length === 0 ? (
                      <tr><td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Sin registros</td></tr>
                    ) : (
                      equipos.map((p) => (
                        <tr key={p.id}>
                          <td style={{ fontWeight: 600, color: 'var(--text-muted)' }}>#{p.id}</td>
                          <td style={{ fontWeight: 700 }}>{p.usuarioNombre}</td>
                          <td>{p.equipoNombre}</td>
                          <td><span className={`badge ${p.estado}`}>{p.estado}</span></td>
                          <td>{p.fechaSolicitud ? new Date(p.fechaSolicitud).toLocaleDateString() : '—'}</td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {VE_LIBROS(user?.rol) && (
            <div className="table-container">
              <div className="card-header" style={{ padding: '16px 20px', margin: 0, background: '#f8fafc' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <BookOpen size={18} color="var(--emerald)" />
                  <h3>Reporte de Préstamos de Libros ({libros.length})</h3>
                </div>
              </div>
              <div className="table-responsive">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Usuario</th>
                      <th>Libro</th>
                      <th>Estado</th>
                      <th>Fecha Solicitud</th>
                    </tr>
                  </thead>
                  <tbody>
                    {libros.length === 0 ? (
                      <tr><td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Sin registros</td></tr>
                    ) : (
                      libros.map((p) => (
                        <tr key={p.id}>
                          <td style={{ fontWeight: 600, color: 'var(--text-muted)' }}>#{p.id}</td>
                          <td style={{ fontWeight: 700 }}>{p.usuarioNombre}</td>
                          <td>{p.libroTitulo}</td>
                          <td><span className={`badge ${p.estado}`}>{p.estado}</span></td>
                          <td>{p.fechaSolicitud ? new Date(p.fechaSolicitud).toLocaleDateString() : '—'}</td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}

      {vista === 'mis-prestamos' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
          <div className="table-container">
            <div className="card-header" style={{ padding: '16px 20px', margin: 0, background: '#f8fafc' }}>
              <h3>Mis Préstamos Históricos</h3>
            </div>
            <div className="table-responsive">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Recurso</th>
                    <th>Tipo</th>
                    <th>Estado</th>
                    <th>Fecha Solicitud</th>
                  </tr>
                </thead>
                <tbody>
                  {[...equipos.map((e) => ({ ...e, tipoItem: 'Equipo', nombre: e.equipoNombre })),
                    ...libros.map((l) => ({ ...l, tipoItem: 'Libro', nombre: l.libroTitulo }))].length === 0 ? (
                    <tr><td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No tienes préstamos registrados</td></tr>
                  ) : (
                    [...equipos.map((e) => ({ ...e, tipoItem: 'Equipo', nombre: e.equipoNombre })),
                     ...libros.map((l) => ({ ...l, tipoItem: 'Libro', nombre: l.libroTitulo }))].map((item, idx) => (
                      <tr key={idx}>
                        <td style={{ fontWeight: 600, color: 'var(--text-muted)' }}>#{item.id}</td>
                        <td style={{ fontWeight: 700 }}>{item.nombre}</td>
                        <td><span style={{ fontSize: 12, background: 'var(--bg-subtle)', padding: '2px 8px', borderRadius: 4 }}>{item.tipoItem}</span></td>
                        <td><span className={`badge ${item.estado}`}>{item.estado}</span></td>
                        <td>{item.fechaSolicitud ? new Date(item.fechaSolicitud).toLocaleDateString() : '—'}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {vista === 'usuarios-activos' && (
        <div className="table-container">
          <div className="card-header" style={{ padding: '16px 20px', margin: 0, background: '#f8fafc' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Users size={18} color="var(--primary)" />
              <h3>Usuarios Activos con Préstamos ({usuariosAct.length})</h3>
            </div>
          </div>
          <div className="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre Completo</th>
                  <th>Rol</th>
                  <th>Préstamos Activos</th>
                </tr>
              </thead>
              <tbody>
                {usuariosAct.length === 0 ? (
                  <tr><td colSpan={4} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Sin usuarios activos</td></tr>
                ) : (
                  usuariosAct.map((u) => (
                    <tr key={u.id}>
                      <td style={{ fontWeight: 600, color: 'var(--text-muted)' }}>#{u.id}</td>
                      <td style={{ fontWeight: 700 }}>{u.nombre}</td>
                      <td><span className="badge">{u.rol}</span></td>
                      <td>
                        <strong style={{ color: 'var(--primary)', fontSize: 14 }}>{u.prestamosActivos}</strong>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

export default Reportes
