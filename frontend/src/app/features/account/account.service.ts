import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Account } from './account.model';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly http = inject(HttpClient);

  private readonly accountState = signal<Account | null>(null);

  readonly account = this.accountState.asReadonly();

  getAccount(): Observable<Account> {
    return this.http
      .get<Account>(`${environment.apiUrl}/account`)
      .pipe(tap((account) => this.accountState.set(account)));
  }

  createAccount(initialBalance: number): Observable<Account> {
    return this.http
      .post<Account>(`${environment.apiUrl}/account`, {
        initialBalance,
      })
      .pipe(tap((account) => this.accountState.set(account)));
  }

  updateAccount(initialBalance: number): Observable<Account> {
    return this.http
      .put<Account>(`${environment.apiUrl}/account`, {
        initialBalance,
      })
      .pipe(tap((account) => this.accountState.set(account)));
  }
}
