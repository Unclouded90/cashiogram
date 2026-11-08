import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

import { ThemeService } from './core/theme/theme.service';
import { Options } from './features/options/options';

@Component({
  imports: [RouterOutlet, RouterLink, Options],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  constructor() {
    inject(ThemeService);
  }
}
