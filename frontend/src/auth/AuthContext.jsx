import { createContext, useContext, useEffect, useState } from 'react'
import { login as apiLogin, obtenerMe } from '../api/auth.js'
import { getToken, setToken, clearToken } from '../api/client.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const restore = async () => {
    if (!getToken()) {
      setLoading(false)
      return
    }
    try {
      setUser(await obtenerMe())
    } catch {
      clearToken()
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    restore()
  }, [])

  const login = async (correo, password) => {
    const data = await apiLogin(correo, password)
    setToken(data.token)
    setUser(data.usuario)
    return data.usuario
  }

  const logout = () => {
    clearToken()
    setUser(null)
  }

  const value = { user, loading, login, logout, restore }
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  return ctx
}
