import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { AuthService } from '../auth.service';
import { Comunidad, PersonaSimulada, PersonaSimuladaService } from '../services/persona-simulada.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';
import { PuestoTipo, PuestoTipoService } from '../services/puesto-tipo.service';
import { ContratoPersona, ContratoPersonaService, ApiResponse as ContratoApiResponse } from '../services/contrato-persona.service';
import { ConfirmDialogService } from '../services/confirm-dialog.service';
import { finalize } from 'rxjs/operators';

type Vista = 'lista' | 'form';

interface AyudaContextual {
  titulo: string;
  descripcion: string;
  puntos?: string[];
}

@Component({
  selector: 'app-contratos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './contratos.component.html',
  styleUrls: []
})
/**
 * Componente encargado de gestionar el ciclo de vida de los contratos
 * asociados a una persona simulada y de contextualizar su edición.
 */
export class ContratosComponent implements OnInit {
  currentUsername: string | null = null;
  currentUserId: number | null = null;
	usernameFormatted: string | null = null;
  menuItems: any[] = [];
  isAdmin = false;

  personaSeleccionada: PersonaSimulada | null = null;
  canSwitchDocente = false;
  puestos: PuestoTipo[] = [];
  contratos: ContratoPersona[] = [];
  comunidades: Comunidad[] = [];

  vista: Vista = 'lista';
  modoAlta = true;
  contratoEditandoId: number | null = null;

  formulario = this.obtenerFormularioVacio();

  mensaje = '';
  tipoMensaje: 'success' | 'error' = 'success';
  mostrarMensaje = false;
  cargando = false;

  readonly mensajeSinPersona = 'Para incluir un nuevo contrato, primero debe crear una persona simulada y puestos tipo para esa persona';
  ayudaActivaKey = 'general';
  readonly ayudas: Record<string, AyudaContextual> = {
    general: {
      titulo: 'Ayuda de la pantalla',
      descripcion: 'En esta pantalla se definen los periodos de contrato del docente y el puesto tipo aplicado en el que se encuentra en cada contrato',
      puntos: [
        'Crea un contrato ya sea por cambio de contrato o por variación de puesto tipo (Ej: cambio de comunidad o cumplimiento de trienio)',
        'La fecha de inicio es obligatoria y la fecha fin es opcional.',
        'Si no indicas fecha fin, el contrato se considera vigente.',
        'Selecciona el check de vacaciones no disfrutadas si el contrato finaliza antes del 31 de agosto, ya que se deberían abonar',
        'Pulsa modificar para acceder a la pantalla de modificación del montrato seleccionado'

      ]
    },
    puestoTipo: {
      titulo: 'Puesto tipo',
      descripcion: 'Selecciona el puesto que corresponde al contrato en el periodo indicado.'
    },
    fechaDesde: {
      titulo: 'Fecha inicio del contrato',
      descripcion: 'Marca el día en el que se inicia el contrato.'
    },
    fechaHasta: {
      titulo: 'Fecha fin de contrato',
      descripcion: 'Fecha fin de contrato. Si la informas, debe ser posterior a la fecha de inicio.'
    },
    vacacionesNoDisfrutadas: {
      titulo: 'Vacaciones no disfrutadas',
      descripcion: 'Activa esta opción si ala fecha fin de contrato no es el 31 de agosto. En ese caso, se deberá calcular el importe de vacaciones no disfrutadas.'
    }
  };
  readonly mensajeSinPuestos = 'Para incluir un nuevo contrato, primero debe al menos un puestos tipo para esa persona que indique las características del puesto que se ocupa durante el contrato.';

  constructor(
    private authService: AuthService,
    private router: Router,
    private personaSimuladaService: PersonaSimuladaService,
    private personaSelectionService: PersonaSimuladaSelectionService,
    private puestoTipoService: PuestoTipoService,
    private contratoPersonaService: ContratoPersonaService,
    private confirmDialogService: ConfirmDialogService
  ) {}

  /**
   * Inicializa la gestión de contratos cargando persona activa, menús y datos asociados.
   */
  ngOnInit(): void {
    this.currentUsername = this.authService.getUsername();
    this.currentUserId = this.authService.getUserId();
    this.usernameFormatted = this.formatCamelCase(this.currentUsername || '');  
    this.updateMenuItems();
    this.actualizarDisponibilidadCambioDocente();
    this.cargarComunidades();
    this.suscribirseAPersonaSeleccionada();
    this.cargarPersonaPorDefectoSiNecesario();
  }

