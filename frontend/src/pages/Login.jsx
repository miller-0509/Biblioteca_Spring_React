import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { registro as apiRegistro, reenviarVerificacion as apiReenviar, recuperarPassword as apiRecuperar } from '../api/auth.js'
import {
  Library,
  Mail,
  Lock,
  ArrowRight,
  ShieldCheck,
  User,
  Send,
  KeyRound,
  ArrowLeft,
  AlertCircle,
  CheckCircle2
} from 'lucide-react'

const ROLES_VALIDOS = ['administrador', 'bibliotecario', 'almacenista', 'aprendiz', 'instructor']

function Login() {
  const { user, login } = useAuth()
  const navigate = useNavigate()

  // Modos de la vista: 'login' | 'registro' | 'reenviar' | 'recuperar'
  const [modo, setModo] = useState('login')

  // Estados de formularios
  const [correo, setCorreo] = useState('')
  const [password, setPassword] = useState('')
  const [nombres, setNombres] = useState('')
  const [apellidos, setApellidos] = useState('')

  const [cargando, setCargando] = useState(false)
  const [message, setMessage] = useState({ type: '', text: '', requiereVerificacion: false })

  if (user) return <Navigate to="/" replace />

  const handleLogin = async (e) => {
    e.preventDefault()
    setMessage({ type: '', text: '', requiereVerificacion: false })
    setCargando(true)
    try {
      const u = await login(correo.trim(), password)
      if (!ROLES_VALIDOS.includes(u.rol)) {
        setMessage({ type: 'error', text: 'Tu usuario aún no está habilitado.', requiereVerificacion: false })
        return
      }
      navigate('/', { replace: true })
    } catch (err) {
      const msg = err.message || 'Credenciales inválidas'
      const esNoVerificado = msg.toLowerCase().includes('verificar') || msg.toLowerCase().includes('verificación')
      setMessage({
        type: 'error',
        text: msg,
        requiereVerificacion: esNoVerificado
      })
    } finally {
      setCargando(false)
    }
  }

  const handleRegistro = async (e) => {
    e.preventDefault()
    setMessage({ type: '', text: '', requiereVerificacion: false })
    setCargando(true)
    try {
      const res = await apiRegistro({
        nombres: nombres.trim(),
        apellidos: apellidos.trim(),
        correo: correo.trim().toLowerCase(),
        password
      })
      setMessage({
        type: 'success',
        text: res?.mensaje || 'Registro exitoso. Hemos enviado el correo de verificación.',
        requiereVerificacion: false
      })
      setModo('login')
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'No se pudo completar el registro.', requiereVerificacion: false })
    } finally {
      setCargando(false)
    }
  }

  const handleReenviarVerificacion = async (e) => {
    e.preventDefault()
    if (!correo.trim()) return
    setMessage({ type: '', text: '', requiereVerificacion: false })
    setCargando(true)
    try {
      const res = await apiReenviar(correo.trim().toLowerCase())
      setMessage({
        type: 'success',
        text: res?.mensaje || 'Enlace de verificación reenviado a tu correo.',
        requiereVerificacion: false
      })
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'No se pudo reenviar el enlace.', requiereVerificacion: false })
    } finally {
      setCargando(false)
    }
  }

  const handleRecuperarPassword = async (e) => {
    e.preventDefault()
    if (!correo.trim()) return
    setMessage({ type: '', text: '', requiereVerificacion: false })
    setCargando(true)
    try {
      const res = await apiRecuperar(correo.trim().toLowerCase())
      setMessage({
        type: 'success',
        text: res?.mensaje || 'Instrucciones enviadas a tu correo.',
        requiereVerificacion: false
      })
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'No se pudo procesar la solicitud.', requiereVerificacion: false })
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="login-container">
      <div className="login-glow-bg" style={{ top: '-10%', right: '-10%' }}></div>
      <div
        className="login-glow-bg"
        style={{
          bottom: '-10%',
          left: '-10%',
          background: 'radial-gradient(circle, rgba(139, 92, 246, 0.2) 0%, transparent 70%)'
        }}
      ></div>

      <div className="login-card" style={{ maxWidth: 480 }}>
        <div className="login-header">
          <div className="login-logo">
            <Library size={28} />
          </div>
          <h2>Sistema Biblioteca & Almacén</h2>
          <p>SENA ADSO · Centro de Formación</p>
        </div>

        {/* Pestañas de Navegación de Autenticación */}
        <div
          style={{
            display: 'flex',
            borderBottom: '2px solid #e2e8f0',
            marginBottom: 22,
            gap: 8
          }}
        >
          <button
            type="button"
            onClick={() => {
              setModo('login')
              setMessage({ type: '', text: '', requiereVerificacion: false })
            }}
            style={{
              flex: 1,
              background: 'transparent',
              border: 'none',
              borderBottom: modo === 'login' ? '3px solid #4f46e5' : '3px solid transparent',
              color: modo === 'login' ? '#4f46e5' : '#64748b',
              padding: '10px 4px',
              fontWeight: 700,
              fontSize: 14,
              cursor: 'pointer',
              borderRadius: 0,
              boxShadow: 'none',
              marginBottom: -2
            }}
          >
            Iniciar Sesión
          </button>
          <button
            type="button"
            onClick={() => {
              setModo('registro')
              setMessage({ type: '', text: '', requiereVerificacion: false })
            }}
            style={{
              flex: 1,
              background: 'transparent',
              border: 'none',
              borderBottom: modo === 'registro' ? '3px solid #059669' : '3px solid transparent',
              color: modo === 'registro' ? '#059669' : '#64748b',
              padding: '10px 4px',
              fontWeight: 700,
              fontSize: 14,
              cursor: 'pointer',
              borderRadius: 0,
              boxShadow: 'none',
              marginBottom: -2
            }}
          >
            Crear Cuenta
          </button>
        </div>

        {message.text && (
          <div className={`alert ${message.type}`} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, width: '100%' }}>
              {message.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
              <span style={{ fontWeight: 600 }}>{message.text}</span>
            </div>
            {message.requiereVerificacion && (
              <button
                type="button"
                onClick={() => setModo('reenviar')}
                className="btn-sm"
                style={{
                  alignSelf: 'flex-start',
                  marginTop: 4,
                  background: '#3730a3',
                  color: '#ffffff',
                  boxShadow: 'none'
                }}
              >
                Reenviar correo de activación →
              </button>
            )}
          </div>
        )}

        {/* ── MODO 1: LOGIN ── */}
        {modo === 'login' && (
          <form onSubmit={handleLogin} autoComplete="off" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <label>
              <span>Correo institucional / usuario</span>
              <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                <Mail size={16} style={{ position: 'absolute', left: 12, color: '#64748b', pointerEvents: 'none' }} />
                <input
                  type="email"
                  style={{ paddingLeft: 38 }}
                  value={correo}
                  onChange={(e) => setCorreo(e.target.value)}
                  placeholder="ejemplo@email.com"
                  autoComplete="off"
                  required
                />
              </div>
            </label>

            <label>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>Contraseña</span>
                <button
                  type="button"
                  onClick={() => setModo('recuperar')}
                  style={{
                    background: 'none',
                    border: 'none',
                    padding: 0,
                    color: '#4f46e5',
                    fontSize: 12.5,
                    fontWeight: 600,
                    cursor: 'pointer',
                    textDecoration: 'underline',
                    boxShadow: 'none'
                  }}
                >
                  ¿Olvidaste tu contraseña?
                </button>
              </div>
              <div style={{ position: 'relative', display: 'flex', alignItems: 'center', marginTop: 4 }}>
                <Lock size={16} style={{ position: 'absolute', left: 12, color: '#64748b', pointerEvents: 'none' }} />
                <input
                  type="password"
                  style={{ paddingLeft: 38 }}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  autoComplete="new-password"
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

            <div style={{ textAlign: 'center', marginTop: 6 }}>
              <button
                type="button"
                onClick={() => setModo('reenviar')}
                style={{
                  background: 'none',
                  border: 'none',
                  color: '#64748b',
                  fontSize: 13,
                  fontWeight: 500,
                  cursor: 'pointer',
                  textDecoration: 'underline',
                  boxShadow: 'none'
                }}
              >
                ¿No recibiste el correo de activación? Reenvíalo aquí
              </button>
            </div>
          </form>
        )}

        {/* ── MODO 2: REGISTRO ── */}
        {modo === 'registro' && (
          <form onSubmit={handleRegistro} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
              <label>
                <span>Nombres</span>
                <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                  <User size={16} style={{ position: 'absolute', left: 12, color: '#64748b', pointerEvents: 'none' }} />
                  <input
                    type="text"
                    style={{ paddingLeft: 38 }}
                    value={nombres}
                    onChange={(e) => setNombres(e.target.value)}
                    placeholder="Ej. Juan"
                    required
                  />
                </div>
              </label>
              <label>
                <span>Apellidos</span>
                <input
                  type="text"
                  value={apellidos}
                  onChange={(e) => setApellidos(e.target.value)}
                  placeholder="Ej. Pérez"
                  required
                />
              </label>
            </div>

            <label>
              <span>Correo electrónico institucional</span>
              <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                <Mail size={16} style={{ position: 'absolute', left: 12, color: '#64748b', pointerEvents: 'none' }} />
                <input
                  type="email"
                  style={{ paddingLeft: 38 }}
                  value={correo}
                  onChange={(e) => setCorreo(e.target.value)}
                  placeholder="juan.perez@correo.com"
                  required
                />
              </div>
            </label>

            <label>
              <span>Contraseña (Mín. 8 caracteres, 1 mayúscula y 1 número)</span>
              <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                <Lock size={16} style={{ position: 'absolute', left: 12, color: '#64748b', pointerEvents: 'none' }} />
                <input
                  type="password"
                  style={{ paddingLeft: 38 }}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Ej. Clave2026*"
                  required
                />
              </div>
            </label>

            <div
              style={{
                background: '#ecfdf5',
                border: '1px solid #a7f3d0',
                borderRadius: 8,
                padding: '10px 14px',
                fontSize: 12.5,
                color: '#065f46',
                display: 'flex',
                alignItems: 'center',
                gap: 8
              }}
            >
              <ShieldCheck size={16} style={{ flexShrink: 0, color: '#059669' }} />
              <span>
                Al registrarte se enviará un enlace de verificación a tu correo para activar tu cuenta.
              </span>
            </div>

            <button
              type="submit"
              disabled={cargando}
              className="btn-success"
              style={{ marginTop: 4, padding: '11px 16px' }}
            >
              {cargando ? (
                <span>Creando cuenta...</span>
              ) : (
                <>
                  <span>Registrarme y recibir correo</span>
                  <Send size={15} />
                </>
              )}
            </button>
          </form>
        )}

        {/* ── MODO 3: REENVIAR VERIFICACIÓN ── */}
        {modo === 'reenviar' && (
          <form onSubmit={handleReenviarVerificacion} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div style={{ fontSize: 13.5, color: '#475569', lineHeight: 1.5 }}>
              Ingresa el correo con el que te registraste para enviarte un nuevo enlace de activación.
            </div>

            <label>
              <span>Correo registrado</span>
              <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                <Mail size={16} style={{ position: 'absolute', left: 12, color: '#64748b', pointerEvents: 'none' }} />
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

            <button type="submit" disabled={cargando} style={{ padding: '11px 16px' }}>
              {cargando ? (
                <span>Enviando enlace...</span>
              ) : (
                <>
                  <Send size={15} />
                  <span>Reenviar Enlace de Activación</span>
                </>
              )}
            </button>

            <button
              type="button"
              onClick={() => setModo('login')}
              className="btn-secondary"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 6
              }}
            >
              <ArrowLeft size={14} />
              <span>Volver a Iniciar Sesión</span>
            </button>
          </form>
        )}

        {/* ── MODO 4: RECUPERAR CONTRASEÑA ── */}
        {modo === 'recuperar' && (
          <form onSubmit={handleRecuperarPassword} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div style={{ fontSize: 13.5, color: '#475569', lineHeight: 1.5 }}>
              Te enviaremos las instrucciones de restablecimiento de contraseña a tu bandeja de entrada.
            </div>

            <label>
              <span>Correo de tu cuenta</span>
              <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                <Mail size={16} style={{ position: 'absolute', left: 12, color: '#64748b', pointerEvents: 'none' }} />
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

            <button type="submit" disabled={cargando} style={{ padding: '11px 16px' }}>
              {cargando ? (
                <span>Procesando...</span>
              ) : (
                <>
                  <KeyRound size={15} />
                  <span>Enviar Instrucciones</span>
                </>
              )}
            </button>

            <button
              type="button"
              onClick={() => setModo('login')}
              className="btn-secondary"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 6
              }}
            >
              <ArrowLeft size={14} />
              <span>Volver a Iniciar Sesión</span>
            </button>
          </form>
        )}
      </div>
    </div>
  )
}

export default Login
