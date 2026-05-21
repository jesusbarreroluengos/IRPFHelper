import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { PersonaSimulada, PersonaSimuladaService } from '../services/persona-simulada.service';
import { AuthService } from '../auth.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';
import { environment } from '../../environments/environment';

interface PasswordData {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

@Component({
  selector: 'app-cambiar-password',
  imports: [CommonModule, FormsModule],
  templateUrl: './cambiar-password.html'
})
/**
 * Componente destinado a la actualización de credenciales del usuario
 * autenticado, incluyendo las validaciones básicas del formulario.
 */
export class CambiarPasswordComponent implements OnInit {
  private apiBase = environment.apiUrl || '/api';

  passwordData: PasswordData = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
    
  };

  isLoading = false;
  message = '';
  isSuccess = false;
  currentUsername: string | null = null;
  currentUserId: number | null = null;
  usernameFormatted: string | null = null;
  personaSeleccionada: PersonaSimulada | null = null;
  canSwitchDocente = false;
  isAdmin = false;
  menuItems: any[] = [];

  constructor(
    private router: Router,
    private http: HttpClient,
    private authService: AuthService,
    private personaSimuladaService: PersonaSimuladaService,
    private personaSelectionService: PersonaSimuladaSelectionService

  ) {}

  /**
   * Inicializa la pantalla de cambio de contraseña y verifica el contexto de usuario actual.
   */
  ngOnInit(): void {

    this.currentUsername = this.authService.getUsername();
    this.currentUserId = this.authService.getUserId();
    this.usernameFormatted = this.formatCamelCase(this.currentUsername || '');  
    this.suscribirseAPersonaSeleccionada();
    this.actualizarDisponibilidadCambioDocente();

    // Verificar que el usuario esté autenticado
    if (!this.authService.getUsername()) {
      this.router.navigate(['/login']);
      return;
    }
    
    this.currentUsername = this.authService.getUsername();
    this.updateMenuItems();
  }
  /**
   * Sincroniza la persona simulada activa con el estado local del componente.
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
      { name: 'Datos Personales', route: '/datos-personales', icon: '👤' },
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
   * Navega a una ruta interna del frontend.
   * @param route Ruta destino dentro de la aplicación.
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
   * Abre el selector de docentes cuando está habilitado el cambio de persona activa.
   */
  navigateToDocenteSelector(): void {
    if (!this.canSwitchDocente) {
      return;
    }
    this.router.navigate(['/seleccion-docente']);
  }

  /**
   * Determina si el usuario actual puede alternar entre varias personas simuladas.
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
   * Valida el formulario y solicita al backend el cambio de contraseña.
   */
  onSubmit(): void {
    if (this.passwordData.newPassword !== this.passwordData.confirmPassword) {
      this.showMessage('Las contraseñas no coinciden', false);
      return;
    }

    this.isLoading = true;
    this.message = '';

    const username = this.authService.getUsername();
    if (!username) {
      this.showMessage('Error: Usuario no encontrado', false);
      this.isLoading = false;
      return;
    }

    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    const body = {
      username: username,
      currentPassword: this.passwordData.currentPassword,
      newPassword: this.passwordData.newPassword
    };

    this.http.put(`${this.apiBase}/users/change-password`, body, { 
      headers,
      responseType: 'text'
    }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.showMessage('Contraseña actualizada exitosamente', true);
        // Redirigir a principal después de 2 segundos
        setTimeout(() => {
          this.router.navigate(['/principal'], { 
            queryParams: { passwordChanged: 'true' } 
          });
        }, 2000);
      },
      error: (error) => {
        this.isLoading = false;
        let errorMessage = 'Error al cambiar la contraseña';
        
        if (error.status === 401) {
          errorMessage = 'La contraseña actual es incorrecta';
        } else if (error.status === 404) {
          errorMessage = 'Usuario no encontrado';
        } else if (error.status === 400) {
          errorMessage = 'Faltan campos requeridos';
        }
        
        this.showMessage(errorMessage, false);
      }
    });
  }

  /**
   * Cancela la operación y vuelve a la vista principal.
   */
  onCancel(): void {
    this.router.navigate(['/principal']);
  }

  /**
   * Muestra un mensaje temporal de éxito o error en la interfaz.
   * @param text Texto que se desea presentar al usuario.
   * @param success Indica si el mensaje corresponde a una operación exitosa.
   */
  private showMessage(text: string, success: boolean): void {
    this.message = text;
    this.isSuccess = success;
    
    // Limpiar mensaje después de 5 segundos
    setTimeout(() => {
      this.message = '';
    }, 5000);
  }

  /**
   * Restablece los campos del formulario al estado inicial.
   */
  private resetForm(): void {
    this.passwordData = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
  }
  /**
   * Convierte un texto técnico a una presentación legible para la interfaz.
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

