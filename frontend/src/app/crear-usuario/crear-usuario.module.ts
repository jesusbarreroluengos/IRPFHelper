import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { CrearUsuarioComponent } from './crear-usuario.component';

@NgModule({
  declarations: [
    CrearUsuarioComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule
  ],
  exports: [
    CrearUsuarioComponent
  ]
})
/**
 * Módulo orientado al alta de nuevos usuarios en la aplicación.
 */
export class CrearUsuarioModule { }

