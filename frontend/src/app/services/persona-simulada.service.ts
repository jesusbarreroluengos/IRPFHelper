import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface PersonaSimulada {
  idPersona?: number;
  id: number;
  nombre: string;
  nifFicticio?: string;
  anioNac: number;
  codDiscapacidad: string;
  codSitfam: string;
  indCeumelilla: string;
  codContrato: string;
  impPensionConyuge?: number;
  impPensionHijos?: number;
  idComunidad?: number;
  discapacidad?: {
    codDiscapacidad: string;
    descDiscapacidad: string;
  };
  situacionFamiliar?: {
    codSitfam: string;
    descSitfam: string;
  };
  contrato?: {
    codContrato: string;
    descContrato: string;
  };
  comunidad?: {
    idComunidad: number;
    descComunidad: string;
  };
}

export interface Discapacidad {
  codDiscapacidad: string;
  descDiscapacidad: string;
}

export interface SituacionFamiliar {
  codSitfam: string;
  descSitfam: string;
}

export interface Contrato {
  codContrato: string;
  descContrato: string;
}

export interface Comunidad {
  idComunidad: number;
  descComunidad: string;
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
 * Servicio encargado de gestionar las personas simuladas y sus relaciones
 * familiares, contractuales y territoriales.
 */
export class PersonaSimuladaService {
  private apiUrl = environment.apiUrl || '/api';

  constructor(private http: HttpClient) { }

  // Obtener todas las personas simuladas de un usuario
  /**
   * Recupera todas las personas simuladas asociadas a un usuario.
   * @param idUsuario Identificador del usuario propietario de las personas simuladas.
   * @returns Observable con la lista de personas recuperadas.
   */
  getPersonasSimuladasByUsuario(idUsuario: number): Observable<PersonaSimulada[]> {
    return this.http.get<PersonaSimulada[]>(`${this.apiUrl}/personas-simuladas/usuario/${idUsuario}/con-relaciones`, {
      withCredentials: true
    });
  }

  // Obtener una persona simulada por ID
  /**
   * Recupera una persona simulada concreta por su identificador.
   * @param id Identificador de la persona que se desea consultar.
   * @returns Observable con la persona simulada solicitada.
   */
  getPersonaSimuladaById(id: number): Observable<PersonaSimulada> {
    return this.http.get<PersonaSimulada>(`${this.apiUrl}/personas-simuladas/${id}`, {
      withCredentials: true
    });
  }

