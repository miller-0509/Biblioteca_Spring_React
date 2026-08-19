import { get, post, put, del } from './client'

export const listarLibros = (page = 0, size = 10) => get(`/libros?page=${page}&size=${size}`)
export const obtenerLibro = (id) => get(`/libros/${id}`)
export const crearLibro = (libro) => post('/libros', libro)
export const actualizarLibro = (id, libro) => put(`/libros/${id}`, libro)
export const cambiarEstadoLibro = (id, estado) =>
  put(`/libros/${id}/estado`, estado)
export const eliminarLibro = (id) => del(`/libros/${id}`)
