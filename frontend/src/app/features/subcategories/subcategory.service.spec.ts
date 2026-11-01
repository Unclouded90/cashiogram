import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { SubcategoryService } from './subcategory.service';

describe('SubcategoryService', () => {
  let service: SubcategoryService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SubcategoryService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(SubcategoryService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should get subcategories by category', () => {
    const subcategories = [
      {
        id: 'subcategory-1',
        name: 'Food',
        description: 'Meals and supermarket',
        categoryId: 'category-1',
        createdAt: '2026-11-01T15:00:00Z',
      },
    ];

    service.getSubcategoriesByCategory('category-1').subscribe((result) => {
      expect(result).toEqual(subcategories);
    });

    const request = httpTesting.expectOne(
      `${environment.apiUrl}/subcategories?categoryId=category-1`,
    );

    expect(request.request.method).toBe('GET');

    request.flush(subcategories);
  });

  it('should create a subcategory', () => {
    const createdSubcategory = {
      id: 'subcategory-1',
      name: 'Food',
      description: 'Meals and supermarket',
      categoryId: 'category-1',
      createdAt: '2026-11-01T15:00:00Z',
    };

    service
      .createSubcategory('category-1', {
        name: 'Food',
        description: 'Meals and supermarket',
      })
      .subscribe((result) => {
        expect(result).toEqual(createdSubcategory);
      });

    const request = httpTesting.expectOne(
      `${environment.apiUrl}/categories/category-1/subcategories`,
    );

    expect(request.request.method).toBe('POST');

    expect(request.request.body).toEqual({
      name: 'Food',
      description: 'Meals and supermarket',
    });

    request.flush(createdSubcategory);
  });

  it('should update a subcategory', () => {
    const updatedSubcategory = {
      id: 'subcategory-1',
      name: 'Food',
      description: 'Updated description',
      categoryId: 'category-1',
      createdAt: '2026-11-01T15:00:00Z',
    };

    service
      .updateSubcategory('subcategory-1', {
        description: 'Updated description',
      })
      .subscribe((result) => {
        expect(result).toEqual(updatedSubcategory);
      });

    const request = httpTesting.expectOne(`${environment.apiUrl}/subcategories/subcategory-1`);

    expect(request.request.method).toBe('PATCH');

    expect(request.request.body).toEqual({
      description: 'Updated description',
    });

    request.flush(updatedSubcategory);
  });

  it('should delete a subcategory', () => {
    service.deleteSubcategory('subcategory-1').subscribe();

    const request = httpTesting.expectOne(`${environment.apiUrl}/subcategories/subcategory-1`);

    expect(request.request.method).toBe('DELETE');

    request.flush(null);
  });
});
