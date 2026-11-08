import { Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, Observable, of, Subscription } from 'rxjs';

import { Card } from '../../shared/card/card';
import {
  CategoryTotal,
  PeriodTotal,
  ReportFilters,
  ReportTransactionType,
  SubcategoryTotal,
} from './report.model';
import { Category } from '../categories/category.model';
import { CategoryService } from '../categories/category.service';
import { ReportService } from './report.service';
import { ThemeService } from '../../core/theme/theme.service';

type ReportGroupBy = 'TYPE' | 'CATEGORY' | 'SUBCATEGORY';

interface ChartSlice {
  key: string;
  label: string;
  amount: number;
  percentage: number;
  color: string;
}

interface BreakdownSubcategoryRow {
  id: string;
  name: string;
  income: number;
  expense: number;
  isUnallocated: boolean;
}

interface BreakdownCategoryRow {
  id: string;
  name: string;
  income: number;
  expense: number;
  subcategories: BreakdownSubcategoryRow[];
}

interface BreakdownData {
  incomeCategories: CategoryTotal[];
  expenseCategories: CategoryTotal[];
  incomeSubcategories: SubcategoryTotal[];
  expenseSubcategories: SubcategoryTotal[];
}

@Component({
  selector: 'app-reports',
  imports: [Card, RouterLink],
  templateUrl: './reports.html',
  styleUrl: './reports.scss',
})
export class Reports implements OnInit, OnDestroy {
  private readonly reportService = inject(ReportService);
  private readonly categoryService = inject(CategoryService);
  private readonly today = new Date();

  private request?: Subscription;

  private readonly colors = {
    light: ['#657781', '#b78253', '#647c6c', '#947d9b', '#bf7d76', '#8199aa', '#8c9458', '#b18b6d'],
    dark: ['#f59a45', '#4d96d7', '#e6b15f', '#6cb6d4', '#f47b58', '#a78bfa', '#3fc5b0', '#d872a0'],
  } as const;

  private readonly currencyFormatter = new Intl.NumberFormat('pt-PT', {
    style: 'currency',
    currency: 'EUR',
  });

  private readonly percentageFormatter = new Intl.NumberFormat('pt-PT', {
    maximumFractionDigits: 1,
  });

  private readonly themeService = inject(ThemeService);

  protected readonly from = signal(
    this.formatDate(new Date(this.today.getFullYear(), this.today.getMonth(), 1)),
  );

  protected readonly to = signal(
    this.formatDate(new Date(this.today.getFullYear(), this.today.getMonth() + 1, 0)),
  );

  protected readonly groupBy = signal<ReportGroupBy>('TYPE');
  protected readonly selectedType = signal<ReportTransactionType>('EXPENSE');

  protected readonly appliedFilters = signal<ReportFilters | null>(null);

  protected readonly periodTotal = signal<PeriodTotal | null>(null);
  protected readonly categoryTotals = signal<CategoryTotal[]>([]);
  protected readonly subcategoryTotals = signal<SubcategoryTotal[]>([]);

  protected readonly categories = signal<Category[]>([]);
  protected readonly categoryLoadError = signal('');

  protected readonly selectedCategoryIds = signal<string[]>([]);
  protected readonly subcategoryName = signal('');

  protected readonly minAmountInput = signal('');
  protected readonly maxAmountInput = signal('');

  protected readonly minSplitAmountInput = signal('');
  protected readonly maxSplitAmountInput = signal('');

  protected readonly loading = signal(false);
  protected readonly error = signal('');

