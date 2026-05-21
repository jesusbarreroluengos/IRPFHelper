import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map, of, switchMap, tap, throwError } from 'rxjs';
import { Usuario } from './models/usuario.model';
import { environment } from '../environments/environment';

interface AuthApiResponse {
  message: string;
  code?: string;
  verified?: string;
}

@Injectable({
  providedIn: 'root'
})
/**
 * Servicio de autenticación que coordina el inicio de sesión, la verificación
 * de correo y la persistencia del contexto de usuario.
 */
export class AuthService {
  private apiBase = environment.apiUrl || '/api';
  private loginUrl = `${this.apiBase}/auth/login`;
  private resendVerificationUrl = `${this.apiBase}/auth/resend-verification`;
  private verifyEmailUrl = `${this.apiBase}/auth/verify-email`;
  private usersUrl = `${this.apiBase}/users/all`;

  private currentUsername: string | null = null;
  private currentUser: Usuario | null = null;

  constructor(private http: HttpClient) {
    console.log('AuthService initialized');
  }

  /**
   * Autentica al usuario contra la API, recupera su perfil completo y persiste
   * los datos mínimos de sesión en memoria y en `localStorage`.
   * @param username Nombre de usuario introducido en el formulario de acceso.
   * @param password Contraseña asociada al usuario.
   * @returns Observable con el mensaje de confirmación del inicio de sesión.
   */
  login(username: string, password: string): Observable<string> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json; charset=utf-8'
    });

    return this.http.post<AuthApiResponse>(
      this.loginUrl,
      { username, password },
      { headers, withCredentials: true }
    ).pipe(
      tap(() => {
        this.currentUsername = username;
        localStorage.setItem('username', username);
      }),
      switchMap(() => this.http.get<Usuario[]>(this.usersUrl)),
      switchMap((usuarios) => {
        const usuarioActual = usuarios.find(u => u.usuario === username);

        if (!usuarioActual) {
          return throwError(() => ({
            status: 401,
            error: { message: 'Usuario no encontrado' }
          }));
        }

        if (usuarioActual.esverificado !== 'S') {
          this.clearUsername();
          return throwError(() => ({
            status: 403,
            error: {
              message: 'Debe verificar la direccion de correo antes de acceder.',
              code: 'EMAIL_NOT_VERIFIED'
            }
          }));
        }

        this.currentUser = usuarioActual;
        localStorage.setItem('currentUser', JSON.stringify(usuarioActual));
        return of('Login exitoso');
      }),
      map((mensaje) => mensaje)
    );
  }

  /**
   * Solicita al backend el reenvío del correo de verificación para un usuario.
   * @param username Nombre de usuario al que se reenviará el correo.
   * @param password Contraseña usada para revalidar la solicitud.
   * @param baseUrl URL base de la aplicación para construir el enlace de verificación.
   * @returns Observable con el mensaje devuelto por la API.
   */
  resendVerificationEmail(username: string, password: string, baseUrl: string): Observable<string> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json; charset=utf-8'
    });

    return this.http.post<AuthApiResponse>(
      this.resendVerificationUrl,
      { username, password, baseUrl },
      { headers, withCredentials: true }
    ).pipe(
      map((response) => response.message || 'Correo de verificacion reenviado')
    );
  }

  /**
   * Valida en el backend el token recibido por correo electrónico.
   * @param token Token de verificación incluido en el enlace enviado al usuario.
   * @returns Observable con la respuesta de verificación de la API.
   */
  verifyEmailToken(token: string): Observable<AuthApiResponse> {
    return this.http.get<AuthApiResponse>(
      `${this.verifyEmailUrl}?token=${encodeURIComponent(token)}`,
      { withCredentials: true }
    );
  }

  /**
   * Recupera el identificador textual del usuario autenticado.
   */
  getUsername(): string | null {
    if (!this.currentUsername) {
      this.currentUsername = localStorage.getItem('username');
    }
    return this.currentUsername;
  }

  /**
   * Recupera el perfil completo del usuario almacenado en memoria o en el
   * almacenamiento local del navegador.
   */
  getCurrentUser(): Usuario | null {
    if (!this.currentUser) {
      const userStr = localStorage.getItem('currentUser');
      if (userStr) {
        this.currentUser = JSON.parse(userStr);
      }
    }
    return this.currentUser;
  }

  /**
   * Obtiene el identificador numérico del usuario autenticado.
   */
  getUserId(): number | null {
    const user = this.getCurrentUser();
    return user ? user.id : null;
  }

  /**
   * Indica si el usuario actual dispone de privilegios de administración.
   */
  isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user ? user.esadmin === 'S' : false;
  }

  /**
   * Elimina la información de sesión persistida durante el cierre de sesión.
   */
  clearUsername(): void {
    this.currentUsername = null;
    this.currentUser = null;
    localStorage.removeItem('username');
    localStorage.removeItem('currentUser');
  }
}
