import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { CategoryService } from './category.service';

describe('CategoryService', () => {
  let service: CategoryService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CategoryService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(CategoryService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should get categories', () => {
    const expectedCategories = [
      {
        id: '1',
        name: 'Groceries',
        description: 'General purchases',
        createdAt: '2026-10-31T10:00:00Z',
      },
    ];

    service.getCategories().subscribe((categories) => {
      expect(categories).toEqual(expectedCategories);
    });

    const request = httpTesting.expectOne(`${environment.apiUrl}/categories`);

    expect(request.request.method).toBe('GET');

    request.flush(expectedCategories);
  });

  it('should create a category', () => {
    const createdCategory = {
      id: '1',
      name: 'Groceries',
      description: 'General purchases',
      createdAt: '2026-10-31T18:00:00Z',
    };

    service
      .createCategory({
        name: 'Groceries',
        description: 'General purchases',
      })
      .subscribe((category) => {
        expect(category).toEqual(createdCategory);
      });

    const request = httpTesting.expectOne(`${environment.apiUrl}/categories`);

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      name: 'Groceries',
      description: 'General purchases',
    });

    request.flush(createdCategory);
  });

  it('should update a category', () => {
    const updatedCategory = {
      id: '1',
      name: 'Food',
      description: 'Food purchases',
      createdAt: '2026-10-31T18:00:00Z',
    };

    service
      .updateCategory('1', {
        name: 'Food',
        description: 'Food purchases',
      })
      .subscribe((category) => {
        expect(category).toEqual(updatedCategory);
      });

    const request = httpTesting.expectOne(`${environment.apiUrl}/categories/1`);

    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({
      name: 'Food',
      description: 'Food purchases',
    });

    request.flush(updatedCategory);
  });

  it('should delete a category', () => {
    service.deleteCategory('1').subscribe();

    const request = httpTesting.expectOne(`${environment.apiUrl}/categories/1`);

    expect(request.request.method).toBe('DELETE');

    request.flush(null);
  });
});
