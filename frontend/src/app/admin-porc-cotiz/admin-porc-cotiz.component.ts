import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { PersonaSimulada } from '../services/persona-simulada.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';
import { TablasMaestrasService, TaPorcCotiz } from '../services/tablas-maestras.service';
import { ConfirmDialogService } from '../services/confirm-dialog.service';

interface AyudaContextual { titulo: string; descripcion: string; puntos?: string[]; }

@Component({
  selector: 'app-admin-porc-cotiz',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-porc-cotiz.component.html'
})
export class AdminPorcCotizComponent implements OnInit {
  currentUsername: string | null = null;
  usernameFormatted: string | null = null;
  menuItems: any[] = [];
  isAdmin = false;
  personaSeleccionada: PersonaSimulada | null = null;
  canSwitchDocente = false;

  registros: TaPorcCotiz[] = [];
  formulario: Partial<TaPorcCotiz> = {};
  importeEdicion: { [anio: number]: number | null } = {};

  mensaje = '';
  tipoMensaje: 'success' | 'error' = 'success';
  mostrarMensaje = false;

  ayudaActivaKey = 'general';
  readonly ayudas: Record<string, AyudaContextual> = {
    general: {
      titulo: 'Porcentaje de Cotización',
      descripcion: 'Administra el porcentaje de cotización a la Seguridad Social aplicable a cada ejercicio.',
      puntos: [
        'El porcentaje se expresa con hasta 5 decimales (formato 99,99999)',
        'Solo puede existir un porcentaje por año',
        'El año actual está preseleccionado en el formulario de alta'
      ]
    },
    anio: { titulo: 'Año', descripcion: 'Año al que aplica el porcentaje de cotización. Solo puede existir un registro por año.' },
    porcentaje: { titulo: 'Porcentaje', descripcion: 'Porcentaje de cotización. Ejemplo: 6,35 para el 6,35%. Admite hasta 5 decimales.' }
  };

  aniosDisponibles: number[] = [];
  anioActual = new Date().getFullYear();

  constructor(
    private authService: AuthService,
    private router: Router,
    private personaSelectionService: PersonaSimuladaSelectionService,
    private service: TablasMaestrasService,
    private confirmDialogService: ConfirmDialogService
  ) {}

  ngOnInit(): void {
    this.currentUsername = this.authService.getUsername();
    this.usernameFormatted = this.formatCamelCase(this.currentUsername || '');
    this.updateMenuItems();
    this.personaSelectionService.personaSeleccionada$.subscribe(p => { this.personaSeleccionada = p; });
    this.generarAnios();
    this.formulario.anio = this.anioActual;
    this.cargarDatos();
  }

  private generarAnios(): void {
    const base = this.anioActual;
    for (let i = base + 2; i >= base - 10; i--) {
      this.aniosDisponibles.push(i);
    }
  }

  cargarDatos(): void {
    this.service.getPorcCotiz().subscribe({
      next: data => {
        this.registros = data.sort((a, b) => b.anio - a.anio);
        this.importeEdicion = {};
        this.registros.forEach(r => this.importeEdicion[r.anio] = r.porcentaje);
      },
      error: () => this.mostrarError('Error al cargar los datos')
    });
  }

  alta(): void {
    if (!this.formulario.anio) { this.mostrarError('El año es obligatorio'); return; }
    if (this.formulario.porcentaje == null) { this.mostrarError('El porcentaje es obligatorio'); return; }
    if (+this.formulario.porcentaje <= 0) { this.mostrarError('El porcentaje debe ser mayor que cero'); return; }
    this.service.createPorcCotiz(this.formulario as TaPorcCotiz).subscribe({
      next: () => {
        this.mostrarExito('Registro creado correctamente');
        this.formulario = { anio: this.anioActual };
        this.cargarDatos();
      },
      error: err => this.mostrarError(err.error || 'Error al crear el registro')
    });
  }

  guardarEdicion(r: TaPorcCotiz): void {
    const val = this.importeEdicion[r.anio];
    if (val == null || +val <= 0) { this.mostrarError('El porcentaje debe ser mayor que cero'); return; }
    this.service.updatePorcCotiz(r.anio, { ...r, porcentaje: +val }).subscribe({
      next: () => { this.mostrarExito('Registro modificado correctamente'); this.cargarDatos(); },
      error: err => this.mostrarError(err.error || 'Error al modificar el registro')
    });
  }

  async eliminar(r: TaPorcCotiz): Promise<void> {
    if (!await this.confirmDialogService.confirm(`¿Desea eliminar el registro del año ${r.anio}?`)) return;
    this.service.deletePorcCotiz(r.anio).subscribe({
      next: () => { this.mostrarExito('Registro eliminado correctamente'); this.cargarDatos(); },
      error: () => this.mostrarError('Error al eliminar el registro')
    });
  }



  setAyudaActiva(key: string): void { this.ayudaActivaKey = key; }
  restablecerAyudaGeneral(): void { this.ayudaActivaKey = 'general'; }
  get ayudaActiva(): AyudaContextual { return this.ayudas[this.ayudaActivaKey] || this.ayudas['general']; }

  private mostrarExito(msg: string): void { this.mensaje = msg; this.tipoMensaje = 'success'; this.mostrarMensaje = true; }
  private mostrarError(msg: string): void { this.mensaje = msg; this.tipoMensaje = 'error'; this.mostrarMensaje = true; }
  cerrarMensaje(): void { this.mostrarMensaje = false; }

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
    this.menuItems = allMenuItems.filter(item => !item.requiresAdmin || this.isAdmin);
  }

  navigateTo(route: string): void { this.router.navigate([route]); }
  navigateToDocenteSelector(): void { this.router.navigate(['/seleccion-docente']); }
  private formatCamelCase(name: string): string {
    return name ? name.charAt(0).toUpperCase() + name.slice(1).toLowerCase() : '';
  }
}
