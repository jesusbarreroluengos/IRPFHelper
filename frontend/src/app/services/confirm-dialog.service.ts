import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface ConfirmDialogConfig {
  message: string;
  resolve: (value: boolean) => void;
}

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  readonly dialog$ = new Subject<ConfirmDialogConfig | null>();

  confirm(message: string): Promise<boolean> {
    return new Promise(resolve => {
      this.dialog$.next({ message, resolve });
    });
  }
}
