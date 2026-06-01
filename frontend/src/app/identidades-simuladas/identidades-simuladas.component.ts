import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { PersonaSimuladaService, PersonaSimulada, Discapacidad, SituacionFamiliar, Contrato, Comunidad, ApiResponse } from '../services/persona-simulada.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';
import { DescendientesModalComponent, Descendiente } from './descendientes-modal/descendientes-modal.component';
import { AscendientesModalComponent, Ascendiente } from './ascendientes-modal/ascendientes-modal.component';
import { ConfirmDialogService } from '../services/confirm-dialog.service';

interface AyudaContextual {
  titulo: string;
  descripcion: string;
  puntos?: string[];
}

@Component({
  selector: 'app-identidades-simuladas',
  standalone: true,
  imports: [CommonModule, FormsModule, DescendientesModalComponent, AscendientesModalComponent],
  templateUrl: './identidades-simuladas.component.html',
  styleUrls: []
})
/**
 * Componente que centraliza la captura y edición de los datos personales y
 * familiares asociados a cada docente simulado.
 */
export class IdentidadesSimuladasComponent implements OnInit {
  // Datos del usuario actual
  currentUserId: number | null = null;
  currentUsername: string | null = null;
  usernameFormatted: string | null = null;
  menuItems: any[] = [];
  isAdmin = false;

  // Listas de datos
  personasSimuladas: PersonaSimulada[] = [];
  discapacidades: Discapacidad[] = [];
  situacionesFamiliares: SituacionFamiliar[] = [];
  contratos: Contrato[] = [];
  comunidades: Comunidad[] = [];
  
  // Datos familiares
  descendientes: Descendiente[] = [];
  ascendientes: Ascendiente[] = [];
  
  // Modales
  mostrarModalDescendientes: boolean = false;
  mostrarModalAscendientes: boolean = false;

  // Persona simulada seleccionada
  personaSeleccionada: PersonaSimulada | null = null;
  personaSeleccionadaId: number | null = null;
  canSwitchDocente = false;

  // Formulario
  formularioPersona: Partial<PersonaSimulada> = {
    nombre: '',
    anioNac: undefined,
    codDiscapacidad: '',
    codSitfam: '',
    indCeumelilla: 'N',
    codContrato: '',
    impPensionConyuge: 0,
    impPensionHijos: 0,
    idComunidad: 1
  };

  // Estados del componente
  modoAlta: boolean = false;
  cargando: boolean = false;
  mensaje: string = '';
  tipoMensaje: 'success' | 'error' = 'success';
  mostrarMensaje: boolean = false;

  ayudaActivaKey = 'general';
  readonly ayudas: Record<string, AyudaContextual> = {
    general: {
      titulo: 'Ayuda de la pantalla',
      descripcion: 'En esta pantalla se definen los datos personales y familiares del docente simulado que influyen en el cálculo de IRPF.',
      puntos: [
        'Si tienes varios docentes, selecciona uno en el desplegable superior.',
        'Completa primero los campos obligatorios marcados con *.',
        'Usa los bloques de descendientes y ascendientes para completar la situación familiar.'
      ]
    },
    personaSeleccionada: {
      titulo: 'Docente simulado',
      descripcion: 'Selecciona el docente con el que quieres trabajar. Al cambiar, se cargan sus datos guardados.'
    },
    nombre: {
      titulo: 'Nombre',
      descripcion: 'Introduce un nombre claro para identificar al docente con el que estás trabajando en el resto de pantallas.'
    },
    anioNac: {
      titulo: 'Año de nacimiento',
      descripcion: 'Año de nacimiento de la persona.'
    },
    discapacidad: {
      titulo: 'Discapacidad',
      descripcion: 'Selecciona el grado de discapacidad '
    },
    situacionFamiliar: {
      titulo: 'Situación familiar',
      descripcion: 'Define la situación familiar del docente para aplicar las reglas de IRPF correspondientes.',
	  
      puntos: [
        'Soltero, viudo, divorciado o separado legalmente con hijos menores de 18 años',
        'Casado (con o sin hijos) y con cónyge que gana o prevé ganar menos de 1.500 euros/año',
        'No incluido en las anteriores'
      ]
	   
	  
    },
    residenciaCeutaMelilla: {
      titulo: 'Residencia en Ceuta o Melilla',
      descripcion: 'Marca esta casilla solo cuando se resida en Ceuta o Melilla durante el ejercicio fiscal'
    },
    contrato: {
      titulo: 'Contrato',
      descripcion: 'Elige el tipo de contrato de referencia asociado al docente.'
    },
    pensionConyuge: {
      titulo: 'Pensión cónyuge',
      descripcion: 'Importe anual fijado judicialmente por pensión compensatoria a favor del cónyuge.'
    },
    pensionHijos: {
      titulo: 'Pensión hijos',
      descripcion: 'Importe anual fijado judicialmente por pensión de alimentos a favor de hijos.'
    },    idComunidad: {
      titulo: 'Comunidad Autónoma',
      descripcion: 'Selecciona la comunidad autónoma donde se imparte la docencia.'
    }

  };

