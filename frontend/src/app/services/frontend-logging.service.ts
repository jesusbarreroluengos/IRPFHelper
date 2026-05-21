import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, EMPTY, Observable } from 'rxjs';
import { environment } from '../../environments/environment';

type FrontendLogLevel = 'DEBUG' | 'INFO' | 'WARN' | 'ERROR';
type FrontendLogType = 'ACTION' | 'HTTP' | 'ERROR';

interface FrontendLogPayload {
  timestamp: string;
  level: FrontendLogLevel;
  type: FrontendLogType;
  message: string;
  context?: string;
  url: string;
  method?: string;
  statusCode?: number;
  username?: string | null;
  userAgent: string;
  stackTrace?: string;
}

@Injectable({
  providedIn: 'root'
})
/**
 * Servicio transversal de trazabilidad que registra acciones del usuario y
 * errores del cliente para apoyar el análisis técnico de la aplicación.
 */
export class FrontendLoggingService {
  private readonly endpoint = `${environment.apiUrl}/logs/frontend`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Registra una acción funcional realizada por el usuario en la interfaz.
   * @param message Descripción breve de la acción ejecutada.
   * @param context Información opcional adicional para contextualizar el evento.
   */
  logAction(message: string, context?: unknown): void {
    this.send({
      level: 'INFO',
      type: 'ACTION',
      message,
      context: this.serialize(context)
    });
  }

  /**
   * Registra una petición HTTP completada correctamente.
   * @param method Método HTTP utilizado en la solicitud.
   * @param url URL invocada por el cliente.
   * @param statusCode Código de estado recibido en la respuesta.
   */
  logHttp(method: string, url: string, statusCode: number): void {
    this.send({
      level: 'INFO',
      type: 'HTTP',
      message: `HTTP ${method} ${url}`,
      method,
      statusCode,
      context: this.serialize({ ok: statusCode >= 200 && statusCode < 300 })
    });
  }

  /**
   * Registra un fallo HTTP producido durante una petición del frontend.
   * @param method Método HTTP de la petición fallida.
   * @param url URL invocada por el cliente.
   * @param statusCode Código de estado asociado al error.
   * @param error Error original capturado para adjuntarlo al log.
   */
  logHttpError(method: string, url: string, statusCode: number, error: unknown): void {
    this.send({
      level: 'ERROR',
      type: 'HTTP',
      message: `HTTP ${method} ${url} fallo`,
      method,
      statusCode,
      context: this.serialize(error),
      stackTrace: this.extractStack(error)
    });
  }

  /**
   * Registra una excepción no controlada detectada en el cliente.
   * @param error Error capturado por el manejador global o por la aplicación.
   */
  logUnhandledError(error: unknown): void {
    this.send({
      level: 'ERROR',
      type: 'ERROR',
      message: 'Error no controlado en frontend',
      context: this.serialize(error),
      stackTrace: this.extractStack(error)
    });
  }

  /**
   * Completa el payload de trazabilidad común y lo envía al backend.
   * @param partial Datos del log específicos del evento que se quiere registrar.
   */
  private send(partial: Omit<FrontendLogPayload, 'timestamp' | 'url' | 'username' | 'userAgent'>): void {
    const payload: FrontendLogPayload = {
      ...partial,
      timestamp: new Date().toISOString(),
      url: window.location.href,
      username: localStorage.getItem('username'),
      userAgent: navigator.userAgent
    };

    this.http.post<void>(this.endpoint, payload).pipe(
      catchError((): Observable<void> => EMPTY)
    ).subscribe();
  }

  /**
   * Serializa un valor arbitrario para poder incluirlo en el contexto del log.
   * @param value Valor a convertir en texto legible para su trazabilidad.
   * @returns Cadena serializada o `undefined` si no hay valor útil que registrar.
   */
  private serialize(value: unknown): string | undefined {
    if (value === undefined || value === null) {
      return undefined;
    }

    if (typeof value === 'string') {
      return value;
    }

    try {
      return JSON.stringify(value);
    } catch {
      return String(value);
    }
  }

  /**
   * Extrae la traza de pila cuando el error la contiene en un formato conocido.
   * @param error Error del que se intentará recuperar la pila.
   * @returns Pila serializada o `undefined` si no está disponible.
   */
  private extractStack(error: unknown): string | undefined {
    if (error instanceof Error) {
      return error.stack;
    }

    if (typeof error === 'object' && error !== null && 'stack' in error) {
      const stack = (error as { stack?: unknown }).stack;
      return typeof stack === 'string' ? stack : undefined;
    }

    return undefined;
  }
}

