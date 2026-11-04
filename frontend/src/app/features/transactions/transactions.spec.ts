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
});