  // Años para el desplegable
  aniosNacimiento: number[] = [];

  constructor(
    private personaSimuladaService: PersonaSimuladaService,
    private authService: AuthService,
    private router: Router,
    private personaSelectionService: PersonaSimuladaSelectionService,
    private confirmDialogService: ConfirmDialogService
  ) {
    this.generarAniosNacimiento();
  }

  /**
   * Inicializa la gestión de identidades simuladas cargando catálogos, menús y datos del usuario.
   */
  ngOnInit(): void {
    this.currentUserId = this.authService.getUserId();
    this.currentUsername = this.authService.getUsername();
    this.usernameFormatted = this.formatCamelCase(this.currentUsername || '');  
    this.updateMenuItems();
    
    if (!this.currentUserId) {
      this.router.navigate(['/login']);
      return;
    }

    this.cargarDatos();
  }

  /**
   * Genera el rango de años de nacimiento disponible para el formulario.
   */
  generarAniosNacimiento(): void {
    const anioActual = new Date().getFullYear();
    const anioLimite = anioActual - 15;
    const anioMinimo = 1939;
    
    for (let anio = anioLimite; anio >= anioMinimo; anio--) {
      this.aniosNacimiento.push(anio);
    }
  }

  /**
   * Recupera catálogos y personas simuladas, y establece la selección inicial de la pantalla.
   * @param idPersonaPreferida Identificador opcional de la persona que se quiere priorizar.
   */
  cargarDatos(idPersonaPreferida?: number | null): void {
    this.cargando = true;
    
    // Cargar datos en paralelo
    Promise.all([
      this.personaSimuladaService.getPersonasSimuladasByUsuario(this.currentUserId!).toPromise(),
      this.personaSimuladaService.getDiscapacidades().toPromise(),
      this.personaSimuladaService.getSituacionesFamiliares().toPromise(),
      this.personaSimuladaService.getContratos().toPromise(),
      this.personaSimuladaService.getComunidades().toPromise()
    ]).then(([personas, discapacidades, situacionesFamiliares, contratos, comunidades]) => {
      this.personasSimuladas = personas || [];
      this.canSwitchDocente = this.personasSimuladas.length > 1;
      this.discapacidades = discapacidades || [];
      this.situacionesFamiliares = situacionesFamiliares || [];
      this.contratos = contratos || [];
      this.comunidades = (comunidades || []).sort((a, b) => a.descComunidad.localeCompare(b.descComunidad));

      // Ordenar personas alfabéticamente por nombre
      this.personasSimuladas.sort((a, b) => a.nombre.localeCompare(b.nombre));

      // Seleccionar persona por defecto o desde localStorage
      if (this.personasSimuladas.length > 0) {
        // Intentar cargar desde localStorage primero
        const personaGuardada = localStorage.getItem('personaSimuladaSeleccionada');
        let personaSeleccionada: PersonaSimulada | null = null;

        if (idPersonaPreferida) {
          personaSeleccionada = this.personasSimuladas.find(p => p.idPersona === idPersonaPreferida) || null;
        }

        if (!personaSeleccionada && personaGuardada) {
          try {
            const personaData = JSON.parse(personaGuardada);
            personaSeleccionada = this.personasSimuladas.find(p => p.idPersona === personaData.idPersona) || null;
          } catch (error) {
            console.error('Error parsing persona from localStorage:', error);
          }
        }
        
        // Si no hay persona guardada o no se encuentra, seleccionar la de ID más bajo
        if (!personaSeleccionada) {
          personaSeleccionada = this.personasSimuladas.reduce((min, persona) => 
            persona.idPersona! < min.idPersona! ? persona : min
          );
        }
        
        this.seleccionarPersona(personaSeleccionada.idPersona!);
      } else {
        this.modoAlta = true;
        this.inicializarFormularioAlta();
      }

      this.cargando = false;
    }).catch(error => {
      console.error('Error cargando datos:', error);
      this.canSwitchDocente = false;
      this.mostrarMensajeError('Error al cargar los datos');
      this.cargando = false;
    });
  }

