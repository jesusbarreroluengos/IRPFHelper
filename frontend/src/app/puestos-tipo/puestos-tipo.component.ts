import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { Comunidad, PersonaSimulada, PersonaSimuladaService } from '../services/persona-simulada.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';
import { ApiResponse, Estudio, Jornada, PuestoTipo, PuestoTipoService } from '../services/puesto-tipo.service';

interface AyudaContextual {
  titulo: string;
  descripcion: string;
  puntos?: string[];
}

@Component({
  selector: 'app-puestos-tipo',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './puestos-tipo.component.html',
  styleUrls: [],
  styles: [`
    .importes-calculados-panel {
      background-color: #eaf4fb;
      border: 1px solid #b6d9f0;
      border-radius: 8px;
      padding: 16px 20px 8px 20px;
      margin-bottom: 16px;
    }
    .importes-calculados-titulo {
      font-size: 0.9rem;
      font-weight: 600;
      color: #2c6e9e;
      margin: 0 0 12px 0;
      letter-spacing: 0.01em;
    }
  `]
})
/**
 * Componente dedicado a la definición y mantenimiento de los puestos tipo que
 * sirven de base para la simulación retributiva y fiscal.
 */
export class PuestosTipoComponent implements OnInit {
  currentUsername: string | null = null;
  currentUserId: number | null = null;
	usernameFormatted: string | null = null;
  menuItems: any[] = [];
  isAdmin = false;

  personaSeleccionada: PersonaSimulada | null = null;
  canSwitchDocente = false;
  puestos: PuestoTipo[] = [];
  puestoSeleccionadoId: number | null = null;
  modoAlta = true;

  estudios: Estudio[] = [
    { codEstudio: 'P', descEstudio: 'Primaria' },
    { codEstudio: 'S', descEstudio: 'Secundaria' },
    { codEstudio: 'C', descEstudio: 'Catedráticos' },
    { codEstudio: 'F', descEstudio: 'Formación Profesional' }
  ];
  jornadas: Jornada[] = [];
  comunidades: Comunidad[] = [];

  trienios = Array.from({ length: 16 }, (_, i) => i);
  sexenios = Array.from({ length: 6 }, (_, i) => i);

  formulario: Partial<PuestoTipo> = this.obtenerFormularioVacio();
  importeEspecificoTabla: number | null = null;
  importesDetalle: {
    importeBrutoMes: number;
    importeExtra: number;
    importeBaseCotizacion: number;
    importeSueldo: number;
    importeComplementoDestino: number;
    importeTrienios: number;
    importeSexenios: number;
    importeSueldoExtra: number;
    importeTrieniosExtra: number;
  } = {
    importeBrutoMes: 0,
    importeExtra: 0,
    importeBaseCotizacion: 0,
    importeSueldo: 0,
    importeComplementoDestino: 0,
    importeTrienios: 0,
    importeSexenios: 0,
    importeSueldoExtra: 0,
    importeTrieniosExtra: 0
  };

  cargando = false;
  mensaje = '';
  tipoMensaje: 'success' | 'error' = 'success';
  mostrarMensaje = false;

