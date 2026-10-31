import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Balance } from './balance.model';

@Injectable({
  providedIn: 'root',
})
export class BalanceService {
  private readonly http = inject(HttpClient);

  private readonly balanceState = signal<Balance | null>(null);

  readonly balance = this.balanceState.asReadonly();

  getBalance(): Observable<Balance> {
    return this.http
      .get<Balance>(`${environment.apiUrl}/balance`)
      .pipe(tap((balance) => this.balanceState.set(balance)));
  }
}
