import { Component, EventEmitter, Output } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

interface ParsedError {
  message: string;
  code?: string;
}

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: []
})
/**
 * Componente que implementa el acceso al sistema y gestiona tanto la
 * autenticación como el reenvío del correo de verificación.
 */
export class LoginComponent {
  @Output() loginSuccess = new EventEmitter<void>();

  username = '';
  password = '';
  message = '';
  loggedIn = false;
  showResendVerificationButton = false;
  resendInProgress = false;

  constructor(private authService: AuthService, private router: Router) {}

  /**
   * Redirige al formulario de alta de usuario desde la pantalla de acceso.
   */
  goToCrearUsuario(): void {
    this.router.navigate(['/crear-usuario']);
  }

  /**
   * Procesa el formulario de login y gestiona tanto el acceso como los errores de validación.
   */
  onSubmit(): void {
    this.message = '';
    this.showResendVerificationButton = false;

    this.authService.login(this.username, this.password).subscribe({
      next: (response) => {
        this.message = response;
        this.loggedIn = true;
        this.loginSuccess.emit();
        this.router.navigate(['/principal']);
      },
      error: (err) => {
        console.error('Error:', err);
        const parsedError = this.parseBackendError(err);

        this.message = parsedError.message || 'Error de autenticacion';
        this.showResendVerificationButton = parsedError.code === 'EMAIL_NOT_VERIFIED';
      }
    });
  }

  /**
   * Solicita un nuevo correo de verificación usando las credenciales introducidas.
   */
  reenviarCorreoVerificacion(): void {
    if (!this.username || !this.password) {
      this.message = 'Introduzca usuario y contrasena para reenviar el correo de verificacion.';
      return;
    }

    this.resendInProgress = true;
    this.authService.resendVerificationEmail(this.username, this.password, this.getAppBaseUrl()).subscribe({
      next: (responseMessage) => {
        this.message = responseMessage;
        this.resendInProgress = false;
      },
      error: (err) => {
        const parsedError = this.parseBackendError(err);
        this.message = parsedError.message || 'No se pudo reenviar el correo de verificacion.';
        this.resendInProgress = false;
      }
    });
  }

  /**
   * Normaliza el error devuelto por el backend para mostrar mensajes coherentes en la vista.
   * @param err Error crudo recibido desde la petición HTTP.
   * @returns Objeto con mensaje y código opcional interpretados para la interfaz.
   */
  private parseBackendError(err: any): ParsedError {
    if (!err) {
      return { message: 'Error inesperado' };
    }

    if (typeof err.error === 'string') {
      try {
        const parsed = JSON.parse(err.error);
        return {
          message: parsed?.message || err.error,
          code: parsed?.code
        };
      } catch {
        return { message: err.error };
      }
    }

    if (typeof err.error === 'object' && err.error !== null) {
      return {
        message: err.error.message || `Error inesperado: ${err.status}`,
        code: err.error.code
      };
    }

    return { message: `Error inesperado: ${err.status} - ${err.statusText}` };
  }

  /**
   * Construye la URL base actual de la aplicación para reutilizarla en enlaces de verificación.
   * @returns URL base formada por origen y ruta base del frontend.
   */
  private getAppBaseUrl(): string {
    const path = window.location.pathname || '';
    const basePath = path.startsWith('/irpfhelper') ? '/irpfhelper' : '';
    return `${window.location.origin}${basePath}`;
  }
}
