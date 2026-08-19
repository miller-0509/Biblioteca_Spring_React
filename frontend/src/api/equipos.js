import { get, post, put, del } from './client'

export const listarEquipos = (params) => {
  const qs = new URLSearchParams()
  if (params?.busqueda) qs.set('busqueda', params.busqueda)
  if (params?.estado) qs.set('estado', params.estado)
  if (params?.tipo) qs.set('tipo', params.tipo)
  const q = qs.toString()
  return get(`/equipos${q ? `?${q}` : ''}`)
}
export const listarEquiposDisponibles = () => get('/equipos/disponibles')
export const obtenerEquipo = (id) => get(`/equipos/${id}`)
export const crearEquipo = (equipo) => post('/equipos', equipo)
export const actualizarEquipo = (id, equipo) => put(`/equipos/${id}`, equipo)
export const cambiarEstadoEquipo = (id, dto) =>
  put(`/equipos/${id}/estado`, dto)
export const eliminarEquipo = (id) => del(`/equipos/${id}`)
export const listarHistorialEquipos = (idEquipo) =>
  get(`/historial-estado-equipos/equipo/${idEquipo}`)
