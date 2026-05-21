import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PersonaSimuladaService, Discapacidad, ApiResponse } from '../../services/persona-simulada.service';

export interface Ascendiente {
  idAscendiente?: number;
  idPersona: number;
  anioNac: number;
  codDiscapacidad: string;
  indCompartido: number;
  indNivrenta?: string;
  codGradoconv?: string;
  indMovred?: string;
}

@Component({
  selector: 'app-ascendientes-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ascendientes-modal.component.html',
  styleUrls: ['./ascendientes-modal.component.css']
})
/**
 * Componente modal utilizado para gestionar la información de ascendientes
 * vinculada a una persona simulada.
 */
export class AscendientesModalComponent implements OnInit, OnChanges {
  @Input() personaId: number | null = null;
  @Input() isVisible: boolean = false;
  @Output() closeModal = new EventEmitter<void>();
  @Output() ascendientesUpdated = new EventEmitter<Ascendiente[]>();

  // Datos del componente
  ascendientes: Ascendiente[] = [];
  discapacidades: Discapacidad[] = [];
  
  // Formulario de nuevo ascendiente
  formularioAscendiente: Partial<Ascendiente> = {
    anioNac: undefined,
    codDiscapacidad: '',
    indCompartido: 1
  };

  // Estados del componente
  cargando: boolean = false;
  mensaje: string = '';
  mostrarMensaje: boolean = false;

  // Años para el desplegable (1910 hasta año actual)
  aniosNacimiento: number[] = [];

  // Numero de hijos que ocomparten el ascendiente
  hijosCompartido: number[] = [];


