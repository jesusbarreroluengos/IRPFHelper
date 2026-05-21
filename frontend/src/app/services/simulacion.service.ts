import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SimulacionRequest {
  idPersona: number;
  ejercicio: number;
  impBrutoAbonado?: number | null;
  impRetencionesPracticadas?: number | null;
  impGastosRealizados?: number | null;
  fechaHastaAbonado?: string | null;
}

export interface SimulacionMes {
  mesNumero: number;
  mesNombre: string;
  importeBrutoMes: number;
  importeCotizadoMes: number;
}

export interface SimulacionResponseData {
  meses: SimulacionMes[];
  totalBrutoPendiente: number;
  totalGastosPendiente: number;
  porcIrpf: number;
  importeBrutoAnual: number | null;
  importeRetencionesAnual: number | null;
  idSimulacion: number;
}

export interface ApiResponse {
  success: boolean;
  message: string;
  data?: SimulacionResponseData;
}

@Injectable({
  providedIn: 'root'
})
/**
 * Servicio que comunica el frontend con la API de cálculo para solicitar y
 * recuperar los resultados de la simulación.
 */
export class SimulacionService {
  private apiUrl = environment.apiUrl || '/api';

  constructor(private http: HttpClient) {}

  /**
   * Envía al backend los datos necesarios para calcular la simulación de IRPF.
   * @param payload Datos de entrada de la simulación, incluidos persona y ejercicio.
   * @returns Observable con el resultado agregado de la simulación.
   */
  ejecutarSimulacion(payload: SimulacionRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/simulacion/ejecutar`, payload, { withCredentials: true });
  }
}

