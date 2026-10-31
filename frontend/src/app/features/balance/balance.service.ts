import { inject, Injectable } from '@angular/core';
import { Balance } from './balance.model';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class BalanceService {
  private readonly http = inject(HttpClient);

  getBalance(): Observable<Balance> {
    return this.http.get<Balance>(`${environment.apiUrl}/balance`);
  }
}
