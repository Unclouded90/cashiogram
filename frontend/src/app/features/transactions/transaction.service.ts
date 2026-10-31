import { inject, Injectable } from '@angular/core';
import { CreateTransaction, Transaction, UpdateTransaction } from './transaction.model';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TransactionService {
  private readonly http = inject(HttpClient);

  getTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${environment.apiUrl}/transactions`);
  }

  createTransaction(transaction: CreateTransaction): Observable<Transaction> {
    return this.http.post<Transaction>(`${environment.apiUrl}/transactions`, transaction);
  }

  updateTransaction(
    transactionId: string,
    transaction: UpdateTransaction,
  ): Observable<Transaction> {
    return this.http.patch<Transaction>(
      `${environment.apiUrl}/transactions/${transactionId}`,
      transaction,
    );
  }

  deleteTransaction(transactionId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/transactions/${transactionId}`);
  }
}
