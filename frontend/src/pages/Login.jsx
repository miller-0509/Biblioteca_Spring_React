import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { Library, Mail, Lock, ArrowRight, ShieldCheck, Sparkles } from 'lucide-react'

const ROLES_DEMO = [
  { rol: 'Administrador', correo: 'admin@email.com', pass: 'Admin1234', color: '#4f46e5' },
  { rol: 'Bibliotecario', correo: 'carlos@email.com', pass: '123456', color: '#06b6d4' },
  { rol: 'Almacenista', correo: 'almacen@email.com', pass: 'Clave1234', color: '#8b5cf6' },
  { rol: 'Aprendiz', correo: 'maria@email.com', pass: '654321', color: '#10b981' },
  { rol: 'Instructor', correo: 'pedro@email.com', pass: 'Clave1234', color: '#f59e0b' }
]

const ROLES_VALIDOS = ['administrador', 'bibliotecario', 'almacenista', 'aprendiz', 'instructor']

function Login() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [correo, setCorreo] = useState('admin@email.com')
  const [password, setPassword] = useState('Admin1234')
  const [cargando, setCargando] = useState(false)
  const [message, setMessage] = useState({ type: '', text: '' })

  if (user) return <Navigate to="/" replace />

  const handleSubmit = async (e) => {
    e.preventDefault()
    setMessage({ type: '', text: '' })
    setCargando(true)
    try {
      const u = await login(correo, password)
      if (!ROLES_VALIDOS.includes(u.rol)) {
        setMessage({ type: 'error', text: 'Tu usuario aún no está habilitado.' })
        return
      }
      navigate('/', { replace: true })
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Credenciales inválidas' })
    } finally {
      setCargando(false)
    }
  }

  const selectDemo = (demo) => {
    setCorreo(demo.correo)
    setPassword(demo.pass)
    setMessage({ type: 'success', text: `Usuario de prueba seleccionado: ${demo.rol}` })
  }

  return (
    <div className="login-container">
      <div className="login-glow-bg" style={{ top: '-10%', right: '-10%' }}></div>
      <div className="login-glow-bg" style={{ bottom: '-10%', left: '-10%', background: 'radial-gradient(circle, rgba(139, 92, 246, 0.15) 0%, transparent 70%)' }}></div>

      <div className="login-card">
        <div className="login-header">
          <div className="login-logo">
            <Library size={28} />
          </div>
          <h2>Sistema Biblioteca & Almacén</h2>
          <p>SENA ADSO · Inicia sesión para continuar</p>
        </div>

        {message.text && (
          <div className={`alert ${message.type}`}>
            <span>{message.text}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <label>
            <span>Correo institucional / usuario</span>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <Mail size={16} style={{ position: 'absolute', left: 12, color: '#94a3b8', pointerEvents: 'none' }} />
              <input
                type="email"
                style={{ paddingLeft: 38 }}
                value={correo}
                onChange={(e) => setCorreo(e.target.value)}
                placeholder="ejemplo@email.com"
                required
              />
            </div>
          </label>

          <label>
            <span>Contraseña</span>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <Lock size={16} style={{ position: 'absolute', left: 12, color: '#94a3b8', pointerEvents: 'none' }} />
              <input
                type="password"
                style={{ paddingLeft: 38 }}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
              />
            </div>
          </label>

          <button type="submit" disabled={cargando} style={{ marginTop: 6, padding: '11px 16px' }}>
            {cargando ? (
              <span>Iniciando sesión...</span>
            ) : (
              <>
                <span>Ingresar al sistema</span>
                <ArrowRight size={16} />
              </>
            )}
          </button>
        </form>

        <div className="demo-roles-container">
          <div className="demo-roles-title">
            <Sparkles size={13} style={{ display: 'inline', marginRight: 4, verticalAlign: 'middle', color: '#6366f1' }} />
            Acceso Rápido de Prueba (1-Clic)
          </div>
          <div className="demo-roles-grid">
            {ROLES_DEMO.map((demo) => (
              <button
                key={demo.rol}
                type="button"
                className="demo-role-btn"
                onClick={() => selectDemo(demo)}
              >
                <span>{demo.rol}</span>
                <ShieldCheck size={13} style={{ color: demo.color }} />
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login
