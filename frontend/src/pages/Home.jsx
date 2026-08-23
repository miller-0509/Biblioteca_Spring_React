import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { obtenerDashboard } from '../api/reportes.js'
import { useAuth } from '../auth/AuthContext.jsx'
import {
  BookOpen,
  Laptop,
  BookmarkCheck,
  CalendarCheck,
  AlertTriangle,
  Users,
  ShieldAlert,
  ArrowRight,
  TrendingUp,
  Sparkles,
  Layers,
  FileSpreadsheet
} from 'lucide-react'

const TITULOS = {
  administrador: 'Panel de Administración Global',
  bibliotecario: 'Gestión y Préstamos de Biblioteca',
  almacenista: 'Control y Almacén de Equipos',
  aprendiz: 'Portal del Aprendiz SENA',
  instructor: 'Portal del Instructor SENA',
}

const META_ESTADISTICAS = {
  totalEquipos: { label: 'Equipos Registrados', icon: Laptop, color: 'indigo' },
  totalLibros: { label: 'Libros en Catálogo', icon: BookOpen, color: 'emerald' },
  equiposPrestados: { label: 'Equipos en Préstamo', icon: Laptop, color: 'amber' },
  librosPrestados: { label: 'Libros en Préstamo', icon: BookOpen, color: 'purple' },
  prestamosEquiposActivos: { label: 'Préstamos Equipos Activos', icon: CalendarCheck, color: 'indigo' },
  prestamosLibrosActivos: { label: 'Préstamos Libros Activos', icon: BookmarkCheck, color: 'emerald' },
  multasActivas: { label: 'Sanciones Activas', icon: AlertTriangle, color: 'rose' },
  usuariosActivos: { label: 'Usuarios Registrados', icon: Users, color: 'indigo' },
  sancionesVigentes: { label: 'Sanciones Vigentes', icon: AlertTriangle, color: 'rose' },
  multasLibros: { label: 'Sanciones de Libros', icon: AlertTriangle, color: 'rose' },
  multasEquipos: { label: 'Sanciones de Equipos', icon: AlertTriangle, color: 'rose' },
  prestamosActivosUsuario: { label: 'Mis Préstamos Activos', icon: BookmarkCheck, color: 'indigo' },
  multasPendientesUsuario: { label: 'Mis Sanciones', icon: AlertTriangle, color: 'rose' },
  limitePrestamos: { label: 'Límite Máximo Préstamos', icon: Layers, color: 'purple' },
}

