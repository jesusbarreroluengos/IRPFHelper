import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { PersonaSimulada } from './persona-simulada.service';

@Injectable({
  providedIn: 'root'
})
/**
 * Servicio de estado compartido que difunde la persona simulada actualmente
 * seleccionada entre los distintos componentes de la interfaz.
 */
export class PersonaSimuladaSelectionService {
  private personaSeleccionadaSubject = new BehaviorSubject<PersonaSimulada | null>(null);
  public personaSeleccionada$ = this.personaSeleccionadaSubject.asObservable();

  constructor() {
    // Cargar el docente seleccionado desde localStorage al inicializar
    this.cargarDesdeLocalStorage();
  }

  /**
   * Actualiza la persona simulada activa y sincroniza la selección con `localStorage`.
   * @param persona Persona seleccionada o `null` para limpiar la selección actual.
   */
  setPersonaSeleccionada(persona: PersonaSimulada | null): void {
    this.personaSeleccionadaSubject.next(persona);
    
    if (persona) {
      localStorage.setItem('personaSimuladaSeleccionada', JSON.stringify(persona));
    } else {
      localStorage.removeItem('personaSimuladaSeleccionada');
    }
  }

  /**
   * Devuelve la persona simulada actualmente compartida por el servicio.
   * @returns Persona seleccionada o `null` si no existe una selección activa.
   */
  getPersonaSeleccionada(): PersonaSimulada | null {
    return this.personaSeleccionadaSubject.value;
  }

  /**
   * Restaura la última persona simulada persistida en el navegador.
   */
  private cargarDesdeLocalStorage(): void {
    const personaGuardada = localStorage.getItem('personaSimuladaSeleccionada');
    if (personaGuardada) {
      try {
        const persona = JSON.parse(personaGuardada);
        this.personaSeleccionadaSubject.next(persona);
      } catch (error) {
        console.error('Error parsing persona from localStorage:', error);
        localStorage.removeItem('personaSimuladaSeleccionada');
      }
    }
  }
}

