import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../auth.service';
import { PersonaSimulada, PersonaSimuladaService } from '../services/persona-simulada.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';
import { PuestoTipoService } from '../services/puesto-tipo.service';
import { ContratoPersonaService } from '../services/contrato-persona.service';
import { SimulacionService, SimulacionMes } from '../services/simulacion.service';

interface AyudaContextual {
  titulo: string;
  descripcion: string;
  puntos?: string[];
}

@Component({
  selector: 'app-simulacion-retribuciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './simulacion-retribuciones.component.html',
  styles: [`
    .nominas-previas-group {
      margin-bottom: 30px;
      padding: 20px;
      border: 1px solid #b6d7ff;
      border-radius: 12px;
      background: linear-gradient(180deg, #eef7ff 0%, #dceeff 100%);
    }

    .nominas-previas-group legend {
      padding: 0 10px;
      font-weight: 700;
      color: #1d4f91;
      font-size: 1rem;
    }

    .nominas-previas-group .form-grid {
      margin-bottom: 0;
    }

    .required-mark {
      color: #c62828;
      margin-left: 4px;
    }

    .field-error {
      margin-top: 6px;
      color: #c62828;
      font-size: 0.9rem;
      font-weight: 600;
    }

    .form-input:disabled,
    .form-select:disabled {
      background-color: #f2f6fb;
      color: #6b7280;
      cursor: not-allowed;
    }

    .simulacion-resumen {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 16px;
      margin: 24px 0;
    }

    .simulacion-resumen-card {
      background: #ffffff;
      border: 1px solid #d8e4f2;
      border-radius: 14px;
      padding: 20px;
      box-shadow: 0 4px 16px rgba(15, 23, 42, 0.08);
      text-align: center;
    }

    .simulacion-resumen-card h3 {
      margin: 0 0 10px;
      font-size: 1rem;
      color: #36506c;
    }

    .simulacion-resumen-card p {
      margin: 0;
      font-size: 1.8rem;
      font-weight: 700;
      color: #0f2740;
    }
  `]
})
/**
 * Componente responsable de ejecutar y presentar la simulación de
 * retribuciones e IRPF a partir de la información previamente registrada.
 */
export class SimulacionRetribucionesComponent implements OnInit {
  currentUsername: string | null = null;
  currentUserId: number | null = null;
	usernameFormatted: string | null = null;
  menuItems: any[] = [];
  isAdmin = false;

  personaSeleccionada: PersonaSimulada | null = null;
  canSwitchDocente = false;
  bloqueado = false;
  mensajeBloqueo = '';

  ejercicioSeleccionado: number = new Date().getFullYear();
  ejercicios: number[] = [];
  mesesOpciones: { value: string; label: string }[] = [];

  impBrutoAbonado?: number | null;
  impRetencionesPracticadas?: number | null;
  impGastosRealizados?: number | null;
  mesHastaAbonado: string | null = null;

  resultadoMeses: SimulacionMes[] = [];
  totalBrutoPendiente = 0;
  totalGastosPendiente = 0;
  porcIrpf: number | null = null;
  importeBrutoAnual: number | null = null;
  importeRetencionesAnual: number | null = null;

  cargando = false;
  intentoSimulacion = false;
  mensaje = '';
  tipoMensaje: 'success' | 'error' = 'success';
  mostrarMensaje = false;
  ayudaActivaKey = 'general';
  readonly ayudas: Record<string, AyudaContextual> = {
    general: {
      titulo: 'Ayuda de la pantalla',
      descripcion: 'Esta simulación calcula las retribuciones pendientes del ejercicio y el porcentaje de IRPF a aplicar hasta final de año.',
      puntos: [
        'Selecciona el ejercicio',
		'Si ya has cobrado alguna nómina este año, incluye el mes hasta el que has cobrado y a continuación el importe bruto que te han abonado en esas nominas, la suma de las retenciones de IRPF y la suma de gastos (descuento de cuota obrera de seguridad social)',
        'Pulsa el botón ejecutar y se calculará el porcentaje de IRPF que deberás cotizar a partir del mes siguiente al último mes abonado.'
      ]
    },
    ejercicio: {
      titulo: 'Ejercicio',
      descripcion: 'Año fiscal sobre el que se realizará la simulación.'
    },
    mesHasta: {
      titulo: 'Cobrado hasta',
      descripcion: 'Mes hasta el que se han cobrado ya retribuciones. Si no aplica, déjalo sin seleccionar.'
    },
    impBrutoAbonado: {
      titulo: 'Importe bruto abonado',
      descripcion: 'Suma bruta ya pagada en el ejercicio'
    },
    impRetenciones: {
      titulo: 'Retenciones practicadas',
      descripcion: 'Importe de IRPF ya retenido en nóminas abonadas'
    },
    impGastos: {
      titulo: 'Gastos abonados (S.S.)',
      descripcion: 'Cotizaciones a la Seguridad Social ya descontadas en nóminas abonadas'
    }
  };

