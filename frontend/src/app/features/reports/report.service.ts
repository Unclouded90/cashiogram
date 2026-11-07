import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CategoryTotal,
  PeriodTotal,
  ReportFilters,
  ReportTransactionType,
  SubcategoryTotal,
} from './report.model';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiUrl}/reports`;

  getPeriodTotal(filters: ReportFilters): Observable<PeriodTotal> {
    return this.http.get<PeriodTotal>(`${this.apiUrl}/period`, {
      params: this.buildParams(filters),
    });
  }

  getCategoryTotals(
    type: ReportTransactionType,
    filters: ReportFilters,
  ): Observable<CategoryTotal[]> {
    return this.http.get<CategoryTotal[]>(`${this.apiUrl}/categories`, {
      params: this.buildParams(filters).set('type', type),
    });
  }

  getSubcategoryTotals(
    type: ReportTransactionType,
    filters: ReportFilters,
  ): Observable<SubcategoryTotal[]> {
    return this.http.get<SubcategoryTotal[]>(`${this.apiUrl}/subcategories`, {
      params: this.buildParams(filters).set('type', type),
    });
  }

  private buildParams(filters: ReportFilters): HttpParams {
    let params = new HttpParams().set('from', filters.from).set('to', filters.to);

    for (const categoryId of filters.categoryIds ?? []) {
      params = params.append('categoryIds', categoryId);
    }

    if (filters.subcategoryName?.trim()) {
      params = params.set('subcategoryName', filters.subcategoryName.trim());
    }

    const amountFilters = ['minAmount', 'maxAmount', 'minSplitAmount', 'maxSplitAmount'] as const;

    for (const key of amountFilters) {
      const value = filters[key];

      if (value !== undefined) {
        params = params.set(key, value.toString());
      }
    }

    return params;
  }
}
