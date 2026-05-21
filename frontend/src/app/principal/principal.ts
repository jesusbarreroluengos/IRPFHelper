import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { forkJoin, of, Subscription } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AuthService } from '../auth.service';
import { PersonaSimuladaService, PersonaSimulada } from '../services/persona-simulada.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';
import { PuestoTipoService } from '../services/puesto-tipo.service';
import { ContratoPersonaService } from '../services/contrato-persona.service';

@Component({
  selector: 'app-principal',
  imports: [CommonModule],
  templateUrl: './principal.html'
})
/**
 * Componente principal de la aplicaciÃ³n.
 * Presenta el tablero de navegaciÃ³n y supervisa el grado de avance de las
 * tareas previas requeridas para ejecutar la simulaciÃ³n.
 */
export class Principal implements OnInit, OnDestroy {
  currentUsername: string | null = null;
  currentUserId: number | null = null;
  usernameFormatted: string | null = null;
  menuItems: any[] = [];
  successMessage: string = '';
  showSuccessMessage: boolean = false;
  personaSeleccionada: PersonaSimulada | null = null;
  canSwitchDocente = false;
  isAdmin = false;

  paso1Completado = false;
  paso2Completado = false;
  paso3Completado = false;
  paso4Disponible = false;

  readonly paso1MensajeOk = 'Ya has incluido un docente simulado. Puedes incluir m\u00e1s cuando lo necesites.';
  readonly paso1MensajeKo = 'Es necesario incluir un docente simulado para poder realizar la simulaci\u00f3n de retribnuciones y de IRPF.';
  readonly paso2MensajeOk = 'Ya has incluido un puesto tipo para el docente simulado. Puedes incluir m\u00e1s cuando lo necesites.';
  readonly paso2MensajeKo = 'Es necesario incluir al menos un puesto tipo para el docente simulado si queremos realizar la simulaci\u00f3n de retribnuciones y de IRPF.';
  readonly paso3MensajeOk = 'Ya has incluido un puesto tipo para el docente simulado. Puedes incluir m\u00e1s cuando lo necesites.';
  readonly paso3MensajeKo = 'Es necesario incluir al menos un contrato para el docente simulado si queremos realizar la simulaci\u00f3n de retribnuciones y de IRPF.';
  readonly paso4MensajeOk = 'Ya puedes realizar una simulaci\u00f3n';
  readonly paso4MensajeKo = 'Es necesario completar el paso 3 para poder realizar una simulaci\u00f3n.';

  private personaSeleccionadaSub?: Subscription;
  private queryParamsSub?: Subscription;

  constructor(
    private router: Router,
    private authService: AuthService,
    private route: ActivatedRoute,
    private personaSimuladaService: PersonaSimuladaService,
    private personaSelectionService: PersonaSimuladaSelectionService,
    private puestoTipoService: PuestoTipoService,
    private contratoPersonaService: ContratoPersonaService
  ) {}

