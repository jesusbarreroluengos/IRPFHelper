import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario } from '../models/usuario.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
/**
 * Servicio de acceso a datos que encapsula las operaciones CRUD asociadas a
 * la entidad de usuario.
 */
export class UsuarioService {
  private apiBase = environment.apiUrl || '/api';
  private baseUrl = `${this.apiBase}/users`;

  constructor(private http: HttpClient) {}

  /**
   * Recupera el listado completo de usuarios registrados en la aplicación.
   * @returns Observable con la colección de usuarios devuelta por la API.
   */
  getUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.baseUrl}/all`);
  }

  /**
   * Elimina un usuario existente por su identificador.
   * @param id Identificador del usuario que se desea borrar.
   * @returns Observable con la respuesta textual normalizada por Angular.
   */
  deleteUsuario(id: number): Observable<any> {
    console.log('Enviando petición DELETE a:', `${this.baseUrl}/delete/${id}`);
    return this.http.delete<any>(`${this.baseUrl}/delete/${id}`, {
      responseType: 'text' as 'json'
    });
  }
}
