import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { Account } from '../account/account.model';
import { AccountService } from '../account/account.service';
import { Balance } from '../balance/balance.model';
import { BalanceService } from '../balance/balance.service';
import { CategoryService } from '../categories/category.service';
import { TransactionService } from '../transactions/transaction.service';
import { Dashboard } from './dashboard';

describe('Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;

  const accountState = signal<Account | null>(null);

  const balanceState = signal<Balance | null>(null);

  const accountServiceMock = {
    account: accountState.asReadonly(),
  };

  const balanceServiceMock = {
    balance: balanceState.asReadonly(),
    getBalance: vi.fn(),
  };

  const transactionServiceMock = {
    getTransactions: vi.fn(),
  };

  const categoryServiceMock = {
    getCategories: vi.fn(),
  };

  beforeEach(async () => {
    accountState.set(null);

    balanceState.set({
      initialBalance: 1000,
      totalIncome: 0,
      totalExpenses: 0,
      currentBalance: 1000,
    });

    vi.clearAllMocks();

    balanceServiceMock.getBalance.mockReturnValue(
      of({
        initialBalance: 1000,
        totalIncome: 0,
        totalExpenses: 0,
        currentBalance: 1000,
      }),
    );

    transactionServiceMock.getTransactions.mockReturnValue(of([]));

    categoryServiceMock.getCategories.mockReturnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        {
          provide: AccountService,
          useValue: accountServiceMock,
        },
        {
          provide: BalanceService,
          useValue: balanceServiceMock,
        },
        {
          provide: TransactionService,
          useValue: transactionServiceMock,
        },
        {
          provide: CategoryService,
          useValue: categoryServiceMock,
        },
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;

    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
