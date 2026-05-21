import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { AuthService } from '../auth.service';
import { PersonaSimulada, PersonaSimuladaService } from '../services/persona-simulada.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';

@Component({
  selector: 'app-selector-docente',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './selector-docente.component.html',
  styleUrls: []
})
/**
 * Componente que permite seleccionar la persona simulada activa sobre la que
 * se aplicarÃ¡n el resto de operaciones del sistema.
 */
export class SelectorDocenteComponent implements OnInit {
  currentUsername: string | null = null;
  currentUserId: number | null = null;
  usernameFormatted: string | null = null;
  menuItems: any[] = [];
  isAdmin = false;

  personaSeleccionada: PersonaSimulada | null = null;
  personasSimuladas: PersonaSimulada[] = [];
  canSwitchDocente = false;

  cargando = false;
  mensajeError = '';

  constructor(
    private router: Router,
    private authService: AuthService,
    private personaSimuladaService: PersonaSimuladaService,
    private personaSelectionService: PersonaSimuladaSelectionService
  ) {}

  /**
   * Inicializa el selector de docentes cargando la persona activa y el listado disponible.
   */
  ngOnInit(): void {
    this.currentUsername = this.authService.getUsername();
    this.currentUserId = this.authService.getUserId();
    this.usernameFormatted = this.formatCamelCase(this.currentUsername || '');

    this.updateMenuItems();
    this.suscribirseAPersonaSeleccionada();
    this.cargarDocentes();
  }

  /**
   * Mantiene sincronizada la selecciÃ³n local con el servicio compartido de personas simuladas.
   */
  private suscribirseAPersonaSeleccionada(): void {
    this.personaSelectionService.personaSeleccionada$.subscribe(persona => {
      this.personaSeleccionada = persona;
    });
  }

  /**
   * Recupera y ordena los docentes disponibles para el usuario autenticado.
   */
  private cargarDocentes(): void {
    if (!this.currentUserId) {
      this.router.navigate(['/login']);
      return;
    }

    this.cargando = true;
    this.mensajeError = '';

    this.personaSimuladaService.getPersonasSimuladasByUsuario(this.currentUserId).subscribe({
      next: personas => {
        const lista = (personas || []).slice().sort((a, b) => a.nombre.localeCompare(b.nombre));
        this.personasSimuladas = lista;
        this.canSwitchDocente = lista.length > 1;

        if (lista.length === 0) {
          this.personaSelectionService.setPersonaSeleccionada(null);
          this.router.navigate(['/datos-personales']);
          return;
        }

        if (lista.length === 1) {
          this.personaSelectionService.setPersonaSeleccionada(lista[0]);
          this.router.navigate(['/principal']);
          return;
        }

        const personaActualValida = !!this.personaSeleccionada?.idPersona
          && lista.some(persona => persona.idPersona === this.personaSeleccionada?.idPersona);

        if (!personaActualValida) {
          const personaPorDefecto = lista.reduce((min, persona) =>
            (persona.idPersona || 0) < (min.idPersona || 0) ? persona : min
          );
          this.personaSelectionService.setPersonaSeleccionada(personaPorDefecto);
        }

        this.cargando = false;
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar los docentes simulados';
        this.canSwitchDocente = false;
        this.cargando = false;
      }
    });
  }

  /**
   * Recalcula las opciones de menÃº visibles segÃºn el rol del usuario autenticado.
   */
  updateMenuItems(): void {
    this.isAdmin = this.authService.isAdmin();
    const allMenuItems = [
      { name: 'Inicio', route: '/principal', icon: '\ud83c\udfe0' },
      { name: 'Administración', route: '/administracion', icon: '⚙️', requiresAdmin: true },
      { name: 'Docentes Simulados', route: '/datos-personales', icon: '\ud83d\udc64' },
      { name: 'Puestos Tipo', route: '/puestos-tipo', icon: '\ud83e\uddd1\u200d\ud83c\udfeb' },
      { name: 'Contratos', route: '/contratos', icon: '\ud83d\udcc4' },
      { name: 'Simulaci\u00f3n IRPF', route: '/simulacion', icon: '\ud83d\udcb0' }
    ];

    this.menuItems = allMenuItems.filter(item => {
      if (item.requiresAdmin) {
        return this.isAdmin;
      }
      return true;
    });
  }

  /**
   * Establece un docente como persona activa y vuelve a la vista principal.
   * @param persona Persona simulada elegida por el usuario.
   */
  seleccionarDocente(persona: PersonaSimulada): void {
    this.personaSelectionService.setPersonaSeleccionada(persona);
    this.router.navigate(['/principal']);
  }

  /**
   * Navega al selector solo cuando el usuario dispone de permisos para cambiar de docente.
   */
  navigateToDocenteSelector(): void {
    if (!this.canSwitchDocente) {
      return;
    }
    this.router.navigate(['/seleccion-docente']);
  }

  /**
   * Navega a una ruta interna de la aplicaciÃ³n.
   * @param route Ruta destino dentro del frontend.
   */
  navigateTo(route: string): void {
    if (route === '/login') {
      this.authService.clearUsername();
      localStorage.removeItem('token');
      localStorage.removeItem('personaSimuladaSeleccionada');
    }
    this.router.navigate([route]);
  }

  /**
   * Convierte un texto tÃ©cnico a una etiqueta mÃ¡s legible para la interfaz.
   * @param value Texto original a formatear.
   * @returns Cadena con capitalizaciÃ³n por palabra.
   */
  formatCamelCase(value: string): string {
    return value
      .toLowerCase()
      .replace(/[_-]/g, ' ')
      .replace(/\b\w/g, char => char.toUpperCase());
  }
}

