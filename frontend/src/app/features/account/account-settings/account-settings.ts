import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';

import { AccountService } from '../account.service';

@Component({
  selector: 'app-account-settings',
  imports: [],
  templateUrl: './account-settings.html',
  styleUrl: './account-settings.scss',
})
export class AccountSettings implements OnInit {
  private readonly accountService = inject(AccountService);

  protected readonly account = this.accountService.account;

  ngOnInit(): void {
    this.accountService.getAccount().subscribe({
      error: (error: HttpErrorResponse) => {
        if (error.status !== 404) {
          console.error('Unable to load account.');
        }
      },
    });
  }

  createAccount(initialBalance: string): void {
    const value = Number(initialBalance);

    this.accountService.createAccount(value).subscribe();
  }

  updateAccount(initialBalance: string): void {
    const value = Number(initialBalance);

    this.accountService.updateAccount(value).subscribe();
  }
}
