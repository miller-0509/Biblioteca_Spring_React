import { get, getBlob } from './client'

export const obtenerDashboard = () => get('/reportes/dashboard')
export const obtenerInventario = (params = {}) => {
  const qs = new URLSearchParams()
  if (params.estado) qs.set('estado', params.estado)
  if (params.tipo) qs.set('tipo', params.tipo)
  if (params.fechaInicio) qs.set('fechaInicio', params.fechaInicio)
  if (params.fechaFin) qs.set('fechaFin', params.fechaFin)
  const q = qs.toString()
  return get(`/reportes/inventario${q ? `?${q}` : ''}`)
}
export const obtenerReportePrestamos = (params = {}) => {
  const qs = new URLSearchParams()
  if (params.estado) qs.set('estado', params.estado)
  if (params.tipoRecurso) qs.set('tipoRecurso', params.tipoRecurso)
  if (params.fechaInicio) qs.set('fechaInicio', params.fechaInicio)
  if (params.fechaFin) qs.set('fechaFin', params.fechaFin)
  const q = qs.toString()
  return get(`/reportes/prestamos${q ? `?${q}` : ''}`)
}
export const obtenerMisPrestamos = (estado) =>
  get(`/reportes/mis-prestamos${estado ? `?estado=${estado}` : ''}`)
export const obtenerUsuariosActivos = () => get('/reportes/usuarios-activos')
export const exportarExcel = async (tipoReporte) => {
  const blob = await getBlob(`/reportes/exportar/excel/${tipoReporte}`)
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `reporte_${tipoReporte}_${new Date().toISOString().slice(0, 10)}.xlsx`
  a.click()
  URL.revokeObjectURL(url)
}

export const exportarPdf = async (tipoReporte) => {
  const blob = await getBlob(`/reportes/exportar/pdf/${tipoReporte}`)
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `reporte_${tipoReporte}_${new Date().toISOString().slice(0, 10)}.pdf`
  a.click()
  URL.revokeObjectURL(url)
}

