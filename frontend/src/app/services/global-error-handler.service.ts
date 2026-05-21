import { ErrorHandler, Injectable } from '@angular/core';
import { FrontendLoggingService } from './frontend-logging.service';

@Injectable()
/**
 * Manejador global de errores que centraliza el tratamiento de excepciones no
 * controladas producidas en la capa cliente.
 */
export class GlobalErrorHandler implements ErrorHandler {
  constructor(private readonly loggingService: FrontendLoggingService) {}

  /**
   * Centraliza el tratamiento de errores no controlados y los delega al sistema de logging.
   * @param error Error capturado por Angular en tiempo de ejecución.
   */
  handleError(error: unknown): void {
    this.loggingService.logUnhandledError(error);
    console.error(error);
  }
}
