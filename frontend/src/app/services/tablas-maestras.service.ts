import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TaPorcCotiz {
  anio: number;
  porcentaje: number;
}

export interface TaDestino {
  anio: number;
  codEstudio: string;
  importe: number;
  estudio?: { codEstudio: string; descEstudio: string };
}

export interface TaEspecifico {
  anio: number;
  codEstudio: string;
  idComunidad: number;
  importe: number;
  estudio?: { codEstudio: string; descEstudio: string };
  comunidad?: { idComunidad: number; descComunidad: string };
}

export interface TaSexenio {
  anio: number;
  numSexenio: string;
  idComunidad: number;
  importe: number;
}

export interface TaSueldo {
  anio: number;
  codEstudio: string;
  importe: number;
  estudio?: { codEstudio: string; descEstudio: string };
}

export interface TaSueldoExtra {
  anio: number;
  codEstudio: string;
  importe: number;
  estudio?: { codEstudio: string; descEstudio: string };
}

export interface TaTrienio {
  anio: number;
  codEstudio: string;
  importe: number;
  estudio?: { codEstudio: string; descEstudio: string };
}

export interface TaTrienioExtra {
  anio: number;
  codEstudio: string;
  importe: number;
  estudio?: { codEstudio: string; descEstudio: string };
}

export interface Estudio {
  codEstudio: string;
  descEstudio: string;
}

export interface Comunidad {
  idComunidad: number;
  descComunidad: string;
}

@Injectable({
  providedIn: 'root'
})
export class TablasMaestrasService {
  private api = environment.apiUrl || '/api';

  constructor(private http: HttpClient) {}

  // --- ta_porc_cotiz ---
  getPorcCotiz(): Observable<TaPorcCotiz[]> {
    return this.http.get<TaPorcCotiz[]>(`${this.api}/admin/porc-cotiz/all`, { withCredentials: true });
  }
  createPorcCotiz(entity: TaPorcCotiz): Observable<any> {
    return this.http.post(`${this.api}/admin/porc-cotiz/create`, entity, { withCredentials: true });
  }
  updatePorcCotiz(anio: number, entity: TaPorcCotiz): Observable<any> {
    return this.http.put(`${this.api}/admin/porc-cotiz/update/${anio}`, entity, { withCredentials: true });
  }
  deletePorcCotiz(anio: number): Observable<any> {
    return this.http.delete(`${this.api}/admin/porc-cotiz/delete/${anio}`, { withCredentials: true, responseType: 'text' });
  }

  // --- ta_destino ---
  getDestino(): Observable<TaDestino[]> {
    return this.http.get<TaDestino[]>(`${this.api}/admin/destino/all`, { withCredentials: true });
  }
  createDestino(entity: TaDestino): Observable<any> {
    return this.http.post(`${this.api}/admin/destino/create`, entity, { withCredentials: true });
  }
  updateDestino(anio: number, codEstudio: string, body: { importe: number }): Observable<any> {
    return this.http.put(`${this.api}/admin/destino/update/${anio}/${codEstudio}`, body, { withCredentials: true });
  }
  deleteDestino(anio: number, codEstudio: string): Observable<any> {
    return this.http.delete(`${this.api}/admin/destino/delete/${anio}/${codEstudio}`, { withCredentials: true, responseType: 'text' });
  }

  // --- ta_especifico ---
  getEspecifico(): Observable<TaEspecifico[]> {
    return this.http.get<TaEspecifico[]>(`${this.api}/admin/especifico/all`, { withCredentials: true });
  }
  createEspecifico(entity: TaEspecifico): Observable<any> {
    return this.http.post(`${this.api}/admin/especifico/create`, entity, { withCredentials: true });
  }
  updateEspecifico(anio: number, codEstudio: string, idComunidad: number, body: { importe: number }): Observable<any> {
    return this.http.put(`${this.api}/admin/especifico/update/${anio}/${codEstudio}/${idComunidad}`, body, { withCredentials: true });
  }
  deleteEspecifico(anio: number, codEstudio: string, idComunidad: number): Observable<any> {
    return this.http.delete(`${this.api}/admin/especifico/delete/${anio}/${codEstudio}/${idComunidad}`, { withCredentials: true, responseType: 'text' });
  }

