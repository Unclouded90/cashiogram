import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { Card } from '../../../shared/card/card';
import { SettingsPage } from '../../../shared/settings-page/settings-page';
import { Subcategory } from '../../subcategories/subcategory.model';
import { SubcategoryService } from '../../subcategories/subcategory.service';
import { Category } from '../category.model';
import { CategoryService } from '../category.service';

@Component({
  selector: 'app-category-details',
  imports: [Card, SettingsPage],
  templateUrl: './category-details.html',
  styleUrl: './category-details.scss',
})
export class CategoryDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly categoryService = inject(CategoryService);
  private readonly subcategoryService = inject(SubcategoryService);

  protected readonly category = signal<Category | null>(null);
  protected readonly subcategories = signal<Subcategory[]>([]);

  protected readonly editingCategory = signal(false);
  protected readonly addingSubcategory = signal(false);
  protected readonly editingSubcategoryId = signal<string | null>(null);

  ngOnInit(): void {
    const categoryId = this.route.snapshot.paramMap.get('categoryId');

    if (!categoryId) {
      return;
    }

    this.categoryService.getCategory(categoryId).subscribe({
      next: (category: Category) => {
        this.category.set(category);
      },
    });

    this.subcategoryService.getSubcategoriesByCategory(categoryId).subscribe({
      next: (subcategories: Subcategory[]) => {
        this.subcategories.set(subcategories);
      },
    });
  }

  protected goBackToCategories(): void {
    this.router.navigate(['/settings/categories']);
  }

  protected editCategory(): void {
    this.editingCategory.set(true);
  }

  protected cancelCategoryEdit(): void {
    this.editingCategory.set(false);
  }

  protected updateCategory(nameInput: HTMLInputElement, descriptionInput: HTMLInputElement): void {
    const categoryId = this.category()?.id;

    if (!categoryId) {
      return;
    }

    this.categoryService
      .updateCategory(categoryId, {
        name: nameInput.value,
        description: descriptionInput.value,
      })
      .subscribe({
        next: (updatedCategory: Category) => {
          this.category.set(updatedCategory);
          this.editingCategory.set(false);
        },
      });
  }

  protected deleteCategory(): void {
    const categoryId = this.category()?.id;

    if (!categoryId) {
      return;
    }

    const confirmed = window.confirm('Are you sure you want to delete this category?');

    if (!confirmed) {
      return;
    }

    this.categoryService.deleteCategory(categoryId).subscribe({
      next: () => {
        this.router.navigate(['/settings/categories']);
      },
      error: (error) => {
        window.alert(error.error?.message || 'Unable to delete this category.');
      },
    });
  }

  protected showSubcategoryForm(): void {
    if (this.editingSubcategoryId()) {
      const confirmed = window.confirm(
        'You have unsaved changes. If you continue, they will be lost. Do you want to add a new subcategory anyway?',
      );

      if (!confirmed) {
        return;
      }

      this.editingSubcategoryId.set(null);
    }

    this.addingSubcategory.set(true);
  }

  protected cancelSubcategoryForm(): void {
    this.addingSubcategory.set(false);
  }

  protected createSubcategory(
    nameInput: HTMLInputElement,
    descriptionInput: HTMLInputElement,
  ): void {
    const categoryId = this.category()?.id;

    if (!categoryId) {
      return;
    }

    this.subcategoryService
      .createSubcategory(categoryId, {
        name: nameInput.value,
        description: descriptionInput.value || null,
      })
      .subscribe({
        next: (subcategory: Subcategory) => {
          this.subcategories.update((subcategories) => [...subcategories, subcategory]);

          this.addingSubcategory.set(false);
        },
      });
  }

  protected editSubcategory(subcategoryId: string): void {
    if (this.addingSubcategory()) {
      const confirmed = window.confirm(
        'You have an unsaved new subcategory. If you continue, it will be lost. Do you want to edit this subcategory anyway?',
      );

      if (!confirmed) {
        return;
      }

      this.addingSubcategory.set(false);
    }

    this.editingSubcategoryId.set(subcategoryId);
  }

  protected cancelSubcategoryEdit(): void {
    this.editingSubcategoryId.set(null);
  }

  protected updateSubcategory(
    subcategoryId: string,
    nameInput: HTMLInputElement,
    descriptionInput: HTMLInputElement,
  ): void {
    this.subcategoryService
      .updateSubcategory(subcategoryId, {
        name: nameInput.value,
        description: descriptionInput.value,
      })
      .subscribe({
        next: (updatedSubcategory: Subcategory) => {
          this.subcategories.update((subcategories) =>
            subcategories.map((subcategory) =>
              subcategory.id === updatedSubcategory.id ? updatedSubcategory : subcategory,
            ),
          );

          this.editingSubcategoryId.set(null);
        },
      });
  }

  protected deleteSubcategory(subcategoryId: string): void {
    const confirmed = window.confirm('Are you sure you want to delete this subcategory?');

    if (!confirmed) {
      return;
    }

    this.subcategoryService.deleteSubcategory(subcategoryId).subscribe({
      next: () => {
        this.subcategories.update((subcategories) =>
          subcategories.filter((subcategory) => subcategory.id !== subcategoryId),
        );

        this.editingSubcategoryId.set(null);
      },
      error: (error) => {
        window.alert(error.error?.message || 'Unable to delete this subcategory.');
      },
    });
  }
}
