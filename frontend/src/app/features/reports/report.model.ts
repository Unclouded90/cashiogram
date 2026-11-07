export type ReportTransactionType = 'INCOME' | 'EXPENSE';

export interface ReportFilters {
  from: string;
  to: string;
  categoryIds?: string[];
  subcategoryName?: string;
  minAmount?: number;
  maxAmount?: number;
  minSplitAmount?: number;
  maxSplitAmount?: number;
}

export interface PeriodTotal {
  from: string;
  to: string;
  totalIncome: number;
  totalExpenses: number;
  netTotal: number;
}

export interface CategoryTotal {
  categoryId: string;
  categoryName: string;
  total: number;
}

export interface SubcategoryTotal {
  subcategoryId: string;
  subcategoryName: string;
  categoryId: string;
  categoryName: string;
  total: number;
}
