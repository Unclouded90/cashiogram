import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CreateSubcategory, Subcategory, UpdateSubcategory } from './subcategory.model';

@Injectable({
  providedIn: 'root',
})
export class SubcategoryService {
  private readonly http = inject(HttpClient);

  getSubcategories(): Observable<Subcategory[]> {
    return this.http.get<Subcategory[]>(`${environment.apiUrl}/subcategories`);
  }

  getSubcategoriesByCategory(categoryId: string): Observable<Subcategory[]> {
    return this.http.get<Subcategory[]>(`${environment.apiUrl}/subcategories`, {
      params: { categoryId },
    });
  }

  createSubcategory(categoryId: string, subcategory: CreateSubcategory): Observable<Subcategory> {
    return this.http.post<Subcategory>(
      `${environment.apiUrl}/categories/${categoryId}/subcategories`,
      subcategory,
    );
  }

  updateSubcategory(
    subcategoryId: string,
    subcategory: UpdateSubcategory,
  ): Observable<Subcategory> {
    return this.http.patch<Subcategory>(
      `${environment.apiUrl}/subcategories/${subcategoryId}`,
      subcategory,
    );
  }

  deleteSubcategory(subcategoryId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/subcategories/${subcategoryId}`);
  }
}
