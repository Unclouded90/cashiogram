import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { AccountService } from './account.service';

describe('AccountService', () => {
  let service: AccountService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AccountService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AccountService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should get the account', () => {
    const expectedAccount = {
      id: 'account-id',
      initialBalance: 1000,
      createdAt: '2026-10-31T10:00:00Z',
      updatedAt: '2026-10-31T10:00:00Z',
    };

    service.getAccount().subscribe((account) => {
      expect(account).toEqual(expectedAccount);
      expect(service.account()).toEqual(expectedAccount);
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/account`);

    expect(request.request.method).toBe('GET');

    request.flush(expectedAccount);
  });

  it('should create the account', () => {
    const createdAccount = {
      id: 'account-id',
      initialBalance: 1000,
      createdAt: '2026-10-31T10:00:00Z',
      updatedAt: '2026-10-31T10:00:00Z',
    };

    service.createAccount(1000).subscribe((account) => {
      expect(account).toEqual(createdAccount);
      expect(service.account()).toEqual(createdAccount);
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/account`);

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      initialBalance: 1000,
    });

    request.flush(createdAccount);
  });

  it('should update the account', () => {
    const updatedAccount = {
      id: 'account-id',
      initialBalance: 1500,
      createdAt: '2026-10-31T10:00:00Z',
      updatedAt: '2026-10-31T11:00:00Z',
    };

    service.updateAccount(1500).subscribe((account) => {
      expect(account).toEqual(updatedAccount);
      expect(service.account()).toEqual(updatedAccount);
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/account`);

    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      initialBalance: 1500,
    });

    request.flush(updatedAccount);
  });
});
