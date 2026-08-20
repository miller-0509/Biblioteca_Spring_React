// Cliente base de la API con Cache en Memoria para transiciones instantáneas.
// En desarrollo se usa el proxy de Vite (/api -> backend). En producción se
// puede apuntar a otra URL con la variable VITE_API_URL (ej. https://api.midominio.com).
export const API_BASE = (import.meta.env.VITE_API_URL || '/api').replace(/\/+$/, '')

const TOKEN_KEY = 'biblioteca_token'

// Limpiar residuo antiguo de localStorage para asegurar que la sesión dependa de la pestaña actual
try {
  localStorage.removeItem(TOKEN_KEY)
} catch {
  // Ignorar si el almacenamiento está restringido
}

export const getToken = () => sessionStorage.getItem(TOKEN_KEY)
export const setToken = (token) => sessionStorage.setItem(TOKEN_KEY, token)
export const clearToken = () => {
  sessionStorage.removeItem(TOKEN_KEY)
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch {}
  clearApiCache()
}

// In-Memory API Cache con TTL de 30 segundos
const cache = new Map()
const CACHE_TTL_MS = 30000 // 30 segundos

export const clearApiCache = (pathPrefix = '') => {
  if (!pathPrefix) {
    cache.clear()
  } else {
    for (const key of cache.keys()) {
      if (key.startsWith(pathPrefix)) {
        cache.delete(key)
      }
    }
  }
}

async function request(path, options = {}) {
  const isGet = !options.method || options.method.toUpperCase() === 'GET'
  const cacheKey = path

  // Si es un GET y no se pide bypass, verificar cache
  if (isGet && !options.skipCache) {
    const cached = cache.get(cacheKey)
    if (cached && Date.now() - cached.timestamp < CACHE_TTL_MS) {
      return cached.data
    }
  }

  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) }
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers })

  if (res.status === 401) {
    clearToken()
    if (!window.location.pathname.startsWith('/login')) {
      window.location.href = '/login'
    }
  }

  if (!res.ok) {
    let message = `Error ${res.status}`
    try {
      const body = await res.json()
      if (body?.message) message = body.message
      else if (body?.error) message = body.error
    } catch {
      // respuesta sin cuerpo JSON
    }
    throw new Error(message)
  }

  if (res.status === 204) {
    // Si fue mutación, invalidamos la cache
    clearApiCache()
    return null
  }

  const data = await res.json()

  // Guardar en cache si es GET
  if (isGet) {
    cache.set(cacheKey, { data, timestamp: Date.now() })
  } else {
    // Cualquier POST / PUT / DELETE limpia la cache para refresco inmediato
    clearApiCache()
  }

  return data
}

export const get = (path, options = {}) => request(path, { method: 'GET', ...options })
export const post = (path, body) =>
  request(path, { method: 'POST', body: JSON.stringify(body) })
export const put = (path, body) =>
  request(path, { method: 'PUT', body: JSON.stringify(body) })
export const del = (path) => request(path, { method: 'DELETE' })

export const getBlob = async (path) => {
  const headers = {}
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`
  const res = await fetch(`${API_BASE}${path}`, { headers })
  if (!res.ok) throw new Error(`Error ${res.status}`)
  return res.blob()
}
