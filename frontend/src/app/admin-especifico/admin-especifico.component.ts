import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { PersonaSimulada } from '../services/persona-simulada.service';
import { PersonaSimuladaSelectionService } from '../services/persona-simulada-selection.service';
import { TablasMaestrasService, TaEspecifico, Estudio, Comunidad } from '../services/tablas-maestras.service';
import { ConfirmDialogService } from '../services/confirm-dialog.service';

interface AyudaContextual { titulo: string; descripcion: string; puntos?: string[]; }

@Component({
  selector: 'app-admin-especifico',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-especifico.component.html'
})
export class AdminEspecificoComponent implements OnInit {
  currentUsername: string | null = null;
  usernameFormatted: string | null = null;
  menuItems: any[] = [];
  isAdmin = false;
  personaSeleccionada: PersonaSimulada | null = null;
  canSwitchDocente = false;

  registros: TaEspecifico[] = [];
  estudios: Estudio[] = [];
  comunidades: Comunidad[] = [];
  formulario: Partial<TaEspecifico> = {};
  importeEdicion: { [key: string]: number | null } = {};

  filtroAnio: number | '' = '';
  filtroCodEstudio: string = '';
  filtroIdComunidad: number | '' = '';

  get registrosFiltrados(): TaEspecifico[] {
    return this.registros.filter(r =>
      (this.filtroAnio === '' || r.anio === +this.filtroAnio) &&
      (this.filtroCodEstudio === '' || r.codEstudio === this.filtroCodEstudio) &&
      (this.filtroIdComunidad === '' || r.idComunidad === +this.filtroIdComunidad)
    );
  }

  mensaje = '';
  tipoMensaje: 'success' | 'error' = 'success';
  mostrarMensaje = false;

  ayudaActivaKey = 'general';
  readonly ayudas: Record<string, AyudaContextual> = {
    general: {
      titulo: 'Complemento Específico General Docente',
      descripcion: 'Administra el importe del complemento específico general docente por año, tipo de docencia y comunidad autónoma.',
      puntos: [
        'Solo puede existir un registro por combinación de año, tipo de docencia y comunidad',
        'El importe debe ser mayor que cero'
      ]
    },
    anio: { titulo: 'Año', descripcion: 'Año al que aplica el importe.' },
    codEstudio: { titulo: 'Tipo de Docencia', descripcion: 'Tipo de docencia al que aplica el complemento.' },
    idComunidad: { titulo: 'Comunidad Autónoma', descripcion: 'Comunidad autónoma a la que aplica el complemento.' },
    importe: { titulo: 'Importe', descripcion: 'Importe mensual del complemento específico en euros. Debe ser mayor que cero.' }
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
    this.service.getEstudios().subscribe({ next: d => this.estudios = d });
    this.service.getComunidades().subscribe({ next: d => this.comunidades = d.sort((a, b) => a.idComunidad - b.idComunidad) });
    this.cargarDatos();
  }

  private generarAnios(): void {
    const base = this.anioActual;
    for (let i = base + 2; i >= base - 10; i--) this.aniosDisponibles.push(i);
  }

  cargarDatos(): void {
    this.service.getEspecifico().subscribe({
      next: data => {
        this.registros = data.sort((a, b) => b.anio - a.anio || a.codEstudio.localeCompare(b.codEstudio) || a.idComunidad - b.idComunidad);
        this.importeEdicion = {};
        this.registros.forEach(r => this.importeEdicion[this.clave(r)] = r.importe);
      },
      error: () => this.mostrarError('Error al cargar los datos')
    });
  }

  alta(): void {
    if (!this.formulario.anio) { this.mostrarError('El año es obligatorio'); return; }
    if (!this.formulario.codEstudio) { this.mostrarError('El tipo de docencia es obligatorio'); return; }
    if (!this.formulario.idComunidad) { this.mostrarError('La comunidad autónoma es obligatoria'); return; }
    if (!this.formulario.importe || +this.formulario.importe <= 0) { this.mostrarError('El importe debe ser mayor que cero'); return; }
    this.service.createEspecifico(this.formulario as TaEspecifico).subscribe({
      next: () => { this.mostrarExito('Registro creado correctamente'); this.formulario = { anio: this.anioActual }; this.cargarDatos(); },
      error: err => this.mostrarError(err.error || 'Error al crear el registro')
    });
  }

  clave(r: TaEspecifico): string { return `${r.anio}_${r.codEstudio}_${r.idComunidad}`; }
  guardarEdicion(r: TaEspecifico): void {
    const val = this.importeEdicion[this.clave(r)];
    if (!val || +val <= 0) { this.mostrarError('El importe debe ser mayor que cero'); return; }
    this.service.updateEspecifico(r.anio, r.codEstudio, r.idComunidad, { importe: +val }).subscribe({
      next: () => { this.mostrarExito('Registro modificado correctamente'); this.cargarDatos(); },
      error: err => this.mostrarError(err.error || 'Error al modificar el registro')
    });
  }
  async eliminar(r: TaEspecifico): Promise<void> {
    if (!await this.confirmDialogService.confirm(`¿Desea eliminar el registro del año ${r.anio} - ${r.codEstudio}?`)) return;
    this.service.deleteEspecifico(r.anio, r.codEstudio, r.idComunidad).subscribe({
      next: () => { this.mostrarExito('Registro eliminado correctamente'); this.cargarDatos(); },
      error: () => this.mostrarError('Error al eliminar el registro')
    });
  }

  descEstudio(cod: string): string { return this.estudios.find(e => e.codEstudio === cod)?.descEstudio || cod; }
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