  /**
   * Cambia la persona simulada activa a partir del identificador elegido en la interfaz.
   * @param idPersona Identificador de la persona seleccionada, numérico o textual.
   */
  seleccionarPersona(idPersona: number | string): void {
    // Convertir a number si viene como string
    const idPersonaNumber = typeof idPersona === 'string' ? parseInt(idPersona, 10) : idPersona;
    
    this.personaSeleccionadaId = idPersonaNumber;
    this.personaSeleccionada = this.personasSimuladas.find(p => p.idPersona === idPersonaNumber) || null;
    this.modoAlta = false;
    
    if (this.personaSeleccionada) {
      this.cargarDatosPersona();
      // Notificar al servicio de selección para que otros componentes se actualicen
      this.personaSelectionService.setPersonaSeleccionada(this.personaSeleccionada);
    }
  }

  /**
   * Copia los datos de la persona seleccionada al formulario de edición.
   */
  cargarDatosPersona(): void {
    if (!this.personaSeleccionada) return;

    this.formularioPersona = {
      nombre: this.personaSeleccionada.nombre,
      anioNac: this.personaSeleccionada.anioNac,
      codDiscapacidad: this.personaSeleccionada.codDiscapacidad,
      codSitfam: this.personaSeleccionada.codSitfam,
      indCeumelilla: this.personaSeleccionada.indCeumelilla,
      codContrato: this.personaSeleccionada.codContrato,
      impPensionConyuge: this.personaSeleccionada.impPensionConyuge || 0,
      impPensionHijos: this.personaSeleccionada.impPensionHijos || 0,
      idComunidad: this.personaSeleccionada.idComunidad ?? this.obtenerIdComunidadPorDefecto()
    };
    
    // Cargar datos familiares
    this.cargarDatosFamiliares();
  }

  /**
   * Recupera la información de ascendientes y descendientes de la persona activa.
   */
  cargarDatosFamiliares(): void {
    if (!this.personaSeleccionadaId) return;
    
    // Cargar descendientes del backend
    this.personaSimuladaService.getDescendientes(this.personaSeleccionadaId).subscribe({
      next: (descendientes) => {
        this.descendientes = descendientes || [];
      },
      error: (error) => {
        console.error('Error cargando descendientes:', error);
        this.descendientes = [];
      }
    });
    
    // Cargar ascendientes del backend
    this.personaSimuladaService.getAscendientes(this.personaSeleccionadaId).subscribe({
      next: (ascendientes) => {
        this.ascendientes = ascendientes || [];
      },
      error: (error) => {
        console.error('Error cargando ascendientes:', error);
        this.ascendientes = [];
      }
    });
  }

  /**
   * Calcula la edad aproximada a partir del año de nacimiento indicado.
   * @param anioNac Año de nacimiento de la persona.
   * @returns Edad resultante respecto al año actual.
   */
  calcularEdad(anioNac: number): number {
    if (!anioNac || anioNac === 0) return 0;
    const anioActual = new Date().getFullYear();
    return anioActual - anioNac;
  }

