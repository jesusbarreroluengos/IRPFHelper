import { Component, OnInit } from '@angular/core';
import { UsuarioService } from '../services/usuario.service';
import { Usuario } from '../models/usuario.model';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { PersonaSimulada, PersonaSimuladaService } from '../services/persona-simulada.service';
import { AuthService } from '../auth.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';
import { environment } from '../../environments/environment';
import { ConfirmDialogService } from '../services/confirm-dialog.service';

@Component({
  selector: 'app-personas',
  standalone: false,
  templateUrl: './personas.component.html'
})
/**
 * Componente destinado a la administración de usuarios, incluyendo consulta,
 * edición, paginación y eliminación de registros.
 */
export class PersonasComponent implements OnInit {
  private apiBase = environment.apiUrl || '/api';
  usuarios: Usuario[] = [];
  usuariosPaginados: Usuario[] = [];
  message: string = '';
  currentUsername: string | null = null;
  currentUserId: number | null = null;
 	usernameFormatted: string | null = null;
  personaSeleccionada: PersonaSimulada | null = null;
  canSwitchDocente = false;
  isAdmin = false;

  
  // Configuración de paginación
  itemsPorPagina = 5;
  paginaActual = 1;
  totalPaginas = 0;
  totalUsuarios = 0;

  // Menú lateral
  menuItems: any[] = [];

  constructor(
    private usuarioService: UsuarioService, 
    private http: HttpClient,
    private router: Router,
    private authService: AuthService,
    private personaSimuladaService: PersonaSimuladaService,
    private personaSelectionService: PersonaSimuladaSelectionService,
    private confirmDialogService: ConfirmDialogService

  ) {}

  /**
   * Inicializa la pantalla de administración de usuarios, carga datos y sincroniza la persona activa.
   */
  ngOnInit(): void {
    this.currentUsername = this.authService.getUsername();
    this.currentUserId = this.authService.getUserId();
    this.usernameFormatted = this.formatCamelCase(this.currentUsername || '');  
    this.suscribirseAPersonaSeleccionada();
    this.actualizarDisponibilidadCambioDocente();
    this.updateMenuItems();
    this.cargarUsuarios();
  }
  /**
   * Mantiene la persona simulada seleccionada sincronizada con el servicio compartido.
   */
  private suscribirseAPersonaSeleccionada(): void {
    this.personaSelectionService.personaSeleccionada$.subscribe(persona => {
      this.personaSeleccionada = persona;
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

    // Filtrar elementos del menú basado en permisos de administrador
    this.menuItems = allMenuItems.filter(item => {
      if (item.requiresAdmin) {
        return this.isAdmin;
      }
      return true;
    });
  }

  /**
   * Recupera el listado de usuarios y prepara la estructura usada por la tabla paginada.
   */
  cargarUsuarios(): void {
    console.log('Cargando usuarios...');
    this.usuarioService.getUsuarios().subscribe({
      next: (data) => {
        console.log('Usuarios recibidos:', data);
        this.usuarios = data.map(usuario => ({
          ...usuario,
          numAccErroneos: this.normalizarNumAccErroneos(usuario.numAccErroneos)
        }));
        this.totalUsuarios = this.usuarios.length;
        this.totalPaginas = Math.ceil(this.totalUsuarios / this.itemsPorPagina);
        this.actualizarUsuariosPaginados();
      },
      error: (error) => {
        console.error('Error al obtener usuarios:', error);
        alert('Error al cargar usuarios: ' + (error.error || error.message || 'Error desconocido'));
      }
    });
  }

  /**
   * Actualiza el subconjunto de usuarios mostrado en la página actual.
   */
  actualizarUsuariosPaginados(): void {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina;
    const fin = inicio + this.itemsPorPagina;
    this.usuariosPaginados = this.usuarios.slice(inicio, fin);
  }

  /**
   * Cambia la página visible de la tabla si el número solicitado es válido.
   * @param pagina Número de página al que se quiere navegar.
   */
  cambiarPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas) {
      this.paginaActual = pagina;
      this.actualizarUsuariosPaginados();
    }
  }

