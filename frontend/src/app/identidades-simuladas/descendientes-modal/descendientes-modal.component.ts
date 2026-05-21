import { Component, OnInit, OnChanges, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PersonaSimuladaService, Discapacidad, ApiResponse } from '../../services/persona-simulada.service';

export interface Descendiente {
  idDescendiente?: number;
  idPersona: number;
  anioNac: number | null;
  anioAdopcion: number | null;
  indPorentero: string;
  codDiscapacidad: string;
  indMovred: string;
  nombre?: string;
}

@Component({
  selector: 'app-descendientes-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './descendientes-modal.component.html',
  styleUrls: ['./descendientes-modal.component.css']
})
/**
 * Componente modal destinado a la administración de descendientes asociados a
 * la persona simulada actualmente seleccionada.
 */
export class DescendientesModalComponent implements OnInit, OnChanges {
  @Input() personaId: number | null = null;
  @Input() isVisible: boolean = false;
  @Output() closeModal = new EventEmitter<void>();
  @Output() descendientesUpdated = new EventEmitter<Descendiente[]>();

  discapacidades: Discapacidad[] = [];
  descendientes: Descendiente[] = [];

  formularioDescendiente: Partial<Descendiente> = {
    anioNac: null,
    anioAdopcion: null,
    indPorentero: 'N',
    codDiscapacidad: '',
    indMovred: 'N'
  };

  cargando = false;
  mensaje = '';
  tipoMensaje: 'success' | 'error' = 'success';
  mostrarMensaje = false;

  aniosNacimiento: number[] = [];
  aniosAdopcion: number[] = [];

  constructor(private personaSimuladaService: PersonaSimuladaService) {
    this.generarAnios();
  }

  ngOnInit(): void {
    if (this.isVisible && this.personaId) {
      this.cargarDatos();
    }
  }

  ngOnChanges(): void {
    if (this.isVisible && this.personaId) {
      this.cargarDatos();
    }
  }

  generarAnios(): void {
    const anioActual = new Date().getFullYear();
    const anioInicio = 1990;

    this.aniosNacimiento = [];
    this.aniosAdopcion = [];

    for (let anio = anioActual; anio >= anioInicio; anio--) {
      this.aniosNacimiento.push(anio);
      this.aniosAdopcion.push(anio);
    }
  }

  cargarDatos(): void {
    if (!this.personaId) {
      this.cargando = false;
      return;
    }

    this.cargando = true;

    this.personaSimuladaService.getDiscapacidades().subscribe({
      next: (discapacidades) => {
        this.discapacidades = discapacidades || [];

        const sinDiscapacidad = this.discapacidades.find(d =>
          d.descDiscapacidad.toLowerCase().includes('sin discapacidad')
        );

        if (sinDiscapacidad && !this.formularioDescendiente.codDiscapacidad) {
          this.formularioDescendiente.codDiscapacidad = sinDiscapacidad.codDiscapacidad;
        }

        this.cargarDescendientes();
      },
      error: (error) => {
        console.error('Error cargando discapacidades:', error);
        this.mostrarMensajeError('Error al cargar las discapacidades');
        this.cargando = false;
      }
    });
  }

  cargarDescendientes(): void {
    if (!this.personaId) {
      this.cargando = false;
      return;
    }

    this.cargando = true;

    this.personaSimuladaService.getDescendientes(this.personaId).subscribe({
      next: (descendientes) => {
        this.descendientes = descendientes || [];
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error cargando descendientes:', error);
        this.mostrarMensajeError('Error al cargar los descendientes');
        this.cargando = false;
      }
    });
  }

