import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { TransactionService } from './transaction.service';

describe('TransactionService', () => {
  let service: TransactionService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TransactionService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(TransactionService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should get transactions', () => {
    const transactions = [
      {
        id: '1',
        amount: 50,
        transactionType: 'EXPENSE' as const,
        categoryId: 'category-1',
        date: '2026-10-31',
        description: 'Groceries',
        splits: [],
        createdAt: '2026-10-31T19:00:00Z',
      },
    ];

    service.getTransactions().subscribe((result) => {
      expect(result).toEqual(transactions);
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/transactions`);

    expect(request.request.method).toBe('GET');

    request.flush(transactions);
  });

  it('should create a transaction', () => {
    const createdTransaction = {
      id: '1',
      amount: 50,
      transactionType: 'EXPENSE' as const,
      categoryId: 'category-1',
      date: '2026-10-31',
      description: 'Groceries',
      splits: [],
      createdAt: '2026-10-31T19:00:00Z',
    };

    service
      .createTransaction({
        amount: 50,
        transactionType: 'EXPENSE',
        categoryId: 'category-1',
        date: '2026-10-31',
        description: 'Groceries',
      })
      .subscribe((result) => {
        expect(result).toEqual(createdTransaction);
      });

    const request = httpTesting.expectOne(`${environment.apiUrl}/transactions`);

    expect(request.request.method).toBe('POST');

    expect(request.request.body).toEqual({
      amount: 50,
      transactionType: 'EXPENSE',
      categoryId: 'category-1',
      date: '2026-10-31',
      description: 'Groceries',
    });

    request.flush(createdTransaction);
  });

  it('should update a transaction', () => {
    const updatedTransaction = {
      id: '1',
      amount: 75,
      transactionType: 'EXPENSE' as const,
      categoryId: 'category-1',
      date: '2026-10-31',
      description: 'Updated groceries',
      splits: [],
      createdAt: '2026-10-31T19:00:00Z',
    };

    service
      .updateTransaction('1', {
        amount: 75,
        description: 'Updated groceries',
      })
      .subscribe((result) => {
        expect(result).toEqual(updatedTransaction);
      });

    const request = httpTesting.expectOne(`${environment.apiUrl}/transactions/1`);

    expect(request.request.method).toBe('PATCH');

    expect(request.request.body).toEqual({
      amount: 75,
      description: 'Updated groceries',
    });

    request.flush(updatedTransaction);
  });

  it('should delete a transaction', () => {
    service.deleteTransaction('1').subscribe();

    const request = httpTesting.expectOne(`${environment.apiUrl}/transactions/1`);

    expect(request.request.method).toBe('DELETE');

    request.flush(null);
  });
});