  paginaAnterior(): void {
    if (this.paginaActual > 1) {
      this.cambiarPagina(this.paginaActual - 1);
    }
  }

  paginaSiguiente(): void {
    if (this.paginaActual < this.totalPaginas) {
      this.cambiarPagina(this.paginaActual + 1);
    }
  }

  /**
   * Genera la secuencia de páginas disponible para el paginador.
   * @returns Array con los números de página mostrables.
   */
  getNumerosPaginas(): number[] {
    const numeros: number[] = [];
    for (let i = 1; i <= this.totalPaginas; i++) {
      numeros.push(i);
    }
    return numeros;
  }

  // Método para trackBy en ngFor (mejora el rendimiento)
  /**
   * Proporciona una clave estable para optimizar el renderizado de filas en Angular.
   * @param index Índice de la fila en la iteración actual.
   * @param usuario Usuario asociado a la fila renderizada.
   * @returns Identificador único del usuario.
   */
  trackByUsuarioId(index: number, usuario: Usuario): number {
    return usuario.id;
  }

  // Método para editar usuario
  /**
   * Guarda los cambios realizados sobre un usuario desde la tabla de administración.
   * @param usuario Usuario con las modificaciones aplicadas en la interfaz.
   */
  editarUsuario(usuario: Usuario): void {
    console.log('Editar usuario:', usuario);

    const payload = {
      id: usuario.id,
      usuario: usuario.usuario,
      email: usuario.email,
      password: usuario.password,
      esadmin: this.getEsAdminBool(usuario) ? 'S' : 'N',
      esverificado: this.getEsVerificadoBool(usuario) ? 'S' : 'N',
      numAccErroneos: this.normalizarNumAccErroneos(usuario.numAccErroneos)
    };

    // Limpiar mensaje anterior
    this.message = '';

    this.http.post(`${this.apiBase}/users/update`, payload, { responseType: 'text' })
      .subscribe({
        next: (res) => {
          console.log('Usuario actualizado exitosamente:', res);
          this.message = 'Los datos del usuario han sido actualizados correctamente.';
          // Recargar usuarios después de un breve delay para que el usuario vea el mensaje
          setTimeout(() => {
            this.cargarUsuarios();
            // Limpiar mensaje después de mostrar la lista actualizada
            setTimeout(() => this.clearMessage(), 3000);
          }, 1000);
        },
        error: err => {
          console.error('Error al actualizar usuario:', err);
          this.message = 'Error al actualizar usuario: ' + (err.error || err.message || 'Error desconocido');
          // Limpiar mensaje de error después de 5 segundos
          setTimeout(() => this.clearMessage(), 5000);
        }
      });
  }
  /**
   * Convierte el indicador textual de administrador a un booleano utilizable en la vista.
   * @param usuario Usuario cuyo rol se quiere interpretar.
   * @returns `true` si el usuario es administrador.
   */
getEsAdminBool(usuario:Usuario): boolean {
  return usuario.esadmin === 'S';
}

  /**
   * Actualiza en memoria el indicador de administrador según el estado de un checkbox.
   * @param usuario Usuario que se está editando.
   * @param checkedEvent Evento del control que contiene el nuevo valor marcado.
   */
setEsAdminBool(usuario: Usuario, checkedEvent: Event) {
  const checked = (checkedEvent.target as HTMLInputElement).checked;
  usuario.esadmin = checked ? 'S' : 'N';
}

  /**
   * Convierte el estado textual de verificación a un booleano para la interfaz.
   * @param usuario Usuario cuyo estado de verificación se consulta.
   * @returns `true` si el usuario ya está verificado.
   */
getEsVerificadoBool(usuario: Usuario): boolean {
  return usuario.esverificado === 'S';
}

  /**
   * Actualiza en memoria el estado de verificación de un usuario desde la vista.
   * @param usuario Usuario que se está modificando.
   * @param checkedEvent Evento del checkbox con el nuevo estado.
   */
setEsVerificadoBool(usuario: Usuario, checkedEvent: Event) {
  const checked = (checkedEvent.target as HTMLInputElement).checked;
  usuario.esverificado = checked ? 'S' : 'N';
}