  /**
   * Obtiene la descripción legible asociada a un código de discapacidad.
   * @param codDiscapacidad Código cuyo texto descriptivo se desea mostrar.
   * @returns Descripción encontrada o cadena vacía si no existe.
   */
  obtenerDescripcionDiscapacidad(codDiscapacidad: string): string {
    // Buscar en las discapacidades cargadas
    const discapacidad = this.discapacidades.find(d => d.codDiscapacidad === codDiscapacidad);
    return discapacidad ? discapacidad.descDiscapacidad : 'Desconocida';
  }

  /**
   * Abre el modal de gestión de descendientes de la persona activa.
   */
  editarDescendientes(): void {
    if (!this.personaSeleccionadaId) {
      this.mostrarMensajeError('No hay persona simulada seleccionada');
      return;
    }
    this.mostrarModalDescendientes = true;
  }

  /**
   * Abre el modal de gestión de ascendientes de la persona activa.
   */
  editarAscendientes(): void {
    if (!this.personaSeleccionadaId) {
      this.mostrarMensajeError('No hay persona simulada seleccionada');
      return;
    }
    this.mostrarModalAscendientes = true;
  }

  cerrarModalDescendientes(): void {
    this.mostrarModalDescendientes = false;
  }

  cerrarModalAscendientes(): void {
    this.mostrarModalAscendientes = false;
  }

  onDescendientesUpdated(descendientes: Descendiente[]): void {
    this.descendientes = descendientes;
    // También recargar los datos para asegurar sincronización
    this.cargarDatosFamiliares();
  }

  onAscendientesUpdated(ascendientes: Ascendiente[]): void {
    this.ascendientes = ascendientes;
    // También recargar los datos para asegurar sincronización
    this.cargarDatosFamiliares();
  }

  /**
   * Prepara el formulario para crear una nueva identidad simulada.
   */
  nuevaIdentidad(): void {
    this.modoAlta = true;
    this.personaSeleccionada = null;
    this.personaSeleccionadaId = null;
    this.descendientes = [];
    this.ascendientes = [];
    this.inicializarFormularioAlta();
  }

  /**
   * Inicializa el formulario de alta con valores por defecto y catálogos base.
   */
  inicializarFormularioAlta(): void {
    // Buscar valores por defecto
    const sinDiscapacidad = this.discapacidades.find(d => d.descDiscapacidad.toLowerCase().includes('sin discapacidad'));
    const noIncluido = this.situacionesFamiliares.find(s => s.descSitfam.toLowerCase().includes('no incluido'));
    const resto = this.contratos.find(c => c.descContrato.toLowerCase().includes('resto'));

    this.formularioPersona = {
      nombre: '',
      anioNac: undefined,
      codDiscapacidad: sinDiscapacidad?.codDiscapacidad || '',
      codSitfam: noIncluido?.codSitfam || '',
      indCeumelilla: 'N',
      codContrato: resto?.codContrato || '',
      impPensionConyuge: 0,
      impPensionHijos: 0,
      idComunidad: this.obtenerIdComunidadPorDefecto()
    };
  }

  /**
   * Resuelve la comunidad por defecto para nuevas identidades simuladas.
   * @returns Identificador de comunidad a usar como valor inicial.
   */
  obtenerIdComunidadPorDefecto(): number {
    const comunidadPorDefecto = this.comunidades.find(c => c.idComunidad === 1);
    return comunidadPorDefecto?.idComunidad || this.comunidades[0]?.idComunidad || 1;
  }

  /**
   * Valida y persiste el alta o la edición de la identidad simulada actual.
   */
  guardarPersona(): void {
    if (!this.validarFormulario()) return;
    if (!this.currentUserId) {
      this.mostrarMensajeError('No se pudo obtener el ID del usuario');
      return;
    }

    this.cargando = true;
    const datosPersona = {
      ...this.formularioPersona,
      id: this.currentUserId
    };

    if (this.modoAlta) {
      this.personaSimuladaService.createPersonaSimulada(datosPersona).subscribe({
        next: (response: ApiResponse) => {
          if (response.success) {
            this.mostrarMensajeExito(response.message);
            this.cargarDatos(response.data?.idPersona ?? null);
          } else {
            this.mostrarMensajeError(response.message);
            this.cargando = false;
          }
        },
        error: (error) => {
          const errorMessage = error.error?.message || error.message || 'Error desconocido';
          this.mostrarMensajeError('Error al crear la identidad simulada: ' + errorMessage);
          this.cargando = false;
        }
      });
    } else {
      this.personaSimuladaService.updatePersonaSimulada(this.personaSeleccionadaId!, datosPersona).subscribe({
        next: (response: ApiResponse) => {
          if (response.success) {
            this.mostrarMensajeExito(response.message);
            this.cargarDatos();
          } else {
            this.mostrarMensajeError(response.message);
            this.cargando = false;
          }
        },
        error: (error) => {
          const errorMessage = error.error?.message || error.message || 'Error desconocido';
          this.mostrarMensajeError('Error al actualizar la identidad simulada: ' + errorMessage);
          this.cargando = false;
        }
      });
    }
  }