  constructor(
    private authService: AuthService,
    private router: Router,
    private personaSimuladaService: PersonaSimuladaService,
    private personaSelectionService: PersonaSimuladaSelectionService,
    private puestoTipoService: PuestoTipoService,
    private contratoPersonaService: ContratoPersonaService,
    private simulacionService: SimulacionService
  ) {}

  /**
   * Inicializa la pantalla de simulación cargando catálogos, persona activa y estado del formulario.
   */
  ngOnInit(): void {
    this.currentUsername = this.authService.getUsername();
    this.currentUserId = this.authService.getUserId();
    this.usernameFormatted = this.formatCamelCase(this.currentUsername || '');  
    this.updateMenuItems();
    this.actualizarDisponibilidadCambioDocente();
    this.initEjercicios();
    this.actualizarMesesOpciones();
    this.suscribirseAPersonaSeleccionada();
    this.cargarPersonaPorDefectoSiNecesario();
  }

  /**
   * Recalcula las opciones de menú visibles según el rol del usuario autenticado.
   */
  private updateMenuItems(): void {
    this.isAdmin = this.authService.isAdmin();
    const allMenuItems = [
      { name: 'Inicio', route: '/principal', icon: '🏠' },
      { name: 'Administración', route: '/administracion', icon: '⚙️', requiresAdmin: true },
      { name: 'Docentes Simulados', route: '/datos-personales', icon: '👤' },
      { name: 'Puestos Tipo', route: '/puestos-tipo', icon: '🧑‍🏫' },
      { name: 'Contratos', route: '/contratos', icon: '📄' },
      { name: 'Simulación IRPF', route: '/simulacion', icon: '💰' }
    ];

    this.menuItems = allMenuItems.filter(item => {
      if (item.requiresAdmin) {
        return this.isAdmin;
      }
      return true;
    });
  }

  /**
   * Genera el rango de ejercicios disponibles para la simulación.
   */
  private initEjercicios(): void {
    const actual = new Date().getFullYear();
    this.ejercicios = [
      actual,
      actual + 1,
      actual - 1,
      actual - 2,
      actual - 3,
      actual - 4
    ];
  }

  /**
   * Regenera las opciones de mes disponibles en función del ejercicio seleccionado.
   */
  private actualizarMesesOpciones(): void {
    const year = this.ejercicioSeleccionado;
    this.mesesOpciones = Array.from({ length: 11 }, (_, i) => i + 1).map(m => ({
      value: `${year}-${String(m).padStart(2, '0')}-01`,
      label: `${String(m).padStart(2, '0')}/${year}`
    }));
  }

  /**
   * Mantiene sincronizada la persona simulada activa con el estado local del componente.
   */
  private suscribirseAPersonaSeleccionada(): void {
    this.personaSelectionService.personaSeleccionada$.subscribe(persona => {
      this.personaSeleccionada = persona;
      this.verificarRequisitos();
    });
  }

  /**
   * Selecciona automáticamente una persona por defecto si el usuario no tiene una activa.
   */
  private cargarPersonaPorDefectoSiNecesario(): void {
    if (this.personaSeleccionada || !this.currentUserId) {
      return;
    }
    this.personaSimuladaService.getPersonasSimuladasByUsuario(this.currentUserId).subscribe({
      next: personas => {
        this.canSwitchDocente = (personas?.length || 0) > 1;
        if (personas && personas.length > 0) {
          const personaPorDefecto = personas.reduce((min, persona) =>
            (persona.idPersona || 0) < (min.idPersona || 0) ? persona : min
          );
          this.personaSelectionService.setPersonaSeleccionada(personaPorDefecto);
        } else {
          this.bloquear('Debe introducir los datos de la persona simulada para realizar la simulación, crear el puesto o puestos tipos y los contratos previstos para el ejercicio');
        }
      },
      error: () => {
        this.canSwitchDocente = false;
        this.bloquear('Debe introducir los datos de la persona simulada para realizar la simulación, crear el puesto o puestos tipos y los contratos previstos para el ejercicio');
      }
    });
  }

