import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { PersonaSimulada } from '../services/persona-simulada.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';

@Component({
  selector: 'app-administracion',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './administracion.component.html'
})
export class AdministracionComponent implements OnInit {
  currentUsername: string | null = null;
  usernameFormatted: string | null = null;
  menuItems: any[] = [];
  isAdmin = false;
  personaSeleccionada: PersonaSimulada | null = null;
  canSwitchDocente = false;

  readonly opciones = [
    { titulo: 'Administración de Usuarios', descripcion: 'Gestión de usuarios de la aplicación', icono: '👥', ruta: '/usuarios' },
    { titulo: 'Porcentaje de Cotización', descripcion: 'Tabla de porcentajes de cotización por año', icono: '📊', ruta: '/admin/porc-cotiz' },
    { titulo: 'Complemento de Destino', descripcion: 'Tabla de complemento de Destino por año y estudio', icono: '💰', ruta: '/admin/destino' },
    { titulo: 'Complemento Específico General Docente', descripcion: 'Tabla de Complemento Específico por año, estudio y comunidad', icono: '💰', ruta: '/admin/especifico' },
    { titulo: 'Sexenios', descripcion: 'Tabla de sexenios por año, número y comunidad', icono: '💰', ruta: '/admin/sexenio' },
    { titulo: 'Sueldo', descripcion: 'Tabla de sueldos por año y estudio', icono: '💰', ruta: '/admin/sueldo' },
    { titulo: 'Sueldo para Extra', descripcion: 'Tabla de sueldos para extra por año y estudio', icono: '💰', ruta: '/admin/sueldo-extra' },
    { titulo: 'Trienios', descripcion: 'Tabla de trienios por año y estudio', icono: '💰', ruta: '/admin/trienio' },
    { titulo: 'Trienios para Extra', descripcion: 'Tabla de trienios para extra por año y estudio', icono: '💰', ruta: '/admin/trienio-extra' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
    private personaSelectionService: PersonaSimuladaSelectionService
  ) {}

  ngOnInit(): void {
    this.currentUsername = this.authService.getUsername();
    this.usernameFormatted = this.formatCamelCase(this.currentUsername || '');
    this.isAdmin = this.authService.isAdmin();
    this.updateMenuItems();
    this.personaSelectionService.personaSeleccionada$.subscribe(p => {
      this.personaSeleccionada = p;
    });
  }

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
    this.menuItems = allMenuItems.filter(item => {
      if (item.requiresAdmin) return this.isAdmin;
      return true;
    });
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  navigateToDocenteSelector(): void {
    this.router.navigate(['/seleccion-docente']);
  }

  private formatCamelCase(name: string): string {
    return name ? name.charAt(0).toUpperCase() + name.slice(1).toLowerCase() : '';
  }
}
