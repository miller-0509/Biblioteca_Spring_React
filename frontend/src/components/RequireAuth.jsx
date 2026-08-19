import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'

export default function RequireAuth({ roles, children }) {
  const { user, loading } = useAuth()

  if (loading) return <div className="empty">Cargando…</div>
  if (!user) return <Navigate to="/login" replace />

  if (roles && !roles.includes(user.rol)) {
    return <Navigate to="/" replace />
  }
  return children
}
