import { Component, signal } from '@angular/core';

import { AccountSettings } from '../account/account-settings/account-settings';

@Component({
  selector: 'app-options',
  imports: [AccountSettings],
  templateUrl: './options.html',
  styleUrl: './options.scss',
})
export class Options {
  protected readonly open = signal(false);

  protected toggle(): void {
    this.open.update((open) => !open);
  }
}
