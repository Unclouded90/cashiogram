import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Account } from '../account.model';
import { AccountService } from '../account.service';
import { AccountSettings } from './account-settings';

describe('AccountSettings', () => {
  let component: AccountSettings;
  let fixture: ComponentFixture<AccountSettings>;

  const accountState = signal<Account | null>(null);

  const accountServiceMock = {
    account: accountState.asReadonly(),
    getAccount: vi.fn(),
    createAccount: vi.fn(),
    updateAccount: vi.fn(),
  };

  beforeEach(async () => {
    accountState.set(null);
    vi.clearAllMocks();

    const account: Account = {
      id: 'account-id',
      initialBalance: 1000,
      createdAt: '2026-10-31T10:00:00Z',
      updatedAt: '2026-10-31T10:00:00Z',
    };

    accountServiceMock.getAccount.mockImplementation(() => {
      accountState.set(account);
      return of(account);
    });

    await TestBed.configureTestingModule({
      imports: [AccountSettings],
      providers: [
        {
          provide: AccountService,
          useValue: accountServiceMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AccountSettings);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load and display the existing account', () => {
    expect(accountServiceMock.getAccount).toHaveBeenCalled();

    const compiled = fixture.nativeElement as HTMLElement;
    const input = compiled.querySelector('#initial-balance') as HTMLInputElement;

    expect(input).toBeTruthy();
    expect(input.value).toBe('1000');
  });

  it('should show account creation when no account exists', () => {
    accountState.set(null);

    accountServiceMock.getAccount.mockReturnValueOnce(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 404,
          }),
      ),
    );

    const emptyFixture = TestBed.createComponent(AccountSettings);

    emptyFixture.detectChanges();

    const compiled = emptyFixture.nativeElement as HTMLElement;

    const input = compiled.querySelector('#initial-balance') as HTMLInputElement;

    const button = compiled.querySelector('button');

    expect(input).toBeTruthy();
    expect(input.value).toBe('');
    expect(button?.textContent).toContain('Create Account');
  });

  it('should create an account with the entered initial balance', () => {
    accountState.set(null);

    accountServiceMock.getAccount.mockReturnValueOnce(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 404,
          }),
      ),
    );

    accountServiceMock.createAccount.mockReturnValue(
      of({
        id: 'account-id',
        initialBalance: 1200,
        createdAt: '2026-10-31T10:00:00Z',
        updatedAt: '2026-10-31T10:00:00Z',
      }),
    );

    const emptyFixture = TestBed.createComponent(AccountSettings);
    emptyFixture.detectChanges();

    const compiled = emptyFixture.nativeElement as HTMLElement;

    const input = compiled.querySelector('#initial-balance') as HTMLInputElement;

    const button = compiled.querySelector('button') as HTMLButtonElement;

    input.value = '1200';
    button.click();

    expect(accountServiceMock.createAccount).toHaveBeenCalledWith(1200);
  });

  it('should update the account with the entered initial balance', () => {
    const account: Account = {
      id: 'account-id',
      initialBalance: 1000,
      createdAt: '2026-10-31T10:00:00Z',
      updatedAt: '2026-10-31T10:00:00Z',
    };

    accountState.set(account);

    accountServiceMock.getAccount.mockImplementation(() => {
      accountState.set(account);
      return of(account);
    });

    accountServiceMock.updateAccount.mockReturnValue(
      of({
        ...account,
        initialBalance: 1500,
        updatedAt: '2026-10-31T11:00:00Z',
      }),
    );

    const existingFixture = TestBed.createComponent(AccountSettings);
    existingFixture.detectChanges();

    const compiled = existingFixture.nativeElement as HTMLElement;

    const input = compiled.querySelector('#initial-balance') as HTMLInputElement;

    const button = compiled.querySelector('button') as HTMLButtonElement;

    input.value = '1500';
    button.click();

    expect(accountServiceMock.updateAccount).toHaveBeenCalledWith(1500);
  });
});
