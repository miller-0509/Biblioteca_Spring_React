import { get, post, put, del } from './client'

export const listarPrestamos = () => get('/prestamos-libros')
export const obtenerPrestamo = (id) => get(`/prestamos-libros/${id}`)
export const listarPrestamosPorUsuario = (idUsuario) =>
  get(`/prestamos-libros/usuario/${idUsuario}`)
export const listarPrestamosPorLibro = (idLibro) =>
  get(`/prestamos-libros/libro/${idLibro}`)
export const crearPrestamo = (prestamo) => post('/prestamos-libros', prestamo)
export const actualizarPrestamo = (id, prestamo) =>
  put(`/prestamos-libros/${id}`, prestamo)
export const cambiarEstadoPrestamo = (id, estado) =>
  put(`/prestamos-libros/${id}/estado`, estado)
export const eliminarPrestamo = (id) => del(`/prestamos-libros/${id}`)
