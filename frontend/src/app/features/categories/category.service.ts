import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Category, CreateCategory, UpdateCategory } from './category.model';

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private readonly http = inject(HttpClient);

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${environment.apiUrl}/categories`);
  }

  createCategory(category: CreateCategory): Observable<Category> {
    return this.http.post<Category>(`${environment.apiUrl}/categories`, category);
  }

  updateCategory(categoryId: string, category: UpdateCategory): Observable<Category> {
    return this.http.patch<Category>(`${environment.apiUrl}/categories/${categoryId}`, category);
  }

  deleteCategory(categoryId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/categories/${categoryId}`);
  }
}
