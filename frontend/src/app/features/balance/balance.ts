import { CurrencyPipe } from '@angular/common';
import { Component, effect, inject, OnInit, signal } from '@angular/core';

import { AccountService } from '../account/account.service';
import { Balance as BalanceModel } from './balance.model';
import { BalanceService } from './balance.service';

@Component({
  selector: 'app-balance',
  imports: [CurrencyPipe],
  templateUrl: './balance.html',
  styleUrl: './balance.scss',
})
export class Balance implements OnInit {
  private readonly balanceService = inject(BalanceService);
  private readonly accountService = inject(AccountService);

  protected readonly balance = signal<BalanceModel | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    effect(() => {
      const account = this.accountService.account();

      if (account) {
        this.loadBalance();
      }
    });
  }

  ngOnInit(): void {
    this.loadBalance();
  }

  private loadBalance(): void {
    this.balanceService.getBalance().subscribe({
      next: (balance: BalanceModel) => {
        this.balance.set(balance);
        this.error.set(null);
      },
      error: () => {
        this.error.set('Unable to load balance.');
      },
    });
  }
}
