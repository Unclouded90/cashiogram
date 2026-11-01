import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { Categories } from './categories';
import { CategoryService } from './category.service';

describe('Categories', () => {
  let component: Categories;
  let fixture: ComponentFixture<Categories>;

  const categories = [
    {
      id: '1',
      name: 'Groceries',
      description: 'General purchases',
      createdAt: '2026-10-31T18:00:00Z',
    },
    {
      id: '2',
      name: 'Cats',
      description: 'Food and litter',
      createdAt: '2026-10-31T18:05:00Z',
    },
  ];

  const categoryServiceMock = {
    getCategories: vi.fn(),
    createCategory: vi.fn(),
    updateCategory: vi.fn(),
    deleteCategory: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    categoryServiceMock.getCategories.mockReturnValue(of(categories));

    await TestBed.configureTestingModule({
      imports: [Categories],
      providers: [
        {
          provide: CategoryService,
          useValue: categoryServiceMock,
        },
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Categories);
    component = fixture.componentInstance;

    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load and display categories', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    expect(categoryServiceMock.getCategories).toHaveBeenCalled();

    expect(compiled.textContent).toContain('Groceries');
    expect(compiled.textContent).toContain('General purchases');
    expect(compiled.textContent).toContain('Cats');
    expect(compiled.textContent).toContain('Food and litter');
  });

  it('should create a category and clear the inputs', async () => {
    const createdCategory = {
      id: '3',
      name: 'Transport',
      description: 'Fuel and tolls',
      createdAt: '2026-10-31T18:10:00Z',
    };

    categoryServiceMock.createCategory.mockReturnValue(of(createdCategory));

    const compiled = fixture.nativeElement as HTMLElement;

    const nameInput = compiled.querySelector('input[placeholder="Name"]') as HTMLInputElement;

    const descriptionInput = compiled.querySelector(
      'input[placeholder="Description"]',
    ) as HTMLInputElement;

    const addButton = Array.from(compiled.querySelectorAll('button')).find((button) =>
      button.textContent?.includes('Add Category'),
    );

    nameInput.value = 'Transport';
    descriptionInput.value = 'Fuel and tolls';

    addButton?.click();

    await fixture.whenStable();

    expect(categoryServiceMock.createCategory).toHaveBeenCalledWith({
      name: 'Transport',
      description: 'Fuel and tolls',
    });

    expect(nameInput.value).toBe('');
    expect(descriptionInput.value).toBe('');

    expect(compiled.textContent).toContain('Transport');
    expect(compiled.textContent).toContain('Fuel and tolls');
  });
});
