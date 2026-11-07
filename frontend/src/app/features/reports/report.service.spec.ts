import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { ReportService } from './report.service';

describe('ReportService', () => {
  let service: ReportService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ReportService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ReportService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should get period totals', () => {
    const expected = {
      from: '2026-10-01',
      to: '2026-10-31',
      totalIncome: 2500,
      totalExpenses: 1500,
      netTotal: 1000,
    };

    service
      .getPeriodTotal({
        from: '2026-10-01',
        to: '2026-10-31',
      })
      .subscribe((result) => {
        expect(result).toEqual(expected);
      });

    const request = httpTesting.expectOne(
      `${environment.apiUrl}/reports/period?from=2026-10-01&to=2026-10-31`,
    );

    expect(request.request.method).toBe('GET');

    request.flush(expected);
  });

  it('should get category totals with filters', () => {
    const expected = [
      {
        categoryId: 'category-1',
        categoryName: 'Groceries',
        total: 150,
      },
    ];

    service
      .getCategoryTotals('EXPENSE', {
        from: '2026-10-01',
        to: '2026-10-31',
        categoryIds: ['category-1', 'category-2'],
        minAmount: 50,
        maxAmount: 200,
      })
      .subscribe((result) => {
        expect(result).toEqual(expected);
      });

    const request = httpTesting.expectOne(
      (request) => request.url === `${environment.apiUrl}/reports/categories`,
    );

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('type')).toBe('EXPENSE');
    expect(request.request.params.get('from')).toBe('2026-10-01');
    expect(request.request.params.get('to')).toBe('2026-10-31');
    expect(request.request.params.getAll('categoryIds')).toEqual(['category-1', 'category-2']);
    expect(request.request.params.get('minAmount')).toBe('50');
    expect(request.request.params.get('maxAmount')).toBe('200');

    request.flush(expected);
  });

  it('should get subcategory totals with split filters', () => {
    const expected = [
      {
        subcategoryId: 'subcategory-1',
        subcategoryName: 'Cats',
        categoryId: 'category-1',
        categoryName: 'Groceries',
        total: 60,
      },
    ];

    service
      .getSubcategoryTotals('EXPENSE', {
        from: '2026-10-01',
        to: '2026-10-31',
        subcategoryName: '  cat  ',
        minSplitAmount: 0,
        maxSplitAmount: 100,
      })
      .subscribe((result) => {
        expect(result).toEqual(expected);
      });

    const request = httpTesting.expectOne(
      (request) => request.url === `${environment.apiUrl}/reports/subcategories`,
    );

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('type')).toBe('EXPENSE');
    expect(request.request.params.get('subcategoryName')).toBe('cat');
    expect(request.request.params.get('minSplitAmount')).toBe('0');
    expect(request.request.params.get('maxSplitAmount')).toBe('100');

    request.flush(expected);
  });
});