  // --- ta_sexenio ---
  getSexenio(): Observable<TaSexenio[]> {
    return this.http.get<TaSexenio[]>(`${this.api}/admin/sexenio/all`, { withCredentials: true });
  }
  createSexenio(entity: TaSexenio): Observable<any> {
    return this.http.post(`${this.api}/admin/sexenio/create`, entity, { withCredentials: true });
  }
  updateSexenio(anio: number, numSexenio: string, idComunidad: number, body: { importe: number }): Observable<any> {
    return this.http.put(`${this.api}/admin/sexenio/update/${anio}/${numSexenio}/${idComunidad}`, body, { withCredentials: true });
  }
  deleteSexenio(anio: number, numSexenio: string, idComunidad: number): Observable<any> {
    return this.http.delete(`${this.api}/admin/sexenio/delete/${anio}/${numSexenio}/${idComunidad}`, { withCredentials: true, responseType: 'text' });
  }

  // --- ta_sueldo ---
  getSueldo(): Observable<TaSueldo[]> {
    return this.http.get<TaSueldo[]>(`${this.api}/admin/sueldo/all`, { withCredentials: true });
  }
  createSueldo(entity: TaSueldo): Observable<any> {
    return this.http.post(`${this.api}/admin/sueldo/create`, entity, { withCredentials: true });
  }
  updateSueldo(anio: number, codEstudio: string, body: { importe: number }): Observable<any> {
    return this.http.put(`${this.api}/admin/sueldo/update/${anio}/${codEstudio}`, body, { withCredentials: true });
  }
  deleteSueldo(anio: number, codEstudio: string): Observable<any> {
    return this.http.delete(`${this.api}/admin/sueldo/delete/${anio}/${codEstudio}`, { withCredentials: true, responseType: 'text' });
  }

  // --- ta_sueldo_extra ---
  getSueldoExtra(): Observable<TaSueldoExtra[]> {
    return this.http.get<TaSueldoExtra[]>(`${this.api}/admin/sueldo-extra/all`, { withCredentials: true });
  }
  createSueldoExtra(entity: TaSueldoExtra): Observable<any> {
    return this.http.post(`${this.api}/admin/sueldo-extra/create`, entity, { withCredentials: true });
  }
  updateSueldoExtra(anio: number, codEstudio: string, body: { importe: number }): Observable<any> {
    return this.http.put(`${this.api}/admin/sueldo-extra/update/${anio}/${codEstudio}`, body, { withCredentials: true });
  }
  deleteSueldoExtra(anio: number, codEstudio: string): Observable<any> {
    return this.http.delete(`${this.api}/admin/sueldo-extra/delete/${anio}/${codEstudio}`, { withCredentials: true, responseType: 'text' });
  }

  // --- ta_trienio ---
  getTrienio(): Observable<TaTrienio[]> {
    return this.http.get<TaTrienio[]>(`${this.api}/admin/trienio/all`, { withCredentials: true });
  }
  createTrienio(entity: TaTrienio): Observable<any> {
    return this.http.post(`${this.api}/admin/trienio/create`, entity, { withCredentials: true });
  }
  updateTrienio(anio: number, codEstudio: string, body: { importe: number }): Observable<any> {
    return this.http.put(`${this.api}/admin/trienio/update/${anio}/${codEstudio}`, body, { withCredentials: true });
  }
  deleteTrienio(anio: number, codEstudio: string): Observable<any> {
    return this.http.delete(`${this.api}/admin/trienio/delete/${anio}/${codEstudio}`, { withCredentials: true, responseType: 'text' });
  }

  // --- ta_trienio_extra ---
  getTrienioExtra(): Observable<TaTrienioExtra[]> {
    return this.http.get<TaTrienioExtra[]>(`${this.api}/admin/trienio-extra/all`, { withCredentials: true });
  }
  createTrienioExtra(entity: TaTrienioExtra): Observable<any> {
    return this.http.post(`${this.api}/admin/trienio-extra/create`, entity, { withCredentials: true });
  }
  updateTrienioExtra(anio: number, codEstudio: string, body: { importe: number }): Observable<any> {
    return this.http.put(`${this.api}/admin/trienio-extra/update/${anio}/${codEstudio}`, body, { withCredentials: true });
  }
  deleteTrienioExtra(anio: number, codEstudio: string): Observable<any> {
    return this.http.delete(`${this.api}/admin/trienio-extra/delete/${anio}/${codEstudio}`, { withCredentials: true, responseType: 'text' });
  }

  // --- catálogos comunes ---
  getEstudios(): Observable<Estudio[]> {
    return this.http.get<Estudio[]>(`${this.api}/admin/destino/estudios`, { withCredentials: true });
  }
  getComunidades(): Observable<Comunidad[]> {
    return this.http.get<Comunidad[]>(`${this.api}/comunidades/all`, { withCredentials: true });
  }
}