  constructor(private personaSimuladaService: PersonaSimuladaService) {
    this.generarAniosNacimiento();
    this.generarHijosCompartido();
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['personaId'] && !changes['personaId'].firstChange) {
      this.cargarDatos();
    }
  }

  generarAniosNacimiento(): void {
    const anioActual = new Date().getFullYear();
    for (let anio = 1910; anio <= anioActual; anio++) {
      this.aniosNacimiento.push(anio);
    }
    // Ordenar de forma ascendente (más antiguo primero)
    this.aniosNacimiento.sort((a, b) => a - b);
  }

  generarHijosCompartido(): void {
    // Generar opciones para hijos que comparten (1 a 10)
    for (let i = 1; i < 10; i++) {
      this.hijosCompartido.push(i);
    }
    // Ordenar de forma ascendente (más antiguo primero)
    this.hijosCompartido.sort((a, b) => a - b);

  }

  cargarDatos(): void {
    if (!this.personaId) {
      this.cargando = false;
      return;
    }

    this.cargando = true;

    // Cargar discapacidades
    this.personaSimuladaService.getDiscapacidades().subscribe({
      next: (discapacidades) => {
        this.discapacidades = discapacidades || [];
        // Establecer "sin discapacidad" como valor por defecto
        const sinDiscapacidad = this.discapacidades.find(d => d.descDiscapacidad.toLowerCase().includes('sin'));
        if (sinDiscapacidad) {
          this.formularioAscendiente.codDiscapacidad = sinDiscapacidad.codDiscapacidad;
        }
        this.cargarAscendientes();
      },
      error: (error) => {
        console.error('Error cargando discapacidades:', error);
        this.mostrarMensajeError('Error al cargar las discapacidades');
        this.cargando = false;
      }
    });
  }

  cargarAscendientes(): void {
    if (!this.personaId) {
      this.cargando = false;
      return;
    }

    this.personaSimuladaService.getAscendientes(this.personaId).subscribe({
      next: (ascendientes) => {
        this.ascendientes = ascendientes || [];
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error cargando ascendientes:', error);
        this.mostrarMensajeError('Error al cargar los ascendientes');
        this.cargando = false;
      }
    });
  }

  crearAscendiente(): void {
    if (!this.validarFormulario()) return;

    const nuevoAscendiente: Ascendiente = {
      idPersona: this.personaId!,
      anioNac: this.formularioAscendiente.anioNac!,
      codDiscapacidad: this.formularioAscendiente.codDiscapacidad!,
      indCompartido: this.formularioAscendiente.indCompartido!,
      indNivrenta: 'N', // Por defecto
      codGradoconv: 'N', // Por defecto
      indMovred: 'N' // Por defecto
    };

    this.cargando = true;

    this.personaSimuladaService.createAscendiente(nuevoAscendiente).subscribe({
      next: (response) => {
        if (response.success) {
          this.cargarAscendientes();
          this.limpiarFormulario();
          this.mostrarMensajeExito('Ascendiente creado exitosamente');
          this.ascendientesUpdated.emit(this.ascendientes);
        } else {
          this.mostrarMensajeError(response.message || 'Error al crear el ascendiente');
        }
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error creando ascendiente:', error);
        this.mostrarMensajeError('Error al crear el ascendiente');
        this.cargando = false;
      }
    });
  }

  actualizarAscendiente(ascendiente: Ascendiente): void {
    if (!ascendiente.idAscendiente) return;

    this.cargando = true;

    this.personaSimuladaService.updateAscendiente(ascendiente.idAscendiente, ascendiente).subscribe({
      next: (response) => {
        if (response.success) {
          this.mostrarMensajeExito('Ascendiente actualizado exitosamente');
          this.ascendientesUpdated.emit(this.ascendientes);
        } else {
          this.mostrarMensajeError(response.message || 'Error al actualizar el ascendiente');
        }
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error actualizando ascendiente:', error);
        this.mostrarMensajeError('Error al actualizar el ascendiente');
        this.cargando = false;
      }
    });
  }

  eliminarAscendiente(ascendiente: Ascendiente): void {
    if (!ascendiente.idAscendiente) return;

    if (!confirm('¿Está seguro de que desea eliminar este ascendiente?')) {
      return;
    }

    this.cargando = true;

    this.personaSimuladaService.deleteAscendiente(ascendiente.idAscendiente).subscribe({
      next: (response) => {
        if (response.success) {
          this.ascendientes = this.ascendientes.filter(a => a.idAscendiente !== ascendiente.idAscendiente);
          this.mostrarMensajeExito('Ascendiente eliminado exitosamente');
          this.ascendientesUpdated.emit(this.ascendientes);
        } else {
          this.mostrarMensajeError(response.message || 'Error al eliminar el ascendiente');
        }
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error eliminando ascendiente:', error);
        this.mostrarMensajeError('Error al eliminar el ascendiente');
        this.cargando = false;
      }
    });
  }

  validarFormulario(): boolean {
    if (!this.formularioAscendiente.anioNac) {
      this.mostrarMensajeError('El año de nacimiento es obligatorio');
      return false;
    }

    if (!this.formularioAscendiente.indCompartido) {
      this.mostrarMensajeError('El número de hijos que comparten el cuidado del padre es obligatorio');
      return false;
    }

    if (!this.formularioAscendiente.codDiscapacidad) {
      this.mostrarMensajeError('La discapacidad es obligatoria');
      return false;
    }

    return true;
  }

  limpiarFormulario(): void {
    this.formularioAscendiente = {
      anioNac: undefined,
      codDiscapacidad: '',
      indCompartido: 1
    };
    
    // Restablecer valor por defecto de discapacidad
    const sinDiscapacidad = this.discapacidades.find(d => d.descDiscapacidad.toLowerCase().includes('sin'));
    if (sinDiscapacidad) {
      this.formularioAscendiente.codDiscapacidad = sinDiscapacidad.codDiscapacidad;
    }
  }

  obtenerDescripcionDiscapacidad(codDiscapacidad: string): string {
    const discapacidad = this.discapacidades.find(d => d.codDiscapacidad === codDiscapacidad);
    return discapacidad ? discapacidad.descDiscapacidad : 'Desconocida';
  }

  calcularEdad(anioNac: number): number {
    if (!anioNac || anioNac === 0) return 0;
    return new Date().getFullYear() - anioNac;
  }

  mostrarMensajeExito(mensaje: string): void {
    this.mensaje = mensaje;
    this.mostrarMensaje = true;
    setTimeout(() => this.cerrarMensaje(), 3000);
  }

  mostrarMensajeError(mensaje: string): void {
    this.mensaje = mensaje;
    this.mostrarMensaje = true;
    setTimeout(() => this.cerrarMensaje(), 5000);
  }

  cerrarMensaje(): void {
    this.mostrarMensaje = false;
    this.mensaje = '';
  }

  cerrarModal(): void {
    this.closeModal.emit();
  }
}
