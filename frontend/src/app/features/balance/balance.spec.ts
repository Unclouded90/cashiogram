import { signal, LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt-PT';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Account } from '../account/account.model';
import { AccountService } from '../account/account.service';
import { Balance } from './balance';
import { Balance as BalanceModel } from './balance.model';
import { BalanceService } from './balance.service';

registerLocaleData(localePt);

describe('Balance', () => {
  let component: Balance;
  let fixture: ComponentFixture<Balance>;

  const accountState = signal<Account | null>(null);

  const balanceState = signal<BalanceModel | null>(null);

  const balanceServiceMock = {
    balance: balanceState.asReadonly(),
    getBalance: vi.fn(),
  };

  const accountServiceMock = {
    account: accountState.asReadonly(),
  };

  beforeEach(async () => {
    accountState.set(null);
    balanceState.set(null);
    vi.clearAllMocks();

    balanceServiceMock.getBalance.mockImplementation(() => {
      const balance = {
        initialBalance: 1000,
        totalIncome: 500,
        totalExpenses: 200,
        currentBalance: 1300,
      };

      balanceState.set(balance);

      return of(balance);
    });

    await TestBed.configureTestingModule({
      imports: [Balance],
      providers: [
        {
          provide: BalanceService,
          useValue: balanceServiceMock,
        },
        {
          provide: AccountService,
          useValue: accountServiceMock,
        },
        {
          provide: LOCALE_ID,
          useValue: 'pt-PT',
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Balance);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load the current balance', () => {
    expect(balanceServiceMock.getBalance).toHaveBeenCalled();

    const compiled = fixture.nativeElement as HTMLElement;
    const balanceValue = compiled.querySelector('.balance-value');

    expect(balanceValue).toBeTruthy();

    const formattedValue = balanceValue?.textContent?.replace(/\s+/g, ' ').trim();

    expect(formattedValue).toContain('1 300,00 €');
  });

  it('should show an error when loading the balance fails', () => {
    balanceServiceMock.getBalance.mockReturnValueOnce(
      throwError(() => new Error('Failed to load balance')),
    );

    const errorFixture = TestBed.createComponent(Balance);

    errorFixture.detectChanges();

    const compiled = errorFixture.nativeElement as HTMLElement;
    const errorMessage = compiled.querySelector('.balance-error');

    expect(errorMessage).toBeTruthy();
    expect(errorMessage?.textContent).toContain('Unable to load balance.');
  });

  it('should reload the balance when the account changes', async () => {
    balanceServiceMock.getBalance.mockImplementation(() => {
      const updatedBalance = {
        initialBalance: 1500,
        totalIncome: 500,
        totalExpenses: 200,
        currentBalance: 1800,
      };

      balanceState.set(updatedBalance);

      return of(updatedBalance);
    });

    accountState.set({
      id: 'account-id',
      initialBalance: 1500,
      createdAt: '2026-10-31T10:00:00Z',
      updatedAt: '2026-10-31T11:00:00Z',
    });

    fixture.detectChanges();
    await fixture.whenStable();

    expect(balanceServiceMock.getBalance).toHaveBeenCalledTimes(2);

    const compiled = fixture.nativeElement as HTMLElement;
    const balanceValue = compiled.querySelector('.balance-value');
    const formattedValue = balanceValue?.textContent?.replace(/\s+/g, ' ').trim();

    expect(formattedValue).toContain('1 800,00 €');
  });
});
