import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { FrontendLoggingService } from './services/frontend-logging.service';

@Component({
  selector: 'app-root',
  standalone: false,
  template: `
    <router-outlet></router-outlet>
  `
})
/**
 * Componente raíz que inicializa los mecanismos de trazabilidad global y
 * actúa como contenedor del sistema de navegación.
 */
export class AppComponent implements OnInit {
  constructor(
    private readonly router: Router,
    private readonly loggingService: FrontendLoggingService
  ) {}

  /**
   * Inicializa los listeners globales de navegación y errores no controlados del frontend.
   */
  ngOnInit(): void {
    this.loggingService.logAction('Aplicacion iniciada');

    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd)
    ).subscribe((event) => {
      const navigation = event as NavigationEnd;
      this.loggingService.logAction('Navegacion', {
        url: navigation.urlAfterRedirects
      });
    });

    window.addEventListener('unhandledrejection', (event: PromiseRejectionEvent) => {
      this.loggingService.logUnhandledError(event.reason);
    });
  }
}
