import { get, post, put } from './client'

export const listarPrestamosEquipos = () => get('/prestamos')
export const obtenerPrestamoEquipo = (id) => get(`/prestamos/${id}`)
export const listarPrestamosEquiposPorUsuario = (idUsuario) =>
  get(`/prestamos/usuario/${idUsuario}`)
export const listarPrestamosEquiposPorEquipo = (idEquipo) =>
  get(`/prestamos/equipo/${idEquipo}`)
export const solicitarPrestamoEquipo = (dto) => post('/prestamos', dto)
export const aceptarPrestamoEquipo = (id) => put(`/prestamos/${id}/aceptar`)
export const rechazarPrestamoEquipo = (id, razon) =>
  put(`/prestamos/${id}/rechazar`, { razon })
export const devolverPrestamoEquipo = (id, dto) =>
  put(`/prestamos/${id}/devolver`, dto)
export const solicitarRenovacionEquipo = (id, motivoRenovacion) =>
  put(`/prestamos/${id}/renovar`, { motivoRenovacion })
export const procesarRenovacionEquipo = (id, accion, motivoRechazo) =>
  put(`/prestamos/${id}/procesar-renovacion`, { accion, motivoRechazo })
export const listarRenovacionesEquipos = () => get('/renovaciones-equipos')
export const listarRenovacionesEquipoPorPrestamo = (id) =>
  get(`/renovaciones-equipos/prestamo/${id}`)