function Home() {
  const { user } = useAuth()
  const [stats, setStats] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    obtenerDashboard()
      .then(setStats)
      .catch((e) => setError(e.message))
  }, [])

  const tieneMultas = stats?.tieneMultasPendientes
  const puedeLibros = user?.rol === 'administrador' || user?.rol === 'bibliotecario'
  const puedeEquipos = user?.rol === 'administrador' || user?.rol === 'almacenista'

  return (
    <div className="animate-fade-in">
      {/* Hero Welcome Banner */}
      <div
        className="card"
        style={{
          background: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 45%, #4338ca 100%)',
          color: '#ffffff',
          border: 'none',
          boxShadow: '0 12px 28px -6px rgba(49, 46, 129, 0.35)',
          padding: '30px 32px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 20,
          borderRadius: 18
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
            <span
              style={{
                background: 'rgba(255, 255, 255, 0.18)',
                backdropFilter: 'blur(8px)',
                padding: '4px 12px',
                borderRadius: '999px',
                fontSize: 11.5,
                fontWeight: 700,
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
                color: '#ffffff'
              }}
            >
              {user?.rol}
            </span>
            <span style={{ fontSize: 13, color: '#e0e7ff', fontWeight: 500 }}>
              • Centro de Servicios y Gestión SENA
            </span>
          </div>
          <h2 style={{ fontSize: 24, fontWeight: 800, margin: '0 0 6px', letterSpacing: '-0.02em', color: '#ffffff' }}>
            {TITULOS[user?.rol] || 'Bienvenido al Sistema'}
          </h2>
          <p style={{ margin: 0, fontSize: 14, color: '#c7d2fe' }}>
            Hola, <strong>{user?.nombres} {user?.apellidos}</strong>. Aquí tienes el resumen actualizado de recursos y préstamos en tiempo real.
          </p>
        </div>

        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          {puedeLibros && (
            <Link to="/libros">
              <button
                className="btn-secondary"
                style={{
                  background: 'rgba(255, 255, 255, 0.12)',
                  color: '#ffffff',
                  borderColor: 'rgba(255, 255, 255, 0.25)',
                  boxShadow: 'none'
                }}
              >
                <BookOpen size={15} />
                <span>Explorar Libros</span>
              </button>
            </Link>
          )}
          {puedeEquipos && (
            <Link to="/equipos">
              <button
                className="btn-secondary"
                style={{
                  background: 'rgba(255, 255, 255, 0.12)',
                  color: '#ffffff',
                  borderColor: 'rgba(255, 255, 255, 0.25)',
                  boxShadow: 'none'
                }}
              >
                <Laptop size={15} />
                <span>Ver Equipos</span>
              </button>
            </Link>
          )}
          <Link to="/reportes">
            <button style={{ background: '#ffffff', color: '#312e81', fontWeight: 700 }}>
              <TrendingUp size={15} />
              <span>Ver Reportes</span>
            </button>
          </Link>
        </div>
      </div>

      {error && (
        <div className="alert error">
          <AlertTriangle size={18} />
          <span>{error}</span>
        </div>
      )}

      {tieneMultas && (
        <div
          className="alert error"
          style={{
            padding: '16px 20px',
            borderRadius: 14,
            display: 'flex',
            alignItems: 'flex-start',
            gap: 14
          }}
        >
          <ShieldAlert size={24} style={{ flexShrink: 0, marginTop: 2, color: '#dc2626' }} />
          <div>
            <strong style={{ fontSize: 14.5, color: '#991b1b', display: 'block' }}>
              Atención: Tienes sanciones activas o en proceso.
            </strong>
            <p style={{ margin: '3px 0 0', fontSize: 13, color: '#7f1d1d' }}>
              No podrás solicitar nuevos préstamos ni realizar renovaciones de libros o equipos hasta cumplir el tiempo de suspensión estipulado.
            </p>
          </div>
        </div>
      )}

      {/* KPI Stats Section */}
      <div style={{ marginBottom: 14, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h3 style={{ fontSize: 16, fontWeight: 700, color: 'var(--text-main)' }}>
          Métricas y Estadísticas Clave
        </h3>
        <span style={{ fontSize: 12.5, color: 'var(--text-muted)' }}>Actualizado automáticamente</span>
      </div>

      {stats ? (
        <div className="stats-grid">
          {Object.entries(stats)
            .filter(([k]) => k !== 'rol' && k !== 'tieneMultasPendientes')
            .map(([key, value]) => {
              const meta = META_ESTADISTICAS[key] || {
                label: key,
                icon: Layers,
                color: 'indigo'
              }
              const Icon = meta.icon
              return (
                <div key={key} className="stat-card">
                  <div className={`stat-icon-wrapper ${meta.color}`}>
                    <Icon size={22} />
                  </div>
                  <div className="stat-info">
                    <div className="stat-label">{meta.label}</div>
                    <div className="stat-value">{value}</div>
                  </div>
                </div>
              )
            })}
        </div>
      ) : (
        <div className="stats-grid">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="stat-card" style={{ opacity: 0.7 }}>
              <div className="stat-icon-wrapper indigo" style={{ animation: 'pulse 1.5s infinite' }}>
                <Sparkles size={20} />
              </div>
              <div className="stat-info">
                <div className="stat-label" style={{ height: 14, width: 90, background: 'var(--bg-subtle)', borderRadius: 4 }} />
                <div className="stat-value" style={{ height: 24, width: 40, background: 'var(--bg-subtle)', borderRadius: 4, marginTop: 4 }} />
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Quick Action Shortcuts */}
      <div className="card" style={{ marginTop: 8 }}>
        <div className="card-header">
          <h3>Accesos Rápidos del Sistema</h3>
          <span style={{ fontSize: 12.5, color: 'var(--text-muted)' }}>Módulos autorizados para tu rol</span>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: 14 }}>
          <Link to="/prestamos-libros" style={{ textDecoration: 'none' }}>
            <div className="stat-card" style={{ cursor: 'pointer', padding: '16px 18px' }}>
              <div className="stat-icon-wrapper emerald">
                <BookmarkCheck size={20} />
              </div>
              <div style={{ flex: 1 }}>
                <strong style={{ fontSize: 14, color: 'var(--text-main)', display: 'block' }}>Préstamos de Libros</strong>
                <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>Gestionar solicitudes y entregas</span>
              </div>
              <ArrowRight size={16} color="var(--text-muted)" />
            </div>
          </Link>

          <Link to="/prestamos-equipos" style={{ textDecoration: 'none' }}>
            <div className="stat-card" style={{ cursor: 'pointer', padding: '16px 18px' }}>
              <div className="stat-icon-wrapper indigo">
                <CalendarCheck size={20} />
              </div>
              <div style={{ flex: 1 }}>
                <strong style={{ fontSize: 14, color: 'var(--text-main)', display: 'block' }}>Préstamos de Equipos</strong>
                <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>Control de salida y devolución</span>
              </div>
              <ArrowRight size={16} color="var(--text-muted)" />
            </div>
          </Link>

          <Link to="/multas" style={{ textDecoration: 'none' }}>
            <div className="stat-card" style={{ cursor: 'pointer', padding: '16px 18px' }}>
              <div className="stat-icon-wrapper rose">
                <AlertTriangle size={20} />
              </div>
              <div style={{ flex: 1 }}>
                <strong style={{ fontSize: 14, color: 'var(--text-main)', display: 'block' }}>Sanciones y Multas</strong>
                <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>Consultar estados de suspensión</span>
              </div>
              <ArrowRight size={16} color="var(--text-muted)" />
            </div>
          </Link>

          <Link to="/reportes" style={{ textDecoration: 'none' }}>
            <div className="stat-card" style={{ cursor: 'pointer', padding: '16px 18px' }}>
              <div className="stat-icon-wrapper purple">
                <FileSpreadsheet size={20} />
              </div>
              <div style={{ flex: 1 }}>
                <strong style={{ fontSize: 14, color: 'var(--text-main)', display: 'block' }}>Exportar Reportes</strong>
                <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>Descargar reportes en formato Excel</span>
              </div>
              <ArrowRight size={16} color="var(--text-muted)" />
            </div>
          </Link>
        </div>
      </div>
    </div>
  )
}

export default Home
