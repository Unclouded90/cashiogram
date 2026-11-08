import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { BalanceService } from '../balance/balance.service';
import { CategoryService } from '../categories/category.service';
import { Transactions } from './transactions';
import { TransactionService } from './transaction.service';

describe('Transactions', () => {
  let component: Transactions;
  let fixture: ComponentFixture<Transactions>;

  const categories = [
    {
      id: 'category-1',
      name: 'Groceries',
      description: 'General purchases',
      createdAt: '2026-10-31T18:00:00Z',
    },
  ];

  const transactions = [
    {
      id: 'transaction-1',
      amount: 98.76,
      transactionType: 'EXPENSE' as const,
      categoryId: 'category-1',
      date: '2026-10-22',
      description: 'Monthly groceries',
      splits: [],
      createdAt: '2026-10-31T19:00:00Z',
    },
    {
      id: 'transaction-income',
      amount: 2500,
      transactionType: 'INCOME' as const,
      categoryId: 'category-1',
      date: '2026-11-03',
      description: 'November salary',
      splits: [],
      createdAt: '2026-11-03T10:00:00Z',
    },
    {
      id: 'transaction-vet',
      amount: 65,
      transactionType: 'EXPENSE' as const,
      categoryId: 'category-1',
      date: '2026-11-07',
      description: 'Vet appointment',
      splits: [],
      createdAt: '2026-11-07T10:00:00Z',
    },
  ];

  const transactionServiceMock = {
    getTransactions: vi.fn(),
    createTransaction: vi.fn(),
  };

  const categoryServiceMock = {
    getCategories: vi.fn(),
  };

  const balanceServiceMock = {
    getBalance: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    transactionServiceMock.getTransactions.mockReturnValue(of(transactions));

    categoryServiceMock.getCategories.mockReturnValue(of(categories));

    balanceServiceMock.getBalance.mockReturnValue(
      of({
        initialBalance: 1000,
        totalIncome: 0,
        totalExpenses: 98.76,
        currentBalance: 901.24,
      }),
    );

    await TestBed.configureTestingModule({
      imports: [Transactions],
      providers: [
        provideRouter([]),
        {
          provide: TransactionService,
          useValue: transactionServiceMock,
        },
        {
          provide: CategoryService,
          useValue: categoryServiceMock,
        },
        {
          provide: BalanceService,
          useValue: balanceServiceMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Transactions);
    component = fixture.componentInstance;

    fixture.detectChanges();

    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load transactions and display the category name and description', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    expect(transactionServiceMock.getTransactions).toHaveBeenCalled();

    expect(categoryServiceMock.getCategories).toHaveBeenCalled();

    expect(compiled.textContent).toContain('Groceries');
    expect(compiled.textContent).toContain('Monthly groceries');
    expect(compiled.textContent).not.toContain('category-1');
  });

  it('should create a transaction and refresh the balance', async () => {
    const createdTransaction = {
      id: 'transaction-2',
      amount: 50,
      transactionType: 'EXPENSE' as const,
      categoryId: 'category-1',
      date: '2026-10-31',
      description: 'Extra groceries',
      splits: [],
      createdAt: '2026-10-31T20:00:00Z',
    };

    transactionServiceMock.createTransaction.mockReturnValue(of(createdTransaction));

    const compiled = fixture.nativeElement as HTMLElement;

    const amountInput = compiled.querySelector('input[placeholder="Amount"]') as HTMLInputElement;

    const descriptionInput = compiled.querySelector(
      'input[placeholder="Description"]',
    ) as HTMLInputElement;

    const selects = compiled.querySelectorAll('.transaction-form select');

    const typeInput = selects[0] as HTMLSelectElement;
    const categoryInput = selects[1] as HTMLSelectElement;

    const dateInput = compiled.querySelector('input[type="date"]') as HTMLInputElement;

    amountInput.value = '50';
    typeInput.value = 'EXPENSE';
    categoryInput.value = 'category-1';
    dateInput.value = '2026-10-31';
    descriptionInput.value = 'Extra groceries';

    const addButton = Array.from(compiled.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.includes('Add Transaction'),
    );

    addButton?.click();

    await fixture.whenStable();
    fixture.detectChanges();

    expect(transactionServiceMock.createTransaction).toHaveBeenCalledWith({
      amount: 50,
      transactionType: 'EXPENSE',
      categoryId: 'category-1',
      date: '2026-10-31',
      description: 'Extra groceries',
    });

    expect(balanceServiceMock.getBalance).toHaveBeenCalled();

    expect(amountInput.value).toBe('');
    expect(categoryInput.value).toBe('');
    expect(dateInput.value).toBe('');
    expect(descriptionInput.value).toBe('');

    expect(compiled.textContent).toContain('Extra groceries');
  });

  it('should filter transactions by type and date', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelectorAll('.transaction-card-link')).toHaveLength(3);

    const toggleButton = compiled.querySelector(
      '.transactions-filters-toggle',
    ) as HTMLButtonElement;

    toggleButton.click();
    fixture.detectChanges();

    const typeSelect = compiled.querySelector('#transaction-filter-type') as HTMLSelectElement;

    const fromInput = compiled.querySelector('#transaction-filter-from') as HTMLInputElement;

    const toInput = compiled.querySelector('#transaction-filter-to') as HTMLInputElement;

    typeSelect.value = 'EXPENSE';
    typeSelect.dispatchEvent(new Event('change'));

    fromInput.value = '2026-11-01';
    fromInput.dispatchEvent(new Event('change'));

    toInput.value = '2026-11-30';
    toInput.dispatchEvent(new Event('change'));

    fixture.detectChanges();

    expect(compiled.querySelectorAll('.transaction-card-link')).toHaveLength(1);

    expect(compiled.textContent).toContain('Vet appointment');
    expect(compiled.textContent).not.toContain('Monthly groceries');
    expect(compiled.textContent).not.toContain('November salary');

    expect(transactionServiceMock.getTransactions).toHaveBeenCalledTimes(1);
  });

  it('should clear transaction filters', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const toggleButton = compiled.querySelector(
      '.transactions-filters-toggle',
    ) as HTMLButtonElement;

    toggleButton.click();
    fixture.detectChanges();

    const typeSelect = compiled.querySelector('#transaction-filter-type') as HTMLSelectElement;

    typeSelect.value = 'INCOME';
    typeSelect.dispatchEvent(new Event('change'));

    fixture.detectChanges();

    expect(compiled.querySelectorAll('.transaction-card-link')).toHaveLength(1);

    const clearButton = compiled.querySelector('.clear-filters-button') as HTMLButtonElement;

    clearButton.click();
    fixture.detectChanges();

    expect(compiled.querySelectorAll('.transaction-card-link')).toHaveLength(3);
    expect(typeSelect.value).toBe('ALL');
  });

  it('should reject an invalid transaction filter period', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const toggleButton = compiled.querySelector(
      '.transactions-filters-toggle',
    ) as HTMLButtonElement;

    toggleButton.click();
    fixture.detectChanges();

    const fromInput = compiled.querySelector('#transaction-filter-from') as HTMLInputElement;

    const toInput = compiled.querySelector('#transaction-filter-to') as HTMLInputElement;

    fromInput.value = '2026-11-30';
    fromInput.dispatchEvent(new Event('change'));

    toInput.value = '2026-11-01';
    toInput.dispatchEvent(new Event('change'));

    fixture.detectChanges();

    expect(compiled.textContent).toContain('From date cannot be after To date.');

    expect(compiled.querySelectorAll('.transaction-card-link')).toHaveLength(0);
  });
});
