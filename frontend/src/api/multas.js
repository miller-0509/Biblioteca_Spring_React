import { get, post } from './client'

export const listarMultas = (estado) =>
  get(`/multas${estado ? `?estado=${estado}` : ''}`)
export const condonarMulta = (id, observacion) =>
  post(`/multas/${id}/condonar`, { observacion })
