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

  it('should get a transaction', () => {
    const transaction = {
      id: '1',
      amount: 50,
      transactionType: 'EXPENSE' as const,
      categoryId: 'category-1',
      date: '2026-10-31',
      description: 'Groceries',
      splits: [],
      createdAt: '2026-10-31T19:00:00Z',
    };

    service.getTransaction('1').subscribe((result) => {
      expect(result).toEqual(transaction);
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/transactions/1`);

    expect(request.request.method).toBe('GET');

    request.flush(transaction);
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

  it('should create a transaction split', () => {
    const split = {
      subcategoryId: 'subcategory-1',
      amount: 30,
    };

    const createdSplit = {
      id: 'split-1',
      subcategoryId: 'subcategory-1',
      amount: 30,
    };

    service.createTransactionSplit('1', split).subscribe((result) => {
      expect(result).toEqual(createdSplit);
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/transactions/1/splits`);

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(split);

    request.flush(createdSplit);
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

  it('should update a transaction split', () => {
    const update = {
      amount: 45,
    };

    const updatedSplit = {
      id: 'split-1',
      subcategoryId: 'subcategory-1',
      amount: 45,
    };

    service.updateTransactionSplit('1', 'split-1', update).subscribe((result) => {
      expect(result).toEqual(updatedSplit);
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/transactions/1/splits/split-1`);

    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(update);

    request.flush(updatedSplit);
  });

  it('should delete a transaction', () => {
    service.deleteTransaction('1').subscribe();

    const request = httpTesting.expectOne(`${environment.apiUrl}/transactions/1`);

    expect(request.request.method).toBe('DELETE');

    request.flush(null);
  });

  it('should delete a transaction split', () => {
    service.deleteTransactionSplit('1', 'split-1').subscribe();

    const request = httpTesting.expectOne(`${environment.apiUrl}/transactions/1/splits/split-1`);

    expect(request.request.method).toBe('DELETE');

    request.flush(null);
  });
});
