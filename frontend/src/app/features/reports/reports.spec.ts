import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';

import { CategoryService } from '../categories/category.service';
import { ReportService } from './report.service';
import { Reports } from './reports';
import { CategoryTotal } from './report.model';

describe('Reports', () => {
  let fixture: ComponentFixture<Reports>;

  const period = {
    from: '2026-10-01',
    to: '2026-10-31',
    totalIncome: 2500,
    totalExpenses: 1500,
    netTotal: 1000,
  };

  const reportServiceMock = {
    getPeriodTotal: vi.fn(),
    getCategoryTotals: vi.fn(),
    getSubcategoryTotals: vi.fn(),
  };

  const categoryServiceMock = {
    getCategories: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    categoryServiceMock.getCategories.mockReturnValue(
      of([
        {
          id: 'category-1',
          name: 'Groceries',
          description: null,
          createdAt: '2026-10-01T00:00:00',
        },
        {
          id: 'category-2',
          name: 'Veterinarian Bills',
          description: null,
          createdAt: '2026-10-01T00:00:00',
        },
      ]),
    );

    reportServiceMock.getPeriodTotal.mockReturnValue(of(period));
    reportServiceMock.getCategoryTotals.mockReturnValue(of([]));
    reportServiceMock.getSubcategoryTotals.mockReturnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [Reports],
      providers: [
        provideRouter([]),
        {
          provide: ReportService,
          useValue: reportServiceMock,
        },
        {
          provide: CategoryService,
          useValue: categoryServiceMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Reports);
    fixture.detectChanges();

    await fixture.whenStable();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should load the current month on initialization', () => {
    const today = new Date();

    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const lastDay = String(new Date(year, today.getMonth() + 1, 0).getDate()).padStart(2, '0');

    expect(reportServiceMock.getPeriodTotal).toHaveBeenCalledWith({
      from: `${year}-${month}-01`,
      to: `${year}-${month}-${lastDay}`,
    });
  });

  it('should display the period totals', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const text = compiled.textContent?.replace(/[\s\u00A0\u202F]/g, '');

    expect(text).toContain('2500,00€');
    expect(text).toContain('1500,00€');
    expect(text).toContain('1000,00€');
  });

  it('should reload totals when applying a new period', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const fromInput = compiled.querySelector('#report-from') as HTMLInputElement;

    const toInput = compiled.querySelector('#report-to') as HTMLInputElement;

    fromInput.value = '2026-10-01';
    fromInput.dispatchEvent(new Event('change'));

    toInput.value = '2026-10-31';
    toInput.dispatchEvent(new Event('change'));

    const button = compiled.querySelector('.filters-content button') as HTMLButtonElement;

    button.click();

    expect(reportServiceMock.getPeriodTotal).toHaveBeenCalledWith({
      from: '2026-10-01',
      to: '2026-10-31',
    });

    expect(reportServiceMock.getPeriodTotal).toHaveBeenCalledTimes(2);
  });

  it('should prevent applying an invalid period', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const fromInput = compiled.querySelector('#report-from') as HTMLInputElement;

    const toInput = compiled.querySelector('#report-to') as HTMLInputElement;

    fromInput.value = '2026-10-31';
    fromInput.dispatchEvent(new Event('change'));

    toInput.value = '2026-10-01';
    toInput.dispatchEvent(new Event('change'));

    fixture.detectChanges();

    const button = compiled.querySelector('.filters-content button') as HTMLButtonElement;

    expect(button.disabled).toBe(true);
    expect(compiled.textContent).toContain('From date cannot be after To date.');

    expect(reportServiceMock.getPeriodTotal).toHaveBeenCalledTimes(1);
  });

  it('should display income and expenses by default', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('.pie-chart')).toBeTruthy();
    expect(compiled.textContent).toContain('Income');
    expect(compiled.textContent).toContain('Expenses');

    expect(reportServiceMock.getCategoryTotals).toHaveBeenCalledTimes(2);
    expect(reportServiceMock.getSubcategoryTotals).toHaveBeenCalledTimes(2);
  });

  it('should load category totals when grouping by category', () => {
    reportServiceMock.getCategoryTotals.mockReturnValue(
      of([
        {
          categoryId: 'category-1',
          categoryName: 'Groceries',
          total: 900,
        },
        {
          categoryId: 'category-2',
          categoryName: 'Transport',
          total: 600,
        },
      ]),
    );

    const compiled = fixture.nativeElement as HTMLElement;

    const groupSelect = compiled.querySelector('#report-group-by') as HTMLSelectElement;

    groupSelect.value = 'CATEGORY';
    groupSelect.dispatchEvent(new Event('change'));

    fixture.detectChanges();

    expect(reportServiceMock.getCategoryTotals).toHaveBeenCalledWith(
      'EXPENSE',
      expect.objectContaining({
        from: expect.any(String),
        to: expect.any(String),
      }),
    );

    expect(compiled.textContent).toContain('Groceries');
    expect(compiled.textContent).toContain('Transport');
    expect(compiled.textContent).toContain('60%');
    expect(compiled.textContent).toContain('40%');
  });

  it('should display subcategories separately and include unallocated', () => {
    reportServiceMock.getSubcategoryTotals.mockReturnValue(
      of([
        {
          subcategoryId: 'subcategory-1',
          subcategoryName: 'Cats',
          categoryId: 'category-1',
          categoryName: 'Groceries',
          total: 500,
        },
        {
          subcategoryId: 'subcategory-2',
          subcategoryName: 'Cats',
          categoryId: 'category-2',
          categoryName: 'Veterinarian Bills',
          total: 600,
        },
      ]),
    );

    const compiled = fixture.nativeElement as HTMLElement;

    const groupSelect = compiled.querySelector('#report-group-by') as HTMLSelectElement;

    groupSelect.value = 'SUBCATEGORY';
    groupSelect.dispatchEvent(new Event('change'));

    fixture.detectChanges();

    expect(reportServiceMock.getSubcategoryTotals).toHaveBeenCalledWith(
      'EXPENSE',
      expect.objectContaining({
        from: expect.any(String),
        to: expect.any(String),
      }),
    );

    expect(compiled.textContent).toContain('Cats (Groceries)');
    expect(compiled.textContent).toContain('Cats (Veterinarian Bills)');
    expect(compiled.textContent).toContain('Unallocated');

    const normalizedText = compiled.textContent?.replace(/\s/g, '');

    expect(normalizedText).toContain('400,00€');
  });

  it('should reload categories when transaction type changes', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const groupSelect = compiled.querySelector('#report-group-by') as HTMLSelectElement;

    groupSelect.value = 'CATEGORY';
    groupSelect.dispatchEvent(new Event('change'));

    fixture.detectChanges();

    const typeSelect = compiled.querySelector('#report-transaction-type') as HTMLSelectElement;

    typeSelect.value = 'INCOME';
    typeSelect.dispatchEvent(new Event('change'));

    fixture.detectChanges();

    expect(reportServiceMock.getCategoryTotals).toHaveBeenCalledWith(
      'INCOME',
      expect.objectContaining({
        from: expect.any(String),
        to: expect.any(String),
      }),
    );
  });

  it('should apply category, subcategory and amount filters', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const categories = compiled.querySelectorAll<HTMLInputElement>(
      '.category-option input[type="checkbox"]',
    );

    categories[0].checked = true;
    categories[0].dispatchEvent(new Event('change'));

    const setInput = (selector: string, value: string) => {
      const input = compiled.querySelector(selector) as HTMLInputElement;

      input.value = value;
      input.dispatchEvent(new Event('input'));
    };

    setInput('#report-subcategory-name', '  cat  ');
    setInput('#report-min-amount', '50');
    setInput('#report-max-amount', '200');
    setInput('#report-min-split', '0');
    setInput('#report-max-split', '100');

    const button = compiled.querySelector('.filter-actions button') as HTMLButtonElement;

    button.click();

    expect(reportServiceMock.getPeriodTotal).toHaveBeenLastCalledWith({
      from: expect.any(String),
      to: expect.any(String),
      categoryIds: ['category-1'],
      subcategoryName: 'cat',
      minAmount: 50,
      maxAmount: 200,
      minSplitAmount: 0,
      maxSplitAmount: 100,
    });
  });

  it('should distribute matching subcategory splits by category', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const search = compiled.querySelector('#report-subcategory-name') as HTMLInputElement;

    search.value = 'Cats';
    search.dispatchEvent(new Event('input'));

    const applyButton = compiled.querySelector('.filter-actions button') as HTMLButtonElement;

    applyButton.click();

    const groupSelect = compiled.querySelector('#report-group-by') as HTMLSelectElement;

    groupSelect.value = 'CATEGORY';
    groupSelect.dispatchEvent(new Event('change'));

    expect(reportServiceMock.getCategoryTotals).toHaveBeenCalledWith(
      'EXPENSE',
      expect.objectContaining({
        subcategoryName: 'Cats',
      }),
    );
  });

  it('should prevent invalid amount ranges', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const min = compiled.querySelector('#report-min-amount') as HTMLInputElement;
    const max = compiled.querySelector('#report-max-amount') as HTMLInputElement;

    min.value = '200';
    min.dispatchEvent(new Event('input'));

    max.value = '100';
    max.dispatchEvent(new Event('input'));

    fixture.detectChanges();

    const button = compiled.querySelector('.filter-actions button') as HTMLButtonElement;

    expect(button.disabled).toBe(true);
    expect(compiled.textContent).toContain('Minimum transaction amount cannot exceed maximum.');

    expect(reportServiceMock.getPeriodTotal).toHaveBeenCalledTimes(1);
  });

  it('should not show unallocated when split filters are active', () => {
    reportServiceMock.getPeriodTotal.mockReturnValue(
      of({
        from: '2026-10-01',
        to: '2026-10-31',
        totalIncome: 0,
        totalExpenses: 60,
        netTotal: -60,
      }),
    );

    reportServiceMock.getSubcategoryTotals.mockReturnValue(
      of([
        {
          subcategoryId: 'subcategory-1',
          subcategoryName: 'Cats',
          categoryId: 'category-1',
          categoryName: 'Groceries',
          total: 60,
        },
      ]),
    );

    const compiled = fixture.nativeElement as HTMLElement;

    const search = compiled.querySelector('#report-subcategory-name') as HTMLInputElement;

    search.value = 'cat';
    search.dispatchEvent(new Event('input'));

    const applyButton = compiled.querySelector('.filter-actions button') as HTMLButtonElement;

    applyButton.click();

    const groupSelect = compiled.querySelector('#report-group-by') as HTMLSelectElement;

    groupSelect.value = 'SUBCATEGORY';
    groupSelect.dispatchEvent(new Event('change'));

    fixture.detectChanges();

    expect(compiled.textContent).toContain('Cats (Groceries)');
    expect(compiled.textContent).not.toContain('Unallocated');
    expect(compiled.textContent).toContain('100%');
  });

  it('should display the complete financial breakdown and unallocated amounts', () => {
    reportServiceMock.getCategoryTotals.mockImplementation((type: string) =>
      of(
        type === 'INCOME'
          ? [{ categoryId: 'salary', categoryName: 'Salary', total: 2500 }]
          : [
              { categoryId: 'groceries', categoryName: 'Groceries', total: 100 },
              { categoryId: 'vet', categoryName: 'Veterinarian Bills', total: 90 },
            ],
      ),
    );

    reportServiceMock.getSubcategoryTotals.mockImplementation((type: string) =>
      of(
        type === 'INCOME'
          ? []
          : [
              {
                subcategoryId: 'cats-groceries',
                subcategoryName: 'Cats',
                categoryId: 'groceries',
                categoryName: 'Groceries',
                total: 60,
              },
              {
                subcategoryId: 'food',
                subcategoryName: 'Food',
                categoryId: 'groceries',
                categoryName: 'Groceries',
                total: 30,
              },
              {
                subcategoryId: 'cats-vet',
                subcategoryName: 'Cats',
                categoryId: 'vet',
                categoryName: 'Veterinarian Bills',
                total: 90,
              },
            ],
      ),
    );

    const compiled = fixture.nativeElement as HTMLElement;

    const fromInput = compiled.querySelector('#report-from') as HTMLInputElement;
    const toInput = compiled.querySelector('#report-to') as HTMLInputElement;

    fromInput.value = '2025-01-01';
    fromInput.dispatchEvent(new Event('change'));

    toInput.value = '2025-01-31';
    toInput.dispatchEvent(new Event('change'));

    const applyButton = compiled.querySelector('.filter-actions button') as HTMLButtonElement;

    applyButton.click();
    fixture.detectChanges();

    expect(reportServiceMock.getCategoryTotals).toHaveBeenCalledWith('EXPENSE', {
      from: '2025-01-01',
      to: '2025-01-31',
    });

    expect(reportServiceMock.getSubcategoryTotals).toHaveBeenCalledWith('INCOME', {
      from: '2025-01-01',
      to: '2025-01-31',
    });

    expect(compiled.querySelectorAll('.breakdown-category-row')).toHaveLength(3);

    const buttons = Array.from(compiled.querySelectorAll<HTMLButtonElement>('.breakdown-toggle'));

    const groceriesButton = buttons.find((button) => button.textContent?.includes('Groceries'));

    expect(groceriesButton).toBeDefined();

    groceriesButton?.click();
    fixture.detectChanges();

    let subcategoryRows = Array.from(compiled.querySelectorAll('.breakdown-subcategory-row'));

    expect(subcategoryRows).toHaveLength(3);

    expect(
      subcategoryRows.some(
        (row) => row.textContent?.includes('Unallocated') && row.textContent?.includes('10,00'),
      ),
    ).toBe(true);

    const vetButton = buttons.find((button) => button.textContent?.includes('Veterinarian Bills'));

    vetButton?.click();
    fixture.detectChanges();

    subcategoryRows = Array.from(compiled.querySelectorAll('.breakdown-subcategory-row'));

    expect(subcategoryRows.filter((row) => row.textContent?.includes('Cats'))).toHaveLength(2);
  });

  it('should not reload the breakdown when only analytical filters change', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const initialCategoryCalls = reportServiceMock.getCategoryTotals.mock.calls.length;
    const initialSubcategoryCalls = reportServiceMock.getSubcategoryTotals.mock.calls.length;

    const search = compiled.querySelector('#report-subcategory-name') as HTMLInputElement;

    search.value = 'Cats';
    search.dispatchEvent(new Event('input'));

    const applyButton = compiled.querySelector('.filter-actions button') as HTMLButtonElement;

    applyButton.click();

    expect(reportServiceMock.getPeriodTotal).toHaveBeenLastCalledWith(
      expect.objectContaining({
        subcategoryName: 'Cats',
      }),
    );

    expect(reportServiceMock.getCategoryTotals).toHaveBeenCalledTimes(initialCategoryCalls);

    expect(reportServiceMock.getSubcategoryTotals).toHaveBeenCalledTimes(initialSubcategoryCalls);
  });

  it('should reload breakdown when returning to a previous period', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const fromInput = compiled.querySelector('#report-from') as HTMLInputElement;
    const toInput = compiled.querySelector('#report-to') as HTMLInputElement;

    const originalFrom = fromInput.value;
    const originalTo = toInput.value;

    const pendingCategories = new Subject<CategoryTotal[]>();

    reportServiceMock.getCategoryTotals.mockImplementation(
      (type: string, filters: { from: string; to: string }) => {
        if (type === 'EXPENSE' && filters.from === '2025-01-01') {
          return pendingCategories.asObservable();
        }

        return of([]);
      },
    );

    const applyButton = compiled.querySelector('.filter-actions button') as HTMLButtonElement;

    const changePeriod = (from: string, to: string): void => {
      fromInput.value = from;
      fromInput.dispatchEvent(new Event('change'));

      toInput.value = to;
      toInput.dispatchEvent(new Event('change'));

      fixture.detectChanges();
      applyButton.click();
      fixture.detectChanges();
    };

    changePeriod('2025-01-01', '2025-01-31');

    changePeriod(originalFrom, originalTo);

    const originalPeriodCalls = reportServiceMock.getCategoryTotals.mock.calls.filter(
      ([type, filters]) =>
        type === 'EXPENSE' && filters.from === originalFrom && filters.to === originalTo,
    );

    expect(originalPeriodCalls).toHaveLength(2);

    expect(compiled.textContent).toContain('No transactions for the selected period.');
  });

  it('should distinguish a real Unallocated subcategory from calculated unallocated', () => {
    reportServiceMock.getCategoryTotals.mockImplementation((type: string) =>
      of(
        type === 'EXPENSE'
          ? [{ categoryId: 'groceries', categoryName: 'Groceries', total: 100 }]
          : [],
      ),
    );

    reportServiceMock.getSubcategoryTotals.mockImplementation((type: string) =>
      of(
        type === 'EXPENSE'
          ? [
              {
                subcategoryId: 'real-unallocated',
                subcategoryName: 'Unallocated',
                categoryId: 'groceries',
                categoryName: 'Groceries',
                total: 60,
              },
            ]
          : [],
      ),
    );

    const compiled = fixture.nativeElement as HTMLElement;

    const fromInput = compiled.querySelector('#report-from') as HTMLInputElement;
    const toInput = compiled.querySelector('#report-to') as HTMLInputElement;

    fromInput.value = '2025-01-01';
    fromInput.dispatchEvent(new Event('change'));

    toInput.value = '2025-01-31';
    toInput.dispatchEvent(new Event('change'));

    const applyButton = compiled.querySelector('.filter-actions button') as HTMLButtonElement;

    applyButton.click();
    fixture.detectChanges();

    const groceriesButton = Array.from(
      compiled.querySelectorAll<HTMLButtonElement>('.breakdown-toggle'),
    ).find((button) => button.textContent?.includes('Groceries'));

    expect(groceriesButton).toBeDefined();

    groceriesButton?.click();
    fixture.detectChanges();

    const rows = Array.from(compiled.querySelectorAll<HTMLElement>('.breakdown-subcategory-row'));

    expect(rows).toHaveLength(2);

    expect(rows[0].textContent).toContain('60,00');
    expect(rows[1].textContent).toContain('40,00');

    expect(rows[0].querySelector('.subcategory-name')?.classList.contains('unallocated')).toBe(
      false,
    );

    expect(rows[1].querySelector('.subcategory-name')?.classList.contains('unallocated')).toBe(
      true,
    );
  });
});
