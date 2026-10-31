import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-options',
  imports: [RouterLink],
  templateUrl: './options.html',
  styleUrl: './options.scss',
})
export class Options {
  protected readonly open = signal(false);

  protected toggle(): void {
    this.open.update((open) => !open);
  }
}
