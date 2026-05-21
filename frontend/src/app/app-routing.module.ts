import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './login/login.component';
import { PersonasComponent } from './personas/personas.component';
import { CrearUsuarioComponent } from './crear-usuario/crear-usuario.component';
import { Principal } from './principal/principal';
import { CambiarPasswordComponent } from './cambiar-password/cambiar-password';
import { IdentidadesSimuladasComponent } from './identidades-simuladas/identidades-simuladas.component';
import { AuthGuard } from './auth.guard';
import { PuestosTipoComponent } from './puestos-tipo/puestos-tipo.component';
import { ContratosComponent } from './contratos/contratos.component';
import { SimulacionRetribucionesComponent } from './simulacion-retribuciones/simulacion-retribuciones.component';
import { VerificarEmailComponent } from './verificar-email/verificar-email.component';
import { SelectorDocenteComponent } from './selector-docente/selector-docente.component';
import { AdministracionComponent } from './administracion/administracion.component';
import { AdminPorcCotizComponent } from './admin-porc-cotiz/admin-porc-cotiz.component';
import { AdminDestinoComponent } from './admin-destino/admin-destino.component';
import { AdminEspecificoComponent } from './admin-especifico/admin-especifico.component';
import { AdminSexenioComponent } from './admin-sexenio/admin-sexenio.component';
import { AdminSueldoComponent } from './admin-sueldo/admin-sueldo.component';
import { AdminSueldoExtraComponent } from './admin-sueldo-extra/admin-sueldo-extra.component';
import { AdminTrienioComponent } from './admin-trienio/admin-trienio.component';
import { AdminTrienioExtraComponent } from './admin-trienio-extra/admin-trienio-extra.component';

/**
 * Definición centralizada de rutas.
 * Este conjunto organiza la navegación entre vistas y aplica las restricciones
 * de acceso en aquellos apartados que requieren autenticación previa.
 */
const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' }, // ruta raíz redirige a login
  { path: 'login', component: LoginComponent },
  { path: 'principal', component: Principal, canActivate: [AuthGuard] },
  { path: 'personas', component: PersonasComponent, canActivate: [AuthGuard] },
  { path: 'crear-usuario', component: CrearUsuarioComponent  },
  { path: 'usuarios/crear', component: CrearUsuarioComponent },
  { path: 'verificar-email', component: VerificarEmailComponent },
  { path: 'usuarios', component: PersonasComponent, canActivate: [AuthGuard] }, // Navega directamente a la lista de usuarios
  { path: 'datos-personales', component: IdentidadesSimuladasComponent, canActivate: [AuthGuard] },
  { path: 'datos-economicos', component: Principal, canActivate: [AuthGuard] },
  { path: 'prevision-ingresos', component: Principal, canActivate: [AuthGuard] },
  { path: 'puestos-tipo', component: PuestosTipoComponent, canActivate: [AuthGuard] },
  { path: 'contratos', component: ContratosComponent, canActivate: [AuthGuard] },
  { path: 'simulacion', component: SimulacionRetribucionesComponent, canActivate: [AuthGuard] },
  { path: 'seleccion-docente', component: SelectorDocenteComponent, canActivate: [AuthGuard] },
  { path: 'resultados', component: Principal, canActivate: [AuthGuard] },
  { path: 'cambiar-password', component: CambiarPasswordComponent, canActivate: [AuthGuard] },
  { path: 'administracion', component: AdministracionComponent, canActivate: [AuthGuard] },
  { path: 'admin/porc-cotiz', component: AdminPorcCotizComponent, canActivate: [AuthGuard] },
  { path: 'admin/destino', component: AdminDestinoComponent, canActivate: [AuthGuard] },
  { path: 'admin/especifico', component: AdminEspecificoComponent, canActivate: [AuthGuard] },
  { path: 'admin/sexenio', component: AdminSexenioComponent, canActivate: [AuthGuard] },
  { path: 'admin/sueldo', component: AdminSueldoComponent, canActivate: [AuthGuard] },
  { path: 'admin/sueldo-extra', component: AdminSueldoExtraComponent, canActivate: [AuthGuard] },
  { path: 'admin/trienio', component: AdminTrienioComponent, canActivate: [AuthGuard] },
  { path: 'admin/trienio-extra', component: AdminTrienioExtraComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
/**
 * Módulo de enrutamiento responsable de encapsular la configuración de
 * navegación de la aplicación.
 */
export class AppRoutingModule { }