  /**
   * Inicializa el estado de la pantalla principal, la selecciÃ³n de persona y la navegaciÃ³n guiada.
   */
  ngOnInit(): void {
    this.currentUsername = this.authService.getUsername();
    this.currentUserId = this.authService.getUserId();
    this.usernameFormatted = this.formatCamelCase(this.currentUsername || '');

    this.updateMenuItems();
    this.suscribirseAPersonaSeleccionada();
    this.cargarEstadoInicialPasos();

    this.queryParamsSub = this.route.queryParams.subscribe(params => {
      if (params['passwordChanged'] === 'true') {
        this.showPasswordChangedMessage();
        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: {}
        });
      }
    });
  }

  /**
   * Libera las suscripciones activas del componente antes de destruir la vista.
   */
  ngOnDestroy(): void {
    this.personaSeleccionadaSub?.unsubscribe();
    this.queryParamsSub?.unsubscribe();
  }

  /**
   * Muestra temporalmente el mensaje de confirmaciÃ³n tras un cambio de contraseÃ±a exitoso.
   */
  showPasswordChangedMessage(): void {
    this.successMessage = '\u00a1Contrase\u00f1a cambiada exitosamente!';
    this.showSuccessMessage = true;

    setTimeout(() => {
      this.showSuccessMessage = false;
    }, 5000);
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
   * Sincroniza el componente con la persona simulada seleccionada globalmente.
   */
  private suscribirseAPersonaSeleccionada(): void {
    this.personaSeleccionadaSub = this.personaSelectionService.personaSeleccionada$.subscribe(persona => {
      this.personaSeleccionada = persona;
      this.actualizarEstadoPaso2y3();
    });
  }

  /**
   * Carga el estado inicial del asistente principal a partir del usuario y sus personas disponibles.
   */
  private cargarEstadoInicialPasos(): void {
    if (!this.currentUserId) {
      this.canSwitchDocente = false;
      this.reiniciarEstadoPasos();
      return;
    }

    this.personaSimuladaService.getPersonasSimuladasByUsuario(this.currentUserId).subscribe({
      next: (personas) => {
        const listaPersonas = personas || [];
        this.paso1Completado = listaPersonas.length > 0;
        this.canSwitchDocente = listaPersonas.length > 1;

        if (listaPersonas.length === 0) {
          this.personaSeleccionada = null;
          this.personaSelectionService.setPersonaSeleccionada(null);
          this.actualizarEstadoPaso2y3();
          return;
        }

        const personaActualValida = !!this.personaSeleccionada?.idPersona
          && listaPersonas.some(persona => persona.idPersona === this.personaSeleccionada?.idPersona);

        if (!personaActualValida) {
          const personaPorDefecto = listaPersonas.reduce((min, persona) =>
            (persona.idPersona || 0) < (min.idPersona || 0) ? persona : min
          );
          this.personaSeleccionada = personaPorDefecto;
          this.personaSelectionService.setPersonaSeleccionada(personaPorDefecto);
        }

        this.actualizarEstadoPaso2y3();
      },
      error: () => {
        this.canSwitchDocente = false;
        this.reiniciarEstadoPasos();
      }
    });
  }

  /**
   * EvalÃºa si la persona seleccionada ya dispone de puestos y contratos para activar los pasos siguientes.
   */
  private actualizarEstadoPaso2y3(): void {
    const idPersona = this.personaSeleccionada?.idPersona;
    if (!idPersona) {
      this.paso2Completado = false;
      this.paso3Completado = false;
      this.sincronizarPaso4();
      return;
    }

    forkJoin({
      puestos: this.puestoTipoService.getPuestosByPersona(idPersona).pipe(catchError(() => of([]))),
      contratos: this.contratoPersonaService.getContratosByPersona(idPersona).pipe(catchError(() => of([])))
    }).subscribe(({ puestos, contratos }) => {
      this.paso2Completado = (puestos?.length || 0) > 0;
      this.paso3Completado = (contratos?.length || 0) > 0;
      this.sincronizarPaso4();
    });
  }

  /**
   * Activa el paso de simulaciÃ³n solo cuando los pasos previos estÃ¡n completos.
   */
  private sincronizarPaso4(): void {
    this.paso4Disponible = this.paso3Completado;
  }

  /**
   * Restablece el estado del asistente cuando no hay una persona seleccionada vÃ¡lida.
   */
  private reiniciarEstadoPasos(): void {
    this.paso1Completado = false;
    this.paso2Completado = false;
    this.paso3Completado = false;
    this.paso4Disponible = false;
  }

  /**
   * Navega a una ruta permitida del flujo principal.
   * @param route Ruta interna a la que se desea acceder.
   */
  navigateTo(route: string): void {
    if (!this.puedeNavegarARuta(route)) {
      return;
    }

    if (route === '/login') {
      this.authService.clearUsername();
      localStorage.removeItem('token');
      localStorage.removeItem('personaSimuladaSeleccionada');
    }
    this.router.navigate([route]);
  }

  /**
   * Abre el selector de docentes cuando el usuario puede alternar entre personas simuladas.
   */
  navigateToDocenteSelector(): void {
    if (!this.canSwitchDocente) {
      return;
    }
    this.router.navigate(['/seleccion-docente']);
  }

  /**
   * Indica si un paso concreto del asistente se encuentra activo.
   * @param paso NÃºmero de paso del flujo principal.
   * @returns `true` cuando el paso puede utilizarse en el estado actual.
   */
  isPasoActivo(paso: 1 | 2 | 3 | 4): boolean {
    if (paso === 1) {
      return true;
    }
    if (paso === 2) {
      return this.paso1Completado;
    }
    if (paso === 3) {
      return this.paso2Completado;
    }
    return this.paso3Completado;
  }

  /**
   * Comprueba si una ruta estÃ¡ habilitada segÃºn el progreso del asistente principal.
   * @param route Ruta cuya disponibilidad se quiere validar.
   * @returns `true` si la navegaciÃ³n estÃ¡ permitida.
   */
  private puedeNavegarARuta(route: string): boolean {
    if (route === '/datos-personales') {
      return this.isPasoActivo(1);
    }
    if (route === '/puestos-tipo') {
      return this.isPasoActivo(2);
    }
    if (route === '/contratos') {
      return this.isPasoActivo(3);
    }
    if (route === '/simulacion') {
      return this.isPasoActivo(4);
    }
    return true;
  }

  /**
   * Convierte un identificador o texto compuesto a un formato legible con capitalizaciÃ³n por palabra.
   * @param value Texto de entrada en camel case o con guiones bajos.
   * @returns Texto normalizado para su presentaciÃ³n en la interfaz.
   */
  formatCamelCase(value: string): string {
    return value
      .toLowerCase()
      .replace(/[_-]/g, ' ')
      .replace(/\b\w/g, char => char.toUpperCase());
  }
}

