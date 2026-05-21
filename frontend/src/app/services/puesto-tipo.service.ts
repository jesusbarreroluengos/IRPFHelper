import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface PuestoTipo {
  idPuestoTipo?: number;
  idPersona: number;
  nomPuesto: string;
  codEstudio: 'P' | 'S' | 'C' | 'F';
  codJornada: number;
  numTrieniosA1: number;
  numTrieniosA2: number;
  numSexenios: number;
  importeEspecDocente: number | null;
  importeOtrosAbonosMes: number;
  idComunidad?: number;
}

export interface ApiResponse {
  success: boolean;
  message: string;
  data?: any;
}

export interface Estudio {
  codEstudio: string;
  descEstudio: string;
}

export interface Jornada {
  codJornada: number;
  descJornada: string;
  porcentaje: number;
}

export interface ImportesDetalle {
  importeBrutoMes: number;
  importeExtra: number;
  importeBaseCotizacion: number;
  importeSueldo: number;
  importeComplementoDestino: number;
  importeTrienios: number;
  importeSexenios: number;
  importeSueldoExtra: number;
  importeTrieniosExtra: number;
}

@Injectable({
  providedIn: 'root'
})
/**
 * Servicio de dominio que centraliza el acceso a los puestos tipo, sus
 * catálogos auxiliares y los importes asociados.
 */
export class PuestoTipoService {
  private apiUrl = environment.apiUrl || '/api';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene los puestos asociados a una persona simulada.
   * @param idPersona Identificador de la persona para la que se consultan puestos.
   * @returns Observable con la lista de puestos recuperados.
   */
  getPuestosByPersona(idPersona: number): Observable<PuestoTipo[]> {
    return this.http.get<PuestoTipo[]>(`${this.apiUrl}/puestos-tipo/persona/${idPersona}`, { withCredentials: true });
  }

  /**
   * Crea un nuevo puesto tipo a partir de los datos del formulario.
   * @param puesto Datos parciales del puesto que se enviarán al backend.
   * @returns Observable con el resultado de la operación.
   */
  createPuesto(puesto: Partial<PuestoTipo>): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/puestos-tipo/create`, puesto, { withCredentials: true });
  }

  /**
   * Actualiza un puesto existente.
   * @param id Identificador del puesto que se desea modificar.
   * @param puesto Datos parciales con los cambios del puesto.
   * @returns Observable con la respuesta de actualización.
   */
  updatePuesto(id: number, puesto: Partial<PuestoTipo>): Observable<ApiResponse> {
    return this.http.put<ApiResponse>(`${this.apiUrl}/puestos-tipo/update/${id}`, puesto, { withCredentials: true });
  }

  /**
   * Elimina un puesto tipo por su identificador.
   * @param id Identificador del puesto a eliminar.
   * @returns Observable con el resultado de borrado.
   */
  deletePuesto(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${this.apiUrl}/puestos-tipo/delete/${id}`, { withCredentials: true });
  }

  /**
   * Solicita el cálculo principal de importes para un puesto.
   * @param puesto Datos parciales del puesto usados como base del cálculo.
   * @returns Observable con los importes calculados por la API.
   */
  calcularImporte(puesto: Partial<PuestoTipo>): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/puestos-tipo/calcular`, puesto, { withCredentials: true });
  }

  /**
   * Solicita el detalle desglosado de importes mensuales y extraordinarios.
   * @param puesto Datos parciales del puesto con los que se hará el cálculo.
   * @returns Observable con el detalle de importes generado por el backend.
   */
  calcularImportesDetalle(puesto: Partial<PuestoTipo>): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/puestos-tipo/calcular-detalle`, puesto, { withCredentials: true });
  }

  /**
   * Recupera el catálogo de estudios disponible para los puestos tipo.
   * @returns Observable con las opciones de estudio configuradas.
   */
  getEstudios(): Observable<Estudio[]> {
    return this.http.get<Estudio[]>(`${this.apiUrl}/puestos-tipo/estudios`, { withCredentials: true });
  }

  /**
   * Recupera el catálogo de jornadas disponible para los puestos tipo.
   * @returns Observable con las jornadas configuradas.
   */
  getJornadas(): Observable<Jornada[]> {
    return this.http.get<Jornada[]>(`${this.apiUrl}/puestos-tipo/jornadas`, { withCredentials: true });
  }

  /**
   * Obtiene el importe específico por defecto según estudio y comunidad.
   * @param codEstudio Código del estudio seleccionado.
   * @param idComunidad Identificador opcional de la comunidad autónoma.
   * @returns Observable con el importe específico sugerido por la API.
   */
  getImporteEspecifico(codEstudio: string, idComunidad?: number): Observable<ApiResponse> {
    const query = idComunidad !== undefined ? `?idComunidad=${idComunidad}` : '';
    return this.http.get<ApiResponse>(`${this.apiUrl}/puestos-tipo/especifico-default/${codEstudio}${query}`, { withCredentials: true });
  }
}