  crearDescendiente(): void {
    if (!this.validarFormulario()) {
      return;
    }

    if (!this.personaId) {
      this.mostrarMensajeError('No se pudo obtener el ID de la persona');
      return;
    }

    this.cargando = true;

    const datosDescendiente = {
      idPersona: this.personaId,
      anioNac: this.formularioDescendiente.anioNac || null,
      anioAdopcion: this.formularioDescendiente.anioAdopcion || null,
      indPorentero: this.formularioDescendiente.indPorentero || 'N',
      codDiscapacidad: this.formularioDescendiente.codDiscapacidad || '',
      indMovred: 'N'
    };

    if (!datosDescendiente.codDiscapacidad) {
      this.mostrarMensajeError('Debe seleccionar una discapacidad');
      this.cargando = false;
      return;
    }

    this.personaSimuladaService.createDescendiente(datosDescendiente).subscribe({
      next: (response: ApiResponse) => {
        if (response.success) {
          this.mostrarMensajeExito(response.message);
          this.limpiarFormulario();
          this.cargarDescendientes();
        } else {
          this.mostrarMensajeError(response.message);
          this.cargando = false;
        }
      },
      error: (error) => {
        console.error('Error creando descendiente:', error);

        let errorMessage = 'Error desconocido';

        if (error.error) {
          if (typeof error.error === 'string') {
            errorMessage = error.error;
          } else if (error.error.message) {
            errorMessage = error.error.message;
          } else if (error.error.error) {
            errorMessage = error.error.error;
          } else if (typeof error.error === 'object') {
            try {
              if (error.error.detail) {
                errorMessage = error.error.detail;
              } else if (error.error.title) {
                errorMessage = error.error.title;
              } else if (error.error.status) {
                errorMessage = `Error ${error.error.status}: ${error.error.title || 'Error del servidor'}`;
              } else {
                errorMessage = JSON.stringify(error.error);
              }
            } catch {
              errorMessage = 'Error al procesar la respuesta del servidor';
            }
          }
        } else if (error.message) {
          errorMessage = error.message;
        }

        if (typeof errorMessage === 'string') {
          errorMessage = errorMessage.replace(/^SyntaxError:.*$/gm, '').trim();
          if (errorMessage.startsWith('"') && errorMessage.endsWith('"')) {
            errorMessage = errorMessage.slice(1, -1);
          }
        } else if (typeof errorMessage === 'object') {
          errorMessage = JSON.stringify(errorMessage);
        }

        if (typeof errorMessage !== 'string') {
          errorMessage = String(errorMessage);
        }

        if (errorMessage.length > 200) {
          errorMessage = errorMessage.substring(0, 200) + '...';
        }

        this.mostrarMensajeError('Error al crear el descendiente: ' + errorMessage);
        this.cargando = false;
      }
    });
  }

  editarDescendiente(descendiente: Descendiente): void {
    alert('Funcionalidad de edición pendiente de implementar');
  }

  eliminarDescendiente(descendiente: Descendiente): void {
    if (!descendiente.idDescendiente) {
      return;
    }

    const confirmacion = confirm(
      '¿Está seguro de que desea eliminar este descendiente?\n\n' +
      `Año de nacimiento: ${descendiente.anioNac || 'No especificado'}\n` +
      `Año de adopción: ${descendiente.anioAdopcion || 'No especificado'}\n` +
      `Por entero: ${descendiente.indPorentero === 'S' ? 'Sí' : 'No'}`
    );

    if (!confirmacion) {
      return;
    }

    this.cargando = true;

    this.personaSimuladaService.deleteDescendiente(descendiente.idDescendiente).subscribe({
      next: (response: ApiResponse) => {
        if (response.success) {
          this.mostrarMensajeExito(response.message);
          this.cargarDescendientes();
        } else {
          this.mostrarMensajeError(response.message);
          this.cargando = false;
        }
      },
      error: (error) => {
        console.error('Error eliminando descendiente:', error);

        let errorMessage = 'Error desconocido';

        if (error.error) {
          if (typeof error.error === 'string') {
            errorMessage = error.error;
          } else if (error.error.message) {
            errorMessage = error.error.message;
          } else if (error.error.error) {
            errorMessage = error.error.error;
          }
        } else if (error.message) {
          errorMessage = error.message;
        }

        if (typeof errorMessage === 'string') {
          errorMessage = errorMessage.replace(/^SyntaxError:.*$/gm, '').trim();
          if (errorMessage.startsWith('"') && errorMessage.endsWith('"')) {
            errorMessage = errorMessage.slice(1, -1);
          }
        } else if (typeof errorMessage === 'object') {
          errorMessage = JSON.stringify(errorMessage);
        }

        if (typeof errorMessage !== 'string') {
          errorMessage = String(errorMessage);
        }

        if (errorMessage.length > 200) {
          errorMessage = errorMessage.substring(0, 200) + '...';
        }

        this.mostrarMensajeError('Error al eliminar el descendiente: ' + errorMessage);
        this.cargando = false;
      }
    });
  }

