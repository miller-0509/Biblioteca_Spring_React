import { get, post } from './client'

export const listarUsuarios = () => get('/usuarios')
export const crearUsuario = (usuario) => post('/usuarios', usuario)
