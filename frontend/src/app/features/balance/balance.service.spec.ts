import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { BalanceService } from './balance.service';
import { environment } from '../../../environments/environment';

describe('BalanceService', () => {
  let service: BalanceService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [BalanceService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(BalanceService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should get the balance', () => {
    const expectedBalance = {
      initialBalance: 1000,
      totalIncome: 500,
      totalExpenses: 200,
      currentBalance: 1300,
    };

    service.getBalance().subscribe((balance) => {
      expect(balance).toEqual(expectedBalance);
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/balance`);

    expect(request.request.method).toBe('GET');

    request.flush(expectedBalance);
  });
});