  protected readonly chartSlices = computed<ChartSlice[]>(() => {
    const period = this.periodTotal();

    if (!period) {
      return [];
    }

    let rows: { key: string; label: string; amount: number }[] = [];

    switch (this.groupBy()) {
      case 'TYPE':
        rows = [
          { key: 'INCOME', label: 'Income', amount: period.totalIncome },
          { key: 'EXPENSE', label: 'Expenses', amount: period.totalExpenses },
        ];
        break;

      case 'CATEGORY':
        rows = this.categoryTotals().map((category) => ({
          key: category.categoryId,
          label: category.categoryName,
          amount: category.total,
        }));
        break;

      case 'SUBCATEGORY': {
        rows = this.subcategoryTotals().map((subcategory) => ({
          key: subcategory.subcategoryId,
          label: `${subcategory.subcategoryName} (${subcategory.categoryName})`,
          amount: subcategory.total,
        }));

        const allocated = rows.reduce((sum, row) => sum + row.amount, 0);

        const transactionTotal =
          this.selectedType() === 'EXPENSE' ? period.totalExpenses : period.totalIncome;

        const unallocated = Math.round((transactionTotal - allocated) * 100) / 100;

        const filters = this.appliedFilters();

        const hasSplitFilters =
          !!filters?.subcategoryName ||
          filters?.minSplitAmount !== undefined ||
          filters?.maxSplitAmount !== undefined;

        if (!hasSplitFilters && unallocated > 0) {
          rows.push({
            key: 'UNALLOCATED',
            label: 'Unallocated',
            amount: unallocated,
          });
        }

        break;
      }
    }

    const positiveRows = rows.filter((row) => row.amount > 0);
    const total = positiveRows.reduce((sum, row) => sum + row.amount, 0);

    const palette = this.colors[this.themeService.theme()];

    return positiveRows.map((row, index) => ({
      ...row,
      percentage: (row.amount / total) * 100,
      color: palette[index % palette.length],
    }));
  });

  protected readonly chartTotal = computed(() =>
    this.chartSlices().reduce((sum, slice) => sum + slice.amount, 0),
  );

  protected readonly piePaths = computed(() => {
    const slices = this.chartSlices();
    const total = this.chartTotal();

    if (slices.length <= 1 || total === 0) {
      return [];
    }

    const center = 100;
    const radius = 96;

    let angle = -90;

    return slices.map((slice) => {
      const startAngle = angle;
      const sweep = (slice.amount / total) * 360;

      angle += sweep;

      const toRadians = (degrees: number) => (degrees * Math.PI) / 180;

      const startX = center + radius * Math.cos(toRadians(startAngle));
      const startY = center + radius * Math.sin(toRadians(startAngle));

      const endX = center + radius * Math.cos(toRadians(angle));
      const endY = center + radius * Math.sin(toRadians(angle));

      const largeArc = sweep > 180 ? 1 : 0;

      return {
        key: slice.key,
        color: slice.color,
        path: `
        M ${center} ${center}
        L ${startX} ${startY}
        A ${radius} ${radius} 0 ${largeArc} 1 ${endX} ${endY}
        Z
      `,
      };
    });
  });

  protected readonly filterError = computed(() => {
    if (!this.from() || !this.to()) {
      return 'Both dates are required.';
    }

    if (this.from() > this.to()) {
      return 'From date cannot be after To date.';
    }

    const ranges = [
      ['Transaction', this.minAmountInput(), this.maxAmountInput()],
      ['Split', this.minSplitAmountInput(), this.maxSplitAmountInput()],
    ] as const;

    for (const [label, minInput, maxInput] of ranges) {
      const min = this.optionalAmount(minInput);
      const max = this.optionalAmount(maxInput);

      if (
        (min !== undefined && (!Number.isFinite(min) || min < 0)) ||
        (max !== undefined && (!Number.isFinite(max) || max < 0))
      ) {
        return `${label} amounts must be valid non-negative numbers.`;
      }

      if (min !== undefined && max !== undefined && min > max) {
        return `Minimum ${label.toLowerCase()} amount cannot exceed maximum.`;
      }
    }

    return '';
  });

  private breakdownRequest?: Subscription;

  private readonly breakdownPeriodKey = signal('');

  protected readonly breakdownData = signal<BreakdownData | null>(null);
  protected readonly breakdownLoading = signal(false);
  protected readonly breakdownError = signal('');

  protected readonly expandedCategoryIds = signal<string[]>([]);