  actualizarDescendiente(descendiente: Descendiente): void {
    if (!descendiente.idDescendiente) {
      return;
    }

    if (!this.validarDescendiente(descendiente)) {
      return;
    }

    this.personaSimuladaService.updateDescendiente(descendiente.idDescendiente, descendiente).subscribe({
      next: (response: ApiResponse) => {
        if (response.success) {
          this.mostrarMensajeExito('Descendiente actualizado exitosamente');
        } else {
          this.mostrarMensajeError(response.message);
        }
      },
      error: (error) => {
        console.error('Error actualizando descendiente:', error);
        this.mostrarMensajeError('Error al actualizar el descendiente');
      }
    });
  }

  validarFormulario(): boolean {
    if (!this.formularioDescendiente.anioNac) {
      this.mostrarMensajeError('El año de nacimiento es obligatorio');
      return false;
    }

    if (!this.esAnioAdopcionValido(this.formularioDescendiente)) {
      this.mostrarMensajeError('El año de adopción debe ser mayor o igual al año de nacimiento');
      return false;
    }

    if (!this.formularioDescendiente.codDiscapacidad) {
      this.mostrarMensajeError('Debe seleccionar una discapacidad');
      return false;
    }

    return true;
  }

  limpiarFormulario(): void {
    const sinDiscapacidad = this.discapacidades.find(d =>
      d.descDiscapacidad.toLowerCase().includes('sin discapacidad')
    );

    this.formularioDescendiente = {
      anioNac: null,
      anioAdopcion: null,
      indPorentero: 'N',
      codDiscapacidad: sinDiscapacidad?.codDiscapacidad || '',
      indMovred: 'N'
    };
  }

  cerrarModal(): void {
    this.cargando = false;
    this.closeModal.emit();
    this.descendientesUpdated.emit(this.descendientes);
  }

  mostrarMensajeExito(mensaje: string): void {
    this.mensaje = mensaje;
    this.tipoMensaje = 'success';
    this.mostrarMensaje = true;
    this.cargando = false;

    setTimeout(() => {
      this.mostrarMensaje = false;
    }, 5000);
  }

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

  obtenerDescripcionDiscapacidad(codDiscapacidad: string): string {
    const discapacidad = this.discapacidades.find(d => d.codDiscapacidad === codDiscapacidad);
    return discapacidad ? discapacidad.descDiscapacidad : 'Desconocida';
  }

  onAnioNacimientoChange(): void {
    if (!this.formularioDescendiente.anioNac) {
      this.formularioDescendiente.anioNac = null;
    }
  }

  onAnioAdopcionChange(): void {
    if (!this.formularioDescendiente.anioAdopcion) {
      this.formularioDescendiente.anioAdopcion = null;
    }
  }

  onAnioNacimientoTableChange(descendiente: Descendiente): void {
    this.actualizarDescendiente(descendiente);
  }

  onAnioAdopcionTableChange(descendiente: Descendiente): void {
    this.actualizarDescendiente(descendiente);
  }

  validarDescendiente(descendiente: Descendiente): boolean {
    if (!descendiente.anioNac) {
      this.mostrarMensajeError('El año de nacimiento es obligatorio');
      return false;
    }

    if (!this.esAnioAdopcionValido(descendiente)) {
      this.mostrarMensajeError('El año de adopción debe ser mayor o igual al año de nacimiento');
      return false;
    }

    if (!descendiente.codDiscapacidad) {
      this.mostrarMensajeError('Debe seleccionar una discapacidad');
      return false;
    }

    return true;
  }

  puedeGuardarFormulario(): boolean {
    return !!this.formularioDescendiente.anioNac
      && !!this.formularioDescendiente.codDiscapacidad
      && this.esAnioAdopcionValido(this.formularioDescendiente);
  }

  private esAnioAdopcionValido(descendiente: Partial<Descendiente>): boolean {
    if (!descendiente.anioNac || !descendiente.anioAdopcion) {
      return true;
    }

    return descendiente.anioAdopcion >= descendiente.anioNac;
  }
}
