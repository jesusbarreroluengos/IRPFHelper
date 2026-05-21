import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
/**
 * Guarda de navegación que verifica la existencia de una sesión local válida
 * antes de conceder acceso a las rutas protegidas.
 */
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  /**
   * Decide si la ruta puede activarse en función de la existencia de una sesión local.
   * @returns `true` si hay usuario autenticado o una redirección al login en caso contrario.
   */
  canActivate(): boolean | UrlTree {
    const username = localStorage.getItem('username');

    if (username) {
      return true;
    }

    return this.router.parseUrl('/login');
  }
}
