import { get, post } from './client'

export const login = (correo, password) => post('/auth/login', { correo, password })
export const registro = (datos) => post('/auth/registro', datos)
export const reenviarVerificacion = (correo) => post('/auth/reenviar-verificacion', { correo })
export const recuperarPassword = (correo) => post('/auth/recuperar-password', { correo })
export const restablecerPassword = (token, password, passwordConfirm) =>
  post(`/auth/restablecer-password/${token}`, { password, passwordConfirm })
export const obtenerMe = () => get('/auth/me')
