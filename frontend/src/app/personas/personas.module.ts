import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PersonasComponent } from './personas.component';

@NgModule({
  declarations: [
    PersonasComponent
  ],
  imports: [
    CommonModule
  ],
  exports: [
    PersonasComponent
  ]
})
/**
 * Módulo funcional asociado a la gestión administrativa de usuarios dentro del
 * sistema.
 */
export class PersonasModule { }

