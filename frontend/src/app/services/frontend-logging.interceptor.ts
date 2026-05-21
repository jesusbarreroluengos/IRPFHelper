import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse
} from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { FrontendLoggingService } from './frontend-logging.service';

@Injectable()
/**
 * Interceptor HTTP encargado de instrumentar las peticiones salientes para su
 * posterior seguimiento y análisis.
 */
export class FrontendLoggingInterceptor implements HttpInterceptor {
  constructor(private readonly loggingService: FrontendLoggingService) {}

  /**
   * Intercepta cada petición HTTP para registrar respuestas correctas y errores.
   * @param req Solicitud HTTP original emitida por Angular.
   * @param next Siguiente manejador de la cadena de interceptores.
   * @returns Observable con la secuencia original instrumentada para logging.
   */
  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (req.url.includes('/logs/frontend')) {
      return next.handle(req);
    }

    return next.handle(req).pipe(
      tap({
        next: (event: HttpEvent<unknown>) => {
          if (event instanceof HttpResponse) {
            this.loggingService.logHttp(req.method, req.urlWithParams, event.status);
          }
        },
        error: (error: unknown) => {
          const statusCode = error instanceof HttpErrorResponse ? error.status : 0;
          this.loggingService.logHttpError(req.method, req.urlWithParams, statusCode, error);
        }
      })
    );
  }
}