  // Crear una nueva persona simulada
  /**
   * Crea una nueva persona simulada en backend.
   * @param personaSimulada Datos parciales de la persona que se va a persistir.
   * @returns Observable con el resultado de la creación.
   */
  createPersonaSimulada(personaSimulada: Partial<PersonaSimulada>): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/personas-simuladas/create`, personaSimulada, {
      withCredentials: true
    });
  }

  // Actualizar una persona simulada
  /**
   * Actualiza una persona simulada existente.
   * @param id Identificador de la persona que se desea modificar.
   * @param personaSimulada Datos parciales con los cambios de la persona.
   * @returns Observable con el resultado de la actualización.
   */
  updatePersonaSimulada(id: number, personaSimulada: Partial<PersonaSimulada>): Observable<ApiResponse> {
    return this.http.put<ApiResponse>(`${this.apiUrl}/personas-simuladas/update/${id}`, personaSimulada, {
      withCredentials: true
    });
  }

  // Eliminar una persona simulada
  /**
   * Elimina una persona simulada por su identificador.
   * @param id Identificador de la persona que se desea borrar.
   * @returns Observable con el resultado de la eliminación.
   */
  deletePersonaSimulada(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${this.apiUrl}/personas-simuladas/delete/${id}`, {
      withCredentials: true
    });
  }

  // Obtener todas las discapacidades
  /**
   * Recupera el catálogo de discapacidades disponible para las identidades simuladas.
   * @returns Observable con las discapacidades configuradas.
   */
  getDiscapacidades(): Observable<Discapacidad[]> {
    return this.http.get<Discapacidad[]>(`${this.apiUrl}/discapacidades/all`, {
      withCredentials: true
    });
  }

  // Obtener todas las situaciones familiares
  /**
   * Recupera el catálogo de situaciones familiares.
   * @returns Observable con las situaciones familiares disponibles.
   */
  getSituacionesFamiliares(): Observable<SituacionFamiliar[]> {
    return this.http.get<SituacionFamiliar[]>(`${this.apiUrl}/situaciones-familiares/all`, {
      withCredentials: true
    });
  }

  // Obtener todos los contratos
  /**
   * Recupera el catálogo de tipos de contrato asociado a identidades simuladas.
   * @returns Observable con los contratos configurados.
   */
  getContratos(): Observable<Contrato[]> {
    return this.http.get<Contrato[]>(`${this.apiUrl}/contratos/all`, {
      withCredentials: true
    });
  }

  /**
   * Recupera el catálogo de comunidades autónomas disponibles.
   * @returns Observable con las comunidades configuradas.
   */
  getComunidades(): Observable<Comunidad[]> {
    return this.http.get<Comunidad[]>(`${this.apiUrl}/comunidades/all`, {
      withCredentials: true
    });
  }

  // ===== MÉTODOS PARA DESCENDIENTES =====
  
  // Obtener descendientes de una persona simulada
  /**
   * Recupera los descendientes asociados a una persona simulada.
   * @param idPersona Identificador de la persona cuyas relaciones se consultan.
   * @returns Observable con la lista de descendientes.
   */
  getDescendientes(idPersona: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/descendientes/persona/${idPersona}`, {
      withCredentials: true
    });
  }

  // Crear un nuevo descendiente
  /**
   * Crea un descendiente para la persona simulada indicada en el payload.
   * @param descendiente Datos del descendiente que se desea persistir.
   * @returns Observable con el resultado de la creación.
   */
  createDescendiente(descendiente: any): Observable<ApiResponse> {
    return this.http.post(`${this.apiUrl}/descendientes/create`, descendiente, {
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/plain, application/json' // Aceptar tanto texto plano como JSON
      },
      responseType: 'text' // Esperar respuesta como texto
    }).pipe(
      map(response => {
        // Si la respuesta contiene "exitosamente", es un éxito
        if (typeof response === 'string' && response.includes('exitosamente')) {
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
        
        // Intentar parsear como JSON si es posible
        try {
          const jsonResponse = JSON.parse(response);
          return jsonResponse as ApiResponse;
        } catch (e) {
          // Si no es JSON válido, tratar como mensaje de éxito
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
      }),
      catchError(error => {
        console.error('Error en createDescendiente:', error);
        
        // Si el error es por JSON inválido, intentar extraer el mensaje del texto
        if (error.error && typeof error.error === 'string') {
          // Crear una respuesta de error personalizada
          const customError = {
            ...error,
            error: {
              success: false,
              message: error.error
            }
          };
          return throwError(() => customError);
        }
        
        return throwError(() => error);
      })
    );
  }

  // Actualizar un descendiente
  /**
   * Actualiza un descendiente existente.
   * @param id Identificador del descendiente que se desea modificar.
   * @param descendiente Datos actualizados del descendiente.
   * @returns Observable con el resultado de la actualización.
   */
  updateDescendiente(id: number, descendiente: any): Observable<ApiResponse> {
    return this.http.put(`${this.apiUrl}/descendientes/update/${id}`, descendiente, {
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/plain, application/json' // Aceptar tanto texto plano como JSON
      },
      responseType: 'text' // Esperar respuesta como texto
    }).pipe(
      map(response => {
        // Si la respuesta contiene "exitosamente", es un éxito
        if (typeof response === 'string' && response.includes('exitosamente')) {
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
        
        // Intentar parsear como JSON si es posible
        try {
          const jsonResponse = JSON.parse(response);
          return jsonResponse as ApiResponse;
        } catch (e) {
          // Si no es JSON válido, tratar como mensaje de éxito
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
      }),
      catchError(error => {
        console.error('Error en updateDescendiente:', error);
        
        // Si el error es por JSON inválido, intentar extraer el mensaje del texto
        if (error.error && typeof error.error === 'string') {
          return throwError(() => ({
            ...error,
            error: {
              message: error.error,
              success: false
            }
          }));
        }
        
        return throwError(() => error);
      })
    );
  }

  // Eliminar un descendiente
  /**
   * Elimina un descendiente por su identificador.
   * @param id Identificador del descendiente que se desea borrar.
   * @returns Observable con el resultado de la eliminación.
   */
  deleteDescendiente(id: number): Observable<ApiResponse> {
    return this.http.delete(`${this.apiUrl}/descendientes/delete/${id}`, {
      withCredentials: true,
      headers: {
        'Accept': 'text/plain, application/json'
      },
      responseType: 'text'
    }).pipe(
      map(response => {
        // Si la respuesta contiene "exitosamente", es un éxito
        if (typeof response === 'string' && response.includes('exitosamente')) {
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
        
        // Intentar parsear como JSON si es posible
        try {
          const jsonResponse = JSON.parse(response);
          return jsonResponse as ApiResponse;
        } catch (e) {
          // Si no es JSON válido, tratar como mensaje de éxito
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
      }),
      catchError(error => {
        console.error('Error en deleteDescendiente:', error);
        
        // Si el error es por JSON inválido, intentar extraer el mensaje del texto
        if (error.error && typeof error.error === 'string') {
          const customError = {
            ...error,
            error: {
              success: false,
              message: error.error
            }
          };
          return throwError(() => customError);
        }
        
        return throwError(() => error);
      })
    );
  }

  // ===== MÉTODOS PARA ASCENDIENTES =====
  
  // Obtener ascendientes de una persona simulada
  /**
   * Recupera los ascendientes asociados a una persona simulada.
   * @param idPersona Identificador de la persona cuyas relaciones se consultan.
   * @returns Observable con la lista de ascendientes.
   */
  getAscendientes(idPersona: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/ascendientes/persona/${idPersona}`, {
      withCredentials: true
    });
  }

  // Crear un nuevo ascendiente
  /**
   * Crea un ascendiente para la persona simulada indicada en el payload.
   * @param ascendiente Datos del ascendiente que se desea persistir.
   * @returns Observable con el resultado de la creación.
   */
  createAscendiente(ascendiente: any): Observable<ApiResponse> {
    return this.http.post(`${this.apiUrl}/ascendientes/create`, ascendiente, {
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/plain, application/json' // Aceptar tanto texto plano como JSON
      },
      responseType: 'text' // Esperar respuesta como texto
    }).pipe(
      map(response => {
        // Si la respuesta contiene "exitosamente", es un éxito
        if (typeof response === 'string' && response.includes('exitosamente')) {
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
        
        // Intentar parsear como JSON si es posible
        try {
          const jsonResponse = JSON.parse(response);
          return jsonResponse as ApiResponse;
        } catch (e) {
          // Si no es JSON válido, tratar como mensaje de éxito
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
      }),
      catchError(error => {
        console.error('Error en createAscendiente:', error);
        
        // Si el error es por JSON inválido, intentar extraer el mensaje del texto
        if (error.error && typeof error.error === 'string') {
          // Crear una respuesta de error personalizada
          const customError = {
            ...error,
            error: {
              success: false,
              message: error.error
            }
          };
          return throwError(() => customError);
        }
        
        return throwError(() => error);
      })
    );
  }

  // Actualizar un ascendiente
  /**
   * Actualiza un ascendiente existente.
   * @param id Identificador del ascendiente que se desea modificar.
   * @param ascendiente Datos actualizados del ascendiente.
   * @returns Observable con el resultado de la actualización.
   */
  updateAscendiente(id: number, ascendiente: any): Observable<ApiResponse> {
    return this.http.put(`${this.apiUrl}/ascendientes/update/${id}`, ascendiente, {
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/plain, application/json' // Aceptar tanto texto plano como JSON
      },
      responseType: 'text' // Esperar respuesta como texto
    }).pipe(
      map(response => {
        // Si la respuesta contiene "exitosamente", es un éxito
        if (typeof response === 'string' && response.includes('exitosamente')) {
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
        
        // Intentar parsear como JSON si es posible
        try {
          const jsonResponse = JSON.parse(response);
          return jsonResponse as ApiResponse;
        } catch (e) {
          // Si no es JSON válido, tratar como mensaje de éxito
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
      }),
      catchError(error => {
        console.error('Error en updateAscendiente:', error);
        
        // Si el error es por JSON inválido, intentar extraer el mensaje del texto
        if (error.error && typeof error.error === 'string') {
          return throwError(() => ({
            ...error,
            error: {
              message: error.error,
              success: false
            }
          }));
        }
        
        return throwError(() => error);
      })
    );
  }

  // Eliminar un ascendiente
  /**
   * Elimina un ascendiente por su identificador.
   * @param id Identificador del ascendiente que se desea borrar.
   * @returns Observable con el resultado de la eliminación.
   */
  deleteAscendiente(id: number): Observable<ApiResponse> {
    return this.http.delete(`${this.apiUrl}/ascendientes/delete/${id}`, {
      withCredentials: true,
      headers: {
        'Accept': 'text/plain, application/json'
      },
      responseType: 'text'
    }).pipe(
      map(response => {
        // Si la respuesta contiene "exitosamente", es un éxito
        if (typeof response === 'string' && response.includes('exitosamente')) {
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
        
        // Intentar parsear como JSON si es posible
        try {
          const jsonResponse = JSON.parse(response);
          return jsonResponse as ApiResponse;
        } catch (e) {
          // Si no es JSON válido, tratar como mensaje de éxito
          return {
            success: true,
            message: response
          } as ApiResponse;
        }
      }),
      catchError(error => {
        console.error('Error en deleteAscendiente:', error);
        
        // Si el error es por JSON inválido, intentar extraer el mensaje del texto
        if (error.error && typeof error.error === 'string') {
          const customError = {
            ...error,
            error: {
              success: false,
              message: error.error
            }
          };
          return throwError(() => customError);
        }
        
        return throwError(() => error);
      })
    );
  }
}