  /**
   * Determina si el usuario puede alternar entre distintas personas simuladas.
   */
  private actualizarDisponibilidadCambioDocente(): void {
    if (!this.currentUserId) {
      this.canSwitchDocente = false;
      return;
    }

    this.personaSimuladaService.getPersonasSimuladasByUsuario(this.currentUserId).subscribe({
      next: personas => {
        this.canSwitchDocente = (personas?.length || 0) > 1;
      },
      error: () => {
        this.canSwitchDocente = false;
      }
    });
  }

  /**
   * Comprueba que la persona seleccionada tenga datos suficientes para ejecutar una simulación.
   */
  private verificarRequisitos(): void {
    if (!this.personaSeleccionada?.idPersona) {
      this.bloquear('Debe introducir los datos de la persona simulada para realizar la simulación, crear el puesto o puestos tipos y los contratos previstos para el ejercicio');
      return;
    }
    forkJoin({
      puestos: this.puestoTipoService.getPuestosByPersona(this.personaSeleccionada.idPersona),
      contratos: this.contratoPersonaService.getContratosByPersona(this.personaSeleccionada.idPersona)
    }).subscribe({
      next: ({ puestos, contratos }) => {
        if (!puestos || puestos.length === 0) {
          this.bloquear('Debe crear el puesto o puestos tipos y los contratos previstos para el ejercicio');
          return;
        }
        if (!contratos || contratos.length === 0) {
          this.bloquear('Debe crear los contratos previstos para el ejercicio');
          return;
        }
        this.desbloquear();
      },
      error: () => {
        this.bloquear('No se pudo verificar la información necesaria');
      }
    });
  }

  /**
   * Bloquea la simulación mostrando el motivo al usuario.
   * @param mensaje Texto explicativo de la condición que impide continuar.
   */
  private bloquear(mensaje: string): void {
    this.bloqueado = true;
    this.mensajeBloqueo = mensaje;
  }

  private desbloquear(): void {
    this.bloqueado = false;
    this.mensajeBloqueo = '';
  }

  /**
   * Reacciona al cambio de ejercicio actualizando meses y reseteando el resultado vigente.
   */
  onCambioEjercicio(): void {
    this.actualizarMesesOpciones();
    this.mesHastaAbonado = null;
    this.impBrutoAbonado = null;
    this.impRetencionesPracticadas = null;
    this.impGastosRealizados = null;
    this.resultadoMeses = [];
    this.totalBrutoPendiente = 0;
    this.totalGastosPendiente = 0;
    this.porcIrpf = null;
    this.importeBrutoAnual = null;
    this.importeRetencionesAnual = null;
    this.intentoSimulacion = false;
  }

  get hayMesSeleccionado(): boolean {
    return !!this.mesHastaAbonado;
  }

  get impBrutoAbonadoEsValido(): boolean {
    if (!this.hayMesSeleccionado) {
      return true;
    }
    const valor = this.toNumberOrNull(this.impBrutoAbonado);
    return valor !== null && valor > 0;
  }

  /**
   * Recalcula el estado derivado cuando cambia el último mes abonado seleccionado.
   */
  onCambioMesHasta(): void {
    if (!this.hayMesSeleccionado) {
      this.impBrutoAbonado = null;
      this.impRetencionesPracticadas = null;
      this.impGastosRealizados = null;
    }
    this.resultadoMeses = [];
    this.totalBrutoPendiente = 0;
    this.totalGastosPendiente = 0;
    this.porcIrpf = null;
    this.importeBrutoAnual = null;
    this.importeRetencionesAnual = null;
    this.intentoSimulacion = false;
  }

