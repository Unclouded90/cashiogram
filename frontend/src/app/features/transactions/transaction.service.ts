import { inject, Injectable } from '@angular/core';
import {
  CreateTransaction,
  CreateTransactionSplit,
  Transaction,
  TransactionSplit,
  UpdateTransaction,
  UpdateTransactionSplit,
} from './transaction.model';
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

  getTransaction(transactionId: string): Observable<Transaction> {
    return this.http.get<Transaction>(`${environment.apiUrl}/transactions/${transactionId}`);
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

  createTransactionSplit(
    transactionId: string,
    split: CreateTransactionSplit,
  ): Observable<TransactionSplit> {
    return this.http.post<TransactionSplit>(
      `${environment.apiUrl}/transactions/${transactionId}/splits`,
      split,
    );
  }

  updateTransactionSplit(
    transactionId: string,
    splitId: string,
    split: UpdateTransactionSplit,
  ): Observable<TransactionSplit> {
    return this.http.patch<TransactionSplit>(
      `${environment.apiUrl}/transactions/${transactionId}/splits/${splitId}`,
      split,
    );
  }

  deleteTransactionSplit(transactionId: string, splitId: string): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/transactions/${transactionId}/splits/${splitId}`,
    );
  }
}
