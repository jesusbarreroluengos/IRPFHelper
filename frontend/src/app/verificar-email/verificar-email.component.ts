import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-verificar-email',
  standalone: false,
  templateUrl: './verificar-email.component.html',
  styleUrls: []
})
/**
 * Componente que materializa el flujo de confirmación del correo electrónico
 * mediante el procesamiento del token recibido.
 */
export class VerificarEmailComponent implements OnInit {
  loading = true;
  verificationSuccess = false;
  message = 'Procesando verificacion del correo...';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  /**
   * Lee el token de la URL y lanza la verificación del correo al cargar el componente.
   */
  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.loading = false;
      this.verificationSuccess = false;
      this.message = 'No se ha recibido un token de verificacion valido.';
      return;
    }

    this.authService.verifyEmailToken(token).subscribe({
      next: (response) => {
        this.loading = false;
        this.verificationSuccess = response.verified === 'S';
        this.message = response.message || 'Verificacion completada.';
      },
      error: (err) => {
        this.loading = false;
        this.verificationSuccess = false;
        this.message = this.parseBackendMessage(err);
      }
    });
  }

  /**
   * Redirige a la pantalla de login una vez resuelto el proceso de verificación.
   */
  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  /**
   * Extrae un mensaje legible a partir del error devuelto por la API de verificación.
   * @param err Error crudo recibido durante la verificación del correo.
   * @returns Texto listo para mostrarse al usuario.
   */
  private parseBackendMessage(err: any): string {
    if (!err) {
      return 'No se pudo validar el enlace de verificacion.';
    }

    if (typeof err.error === 'string') {
      try {
        const parsed = JSON.parse(err.error);
        return parsed?.message || err.error;
      } catch {
        return err.error;
      }
    }

    if (typeof err.error === 'object' && err.error !== null) {
      return err.error.message || 'No se pudo validar el enlace de verificacion.';
    }

    return 'No se pudo validar el enlace de verificacion.';
  }
}