  ayudaActivaKey = 'general';
  readonly ayudas: Record<string, AyudaContextual> = {
    general: {
      titulo: 'Ayuda de la pantalla',
      descripcion: 'En esta pantalla se definen los puestos docentes del docente seleccionado, presentando de forma informativa el bruto mensual, la paga extra y la base de cotización.',
      puntos: [
        'Puedes crear varios puestos tipo para un mismo docente si se imparte docencia en varias comunidades o si los datos varian a lo largo del ejercicio',
        'Los importes brutos, extra y base de cotización se recalculan con los cambios realizados',
        'Guarda los cambios antes de pasar a la pantalla de contratos.'
      ]
    },
    
    puestoSeleccionado: {
      titulo: 'Puesto tipo',
      descripcion: 'Selecciona un puesto ya creado para modificarlo o revisarlo.'
    },
    idComunidad: {
      titulo: 'Comunidad Autónoma',
      descripcion: 'Selecciona la comunidad autónoma donde se imparte la docencia.'
    },
    nomPuesto: {
      titulo: 'Nombre del puesto',
      descripcion: 'Es un nombre que te servirá para identificar el puesto. Usa un nombre corto y reconocible.'
    },
    codEstudio: {
      titulo: 'Tipo de docencia',
      descripcion: 'Define si el puesto corresponde a un puesto de docente en Primaria, Secundaria, Formación Profesional o Catedrático.'
    },
    codJornada: {
      titulo: 'Jornada',
      descripcion: 'Indica la jornada que realizarás en el puesto y los importes se reducirán si la jornada no es completa'
    },
    numTrieniosA1: {
      titulo: 'Trienios A1',
      descripcion: 'Indica los trienios acumulados en A1 para puestos de secundaria. ',
      puntos: [
        'Si tienes trienios de secundaria (grupo A1) aunque estés en un puesto de primaria (A2) deberán ser informados para que entren en el cálculo',
        'Si cumples un trienio a mitad de ejercicio deberás crear dos puestos, uno sin el último trienio y otro con el, indicando creando dos contratos con los dos periodos'
      ]
	  
    },
    numTrieniosA2: {
      titulo: 'Trienios A2',
      descripcion: 'Indica los trienios acumulados en A2 para puestos de primaria.',
      puntos: [
        'Si tienes trienios de secundaria (grupo A1) aunque estés en un puesto de primaria (A2) deberán ser informados para que entren en el cálculo',
        'Si cumples un trienio a mitad de ejercicio deberás crear dos puestos, uno sin el último trienio y otro con el, indicando creando dos contratos con los dos periodos'
      ]
    },
    numSexenios: {
      titulo: 'Sexenios',
      descripcion: 'Número de sexenios reconocidos en el puesto.',
      puntos: [
        'Si cumples un sexenio a mitad de ejercicio deberás crear dos puestos, uno sin el último sexenio y otro con el, indicando creando dos contratos con los dos periodos'
      ]
    },
    importeEspecDocente: {
      titulo: 'Importe complementos Específicos',
      descripcion: 'Si lo dejas en blanco o a cero, se toma el valor del complemento general docente asociado al tipo de docencia en la Comunidad Autónoma.',
      puntos: [
        'Si a parte del complemento específico general docente tienes otros complementos, súmalos y pon el importe mensual total que cobras en ellos',
        'En la suma deberán incluirse los complementos que se cobran en la paga extra',
        'Los complementos que se cobran mensualmente pero no en la paga se inclirán en el campo de Otros Importes Mensuales'
      ]
    },
    importeOtrosAbonosMes: {
      titulo: 'Otros importes mensuales',
      descripcion: 'Incluye aquí cualquier otro abono mensual que se cobre todos los meses pero que no forme parte de la paga extra, como importes de productividad.'
    }
  };
  constructor(
    private authService: AuthService,
    private router: Router,
    private personaSimuladaService: PersonaSimuladaService,
    private personaSelectionService: PersonaSimuladaSelectionService,
    private puestoTipoService: PuestoTipoService
  ) {}

  /**
   * Inicializa la gestión de puestos cargando catálogos, persona activa y datos previos.
   */
  ngOnInit(): void {
    this.currentUsername = this.authService.getUsername();
    this.currentUserId = this.authService.getUserId();
    this.usernameFormatted = this.formatCamelCase(this.currentUsername || '');  
    this.updateMenuItems();
    this.cargarEstudios();
    this.cargarJornadas();
    this.cargarComunidades();
    this.actualizarDisponibilidadCambioDocente();
    this.suscribirseAPersonaSeleccionada();
    this.cargarPersonaPorDefectoSiNecesario();
    this.cargarEspecificoTabla(this.formulario.codEstudio || 'P');
  }

  /**
   * Sincroniza el componente con la persona simulada seleccionada y su comunidad asociada.
   */
  private suscribirseAPersonaSeleccionada(): void {
    this.personaSelectionService.personaSeleccionada$.subscribe(persona => {
      if (persona?.idPersona && persona.idComunidad == null) {
        this.personaSimuladaService.getPersonaSimuladaById(persona.idPersona).subscribe({
          next: personaActualizada => this.personaSelectionService.setPersonaSeleccionada(personaActualizada),
          error: () => {
            this.personaSeleccionada = persona;
            this.cargarPuestos();
          }
        });
        return;
      }
      this.personaSeleccionada = persona;
      if (persona) {
        this.cargarPuestos();
      } else {
        this.puestos = [];
      }
    });
  }

