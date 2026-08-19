import { lazy, Suspense } from 'react'
import { BrowserRouter, Routes, Route, NavLink, Navigate, useLocation } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext.jsx'
import RequireAuth from './components/RequireAuth.jsx'
import {
  BookOpen,
  Laptop,
  BookmarkCheck,
  CalendarCheck,
  AlertCircle,
  BarChart3,
  Users,
  LogOut,
  Library,
  LayoutDashboard,
  ShieldCheck
} from 'lucide-react'

import Login from './pages/Login.jsx'
import VerificarEmail from './pages/VerificarEmail.jsx'
import Home from './pages/Home.jsx'
import Libros from './pages/Libros.jsx'
import Prestamos from './pages/Prestamos.jsx'
import Usuarios from './pages/Usuarios.jsx'
import Equipos from './pages/Equipos.jsx'
import PrestamosEquipos from './pages/PrestamosEquipos.jsx'
import Multas from './pages/Multas.jsx'
import Reportes from './pages/Reportes.jsx'

const ADMIN = 'administrador'
const BIBLIOTECARIO = 'bibliotecario'
const ALMACENISTA = 'almacenista'

const TITULOS_RUTAS = {
  '/': 'Panel de Control',
  '/libros': 'Catálogo de Libros',
  '/equipos': 'Inventario de Equipos',
  '/prestamos-libros': 'Gestión de Préstamos de Libros',
  '/prestamos-equipos': 'Gestión de Préstamos de Equipos',
  '/multas': 'Sanciones & Suspensiones',
  '/reportes': 'Reportes & Estadísticas',
  '/usuarios': 'Administración de Usuarios'
}

function Layout() {
  const { user, logout } = useAuth()
  const location = useLocation()

  const puedeUsuarios = user?.rol === ADMIN

  const nav = [
    { to: '/', label: 'Inicio', end: true, show: true, icon: LayoutDashboard },
    { to: '/libros', label: 'Catálogo Libros', show: true, icon: BookOpen },
    { to: '/equipos', label: 'Inventario Equipos', show: true, icon: Laptop },
    { to: '/prestamos-libros', label: 'Préstamos Libros', show: true, icon: BookmarkCheck },
    { to: '/prestamos-equipos', label: 'Préstamos Equipos', show: true, icon: CalendarCheck },
    { to: '/multas', label: 'Multas y Sanciones', show: true, icon: AlertCircle },
    { to: '/reportes', label: 'Reportes', show: true, icon: BarChart3 },
    { to: '/usuarios', label: 'Usuarios', show: puedeUsuarios, icon: Users },
  ].filter((n) => n.show)

  const currentTitle = TITULOS_RUTAS[location.pathname] || 'Biblioteca SENA'
  const userInitials = (user?.nombres?.[0] || 'U') + (user?.apellidos?.[0] || '')

  return (
    <div className="app-layout">
      <aside className="app-sidebar">
        <div className="sidebar-header">
          <div className="sidebar-brand-icon">
            <Library size={22} />
          </div>
          <div className="sidebar-brand-text">
            <h1>Biblioteca</h1>
            <p>SENA ADSO</p>
          </div>
        </div>

        <div className="sidebar-nav-container">
          <span className="sidebar-section-title">Navegación</span>
          {nav.map((n) => {
            const Icon = n.icon
            return (
              <NavLink
                key={n.to}
                to={n.to}
                end={n.end}
                className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
              >
                <Icon className="icon" />
                <span>{n.label}</span>
              </NavLink>
            )
          })}
        </div>

        <div className="sidebar-footer">
          <div className="user-profile-widget">
            <div className="user-avatar">{userInitials.toUpperCase()}</div>
            <div className="user-info">
              <div className="user-info-name">{user?.nombres} {user?.apellidos}</div>
              <div className="user-info-role">{user?.rol}</div>
            </div>
          </div>
          <button className="btn-logout" onClick={logout}>
            <LogOut size={14} />
            <span>Cerrar sesión</span>
          </button>
        </div>
      </aside>

      <div className="app-main">
        <header className="app-topbar">
          <div className="topbar-left">
            <h2 className="page-title">{currentTitle}</h2>
            <span className="page-badge-role">
              <ShieldCheck size={13} style={{ display: 'inline', marginRight: 4, verticalAlign: 'middle' }} />
              {user?.rol}
            </span>
          </div>
          <div className="topbar-right">
            <div className="live-indicator">
              <span className="live-dot"></span>
              <span>En línea</span>
            </div>
          </div>
        </header>

        <main className="app-content animate-fade-in-up">
          <Suspense fallback={
            <div className="empty-state">
              <div className="empty-state-icon">
                <Library size={24} className="animate-spin" />
              </div>
              <h4>Cargando módulo...</h4>
              <p>Por favor espera mientras preparamos la información.</p>
            </div>
          }>
            <Routes>
              <Route path="/" element={<RequireAuth><Home /></RequireAuth>} />
              <Route path="/libros" element={<RequireAuth><Libros /></RequireAuth>} />
              <Route path="/equipos" element={<RequireAuth><Equipos /></RequireAuth>} />
              <Route path="/prestamos-libros" element={<RequireAuth><Prestamos /></RequireAuth>} />
              <Route path="/prestamos-equipos" element={<RequireAuth><PrestamosEquipos /></RequireAuth>} />
              <Route path="/multas" element={<RequireAuth><Multas /></RequireAuth>} />
              <Route path="/reportes" element={<RequireAuth><Reportes /></RequireAuth>} />
              <Route
                path="/usuarios"
                element={
                  <RequireAuth roles={[ADMIN]}>
                    <Usuarios />
                  </RequireAuth>
                }
              />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Suspense>
        </main>
      </div>
    </div>
  )
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route
            path="/login"
            element={
              <Suspense fallback={<div className="login-container">Cargando...</div>}>
                <Login />
              </Suspense>
            }
          />
          <Route
            path="/verificar-email"
            element={
              <Suspense fallback={<div className="login-container">Cargando...</div>}>
                <VerificarEmail />
              </Suspense>
            }
          />
          <Route path="/*" element={<Layout />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
