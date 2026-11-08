import { DOCUMENT } from '@angular/common';
import { inject, Injectable, signal } from '@angular/core';

export type AppTheme = 'light' | 'dark';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly storageKey = 'cashiogram-theme';

  readonly theme = signal<AppTheme>(this.getSavedTheme());

  constructor() {
    this.applyTheme(this.theme());
  }

  toggleTheme(): void {
    const nextTheme = this.theme() === 'light' ? 'dark' : 'light';

    this.theme.set(nextTheme);
    this.applyTheme(nextTheme);

    this.document.defaultView?.localStorage.setItem(this.storageKey, nextTheme);
  }

  private getSavedTheme(): AppTheme {
    const saved = this.document.defaultView?.localStorage.getItem(this.storageKey);

    return saved === 'dark' ? 'dark' : 'light';
  }

  private applyTheme(theme: AppTheme): void {
    this.document.documentElement.setAttribute('data-theme', theme);
  }
}