  /**
   * Recalcula las opciones de menú visibles según el rol del usuario autenticado.
   */
  updateMenuItems(): void { 
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
   * Sincroniza el componente con la persona simulada seleccionada globalmente.
   */
  private suscribirseAPersonaSeleccionada(): void {
    this.personaSelectionService.personaSeleccionada$.subscribe(persona => {
      this.personaSeleccionada = persona;
      if (persona?.idPersona) {
        this.vista = 'lista';
        this.contratoEditandoId = null;
        this.formulario = this.obtenerFormularioVacio();
        this.cargarDatosPersona();
      } else {
        this.contratos = [];
        this.puestos = [];
      }
    });
  }

  /**
   * Selecciona una persona por defecto cuando no existe una activa al entrar en la pantalla.
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
        }
      },
      error: () => {
        this.canSwitchDocente = false;
        this.mostrarMensajeError('No se pudo cargar la persona simulada por defecto');
      }
    });
  }

  /**
   * Determina si el usuario puede cambiar entre varias personas simuladas.
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
   * Recupera puestos y contratos de la persona activa para construir la vista.
   */
  private cargarDatosPersona(): void {
    if (!this.personaSeleccionada?.idPersona) return;
    this.cargando = true;
    forkJoin({
      puestos: this.puestoTipoService.getPuestosByPersona(this.personaSeleccionada.idPersona),
      contratos: this.contratoPersonaService.getContratosByPersona(this.personaSeleccionada.idPersona)
    }).subscribe({
      next: ({ puestos, contratos }) => {
        this.puestos = puestos || [];
        this.contratos = contratos || [];
        this.cargando = false;
      },
      error: () => {
        this.mostrarMensajeError('No se pudieron cargar los contratos');
        this.cargando = false;
      }
    });
  }

  /**
   * Prepara el formulario para crear un nuevo contrato.
   */
  abrirCrear(): void {
    if (!this.personaSeleccionada?.idPersona) {
      this.mostrarMensajeError(this.mensajeSinPersona);
      return;
    }
    if (!this.tienePuestos()) {
      this.mostrarMensajeError(this.mensajeSinPuestos);
      return;
    }
    this.vista = 'form';
    this.modoAlta = true;
    this.contratoEditandoId = null;
    const primerPuesto = this.puestos[0]?.idPuestoTipo ?? null;
    const hoy = new Date().toISOString().slice(0, 10);
    this.formulario = {
      idPuestoTipo: primerPuesto,
      fechaDesde: hoy,
      fechaHasta: '',
      indVacNoDisfrutadas: false
    };
  }

  /**
   * Carga en el formulario los datos del contrato seleccionado para su edición.
   * @param contrato Contrato que se desea modificar.
   */
  abrirEdicion(contrato: ContratoPersona): void {
    this.vista = 'form';
    this.modoAlta = false;
    this.contratoEditandoId = contrato.idContratoPersona ?? null;
    this.formulario = {
      idPuestoTipo: contrato.idPuestoTipo,
      fechaDesde: contrato.fechaDesde,
      fechaHasta: contrato.fechaHasta || '',
      indVacNoDisfrutadas: (contrato.indVacNoDisfrutadas || '').toUpperCase() === 'S'
    };
  }

  /**
   * Valida y persiste el alta o la edición del contrato actualmente cargado en el formulario.
   */
  guardar(): void {
    if (!this.personaSeleccionada?.idPersona) {
      this.mostrarMensajeError(this.mensajeSinPersona);
      return;
    }
    if (!this.formulario.idPuestoTipo) {
      this.mostrarMensajeError('Seleccione el puesto tipo del contrato');
      return;
    }
    if (!this.formulario.fechaDesde) {
      this.mostrarMensajeError('La fecha desde es obligatoria');
      return;
    }
    if (this.formulario.fechaHasta && this.formulario.fechaHasta <= this.formulario.fechaDesde) {
      this.mostrarMensajeError('La fecha hasta debe ser posterior a la fecha desde');
      return;
    }

    const payload: Partial<ContratoPersona> = {
      idPersona: this.personaSeleccionada.idPersona,
      idPuestoTipo: Number(this.formulario.idPuestoTipo),
      fechaDesde: this.formulario.fechaDesde,
      fechaHasta: this.formulario.fechaHasta || null,
      indVacNoDisfrutadas: this.formulario.indVacNoDisfrutadas ? 'S' : 'N'
    };

 this.cargando = true;

  const req$ = this.modoAlta
    ? this.contratoPersonaService.createContrato(payload)
    : this.contratoEditandoId
      ? this.contratoPersonaService.updateContrato(this.contratoEditandoId, payload)
      : null;

  if (!req$) return;

  req$
    .pipe(finalize(() => (this.cargando = false)))
    .subscribe({
      next: (resp) => {
        if (resp.success) {
          this.mostrarMensajeExito(resp.message);
          this.volverALista(true);
        } else {
          this.mostrarMensajeError(resp.message);
          // aquí ya no te quedas pillado: finalize apaga el spinner
        }
      },
      error: (err) => {
        const mensaje = err.error?.message || err.error || err.message || 'Error al guardar el contrato';
        this.mostrarMensajeError(mensaje);
      }
    });    
  }

  /**
   * Elimina el contrato seleccionado actualmente en la vista.
   */
  async eliminar(): Promise<void> {
    if (!this.contratoEditandoId) return;
    const confirmar = await this.confirmDialogService.confirm('¿Seguro que desea eliminar este contrato?');
    if (!confirmar) return;
    this.cargando = true;
    this.contratoPersonaService.deleteContrato(this.contratoEditandoId)
      .pipe(finalize(() => (this.cargando = false))).subscribe({
      next: (resp: ContratoApiResponse) => {
        if (resp.success) {
          this.mostrarMensajeExito(resp.message);
          this.volverALista(true);
        } else {
          this.mostrarMensajeError(resp.message);
        }
      },
      error: (err) => {
        const mensaje = err.error?.message || err.message || 'Error al eliminar el contrato';
        this.mostrarMensajeError(mensaje);
      },
      complete: () => this.cargando = false
    });
  }

  /**
   * Restablece la vista de listado y opcionalmente recarga los datos desde backend.
   * @param recargar Indica si deben volver a consultarse los datos de la persona.
   */
  volverALista(recargar: boolean = false): void {
    this.vista = 'lista';
    this.modoAlta = true;
    this.contratoEditandoId = null;
    this.formulario = this.obtenerFormularioVacio();
    if (recargar) {
      this.cargarDatosPersona();
    }
  }

  /**
   * Convierte una fecha ISO a un formato legible por el usuario.
   * @param fecha Fecha en formato texto o valor vacío.
   * @returns Fecha formateada o cadena vacía si no hay valor.
   */
  formatearFecha(fecha?: string | null): string {
    if (!fecha) return '';
    const [anio, mes, dia] = fecha.split('-');
    if (!anio || !mes || !dia) return fecha;
    return `${dia}/${mes}/${anio}`;
  }

  /**
   * Obtiene el nombre del puesto asociado a un identificador.
   * @param idPuestoTipo Identificador del puesto a localizar.
   * @returns Nombre del puesto o cadena vacía si no existe coincidencia.
   */
  getNombrePuesto(idPuestoTipo: number | undefined): string {
    if (!idPuestoTipo) return '';
    const puesto = this.puestos.find(p => p.idPuestoTipo === idPuestoTipo);
    return puesto?.nomPuesto || '';
  }

  getComunidadPuesto(idPuestoTipo: number | null | undefined): string {
    if (!idPuestoTipo) return '';
    const puesto = this.puestos.find(p => p.idPuestoTipo === idPuestoTipo);
    if (!puesto?.idComunidad) return '';
    const comunidad = this.comunidades.find(c => c.idComunidad === puesto.idComunidad);
    return comunidad?.descComunidad || '';
  }

  private cargarComunidades(): void {
    this.personaSimuladaService.getComunidades().subscribe({
      next: comunidades => {
        this.comunidades = comunidades || [];
      },
      error: () => {
        this.comunidades = [];
      }
    });
  }

  tienePuestos(): boolean {
    return this.puestos && this.puestos.length > 0;
  }

  /**
   * Muestra un mensaje temporal de confirmación en la pantalla.
   * @param mensaje Texto de éxito que se desea mostrar.
   */
  mostrarMensajeExito(mensaje: string): void {
    this.mensaje = mensaje;
    this.tipoMensaje = 'success';
    this.mostrarMensaje = true;
    setTimeout(() => this.mostrarMensaje = false, 4000);
  }

  /**
   * Muestra un mensaje temporal de error en la pantalla.
   * @param mensaje Texto descriptivo del problema detectado.
   */
  mostrarMensajeError(mensaje: string): void {
    this.mensaje = mensaje;
    this.tipoMensaje = 'error';
    this.mostrarMensaje = true;
    setTimeout(() => this.mostrarMensaje = false, 5000);
  }

  cerrarMensaje(): void {
    this.mostrarMensaje = false;
  }

  get ayudaActiva(): AyudaContextual {
    return this.ayudas[this.ayudaActivaKey] || this.ayudas['general'];
  }

  setAyudaActiva(key: string): void {
    this.ayudaActivaKey = this.ayudas[key] ? key : 'general';
  }

  restablecerAyudaGeneral(): void {
    this.ayudaActivaKey = 'general';
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
   * Abre el selector de docentes cuando la funcionalidad está habilitada.
   */
  navigateToDocenteSelector(): void {
    if (!this.canSwitchDocente) {
      return;
    }
    this.router.navigate(['/seleccion-docente']);
  }

  /**
   * Construye el estado inicial vacío del formulario de contratos.
   * @returns Objeto base listo para un alta nueva.
   */
  private obtenerFormularioVacio() {
    return {
      idPuestoTipo: null as number | null,
      fechaDesde: '',
      fechaHasta: '',
      indVacNoDisfrutadas: false
    };
  }

  /**
   * Convierte un texto técnico a una etiqueta legible para la interfaz.
   * @param value Texto original a transformar.
   * @returns Cadena normalizada con capitalización por palabra.
   */
formatCamelCase(value: string): string {
  return value
    .toLowerCase()
    .replace(/[_-]/g, ' ')
    .replace(/\b\w/g, char => char.toUpperCase());
}

}

