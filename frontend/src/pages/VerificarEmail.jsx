import { useEffect, useState } from 'react'
import { useSearchParams, useNavigate, Link } from 'react-router-dom'
import { verificarEmail, reenviarVerificacion } from '../api/auth.js'
import {
  Library,
  CheckCircle2,
  AlertCircle,
  Mail,
  ArrowRight,
  RefreshCw,
  Send,
  ShieldCheck
} from 'lucide-react'

function VerificarEmail() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')

  const [estado, setEstado] = useState('cargando') // 'cargando' | 'exito' | 'error' | 'sin_token'
  const [mensaje, setMensaje] = useState('')
  const [correoReenvio, setCorreoReenvio] = useState('')
  const [reenviando, setReenviando] = useState(false)
  const [mensajeReenvio, setMensajeReenvio] = useState({ tipo: '', texto: '' })

  useEffect(() => {
    if (!token) {
      setEstado('sin_token')
      setMensaje('No se proporcionó ningún token de verificación en el enlace.')
      return
    }

    let cancelado = false

    const procesarVerificacion = async () => {
      setEstado('cargando')
      try {
        const res = await verificarEmail(token)
        if (!cancelado) {
          setEstado('exito')
          setMensaje(res?.mensaje || '¡Tu correo electrónico ha sido verificado exitosamente! Ya puedes iniciar sesión.')
        }
      } catch (err) {
        if (!cancelado) {
          setEstado('error')
          setMensaje(err.message || 'El enlace de verificación es inválido o ha expirado.')
        }
      }
    }

    procesarVerificacion()

    return () => {
      cancelado = true
    }
  }, [token])

  const handleReenviar = async (e) => {
    e.preventDefault()
    if (!correoReenvio.trim()) return
    setReenviando(true)
    setMensajeReenvio({ tipo: '', texto: '' })
    try {
      const res = await reenviarVerificacion(correoReenvio.trim())
      setMensajeReenvio({
        tipo: 'success',
        texto: res?.mensaje || 'Hemos enviado un nuevo enlace a tu correo.'
      })
    } catch (err) {
      setMensajeReenvio({
        tipo: 'error',
        texto: err.message || 'No se pudo reenviar el enlace.'
      })
    } finally {
      setReenviando(false)
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
          background: 'radial-gradient(circle, rgba(5, 150, 105, 0.18) 0%, transparent 70%)'
        }}
      ></div>

      <div className="login-card" style={{ maxWidth: 500, textAlign: 'center' }}>
        <div className="login-header">
          <div className="login-logo" style={{ margin: '0 auto 14px' }}>
            <Library size={28} />
          </div>
          <h2>Verificación de Cuenta</h2>
          <p>Sistema de Gestión de Biblioteca & Almacén SENA</p>
        </div>

        {/* Estado: Cargando */}
        {estado === 'cargando' && (
          <div style={{ padding: '32px 16px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16 }}>
            <RefreshCw size={40} className="animate-spin" style={{ color: '#4f46e5' }} />
            <h3 style={{ margin: 0, fontSize: 18, color: '#0f172a', fontWeight: 700 }}>
              Verificando tu correo...
            </h3>
            <p style={{ margin: 0, fontSize: 14, color: '#64748b' }}>
              Validando el token de seguridad. Por favor espera un momento.
            </p>
          </div>
        )}

        {/* Estado: Éxito */}
        {estado === 'exito' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 20, padding: '10px 0' }}>
            <div
              style={{
                width: 64,
                height: 64,
                borderRadius: '50%',
                background: '#ecfdf5',
                border: '2px solid #a7f3d0',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto',
                color: '#059669'
              }}
            >
              <CheckCircle2 size={36} />
            </div>

            <div>
              <h3 style={{ margin: '0 0 8px', fontSize: 20, color: '#0f172a', fontWeight: 800 }}>
                ¡Correo Verificado con Éxito!
              </h3>
              <p style={{ margin: 0, fontSize: 14, color: '#475569', lineHeight: 1.5 }}>
                {mensaje}
              </p>
            </div>

            <div
              style={{
                background: '#ecfdf5',
                border: '1px solid #a7f3d0',
                borderRadius: 8,
                padding: '12px 16px',
                fontSize: 13,
                color: '#065f46',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 8
              }}
            >
              <ShieldCheck size={16} color="#059669" />
              <span>Tu cuenta ya está completamente autorizada para ingresar.</span>
            </div>

            <button
              onClick={() => navigate('/login')}
              style={{
                padding: '12px 20px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 8,
                width: '100%'
              }}
            >
              <span>Ir a Iniciar Sesión</span>
              <ArrowRight size={16} />
            </button>
          </div>
        )}

        {/* Estado: Error o Sin Token */}
        {(estado === 'error' || estado === 'sin_token') && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 20, padding: '10px 0' }}>
            <div
              style={{
                width: 64,
                height: 64,
                borderRadius: '50%',
                background: '#fef2f2',
                border: '2px solid #fecaca',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto',
                color: '#dc2626'
              }}
            >
              <AlertCircle size={36} />
            </div>

            <div>
              <h3 style={{ margin: '0 0 8px', fontSize: 20, color: '#0f172a', fontWeight: 800 }}>
                {estado === 'sin_token' ? 'Enlace Incompleto' : 'Enlace Inválido o Expirado'}
              </h3>
              <p style={{ margin: 0, fontSize: 14, color: '#475569', lineHeight: 1.5 }}>
                {mensaje}
              </p>
            </div>

            {mensajeReenvio.texto && (
              <div className={`alert ${mensajeReenvio.tipo}`} style={{ textAlign: 'left' }}>
                <span>{mensajeReenvio.texto}</span>
              </div>
            )}

            <form
              onSubmit={handleReenviar}
              style={{
                display: 'flex',
                flexDirection: 'column',
                gap: 12,
                background: '#f8fafc',
                padding: 18,
                borderRadius: 10,
                border: '1px solid #e2e8f0',
                textAlign: 'left'
              }}
            >
              <label>
                <span style={{ fontSize: 13, color: '#334155', fontWeight: 600 }}>
                  ¿Necesitas un nuevo enlace? Ingresa tu correo:
                </span>
                <div style={{ position: 'relative', display: 'flex', alignItems: 'center', marginTop: 6 }}>
                  <Mail
                    size={16}
                    style={{ position: 'absolute', left: 12, color: '#64748b', pointerEvents: 'none' }}
                  />
                  <input
                    type="email"
                    style={{ paddingLeft: 38 }}
                    value={correoReenvio}
                    onChange={(e) => setCorreoReenvio(e.target.value)}
                    placeholder="tu_correo@email.com"
                    required
                  />
                </div>
              </label>
              <button
                type="submit"
                disabled={reenviando}
                style={{
                  padding: '10px 16px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 8,
                  background: '#4f46e5'
                }}
              >
                {reenviando ? (
                  <span>Enviando enlace...</span>
                ) : (
                  <>
                    <Send size={15} />
                    <span>Reenviar Correo de Activación</span>
                  </>
                )}
              </button>
            </form>

            <Link
              to="/login"
              style={{
                color: '#4f46e5',
                fontSize: 14,
                fontWeight: 600,
                textDecoration: 'none',
                marginTop: 4,
                display: 'inline-block'
              }}
            >
              ← Volver a la pantalla de inicio de sesión
            </Link>
          </div>
        )}
      </div>
    </div>
  )
}

export default VerificarEmail