  /**
   * Elimina la identidad simulada actualmente seleccionada.
   */
  async eliminarPersona(): Promise<void> {
    if (!this.personaSeleccionadaId) return;

    const confirmacion = await this.confirmDialogService.confirm(
      '¿Está seguro de que desea eliminar esta identidad simulada?\n\n' +
      'Se eliminarán todos los datos asociados:\n' +
      '- Puestos tipo\n' +
      '- Contratos\n' +
      '- Descendientes\n' +
      '- Ascendientes\n' +
      '- Datos económicos\n' +
      '- Simulaciones realizadas'
    );

    if (!confirmacion) return;

    this.cargando = true;
    this.personaSimuladaService.deletePersonaSimulada(this.personaSeleccionadaId).subscribe({
      next: (response: ApiResponse) => {
        if (response.success) {
          this.mostrarMensajeExito(response.message);
          this.cargarDatos();
        } else {
          this.mostrarMensajeError(response.message);
          this.cargando = false;
        }
      },
      error: (error) => {
        const errorMessage = error.error?.message || error.message || 'Error desconocido';
        this.mostrarMensajeError('Error al eliminar la identidad simulada: ' + errorMessage);
        this.cargando = false;
      }
    });
  }

  /**
   * Comprueba que el formulario de identidad contiene todos los datos obligatorios.
   * @returns `true` si la identidad puede guardarse.
   */
  validarFormulario(): boolean {
    if (!this.formularioPersona.nombre?.trim()) {
      this.mostrarMensajeError('El nombre es obligatorio');
      return false;
    }

    if (!this.formularioPersona.anioNac) {
      this.mostrarMensajeError('El año de nacimiento es obligatorio');
      return false;
    }

    if (!this.formularioPersona.codDiscapacidad) {
      this.mostrarMensajeError('Debe seleccionar una discapacidad');
      return false;
    }

    if (!this.formularioPersona.codSitfam) {
      this.mostrarMensajeError('Debe seleccionar una situación familiar');
      return false;
    }

    if (!this.formularioPersona.codContrato) {
      this.mostrarMensajeError('Debe seleccionar un contrato');
      return false;
    }

    if (!this.formularioPersona.idComunidad) {
      this.mostrarMensajeError('Debe seleccionar una comunidad autonoma');
      return false;
    }

    return true;
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
    
    setTimeout(() => {
      this.mostrarMensaje = false;
    }, 5000);
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
    
    setTimeout(() => {
      this.mostrarMensaje = false;
    }, 5000);
  }

  cerrarMensaje(): void {
    this.mostrarMensaje = false;
  }

  get ayudaActiva(): AyudaContextual {
    return this.ayudas[this.ayudaActivaKey] || this.ayudas['general'];
  }

  /**
   * Activa un bloque de ayuda contextual concreto dentro de la pantalla.
   * @param key Clave de la ayuda que debe marcarse como activa.
   */
  setAyudaActiva(key: string): void {
    this.ayudaActivaKey = this.ayudas[key] ? key : 'general';
  }

  restablecerAyudaGeneral(): void {
    this.ayudaActivaKey = 'general';
  }

  /**
   * Navega a una ruta interna indicada por la interfaz.
   * @param ruta Ruta destino dentro del frontend.
   */
  navegarA(ruta: string): void {
    this.router.navigate([ruta]);
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

