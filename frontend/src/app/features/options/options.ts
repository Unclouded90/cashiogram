import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ThemeService } from '../../core/theme/theme.service';

@Component({
  selector: 'app-options',
  imports: [RouterLink],
  templateUrl: './options.html',
  styleUrl: './options.scss',
})
export class Options {
  protected readonly themeService = inject(ThemeService);
  protected readonly open = signal(false);

  protected toggle(): void {
    this.open.update((open) => !open);
  }
}