  /**
   * Selecciona una persona por defecto cuando la pantalla se abre sin una activa.
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
   * Determina si el usuario puede alternar entre varias personas simuladas.
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
   * Recupera el catálogo de estudios disponible para el formulario.
   */
  private cargarEstudios(): void {
    this.puestoTipoService.getEstudios().subscribe({
      next: estudios => {
        if (estudios && estudios.length > 0) {
          this.estudios = estudios;
        }
      },
      error: () => {
        // Si falla dejamos los valores por defecto
      }
    });
  }

  /**
   * Recupera el catálogo de jornadas disponible para el formulario.
   */
  private cargarJornadas(): void {
    this.puestoTipoService.getJornadas().subscribe({
      next: jornadas => {
        if (jornadas && jornadas.length > 0) {
          this.jornadas = jornadas;
        }
      },
      error: () => {
        // Si falla dejamos la jornada por defecto
      }
    });
  }

  /**
   * Recupera y ordena las comunidades autónomas utilizables en el formulario.
   */
  private cargarComunidades(): void {
    this.personaSimuladaService.getComunidades().subscribe({
      next: comunidades => {
        this.comunidades = (comunidades || []).slice().sort((a, b) => a.descComunidad.localeCompare(b.descComunidad));
        if (this.modoAlta) {
          this.formulario.idComunidad = this.obtenerIdComunidadPorDefecto();
        }
      },
      error: () => {
        this.comunidades = [];
      }
    });
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
   * Recupera los puestos existentes para la persona activa y actualiza la selección de la vista.
   */
  cargarPuestos(): void {
    if (!this.personaSeleccionada?.idPersona) {
      return;
    }
    this.cargando = true;
    this.puestoTipoService.getPuestosByPersona(this.personaSeleccionada.idPersona).subscribe({
      next: puestos => {
        this.puestos = puestos || [];
        if (this.puestos.length > 0) {
          const primero = this.puestos[0];
          this.puestoSeleccionadoId = primero.idPuestoTipo ?? null;
          if (this.puestoSeleccionadoId !== null) {
            this.seleccionarPuesto(this.puestoSeleccionadoId);
          }
        } else {
          this.nuevoPuesto();
        }
        this.cargando = false;
      },
      error: () => {
        this.mostrarMensajeError('No se pudieron cargar los puestos tipo');
        this.cargando = false;
      }
    });
  }

  /**
   * Carga en el formulario el puesto seleccionado desde la tabla.
   * @param idPuesto Identificador del puesto que se desea visualizar o editar.
   */
  seleccionarPuesto(idPuesto: number): void {
    const puesto = this.puestos.find(p => p.idPuestoTipo === idPuesto);
    if (!puesto) {
      return;
    }
    this.puestoSeleccionadoId = idPuesto;
    this.modoAlta = false;
    this.formulario = {
      ...this.obtenerFormularioVacio(),
      ...puesto,
      idComunidad: puesto.idComunidad ?? this.obtenerIdComunidadPorDefecto()
    };
    if (this.formulario.importeEspecDocente === 0) {
      this.formulario.importeEspecDocente = null;
    }
    this.cargarEspecificoTabla(this.formulario.codEstudio || 'P');
    this.recalcularImportesDetalle();
  }

  /**
   * Prepara el formulario para dar de alta un nuevo puesto.
   */
  nuevoPuesto(): void {
    this.modoAlta = true;
    this.puestoSeleccionadoId = null;
    this.formulario = this.obtenerFormularioVacio();
    this.importesDetalle = {
      importeBrutoMes: 0, importeExtra: 0, importeBaseCotizacion: 0,
      importeSueldo: 0, importeComplementoDestino: 0, importeTrienios: 0,
      importeSexenios: 0, importeSueldoExtra: 0, importeTrieniosExtra: 0
    };
    this.cargarEspecificoTabla(this.formulario.codEstudio || 'P');
  }

  /**
   * Valida el formulario y persiste el alta o la edición del puesto actual.
   */
  guardar(): void {
    if (!this.personaSeleccionada?.idPersona) {
      this.mostrarMensajeError('Seleccione una persona simulada antes de guardar.');
      return;
    }
    if (!this.validarFormulario()) {
      return;
    }

    const payload: Partial<PuestoTipo> = {
      ...this.formulario,
      idPersona: this.personaSeleccionada.idPersona,
      nomPuesto: this.normalizarNombre(this.formulario.nomPuesto || ''),
      codEstudio: (this.formulario.codEstudio || 'P') as 'P' | 'S' | 'C' | 'F',
      codJornada: Number(this.formulario.codJornada ?? 1),
      numTrieniosA1: Number(this.formulario.numTrieniosA1 ?? 0),
      numTrieniosA2: Number(this.formulario.numTrieniosA2 ?? 0),
      numSexenios: Number(this.formulario.numSexenios ?? 0),
      importeEspecDocente: this.toNumberOrZero(this.formulario.importeEspecDocente),
      importeOtrosAbonosMes: this.toNumberOrZero(this.formulario.importeOtrosAbonosMes),
      idComunidad: Number(this.formulario.idComunidad ?? this.obtenerIdComunidadPorDefecto())
    };

    this.cargando = true;
    if (this.modoAlta) {
      this.puestoTipoService.createPuesto(payload).subscribe({
        next: (resp: ApiResponse) => {
          if (resp.success) {
            this.mostrarMensajeExito(resp.message);
            this.cargarPuestos();
          } else {
            this.mostrarMensajeError(resp.message);
            this.cargando = false;
          }
        },
        error: (err) => {
          const mensaje = err.error?.message || err.message || 'Error al crear el puesto';
          this.mostrarMensajeError(mensaje);
          this.cargando = false;
        }
      });
    } else if (this.puestoSeleccionadoId) {
      this.puestoTipoService.updatePuesto(this.puestoSeleccionadoId, payload).subscribe({
        next: (resp: ApiResponse) => {
          if (resp.success) {
            this.mostrarMensajeExito(resp.message);
            this.cargarPuestos();
          } else {
            this.mostrarMensajeError(resp.message);
            this.cargando = false;
          }
        },
        error: (err) => {
          const mensaje = err.error?.message || err.message || 'Error al actualizar el puesto';
          this.mostrarMensajeError(mensaje);
          this.cargando = false;
        }
      });
    }
  }

  /**
   * Elimina el puesto actualmente seleccionado en la vista.
   */
  eliminar(): void {
    if (!this.puestoSeleccionadoId) return;
    if (!confirm('¿Seguro que desea eliminar este puesto tipo?')) return;
    this.cargando = true;
    this.puestoTipoService.deletePuesto(this.puestoSeleccionadoId).subscribe({
      next: (resp: ApiResponse) => {
        if (resp.success) {
          this.mostrarMensajeExito(resp.message);
          this.cargarPuestos();
        } else {
          this.mostrarMensajeError(resp.message);
          this.cargando = false;
        }
      },
      error: (err) => {
        const mensaje = err.error?.message || err.message || 'Error al eliminar el puesto';
        this.mostrarMensajeError(mensaje);
        this.cargando = false;
      }
    });
  }

  /**
   * Solicita al backend el recálculo de los importes derivados del puesto actual.
   */
  recalcularImportesDetalle(): void {
    if (!this.personaSeleccionada?.idPersona) {
      this.importesDetalle = {
        importeBrutoMes: 0, importeExtra: 0, importeBaseCotizacion: 0,
        importeSueldo: 0, importeComplementoDestino: 0, importeTrienios: 0,
        importeSexenios: 0, importeSueldoExtra: 0, importeTrieniosExtra: 0
      };
      return;
    }
    const payload = this.construirPayloadCalculo();
    this.puestoTipoService.calcularImportesDetalle(payload).subscribe({
      next: (resp) => {
        if (resp.success && resp.data) {
          this.importesDetalle = {
            importeBrutoMes: Number(resp.data.importeBrutoMes ?? 0),
            importeExtra: Number(resp.data.importeExtra ?? 0),
            importeBaseCotizacion: Number(resp.data.importeBaseCotizacion ?? 0),
            importeSueldo: Number(resp.data.importeSueldo ?? 0),
            importeComplementoDestino: Number(resp.data.importeComplementoDestino ?? 0),
            importeTrienios: Number(resp.data.importeTrienios ?? 0),
            importeSexenios: Number(resp.data.importeSexenios ?? 0),
            importeSueldoExtra: Number(resp.data.importeSueldoExtra ?? 0),
            importeTrieniosExtra: Number(resp.data.importeTrieniosExtra ?? 0)
          };
        }
      },
      error: () => {
        this.importesDetalle = {
          importeBrutoMes: 0, importeExtra: 0, importeBaseCotizacion: 0,
          importeSueldo: 0, importeComplementoDestino: 0, importeTrienios: 0,
          importeSexenios: 0, importeSueldoExtra: 0, importeTrieniosExtra: 0
        };
      }
    });
  }

  /**
   * Reacciona al cambio de estudio actualizando importes y datos dependientes.
   */
  onCambioCodEstudio(): void {
    const cod = this.formulario.codEstudio;
    if (!cod) return;
    this.cargarEspecificoTabla(cod);
    this.recalcularImportesDetalle();
  }

  /**
   * Reacciona al cambio de comunidad recalculando el importe específico por defecto.
   */
  onCambioComunidad(): void {
    const cod = this.formulario.codEstudio;
    if (!cod) return;
    this.cargarEspecificoTabla(cod);
    this.recalcularImportesDetalle();
  }

  /**
   * Reacciona al cambio de jornada para recalcular importes cuando procede.
   */
  onCambioJornada(): void {
    if (!this.formulario.codJornada) {
      this.formulario.codJornada = 1;
    }
    this.recalcularImportesDetalle();
  }

  /**
   * Normaliza el nombre del puesto mientras se edita en el formulario.
   */
  onNombreChange(): void {
    this.formulario.nomPuesto = this.normalizarNombre(this.formulario.nomPuesto || '');
  }

  /**
   * Limpia y homogeneiza el nombre del puesto antes de guardarlo.
   * @param nombre Texto introducido por el usuario.
   * @returns Nombre normalizado según las reglas del formulario.
   */
  private normalizarNombre(nombre: string): string {
    const limpio = nombre.toUpperCase();
    return limpio.replace(/[^A-Z0-9ÁÉÍÓÚÜÑ\-_]/g, '');
  }

  /**
   * Construye el payload mínimo necesario para invocar los cálculos del backend.
   * @returns Objeto parcial de puesto con los datos relevantes para el cálculo.
   */
  private construirPayloadCalculo(): Partial<PuestoTipo> {
    return {
      ...this.formulario,
      idPersona: this.personaSeleccionada?.idPersona ?? 0,
      codJornada: Number(this.formulario.codJornada ?? 1),
      numTrieniosA1: Number(this.formulario.numTrieniosA1 ?? 0),
      numTrieniosA2: Number(this.formulario.numTrieniosA2 ?? 0),
      numSexenios: Number(this.formulario.numSexenios ?? 0),
      importeEspecDocente: this.toNumberOrZero(this.formulario.importeEspecDocente),
      importeOtrosAbonosMes: this.toNumberOrZero(this.formulario.importeOtrosAbonosMes),
      idComunidad: Number(this.formulario.idComunidad ?? this.obtenerIdComunidadPorDefecto())
    };
  }

  /**
   * Comprueba que todos los campos obligatorios del formulario tengan valores válidos.
   * @returns `true` si el formulario puede enviarse al backend.
   */
  private validarFormulario(): boolean {
    if (!this.formulario.nomPuesto || this.formulario.nomPuesto.trim() === '') {
      this.mostrarMensajeError('El nombre del puesto es obligatorio.');
      return false;
    }
    const nombre = this.normalizarNombre(this.formulario.nomPuesto);
    if (!/^[A-Z0-9ÁÉÍÓÚÜÑ\-_]+$/.test(nombre)) {
      this.mostrarMensajeError('El nombre solo admite letras, números, guion medio o guion bajo.');
      return false;
    }
    if (!this.formulario.codEstudio) {
      this.mostrarMensajeError('Seleccione el tipo de docencia.');
      return false;
    }
    if (!this.formulario.codJornada) {
      this.mostrarMensajeError('Seleccione una jornada.');
      return false;
    }
    const importeEspecifico = this.toNumberOrZero(this.formulario.importeEspecDocente);
    if (importeEspecifico < 0 || importeEspecifico > 25000) {
      this.mostrarMensajeError('El importe específico debe ser positivo y <= 25000.');
      return false;
    }
    if ((this.formulario.importeOtrosAbonosMes ?? 0) < 0 || (this.formulario.importeOtrosAbonosMes ?? 0) > 25000) {
      this.mostrarMensajeError('Otros importes mensuales debe ser positivo y <= 25000.');
      return false;
    }
    if (!this.formulario.idComunidad) {
      this.mostrarMensajeError('Seleccione una comunidad autonoma.');
      return false;
    }
    this.formulario.nomPuesto = nombre;
    return true;
  }

  /**
   * Genera el estado inicial vacío del formulario de puestos.
   * @returns Objeto base listo para un alta nueva.
   */
  private obtenerFormularioVacio(): Partial<PuestoTipo> {
    return {
      nomPuesto: '',
      codEstudio: 'P',
      codJornada: 1,
      numTrieniosA1: 0,
      numTrieniosA2: 0,
      numSexenios: 0,
      importeEspecDocente: null,
      importeOtrosAbonosMes: 0,
      idComunidad: this.obtenerIdComunidadPorDefecto()
    };
  }

  /**
   * Resuelve la comunidad por defecto a partir de la persona activa o del valor general configurado.
   * @returns Identificador de comunidad a usar al inicializar un nuevo formulario.
   */
  private obtenerIdComunidadPorDefecto(): number {
    return this.personaSeleccionada?.idComunidad
      ?? this.comunidades.find(c => c.idComunidad === 1)?.idComunidad
      ?? this.comunidades[0]?.idComunidad
      ?? 1;
  }

  /**
   * Recupera el importe específico sugerido para el estudio seleccionado.
   * @param codEstudio Código del estudio cuya tabla específica se va a consultar.
   */
  private cargarEspecificoTabla(codEstudio: string): void {
    this.puestoTipoService.getImporteEspecifico(
      codEstudio,
      Number(this.formulario.idComunidad ?? this.obtenerIdComunidadPorDefecto())
    ).subscribe({
      next: (resp) => {
        if (resp.success && resp.data !== undefined) {
          this.importeEspecificoTabla = Number(resp.data);
        } else {
          this.importeEspecificoTabla = null;
        }
      },
      error: () => {
        this.importeEspecificoTabla = null;
      }
    });
  }

  /**
   * Convierte un valor numérico opcional a un número seguro usando cero como respaldo.
   * @param value Valor potencialmente nulo o indefinido.
   * @returns Número normalizado listo para cálculos.
   */
  private toNumberOrZero(value: number | null | undefined): number {
    if (value === null || value === undefined || value === '' as any) {
      return 0;
    }
    const num = Number(value);
    return isNaN(num) ? 0 : num;
  }

  /**
   * Muestra un mensaje temporal de confirmación.
   * @param mensaje Texto de éxito que se desea presentar.
   */
  mostrarMensajeExito(mensaje: string): void {
    this.mensaje = mensaje;
    this.tipoMensaje = 'success';
    this.mostrarMensaje = true;
    this.cargando = false;
    setTimeout(() => this.mostrarMensaje = false, 4000);
  }

  /**
   * Muestra un mensaje temporal de error.
   * @param mensaje Texto descriptivo del problema detectado.
   */
  mostrarMensajeError(mensaje: string): void {
    this.mensaje = mensaje;
    this.tipoMensaje = 'error';
    this.mostrarMensaje = true;
    this.cargando = false;
    setTimeout(() => this.mostrarMensaje = false, 5000);
  }

  cerrarMensaje(): void {
    this.mostrarMensaje = false;
  }

  get ayudaActiva(): AyudaContextual {
    return this.ayudas[this.ayudaActivaKey] || this.ayudas['general'];
  }

  /**
   * Activa un bloque de ayuda contextual concreto.
   * @param key Clave de la ayuda que debe mostrarse como activa.
   */
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
   * Abre el selector de docentes cuando el cambio de persona activa está habilitado.
   */
  navigateToDocenteSelector(): void {
    if (!this.canSwitchDocente) {
      return;
    }
    this.router.navigate(['/seleccion-docente']);
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
