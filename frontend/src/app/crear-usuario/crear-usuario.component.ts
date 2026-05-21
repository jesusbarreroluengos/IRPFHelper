import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-crear-usuario',
  standalone: false,
  templateUrl: './crear-usuario.component.html',
  styleUrls: []
})
/**
 * Componente responsable de canalizar el proceso de registro de nuevos
 * usuarios mediante un formulario de alta.
 */
export class CrearUsuarioComponent {
  private apiBase = environment.apiUrl || '/api';
  private readonly allowedSpecialChars = "!@#$%^&*()-_+={}[]|;:\u00AB\u2018<>.,?/";
  readonly passwordPolicyMessage = 'La contrasena debe tener al menos 12 caracteres, incluir al menos una mayuscula, una minuscula, un numero y un caracter especial permitido.';
  readonly passwordSpecialCharsHelp = 'Caracteres especiales permitidos: !, @, #, $, %, ^, &, *, (, ), -, _, +, =, {, }, [, ], |, ;, :, \u00AB, \u2018, <, >, ,, ., ?, /.';

  username = '';
  password = '';
  email = '';
  confirmPassword = '';
  message = '';

  constructor(private router: Router, private http: HttpClient) {}

  /**
   * Valida los datos del formulario y envía la solicitud de creación de usuario al backend.
   */
  crearUsuario(): void {
    if (!this.username || !this.password || !this.email || !this.confirmPassword) {
      this.message = 'Por favor, rellena todos los campos.';
      return;
    }
    if (!this.email.includes('@')) {
      this.message = 'Por favor, incorpora un email correcto.';
      return;
    }
    if (!this.isSecurePassword(this.password)) {
      this.message = this.passwordPolicyMessage;
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.message = 'Las contraseñas no coinciden.';
      return;
    }

    const payload = {
      usuario: this.username,
      email: this.email,
      password: this.password,
      baseUrl: this.getAppBaseUrl()
    };

    this.message = '';
    this.http.post(`${this.apiBase}/users/create`, payload, { responseType: 'text' })
      .subscribe({
        next: (res) => {
          this.message = res;
        },
        error: err => {
          console.error('Error al crear usuario:', err);
          this.message = 'Error al crear usuario: ' + (err.error || err.message);
        }
      });
  }

  /**
   * Redirige al usuario a la pantalla de inicio de sesión.
   */
  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  get passwordChecks(): {
    minLength: boolean;
    uppercase: boolean;
    lowercase: boolean;
    digit: boolean;
    special: boolean;
  } {
    const password = this.password || '';
    return {
      minLength: password.length >= 12,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      digit: /\d/.test(password),
      special: this.hasAllowedSpecialChar(password)
    };
  }

  /**
   * Comprueba si la contraseña cumple las reglas mínimas de seguridad definidas por la interfaz.
   * @param password Contraseña a validar.
   * @returns `true` cuando la contraseña satisface todos los requisitos.
   */
  private isSecurePassword(password: string): boolean {
    const checks = this.passwordChecksFor(password || '');
    return checks.minLength && checks.uppercase && checks.lowercase && checks.digit && checks.special;
  }

  /**
   * Calcula el estado detallado de cada regla de validación de contraseña.
   * @param password Contraseña sobre la que se evaluarán las comprobaciones.
   * @returns Objeto con el resultado de cada restricción de seguridad.
   */
  private passwordChecksFor(password: string): {
    minLength: boolean;
    uppercase: boolean;
    lowercase: boolean;
    digit: boolean;
    special: boolean;
  } {
    return {
      minLength: password.length >= 12,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      digit: /\d/.test(password),
      special: this.hasAllowedSpecialChar(password)
    };
  }

  /**
   * Verifica si la contraseña contiene alguno de los caracteres especiales permitidos.
   * @param password Contraseña que se desea inspeccionar.
   * @returns `true` si contiene al menos un carácter especial admitido.
   */
  private hasAllowedSpecialChar(password: string): boolean {
    return [...password].some(char => this.allowedSpecialChars.includes(char));
  }

  /**
   * Obtiene la URL base de la aplicación para construir enlaces de verificación.
   * @returns URL base calculada a partir de la localización actual del navegador.
   */
  private getAppBaseUrl(): string {
    const path = window.location.pathname || '';
    const basePath = path.startsWith('/irpfhelper') ? '/irpfhelper' : '';
    return `${window.location.origin}${basePath}`;
  }
}
