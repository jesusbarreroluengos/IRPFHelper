import { ErrorHandler, LOCALE_ID, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';

registerLocaleData(localeEs);

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { LoginModule } from './login/login.module';
import { PersonasModule } from './personas/personas.module';
import { CrearUsuarioModule } from './crear-usuario/crear-usuario.module';
import { Principal } from './principal/principal';
import { CambiarPasswordComponent } from './cambiar-password/cambiar-password';
import { FrontendLoggingInterceptor } from './services/frontend-logging.interceptor';
import { GlobalErrorHandler } from './services/global-error-handler.service';
import { VerificarEmailComponent } from './verificar-email/verificar-email.component';
import { ConfirmDialogComponent } from './confirm-dialog/confirm-dialog.component';

@NgModule({
  declarations: [
    AppComponent,
    VerificarEmailComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    AppRoutingModule,
    LoginModule,
    PersonasModule,
    CrearUsuarioModule,
    Principal,
    CambiarPasswordComponent,
    ConfirmDialogComponent

  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: FrontendLoggingInterceptor, multi: true },
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
    { provide: LOCALE_ID, useValue: 'es' }
  ],
  bootstrap: [AppComponent]
})
/**
 * Módulo raíz de la aplicación.
 * Integra los módulos funcionales, registra los servicios transversales y
 * establece el punto de arranque del cliente Angular.
 */
export class AppModule { }