  /**
   * Valida el formulario y solicita al backend el cálculo de la simulación.
   */
  ejecutarSimulacion(): void {
    if (this.bloqueado || !this.personaSeleccionada?.idPersona) {
      return;
    }

    this.intentoSimulacion = true;

    if (this.hayMesSeleccionado && !this.impBrutoAbonadoEsValido) {
      this.mostrarMensajeError('Si se ha cobrado alguna nómina en el ejercicio, el importe bruto abonado debe informarse y ser mayor que cero.');
      return;
    }

    this.cargando = true;
    this.resultadoMeses = [];
    this.porcIrpf = null;
    this.importeBrutoAnual = null;
    this.importeRetencionesAnual = null;

    const payload = {
      idPersona: this.personaSeleccionada.idPersona,
      ejercicio: this.ejercicioSeleccionado,
      impBrutoAbonado: this.hayMesSeleccionado ? this.toNumberOrNull(this.impBrutoAbonado) : null,
      impRetencionesPracticadas: this.hayMesSeleccionado ? this.toNumberOrNull(this.impRetencionesPracticadas) : null,
      impGastosRealizados: this.hayMesSeleccionado ? this.toNumberOrNull(this.impGastosRealizados) : null,
      fechaHastaAbonado: this.mesHastaAbonado
    };

    this.simulacionService.ejecutarSimulacion(payload).subscribe({
      next: resp => {
        if (resp.success && resp.data) {
          this.resultadoMeses = resp.data.meses || [];
          this.totalBrutoPendiente = Number(resp.data.totalBrutoPendiente ?? 0);
          this.totalGastosPendiente = Number(resp.data.totalGastosPendiente ?? 0);
          this.porcIrpf = Number(resp.data.porcIrpf ?? 0);
          this.importeBrutoAnual = resp.data.importeBrutoAnual !== null ? Number(resp.data.importeBrutoAnual) : null;
          this.importeRetencionesAnual = resp.data.importeRetencionesAnual !== null ? Number(resp.data.importeRetencionesAnual) : null;
          this.mostrarMensajeExito('Simulación ejecutada correctamente');
        } else {
          this.mostrarMensajeError(resp.message || 'No se pudo ejecutar la simulación');
        }
        this.cargando = false;
      },
      error: (err) => {
        const msg = err.error?.message || err.message || 'No se pudo ejecutar la simulación';
        this.mostrarMensajeError(msg);
        this.cargando = false;
      }
    });
  }

  /**
   * Navega a una ruta interna de la aplicación.
   * @param route Ruta destino dentro del frontend.
   */
  navigateTo(route: string) {
    if (route === '/login') {
      this.authService.clearUsername();
      localStorage.removeItem('token');
      localStorage.removeItem('personaSimuladaSeleccionada');
    }
    this.router.navigate([route]);
  }

  /**
   * Abre el selector de docentes cuando el cambio de persona está habilitado.
   */
  navigateToDocenteSelector(): void {
    if (!this.canSwitchDocente) {
      return;
    }
    this.router.navigate(['/seleccion-docente']);
  }

  /**
   * Muestra un mensaje temporal de confirmación.
   * @param mensaje Texto de éxito que se desea presentar.
   */
  mostrarMensajeExito(mensaje: string): void {
    this.mensaje = mensaje;
    this.tipoMensaje = 'success';
    this.mostrarMensaje = true;
    setTimeout(() => (this.mostrarMensaje = false), 4000);
  }

  /**
   * Muestra un mensaje temporal de error.
   * @param mensaje Texto descriptivo del problema detectado.
   */
  mostrarMensajeError(mensaje: string): void {
    this.mensaje = mensaje;
    this.tipoMensaje = 'error';
    this.mostrarMensaje = true;
    setTimeout(() => (this.mostrarMensaje = false), 5000);
  }

  cerrarMensaje(): void {
    this.mostrarMensaje = false;
  }

  get ayudaActiva(): AyudaContextual {
    return this.ayudas[this.ayudaActivaKey] || this.ayudas['general'];
  }

  /**
   * Activa una ayuda contextual concreta dentro de la pantalla.
   * @param key Clave de la ayuda que debe resaltarse.
   */
  setAyudaActiva(key: string): void {
    this.ayudaActivaKey = this.ayudas[key] ? key : 'general';
  }

  restablecerAyudaGeneral(): void {
    this.ayudaActivaKey = 'general';
  }

  get mostrarErrorImpBrutoAbonado(): boolean {
    return this.hayMesSeleccionado && this.intentoSimulacion && !this.impBrutoAbonadoEsValido;
  }

  /**
   * Normaliza valores numéricos opcionales conservando `null` cuando no hay dato.
   * @param value Valor numérico potencialmente vacío.
   * @returns Número válido o `null` si el dato no existe.
   */
  private toNumberOrNull(value: number | null | undefined): number | null {
    if (value === null || value === undefined || value === '' as any) return null;
    const num = Number(value);
    return isNaN(num) ? null : num;
  }

  /**
   * Convierte un texto técnico a una etiqueta legible para la interfaz.
   * @param value Texto original a formatear.
   * @returns Cadena normalizada con capitalización por palabra.
   */
  formatCamelCase(value: string): string {
    return value
      .toLowerCase()
      .replace(/[_-]/g, ' ')
      .replace(/\b\w/g, char => char.toUpperCase());
  }

}
