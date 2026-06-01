import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { PersonaSimulada } from '../services/persona-simulada.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';
import { TablasMaestrasService, TaSexenio, Comunidad } from '../services/tablas-maestras.service';
import { ConfirmDialogService } from '../services/confirm-dialog.service';

interface AyudaContextual { titulo: string; descripcion: string; puntos?: string[]; }

@Component({
  selector: 'app-admin-sexenio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-sexenio.component.html'
})
export class AdminSexenioComponent implements OnInit {
  currentUsername: string | null = null;
  usernameFormatted: string | null = null;
  menuItems: any[] = [];
  isAdmin = false;
  personaSeleccionada: PersonaSimulada | null = null;
  canSwitchDocente = false;

  registros: TaSexenio[] = [];
  comunidades: Comunidad[] = [];
  formulario: Partial<TaSexenio> = {};
  importeEdicion: { [key: string]: number | null } = {};

  filtroAnio: number | '' = '';
  filtroIdComunidad: number | '' = '';

  get registrosFiltrados(): TaSexenio[] {
    return this.registros.filter(r =>
      (this.filtroAnio === '' || r.anio === +this.filtroAnio) &&
      (this.filtroIdComunidad === '' || r.idComunidad === +this.filtroIdComunidad)
    );
  }

  mensaje = '';
  tipoMensaje: 'success' | 'error' = 'success';
  mostrarMensaje = false;

  readonly numSexeniosOpciones = ['1', '2', '3', '4', '5'];

  ayudaActivaKey = 'general';
  readonly ayudas: Record<string, AyudaContextual> = {
    general: {
      titulo: 'Sexenios',
      descripcion: 'Administra el importe de cada sexenio por año, número de sexenio y comunidad autónoma.',
      puntos: [
        'Solo puede existir un registro por combinación de año, número de sexenio y comunidad',
        'El importe debe ser mayor que cero'
      ]
    },
    anio: { titulo: 'Año', descripcion: 'Año al que aplica el importe.' },
    numSexenio: { titulo: 'Número de Sexenio', descripcion: 'Número ordinal del sexenio (del 1 al 5).' },
    idComunidad: { titulo: 'Comunidad Autónoma', descripcion: 'Comunidad autónoma a la que aplica el importe.' },
    importe: { titulo: 'Importe', descripcion: 'Importe mensual del sexenio en euros. Debe ser mayor que cero.' }
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
    this.service.getComunidades().subscribe({ next: d => this.comunidades = d.sort((a, b) => a.idComunidad - b.idComunidad) });
    this.cargarDatos();
  }

  private generarAnios(): void {
    const base = this.anioActual;
    for (let i = base + 2; i >= base - 10; i--) this.aniosDisponibles.push(i);
  }

  cargarDatos(): void {
    this.service.getSexenio().subscribe({
      next: data => {
        this.registros = data.sort((a, b) => b.anio - a.anio || a.numSexenio.localeCompare(b.numSexenio) || a.idComunidad - b.idComunidad);
        this.importeEdicion = {};
        this.registros.forEach(r => this.importeEdicion[this.clave(r)] = r.importe);
      },
      error: () => this.mostrarError('Error al cargar los datos')
    });
  }

  alta(): void {
    if (!this.formulario.anio) { this.mostrarError('El año es obligatorio'); return; }
    if (!this.formulario.numSexenio) { this.mostrarError('El número de sexenio es obligatorio'); return; }
    if (!this.formulario.idComunidad) { this.mostrarError('La comunidad autónoma es obligatoria'); return; }
    if (!this.formulario.importe || +this.formulario.importe <= 0) { this.mostrarError('El importe debe ser mayor que cero'); return; }
    this.service.createSexenio(this.formulario as TaSexenio).subscribe({
      next: () => { this.mostrarExito('Registro creado correctamente'); this.formulario = { anio: this.anioActual }; this.cargarDatos(); },
      error: err => this.mostrarError(err.error || 'Error al crear el registro')
    });
  }

  clave(r: TaSexenio): string { return `${r.anio}_${r.numSexenio}_${r.idComunidad}`; }
  guardarEdicion(r: TaSexenio): void {
    const val = this.importeEdicion[this.clave(r)];
    if (!val || +val <= 0) { this.mostrarError('El importe debe ser mayor que cero'); return; }
    this.service.updateSexenio(r.anio, r.numSexenio, r.idComunidad, { importe: +val }).subscribe({
      next: () => { this.mostrarExito('Registro modificado correctamente'); this.cargarDatos(); },
      error: err => this.mostrarError(err.error || 'Error al modificar el registro')
    });
  }
  async eliminar(r: TaSexenio): Promise<void> {
    if (!await this.confirmDialogService.confirm(`¿Desea eliminar el registro del año ${r.anio}?`)) return;
    this.service.deleteSexenio(r.anio, r.numSexenio, r.idComunidad).subscribe({
      next: () => { this.mostrarExito('Registro eliminado correctamente'); this.cargarDatos(); },
      error: () => this.mostrarError('Error al eliminar el registro')
    });
  }

  descComunidad(id: number): string { return this.comunidades.find(c => c.idComunidad === id)?.descComunidad || String(id); }

  setAyudaActiva(key: string): void { this.ayudaActivaKey = key; }
  restablecerAyudaGeneral(): void { this.ayudaActivaKey = 'general'; }
  get ayudaActiva(): AyudaContextual { return this.ayudas[this.ayudaActivaKey] || this.ayudas['general']; }

  private mostrarExito(msg: string): void { this.mensaje = msg; this.tipoMensaje = 'success'; this.mostrarMensaje = true; }
  private mostrarError(msg: string): void { this.mensaje = msg; this.tipoMensaje = 'error'; this.mostrarMensaje = true; }
  cerrarMensaje(): void { this.mostrarMensaje = false; }

  updateMenuItems(): void {
    this.isAdmin = this.authService.isAdmin();
    const all = [
      { name: 'Inicio', route: '/principal', icon: '🏠' },
      { name: 'Administración', route: '/administracion', icon: '⚙️', requiresAdmin: true },
      { name: 'Docentes Simulados', route: '/datos-personales', icon: '👤' },
      { name: 'Puestos Tipo', route: '/puestos-tipo', icon: '🧑‍🏫' },
      { name: 'Contratos', route: '/contratos', icon: '📄' },
      { name: 'Simulación IRPF', route: '/simulacion', icon: '💰' }
    ];
    this.menuItems = all.filter(item => !item.requiresAdmin || this.isAdmin);
  }
  navigateTo(route: string): void { this.router.navigate([route]); }
  navigateToDocenteSelector(): void { this.router.navigate(['/seleccion-docente']); }
  private formatCamelCase(name: string): string {
    return name ? name.charAt(0).toUpperCase() + name.slice(1).toLowerCase() : '';
  }
}

