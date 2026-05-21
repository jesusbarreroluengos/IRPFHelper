import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ContratoPersona {
  idContratoPersona?: number;
  idPersona: number;
  idPuestoTipo: number;
  fechaDesde: string;
  fechaHasta?: string | null;
  indVacNoDisfrutadas: string;
  puestoTipo?: {
    idPuestoTipo: number;
    nomPuesto: string;
  };
}

export interface ApiResponse {
  success: boolean;
  message: string;
  data?: any;
}

@Injectable({
  providedIn: 'root'
})
/**
 * Servicio que abstrae la gestión de contratos asociados a cada persona
 * simulada y normaliza la interacción con la API correspondiente.
 */
export class ContratoPersonaService {
  private apiUrl = environment.apiUrl || '/api';

  constructor(private http: HttpClient) {}

  /**
   * Recupera todos los contratos asociados a una persona simulada.
   * @param idPersona Identificador de la persona cuyos contratos se consultan.
   * @returns Observable con la lista de contratos obtenida desde la API.
   */
  getContratosByPersona(idPersona: number): Observable<ContratoPersona[]> {
    return this.http.get<ContratoPersona[]>(`${this.apiUrl}/contratos-persona/persona/${idPersona}`, {
      withCredentials: true
    });
  }

  /**
   * Recupera un contrato concreto por su identificador.
   * @param id Identificador del contrato que se desea consultar.
   * @returns Observable con los datos del contrato solicitado.
   */
  getContratoById(id: number): Observable<ContratoPersona> {
    return this.http.get<ContratoPersona>(`${this.apiUrl}/contratos-persona/${id}`, {
      withCredentials: true
    });
  }

  /**
   * Crea un nuevo contrato para una persona simulada.
   * @param contrato Datos parciales del contrato que se enviarán al backend.
   * @returns Observable con el resultado de la creación.
   */
  createContrato(contrato: Partial<ContratoPersona>): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/contratos-persona/create`, contrato, {
      withCredentials: true
    });
  }

  /**
   * Actualiza un contrato existente.
   * @param id Identificador del contrato que se desea modificar.
   * @param contrato Datos parciales con la nueva información del contrato.
   * @returns Observable con el resultado de la actualización.
   */
  updateContrato(id: number, contrato: Partial<ContratoPersona>): Observable<ApiResponse> {
    return this.http.put<ApiResponse>(`${this.apiUrl}/contratos-persona/update/${id}`, contrato, {
      withCredentials: true
    });
  }

  /**
   * Elimina un contrato existente.
   * @param id Identificador del contrato que se desea borrar.
   * @returns Observable con el resultado de la eliminación.
   */
  deleteContrato(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${this.apiUrl}/contratos-persona/delete/${id}`, {
      withCredentials: true
    });
  }
}

