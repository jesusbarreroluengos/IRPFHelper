import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmDialogService } from '../services/confirm-dialog.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="confirm-overlay" *ngIf="visible" (click)="cancelar()">
      <div class="confirm-box" (click)="$event.stopPropagation()">
        <div class="confirm-icon">⚠️</div>
        <p class="confirm-message">{{ message }}</p>
        <div class="confirm-actions">
          <button class="btn btn-danger" (click)="confirmar()">Aceptar</button>
          <button class="btn btn-secondary" (click)="cancelar()">Cancelar</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .confirm-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.45);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
    }
    .confirm-box {
      background: #fff;
      border-radius: 12px;
      padding: 32px 36px;
      max-width: 460px;
      width: 90%;
      box-shadow: 0 8px 32px rgba(0,0,0,0.18);
      text-align: center;
    }
    .confirm-icon {
      font-size: 2.4rem;
      margin-bottom: 12px;
    }
    .confirm-message {
      font-size: 1rem;
      color: #333;
      white-space: pre-line;
      margin-bottom: 28px;
      line-height: 1.6;
    }
    .confirm-actions {
      display: flex;
      gap: 16px;
      justify-content: center;
    }
    .btn {
      padding: 10px 28px;
      border: none;
      border-radius: 8px;
      font-size: 0.95rem;
      cursor: pointer;
      font-weight: 600;
      transition: opacity 0.2s;
    }
    .btn:hover { opacity: 0.85; }
    .btn-danger { background: #dc3545; color: #fff; }
    .btn-secondary { background: #6c757d; color: #fff; }
  `]
})
export class ConfirmDialogComponent implements OnInit {
  visible = false;
  message = '';
  private resolveFn?: (value: boolean) => void;

  constructor(private confirmDialogService: ConfirmDialogService) {}

  ngOnInit(): void {
    this.confirmDialogService.dialog$.subscribe(config => {
      if (config) {
        this.message = config.message;
        this.resolveFn = config.resolve;
        this.visible = true;
      }
    });
  }

  confirmar(): void {
    this.visible = false;
    this.resolveFn?.(true);
  }

  cancelar(): void {
    this.visible = false;
    this.resolveFn?.(false);
  }
}