  ngOnInit(): void {
    this.categoryService.getCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
      },
      error: () => {
        this.categoryLoadError.set('Failed to load categories.');
      },
    });

    this.applyFilters();
  }

  ngOnDestroy(): void {
    this.request?.unsubscribe();
    this.breakdownRequest?.unsubscribe();
  }

  protected applyFilters(): void {
    if (this.filterError()) {
      return;
    }

    const filters: ReportFilters = {
      from: this.from(),
      to: this.to(),
    };

    if (this.selectedCategoryIds().length > 0) {
      filters.categoryIds = [...this.selectedCategoryIds()];
    }

    const name = this.subcategoryName().trim();

    if (name) {
      filters.subcategoryName = name;
    }

    const amountFilters = {
      minAmount: this.optionalAmount(this.minAmountInput()),
      maxAmount: this.optionalAmount(this.maxAmountInput()),
      minSplitAmount: this.optionalAmount(this.minSplitAmountInput()),
      maxSplitAmount: this.optionalAmount(this.maxSplitAmountInput()),
    };

    for (const [key, value] of Object.entries(amountFilters)) {
      if (value !== undefined) {
        filters[key as keyof typeof amountFilters] = value;
      }
    }

    this.appliedFilters.set(filters);
    this.loadReport(filters);

    const periodKey = `${filters.from}|${filters.to}`;

    if (this.breakdownPeriodKey() !== periodKey) {
      this.loadBreakdown(filters.from, filters.to);
    }
  }

  protected changeGroupBy(value: string): void {
    this.groupBy.set(value as ReportGroupBy);
    this.reloadReport();
  }

  protected changeTransactionType(value: string): void {
    this.selectedType.set(value as ReportTransactionType);
    this.reloadReport();
  }

  private reloadReport(): void {
    const filters = this.appliedFilters();

    if (filters) {
      this.loadReport(filters);
    }
  }

  private loadReport(filters: ReportFilters): void {
    this.request?.unsubscribe();

    this.loading.set(true);
    this.error.set('');

    this.periodTotal.set(null);
    this.categoryTotals.set([]);
    this.subcategoryTotals.set([]);

    const groupBy = this.groupBy();
    const type = this.selectedType();

    let detailsRequest: Observable<CategoryTotal[] | SubcategoryTotal[]> = of([]);

    if (groupBy === 'CATEGORY') {
      detailsRequest = this.reportService.getCategoryTotals(type, filters);
    } else if (groupBy === 'SUBCATEGORY') {
      detailsRequest = this.reportService.getSubcategoryTotals(type, filters);
    }

    this.request = forkJoin({
      period: this.reportService.getPeriodTotal(filters),
      details: detailsRequest,
    }).subscribe({
      next: ({ period, details }) => {
        this.periodTotal.set(period);

        if (groupBy === 'CATEGORY') {
          this.categoryTotals.set(details as CategoryTotal[]);
        } else if (groupBy === 'SUBCATEGORY') {
          this.subcategoryTotals.set(details as SubcategoryTotal[]);
        }

        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load financial reports.');
        this.loading.set(false);
      },
    });
  }

  protected formatCurrency(amount: number): string {
    return this.currencyFormatter.format(amount);
  }

  protected formatPercentage(value: number): string {
    return `${this.percentageFormatter.format(value)}%`;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  private optionalAmount(value: string): number | undefined {
    return value.trim() === '' ? undefined : Number(value);
  }

  protected toggleCategory(categoryId: string, checked: boolean): void {
    this.selectedCategoryIds.update((ids) =>
      checked ? [...ids, categoryId] : ids.filter((id) => id !== categoryId),
    );
  }

  protected clearAdvancedFilters(): void {
    this.selectedCategoryIds.set([]);
    this.subcategoryName.set('');

    this.minAmountInput.set('');
    this.maxAmountInput.set('');

    this.minSplitAmountInput.set('');
    this.maxSplitAmountInput.set('');

    this.applyFilters();
  }

  protected readonly breakdownRows = computed<BreakdownCategoryRow[]>(() => {
    const data = this.breakdownData();

    if (!data) {
      return [];
    }

    const categories = new Map<string, BreakdownCategoryRow>();

    const getCategory = (id: string, name: string): BreakdownCategoryRow => {
      let category = categories.get(id);

      if (!category) {
        category = {
          id,
          name,
          income: 0,
          expense: 0,
          subcategories: [],
        };

        categories.set(id, category);
      }

      return category;
    };

    for (const category of data.incomeCategories) {
      getCategory(category.categoryId, category.categoryName).income = category.total;
    }

    for (const category of data.expenseCategories) {
      getCategory(category.categoryId, category.categoryName).expense = category.total;
    }

    const addSubcategory = (subcategory: SubcategoryTotal, type: ReportTransactionType): void => {
      const category = getCategory(subcategory.categoryId, subcategory.categoryName);

      let row = category.subcategories.find((item) => item.id === subcategory.subcategoryId);

      if (!row) {
        row = {
          id: subcategory.subcategoryId,
          name: subcategory.subcategoryName,
          income: 0,
          expense: 0,
          isUnallocated: false,
        };

        category.subcategories.push(row);
      }

      if (type === 'INCOME') {
        row.income += subcategory.total;
      } else {
        row.expense += subcategory.total;
      }
    };

    for (const subcategory of data.incomeSubcategories) {
      addSubcategory(subcategory, 'INCOME');
    }

    for (const subcategory of data.expenseSubcategories) {
      addSubcategory(subcategory, 'EXPENSE');
    }

    for (const category of categories.values()) {
      category.subcategories.sort((a, b) => a.name.localeCompare(b.name));

      const allocatedIncome = category.subcategories.reduce(
        (sum, row) => sum + Math.round(row.income * 100),
        0,
      );

      const allocatedExpense = category.subcategories.reduce(
        (sum, row) => sum + Math.round(row.expense * 100),
        0,
      );

      const unallocatedIncome = Math.max(0, Math.round(category.income * 100) - allocatedIncome);

      const unallocatedExpense = Math.max(0, Math.round(category.expense * 100) - allocatedExpense);

      if (unallocatedIncome > 0 || unallocatedExpense > 0) {
        category.subcategories.push({
          id: `${category.id}-unallocated`,
          name: 'Unallocated',
          income: unallocatedIncome / 100,
          expense: unallocatedExpense / 100,
          isUnallocated: true,
        });
      }
    }

    return [...categories.values()].sort((a, b) => a.name.localeCompare(b.name));
  });

  protected readonly breakdownTotals = computed(() => {
    const rows = this.breakdownRows();

    const income = rows.reduce((sum, row) => sum + Math.round(row.income * 100), 0);

    const expense = rows.reduce((sum, row) => sum + Math.round(row.expense * 100), 0);

    return {
      income: income / 100,
      expense: expense / 100,
      net: (income - expense) / 100,
    };
  });

  private loadBreakdown(from: string, to: string): void {
    this.breakdownRequest?.unsubscribe();

    this.breakdownPeriodKey.set(`${from}|${to}`);
    this.breakdownLoading.set(true);
    this.breakdownError.set('');
    this.breakdownData.set(null);

    const filters: ReportFilters = { from, to };

    this.breakdownRequest = forkJoin({
      incomeCategories: this.reportService.getCategoryTotals('INCOME', filters),
      expenseCategories: this.reportService.getCategoryTotals('EXPENSE', filters),
      incomeSubcategories: this.reportService.getSubcategoryTotals('INCOME', filters),
      expenseSubcategories: this.reportService.getSubcategoryTotals('EXPENSE', filters),
    }).subscribe({
      next: (data) => {
        this.breakdownData.set(data);
        this.breakdownLoading.set(false);
      },
      error: () => {
        this.breakdownPeriodKey.set('');
        this.breakdownError.set('Failed to load financial breakdown.');
        this.breakdownLoading.set(false);
      },
    });
  }

  protected toggleBreakdownCategory(categoryId: string): void {
    this.expandedCategoryIds.update((ids) =>
      ids.includes(categoryId) ? ids.filter((id) => id !== categoryId) : [...ids, categoryId],
    );
  }
}