  /**
   * Normaliza la entrada de accesos erróneos y la aplica al usuario editado.
   * @param usuario Usuario sobre el que se actualiza el contador.
   * @param inputEvent Evento del campo numérico editado por el usuario.
   */
setNumAccErroneos(usuario: Usuario, inputEvent: Event): void {
  const input = inputEvent.target as HTMLInputElement;
  const soloDigitos = input.value.replace(/\D+/g, '').slice(0, 2);

  if (input.value !== soloDigitos) {
    input.value = soloDigitos;
  }

  usuario.numAccErroneos = this.normalizarNumAccErroneos(soloDigitos);
}

  // Método para eliminar usuario
  /**
   * Elimina un usuario tras la confirmación explícita del operador.
   * @param id Identificador del usuario que se desea eliminar.
   */
  async eliminarUsuario(id: number): Promise<void> {
    if (!await this.confirmDialogService.confirm('¿Estás seguro de que quieres eliminar este usuario?')) {
      return;
    }
      console.log('Intentando eliminar usuario con ID:', id);
      
      this.usuarioService.deleteUsuario(id).subscribe({
        next: (response) => {
          console.log('Respuesta del servidor:', response);
          console.log('Usuario eliminado exitosamente');
          
          // Mostrar mensaje de éxito
          alert('Usuario eliminado correctamente');
          
          // Recargar la lista de usuarios
          this.cargarUsuarios();
        },
        error: (error) => {
          console.error('Error al eliminar usuario:', error);
          console.error('Status:', error.status);
          console.error('Message:', error.message);
          console.error('Error completo:', error);
          
          // Mostrar mensaje de error al usuario
          alert('Error al eliminar usuario: ' + (error.error || error.message || 'Error desconocido'));
        }
      });
  }

  // Exponer Math para usar en el template
  Math = Math;

  // Método de navegación
  /**
   * Navega a una ruta interna de la aplicación.
   * @param route Ruta destino dentro del frontend.
   */
  navigateTo(route: string) {
    if (route === '/login') {
      // Limpiar datos de autenticación
      this.authService.clearUsername();
      localStorage.removeItem('token');
      localStorage.removeItem('personaSimuladaSeleccionada');
    }
    this.router.navigate([route]);
  }

  /**
   * Abre el selector de docentes cuando el usuario puede cambiar la persona activa.
   */
  navigateToDocenteSelector(): void {
    if (!this.canSwitchDocente) {
      return;
    }
    this.router.navigate(['/seleccion-docente']);
  }

  /**
   * Determina si el usuario actual dispone de más de una persona simulada seleccionable.
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

  // Método para limpiar mensajes
  /**
   * Limpia el mensaje de feedback mostrado en la pantalla.
   */
  clearMessage(): void {
    this.message = '';
  }

  /**
   * Convierte un texto técnico a una etiqueta más legible para la interfaz.
   * @param value Texto original a transformar.
   * @returns Cadena normalizada con capitalización por palabra.
   */
formatCamelCase(value: string): string {
  return value
    .toLowerCase()
    .replace(/[_-]/g, ' ')
    .replace(/\b\w/g, char => char.toUpperCase());
}

  /**
   * Convierte el valor recibido desde la vista a un entero no negativo seguro.
   * @param valor Valor de entrada que puede llegar como número, texto o nulo.
   * @returns Número normalizado listo para persistirse.
   */
private normalizarNumAccErroneos(valor: unknown): number {
  if (valor === null || valor === undefined) {
    return 0;
  }

  if (typeof valor === 'number') {
    if (!Number.isFinite(valor) || valor < 0) {
      return 0;
    }
    return Math.trunc(valor);
  }

  const soloDigitos = String(valor).replace(/\D+/g, '');
  if (!soloDigitos) {
    return 0;
  }

  return Number.parseInt(soloDigitos, 10);
}

}

