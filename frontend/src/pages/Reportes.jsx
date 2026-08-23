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
  FileSpreadsheet,
  FileText,
  Laptop,
  BookOpen,
  Users,
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
      <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 20 }}>
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
                <span style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-muted)' }}>Tipo de recurso:</span>
                <select
                  style={{ width: 150, padding: '6px 10px' }}
                  value={filtros.tipoRecurso}
                  onChange={(e) => setFiltros({ ...filtros, tipoRecurso: e.target.value })}
                >
                  <option value="">Todos</option>
                  <option value="libro">Solo Libros</option>
                  <option value="equipo">Solo Equipos</option>
                </select>
              </div>
            )}

            <button className="secondary small" onClick={cargar} title="Actualizar">
              <RefreshCw size={14} className={cargando ? 'animate-spin' : ''} />
              <span>Filtrar</span>
            </button>
          </div>

          {/* Export Action Buttons */}
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {vista === 'inventario' && (
              <>
                {VE_LIBROS(user?.rol) && (
                  <button
                    className="secondary small"
                    onClick={() => exportar('inventario-libros')}
                    disabled={descargando === 'inventario-libros_excel'}
                    style={{ background: '#ecfdf5', color: '#065f46', borderColor: '#a7f3d0' }}
                  >
                    <FileSpreadsheet size={14} />
                    <span>Libros (.xlsx)</span>
                  </button>
                )}
                {VE_EQUIPOS(user?.rol) && (
                  <button
                    className="secondary small"
                    onClick={() => exportar('inventario-equipos')}
                    disabled={descargando === 'inventario-equipos_excel'}
                    style={{ background: '#eef2ff', color: '#3730a3', borderColor: '#c7d2fe' }}
                  >
                    <FileSpreadsheet size={14} />
                    <span>Equipos (.xlsx)</span>
                  </button>
                )}
              </>
            )}

            {vista === 'prestamos' && (
              <>
                <button
                  className="secondary small"
                  onClick={() => exportar('prestamos-general')}
                  disabled={descargando === 'prestamos-general_excel'}
                  style={{ background: '#ecfdf5', color: '#065f46', borderColor: '#a7f3d0' }}
                >
                  <FileSpreadsheet size={14} />
                  <span>Excel (.xlsx)</span>
                </button>
                <button
                  className="secondary small"
                  onClick={() => exportar('prestamos-general', 'pdf')}
                  disabled={descargando === 'prestamos-general_pdf'}
                  style={{ background: '#fef2f2', color: '#991b1b', borderColor: '#fecaca' }}
                >
                  <FileText size={14} />
                  <span>PDF (.pdf)</span>
                </button>
              </>
            )}

            {vista === 'mis-prestamos' && (
              <button
                className="secondary small"
                onClick={() => exportar('mis-prestamos')}
                disabled={descargando === 'mis-prestamos_excel'}
                style={{ background: '#ecfdf5', color: '#065f46', borderColor: '#a7f3d0' }}
              >
                <FileSpreadsheet size={14} />
                <span>Mis Préstamos (.xlsx)</span>
              </button>
            )}
          </div>
        </div>
      )}

      {/* Loading state */}
      {cargando && (
        <div className="card" style={{ textAlign: 'center', padding: '40px 16px' }}>
          <RefreshCw size={26} className="animate-spin" style={{ margin: '0 auto 10px', color: 'var(--primary)' }} />
          <p style={{ color: 'var(--text-muted)', fontSize: 13.5, fontWeight: 600 }}>Generando reporte y consolidando datos...</p>
        </div>
      )}

      {/* DATA VIEW 1: VISTA INVENTARIO */}
      {!cargando && vista === 'inventario' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
          {VE_LIBROS(user?.rol) && (
            <div className="table-container">
              <div className="card-header" style={{ padding: '16px 20px', borderBottom: '1px solid var(--card-border)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <BookOpen size={18} color="var(--primary)" />
                  <h3>Inventario de Libros ({libros.length})</h3>
                </div>
              </div>
              <div className="table-responsive">
                <table>
                  <thead>
                    <tr>
                      <th>Título</th>
                      <th>Autor</th>
                      <th>Código</th>
                      <th>Estado</th>
                      <th>Ubicación</th>
                    </tr>
                  </thead>
                  <tbody>
                    {libros.length === 0 ? (
                      <tr><td colSpan={5} style={{ textAlign: 'center', padding: 24, color: 'var(--text-muted)' }}>No hay libros en este reporte</td></tr>
                    ) : (
                      libros.map((l) => (
                        <tr key={l.id}>
                          <td style={{ fontWeight: 700, color: 'var(--text-main)' }}>{l.titulo}</td>
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

          {VE_EQUIPOS(user?.rol) && (
            <div className="table-container">
              <div className="card-header" style={{ padding: '16px 20px', borderBottom: '1px solid var(--card-border)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <Laptop size={18} color="var(--primary)" />
                  <h3>Inventario de Equipos ({equipos.length})</h3>
                </div>
              </div>
              <div className="table-responsive">
                <table>
                  <thead>
                    <tr>
                      <th>Nombre</th>
                      <th>Tipo</th>
                      <th>Marca / Modelo</th>
                      <th>Serie</th>
                      <th>Estado</th>
                      <th>Ubicación</th>
                    </tr>
                  </thead>
                  <tbody>
                    {equipos.length === 0 ? (
                      <tr><td colSpan={6} style={{ textAlign: 'center', padding: 24, color: 'var(--text-muted)' }}>No hay equipos en este reporte</td></tr>
                    ) : (
                      equipos.map((e) => (
                        <tr key={e.id}>
                          <td style={{ fontWeight: 700, color: 'var(--text-main)' }}>{e.nombre}</td>
                          <td>{e.tipoEquipo}</td>
                          <td>{e.marca} {e.modelo}</td>
                          <td><code>{e.numeroSerie || '—'}</code></td>
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
        </div>
      )}

      {/* DATA VIEW 2: VISTA HISTORIAL PRÉSTAMOS */}
      {!cargando && (vista === 'prestamos' || vista === 'mis-prestamos') && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
          <div className="table-container">
            <div className="card-header" style={{ padding: '16px 20px', borderBottom: '1px solid var(--card-border)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <BookOpen size={18} color="var(--primary)" />
                <h3>Préstamos de Libros ({libros.length})</h3>
              </div>
            </div>
            <div className="table-responsive">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Libro</th>
                    {vista === 'prestamos' && <th>Usuario</th>}
                    <th>Fecha Préstamo</th>
                    <th>Devolución Esperada</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {libros.length === 0 ? (
                    <tr><td colSpan={6} style={{ textAlign: 'center', padding: 24, color: 'var(--text-muted)' }}>No hay registros de préstamos de libros</td></tr>
                  ) : (
                    libros.map((p) => (
                      <tr key={p.id}>
                        <td style={{ fontWeight: 700, color: 'var(--text-muted)' }}>#{p.id}</td>
                        <td style={{ fontWeight: 700, color: 'var(--text-main)' }}>{p.libroTitulo || `Libro #${p.libroId}`}</td>
                        {vista === 'prestamos' && <td>{p.usuarioNombre}</td>}
                        <td>{p.fechaPrestamo ? new Date(p.fechaPrestamo).toLocaleDateString() : '—'}</td>
                        <td>{p.fechaDevolucionEsperada ? new Date(p.fechaDevolucionEsperada).toLocaleDateString() : '—'}</td>
                        <td><span className={`badge ${p.estado}`}>{p.estado}</span></td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>

          <div className="table-container">
            <div className="card-header" style={{ padding: '16px 20px', borderBottom: '1px solid var(--card-border)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <Laptop size={18} color="var(--primary)" />
                <h3>Préstamos de Equipos ({equipos.length})</h3>
              </div>
            </div>
            <div className="table-responsive">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Equipo</th>
                    {vista === 'prestamos' && <th>Usuario</th>}
                    <th>Fecha Préstamo</th>
                    <th>Devolución Esperada</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {equipos.length === 0 ? (
                    <tr><td colSpan={6} style={{ textAlign: 'center', padding: 24, color: 'var(--text-muted)' }}>No hay registros de préstamos de equipos</td></tr>
                  ) : (
                    equipos.map((p) => (
                      <tr key={p.id}>
                        <td style={{ fontWeight: 700, color: 'var(--text-muted)' }}>#{p.id}</td>
                        <td style={{ fontWeight: 700, color: 'var(--text-main)' }}>{p.equipoNombre || p.recursoNombre || `Equipo #${p.equipoId}`}</td>
                        {vista === 'prestamos' && <td>{p.usuarioNombre}</td>}
                        <td>{p.fechaPrestamo ? new Date(p.fechaPrestamo).toLocaleDateString() : '—'}</td>
                        <td>{p.fechaDevolucionEsperada ? new Date(p.fechaDevolucionEsperada).toLocaleDateString() : '—'}</td>
                        <td><span className={`badge ${p.estado}`}>{p.estado}</span></td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* DATA VIEW 3: VISTA USUARIOS ACTIVOS */}
      {!cargando && vista === 'usuarios-activos' && (
        <div className="table-container">
          <div className="card-header" style={{ padding: '16px 20px', borderBottom: '1px solid var(--card-border)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Users size={18} color="var(--primary)" />
              <h3>Usuarios Activos en el Sistema ({usuariosAct.length})</h3>
            </div>
          </div>
          <div className="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre Completo</th>
                  <th>Correo Institucional</th>
                  <th>Rol</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {usuariosAct.length === 0 ? (
                  <tr><td colSpan={5} style={{ textAlign: 'center', padding: 24, color: 'var(--text-muted)' }}>No hay usuarios activos registrados</td></tr>
                ) : (
                  usuariosAct.map((u) => (
                    <tr key={u.id}>
                      <td style={{ fontWeight: 700, color: 'var(--text-muted)' }}>#{u.id}</td>
                      <td style={{ fontWeight: 700, color: 'var(--text-main)' }}>{u.nombres} {u.apellidos}</td>
                      <td>{u.correo}</td>
                      <td><span className="badge" style={{ background: 'var(--bg-subtle)', color: 'var(--primary)' }}>{u.rol}</span></td>
                      <td><span style={{ color: 'var(--emerald-800)', fontWeight: 600 }}>● Activo</span></td>
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
