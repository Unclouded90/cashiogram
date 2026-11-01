import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { CategoryService } from '../category.service';
import { SubcategoryService } from '../../subcategories/subcategory.service';
import { CategoryDetails } from './category-details';

describe('CategoryDetails', () => {
  let component: CategoryDetails;
  let fixture: ComponentFixture<CategoryDetails>;

  const category = {
    id: 'category-1',
    name: 'Groceries',
    description: 'General purchases',
    createdAt: '2026-11-01T15:00:00Z',
  };

  const subcategories = [
    {
      id: 'subcategory-1',
      name: 'Food',
      description: 'Meals and supermarket',
      categoryId: 'category-1',
      createdAt: '2026-11-01T15:10:00Z',
    },
    {
      id: 'subcategory-2',
      name: 'Cat Products',
      description: 'Food and litter',
      categoryId: 'category-1',
      createdAt: '2026-11-01T15:15:00Z',
    },
  ];

  const categoryServiceMock = {
    getCategory: vi.fn(),
    updateCategory: vi.fn(),
    deleteCategory: vi.fn(),
  };

  const subcategoryServiceMock = {
    getSubcategoriesByCategory: vi.fn(),
    createSubcategory: vi.fn(),
    updateSubcategory: vi.fn(),
    deleteSubcategory: vi.fn(),
  };

  const routerMock = {
    navigate: vi.fn(),
  };

  const activatedRouteMock = {
    snapshot: {
      paramMap: {
        get: vi.fn().mockReturnValue('category-1'),
      },
    },
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    activatedRouteMock.snapshot.paramMap.get.mockReturnValue('category-1');

    categoryServiceMock.getCategory.mockReturnValue(of(category));

    subcategoryServiceMock.getSubcategoriesByCategory.mockReturnValue(of(subcategories));

    await TestBed.configureTestingModule({
      imports: [CategoryDetails],
      providers: [
        {
          provide: CategoryService,
          useValue: categoryServiceMock,
        },
        {
          provide: SubcategoryService,
          useValue: subcategoryServiceMock,
        },
        {
          provide: ActivatedRoute,
          useValue: activatedRouteMock,
        },
        {
          provide: Router,
          useValue: routerMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoryDetails);
    component = fixture.componentInstance;

    fixture.detectChanges();

    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load and display the category and its subcategories', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    expect(activatedRouteMock.snapshot.paramMap.get).toHaveBeenCalledWith('categoryId');

    expect(categoryServiceMock.getCategory).toHaveBeenCalledWith('category-1');

    expect(subcategoryServiceMock.getSubcategoriesByCategory).toHaveBeenCalledWith('category-1');

    expect(compiled.textContent).toContain('Groceries');
    expect(compiled.textContent).toContain('General purchases');

    expect(compiled.textContent).toContain('Food');
    expect(compiled.textContent).toContain('Meals and supermarket');

    expect(compiled.textContent).toContain('Cat Products');
    expect(compiled.textContent).toContain('Food and litter');
  });

  it('should navigate back to categories', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    const backButton = compiled.querySelector('.back-button') as HTMLButtonElement;

    backButton.click();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/settings/categories']);
  });

  it('should edit and update the category', async () => {
    const updatedCategory = {
      ...category,
      name: 'Food & Groceries',
      description: 'Updated description',
    };

    categoryServiceMock.updateCategory.mockReturnValue(of(updatedCategory));

    const compiled = fixture.nativeElement as HTMLElement;

    const editButton = Array.from(compiled.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === 'Edit Category',
    );

    editButton?.click();

    fixture.detectChanges();

    const nameInput = compiled.querySelector(
      '.category-edit input:first-child',
    ) as HTMLInputElement;

    const descriptionInput = compiled.querySelector(
      '.category-edit input:nth-child(2)',
    ) as HTMLInputElement;

    expect(nameInput.value).toBe('Groceries');
    expect(descriptionInput.value).toBe('General purchases');

    nameInput.value = 'Food & Groceries';
    descriptionInput.value = 'Updated description';

    const saveButton = Array.from(
      compiled.querySelectorAll<HTMLButtonElement>('.category-edit-actions button'),
    ).find((button) => button.textContent?.trim() === 'Save');

    saveButton?.click();

    await fixture.whenStable();
    fixture.detectChanges();

    expect(categoryServiceMock.updateCategory).toHaveBeenCalledWith('category-1', {
      name: 'Food & Groceries',
      description: 'Updated description',
    });

    expect(compiled.textContent).toContain('Food & Groceries');
    expect(compiled.textContent).toContain('Updated description');
  });

  it('should delete the category and navigate back to categories', () => {
    categoryServiceMock.deleteCategory.mockReturnValue(of(undefined));

    vi.spyOn(window, 'confirm').mockReturnValue(true);

    const compiled = fixture.nativeElement as HTMLElement;

    const deleteButton = Array.from(compiled.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === 'Delete Category',
    );

    deleteButton?.click();

    expect(window.confirm).toHaveBeenCalled();

    expect(categoryServiceMock.deleteCategory).toHaveBeenCalledWith('category-1');

    expect(routerMock.navigate).toHaveBeenCalledWith(['/settings/categories']);
  });

  it('should not delete the category when confirmation is cancelled', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    const compiled = fixture.nativeElement as HTMLElement;

    const deleteButton = Array.from(compiled.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === 'Delete Category',
    );

    deleteButton?.click();

    expect(categoryServiceMock.deleteCategory).not.toHaveBeenCalled();

    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('should create a subcategory', async () => {
    const createdSubcategory = {
      id: 'subcategory-3',
      name: 'Household',
      description: 'Cleaning products',
      categoryId: 'category-1',
      createdAt: '2026-11-01T16:00:00Z',
    };

    subcategoryServiceMock.createSubcategory.mockReturnValue(of(createdSubcategory));

    const compiled = fixture.nativeElement as HTMLElement;

    const addButton = Array.from(compiled.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.includes('Add Subcategory'),
    );

    addButton?.click();

    fixture.detectChanges();

    const form = compiled.querySelector('.subcategory-form-card') as HTMLElement;

    const nameInput = form.querySelector('input[placeholder="Name"]') as HTMLInputElement;

    const descriptionInput = form.querySelector(
      'input[placeholder="Description"]',
    ) as HTMLInputElement;

    nameInput.value = 'Household';
    descriptionInput.value = 'Cleaning products';

    const saveButton = Array.from(form.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === 'Save',
    );

    saveButton?.click();

    await fixture.whenStable();
    fixture.detectChanges();

    expect(subcategoryServiceMock.createSubcategory).toHaveBeenCalledWith('category-1', {
      name: 'Household',
      description: 'Cleaning products',
    });

    expect(compiled.textContent).toContain('Household');
    expect(compiled.textContent).toContain('Cleaning products');
  });

  it('should edit and update a subcategory', async () => {
    const updatedSubcategory = {
      ...subcategories[0],
      name: 'Supermarket',
      description: 'Food and general groceries',
    };

    subcategoryServiceMock.updateSubcategory.mockReturnValue(of(updatedSubcategory));

    const compiled = fixture.nativeElement as HTMLElement;

    const subcategoryCard = Array.from(compiled.querySelectorAll('.subcategory-card')).find(
      (card) => card.textContent?.includes('Food'),
    ) as HTMLButtonElement;

    subcategoryCard.click();

    fixture.detectChanges();

    const form = compiled.querySelector('.subcategory-form-card') as HTMLElement;

    const inputs = form.querySelectorAll('input');

    const nameInput = inputs[0] as HTMLInputElement;
    const descriptionInput = inputs[1] as HTMLInputElement;

    expect(nameInput.value).toBe('Food');
    expect(descriptionInput.value).toBe('Meals and supermarket');

    nameInput.value = 'Supermarket';
    descriptionInput.value = 'Food and general groceries';

    const saveButton = Array.from(form.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === 'Save',
    );

    saveButton?.click();

    await fixture.whenStable();
    fixture.detectChanges();

    expect(subcategoryServiceMock.updateSubcategory).toHaveBeenCalledWith('subcategory-1', {
      name: 'Supermarket',
      description: 'Food and general groceries',
    });

    expect(compiled.textContent).toContain('Supermarket');
    expect(compiled.textContent).toContain('Food and general groceries');
  });

  it('should delete a subcategory', async () => {
    subcategoryServiceMock.deleteSubcategory.mockReturnValue(of(undefined));

    vi.spyOn(window, 'confirm').mockReturnValue(true);

    const compiled = fixture.nativeElement as HTMLElement;

    const subcategoryCard = Array.from(compiled.querySelectorAll('.subcategory-card')).find(
      (card) => card.textContent?.includes('Food'),
    ) as HTMLButtonElement;

    subcategoryCard.click();

    fixture.detectChanges();

    const form = compiled.querySelector('.subcategory-form-card') as HTMLElement;

    const deleteButton = Array.from(form.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === 'Delete',
    );

    deleteButton?.click();

    await fixture.whenStable();
    fixture.detectChanges();

    expect(subcategoryServiceMock.deleteSubcategory).toHaveBeenCalledWith('subcategory-1');

    expect(compiled.textContent).not.toContain('Meals and supermarket');

    expect(compiled.textContent).toContain('Cat Products');
  });

  it('should keep editing a subcategory when adding another one is cancelled', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    const compiled = fixture.nativeElement as HTMLElement;

    const subcategoryCard = Array.from(compiled.querySelectorAll('.subcategory-card')).find(
      (card) => card.textContent?.includes('Food'),
    ) as HTMLButtonElement;

    subcategoryCard.click();

    fixture.detectChanges();

    const addButton = Array.from(compiled.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.includes('Add Subcategory'),
    );

    addButton?.click();

    fixture.detectChanges();

    expect(window.confirm).toHaveBeenCalled();

    expect(compiled.querySelector('.subcategory-form-card')).toBeTruthy();
  });

  it('should keep the new subcategory form when editing an existing one is cancelled', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    const compiled = fixture.nativeElement as HTMLElement;

    const addButton = Array.from(compiled.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.includes('Add Subcategory'),
    );

    addButton?.click();

    fixture.detectChanges();

    const subcategoryCard = Array.from(compiled.querySelectorAll('.subcategory-card')).find(
      (card) => card.textContent?.includes('Food'),
    ) as HTMLButtonElement;

    subcategoryCard.click();

    fixture.detectChanges();

    expect(window.confirm).toHaveBeenCalled();

    const form = compiled.querySelector('.subcategory-form-card');

    expect(form).toBeTruthy();

    const nameInput = form?.querySelector('input[placeholder="Name"]');

    expect(nameInput).toBeTruthy();
  });
});
